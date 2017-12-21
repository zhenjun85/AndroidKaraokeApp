package com.trung.karaokeapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.TokenManager;
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.libffmpeg.ExecuteBinaryResponseHandler;
import com.trung.karaokeapp.libffmpeg.FFmpeg;
import com.trung.karaokeapp.libffmpeg.LoadBinaryResponseHandler;
import com.trung.karaokeapp.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.trung.karaokeapp.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;

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

    private String record;
    private String beat;
    private String lyric;
    private KaraokeSong songKar;
    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayerForRecord;

    boolean fromUser = false;
    private FFmpeg ffmpeg;
    private String mp3Record;

    TokenManager tokenManager;
    ApiService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_record);
        ButterKnife.bind(this);

        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);

        //get from Intent
        String songJson = getIntent().getStringExtra("song");
        songKar = new Gson().fromJson(songJson, KaraokeSong.class);
        record = getIntent().getStringExtra("record");
        beat = getIntent().getStringExtra("beat");
        lyric = getIntent().getStringExtra("lyric");

        toolbar.setTitle(songKar.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // //Load FFmpeg Library
        ffmpeg = FFmpeg.getInstance(this);
        loadFFMpegBinary();

        //set SongDetail to activity
        tvSongName.setText(songKar.getName());
        tvGenre.setText(String.format("Genre: %s", songKar.getGenre()));

        //region mediaaaa

        mediaPlayer = new MediaPlayer();
        mediaPlayerForRecord = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(beat);
            mediaPlayerForRecord.setDataSource(record);
            mediaPlayer.prepare();
            mediaPlayerForRecord.prepare();

            tvSongTime.setText(String.format("Time: %s", Utils.convertTimeMsToMMSS(mediaPlayerForRecord.getDuration())));
            tvDuration.setText(Utils.convertTimeMsToMMSS(mediaPlayerForRecord.getDuration()));
            seekBar.setMax(mediaPlayerForRecord.getDuration());

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b) {
                        mediaPlayerForRecord.seekTo(i);
                        mediaPlayer.seekTo(i);
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
                    playing = false;
                    mediaPlayer.seekTo(0);
                    mediaPlayer.pause();
                    mediaPlayerForRecord.seekTo(0);
                    mediaPlayerForRecord.pause();
                    handler.removeCallbacks(playRunable);

                    seekBar.setProgress(0);
                    tvCurrentTime.setText("00:00");
                    btnPlay.setImageDrawable(getDrawable(R.drawable.ic_play_button));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //endregion

    }

    @OnClick(R.id.btnPlay)
    void btnPlayRecord() {
        if (playing) {
            pauseMix();
        }else {
            startMix();
        }
        playing = !playing;
    }

    private void startMix() {
        btnPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_round));
        mediaPlayer.start();
        mediaPlayerForRecord.start();

        thread.run();
    }

    private void pauseMix() {
        btnPlay.setImageDrawable(getDrawable(R.drawable.ic_play_button));
        mediaPlayer.pause();
        mediaPlayerForRecord.pause();

        handler.removeCallbacks(playRunable);
    }

    //Playing
    boolean playing = false;
    Handler handler = new Handler();
    PlayRunable playRunable = new PlayRunable();
    Thread thread = new Thread(playRunable);

    class PlayRunable implements Runnable {
        @Override
        public void run() {
            int currentTime = mediaPlayerForRecord.getCurrentPosition();

            tvCurrentTime.setText(Utils.convertTimeMsToMMSS(currentTime));
            seekBar.setProgress(currentTime);

            handler.postDelayed(playRunable, 500);
        }
    }
    @OnClick(R.id.btnPostRecord)
    void postRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("abccc");
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

//        String cmd = "-y -i " + beat + " -i " + record + " -filter_complex amix=inputs=2:duration=shortest -ac 2 -c:a libmp3lame -q:a 2 " + output;
        String duration = tvDuration.getText().toString().replace(":", "-");
        mp3Record = record.substring(0, record.length() - 4) + "_10_" + duration +  ".mp3";
        ArrayList<String> comm = new ArrayList<>();
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
        comm.add(mp3Record);
        String[] command = comm.toArray(new String[comm.size()]);
        execFFmpegBinary(command, alertDialog, true);
    }

    @OnClick(R.id.btnSaveRecord)
    void saveRecord() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("abccc");
        builder.setCancelable(false);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

//        String cmd = "-y -i " + beat + " -i " + record + " -filter_complex amix=inputs=2:duration=shortest -ac 2 -c:a libmp3lame -q:a 2 " + output;
        String duration = tvDuration.getText().toString().replace(":", "-");
        mp3Record = record.substring(0, record.length() - 4) + "_10_" + duration +  ".mp3";
        ArrayList<String> comm = new ArrayList<>();
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
        comm.add(mp3Record);
        String[] command = comm.toArray(new String[comm.size()]);

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
        Call<Boolean> call = service.updateViewNo(songKar.getId());
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
                        intent.putExtra("record", mp3Record);
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
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        showCancelConfirmDialog();
    }
}
