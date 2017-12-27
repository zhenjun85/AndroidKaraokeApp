package com.trung.karaokeapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.trung.karaokeapp.R;
import com.trung.karaokeapp.adapter.PhotoAdapter;
import com.trung.karaokeapp.entities.PhotoTb;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.RetrofitBuilder;
import com.trung.karaokeapp.network.TokenManager;
import com.trung.karaokeapp.test.ImageFilePath;
import com.trung.karaokeapp.utils.AppBaseCode;
import com.trung.karaokeapp.viewmodel.PhotoViewModel;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoManageActivity extends AppCompatActivity {
    private static final String TAG = "PhotoManageActivity";
    @BindView(R.id.fabAddPhoto) FloatingActionButton fabAddPhoto;
    @BindView(R.id.rvPhoto) RecyclerView rvPhoto;

    private TokenManager tokenManager;
    private ApiService service;
    private Call<ResponseBody> callUploadImage;
    private Call<PhotoTb> callAddPhoto;
    private Call<List<PhotoTb>> callGetPhotos;
    private PhotoAdapter photoAdapter;
    private Menu mOptionMenu;
    private PhotoViewModel photoViewModel;
    private Call<Integer> callDelPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_manage);
        ButterKnife.bind(this);
        tokenManager = TokenManager.getInstance( getSharedPreferences("prefs",Context.MODE_PRIVATE));
        service = RetrofitBuilder.createServiceWithAuth(ApiService.class, tokenManager);
        photoViewModel = ViewModelProviders.of(this).get(PhotoViewModel.class);
        //toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.titile_photo);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        //rv
        callGetPhotos = service.getAllPhotos();
        callGetPhotos.enqueue(new Callback<List<PhotoTb>>() {
            @Override
            public void onResponse(Call<List<PhotoTb>> call, Response<List<PhotoTb>> response) {
                Log.d(TAG, response.toString());
                List<PhotoTb> photoTbList = response.body();
                photoAdapter = new PhotoAdapter(getApplicationContext(), photoTbList, mOptionMenu, photoViewModel, service);
                rvPhoto.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                rvPhoto.setAdapter(photoAdapter);
            }

            @Override
            public void onFailure(Call<List<PhotoTb>> call, Throwable t) {
                Log.d(TAG, t.getMessage());
            }
        });

        //Menu

    }

    @OnClick(R.id.fabAddPhoto)
    void addPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), AppBaseCode.PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppBaseCode.PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            //prepare for upload
            String picturePath = ImageFilePath.getPath(getApplicationContext(), uri);
            final File file = new File(picturePath);
            final RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("myfile", file.getName(), requestBody);
            RequestBody description = RequestBody.create(MultipartBody.FORM, "upload my image");

            callUploadImage = service.postImage(body, description);
            callUploadImage.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.d(TAG, requestBody.toString());
                    String fileName = "";
                    try {
                        fileName = response.body().string();
                        fileName = fileName.substring( fileName.lastIndexOf("/") + 1 );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callAddPhoto = service.addPhoto(fileName);
                    callAddPhoto.enqueue(new Callback<PhotoTb>() {
                        @Override
                        public void onResponse(Call<PhotoTb> call, Response<PhotoTb> response) {
                            Log.d(TAG, response.toString());
                            if (response.body() !=  null) {
                                List<PhotoTb> newListPhotoTb =  photoAdapter.getPhotoTbList();
                                newListPhotoTb.add(response.body());
                                photoAdapter = new PhotoAdapter(getApplicationContext(), newListPhotoTb, mOptionMenu, photoViewModel, service);
                                rvPhoto.setAdapter(photoAdapter);

                                Toast.makeText(getApplicationContext(), "Upload success", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getApplicationContext(), "Upload fail", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<PhotoTb> call, Throwable t) {
                            Log.d(TAG, t.getMessage());
                        }
                    });
                }
                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d(TAG, t.getMessage());
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_photo, menu);
        mOptionMenu = menu;
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mn_set_avatar:
                String imagePath = photoAdapter.getPhotoTbList().get(photoViewModel.getPosSelect()).getPath();
                Call<Integer> callUpdateAvatar = service.updateUserAvatar(imagePath);
                callUpdateAvatar.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        Log.d(TAG, response.toString());
                        if (response.body() == 1){
                            List<PhotoTb> newListPhotoTb = photoAdapter.getPhotoTbList();
                            photoAdapter = new PhotoAdapter(getApplicationContext(), newListPhotoTb, mOptionMenu, photoViewModel, service);
                            rvPhoto.setAdapter(photoAdapter);
                            photoViewModel.setPosSelect(-1);
                            mOptionMenu.getItem(0).setVisible(false);
                            mOptionMenu.getItem(1).setVisible(false);
                            Toast.makeText(getApplicationContext(), "Update avatar success!", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getApplicationContext(), "Update avatar fail!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Log.d(TAG, t.getMessage());
                    }
                });


                break;
            case R.id.mn_del_photo:
                if (photoViewModel.getPosSelect() == -1)
                    break;

                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoManageActivity.this);
                builder.setMessage("Do you want to delete this photo?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {

                                callDelPhoto = service.delPhoto( photoAdapter.getPhotoTbList().get( photoViewModel.getPosSelect() ).getId() );
                                callDelPhoto.enqueue(new Callback<Integer>() {
                                    @Override
                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                        Log.d("photoAdapter", response.toString());
                                        if (response.body() == 1){
                                            List<PhotoTb> newListPhotoTb = photoAdapter.getPhotoTbList();
                                            newListPhotoTb.remove(photoViewModel.getPosSelect());
                                            photoAdapter = new PhotoAdapter(getApplicationContext(), newListPhotoTb, mOptionMenu, photoViewModel, service);
                                            rvPhoto.setAdapter(photoAdapter);
                                            Toast.makeText(getApplicationContext(), "Delete success!", Toast.LENGTH_SHORT).show();
                                            photoViewModel.setPosSelect(-1);
                                            mOptionMenu.getItem(0).setVisible(false);
                                            mOptionMenu.getItem(1).setVisible(false);
                                        }else {
                                            Toast.makeText(getApplicationContext(), "Delete fail!", Toast.LENGTH_SHORT).show();
                                        }
                                        dialogInterface.dismiss();
                                    }

                                    @Override
                                    public void onFailure(Call<Integer> call, Throwable t) {
                                        Log.d("photoAdapter", t.getMessage());
                                    }
                                });
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                    }
                });
                alertDialog.show();

                break;
            default:break;
        }

        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (callUploadImage != null){
            callUploadImage.cancel();
            callUploadImage = null;
        }
        if (callAddPhoto != null){
            callAddPhoto.cancel();
            callAddPhoto = null;
        }
        if (callGetPhotos != null){
            callGetPhotos.cancel();
            callGetPhotos = null;
        }
        if (callDelPhoto != null){
            callDelPhoto.cancel();
            callDelPhoto = null;
        }
    }
}
