package com.example.healthybuddy;


import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FindActivity extends Activity {
    private EditText mEmail, mEmail2, mId;
    private Button btn_find_id, btn_find_pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        mEmail = (EditText) findViewById(R.id.et_email);
        mEmail2 = (EditText) findViewById(R.id.et_email2);
        mId = (EditText) findViewById(R.id.et_id);
        btn_find_id = (Button) findViewById(R.id.btn_find_id);
        btn_find_pwd = (Button) findViewById(R.id.btn_find_pwd);

        btn_find_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail.getText().toString();
                hideKeyboard();
                Log.v("result", email);

                if (email.trim().length()==0 || email==null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                    builder.setTitle("알림")
                            .setMessage("이메일 정보를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create()
                            .show();
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    // 아이디 찾기
                    HashMap<String, RequestBody> map = new HashMap<>();
                    RequestBody Email = RequestBody.create(MediaType.parse("text/plain"), email);
                    map.put("mEmail",Email);

                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<ResponseBody> find = retrofitClient.login.findId(map);
                    Log.v("result", "map"+map.toString());
                    find.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try{
                                if(!response.isSuccessful()){
                                    Log.v("result", "실패");
                                    Log.d("Test", response.toString());
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                                    builder.setTitle("알림")
                                            .setMessage("등록되지 않은 이메일입니다.")
                                            .setPositiveButton("확인", null)
                                            .create()
                                            .show();
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    Log.v("Test", "성공");

                                    String id = response.body().string();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                                    builder.setTitle("알림")
                                            .setMessage("회원님의 아이디는 "+id+"입니다.")
                                            .setPositiveButton("확인", null)
                                            .create()
                                            .show();
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            } catch(Exception e){
                                Log.v("Test", "error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.v("Test", t.toString());
                        }
                    });
                }
            }
        });

        btn_find_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmail2.getText().toString();
                String id = mId.getText().toString();
                hideKeyboard();
                Log.v("result", email);

                if (email.trim().length()==0 || email==null || id.trim().length()==0 || id==null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                    builder.setTitle("알림")
                            .setMessage("아이디와 이메일 정보를 입력해주세요.")
                            .setPositiveButton("확인", null)
                            .create()
                            .show();
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                } else {
                    // 아이디 찾기
                    HashMap<String, RequestBody> map = new HashMap<>();
                    RequestBody Email = RequestBody.create(MediaType.parse("text/plain"), email);
                    RequestBody Id = RequestBody.create(MediaType.parse("text/plain"), id);
                    map.put("mEmail",Email);
                    map.put("mId",Id);

                    RetrofitClient retrofitClient = new RetrofitClient();
                    Call<ResponseBody> find = retrofitClient.login.findPwd(map);
                    Log.v("result", "map"+map.toString());
                    find.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try{
                                if(!response.isSuccessful()){
                                    Log.v("result", "실패");
                                    Log.d("Test", response.toString());
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                                    builder.setTitle("알림")
                                            .setMessage("아이디 또는 이메일 정보가 잘 못되었습니다.")
                                            .setPositiveButton("확인", null)
                                            .create()
                                            .show();
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                } else {
                                    Log.v("Test", "성공");

                                    String id = response.body().string();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                                    builder.setTitle("알림")
                                            .setMessage("회원님의 비밀번호를 메일로 전송했습니다. "+email)
                                            .setPositiveButton("확인", null)
                                            .create()
                                            .show();
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            } catch(Exception e){
                                Log.v("Test", "error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.v("Test", t.toString());
                        }
                    });
                }
            }
        });
    }

    //키보드 숨기기
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEmail.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(mId.getWindowToken(), 0);
    }

    //화면 터치 시 키보드 내려감
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
