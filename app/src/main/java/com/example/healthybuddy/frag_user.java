package com.example.healthybuddy;

import static android.content.Context.MODE_PRIVATE;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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

public class frag_user extends Fragment {
    private View view;
    private ListView m_ListView =null;
    private String pId, token;
    private String sex, height, weight, age, routine;

    public frag_user(){

    }
    public static frag_user newInstance() {
        return new frag_user();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.activity_member, container, false);
        getActivity().setTitle("친구");

        //pId = ((LoginActivity) LoginActivity.context).userID;
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
                    Intent intent = new Intent(getActivity(), AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        // profile 인증
        IdDTO dto = new IdDTO(pId);

        Call<ResponseBody> check = retrofitClient.profile.profileCheck(token,dto);
        check.enqueue(new Callback<ResponseBody>(){
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response){
                try{
                    if(response.body().string().equals("1")){
                        Log.v("Test", "diiiiiiiiiiiiiiiiii");
                    } else {
                        Log.v("Test", "ghhhhhhhh");
                        Toast.makeText(getActivity(),"프로필 설정 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
                        Intent intent = null;
                        intent = new Intent(getActivity(), ProfileActivity.class);
                        startActivity(intent);
                    }
                }catch (Exception e){
                    Log.v("Test", "error");
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t){
                Log.v("Test", t.toString());
            }
        });

        // memberlist
        Call<List<ProfileDTO>> members = retrofitClient.profile.members(token, map);
        members.enqueue(new Callback<List<ProfileDTO>>() {
            @Override
            public void onResponse(Call<List<ProfileDTO>> call, Response<List<ProfileDTO>> response) {
                try {
                    if (response.body().isEmpty()) {
                        Log.v("result", "실패");

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("알림")
                                .setMessage("같은 헬스장에 앱 이용자가 없습니다.")
                                .setPositiveButton("확인", null)
                                .create()
                                .show();
                        //AlertDialog alertDialog = builder.create();
                        //alertDialog.show();
                    } else {
                        ArrayList<itemData> oData = new ArrayList<>();
                        List<ProfileDTO> data = response.body();
                        Log.d("test","비어있니"+data.isEmpty());


                        for (ProfileDTO post : data) {
                            TextView gym = (TextView) view.findViewById(R.id.tv_gym);
                            gym.setText(post.getpGym());

                            // gym 저장
                            setPreference("gym",post.getpGym());

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
                            if(arr[0]=='1'){ routine+="월/"; }
                            if(arr[1]=='1'){ routine+="화/"; }
                            if(arr[2]=='1'){ routine+="수/"; }
                            if(arr[3]=='1'){ routine+="목/"; }
                            if(arr[4]=='1'){ routine+="금/"; }
                            if(arr[5]=='1'){ routine+="토/"; }
                            if(arr[6]=='1'){ routine+="일/"; }

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

                            oItem.Info = sex+height+weight+age+routine;
                            if(post.getpOpen()==1){oData.add(oItem);}
                        }

                        // ListView, Adapter 생성 및 연결 ------------------------
                        m_ListView = (ListView) view.findViewById(R.id.listView);
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
        return view;
    }
    //내부 저장소에 저장된 데이터 가져오기
    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

    //데이터를 내부 저장소에 저장하기
    public void setPreference(String key, String value) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
