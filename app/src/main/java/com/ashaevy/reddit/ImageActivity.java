package com.ashaevy.reddit;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    public static final String SAVED_IMAGE_SDCARD_PATH = "/Pictures/Reddit/";
    public static String IMAGE_URL_KEY = "IMAGE_URL_KEY";

    @BindView(R.id.iv)
    ImageView imageView;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        url = getIntent().getStringExtra(IMAGE_URL_KEY);
        // store full sized copy
        Glide.with(imageView.getContext())
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToSDCard(ImageActivity.this, url);
            }
        });
    }

    private void saveImageToSDCard(final Context context, final String url) {
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            processSavingImage(context, url);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void processSavingImage(final Context context, final String url) {
        Glide
            .with(context)
            .load(url)
            .asBitmap()
            .toBytes(Bitmap.CompressFormat.JPEG, 80)
            .into(new SimpleTarget<byte[]>() {
                @Override public void onResourceReady(final byte[] resource, GlideAnimation<? super byte[]> glideAnimation) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override protected Void doInBackground(Void... params) {
                            File sdcard = Environment.getExternalStorageDirectory();
                            final File file = new File(sdcard + SAVED_IMAGE_SDCARD_PATH +
                                    URLUtil.guessFileName(url, null, null));
                            File dir = file.getParentFile();
                            try {
                                if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory())) {
                                    throw new IOException("Cannot ensure parent directory for file " + file);
                                }
                                BufferedOutputStream s = new BufferedOutputStream(new FileOutputStream(file));
                                s.write(resource);
                                s.flush();
                                s.close();

                                MediaScannerConnection.scanFile(context, new String[]
                                        { file.getPath() }, new String[] { "image/jpeg" }, null);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, getString(R.string.image_saved_toast) +
                                                file.getPath(), Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                }
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            processSavingImage(this, url);
        }
    }
}
