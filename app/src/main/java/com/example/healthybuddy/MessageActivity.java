package com.example.healthybuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healthybuddy.DTO.MessageDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.messageData;

import java.io.IOException;
import java.lang.ref.Reference;
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
    private String pId, token, cId, crId, cNick;
    private Button btn, btn_friend, btn_accept;

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
        cNick = intent.getStringExtra("cNick");
        setTitle(cNick);

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

        HashMap<String, RequestBody> map3 = new HashMap<>();
        RequestBody mtId = RequestBody.create(MediaType.parse("text/plain"), cId);
        map3.put("mId" , id);
        map3.put("mtId" , mtId);
        HashMap<String, RequestBody> map4 = new HashMap<>();
        map4.put("mId" , mtId);
        map4.put("mtId" , id);

        btn_accept = (Button) findViewById(R.id.btn_accept);
        //HashMap<String, RequestBody> map4 = new HashMap<>();
        //map4.put("mtId" , id);
        Call<ResponseBody> chat4 = retrofitClient.mate.list(token, map3);

        // 이미 친구 요청을 보냈을 때
        Call<ResponseBody> chat6 = retrofitClient.mate.list(token, map4);

        Call<ResponseBody> chat7 = retrofitClient.mate.list(token, map3);
        chat7.enqueue(new Callback<ResponseBody>() {
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
                        alreadyFriend(chat6);
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

        btn_friend = (Button) findViewById(R.id.btn_friend);
        btn_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 친구 요청을 보내는 버튼
                // 두 사람의 프로필 공개 여부를 비공개로 바꿔야 함
                Call<ResponseBody> chat3 = retrofitClient.mate.create(token, map3);
                chat3.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "성공");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
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
                Call<ResponseBody> chat5 = retrofitClient.mate.accept(token, map3);
                chat5.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            if(response.body().string().equals("1")) {
                                Log.v("Test", "성공");

                                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
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

    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public void alreadyFriend( Call<ResponseBody> chat6){
        chat6.enqueue(new Callback<ResponseBody>() {
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
                        //btn_friend.setEnabled(false);
                        //btn_accept.setEnabled(false);
                        //btn_accept.setText("이미 친구 사이~");
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
}
