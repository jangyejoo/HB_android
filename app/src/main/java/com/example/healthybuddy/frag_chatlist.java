package com.example.healthybuddy;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
        recyclerView.setAdapter(new ChatRecyclerViewAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));

        return view;
    }

    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private List<ChatModel> chatModels = new ArrayList<>();
        //private String uid;
        private ArrayList<String> destinationUsers = new ArrayList<>();
        private List<itemData> destinationUserModels = new ArrayList<>();

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

            String destinationId = null;
            itemData destinationUserModel = new itemData();

            // 챗방에 있는 유저를 일일히 체크
            for(String user : chatModels.get(position).users.keySet()){
                if(!user.equals(pId)){
                    destinationId = user;
                    //destinationUsers.add(destinationId);
                }
            }

            RetrofitClient retrofitClient = new RetrofitClient();

            HashMap<String, RequestBody> destinationUser = new HashMap<>();
            RequestBody Id = RequestBody.create(MediaType.parse("text/plain"), pId);
            RequestBody Id2 = RequestBody.create(MediaType.parse("text/plain"), destinationId);
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

                                Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.reverseOrder());
                                commentMap.putAll(chatModels.get(position).comments);
                                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                                customViewHolder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);

                                destinationUserModels.add(destinationUserModel);

                                customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(view.getContext(), MessageActivity_firebase.class);
                                        intent.putExtra("id2",destinationUserModel.id2);

                                        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright, R.anim.toleft);
                                        startActivity(intent, activityOptions.toBundle());
                                    }
                                });

                                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                                long unixTime = (long)chatModels.get(position).comments.get(lastMessageKey).timestamp;
                                Date date = new Date(unixTime);
                                customViewHolder.textView_timestamp.setText(simpleDateFormat.format(date));

                            } else {
                                destinationUserModel.img= "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/test/user.png";
                                destinationUserModel.Nickname = "헬스장이 변경되었습니다.";
                                destinationUserModel.id2 = post.getpId();
                                Glide.with(customViewHolder.itemView.getContext())
                                        .load(destinationUserModel.img)
                                        .apply(new RequestOptions().circleCrop())
                                        .into(customViewHolder.imageView);
                                customViewHolder.textView_title.setText(destinationUserModel.Nickname);

                                destinationUserModels.add(destinationUserModel);

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
        public int getItemCount() {
            return chatModels.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            public ImageView imageView;
            public TextView textView_title, textView_last_message, textView_timestamp;

            public CustomViewHolder(View view) {
                super(view);

                imageView = (ImageView) view.findViewById(R.id.chatlistitem_imageview);
                textView_title = (TextView)view.findViewById(R.id.chatlistitem_textview_title);
                textView_last_message = (TextView) view.findViewById(R.id.chatlistitem_textview_lastMessage);
                textView_timestamp = (TextView) view.findViewById(R.id.chatlistitem_textview_timestamp);
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
