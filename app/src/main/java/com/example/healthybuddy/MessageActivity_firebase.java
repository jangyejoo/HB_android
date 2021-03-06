package com.example.healthybuddy;

import static android.text.InputType.TYPE_CLASS_TEXT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.BoringLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.textclassifier.TextLinks;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.healthybuddy.DTO.ChatModel;
import com.example.healthybuddy.DTO.NotificationModel;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.itemData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity_firebase extends AppCompatActivity {

    private EditText text;
    private String mId, token, pId2, chatRoomId, enter, nick;
    private Button btn, btn_friend, btn_accept, btn_plus;
    private RecyclerView recyclerView;
    private itemData destinationUserModel = new itemData();
    private LinearLayout title, time;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private SimpleDateFormat hour_min = new SimpleDateFormat("a HH:mm");
    private SimpleDateFormat day = new SimpleDateFormat("yyyy??? MM??? dd??? E??????");
    private List<ChatModel> chatModels = new ArrayList<>();
    private Object LastTimestamp;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    int peopleCount = 0;

    private static final int PICK_FROM_ALBUM = 10;
    private String currentPhotoPath;
    private String currentVideoPath;
    private Uri imageUri;

    private Boolean noRoom = false;

    private RecyclerViewAdapter recyclerViewAdapter;

    private PopupMenu popupMenu;
    private int alreadyResult;

    private String cal_title, start, end;
    private TextView tv_title, tv_time;

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_firebase);
        context = this;

        mId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        enter = getPreferenceStringEnter(mId);
        nick = getPreferenceString("nick");

        pId2 = getIntent().getStringExtra("id2");
        btn = (Button) findViewById(R.id.message_btn);
        btn_accept = (Button) findViewById(R.id.btn_accept);
        btn_friend = (Button) findViewById(R.id.btn_friend);
        btn_plus = (Button)findViewById(R.id.message_btn_plus);
        text = (EditText) findViewById(R.id.message_editText);
        recyclerView = (RecyclerView)findViewById(R.id.message_recyclerview);
        title = (LinearLayout) findViewById(R.id.message_linearlayout_title);
        time = (LinearLayout) findViewById(R.id.message_linearlayout_time);
        tv_title = (TextView) findViewById(R.id.message_textview_title);
        tv_time = (TextView) findViewById(R.id.message_textview_time);

        RetrofitClient retrofitClient = new RetrofitClient();

        // ?????? ??????
        Call<List<RegisterDTO>> member = retrofitClient.login.members(token);
        member.enqueue(new Callback<List<RegisterDTO>>(){
            @Override
            public void onResponse(Call<List<RegisterDTO>> call, Response<List<RegisterDTO>> response){
                if(response.isSuccessful()){
                    List<RegisterDTO> data = response.body();
                    Log.d("Test", data.get(0).getUSER_ID());
                } else {
                    Log.d("Test", "????????????");
                    Intent intent = new Intent(MessageActivity_firebase.this, AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        HashMap<String, RequestBody> destinationUser = new HashMap<>();
        RequestBody Id = RequestBody.create(MediaType.parse("text/plain"), mId);
        RequestBody Id2 = RequestBody.create(MediaType.parse("text/plain"), pId2);
        destinationUser.put("pId", Id2);

        Call<ProfileDTO> profile = retrofitClient.profile.profile(token, destinationUser);
        profile.enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                try{
                    if(!response.isSuccessful()){
                        Log.d("test","?????? ????????????");
                    }else {
                        ProfileDTO post = response.body();

                        destinationUserModel.img= "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/" + post.getpImg();
                        destinationUserModel.Nickname = post.getpNickname();
                        destinationUserModel.id2 = post.getpId();

                    }
                }catch(Exception e){
                    Log.v("Test", "catch"+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Log.v("test","failure"+t.toString());
            }
        });

        ChatModel chatModel = new ChatModel();
        chatModel.users.put(mId, true);
        chatModel.users.put(pId2, true);
        chatModel.recentTime.put("recentTime", ServerValue.TIMESTAMP);

        checkChatRoom2(new SimpleCallback<Integer>() {
            @Override
            public void callback(Integer data) {
                if(data==1){
                    // ?????? ?????? ?????? ???
                    noRoom = true;
                    Log.d("test","?????? ??????");

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("calendar").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                title.setVisibility(View.GONE);
                            }

                            for(DataSnapshot item : snapshot.getChildren()) {
                                Log.d("test","item :"+item.getValue());
                                Log.d("test","key : "+item.getKey());
                                if (item.getKey().equals("title")){
                                    cal_title = String.valueOf(item.getValue());
                                    Log.d("test","title : "+cal_title);
                                } else if (item.getKey().equals("start")){
                                    start = String.valueOf(item.getValue());
                                    Log.d("test","start : "+start);
                                } else {
                                    end = String.valueOf(item.getValue());
                                    Log.d("test","end : "+end);

                                }
                            }
                            tv_title.setText(cal_title);
                            tv_time.setText(start+"~"+end);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else if (data==0) {
                    // ?????? ?????? ?????? ???
                    Log.d("test", "?????? ?????? ??????");
                } else {
                    // ?????? ?????? ?????? ???
                    noRoom = false;
                    Log.d("test", "?????? ??????");
                }

                if(!noRoom){
                    btn.setEnabled(false);
                    Log.d("test","?????? ?????? ???");
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("test","?????? ?????? ???2");
                            checkChatRoom();
                        }
                    });
                }
            }
        });

        if(enter.equals("yes")){
            // ?????? ????????? ????????????
            Log.d("test","?????????????");
            text.setInputType(InputType.TYPE_CLASS_TEXT);
            text.setImeOptions(EditorInfo.IME_ACTION_SEND);
            text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if(i==EditorInfo.IME_ACTION_SEND){
                        btn.callOnClick();
                    } else {
                        return false;
                    }
                    return true;
                }
            });
        }

        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i==EditorInfo.IME_ACTION_SEND){
                    btn.callOnClick();
                } else {
                    return false;
                }
                return true;
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel chatModel = new ChatModel();
                chatModel.users.put(mId, true);
                chatModel.users.put(pId2, true);

                Object send_time = ServerValue.TIMESTAMP;

                ChatModel.Comment comment = new ChatModel.Comment();
                comment.pId = mId;
                comment.message = text.getText().toString();
                comment.timestamp = send_time;

                chatModel.recentTime.put("recentTime", send_time);
                chatModel.key.put("key", chatRoomId);

                if(text.getText().toString().length()==0){
                    return;
                }

                if(chatRoomId==null) {
                    btn.setEnabled(false);
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            checkChatRoom();
                        }
                    });
                }
                else {
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            sendGcm();
                            //text.setText("");
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("recentTime").updateChildren(chatModel.recentTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("test","??????");
                        }
                    });

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("key").updateChildren(chatModel.key).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d("test","??????");
                        }
                    });
                }
            }
        });

        HashMap<String, RequestBody> map3 = new HashMap<>();
        map3.put("mId" , Id);
        map3.put("mtId" , Id2);

        HashMap<String, RequestBody> map4 = new HashMap<>();
        map4.put("mId" , Id2);
        map4.put("mtId" , Id);

        // ?????? ?????? ????????? ????????? ???
        Call<ResponseBody> already = retrofitClient.mate.list(token, map4);

        Call<ResponseBody> status = retrofitClient.mate.list(token, map3);
        status.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    String res = response.body().string();
                    if(res.equals("1")) {
                        // ?????? ????????? ???????????? ???
                        btn_accept.setEnabled(false);
                        btn_friend.setEnabled(false);
                        alreadyResult=1;
                        Log.d("test","healthy buddy");
                        btn_accept.setText("Healthy Buddy");
                    } else if (res.equals("0")){
                        // ????????? ????????? ????????? ?????? ???
                        Log.v("test","?????? ??????????");
                        btn_friend.setEnabled(false);
                        btn_accept.setEnabled(true);
                        alreadyResult=1;
                    } else {
                        alreadyFriend(already);
                    }
                } catch(Exception e){
                    Log.v("Test", "error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("test",t.toString());
            }
        });

        btn_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????? ????????? ????????? ??????
                // ??? ????????? ????????? ?????? ????????? ???????????? ????????? ???
                Call<ResponseBody> friend = retrofitClient.mate.create(token, map3);
                friend.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "??????");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity_firebase.this);
                                builder.setTitle("??????")
                                        .setMessage("?????? ????????? ???????????????.")
                                        .setPositiveButton("??????", null)
                                        .create()
                                        .show();

                                btn_friend.setEnabled(false);
                                btn_friend.setText("??????????????????");
                            } else {
                                Log.v("result", "??????");
                             }
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("test",t.toString());
                    }
                });
            }
        });

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ?????? ????????? ?????? ?????? (?????? ????????? ????????? ??? enable)
                /*
                ????????? mate table??? ????????? ????????? mtAccept??? 1??? update??????
                ?????? ????????? ??? ??? ?????? ?????? ????????? ??? ??? ????????? ?????? ????????? ????????? ??? ??? ????????? ??????
                 */
                Call<ResponseBody> accept = retrofitClient.mate.accept(token, map3);
                accept.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "??????");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity_firebase.this);
                                builder.setTitle("??????")
                                        .setMessage("?????? ????????? ??????????????????.\n???????????? ???????????? ???????????????.")
                                        .setPositiveButton("??????", null)
                                        .create()
                                        .show();

                                btn_accept.setEnabled(false);
                                btn_accept.setText("Health Buddy");
                            } else {
                                Log.v("result", "??????");
                            }
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("test",t.toString());
                    }
                });

            }
        });

        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ??????, ????????? ??????, ?????? ?????? ?????????
                popupMenu = new PopupMenu(getApplicationContext(),view);
                getMenuInflater().inflate(R.menu.popup,popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getItemId() == R.id.action_menu1){
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            //intent.setType("video/* image/*");
                            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                            startActivityForResult(intent,PICK_FROM_ALBUM);
                        } else if(menuItem.getItemId()==R.id.action_menu2){
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File photoFile = null;
                            try{
                                photoFile = createImageFile();
                            } catch (IOException ex) {

                            }
                            if(photoFile!=null){
                                Uri photoURI = FileProvider.getUriForFile(MessageActivity_firebase.this,
                                        "com.example.android.fileprovider",
                                        photoFile);
                                imageUri = photoURI;
                                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
                                startActivityForResult(intent,0);
                            }
                        } else if(menuItem.getItemId()==R.id.action_menu3){
                            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                            File videoFile = null;
                            try{
                                videoFile = createVideoFile();
                            } catch (IOException ex) {

                            }
                            if(videoFile!=null){
                                Uri videoURI = FileProvider.getUriForFile(MessageActivity_firebase.this,
                                        "com.example.android.fileprovider",
                                        videoFile);
                                imageUri = videoURI;
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
                                startActivityForResult(intent,1);
                            }
                        } else if (menuItem.getItemId()==R.id.action_menu4){
                            if (alreadyResult == 0){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity_firebase.this);
                                builder.setTitle("??????")
                                        .setMessage("????????? ????????????.")
                                        .setPositiveButton("??????", null)
                                        .create()
                                        .show();
                            } else {
                                // ??????
                                Log.d("test","?????? ??????");
                                Intent intent = new Intent(MessageActivity_firebase.this, CalendarActivity.class);
                                intent.putExtra("chatRoomId",chatRoomId);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(time.getVisibility() == View.VISIBLE) {
                    time.setVisibility(View.GONE);
                } else {
                    time.setVisibility(View.VISIBLE);
                }
                title.animate().setDuration(200).rotation(0f);
            }
        });

        checkChatRoom();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d("test", "onRestart");
        recyclerViewAdapter.getMessageList();

        ChatModel chatModel = new ChatModel();
        chatModel.users.put(mId, true);
        chatModel.users.put(pId2, true);
        chatModel.recentTime.put("recentTime", ServerValue.TIMESTAMP);

        checkChatRoom2(new SimpleCallback<Integer>() {
            @Override
            public void callback(Integer data) {
                if(data==1){
                    // ?????? ?????? ?????? ???
                    noRoom = true;
                    Log.d("test","?????? ??????");

                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("calendar").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                title.setVisibility(View.GONE);
                            }

                            for(DataSnapshot item : snapshot.getChildren()) {
                                Log.d("test","item :"+item.getValue());
                                Log.d("test","key : "+item.getKey());
                                if (item.getKey().equals("title")){
                                    cal_title = String.valueOf(item.getValue());
                                    Log.d("test","title : "+cal_title);
                                } else if (item.getKey().equals("start")){
                                    start = String.valueOf(item.getValue());
                                    Log.d("test","start : "+start);
                                } else {
                                    end = String.valueOf(item.getValue());
                                    Log.d("test","end : "+end);

                                }
                            }
                            tv_title.setText(cal_title);
                            tv_time.setText(start+"~"+end);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else if (data==0) {
                    // ?????? ?????? ?????? ???
                    Log.d("test", "?????? ?????? ??????");
                } else {
                    // ?????? ?????? ?????? ???
                    noRoom = false;
                    Log.d("test", "?????? ??????");
                }

                if(!noRoom){
                    btn.setEnabled(false);
                    Log.d("test","?????? ?????? ???");
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("test","?????? ?????? ???2");
                            checkChatRoom();
                        }
                    });
                }
            }
        });
    }

    void sendGcm(){
        Gson gson = new Gson();

        NotificationModel notificationModel = new NotificationModel();
        FirebaseDatabase.getInstance().getReference().child("users").child(pId2).child("pushToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationModel.to = snapshot.getValue(String.class);
                Log.d("test","to : "+notificationModel.to);
                //notificationModel.notification.title = nick;
                //notificationModel.notification.text = text.getText().toString();
                notificationModel.data.title = nick;
                notificationModel.data.id = pId2;
                if(text.getText().toString()!=null){
                    notificationModel.data.text = text.getText().toString();
                } else {
                    notificationModel.data.text = "??????, ?????????";
                }

                //Log.d("test","text : "+notificationModel.notification.text);
                Log.d("test","text : "+text.getText().toString());
                text.setText("");

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"), gson.toJson(notificationModel));
                Request request = new Request.Builder()
                        .header("Content-Type", "application/json")
                        .addHeader("Authorization", "key=AAAAyNC2qPo:APA91bErZaIwHlMjIsFWHE524WJD4WlHjg4ETohEnLWk62U_nX8HGDrj9zw3p5VBKSc7fHHjeZTf8lX796zfI-ZfNvYFm7PR4a14RFMMwfBNrAdrfLvzTBHqYWX9FnRJk2agk-DUlojR")
                        .url("https://fcm.googleapis.com/fcm/send")
                        .post(requestBody)
                        .build();

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                        Log.d("test","error : "+e.getMessage());
                        Log.d("test","call1 : "+call.toString());
                    }

                    @Override
                    public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                        Log.d("test","call2 : "+call.toString());
                        Log.d("test","response : "+response.toString());
                        Log.d("test", "response2 : "+response.body().string());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //?????? ???????????? ????????? ????????? ????????????
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public String getPreferenceStringEnter(String key) {
        SharedPreferences pref = getSharedPreferences("enter.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    void checkChatRoom(){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+mId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot item : snapshot.getChildren()){
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if(chatModel.users.containsKey(pId2)){
                        chatRoomId = item.getKey();
                        Log.d("test","chatRoomId2 : "+chatRoomId);
                        btn.setEnabled(true);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity_firebase.this));
                        recyclerViewAdapter = new RecyclerViewAdapter();
                        recyclerView.setAdapter(recyclerViewAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("test","error " +error.getMessage());
            }
        });
    }

    void checkChatRoom2(@NonNull SimpleCallback<Integer> finishedCallback){
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+mId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Log.d("test","???????????? ?????? ???");
                    finishedCallback.callback(0);
                    return;
                }
                for(DataSnapshot item : snapshot.getChildren()) {
                    ChatModel chatModel = item.getValue(ChatModel.class);
                    if (chatModel.users.containsKey(pId2)) {
                        chatRoomId = item.getKey();
                        Log.d("test", "chatRoomId2 : " + chatRoomId);
                        btn.setEnabled(true);
                        finishedCallback.callback(1);
                        return;
                    }
                }
                if(chatRoomId==null){
                    finishedCallback.callback(-1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("test","error " +error.getMessage());
            }
        });
    }

    /*
    public interface SimpleCallback {
        void callback(Object data);
    }

     */

    public interface SimpleCallback<T>{
        void callback(T data);
    }

    public void alreadyFriend( Call<ResponseBody> already){
        already.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    String res = response.body().string();
                    if(res.equals("")){
                        // ?????? ?????? ??????????????? ?????? ???
                        Log.v("result", "?????? ??? ??? ??? ??????");
                        Log.d("Test", response.toString());
                        btn_friend.setEnabled(true);
                        alreadyResult = 0;
                        title.setVisibility(View.GONE);
                    } else if(res.equals("0")){
                        // ?????? ?????? ??????????????? ?????? ???
                        Log.v("Test", "?????? ??? ??? ??????");
                        Log.d("Test", "??????"+response.body().string());
                        btn_friend.setEnabled(false);
                        btn_friend.setText("??????????????????");
                        alreadyResult = 0;
                        title.setVisibility(View.GONE);
                    } else {
                        Log.v("Test", "?????? ?????? ??????");
                        Log.d("Test", "??????2" + res);
                        alreadyResult = 1;
                        btn_accept.setText("Healthy Buddy");
                    }
                } catch(Exception e){
                    Log.v("Test", "error");
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("test",t.toString());
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String videoFileName = "MOVIE_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(
                videoFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentVideoPath = video.getAbsolutePath();
        return video;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri.toString().contains("image")){
                Intent intent = new Intent(MessageActivity_firebase.this, PhotoActivity.class);
                intent.putExtra("id2",pId2);
                intent.putExtra("photo",data.getData());
                intent.putExtra("chatRoomId",chatRoomId);
                intent.putExtra("mode",0);
                startActivity(intent);
            } else if (uri.toString().contains("video")){
                Intent intent = new Intent(MessageActivity_firebase.this, PhotoActivity.class);
                intent.putExtra("id2",pId2);
                intent.putExtra("photo",data.getData());
                intent.putExtra("chatRoomId",chatRoomId);
                intent.putExtra("mode",10);
                startActivity(intent);
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK){
            Intent intent = new Intent(MessageActivity_firebase.this, PhotoActivity.class);
            intent.putExtra("id2",pId2);
            intent.putExtra("photo", imageUri);
            intent.putExtra("chatRoomId",chatRoomId);
            intent.putExtra("mode",0);
            startActivity(intent);
        } else if (requestCode == 1 && resultCode == RESULT_OK){
            Intent intent = new Intent(MessageActivity_firebase.this, PhotoActivity.class);
            //Log.d("test","test uri : "+imageUri);
            //Log.d("test","videoPath : "+currentVideoPath);

            intent.putExtra("id2",pId2);
            intent.putExtra("photo", imageUri);
            intent.putExtra("chatRoomId",chatRoomId);
            intent.putExtra("mode",10);
            startActivity(intent);
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        List<ChatModel.Comment> comments;


        public RecyclerViewAdapter(){
            comments = new ArrayList<>();

            FirebaseDatabase.getInstance().getReference().child("users").child(pId2).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    getMessageList();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        void getMessageList(){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for(DataSnapshot item : snapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_notify = item.getValue(ChatModel.Comment.class);
                        comment_notify.readUsers.put(mId,true);
                        Log.d("test","?????? ?????????"+mId);
                        readUsersMap.put(key,comment_notify);
                        comments.add(comment_origin);
                    }

                    if(comments.size()==0) {return;}
                    if(!comments.get(comments.size()-1).readUsers.containsKey(mId)){
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // ???????????? ????????? ??? ???
                                notifyDataSetChanged();

                                recyclerView.scrollToPosition(comments.size()-1);
                            }
                        });
                    } else {
                        // ???????????? ????????? ??? ???
                        notifyDataSetChanged();

                        recyclerView.scrollToPosition(comments.size()-1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageViewHolder messageViewHolder = ((MessageViewHolder)holder);

            long unixTime =  (long)comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            String time_hour_min = hour_min.format(date);
            String time_day = day.format(date);

            long pre;
            long pos;
            String pre_time=null;
            String pos_time=null;

            if(position==0){
                pre = 0;
            } else {
                pre = (long)comments.get(position-1).timestamp;
            }

            // ?????? ????????? ???????????? ?????? ??? ?????? ???????????? timestamp??? ?????????
            if(position==comments.size()-1){
                pos = 0;
            } else {
                pos = (long)comments.get(position+1).timestamp;
            }

            Date pre_date = new Date(pre);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            pre_time = simpleDateFormat.format(pre_date);
            String pre_time_day = day.format(pre_date);

            Date pos_date = new Date(pos);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            pos_time = simpleDateFormat.format(pos_date);

            // ????????? ????????? day ??????
            if(!time_day.equals(pre_time_day)){
                messageViewHolder.textView_day.setVisibility(View.VISIBLE);
                messageViewHolder.textView_day.setText(time_day);
            }

            if(comments.get(position).imgUrl!=null){
                if(comments.get(position).imgUrl.contains("images")){
                    messageViewHolder.textView_message.setVisibility(View.GONE);
                    messageViewHolder.imageView_play.setVisibility(View.GONE);
                    messageViewHolder.frameLayout.setVisibility(View.VISIBLE);
                    messageViewHolder.imageview_message.setVisibility(View.VISIBLE);
                    Glide.with(holder.itemView.getContext())
                            .load(comments.get(position).imgUrl)
                            .override(Target.SIZE_ORIGINAL)
                            .into(messageViewHolder.imageview_message);
                } else if (comments.get(position).imgUrl.contains("videos")) {
                    messageViewHolder.textView_message.setVisibility(View.GONE);
                    messageViewHolder.frameLayout.setVisibility(View.VISIBLE);
                    messageViewHolder.imageview_message.setVisibility(View.VISIBLE);
                    messageViewHolder.imageView_play.setVisibility(View.VISIBLE);
                    Glide.with(holder.itemView.getContext())
                            .load(comments.get(position).imgUrl)
                             .override(Target.SIZE_ORIGINAL)
                            .into(messageViewHolder.imageview_message);
                }
            } else {
                messageViewHolder.frameLayout.setVisibility(View.GONE);
            }

            if(comments.get(position).pId.equals(mId)){
                // ?????? ?????? ???????????? ???????????? ?????? ?????? timestamp ??????
                if((position<comments.size()-1 && !comments.get(position+1).pId.equals(mId)) || position==comments.size()-1 ||!time.equals(pos_time)) {
                    messageViewHolder.textview_name.setVisibility(View.GONE);
                    messageViewHolder.textView_message.setText(comments.get(position).message);
                    messageViewHolder.textView_message.setBackgroundResource(R.drawable.right);
                    messageViewHolder.linearlayout_destination.setVisibility(View.GONE);
                    messageViewHolder.linearlayout_main.setGravity(Gravity.RIGHT);
                    messageViewHolder.linearlayout_right.setGravity(Gravity.RIGHT);
                    setReadCounter(position,messageViewHolder.textview_readCounterLeft);
                    messageViewHolder.textView_timestamp.setText(time_hour_min);
                } else {
                    messageViewHolder.textview_name.setVisibility(View.GONE);
                    messageViewHolder.textView_message.setText(comments.get(position).message);
                    messageViewHolder.textView_message.setBackgroundResource(R.drawable.right);
                    messageViewHolder.linearlayout_destination.setVisibility(View.GONE);
                    messageViewHolder.linearlayout_main.setGravity(Gravity.RIGHT);
                    messageViewHolder.linearlayout_right.setGravity(Gravity.RIGHT);
                    messageViewHolder.textView_timestamp.setVisibility(View.GONE);
                    setReadCounter(position,messageViewHolder.textview_readCounterLeft);
                }
                messageViewHolder.imageview_message.setBackgroundResource(R.drawable.right);
            } else {
                // ???????????? ???
                if( ( position<comments.size()-1 && comments.get(position+1).pId.equals(mId)) || position == comments.size()-1 ){
                    // ????????? ??????????????? ??? ?????? ???????????? ????????? ?????? ???
                    if(( position > 0 && comments.get(position-1).pId.equals(mId)) || !time.equals(pre_time)){
                        // ???????????? ????????? ??????
                        Glide.with(holder.itemView.getContext())
                                .load(destinationUserModel.img)
                                .apply(new RequestOptions().circleCrop())
                                .into(messageViewHolder.imageview_profile);
                        messageViewHolder.textview_name.setText(destinationUserModel.Nickname);
                        messageViewHolder.linearlayout_destination.setVisibility(View.VISIBLE);
                    } else {
                        // ???????????? ????????? ??????, ?????? ???????????? ??????
                        messageViewHolder.textview_name.setVisibility(View.GONE);
                        messageViewHolder.linearlayout_destination.setVisibility(View.INVISIBLE);
                    }
                    messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                    messageViewHolder.textView_message.setText(comments.get(position).message);
                    messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                    messageViewHolder.textView_timestamp.setText(time_hour_min);
                    setReadCounter(position,messageViewHolder.textview_readCounterRight);
                } else {
                    // ????????? ???????????? ?????? ???
                    // ?????? ??? ???????????? ????????? ?????? ??? ???????????? ????????? ?????? ?????? ??? ??? ????????? ??????
                    // ??? ?????? ???????????? ????????? ?????? ?????? ?????? ????????? ??????, ?????? ?????? ?????? ????????? ??????

                    // ?????? ??? ???????????? ????????? ?????? ?????? ???????????? ????????? ?????? ?????? ??? ???????????? ??????
                    // ??? ?????? ???????????? ????????? ?????? ?????? ?????? ????????? ??????, ?????? ?????? ?????? ????????? ??????
                    if(( position > 0 && comments.get(position-1).pId.equals(mId)) || !time.equals(pre_time)){
                        if(!time.equals(pos_time)) {
                            Glide.with(holder.itemView.getContext())
                                    .load(destinationUserModel.img)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(messageViewHolder.imageview_profile);
                            messageViewHolder.textview_name.setText(destinationUserModel.Nickname);
                            messageViewHolder.linearlayout_destination.setVisibility(View.VISIBLE);
                            messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                            messageViewHolder.textView_message.setText(comments.get(position).message);
                            messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                            messageViewHolder.textView_timestamp.setText(time_hour_min);
                            setReadCounter(position,messageViewHolder.textview_readCounterRight);
                        }else {
                            Glide.with(holder.itemView.getContext())
                                    .load(destinationUserModel.img)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(messageViewHolder.imageview_profile);
                            messageViewHolder.textview_name.setText(destinationUserModel.Nickname);
                            messageViewHolder.linearlayout_destination.setVisibility(View.VISIBLE);
                            messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                            messageViewHolder.textView_message.setText(comments.get(position).message);
                            messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                            messageViewHolder.textView_timestamp.setVisibility(View.GONE);
                            setReadCounter(position,messageViewHolder.textview_readCounterRight);
                        }
                    } else {
                        // ?????? ??? ???????????? ????????? ?????? ???
                        if(!time.equals(pos_time)) {
                            messageViewHolder.textview_name.setVisibility(View.GONE);
                            messageViewHolder.linearlayout_destination.setVisibility(View.GONE);
                            messageViewHolder.linearlayout_right.setPadding(155,0,0,0);
                            messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                            messageViewHolder.textView_message.setText(comments.get(position).message);
                            messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                            messageViewHolder.textView_timestamp.setText(time_hour_min);
                            setReadCounter(position,messageViewHolder.textview_readCounterRight);
                        }else {
                            messageViewHolder.textview_name.setVisibility(View.GONE);
                            messageViewHolder.linearlayout_destination.setVisibility(View.GONE);
                            messageViewHolder.linearlayout_right.setPadding(155,0,0,0);
                            messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                            messageViewHolder.textView_message.setText(comments.get(position).message);
                            messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                            messageViewHolder.textView_timestamp.setVisibility(View.GONE);
                            setReadCounter(position,messageViewHolder.textview_readCounterRight);
                        }
                    }
                }
                    messageViewHolder.imageview_message.setBackgroundResource(R.drawable.left);
            }

            messageViewHolder.imageview_message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MessageActivity_firebase.this, PhotoDetailActivity.class);
                    intent.putExtra("nickname", comments.get(position).pId);
                    intent.putExtra("photo", comments.get(position).imgUrl);
                    startActivity(intent);
                }
            });

        }

        void setReadCounter(int position, TextView textView){
            if(peopleCount==0){
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) snapshot.getValue();
                        peopleCount = users.size();
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if(count > 0){
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        }else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();
                if(count > 0){
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                }else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textview_name;
            public TextView textview_readCounterLeft;
            public TextView textview_readCounterRight;
            public ImageView imageview_profile;
            public LinearLayout linearlayout_destination;
            public LinearLayout linearlayout_main;
            public LinearLayout linearlayout_right;
            public TextView textView_timestamp;
            public TextView textView_day;
            public ImageView imageview_message;
            public FrameLayout frameLayout;
            public ImageView imageView_play;

            public MessageViewHolder(View view){
                super(view);
                textView_message = (TextView) view.findViewById(R.id.messageItem_textView_message);
                textview_name = (TextView) view.findViewById(R.id.messageItem_textview_name);
                imageview_profile =  (ImageView) view.findViewById(R.id.messageItem_imageview_profile);
                linearlayout_destination = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_destination);
                linearlayout_main = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = (TextView) view.findViewById(R.id.messageItem_textview_timestamp);
                linearlayout_right = (LinearLayout) view.findViewById(R.id.messageItem_linearlayout_right);
                textview_readCounterLeft = (TextView) view.findViewById(R.id.messageItem_textView_readCounterLeft);
                textview_readCounterRight = (TextView) view.findViewById(R.id.messageItem_textView_readCounterRight);
                textView_day = (TextView) view.findViewById(R.id.messageItem_textView_day);
                imageview_message = (ImageView) view.findViewById(R.id.messageItem_imageView_message);
                frameLayout = (FrameLayout) view.findViewById(R.id.messageItem_framelayout);
                imageView_play = (ImageView) view.findViewById(R.id.messageItem_imageView_play);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }

    @Override
    public void onBackPressed(){
        if(databaseReference!=null && valueEventListener!=null){
            Log.d("test","?????? ?????????");
            databaseReference.removeEventListener(valueEventListener);
        }
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    @Override
    protected void onUserLeaveHint() {
        if(databaseReference!=null && valueEventListener!=null){
            Log.d("test","?????? ?????????");
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}
