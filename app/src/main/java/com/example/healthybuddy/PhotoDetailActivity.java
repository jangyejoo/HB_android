package com.example.healthybuddy;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthybuddy.DTO.ChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.UUID;

public class PhotoDetailActivity extends AppCompatActivity {

    private ImageView img;
    private String mId;
    private String token;
    private String nickname;
    private TextView nick, back;
    private String url;
    private VideoView video;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        mId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");

        img = (ImageView) findViewById(R.id.photo_imageView);
        video = (VideoView) findViewById(R.id.photo_videoView);
        back = (TextView) findViewById(R.id.photo_textView_back);
        nick = (TextView) findViewById(R.id.photo_textView_nickname);

        nickname = getIntent().getStringExtra("nickname");
        nick.setText(nickname);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(video);
        video.setMediaController(mediaController);

        url = getIntent().getStringExtra("photo");
        if(url.contains("images")) {
            // 사진
            Glide.with(this)
                    .load(url)
                    .into(img);
            video.setVisibility(View.GONE);
        } else if (url.contains("videos")) {
            // 동영상
            img.setVisibility(View.GONE);
            Bitmap thumb = null;
            try {
                thumb = retriveVideoFrameFromVideo(url);
                if(thumb!=null){
                    video.setBackgroundDrawable(new BitmapDrawable(thumb));
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            video.setVideoURI(Uri.parse(url));
            Log.d("test","videoUrl : "+String.valueOf(url));
            Log.d("test","동영상 왜 시작 안해요.");
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    Log.d("test","동영상 시작이어요.");
                    video.setBackgroundDrawable(null);
                    mediaPlayer.start();
                }
            });
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public static Bitmap retriveVideoFrameFromVideo(String videoPath) throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());
            //   mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Throwable("Exception in retriveVideoFrameFromVideo(String videoPath)" + e.getMessage());

        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }
}
