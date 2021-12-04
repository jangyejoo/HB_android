package com.example.healthybuddy;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.healthybuddy.DTO.ChatDTO;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.chatData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class frag_chat extends Fragment {
    private View view;
    private ListView c_RecyclerView = null;
    private String pId, token, result;

    public static frag_chat newInstance() {
        return new frag_chat();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.activity_chat, container, false);
        getActivity().setTitle("채팅");

        pId = ((LoginActivity) LoginActivity.context).userID;
        token = "Bearer " + getPreferenceString(pId);

        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("pId" , id);

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
                    Toast.makeText(getActivity(),"다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
                    Intent intent = null;
                    intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }
        });

        Call<List<ChatDTO>> chat = retrofitClient.chat.listAll(token, map);
        chat.enqueue(new Callback<List<ChatDTO>>() {
            @Override
            public void onResponse(Call<List<ChatDTO>> call, Response<List<ChatDTO>> response) {
                try {
                    if (response.body().isEmpty()) {
                        Log.v("result", "실패");

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("알림")
                                .setMessage("채팅기록이 없습니다.")
                                .setPositiveButton("확인", null)
                                .create()
                                .show();
                    } else {
                        ArrayList<chatData> cData = new ArrayList<>();
                        List<ChatDTO> data = response.body();
                        for (ChatDTO post : data) {
                            chatData oItem = new chatData();
                            oItem.pImg = "https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg();
                            oItem.pNickname = post.getpNickname();
                            oItem.cId = post.getcId();
                            oItem.crId = post.getcrId();
                            Log.d("test", post.getcId()+" "+ post.getcrId() +" "+ post.getpImg()+" "+ post.getpNickname());
                            cData.add(oItem);
                        }
                        // ListView, Adapter 생성 및 연결 ------------------------
                        c_RecyclerView = (ListView) view.findViewById(R.id.chat_recyclerView);
                        ChatAdapter oAdapter = new ChatAdapter(cData);
                        c_RecyclerView.setAdapter(oAdapter);
                        Log.v("Test", "성공");

                    }
                } catch (Exception e) {
                    Log.v("Test", "error : "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<ChatDTO>> call, Throwable t) {

            }
        });
        return view;
    }
    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }
}