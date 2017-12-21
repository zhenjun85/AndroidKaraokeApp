package com.trung.karaokeapp.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.Utils;
import com.trung.karaokeapp.appclass.Line;
import com.trung.karaokeapp.appclass.LyricFile;
import com.trung.karaokeapp.appclass.Note;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.viewmodel.RecordViewModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    //BindView
    @BindView(R.id.tvSongName)      TextView tvSongName;
    @BindView(R.id.tvSongTime)  TextView tvSongDuration;
    @BindView(R.id.tvGenre)         TextView tvGenre;
    @BindView(R.id.ivCoverSong)     CircleImageView ivCoverSong;
    @BindView(R.id.tvStatus)        TextView tvStatus;
    @BindView(R.id.btnRecordSong)   ImageButton btnRecordSong;
    @BindView(R.id.btnResetSong)   ImageButton btnResetSong;
    @BindView(R.id.tvCurrentTime)   TextView tvCurrentTime;

        //element for lyric scroll
    @BindView(R.id.scrollView)      ScrollView scrollView;
    @BindView(R.id.llLyricContainer) LinearLayout llLyricContainer;
    @BindView(R.id.progressBarTop)  ProgressBar progressBarTop;

    RecordViewModel recordViewModel;

    //for Playing
    String urlBeatOnDevice, urlLyricOnDevice, recordOnDevice;
    LyricFile lyricFile;
    private MediaPlayer mediaPlayer;
    private MediaRecorder mediaAudioRecorder;
    boolean playing = false;
    boolean hasStopped = false;

    Handler handler;
    PlayingRunable playingRunable;
    private KaraokeSong songKaraokeSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        ButterKnife.bind(this);

        //init
        recordViewModel = ViewModelProviders.of(RecordActivity.this).get(RecordViewModel.class);
        handler = new Handler();
        playingRunable = new PlayingRunable();

        //textStatus
