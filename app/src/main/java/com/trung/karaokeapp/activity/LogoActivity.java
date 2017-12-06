package com.trung.karaokeapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.trung.karaokeapp.R;

public class LogoActivity extends AppCompatActivity {
    public final int REQUEST_INTERNET = 123;
    public final int REQUEST_WRITE_EXTERNAL = 124;
    public final int REQUEST_RECORD_AUDIO = 125;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        //Remove navigation bar
        getWindow().getDecorView().setSystemUiVisibility(flags);

        setContentView(R.layout.activity_logo);

        //Check Permission
        if ( checkPermisssion() ) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    openMainActivity();
                }
            }, 1000);
        };

    }
    public void openMainActivity() {
        //Open Home Activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public boolean checkPermisssion() {
        if ( ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET);
                return false;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL);
                return false;
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                return false;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_INTERNET:
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    Toast.makeText(getApplicationContext(), "Internet permission's allowed.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Internet permission's not allowed.", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_RECORD_AUDIO:
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    Toast.makeText(getApplicationContext(), "Recording permission's allowed.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Recording permission's not allowed.", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_WRITE_EXTERNAL:
                if ( grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    Toast.makeText(getApplicationContext(), "Writing external permission's allowed.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Writing external permission's not allowed.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
        //Check Permission again
        if ( checkPermisssion() )
            openMainActivity();
    }
}
