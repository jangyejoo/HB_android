package com.example.healthybuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healthybuddy.DTO.IdDTO;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.itemData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MateActivity extends AppCompatActivity {
    private ListView m_ListView =null;
    private String pId, token;
    private String sex, height, weight, age, routine;
    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mate);

        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");

        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("pId", id);

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
                    Toast.makeText(MateActivity.this,"다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = null;
                    intent = new Intent(MateActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        // memberlist
        Call<List<ProfileDTO>> mates = retrofitClient.profile.members(token, map);
        mates.enqueue(new Callback<List<ProfileDTO>>() {
            @Override
            public void onResponse(Call<List<ProfileDTO>> call, Response<List<ProfileDTO>> response) {
                try {
                    if (!response.isSuccessful()) {
                        Log.v("result", "실패");
                    } else {
                        ArrayList<itemData> oData = new ArrayList<>();
                        List<ProfileDTO> data = response.body();


                        for (ProfileDTO post : data) {
                            TextView gym = (TextView) findViewById(R.id.tv_gym);
                            gym.setText(post.getpGym());
                            sex="";
                            height="";
                            weight="";
                            age="";
                            routine="";

                            char[] arr = new char[7];
                            for (int i = 0; i <7; i++) {
                                arr[i] = post.getpRoutine().charAt(i);
                            }
                            Log.d("Test", String.valueOf(arr));
                            if(arr[6]=='1'){ routine+="월/"; }
                            if(arr[5]=='1'){ routine+="화/"; }
                            if(arr[4]=='1'){ routine+="수/"; }
                            if(arr[3]=='1'){ routine+="목/"; }
                            if(arr[2]=='1'){ routine+="금/"; }
                            if(arr[1]=='1'){ routine+="토/"; }
                            if(arr[0]=='1'){ routine+="일/"; }

                            Log.v("Test", routine);

                            itemData oItem = new itemData();
                            oItem.img = "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg();
                            oItem.Nickname = post.getpNickname();
                            oItem.Detail = post.getpDetail();
                            oItem.id2 = post.getpId();
                            // 성별, 키, 몸무게, 나이, 루틴
                            // 여, 180cm, 59kg, 1999년, 월수금

                            if(post.getpSex()==0) { sex="남 "; }else { sex="여 "; }
                            if(post.getpHeight().equals("비공개")) { height=""; } else { height=post.getpHeight()+" "; }
                            if(post.getpWeight().equals("비공개")) { weight=""; }else { weight= post.getpWeight()+" "; }
                            if(post.getpAge().equals("비공개")) { age=""; }else { age=post.getpAge()+" ";}

                            oItem.Info = sex+height+weight+age+routine.substring(0,routine.length()-1);
                            if(post.getpOpen()==1){oData.add(oItem);}
                        }

                        // ListView, Adapter 생성 및 연결 ------------------------
                        m_ListView = (ListView) findViewById(R.id.listView);
                        ListAdapter oAdapter = new ListAdapter(oData);
                        m_ListView.setAdapter(oAdapter);

                        Log.v("Test", "성공");

                    }
                } catch (Exception e) {
                    Log.v("Test", "error : "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<ProfileDTO>> call, Throwable t) {
                Log.v("Test", "error :" +t.toString());
            }
        });

    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }
}