//        tvStatus.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        //clear linear layout container
        llLyricContainer.removeAllViews();

        //change Color
        progressBarTop.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        //SongInfo Receive
        String songJsonText = getIntent().getStringExtra("song");
        songKaraokeSong = new Gson().fromJson(songJsonText, KaraokeSong.class);



        //update songInfo to activity
        tvSongName.setText(songKaraokeSong.getName());
        tvGenre.setText(String.format("Genre: %s", songKaraokeSong.getGenre()));
        //load Imagecover
        String folderSong =  AppURL.baseUrlSongAndLyric + "/" + songKaraokeSong.getBeat().substring(0, songKaraokeSong.getBeat().length() - 4);
        Glide.with(RecordActivity.this).load(folderSong + "/" + songKaraokeSong.getImage()).into(ivCoverSong);

        //media
        mediaPlayer = new MediaPlayer();
        mediaAudioRecorder = new MediaRecorder();
        mediaAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);


        //prepare url
        String urlBeat = folderSong + "/" + songKaraokeSong.getBeat();
        String urlLyric = folderSong + "/" + songKaraokeSong.getLyric();
        //1. Download Beat and Lyric
        new DownloadFileFromURL().execute(urlLyric, urlBeat);

        //isReadyObserver -> load data, and prepare recording
        recordViewModel.getIsReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    lyricFile = Utils.openSongFile(urlLyricOnDevice);
                    gap = lyricFile.gap;
                    lyricSize = lyricFile.allLines.size();

                    final String recordName = getRecordName(songKaraokeSong);
                    String pathRecord = Environment.getExternalStorageDirectory() + "/" + AppURL.baseRecordsFolder;

                    File file = new File(pathRecord);
                    if (!file.exists()){
                        if (!file.mkdirs()){
                            Log.e(TAG, "Record hasn't been created.");
                            recordViewModel.getIsReady().setValue(false);
                            return;
                        }
                    }
                    recordOnDevice = pathRecord + "/" + recordName;
                    mediaAudioRecorder.setOutputFile( recordOnDevice );
                    try {
                        mediaPlayer.setDataSource(urlBeatOnDevice);
                        mediaPlayer.prepare();
                        mediaAudioRecorder.prepare();
                        //set Duration
                        tvSongDuration.setText(String.format("Time: %s", Utils.convertTimeMsToMMSS(mediaPlayer.getDuration())));
                        progressBarTop.setMax(mediaPlayer.getDuration());

                        //compete listener
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                record();
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "isReady");

                }
            }
        });
    }

    private String getRecordName(KaraokeSong songKaraokeSong) {
        String createdAt = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String recordName = songKaraokeSong.getId() + "_" + songKaraokeSong.getName() + "_" + createdAt + "_raw.3gp";
        return recordName;
    }

    @OnClick(R.id.btnRecordSong)
    void record() {
        if (recordViewModel.getIsReady().getValue()){
            if (!playing) {
                playAudio();
                startRecordMic();
            }
            else {
                //stop
                hasStopped = true;
                stopAudio();
                stopRecordMic();
                stop();
            }
            playing = !playing;
        }
    }
    @OnClick(R.id.btnResetSong)
    void reset() {
        if (playing && !hasStopped) {
            playing = false;
            btnRecordSong.setImageDrawable(getDrawable(R.drawable.ic_voice_recorder));
            progressBarTop.setProgress(0);
            tvStatus.setText("Reset");
            tvCurrentTime.setText("00:00");
            llLyricContainer.removeAllViews();
            countDownReUpdate = 0;
            currentLineToShow = 0;
            currentLine = 0;
            currentLineToRun = 0;
            currentNote = 0;
            currentWidth = 0;
            listTextViewRunning.clear();
            listTextViewWidth.clear();
            listTextViewRunning = new ArrayList<>();
            listTextViewWidth = new ArrayList<>();

            handler.removeCallbacks(playingRunable);
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();

            mediaAudioRecorder.reset();
            //delete old file
            File oldFile = new File(recordOnDevice);
            if (oldFile.exists()){
                oldFile.delete();
            }
            mediaAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaAudioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaAudioRecorder.setOutputFile(recordOnDevice);
            try {
                mediaAudioRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stop() {
        Intent intent = new Intent(getApplicationContext(), ReplayRecordActivity.class);
        intent.putExtra("song", getIntent().getStringExtra("song"));
        intent.putExtra("record", recordOnDevice);
        intent.putExtra("beat", urlBeatOnDevice);
        intent.putExtra("lyric", urlLyricOnDevice);
        startActivity(intent);
        finish();
    }

    private void playAudio() {
        mediaPlayer.start();
        playingRunable.run();

        //change some View
        tvStatus.setText("Playing");
        btnRecordSong.setImageDrawable(getDrawable(R.drawable.ic_stop_40));
    }

    private void pauseAudio() {
        mediaPlayer.pause();
        handler.removeCallbacks(playingRunable);
        //change some View
        tvStatus.setText("Paused");
        btnRecordSong.setImageDrawable(getDrawable(R.drawable.ic_voice_recorder));
    }
    private void stopAudio() {
        mediaPlayer.stop();
        handler.removeCallbacks(playingRunable);
        tvStatus.setText("Stoped");
        progressBarTop.setProgress(0);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecordMic() {
        mediaAudioRecorder.resume();
    }

    private void startRecordMic() {
        mediaAudioRecorder.start();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecordMic() {
        mediaAudioRecorder.pause();
    }
    private void stopRecordMic() {
        mediaAudioRecorder.stop();
    }
    /*
    * Runable Support for display lyric
    * */
    final int TIME_UPDATE_UI = 100;
    int countDownReUpdate = 0;
    int currentLineToShow = 0;
    int currentLine = 0;
    int currentLineToRun = 0;
    int currentNote = 0;
    int currentWidth = 0;
    int gap = 0;
    int lyricSize = 0;
    List<TextView> listTextViewRunning = new ArrayList<>();
    List<Integer> listTextViewWidth = new ArrayList<>();


    private class PlayingRunable implements Runnable {
        @Override
        public void run() {
            int currentTime = mediaPlayer.getCurrentPosition();
            if (countDownReUpdate < 0) {
                countDownReUpdate = TIME_UPDATE_UI;
                tvCurrentTime.setText(Utils.convertTimeMsToMMSS(currentTime));
                progressBarTop.setProgress(currentTime);
            }else {
                countDownReUpdate-=10;
            }

            if (currentLineToShow < 3 && currentTime < gap) {
                addCurrentLineToContainer(currentLineToShow++, 0);
            }

            currentTime -= gap;

            if (currentLineToRun < lyricSize) {
                Line line = lyricFile.allLines.get(currentLineToRun);
                if ( line.start < currentTime && currentTime < line.end ) {
                    float measureOfWord = listTextViewWidth.get(currentLineToRun) / line.lyric.length();
//                    float measureOfWord = 20;
                    if (currentNote < line.notes.size()) {
                        Note note = line.notes.get(currentNote);
                        if ( note.start < currentTime && currentTime < (note.start + note.length) ){
                            int width = Math.round((currentTime - note.start) * 1.0f / note.length * (note.text.length() * measureOfWord));

                            RelativeLayout.LayoutParams layoutParams
                                    = new RelativeLayout.LayoutParams(currentWidth + width, ViewGroup.LayoutParams.WRAP_CONTENT);
                            layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.textViewFixed);
                            listTextViewRunning.get(currentLineToRun).setLayoutParams(layoutParams);
//                            Log.d(TAG, "width: " + (currentWidth + width));
                        }else {
                            if ((note.start + note.length) < currentTime && currentTime < line.end) {
                                currentNote++;
                                currentWidth += note.text.length() * measureOfWord;
//                                Log.d(TAG, "currNote" + currentNote);
                            }
                        }
                    }
                }else {
                    if (line.end < currentTime) {
                        RelativeLayout.LayoutParams layoutParams
                                = new RelativeLayout.LayoutParams(listTextViewWidth.get(currentLineToRun),
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.textViewFixed);
                        listTextViewRunning.get(currentLineToRun).setLayoutParams(layoutParams);

                        currentLineToRun++;
                        currentNote = 0;
                        currentWidth = 0;
//                      Log.d(TAG, "currLine" + currentLineToRun);
                    }
                }
            }

            if (currentLine < lyricSize) {
                Line line = lyricFile.allLines.get(currentLine);
                if ( line.start < currentTime && currentTime < line.end ) {
                    if (currentLineToShow < lyricSize){
                        addCurrentLineToContainer(currentLineToShow++ , (line.end - line.start) / 2);
                    }
                    currentLine++;
                }
            }
            handler.postDelayed(playingRunable, 1);
        }
    }

    //scrollview focus down
    Runnable scrollRunable = new Runnable() {
        @Override
        public void run() {
            scrollView.fullScroll(View.FOCUS_DOWN);
        }
    };
    private void addCurrentLineToContainer(int iLine, float timeDelay) {
        Line line = lyricFile.allLines.get(iLine);

        View view = getLayoutInflater().inflate(R.layout.item_line_of_lyric, llLyricContainer,false);

        TextView textViewFixed = view.findViewById(R.id.textViewFixed);
        TextView textViewRunning = view.findViewById(R.id.textViewRunning);
        textViewFixed.setText(line.lyric);
        textViewRunning.setText(line.lyric);
        textViewFixed.measure(0, 0);

        //add to list
        listTextViewRunning.add(textViewRunning);
        listTextViewWidth.add(textViewFixed.getMeasuredWidth());

        llLyricContainer.addView(view);
        scrollView.postDelayed(scrollRunable, (long) timeDelay);
    }
    public String downloadFile(String urlPath) {
        InputStream in = null;
        try {
            String fileName = urlPath.substring(urlPath.lastIndexOf("/") + 1);

            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/app_karaoke/songs");
            if (!file.exists()) {
                if (!file.mkdirs()){
                    Log.d(TAG, "FolderNotFound");
                }
            }

            URL url = new URL(urlPath.replace(" ", "%20"));
            URLConnection connection = url.openConnection();
            connection.connect();

            in = new BufferedInputStream(connection.getInputStream(), 8192);

            FileOutputStream fos = new FileOutputStream(file.getPath() + "/" + fileName );

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.flush();
            fos.close();
            in.close();
            return file.getPath() + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private class DownloadFileFromURL extends AsyncTask<String, String[], String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvStatus.setText("Downloading...");
        }

        @Override
        protected String[] doInBackground(String... strings) {
            String res[] = new String[strings.length];
            int i = 0;
            for (String urlPath: strings) {
                res[i++] = downloadFile(urlPath);
                Log.d(TAG, urlPath);
            }
            return res;
        }
        @Override
        protected void onPostExecute(String[] s) {
            Log.d(TAG, "Download completely!" ) ;

            urlLyricOnDevice = s[0];
            urlBeatOnDevice = s[1];
            recordViewModel.getIsReady().postValue(true);
            tvStatus.setText("Ready!");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaAudioRecorder.stop();
        }
        handler.removeCallbacks(playingRunable);
        mediaPlayer.release();
        mediaAudioRecorder.release();
    }
}
