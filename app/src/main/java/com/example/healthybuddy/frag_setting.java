package com.example.healthybuddy;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthybuddy.DTO.ChatModel;
import com.example.healthybuddy.DTO.IdDTO;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.PwdDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_setting extends Fragment {
    View view;
    private TextView updateProfile, updatePassword, logout, withdrawal;
    private String pId, token, enter;
    private Switch updateEnter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_setting, container, false);

        // 값 가져오기
        updateProfile = (TextView) view.findViewById(R.id.setting_textView_updateProfile);
        updateEnter = (Switch) view.findViewById(R.id.setting_switch_Enter);
        updatePassword = (TextView)view.findViewById(R.id.setting_textView_updatePassword);
        logout = (TextView) view.findViewById(R.id.setting_textView_logout);
        withdrawal = (TextView) view.findViewById(R.id.setting_textView_withdrawal);

        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        enter = getPreferenceStringEnter(pId);

        Log.d("test","enter : "+enter);
        if(enter.equals("yes")){
            updateEnter.setChecked(true);
        } else if (enter.equals("no")){
            updateEnter.setChecked(false);
        }

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
                    Intent intent = new Intent(getActivity(), AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
                startActivity(intent);
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });

        updateEnter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    Log.d("test", "enter_yes");
                    setPreferenceEnter(pId,"yes");
                } else {
                    Log.d("test", "enter_no");
                    setPreferenceEnter(pId,"no");
                }
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("로그아웃을 합니다")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                RetrofitClient retrofitClient = new RetrofitClient();
                                String jwt = getPreferenceString("token");
                                Call<Void> logout = retrofitClient.login.logout(jwt, pId);
                                logout.enqueue(new Callback<Void>(){
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response){
                                        if(response.isSuccessful()){
                                            Log.d("Test", "로그아웃 성공");

                                            //다른 통신을 하기 위해 token 삭제
                                            setPreference("id",null);
                                            setPreference("token", null);

                                            getActivity().finish();

                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                            startActivity(intent);

                                        } else {
                                            Log.d("test", "로그아웃 실패");
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t){
                                        t.printStackTrace();
                                    }
                                });
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create()
                        .show();
            }
        });

        withdrawal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("정말 탈퇴하시겠습니까?\n탈퇴를 하면 모든 채팅 기록은 삭제됩니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                // firebase 관련 채팅방 다 삭제
                                FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/"+pId).equalTo(true).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot item: snapshot.getChildren()){
                                            Log.d("test","key : "+item.getKey());
                                            FirebaseDatabase.getInstance().getReference().child("chatrooms").child(item.getKey()).removeValue();
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                RetrofitClient retrofitClient = new RetrofitClient();
                                String jwt = getPreferenceString("token");

                                IdDTO dto = new IdDTO(pId);
                                Call<ResponseBody> delete = retrofitClient.register.withdraw(token, dto);
                                delete.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        try {
                                            if(!response.isSuccessful()){
                                                Log.v("result", "실패");
                                                Log.d("test","결과:"+response.body().string());
                                            } else {
                                                if (response.body().string().equals("1")) {
                                                    Log.d("Test", "mysql 삭제 성공");

                                                    Call<Void> logout = retrofitClient.login.logout(jwt, pId);
                                                    logout.enqueue(new Callback<Void>() {
                                                        @Override
                                                        public void onResponse(Call<Void> call, Response<Void> response) {
                                                            if (response.isSuccessful()) {
                                                                Log.d("Test", "탈퇴 성공");
                                                                getActivity().finish();
                                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                                startActivity(intent);

                                                            } else {
                                                                Log.d("test", "탈퇴 실패");
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Void> call, Throwable t) {
                                                            t.printStackTrace();
                                                        }

                                                    });
                                                } else {
                                                    Log.d("test", "mysql 삭제 실패");
                                                }
                                            }
                                        } catch (IOException ioException) {
                                            ioException.printStackTrace();
                                            Log.d("test",ioException.getMessage());
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                                    }
                                });
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create()
                        .show();
            }
        });

        return view;
    }

    //화면 터치 시 키보드 내려감
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getActivity().getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.getActivity().dispatchTouchEvent(ev);
    }

    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

    public String getPreferenceStringEnter(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("enter.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    //데이터를 내부 저장소에 저장하기
    public void setPreference(String key, String value) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void setPreferenceEnter(String key, String value) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("enter.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }


}
