package com.example.healthybuddy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterActivity extends Activity {
    private Button btn_duplicate, btn_register;
    private EditText userId, userPwd, userPwdCheck, userMail;
    private TextView pwd_check;
    private static String BASE_URL = "http://http://hb-env.eba-4ndxmyvz.ap-northeast-2.elasticbeanstalk.com/";
    private Register Register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 값 가져오기
        userId = (EditText) findViewById(R.id.et_rg_id);
        userPwd = (EditText) findViewById(R.id.et_rg_pwd);
        userPwdCheck = (EditText) findViewById(R.id.et_rg_pwdCheck);
        userMail = (EditText) findViewById(R.id.et_rg_mail);
        btn_register = (Button)findViewById(R.id.btn_rg_register);
        btn_duplicate = (Button)findViewById(R.id.btn_duplicate);
        pwd_check = (TextView)findViewById(R.id.tv_rg_check);

        // 아이디 중복 여부
        btn_duplicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //대충 mId 보내서 중복이면 0, 아니면 1 리턴해주는 서버 api
                RetrofitClient retrofitClient = new RetrofitClient();
                IdDTO dto = new IdDTO(userId.getText().toString());

                Call<ResponseBody> idcheck = retrofitClient.register.idCheck(dto);
                idcheck.enqueue(new Callback<ResponseBody>(){
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                        try{
                            if(response.body().string().equals("1")){
                                Log.v("Test", "실패");
                                Toast.makeText(RegisterActivity.this,"중복된 아이디입니다.", Toast.LENGTH_SHORT).show();
                                userId.requestFocus();
                                return;
                            } else {
                                Log.v("Test", "성공");
                                Toast.makeText(RegisterActivity.this,"사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception e){
                            Log.v("Test", "error");
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t){
                        Log.v("Test", "아이디 중복 체크 에러");
                    }
                });
            }
        });

        // 비밀번호 일치하는지 확인
        userPwdCheck.addTextChangedListener(new TextWatcher() {
            // 입력하기 전
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            // 텍스트 변화가 있을 때
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(userPwd.getText().toString().equals(userPwdCheck.getText().toString())){
                    pwd_check.setText("비밀번호가 일치합니다.");
                    pwd_check.setTextColor(Color.parseColor("#000000"));
                    // 가입하기 버튼 활성화
                    btn_register.setEnabled(true);
                } else {
                    pwd_check.setText("비밀번호가 일치하지 않습니다.");
                    pwd_check.setTextColor(Color.parseColor("#FF0000"));
                    // 가입하기 버튼 비활성화
                    btn_register.setEnabled(false);
                }
            }

            // 입력이 끝났을 때
            @Override
            public void afterTextChanged(Editable s) {
                if(userPwd.getText().toString().equals(userPwdCheck.getText().toString())){
                    pwd_check.setText("비밀번호가 일치합니다.");
                    // 가입하기 버튼 활성화
                    btn_register.setEnabled(true);
                } else {
                    pwd_check.setText("비밀번호가 일치하지 않습니다.");
                    pwd_check.setTextColor(Color.parseColor("#FF0000"));
                    // 가입하기 버튼 비활성화
                    btn_register.setEnabled(false);
                }
            }
        });

        // 회원 가입 버튼이 눌렸을 때
        btn_register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                RetrofitClient retrofitClient = new RetrofitClient();
                RegisterDTO dto = new RegisterDTO(userId.getText().toString(),userPwd.getText().toString(),userMail.getText().toString());

                // 비밀번호 정규식
                Pattern pattern = Pattern.compile("^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[`~!@$!%*#^?&\\\\(\\\\)\\\\-_=+])(?!.*[^a-zA-z0-9`~!@$!%*#^?&\\\\(\\\\)\\\\-_=+]).{8,16}$"); // 영문자/숫자/특수문자 포함 8~16자
                Matcher matcher = pattern.matcher(userPwd.getText().toString());

                //Gson 객체선언 후 객체를 gson으로 변환
                //Gson gson = new Gson();
                //String objJson = gson.toJson(dto);

                // 유효성 검사
                if(userId.getText().toString().length()==0){
                    Toast.makeText(RegisterActivity.this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
                    userId.requestFocus();
                    return;
                }
                if(userMail.getText().toString().length()==0){
                    Toast.makeText(RegisterActivity.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    userMail.requestFocus();
                    return;
                } else {
                    if(!Patterns.EMAIL_ADDRESS.matcher(userMail.getText().toString()).matches()){
                        Toast.makeText(RegisterActivity.this, "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                        userMail.requestFocus();
                        return;
                    }
                }

                if(userPwd.getText().toString().length()==0){
                    Toast.makeText(RegisterActivity.this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    userPwd.requestFocus();
                    return;
                } else {
                    if(!matcher.find()){
                        Toast.makeText(RegisterActivity.this, "비밀번호 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                        userPwd.setText("");
                        userPwd.requestFocus();
                        return;
                    }
                }

                // json으로 던져주기
                Call<ResponseBody> signup = retrofitClient.register.goPost(dto);
                Log.d("Test", dto.toString());

                signup.enqueue(new Callback<ResponseBody>(){
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                        try{
                            if(!response.isSuccessful()){
                                Log.v("result", "실패");
                            } else {
                                Log.v("Test", "회원가입성공");

                                // variable check
                                Log.d("Test", response.body().toString());
                                Log.d("Test", call.toString());
                                Log.d("Test", response.toString());

                                Toast.makeText(RegisterActivity.this,"회원가입완료", Toast.LENGTH_SHORT).show();
                                Intent intent = null;
                                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        }catch (Exception e){
                            Log.v("Test", "error");
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t){
                        Log.v("Test", "접속실패");

                    }
                });
            };
        });
    }
}
