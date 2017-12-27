package com.trung.karaokeapp.activity;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.appclass.Line;
import com.trung.karaokeapp.appclass.LyricFile;
import com.trung.karaokeapp.appclass.Note;
import com.trung.karaokeapp.entities.KaraokeSong;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.utils.Utils;
import com.trung.karaokeapp.viewmodel.VideoViewModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoActivity extends FragmentActivity {
    private static final String TAG = "VideoActivity";

    //BindView
    @BindView(R.id.tvSongName)
    TextView tvSongName;
    @BindView(R.id.tvSongTime)  TextView tvSongDuration;
    @BindView(R.id.tvGenre)         TextView tvGenre;
    @BindView(R.id.ivCoverSong)     CircleImageView ivCoverSong;
    @BindView(R.id.tvStatus)        TextView tvStatus;
    @BindView(R.id.btnRecordSong) ImageButton btnRecordSong;
    //@BindView(R.id.btnResetSong)   ImageButton btnResetSong;
    @BindView(R.id.tvCurrentTime)   TextView tvCurrentTime;

    //element for lyric scroll
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.llLyricContainer)
    LinearLayout llLyricContainer;
    @BindView(R.id.progressBarTop)
    ProgressBar progressBarTop;
    VideoViewModel videoViewModel;

    //for Playing
    String urlBeatOnDevice, urlLyricOnDevice, recordOnDevice;
    LyricFile lyricFile;
    MediaPlayer mediaPlayer;
    MediaRecorder mediaAudioRecorder;
    boolean hasStopped = false;

    Handler handler;
    PlayingRunable playingRunable;
    private KaraokeSong karaokeSong;
    private TokenManager tokenManager;
    private ApiService service;
    private CharSequence[] items;
    private int checkedItem = 0;
    private Call<Integer> callReportKaraokeSong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance( getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        //keep screen awake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //init
        videoViewModel = ViewModelProviders.of(this).get(VideoViewModel.class);
        handler = new Handler();
        playingRunable = new PlayingRunable();

        //clear views in linear layout container
        llLyricContainer.removeAllViews();

        //change Color
        progressBarTop.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);
        //SongInfo Receive
        String songJsonText = getIntent().getStringExtra("song");
        karaokeSong = new Gson().fromJson(songJsonText, KaraokeSong.class);
        videoViewModel.setKaraokeSong(karaokeSong);

        //load songInfo to activity
        tvSongName.setText(karaokeSong.getName());
        tvGenre.setText(String.format("Genre: %s", karaokeSong.getGenre()));
        String folderSong =  AppURL.baseUrlSongAndLyric + "/" + karaokeSong.getBeat().substring(0, karaokeSong.getBeat().length() - 4);
        Glide.with(VideoActivity.this).load(folderSong + "/" + karaokeSong.getImage()).into(ivCoverSong);

        //media
        mediaPlayer = new MediaPlayer();

        //url
        String urlBeat = folderSong + "/" + karaokeSong.getBeat();
        String urlLyric = folderSong + "/" + karaokeSong.getLyric();
        //1. Download Beat and Lyric
        new DownloadFileFromURL().execute(urlLyric, urlBeat);

        //isReadyObserver -> load data, and prepare recording
        videoViewModel.getIsReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    lyricFile = Utils.openSongFile(urlLyricOnDevice);
                    gap = lyricFile.gap;
                    lyricSize = lyricFile.allLines.size();

                    try {
                        mediaPlayer.setDataSource(urlBeatOnDevice);
                        mediaPlayer.prepare();

                        //UI
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
    @OnClick(R.id.btnReportSong)
    void reportKaraokeSong() {
       /*
       * 0: wrong lyrics
       * 1: low sound
       * 2: asynchronous lyrics and music
       * 3:
       * */
        items = new CharSequence[]{ "Wrong lyric", "Low sound", "Asynchronous lyrics and music" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkedItem = i;
            }
        }).setPositiveButton("Report", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callReportKaraokeSong = service.reportKaraokeSong( karaokeSong.getId(), checkedItem );
                callReportKaraokeSong.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d(TAG, response.toString());
                        int result = response.body();
                        if (result == 1){
                            Toast.makeText(VideoActivity.this, "Report success!", Toast.LENGTH_SHORT).show();
                        }else if (result == 2) {
                            Toast.makeText(VideoActivity.this, "Report has existed!", Toast.LENGTH_SHORT).show();
                        }else if (result == 3){
                            Toast.makeText(VideoActivity.this, "Report is updated!", Toast.LENGTH_SHORT).show();
                        }else {
                            //result == 0
                            Toast.makeText(VideoActivity.this, "Report fail!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }


    @OnClick(R.id.btnRecordSong)
    void record() {
        if (videoViewModel.getIsReady().getValue()){
            if (!videoViewModel.getIsRecording().getValue()) {
                playAudio();
                videoViewModel.getIsRecording().postValue(true);
            }
            else {
                stopAudio();
                videoViewModel.getIsRecording().postValue(false);
                stop();
            }

        }
    }
    /*
    @OnClick(R.id.btnResetSong)
    void reset() {
        if (videoViewModel.getIsRecording().getValue()) {
            videoViewModel.getIsRecording().postValue(false);

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
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);

            //delete old raw recording file
            File oldFile = new File(videoViewModel.getRecordOnDevice());
            if (oldFile.exists()){
                oldFile.delete();
            }
            //reset recording
        }
    }
    */

    private void stop() {
        Intent intent = new Intent(getApplicationContext(), ReplayRecordActivity.class);
        intent.putExtra("song", getIntent().getStringExtra("song"));
        intent.putExtra("record", videoViewModel.getRecordOnDevice());
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

    private void stopAudio() {
        mediaPlayer.stop();
        handler.removeCallbacks(playingRunable);
        tvStatus.setText("Stoped");
        progressBarTop.setProgress(0);
    }

    private void startRecordMic() {
        mediaAudioRecorder.start();
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
                countDownReUpdate -= 1;
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

    //download file beat and lyric
    class PercentRunnable implements Runnable {
        public String percent = "";

        @Override
        public void run() {
            tvStatus.setText(String.format("Load... %s", percent));
        }
    }
    private class DownloadFileFromURL extends AsyncTask<String, String, String[]> {
        PercentRunnable runnable = new PercentRunnable();

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
                    res[i++] = file.getPath() + "/" + fileName;

                    byte[] buffer = new byte[1024];
                    int read = 0;
                    int total = 0;
                    int totalSize = connection.getContentLength();
                    Log.d(TAG, totalSize + "");
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    decimalFormat.setMaximumFractionDigits(2);


                    while ((read = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                        total += read;
                        publishProgress( decimalFormat.format(total * 100f / totalSize ) );

                    }
                    fos.flush();
                    fos.close();
                    in.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return res;
        }

        @Override
        protected void onProgressUpdate(final String... values) {
            runnable.percent = values[0];
            runOnUiThread( runnable );
        }

        @Override
        protected void onPostExecute(String[] s) {
            Log.d(TAG, "Download completely!" ) ;
            urlLyricOnDevice = s[0];
            urlBeatOnDevice = s[1];
            videoViewModel.getIsReady().postValue(true);
            tvStatus.setText("Ready!");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        handler.removeCallbacks(playingRunable);
        mediaPlayer.release();

        if (callReportKaraokeSong != null){
            callReportKaraokeSong.cancel();
            callReportKaraokeSong = null;
        }
    }
}
