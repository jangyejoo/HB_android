package com.example.healthybuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthybuddy.DTO.ChatDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.chatData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAllActivity extends AppCompatActivity {
    private ListView c_RecyclerView = null;
    private String pId, token, result;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setTitle("채팅");

        pId = ((LoginActivity) LoginActivity.context).userID;
        token = "Bearer " + getPreferenceString(pId);

        Intent intent = getIntent();
        result = intent.getStringExtra("id2");
        Log.d("test","id2 : "+result);

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
                    Toast.makeText(ChatAllActivity.this,"다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = null;
                    intent = new Intent(ChatAllActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        Call<List<ChatDTO>> chat = retrofitClient.chat.listAll(token, map);
        chat.enqueue(new Callback<List<ChatDTO>>() {
            @Override
            public void onResponse(Call<List<ChatDTO>> call, Response<List<ChatDTO>> response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.v("result", "실패");
                    } else {
                        ArrayList<chatData> cData = new ArrayList<>();
                        List<ChatDTO> data = response.body();

                        for (ChatDTO post : data) {
                            chatData oItem = new chatData();
                            oItem.pImg = "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg();
                            oItem.pNickname = post.getpNickname();
                            oItem.cId = post.getcId();
                            oItem.crId = post.getcrId();
                            Log.d("test", post.getcId()+" "+ post.getcrId() +" "+ post.getpImg()+" "+ post.getpNickname());
                            cData.add(oItem);
                        }

                        // ListView, Adapter 생성 및 연결 ------------------------
                        c_RecyclerView = (ListView) findViewById(R.id.chat_recyclerView);
                        ChatAdapter oAdapter = new ChatAdapter(cData);
                        c_RecyclerView.setAdapter(oAdapter);

                        Log.v("Test", "성공");

                    }
                } catch (Exception e) {
                    Log.v("Test", "error : "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<ChatDTO>> call, Throwable t) {

            }
        });


    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

}
