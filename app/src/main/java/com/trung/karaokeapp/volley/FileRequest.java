package com.trung.apptest03;

import android.os.Environment;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by bebw on 12/3/2017.
 */

public class FileRequest extends Request<String> {
    private static final String LOG_TAG = "FileRequest";
    private final Response.Listener<String> listener;

    public FileRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);

        this.listener = listener;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String path = "";
        // Directory
        if (isExternalStorageWritable()) {
            //FileName
            String albumName = "testdownload";
            String fileName = "abc1.jpg";

            try {
                path = getAlbumStorageDir(albumName).getPath() + File.separator + fileName;
                FileOutputStream fos = new FileOutputStream( path );
                fos.write(response.data);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("file path", path);

        return Response.success(path, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
    public File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), albumName);
        if (!file.exists()){
            if (!file.mkdirs()) {
                Log.e(LOG_TAG, "Directory not created");
            }
        }
        return file;
    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
