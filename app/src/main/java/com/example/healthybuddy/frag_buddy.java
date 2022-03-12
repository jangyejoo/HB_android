package com.example.healthybuddy;

import static android.content.Context.MODE_PRIVATE;

import static com.example.healthybuddy.DTO.ProfileDTO.isEmpty;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_buddy extends Fragment {
    private View view;
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

    public static frag_buddy newInstance() {
        return new frag_buddy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_buddy, container, false);
        getActivity().setTitle("Healthy Buddy");
        // 값 가져오기
        pImg = (ImageView) view.findViewById(R.id.iv_img);
        pNickname = (TextView) view.findViewById(R.id.tv_nickname);
        pGym = (TextView) view.findViewById(R.id.tv_gym);
        pAge = (TextView) view.findViewById(R.id.spinner_age);
        pHeight = (TextView) view.findViewById(R.id.spinner_height);
        pWeight = (TextView) view.findViewById(R.id.spinner_weight);
        pMale = (RadioButton) view.findViewById(R.id.rb_male);
        pFemale = (RadioButton) view.findViewById(R.id.rb_female);
        rg_pSex = (RadioGroup) view.findViewById(R.id.rg_sex);
        mon = (CheckBox) view.findViewById(R.id.cb_mon);
        tue = (CheckBox) view.findViewById(R.id.cb_tue);
        wed = (CheckBox) view.findViewById(R.id.cb_wed);
        thr = (CheckBox) view.findViewById(R.id.cb_thr);
        fri = (CheckBox) view.findViewById(R.id.cb_fri);
        sat = (CheckBox) view.findViewById(R.id.cb_sat);
        sun = (CheckBox) view.findViewById(R.id.cb_sun);
        pDetail = (TextView) view.findViewById(R.id.et_msg);
        btn_delete = (Button) view.findViewById(R.id.btn_delete);

        //pId = ((LoginActivity) LoginActivity.context).userID;
        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");

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
                    Intent intent = new Intent(getActivity(), AutoLoginActivity.class);
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
                    if(isEmpty(response.body())) {
                        Log.d("test","뭔가 잘못됐다");
                        pNickname.setText("Healthy Buddy 없음");
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("알림")
                                .setMessage("현재 Healthy Buddy가 없습니다.")
                                .setPositiveButton("확인", null)
                                .create()
                                .show();
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
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
                        if(arr[0]=='1'){ mon.setChecked(true); }
                        if(arr[1]=='1'){ tue.setChecked(true); }
                        if(arr[2]=='1'){ wed.setChecked(true); }
                        if(arr[3]=='1'){ thr.setChecked(true); }
                        if(arr[4]=='1'){ fri.setChecked(true); }
                        if(arr[5]=='1'){ sat.setChecked(true); }
                        if(arr[6]=='1'){ sun.setChecked(true); }

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
                pNickname.setText("Healthy Buddy 없음");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("알림")
                        .setMessage("현재 Healthy Buddy가 없습니다.\n화면을 이동합니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ((MainActivity)getActivity()).replaceFragment(frag_user.newInstance());
                            }
                        })
                        .create()
                        .show();
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("알림")
                                        .setMessage("친구를 삭제합니다.\n프로필을 공개로 변경합니다.")
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                ((MainActivity)getActivity()).replaceFragment(frag_user.newInstance());
                                            }
                                        })
                                        .create()
                                        .show();
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
        return view;
    }
    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }
}