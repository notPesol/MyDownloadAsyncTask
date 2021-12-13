package com.example.mydownloadasynctask;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private ImageView mImageView;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.imageView);
        mEditText = findViewById(R.id.edt_url);

        Button btnDownload = findViewById(R.id.button);
        Button btnLoad = findViewById(R.id.button2);

        btnDownload.setOnClickListener(v -> {
            downloadImage(true);
        });

        btnLoad.setOnClickListener(v -> {
            downloadImage(false);
        });

    }

    private void downloadImage(boolean isSave) {
        String url = mEditText.getText().toString().trim();
        if (url.isEmpty()) {
            return;
        }
        new MydownloadTask().execute(url, isSave);

        mEditText.setText("");
    }

    class MydownloadTask extends AsyncTask<Object, Integer, Bitmap> {
        private ProgressDialog mDialog;
        String result = "";
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMax(100);
            mDialog.setTitle("Download file");
            mDialog.setIndeterminate(false);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMessage("Downloading...");
            mDialog.setProgress(0);
            mDialog.show();

        }

        @Override
        protected Bitmap doInBackground(Object... objects) {

            Bitmap bitmap = null;
            boolean isSave = (Boolean) objects[1];
            try {
                URL url = new URL((String)objects[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = connection.getInputStream();
                if (isSave) {
                    int fileSize = connection.getContentLength();
                    String path = url.getPath();
                    int lastSlashIndex = path.lastIndexOf("/");
                    String fileName = path.substring(lastSlashIndex + 1);

                    File exDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String exPath = exDir.getAbsolutePath();
                    File file = new File(exPath, fileName);
                    if (file.exists()) {
                        String suffix = "_" + System.currentTimeMillis() + ".";
                        fileName = fileName.replace(".", suffix);
                        file = new File(exPath, fileName);
                    }

                    FileOutputStream outputStream = new FileOutputStream(file);

                    byte[] buffer = new byte[8 * 1024];
                    int byteRead = 0;
                    int totalByteRead = 0;
                    int progress = 0;

                    byteRead = inputStream.read(buffer);
                    while (byteRead != -1) {
                        outputStream.write(buffer, 0, byteRead);
                        totalByteRead += byteRead;
                        progress = (totalByteRead / fileSize) * 100;
                        publishProgress(progress);
                        byteRead = inputStream.read(buffer);
                    }

                    outputStream.flush();
                    outputStream.close();
                }

                url = new URL((String)objects[0]);
                connection = (HttpURLConnection) url.openConnection();
                inputStream = connection.getInputStream();

                bitmap = BitmapFactory.decodeStream(inputStream);

                inputStream.close();
                connection.disconnect();

            } catch (IOException e) {
                result = e.getMessage();
            }



            result = "Task complete.";

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mDialog.dismiss();
            mImageView.setImageBitmap(bitmap);

            Toast.makeText(getBaseContext(), result, Toast.LENGTH_SHORT).show();
        }
    }
}