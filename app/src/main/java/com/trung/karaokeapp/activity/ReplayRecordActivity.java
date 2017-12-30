package com.trung.karaokeapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.appclass.LyricFile;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.libffmpeg.ExecuteBinaryResponseHandler;
import com.trung.karaokeapp.libffmpeg.FFmpeg;
import com.trung.karaokeapp.libffmpeg.LoadBinaryResponseHandler;
import com.trung.karaokeapp.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.trung.karaokeapp.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.utils.MediaDecoder;
import com.trung.karaokeapp.utils.Utils;

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReplayRecordActivity extends AppCompatActivity {
    private static final String TAG = "ReplayRecordActivity";
    @BindView(R.id.tvSongName) TextView tvSongName;
    @BindView(R.id.tvSongTime) TextView tvSongTime;
    @BindView(R.id.tvGenre) TextView tvGenre;
    @BindView(R.id.tvCurrentTime) TextView tvCurrentTime;
    @BindView(R.id.tvDuration) TextView tvDuration;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.btnPlay) ImageButton btnPlay;
    @BindView(R.id.seekBarSong) SeekBar seekBar;
    @BindView(R.id.videoView) VideoView videoView;
    @BindView(R.id.tvScoreStatus) TextView tvScoreStatus;

    private String record;
    private String beat;
    private String lyric;
    private KaraokeSong karaokeSong;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerForRecord;
    private FFmpeg ffmpeg;
    private String output;
    private boolean isVideo = false;

    TokenManager tokenManager;
    ApiService service;
    private int scoreOfSong = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_record);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        //keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //get from Intent
        String songJson = getIntent().getStringExtra("song");
        karaokeSong = new Gson().fromJson(songJson, KaraokeSong.class);
        record = getIntent().getStringExtra("record");
        beat = getIntent().getStringExtra("beat");
        lyric = getIntent().getStringExtra("lyric");

        //videoView show or hide
        String exFile = record.substring( record.length() - 4 );
        Log.d( TAG, "__" + exFile );
        if (exFile.equals(".3gp")){
            videoView.setVisibility(View.GONE);
        }else {
            //video
            isVideo = true;
        }

        toolbar.setTitle(karaokeSong.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // //Load FFmpeg Library
        ffmpeg = FFmpeg.getInstance(this);
        loadFFMpegBinary();

        //set SongDetail to activity
        tvSongName.setText(karaokeSong.getName());
        tvGenre.setText(String.format("Genre: %s", karaokeSong.getGenre()));

        //media
        mediaPlayer = new MediaPlayer();
        mediaPlayerForRecord = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(beat);
            mediaPlayerForRecord.setDataSource(record);
            mediaPlayer.prepare();
            mediaPlayerForRecord.prepare();

            videoView.setVideoPath( record );

            tvSongTime.setText(String.format("Time: %s", Utils.convertTimeMsToMMSS(mediaPlayerForRecord.getDuration())));
            tvDuration.setText(Utils.convertTimeMsToMMSS(mediaPlayerForRecord.getDuration()));
            seekBar.setMax(mediaPlayerForRecord.getDuration());

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        mediaPlayerForRecord.seekTo(i);
                        mediaPlayer.seekTo(i);
                        videoView.seekTo(i);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            mediaPlayerForRecord.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer__) {
                    hasStopped = true;
                    playing = false;
                    mediaPlayer.pause();
                    mediaPlayerForRecord.pause();
                    videoView.pause();

                    handler.removeCallbacks(playRunable);
                    //UI
                    seekBar.setProgress(0);
                    tvCurrentTime.setText("00:00");
                    btnPlay.setImageDrawable(getDrawable(R.drawable.ic_play_button));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

        //score
        if (true) {
            new MediaDecodeAudio(this, record, lyric ).execute();
        }
    }

    public class MediaDecodeAudio extends AsyncTask<Void, Double, Void> {
        private Activity ctx;
        private String rawRecordPath;
        private String lyricPath;

        private int blocksize = 2048;
        private MediaDecoder mD;
        private DoubleFFT_1D fft;

        public MediaDecodeAudio (Activity ctx, String raw_record_path, String lyric) {
            this.ctx = ctx;
            this.rawRecordPath = raw_record_path;
            this.lyricPath = lyric;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "start scoring");
            tvScoreStatus.setText("Scoring...");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                mD = new MediaDecoder(rawRecordPath);
                float[] samples = mD.readShortData();
                boolean outIdxFlag = false;
                double sroceSum = 0;
                int numNote = 0;

                LyricFile lyricFile = Utils.openSongFile(lyricPath);

                for(int num=0; num<lyricFile.allLines.size(); num++)
                {
                    for(int j=0; j < lyricFile.allLines.get(num).notes.size(); j++) {
                        if(lyricFile.allLines.get(num).notes.get(j).noteType == 0) {
                            continue;
                        }
                        int a = lyricFile.allLines.get(num).notes.get(j).start;
                        int b = lyricFile.allLines.get(num).notes.get(j).length;
                        int p = lyricFile.allLines.get(num).notes.get(j).pitch;

                        int loopNum = (int) Math.ceil(b * 8 / 2048.0);
                        float[] noteFreq = new float[loopNum];

                        for (int temp = 0; temp < loopNum; temp++) {
                            double[] audioDataDoubles = new double[blocksize * 2];
                            double[] re = new double[blocksize];
                            double[] im = new double[blocksize];
                            double[] magnitude = new double[blocksize / 2];

                            //  Hann
                            double[] preRealData = new double[blocksize];
                            double PI = 3.14159265359;
                            for (int i = 0; i < blocksize; i++) {
                                if(samples.length <= a * 8 + temp * 2048 + i) { //FIXME:
                                    outIdxFlag = true;
                                    break;
                                }
                                double multiplier = 0.5 * (1 - Math.cos(2*PI*i/(blocksize-1)));
                                preRealData[i] = multiplier * samples[a * 8 + temp * 2048 + i];
                            }

                            fft = new DoubleFFT_1D(blocksize);

                            for (int i = 0; i < blocksize; i++) {
                                audioDataDoubles[2 * i] = preRealData[i];
                                audioDataDoubles[(2 * i) + 1] = 0.0;
                            }

                            if(outIdxFlag) {
                                break;
                            }

                            fft.complexForward(audioDataDoubles);

                            for (int i = 0; i < blocksize / 2; i++) {
                                double R = audioDataDoubles[2 * i];
                                double I = audioDataDoubles[2 * i + 1];

                                magnitude[i] = Math.sqrt(I*I + R*R);
                            }

                            double max_magnitude = Double.NEGATIVE_INFINITY;
                            int max_index = -1;
                            // Get the largest magnitude peak
                            for (int i = 0; i < blocksize / 2; i++) {
                                if (max_magnitude < magnitude[i]) {
                                    max_magnitude = magnitude[i];
                                    max_index = i;
                                }
                            }

                            noteFreq[temp] = (8000 * max_index) / blocksize;
                        }

                        if(outIdxFlag) {
                            break;
                        }

                        //Calc score
                        float sum = 0;
                        for (int it = 0; it < loopNum; it++) {
                            sum += noteFreq[it];
                        }
                        float avgFreq = sum / loopNum;
                        double score = (avgFreq > 0)?Utils.convertPitchToScore(avgFreq, p): 0;

                        sroceSum += score*lyricFile.allLines.get(num).notes.get(j).noteType;
                        numNote += lyricFile.allLines.get(num).notes.get(j).noteType;
                    }

                    if(outIdxFlag) {
                        break;
                    }
                }
                publishProgress((numNote < 1) ? 0 : (sroceSum / numNote));
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(Double... frequencies) {
            super.onProgressUpdate(frequencies);
            double f = frequencies[0];
            tvScoreStatus.setText("Score: " + (int) Math.round(f * 10));
            scoreOfSong = (int) Math.round(f * 10);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d(TAG, "stop scoring");
        }
    }

    @OnClick(R.id.btnPlay)
    void btnPlayRecord() {
        if (playing) {
            pauseMix();
            videoView.pause();
        }else {
            if (hasStopped){
                mediaPlayer.seekTo(0);
                mediaPlayerForRecord.seekTo(0);
                videoView.seekTo(0);
            }
            startMix();
            videoView.start();
        }
        playing = !playing;
    }

    private void startMix() {
        btnPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_round));
        mediaPlayer.start();
        mediaPlayerForRecord.start();
        playRunable.run();
    }

    private void pauseMix() {
        btnPlay.setImageDrawable(getDrawable(R.drawable.ic_play_button));
        mediaPlayer.pause();
        mediaPlayerForRecord.pause();

        handler.removeCallbacks(playRunable);
    }

    //Playing
    boolean playing = false;
    boolean hasStopped = false;
    Handler handler = new Handler();
    PlayRunable playRunable = new PlayRunable();

    class PlayRunable implements Runnable {
        @Override
        public void run() {
            int currentTime = mediaPlayerForRecord.getCurrentPosition();

            tvCurrentTime.setText(Utils.convertTimeMsToMMSS(currentTime));
            seekBar.setProgress(currentTime);

            handler.postDelayed(playRunable, 500);
        }
    }

    String[] getCommand() {
        ArrayList<String> comm = null;
        //String cmd = "-y -i " + beat + " -i " + record + " -filter_complex amix=inputs=2:duration=shortest -ac 2 -c:a libmp3lame -q:a 2 " + output;
        String duration = tvDuration.getText().toString().replace(":", "-");
        if (!isVideo) {
            //audio 3gp
            output = record.substring(0, record.length() - 4) + "_" + scoreOfSong + "_" + duration +  ".mp3";
            comm = new ArrayList<>();
            comm.add("-y");
            comm.add("-i");
            comm.add(beat);
            comm.add("-i");
            comm.add(record);
            comm.add("-filter_complex");
            comm.add("amix=inputs=2:duration=shortest");
            comm.add("-ac");
            comm.add("2");
            comm.add("-c:a");
            comm.add("libmp3lame");
            comm.add("-q:a");
            comm.add("2");
            comm.add(output);
        }else {
            //video mp4
            output = record.substring(0, record.length() - 4) + "_" + scoreOfSong + "_" + duration +  ".mp4";
            comm = new ArrayList<>();
            comm.add("-y");
            comm.add("-i");
            comm.add(beat);
            comm.add("-i");
            comm.add(record);
            comm.add("-shortest");
            comm.add(output);
        }

        return comm.toArray(new String[comm.size()]);

    }

    @OnClick(R.id.btnPostRecord)
    void postRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Start merging beat and mic files...");
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        String[] command = getCommand();
        execFFmpegBinary(command, alertDialog, true);
    }

    @OnClick(R.id.btnSaveRecord)
    void saveRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Start merging beat and mic files...");
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        String[] command = getCommand();
        execFFmpegBinary(command, alertDialog, false);
    }
    //FFMPEG
    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.e(TAG, "Fail saving record");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
    private void execFFmpegBinary(final String[] command, final AlertDialog alertDialog, final boolean isPost) {
        //update viewNo
        Call<Boolean> call = service.upViewKs(karaokeSong.getId());
        call.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                Log.d(TAG, response.toString());
            }
            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Log.d(TAG, "Failure:" + t.getMessage());
            }
        });

        //process mixing mp3
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "fail convert record");
                    finish();
                }

                @Override
                public void onSuccess(String s) {
                    Toast.makeText(getBaseContext(),"Record has been saved.", Toast.LENGTH_LONG).show();
                    File file = new File(record);
                    if (file.exists()) {
                        file.delete();
                    }
                    alertDialog.dismiss();

                    if (!isPost) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ReplayRecordActivity.this);
                        builder.setMessage("Save completely, have fun!")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                });
                        final AlertDialog alertDialogConfirm = builder.create();
                        alertDialogConfirm.setOnShowListener(new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface dialogInterface) {
                                alertDialogConfirm.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                            }
                        });
                        alertDialogConfirm.show();
                    }else {
                        Intent intent = new Intent(ReplayRecordActivity.this, PostRecordActivity.class);
                        intent.putExtra("song", getIntent().getStringExtra("song"));
                        intent.putExtra("record", output);
                        intent.putExtra("isVideo", isVideo);
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onProgress(String s) {
                    alertDialog.setMessage( s );
                    Log.d(TAG, s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg ");
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg ");

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e("FFmpeg error", e.getLocalizedMessage());
        }
    }


    @OnClick(R.id.btnCancelRecord)
    void cancelRecord() {
        showCancelConfirmDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "-----");
                showCancelConfirmDialog();

                break;
            default:break;
        }

        return true;
    }
    void showCancelConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to discard this record?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "yes");
                        File file = new File(record);
                        if (file.exists()) {
                            file.delete();
                        }
                        dialogInterface.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
        ;
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            handler.removeCallbacks(playRunable);
        }
        mediaPlayer.release();
        mediaPlayerForRecord.release();
        if (videoView.isPlaying()){
            videoView.stopPlayback();
            videoView = null;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showCancelConfirmDialog();
    }
}
