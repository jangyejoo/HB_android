package com.example.healthybuddy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.healthybuddy.DTO.AutoLoginResponse;
import com.example.healthybuddy.DTO.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoLoginActivity extends AppCompatActivity {

    private String pId, token_before, mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_login);

        pId=getPreferenceString("id");
        token_before = getPreferenceString("token");
        Log.d("retrofit", "token_before"+token_before);

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);

        progressDialog.show();

        //token이 없다면
        if(pId==null && token_before==null){
            Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        //retrofit 생성
        RetrofitClient retrofitClient = new RetrofitClient();
        Call<AutoLoginResponse> autologin = retrofitClient.login.autoLogin(token_before, pId);

        //loginRequest에 저장된 데이터와 함께 init에서 정의한 getLoginResponse 함수를 실행한 후 응답을 받음
        autologin.enqueue(new Callback<AutoLoginResponse>() {
            @Override
            public void onResponse(Call<AutoLoginResponse> call, Response<AutoLoginResponse> response) {

                Log.d("retrofit", "Data fetch success");

                //통신 성공
                if (response.isSuccessful() && response.body() != null) {

                    //response.body()를 result에 저장
                    AutoLoginResponse result = response.body();

                    //받은 코드 저장
                    String resultCode = result.getResultCode();

                    //받은 토큰 저장
                    String token = result.getToken();
                    String token_refresh = result.getRefreshToken();

                    String success = "good token"; //있던 토큰으로 다시 로그인
                    String newtoken = "new access token"; //새로운 access 토큰 저장 후 다시 로그인
                    String expiredrefresh = "expired refresh token"; //다시 로그인하세요
                    String logout = "logout";

                    Log.d("retrofit", "token : "+token);
                    Log.d("retrofit", "token_refresh : "+token_refresh);
                    Log.d("retrofit", "result : "+resultCode);

                    if (resultCode.equals(success)) {
                        // 그냥 main으로 바로 넘어가보자
                        Intent intent = new Intent(AutoLoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", pId);
                        //progressDialog.dismiss();
                        startActivity(intent);

                    } else if (resultCode.equals(newtoken)) {
                        // 새로운 access token을 저장해야한다
                        // 저장하고 main으로 바로 넘어가보자

                        setPreference("id",pId);
                        setPreference("token", token);

                        Intent intent = new Intent(AutoLoginActivity.this, MainActivity.class);
                        intent.putExtra("userId", pId);
                        //progressDialog.dismiss();
                        startActivity(intent);

                    } else if (resultCode.equals(expiredrefresh)) {
                        // 재로그인을 해야한다!
                        //progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(AutoLoginActivity.this);
                        builder.setTitle("알림")
                                .setMessage("토큰이 만료되었습니다.\n다시 로그인해주세요!")
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    } else {
                        // 로그아웃 상태 조용히 로그인 창으로 이동
                        Intent intent = new Intent(AutoLoginActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                }
            }

            //통신 실패
            @Override
            public void onFailure(Call<AutoLoginResponse> call, Throwable t) {
                Log.d("Test",call.toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(AutoLoginActivity.this);
                builder.setTitle("알림")
                        .setMessage("예기치 못한 오류가 발생하였습니다.\n 고객센터에 문의바랍니다.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(0,0);
        Intent intent = getIntent();
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

    //데이터를 내부 저장소에 저장하기
    public void setPreference(String key, String value) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

}