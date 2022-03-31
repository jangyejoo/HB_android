package com.example.healthybuddy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.healthybuddy.DTO.PwdDTO;
import com.example.healthybuddy.DTO.RegisterDTO;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePasswordActivity extends AppCompatActivity {

    private EditText pwd, newPwd, pwdCheck;
    private Button btn;
    private String pId, token;
    private TextView check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        pwd = (EditText) findViewById(R.id.et_rg_pwd);
        newPwd = (EditText) findViewById(R.id.et_rg_new_pwd);
        pwdCheck = (EditText) findViewById(R.id.et_rg_pwdCheck);
        btn = (Button) findViewById(R.id.btn_rg_update);
        check = (TextView) findViewById(R.id.tv_rg_check);

        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");

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
                    Intent intent = new Intent(UpdatePasswordActivity.this, AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        pwdCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(newPwd.getText().toString().equals(pwdCheck.getText().toString())){
                    check.setText("비밀번호가 일치합니다.");
                    check.setTextColor(Color.parseColor("#000000"));
                    // 가입하기 버튼 활성화
                    btn.setEnabled(true);
                } else {
                    check.setText("비밀번호가 일치하지 않습니다.");
                    check.setTextColor(Color.parseColor("#FF0000"));
                    // 가입하기 버튼 비활성화
                    btn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(newPwd.getText().toString().equals(pwdCheck.getText().toString())){
                    check.setText("비밀번호가 일치합니다.");
                    // 가입하기 버튼 활성화
                    btn.setEnabled(true);
                } else {
                    check.setText("비밀번호가 일치하지 않습니다.");
                    check.setTextColor(Color.parseColor("#FF0000"));
                    // 가입하기 버튼 비활성화
                    btn.setEnabled(false);
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PwdDTO dto = new PwdDTO(pId, pwd.getText().toString(), newPwd.getText().toString());

                // 비밀번호 정규식
                Pattern pattern = Pattern.compile("^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[`~!@$!%*#^?&\\\\(\\\\)\\\\-_=+])(?!.*[^a-zA-z0-9`~!@$!%*#^?&\\\\(\\\\)\\\\-_=+]).{8,16}$"); // 영문자/숫자/특수문자 포함 8~16자
                Matcher matcher = pattern.matcher(newPwd.getText().toString());

                if(newPwd.getText().toString().length()==0){
                    Toast.makeText(UpdatePasswordActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    newPwd.requestFocus();
                    return;
                } else {
                    if(!matcher.find()){
                        Toast.makeText(UpdatePasswordActivity.this, "비밀번호 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                        newPwd.setText("");
                        newPwd.requestFocus();
                        return;
                    }
                }

                Call<ResponseBody> update = retrofitClient.register.updatePwd(token, dto);

                update.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try{
                            if(!response.isSuccessful()){
                                Log.v("result", "실패");
                            } else {
                                if(response.body().string().equals("1")){
                                    Log.v("Test", "성공");
                                    Call<Void> logout = retrofitClient.login.logout(token, pId);
                                    logout.enqueue(new Callback<Void>(){
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response){
                                            if(response.isSuccessful()){
                                                AlertDialog.Builder builder = new AlertDialog.Builder(UpdatePasswordActivity.this);
                                                builder.setTitle("알림")
                                                        .setMessage("비밀번호가 변경되었습니다.\n다시 로그인해주세요.")
                                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                Intent intent = new Intent(UpdatePasswordActivity.this, LoginActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        })
                                                        .create()
                                                        .show();
                                            } else {
                                                Log.d("test", "실패");
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t){
                                            t.printStackTrace();
                                        }
                                    });
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(UpdatePasswordActivity.this);
                                    builder.setTitle("알림")
                                            .setMessage("틀린 비밀번호입니다.\n다시 입력해주세요.")
                                            .setPositiveButton("확인", null)
                                            .create()
                                            .show();
                                }
                            }
                        }catch (Exception e){
                            Log.v("Test", "error");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }
        });

    }

    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

}