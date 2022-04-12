package com.example.healthybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.healthybuddy.DTO.ChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class PhotoActivity extends AppCompatActivity {

    private ImageView img;
    private VideoView video;
    private MediaController mediaController;
    private String mId;
    private String token;
    private String pId2;
    private String chatRoomId;
    private int mode;
    private TextView send, back;
    private Uri uri;
    private Bitmap bitmap;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        mId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");

        img = (ImageView) findViewById(R.id.photo_imageView);
        video = (VideoView) findViewById(R.id.photo_videoView);
        send = (TextView) findViewById(R.id.photo_textView_send);
        back = (TextView) findViewById(R.id.photo_textView_back);

        mediaController = new MediaController(this);
        mediaController.setAnchorView(video);
        video.setMediaController(mediaController);

        pId2 = getIntent().getStringExtra("id2");
        chatRoomId = getIntent().getStringExtra("chatRoomId");
        mode = getIntent().getIntExtra("mode",0);

        if(mode == 0) {
            // 사진
            uri = getIntent().getParcelableExtra("photo");
            video.setVisibility(View.GONE);
            img.setImageURI(uri);
        } else {
            // 비디오
            uri = getIntent().getParcelableExtra("photo");
            img.setVisibility(View.GONE);
            video.setVideoURI(uri);

            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }
        // uri = getIntent().getParcelableExtra("photo");
        // img.setImageURI(uri);
        Log.d("test","photo : "+uri);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PhotoActivity.this, "전송", Toast.LENGTH_SHORT).show();
                String random = UUID.randomUUID().toString();
                // storage에 사진 저장
                if(mode == 0){
                    type = "images";
                } else {
                    type = "videos";
                }
                FirebaseStorage.getInstance().getReference().child(type).child(chatRoomId).child(random).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(!task.isSuccessful()){
                            Log.d("test", "why false?" + task.getException());
                        }

                        FirebaseStorage.getInstance().getReference().child(type).child(chatRoomId).child(random).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                ChatModel.Comment comment = new ChatModel.Comment();
                                comment.pId = mId;
                                comment.timestamp = ServerValue.TIMESTAMP;
                                comment.imgUrl = imageUrl;

                                // 채팅방에 image uri 저장
                                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent intent = new Intent(PhotoActivity.this, MessageActivity_firebase.class);
                                        intent.putExtra("chatRoomId",chatRoomId);
                                        intent.putExtra("id2",pId2);
                                        finish();
                                        //startActivity(intent);
                                    }
                                });
                            }
                        });
                    }
                });

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
}