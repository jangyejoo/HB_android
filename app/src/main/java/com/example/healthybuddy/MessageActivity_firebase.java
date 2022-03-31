package com.example.healthybuddy;

import static android.text.InputType.TYPE_CLASS_TEXT;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
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
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity_firebase extends AppCompatActivity {

    private EditText text;
    private String mId, token, pId2, chatRoomId, enter;
    private Button btn, btn_friend, btn_accept, btn_plus;
    private RecyclerView recyclerView;
    private itemData destinationUserModel = new itemData();

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private SimpleDateFormat hour_min = new SimpleDateFormat("a HH:mm");
    private SimpleDateFormat day = new SimpleDateFormat("yyyy년 MM월 dd일 E요일");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_firebase);

        mId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        enter = getPreferenceStringEnter(mId);

        pId2 = getIntent().getStringExtra("id2");
        btn = (Button) findViewById(R.id.message_btn);
        btn_accept = (Button) findViewById(R.id.btn_accept);
        btn_friend = (Button) findViewById(R.id.btn_friend);
        btn_plus = (Button)findViewById(R.id.message_btn_plus);
        text = (EditText) findViewById(R.id.message_editText);
        recyclerView = (RecyclerView)findViewById(R.id.message_recyclerview);

        RetrofitClient retrofitClient = new RetrofitClient();

        // 토큰 인증
        Call<List<RegisterDTO>> member = retrofitClient.login.members(token);
        member.enqueue(new Callback<List<RegisterDTO>>(){
            @Override
            public void onResponse(Call<List<RegisterDTO>> call, Response<List<RegisterDTO>> response){
                if(response.isSuccessful()){
                    List<RegisterDTO> data = response.body();
                    Log.d("Test", data.get(0).getUSER_ID());
                } else {
                    Log.d("Test", "인증실패");
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
                        Log.d("test","뭔가 잘못됐다");
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

        checkChatRoom2(new SimpleCallback<Integer>() {
            @Override
            public void callback(Integer data) {
                if(data==1){
                    // 찾는 방이 있을 때
                    noRoom = true;
                    Log.d("test","방이 있어");
                } else if (data==0) {
                    // 방이 아예 없을 때
                    Log.d("test", "방이 아예 없어");
                } else {
                    // 찾는 방이 없을 때
                    noRoom = false;
                    Log.d("test", "방이 없어");
                }

                if(!noRoom){
                    btn.setEnabled(false);
                    Log.d("test","대체 이게 왜");
                    FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("test","대체 이게 왜2");
                            checkChatRoom();
                        }
                    });
                }
            }
        });

        if(enter.equals("yes")){
            // 엔터 누르면 보내지게
            Log.d("test","바뀌었나?");
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

                ChatModel.Comment comment = new ChatModel.Comment();
                comment.pId = mId;
                comment.message = text.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;

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
                            //sendGcm();
                            text.setText("");
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

        // 이미 친구 요청을 보냈을 때
        Call<ResponseBody> already = retrofitClient.mate.list(token, map4);

        Call<ResponseBody> status = retrofitClient.mate.list(token, map3);
        status.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    String res = response.body().string();
                    if(res.equals("1")) {
                        // 이미 친구가 되어있을 때
                        btn_accept.setEnabled(false);
                        btn_friend.setEnabled(false);
                        btn_accept.setText("Healthy Buddy");
                    } else if (res.equals("0")){
                        // 친구는 아닌데 요청은 왔을 때
                        Log.v("test","이게 아닌데?");
                        btn_friend.setEnabled(false);
                        btn_accept.setEnabled(true);
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
                // 친구 요청을 보내는 버튼
                // 두 사람의 프로필 공개 여부를 비공개로 바꿔야 함
                Call<ResponseBody> friend = retrofitClient.mate.create(token, map3);
                friend.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "성공");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity_firebase.this);
                                builder.setTitle("알림")
                                        .setMessage("친구 요청을 보냈습니다.")
                                        .setPositiveButton("확인", null)
                                        .create()
                                        .show();

                                btn_friend.setEnabled(false);
                                btn_friend.setText("친구요청보냄");
                            } else {
                                Log.v("result", "실패");
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
                // 친구 요청을 받는 버튼 (친구 요청을 받았을 때 enable)
                /*
                만약에 mate table에 정보가 있으면 mtAccept를 1로 update한다
                따로 친구를 볼 수 있는 창이 있어야 할 듯 거기서 친구 삭제와 차단을 할 수 있도록 하자
                 */
                Call<ResponseBody> accept = retrofitClient.mate.accept(token, map3);
                accept.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "성공");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity_firebase.this);
                                builder.setTitle("알림")
                                        .setMessage("친구 요청을 승락했습니다.\n프로필을 비공개로 변경합니다.")
                                        .setPositiveButton("확인", null)
                                        .create()
                                        .show();

                                btn_accept.setEnabled(false);
                                btn_accept.setText("Health Buddy");
                            } else {
                                Log.v("result", "실패");
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
                // 사진, 동영상 첨부, 일정 잡기 메뉴로
                final PopupMenu popupMenu = new PopupMenu(getApplicationContext(),view);
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
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        checkChatRoom();
    }


    //내부 저장소에 저장된 데이터 가져오기
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
                        recyclerView.setAdapter(new RecyclerViewAdapter());
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
                    Log.d("test","데이터가 없을 때");
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
                        // 내가 보낸 친구요청이 없을 때
                        Log.v("result", "이거 안 된 거 맞냐");
                        Log.d("Test", response.toString());
                        btn_friend.setEnabled(true);
                    } else if(res.equals("0")){
                        // 내가 보낸 친구요청이 있을 때
                        Log.v("Test", "이거 된 거 맞냐");
                        Log.d("Test", "내용"+response.body().string());
                        btn_friend.setEnabled(false);
                        btn_friend.setText("친구요청보냄");
                    } else {
                        Log.v("Test", "너네 이미 친구");
                        Log.d("Test", "내용2" + res);
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
                        Log.d("test","이게 뭐냐고"+mId);
                        readUsersMap.put(key,comment_notify);
                        comments.add(comment_origin);
                    }

                    if(comments.size()==0) {return;}
                    if(!comments.get(comments.size()-1).readUsers.containsKey(mId)){
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomId).child("comments")
                                .updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                // 메시지가 갱신이 될 때
                                notifyDataSetChanged();

                                recyclerView.scrollToPosition(comments.size()-1);
                            }
                        });
                    } else {
                        // 메시지가 갱신이 될 때
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

            // 지금 마지막 메세지가 아닐 때 다음 메세지의 timestamp를 불러옴
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

            // 날짜가 바뀌면 day 출력
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
                if(position==comments.size()-1 ||!time.equals(pos_time)) {
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
                if(position==comments.size()-1){
                    // 마지막 메세지인데 그 전에 보낸거랑 시간이 다를 때
                    if(!time.equals(pre_time)){
                        // 프로필과 닉네임 출력
                        Glide.with(holder.itemView.getContext())
                                .load(destinationUserModel.img)
                                .apply(new RequestOptions().circleCrop())
                                .into(messageViewHolder.imageview_profile);
                        messageViewHolder.textview_name.setText(destinationUserModel.Nickname);
                        messageViewHolder.linearlayout_destination.setVisibility(View.VISIBLE);

                    } else {
                        // 프로필과 닉네임 생략, 타임 스탬프는 출력
                        messageViewHolder.textview_name.setVisibility(View.GONE);
                        messageViewHolder.linearlayout_destination.setVisibility(View.INVISIBLE);

                    }
                    messageViewHolder.textView_message.setBackgroundResource(R.drawable.left);
                    messageViewHolder.textView_message.setText(comments.get(position).message);
                    messageViewHolder.linearlayout_main.setGravity(Gravity.LEFT);
                    messageViewHolder.textView_timestamp.setText(time_hour_min);
                    setReadCounter(position,messageViewHolder.textview_readCounterRight);
                } else {
                    // 마지막 메세지가 아닐 때

                    // 바로 전 메세지랑 시간이 다를 때 프로필과 닉네임 출력 근데 또 두 가지로 나뉨
                    // 그 다음 메세지랑 시간이 같을 때는 타임 스탬프 생략, 다를 때는 타임 스탬프 출력

                    // 바로 전 메세지랑 시간이 같을 때는 프로필과 닉네임 생략 근데 또 두가지로 나뉨
                    // 그 다음 메세지랑 시간이 같을 때는 타임 스탬프 생략, 다를 때는 타임 스탬프 출력
                    if(!time.equals(pre_time)){
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
                        // 바로 전 메세지랑 시간이 같을 때
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
            Log.d("test","대체 뭐냐구");
            databaseReference.removeEventListener(valueEventListener);
        }
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}