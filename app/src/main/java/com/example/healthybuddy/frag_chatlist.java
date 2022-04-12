package com.example.healthybuddy;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.Int4;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthybuddy.DTO.ChatModel;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.itemData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_chatlist extends Fragment {

    private String pId, token, gym;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private List<itemData> destinationUserModels = new ArrayList<>();
    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private int alreadyRead;
    private int chatSize;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_chatlist, container, false);

        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        gym = getPreferenceString("gym");
        Log.d("test","현재 사용자의 gym : "+gym);

        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("pId" , id);

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
                    Intent intent = new Intent(getContext(), AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.chatlist_recyclerview);
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter();
        recyclerView.setAdapter(chatRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("test","resume");
        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.chatlist_recyclerview);
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter();
        recyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerViewAdapter.notifyDataSetChanged();
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        private List<ChatModel> sort_before = new ArrayList<>();
        //private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter(){
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+pId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatModels.clear();
                    for(DataSnapshot item: snapshot.getChildren()){
                        chatModels.add(item.getValue(ChatModel.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chatlist_item, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            CustomViewHolder customViewHolder = (CustomViewHolder)holder;
            Log.d("test","onBindViewHolder");

            String destinationId = null;
            itemData destinationUserModel = new itemData();

            Collections.sort(chatModels);

            // 챗방에 있는 유저를 일일히 체크
            for(String user : chatModels.get(position).users.keySet()){
                if(!user.equals(pId)){
                    destinationId = user;
                    //destinationUsers.add(destinationId);
                }
            }

            // 안읽은 메시지 개수 세기?
            // 한 채팅방의 comments 개수 - 내가 읽은 comments 개수
            // chatModels.get(position).comments.size() -> 한 채팅방의 comments 개수
            // chatModels.get(position).comments의 readUsers = "my Id" 를 다 검색해서 나온 개수
            String key = (String) chatModels.get(position).key.get("key");
            int [] alreadyRead = new int[chatModels.size()];

            String finalDestinationId = destinationId;
            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(key).child("comments").orderByChild("readUsers/"+pId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    alreadyRead[position] = 0;
                    Log.d("test","alreadyRead == 0");
                    for(DataSnapshot item : snapshot.getChildren()) {
                        Log.d("test", "readUsers : "+item.getKey());
                        Log.d("test","alreadyRead++");
                        alreadyRead[position]++;
                        Log.d("test","현재 alreadyRead : "+alreadyRead[position]);
                    }

                    RetrofitClient retrofitClient = new RetrofitClient();

                    HashMap<String, RequestBody> destinationUser = new HashMap<>();
                    RequestBody Id = RequestBody.create(MediaType.parse("text/plain"), pId);
                    RequestBody Id2 = RequestBody.create(MediaType.parse("text/plain"), finalDestinationId);
                    destinationUser.put("pId", Id2);

                    Call<ProfileDTO> profile = retrofitClient.profile.profile(token, destinationUser);
                    profile.enqueue(new Callback<ProfileDTO>() {
                        @Override
                        public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                            try{
                                if(!response.isSuccessful()){
                                    Log.d("test","뭔가 잘못됐다");
                                }else {
                                    Log.d("test","onBindViewHolder_profile_start");
                                    ProfileDTO post = response.body();

                                    destinationUserModel.gym = post.getpGym();

                                    if(destinationUserModel.gym.equals(gym)){
                                        destinationUserModel.img= "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/" + post.getpImg();
                                        destinationUserModel.Nickname = post.getpNickname();
                                        destinationUserModel.id2 = post.getpId();
                                        Glide.with(customViewHolder.itemView.getContext())
                                                .load(destinationUserModel.img)
                                                .apply(new RequestOptions().circleCrop())
                                                .into(customViewHolder.imageView);
                                        customViewHolder.textView_title.setText(destinationUserModel.Nickname);

                                        if (chatModels.get(position).comments.size()-alreadyRead[position]!=0){
                                            if (chatModels.get(position).comments.size()-alreadyRead[position] > 10) {
                                                customViewHolder.textView_newmessage.setVisibility(View.VISIBLE);
                                                customViewHolder.textView_newmessage.setText("10+");
                                            } else {
                                                customViewHolder.textView_newmessage.setVisibility(View.VISIBLE);
                                                customViewHolder.textView_newmessage.setText(String.valueOf(chatModels.get(position).comments.size() - alreadyRead[position]));
                                            }
                                        }
                                        Log.d("test","chatSize : "+chatModels.get(position).comments.size());
                                        Log.d("test","alreadyRead : "+alreadyRead[position]);

                                        Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
                                        commentMap.putAll(chatModels.get(position).comments);
                                        String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                                        customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);

                                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                                        long unixTime = (long)chatModels.get(position).comments.get(lastMessageKey).timestamp;
                                        Date date = new Date(unixTime);
                                        customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));

                                        //destinationUserModels.add(destinationUserModel);
                                        //Collections.sort(destinationUserModels);

                                        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(view.getContext(), MessageActivity_firebase.class);
                                                intent.putExtra("id2",destinationUserModel.id2);

                                                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright, R.anim.toleft);
                                                startActivity(intent, activityOptions.toBundle());
                                            }
                                        });

                                    } else {
                                        destinationUserModel.img= "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/test/user.png";
                                        destinationUserModel.Nickname = "헬스장이 변경되었습니다.";
                                        destinationUserModel.id2 = post.getpId();
                                        Glide.with(customViewHolder.itemView.getContext())
                                                .load(destinationUserModel.img)
                                                .apply(new RequestOptions().circleCrop())
                                                .into(customViewHolder.imageView);
                                        customViewHolder.textView_title.setText(destinationUserModel.Nickname);

                                        //destinationUserModels.add(destinationUserModel);

                                    }
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


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            Log.d("test","onBindViewHolder_profile_end");

        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title, textView_last_message, textView_timestamp, textView_newmessage;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.chatlistitem_imageview);
                textView_title = (TextView)view.findViewById(R.id.chatlistitem_textview_title);
                textView_last_message = (TextView) view.findViewById(R.id.chatlistitem_textview_lastMessage);
                textView_timestamp = (TextView) view.findViewById(R.id.chatlistitem_textview_timestamp);
                textView_newmessage = (TextView) view.findViewById(R.id.chatlistitem_textview_newmessage);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }

    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

}
