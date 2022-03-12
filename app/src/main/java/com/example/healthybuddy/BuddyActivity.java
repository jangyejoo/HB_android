package com.example.healthybuddy;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BuddyActivity extends AppCompatActivity {
    private ImageView pImg;
    private TextView pNickname, pGym, pDetail, pAge, pHeight, pWeight;
    private Button btn_delete;
    private Spinner sp_pAge, sp_pHeight, sp_pWeight;
    private CheckBox mon, tue, wed, thr, fri, sat, sun, open;
    private RadioGroup rg_pSex;
    private RadioButton pMale, pFemale;
    private String pId, pRoutine, token;
    private String pSex, pOpen, mate;
    private HashMap<String, RequestBody> map2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buddy);

        // 값 가져오기
        pImg = (ImageView) findViewById(R.id.iv_img);
        pNickname = (TextView) findViewById(R.id.tv_nickname);
        pGym = (TextView) findViewById(R.id.tv_gym);
        pAge = (TextView) findViewById(R.id.spinner_age);
        pHeight = (TextView) findViewById(R.id.spinner_height);
        pWeight = (TextView) findViewById(R.id.spinner_weight);
        pMale = (RadioButton) findViewById(R.id.rb_male);
        pFemale = (RadioButton) findViewById(R.id.rb_female);
        rg_pSex = (RadioGroup) findViewById(R.id.rg_sex);
        mon = (CheckBox) findViewById(R.id.cb_mon);
        tue = (CheckBox) findViewById(R.id.cb_tue);
        wed = (CheckBox) findViewById(R.id.cb_wed);
        thr = (CheckBox) findViewById(R.id.cb_thr);
        fri = (CheckBox) findViewById(R.id.cb_fri);
        sat = (CheckBox) findViewById(R.id.cb_sat);
        sun = (CheckBox) findViewById(R.id.cb_sun);
        pDetail = (TextView) findViewById(R.id.et_msg);
        btn_delete = (Button) findViewById(R.id.btn_delete);

        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        pNickname.setText(pId);
        mate="";
        map2 = new HashMap<>();

        RetrofitClient retrofitClient = new RetrofitClient();
        Call<List<RegisterDTO>> member = retrofitClient.login.members(token);

        member.enqueue(new Callback<List<RegisterDTO>>(){
            @Override
            public void onResponse(Call<List<RegisterDTO>> call, Response<List<RegisterDTO>> response){
                if(response.isSuccessful()){
                    List<RegisterDTO> data = response.body();
                    Log.d("Test", data.get(0).getUSER_ID());
                } else {
                    Log.d("Test", "인증실패");
                    Toast.makeText(BuddyActivity.this,"다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = null;
                    intent = new Intent(BuddyActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        // pId를 보내면 pId의 친구를 보여준다!
        //select * from mate where mId="rds1" or mtId="rds1";
        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("pId", id);

        Call<ProfileDTO> buddy = retrofitClient.mate.buddy(token, map);
        buddy.enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                try{
                    if(!response.isSuccessful()) {
                        Log.d("test","뭔가 잘못됐다");
                    } else {
                        ProfileDTO post = response.body();
                        Glide.with(pImg.getContext()).load("https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg()).into(pImg);
                        pNickname.setText(post.getpNickname());
                        pGym.setText(post.getpGym());
                        pAge.setText(post.getpAge());
                        pHeight.setText(post.getpHeight());
                        pWeight.setText(post.getpWeight());
                        pDetail.setText(post.getpDetail());

                        // mtId 받아오기
                        mate=post.getpId();
                        Log.d("test","mate"+mate);
                        RequestBody mt = RequestBody.create(MediaType.parse("text/plain"), mate);
                        map2.put("mId", id);
                        map2.put("mtId", mt);

                        // 루틴
                        char[] arr = new char[7];
                        for (int i = 0; i <7; i++) {
                            arr[i] = post.getpRoutine().charAt(i);
                        }

                        Log.d("Test", String.valueOf(arr));
                        if(arr[6]=='1'){ mon.setChecked(true); }
                        if(arr[5]=='1'){ tue.setChecked(true); }
                        if(arr[4]=='1'){ wed.setChecked(true); }
                        if(arr[3]=='1'){ thr.setChecked(true); }
                        if(arr[2]=='1'){ fri.setChecked(true); }
                        if(arr[1]=='1'){ sat.setChecked(true); }
                        if(arr[0]=='1'){ sun.setChecked(true); }

                        // 성별 표시
                        if(post.getpSex()==0){
                            pMale.setChecked(true);
                        }
                        if(post.getpSex()==1){
                            pFemale.setChecked(true);
                        }
                    }

                }catch (Exception e){
                    Log.v("Test", "catch"+e.toString());
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Log.v("test","failure"+t.toString());
                Intent intent = new Intent(BuddyActivity.this, MemberActivity.class);
                intent.putExtra("userId", pId);
                startActivity(intent);
            }
        });

        // 친구 삭제
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Call<ResponseBody> delete = retrofitClient.mate.delete(token, map2);
                delete.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try{
                            if(response.body().string().equals("1")){
                                Log.d("test","성공");
                                Log.d("test",response.body().toString());
                                AlertDialog.Builder builder = new AlertDialog.Builder(BuddyActivity.this);
                                builder.setTitle("알림")
                                        .setMessage("친구를 삭제합니다.\n프로필을 공개로 변경합니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent intent = new Intent(BuddyActivity.this, MemberActivity.class);
                                                intent.putExtra("userId", pId);
                                                startActivity(intent);
                                            }
                                        })
                                        .create()
                                        .show();
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        }catch (Exception e){
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
        });
    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

}
