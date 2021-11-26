package com.example.healthybuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthybuddy.DTO.ChatDTO;
import com.example.healthybuddy.DTO.MessageDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.chatData;
import com.example.healthybuddy.DTO.messageData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private RecyclerView c_RecyclerView = null;
    private String pId, token, cId, crId;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        EditText text = (EditText) findViewById(R.id.message_editText);
        c_RecyclerView = (RecyclerView) findViewById(R.id.message_recyclerview);

        // 키보드 숨기기
        //InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(text.getWindowToken(), 0);

        pId = ((LoginActivity) LoginActivity.context).userID;
        token = "Bearer " + getPreferenceString(pId);

        Intent intent = getIntent();
        cId = intent.getStringExtra("cId");
        crId = intent.getStringExtra("crId");

        Log.d("test","cId : "+cId);
        Log.d("test","crId : "+crId);

        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody room = RequestBody.create(MediaType.parse("text/plain"), crId);
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("crId" , room);
        map.put("mgSender",id);

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
                    Toast.makeText(MessageActivity.this,"다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = null;
                    intent = new Intent(MessageActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });


        Call<List<MessageDTO>> chat = retrofitClient.message.list(token, map);
        chat.enqueue(new Callback<List<MessageDTO>>() {
            @Override
            public void onResponse(Call<List<MessageDTO>> call, Response<List<MessageDTO>> response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.v("result", "실패");
                    } else {
                        ArrayList<messageData> cData = new ArrayList<>();
                        List<MessageDTO> data = response.body();

                        for (MessageDTO post : data) {
                            messageData oItem = new messageData();
                            if(pId.equals(post.getMgSender())){
                                oItem.mgDetail=post.getMgDetail();
                                oItem.mgDate=post.getMgDate();
                                oItem.viewType=1;
                                Log.d("test", "right"+post.getpImg()+" "+ post.getpNickname() +" "+ post.getMgSender()+" "+ post.getMgDetail()+" "+post.getMgDate());
                            } else {
                                oItem.pImg = "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg();
                                oItem.pNickname = post.getpNickname();
                                oItem.mgDetail=post.getMgDetail();
                                oItem.mgDate=post.getMgDate();
                                oItem.viewType=0;
                                Log.d("test", "left"+post.getpImg()+" "+ post.getpNickname() +" "+ post.getMgSender()+" "+ post.getMgDetail()+" "+post.getMgDate());
                            }
                            cData.add(oItem);
                        }
                        // ListView, Adapter 생성 및 연결 ------------------------

                        LinearLayoutManager manager = new LinearLayoutManager(MessageActivity.this,RecyclerView.VERTICAL,false);
                        c_RecyclerView.setLayoutManager(manager);
                        c_RecyclerView.setAdapter(new MessageAdapter(cData));
                        c_RecyclerView.getLayoutManager().scrollToPosition(cData.size()-1);

                        Log.v("Test", "성공");
                    }
                } catch (Exception e) {
                    Log.v("Test", "error : "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<MessageDTO>> call, Throwable t) {

            }
        });

        btn = (Button) findViewById(R.id.message_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("test", "메세지를 보냅니다");

                HashMap<String, RequestBody> map2 = new HashMap<>();
                //RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
                RequestBody detail = RequestBody.create(MediaType.parse("text/plain"), text.getText().toString());
                map2.put("mgSender" , id);
                map2.put("chroom" , room);
                map2.put("mgDetail" , detail);
                Log.d("test", text.getText().toString());

                Call<ResponseBody> chat2 = retrofitClient.message.create(token, map2);
                chat2.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(!response.isSuccessful()){
                            Log.v("result", "실패");
                        } else {
                            Log.v("Test", "성공");
                            text.setText("");
                            // 깜빡이지는 않는데 추가는 안된다
                            //c_RecyclerView.getAdapter().notifyDataSetChanged();
                            // 깜박이면서 추가가 된다
                            //overridePendingTransition(0,0);
                            recreate();

                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("test",t.toString());
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

}
