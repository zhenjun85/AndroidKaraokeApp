package com.trung.karaokeapp.activity;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.CommentAdapter;
import com.trung.karaokeapp.appclass.Line;
import com.trung.karaokeapp.appclass.LyricFile;
import com.trung.karaokeapp.entities.CommentTb;
import com.trung.karaokeapp.entities.SharedRecord;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.utils.Utils;
import com.trung.karaokeapp.viewmodel.SrDetailViewModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SrDetailActivity extends AppCompatActivity {
    private static final String TAG = "SharedRecordDetailActiv";
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.ivUserAvatar) CircleImageView ivUserAvatar;
    @BindView(R.id.tvUserName) TextView tvUserName;
    @BindView(R.id.tvNumViews) TextView tvNumViews;
    @BindView(R.id.tvNumLikes) TextView tvNumLikes;
    @BindView(R.id.btnFriend) ImageButton btnFriend;
    @BindView(R.id.tvPostContent) TextView tvPostContent;
    @BindView(R.id.btnLikeToggle) ImageButton btnLikeToggle;
    @BindView(R.id.tvSharedAt) TextView tvSharedAt;
    @BindView(R.id.btnAddComment) ImageButton btnAddComment;
    @BindView(R.id.rvComment) RecyclerView rvComment;
    @BindView(R.id.tvDuration) TextView tvDuration;
    @BindView(R.id.tvCurrentTime) TextView tvCurrentTime;
    @BindView(R.id.seekBarSong) SeekBar seekBarSong;
    @BindView(R.id.btnPlay) ImageButton btnPlay;
    @BindView(R.id.lineOfLyric) TextView lineOfLyric;
    @BindView(R.id.videoView) VideoView videoView;

    private SharedRecord sharedRecord;
    private TokenManager tokenManager;
    private ApiService service;
    private CommentAdapter commentAdapter;
    public int relationStatus;
    private MediaPlayer mediaPlayer;
    private SrDetailViewModel srViewModel;
    private String urlLyric;
    private String urlLyricOnDevice;
    private LyricFile lyricFile;
    private String urlRecord;
    private String urlRecordOnDevice;
    private boolean isPlaying = false;
    public boolean hasStopped = false;
    private Call<List<CommentTb>> callGetComments;
    private Call<Integer> callGetIsLike;
    private Call<Integer> callLikeRecord;
    private Call<CommentTb> callComment;
    private Call<Integer> callRequestFriend;
    private Call<Integer> callGetRelation;
    private boolean isVideo;
    private CharSequence[] items;
    private int checkedItem;
    private Call<Integer> callReportSharedSong;
    private Call<Integer> callUpViewSr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_record_detail);
        ButterKnife.bind(this);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tokenManager = TokenManager.getInstance(getSharedPreferences("prefs", Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        sharedRecord = new Gson().fromJson(getIntent().getStringExtra("sharedrecord"), SharedRecord.class);
        srViewModel = ViewModelProviders.of(this).get(SrDetailViewModel.class);
        String folderName = sharedRecord.getKaraoke().getBeat().substring(0, sharedRecord.getKaraoke().getBeat().length() - 4);
        urlLyric = AppURL.baseUrlSongAndLyric + "/" + folderName + "/" + sharedRecord.getKaraoke().getLyric();
        urlRecord = AppURL.baseUrlAudioRecord + "/" + sharedRecord.getPath();

        //ivUser
        if (sharedRecord.getUser().getAvatar() != null){
            Glide.with(getApplicationContext()).load(AppURL.baseUrlPhotos + "/" + sharedRecord.getUser().getAvatar()).into(ivUserAvatar);
        }else {
            ivUserAvatar.setImageDrawable(getDrawable(R.drawable.ic_face_black_48px) );
        }

        //toolbar
        getSupportActionBar().setTitle( sharedRecord.getKaraoke().getName() );

        if (!sharedRecord.getType().equals("video")){
            isVideo = false;
        }else {
            isVideo = true;
        }
        videoView.setVisibility(View.GONE);
        //download txt file
        new DownloadFileFromURL().execute(urlLyric, urlRecord);

        //region observer
        srViewModel.getIsReady().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean) {
                    Call<SharedRecord> callGetSr = service.getSr(sharedRecord.getId());
                    callGetSr.enqueue(new Callback<SharedRecord>() {
                        @Override
                        public void onResponse(Call<SharedRecord> call, Response<SharedRecord> response) {
                            sharedRecord = response.body();
                            tvNumViews.setText( sharedRecord.getViewNo() );
                            tvNumLikes.setText( sharedRecord.getNumLikes() );
                        }

                        @Override
                        public void onFailure(Call<SharedRecord> call, Throwable t) {

                        }
                    });

                    //UpView
                    callUpViewSr = service.upViewSr(sharedRecord.getId());
                    callUpViewSr.enqueue(new Callback<Integer>() {
                        @Override
                        public void onResponse(Call<Integer> call, Response<Integer> response) {
                            Log.d(TAG, response.toString());
                            if (response.body() == 1){
                                Log.d(TAG, "upview success");
                            }else {
                                Log.d(TAG, "upview fail");
                            }
                        }

                        @Override
                        public void onFailure(Call<Integer> call, Throwable t) {
                            Log.d(TAG, t.getMessage());
                        }
                    });

                    Log.d(TAG, urlRecordOnDevice != null ? "1" : "0" );
                    lyricFile = Utils.openSongFile(urlLyricOnDevice);

                    videoView.setVideoPath(urlRecordOnDevice);
                    if (isVideo){
                        videoView.setVisibility(View.VISIBLE);
                    }
                    //prepare
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(urlRecordOnDevice);
                        mediaPlayer.prepare();

                        tvDuration.setText(Utils.convertTimeMsToMMSS(mediaPlayer.getDuration()));
                        seekBarSong.setMax(mediaPlayer.getDuration());
                        seekBarSong.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                if (b) {
                                    handlerPlaying.removeCallbacks(playingRunable);
                                    mediaPlayer.seekTo(i);
                                    videoView.seekTo(i);

                                    i -= lyricFile.gap;
                                    int j = 0;
                                    for (; j < lyricFile.allLines.size(); j++) {
                                        Line line = lyricFile.allLines.get(j);
                                        if (line.start < i && i < line.end){
                                            break;
                                        }
                                        if (i < line.start){
                                            j--;
                                            break;
                                        }
                                    }
                                    if (j < 0) {
                                        j = 0;
                                    }else if (j >= lyricFile.allLines.size()){
                                        j = lyricFile.allLines.size() - 1;
                                    }
                                    currentLine = j;
                                    lineOfLyric.setText(lyricFile.allLines.get(currentLine++).lyric);
                                    playingRunable.run();
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        })
                        ;
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                tvCurrentTime.setText("00:00");
                                btnPlay.setImageDrawable(getDrawable(R.drawable.ic_play_button));
                                currentLine = 0;
                                //
                                handlerPlaying.removeCallbacks(playingRunable);
                                seekBarSong.setProgress( 0 );
                                videoView.pause();
                                mediaPlayer.pause();
                                isPlaying = false;
                                hasStopped = true;
                            }
                        })
                        ;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //endregion

        //load rvComment
        callGetComments = service.getCommentsOfSr(sharedRecord.getId());
        callGetComments.enqueue(new Callback<List<CommentTb>>() {
            @Override
            public void onResponse(Call<List<CommentTb>> call, Response<List<CommentTb>> response) {
                Log.d(TAG, response.toString());
                commentAdapter = new CommentAdapter(SrDetailActivity.this, response.body());
                rvComment.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                rvComment.setAdapter(commentAdapter);
                setupSharedRecord();
            }

            @Override
            public void onFailure(Call<List<CommentTb>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });

    }

    @OnClick(R.id.btnPlay)
    void playRecord() {
        if (srViewModel.getIsReady().getValue()) {
            if (isPlaying){
                //pause
                mediaPlayer.pause();
                videoView.pause();
                btnPlay.setImageDrawable( getDrawable(R.drawable.ic_play_button) );
                handlerPlaying.removeCallbacks(playingRunable);
            }else {
                //play
                if (hasStopped) {
                    hasStopped = false;
                    mediaPlayer.seekTo(0);
                    videoView.seekTo(0);
                }
                mediaPlayer.start();
                videoView.start();
                btnPlay.setImageDrawable(getDrawable(R.drawable.ic_pause_round));
                playingRunable.run();
            }
            isPlaying = !isPlaying;
        }
    }
    Handler handlerPlaying = new Handler();
    PlayingRunable playingRunable = new PlayingRunable();
    int currentLine = 0;

    class PlayingRunable implements Runnable {
        @Override
        public void run() {
            int currentTime = mediaPlayer.getCurrentPosition();
            seekBarSong.setProgress(currentTime);
            tvCurrentTime.setText(Utils.convertTimeMsToMMSS(currentTime));
            currentTime -= lyricFile.gap;

            if (currentLine >= lyricFile.allLines.size()){
                return;
            }

            Line line = lyricFile.allLines.get(currentLine);
            if (currentTime < 0 && currentLine == 0){
                lineOfLyric.setText( line.lyric );
                currentLine++;
            }
            if ( line.start < currentTime && currentTime < line.end ) {
                lineOfLyric.setText( line.lyric );
                currentLine++;
            }

            handlerPlaying.postDelayed(playingRunable, 10);
        }
    }

    private void setupSharedRecord() {
        tvUserName.setText( sharedRecord.getUser().getName() );
        tvNumViews.setText( sharedRecord.getViewNo() + "" );
        tvNumLikes.setText( sharedRecord.getNumLikes() + "" );
        tvPostContent.setText( sharedRecord.getContent() );
        tvSharedAt.setText( sharedRecord.getShared_at() );

        //IsLike
        callGetIsLike = service.getIsLike(sharedRecord.getId());
        callGetIsLike.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d(TAG, sharedRecord.toString());
                if (response.body() == 1) {
                    btnLikeToggle.setImageDrawable( getDrawable(R.drawable.ic_liked) );
                }else {
                    btnLikeToggle.setImageDrawable( getDrawable(R.drawable.ic_like) );
                }

                getRelation();
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });



    }

    private void getRelation() {
        //getRelationStatus
        callGetRelation = service.getRelationStatus(sharedRecord.getUserId());
        /*
         * 0: same
         * 1: friend
         * 2: request
         * 3: nofriend
         * */
        callGetRelation.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d(TAG, response.toString());
                relationStatus = response.body();
                switch (relationStatus){
                    case 0:
                    case 1:
                        btnFriend.setVisibility(View.GONE);
                        break;
                    case 2:
                        btnFriend.setImageDrawable(getDrawable(R.drawable.ic_stopwatch_1));
                        btnFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(getBaseContext(), "Waiting accept", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 3:
                        btnFriend.setImageDrawable(getDrawable(R.drawable.ic_add_user));
                        btnFriend.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                callRequestFriend = service.requestFriend(sharedRecord.getUserId());
                                callRequestFriend.enqueue(new Callback<Integer>() {
                                    @Override
                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                        Log.d(TAG, response.toString());
                                        if (response.body() == 1){
                                            btnFriend.setImageDrawable(getDrawable(R.drawable.ic_stopwatch_1));
                                            Toast.makeText(SrDetailActivity.this,
                                                    "Requesting friend success!", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(SrDetailActivity.this,
                                                    "Requesting friend has fail!", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Integer> call, Throwable t) {
                                        Log.d(TAG, t.getMessage());
                                    }
                                });
                            }
                        });
                        break;

                    default:break;
                }

            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e(TAG, "fail" +  t.getMessage());
            }
        });

    }

    @OnClick(R.id.btnLikeToggle)
    void likeSharedRecord() {
        callLikeRecord = service.likeRecord(sharedRecord.getId());
        callLikeRecord.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                Log.d("feedAdapter", sharedRecord.toString());
                if (response.body() == 1) {
                    btnLikeToggle.setImageDrawable( getDrawable(R.drawable.ic_liked) );
                    tvNumLikes.setText( (Integer.parseInt(tvNumLikes.getText().toString()) + 1) + "" );
                }else {
                    btnLikeToggle.setImageDrawable( getDrawable(R.drawable.ic_like) );
                    tvNumLikes.setText( (Integer.parseInt(tvNumLikes.getText().toString()) - 1) + "" );
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.d("feedAdapter", t.getMessage());
            }
        });
    }

    @OnClick(R.id.btnAddComment)
    void addComment() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View viewComment = LayoutInflater.from(this).inflate(R.layout.dialog_add_comment, null, false);
        final TextInputLayout tilComment = viewComment.findViewById(R.id.tilComment);
        builder.setView(viewComment)
                .setPositiveButton("Comment", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (tilComment.getEditText().getText().toString().length() != 0) {
                            callComment = service.commentRecord(sharedRecord.getId(), tilComment.getEditText().getText().toString());
                            callComment.enqueue(new Callback<CommentTb>() {
                                @Override
                                public void onResponse(Call<CommentTb> call, Response<CommentTb> response) {
                                    Log.d("feedAdapter", sharedRecord.toString());
                                    CommentTb comment = response.body();
                                    if (comment != null) {
                                        List<CommentTb> list =  commentAdapter.getCommentTbList();
                                        list.add( comment );
                                        commentAdapter = new CommentAdapter(SrDetailActivity.this, list);
                                        rvComment.setAdapter(commentAdapter);

                                        Toast.makeText(getBaseContext(), "Comment has posted!", Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(getBaseContext(), "Comment fail!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<CommentTb> call, Throwable t) {
                                    Log.d("feedAdapter", t.getMessage());
                                }
                            });
                        }
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton( DialogInterface.BUTTON_POSITIVE ).setTextColor(Color.BLACK);
            }
        });
        alertDialog.show();
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lineOfLyric.setText( "Downloading...." );
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
//                        publishProgress( decimalFormat.format(total * 100f / totalSize ) );

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
        protected void onPostExecute(String[] s) {
            Log.d(TAG, "Download completely!" );
            lineOfLyric.setText("Download completely!");
            urlLyricOnDevice = s[0];
            urlRecordOnDevice = s[1];
            srViewModel.getIsReady().postValue(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            handlerPlaying.removeCallbacks(playingRunable);
            mediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shared_record_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mn_report:
                reportSharedRecord();
                break;
            case android.R.id.home:
                onBackPressed();;
                break;
            default:break;
        }
        return  true;
    }

    private void reportSharedRecord() {
        /*
        * 0: Bad recording
        * 1: Lots of noise
        * 2: Asynchronous lyrics and music
        * 3:
        * */
        items = new CharSequence[]{ "Bad recording", "Lots of noise", "Asynchronous lyrics and music" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkedItem = i;
            }
        }).setPositiveButton("Report", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callReportSharedSong = service.reportSharedRecord( sharedRecord.getId(), checkedItem );
                callReportSharedSong.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d(TAG, response.toString());
                        int result = response.body();
                        if (result == 1){
                            Toast.makeText(SrDetailActivity.this, "Report success!", Toast.LENGTH_SHORT).show();
                        }else if (result == 2) {
                            Toast.makeText(SrDetailActivity.this, "Report has existed!", Toast.LENGTH_SHORT).show();
                        }else if (result == 3){
                            Toast.makeText(SrDetailActivity.this, "Report is updated!", Toast.LENGTH_SHORT).show();
                        }else {
                            //result == 0
                            Toast.makeText(SrDetailActivity.this, "Report fail!", Toast.LENGTH_SHORT).show();
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

    /*   private Call<List<CommentTb>> callGetComments;
        private Call<Integer> callGetIsLike;
        private Call<Integer> callLikeRecord;
        private Call<CommentTb> callComment;
        private Call<Integer> callRequestFriend;
        private Call<Integer> callGetRelation;*/
    @Override
    protected void onStop() {
        super.onStop();
        if (callGetComments != null){
            callGetComments.cancel();
            callGetComments = null;
        }
        if (callGetIsLike != null){
            callGetIsLike.cancel();
            callGetIsLike = null;
        }
        if (callLikeRecord != null){
            callLikeRecord.cancel();
            callLikeRecord = null;
        }
        if (callComment != null){
            callComment.cancel();
            callComment = null;
        }
        if (callRequestFriend != null){
            callRequestFriend.cancel();
            callRequestFriend = null;
        }
        if (callGetRelation != null){
            callGetRelation.cancel();
            callGetRelation = null;
        }
        if (callReportSharedSong != null){
            callReportSharedSong.cancel();
            callReportSharedSong = null;
        }
        if (callUpViewSr != null){
            callUpViewSr.cancel();
            callUpViewSr = null;
        }

    }
}
