package com.example.healthybuddy;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.ProfileDTO;
import com.example.healthybuddy.DTO.RegisterDTO;

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
    private ImageView pImg;
    private EditText pNickname, pGym, pDetail;
    private Button btn_update, btn_find, btn_logout;
    private Spinner sp_pAge, sp_pHeight, sp_pWeight;
    private CheckBox mon, tue, wed, thr, fri, sat, sun, open;
    private RadioGroup rg_pSex;
    private RadioButton pMale, pFemale;
    private String pId, pAge, pHeight, pWeight, pRoutine, token;
    private String pSex, pOpen;

    private int IMG_REQUEST=21;
    private Bitmap bitmap;
    private File destFile=null;

    // Gym
    private ActivityResultLauncher<Intent> resultLauncher;
    private String Gym;
    public static frag_setting newInstance() {
        return new frag_setting();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_updateprofile, container, false);
        getActivity().setTitle("프로필 변경");

        // 값 가져오기
        pImg = (ImageView) view.findViewById(R.id.iv_img);
        pNickname = (EditText) view.findViewById(R.id.et_nickname);
        pGym = (EditText)view.findViewById(R.id.et_gym);
        btn_find = (Button)view.findViewById(R.id.btn_find);
        sp_pAge = (Spinner) view.findViewById(R.id.spinner_age);
        sp_pHeight = (Spinner) view.findViewById(R.id.spinner_height);
        sp_pWeight = (Spinner) view.findViewById(R.id.spinner_weight);
        pMale = (RadioButton)view.findViewById(R.id.rb_male);
        pFemale = (RadioButton)view.findViewById(R.id.rb_female);
        rg_pSex = (RadioGroup)view.findViewById(R.id.rg_sex);
        mon = (CheckBox) view.findViewById(R.id.cb_mon);
        tue = (CheckBox) view.findViewById(R.id.cb_tue);
        wed = (CheckBox) view.findViewById(R.id.cb_wed);
        thr = (CheckBox) view.findViewById(R.id.cb_thr);
        fri = (CheckBox) view.findViewById(R.id.cb_fri);
        sat = (CheckBox) view.findViewById(R.id.cb_sat);
        sun = (CheckBox) view.findViewById(R.id.cb_sun);
        open = (CheckBox) view.findViewById(R.id.cb_open);
        pDetail = (EditText) view.findViewById(R.id.et_msg);
        btn_update = (Button) view.findViewById(R.id.btn_update);
        btn_logout = (Button) view.findViewById(R.id.btn_logout);


        //pId = ((LoginActivity) LoginActivity.context).userID;
        pId = getPreferenceString("id");
        token = "Bearer " + getPreferenceString("token");
        pNickname.setText(pId);

        //Gym
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode()==RESULT_OK){
                            Gym = result.getData().getStringExtra("Gym");
                            Log.d("hap", "이야"+Gym+"asdf");
                            pGym.setText(Gym);
                        }
                    }
                }
        );

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

        // image
        pImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                startActivityForResult(intent, IMG_REQUEST);
            }
        });

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                intent = new Intent(getActivity(), GymActivity.class);
                resultLauncher.launch(intent);
            }
        });

        // age spinner
        String [] items_age = new String [50];
        items_age[0]="비공개";
        for(int i=1;i<items_age.length;i++){
            items_age[i] = 1969+i + "년";
        }

        ArrayAdapter<String> age = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, items_age);
        age.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        sp_pAge.setAdapter(age);

        sp_pAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pAge=items_age[i];
                Log.d("Test", pAge);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pAge="no comment";
                Log.d("Test", pAge);
            }
        });

        // height spinner
        String [] items_height = new String [60];
        items_height[0]="비공개";
        for(int i=1;i<items_height.length;i++){
            items_height[i] = 139+i + "cm";
        }

        ArrayAdapter<String> height = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, items_height);
        height.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        sp_pHeight.setAdapter(height);

        sp_pHeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pHeight=items_height[i];
                Log.d("Test", pHeight);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pHeight="no comment";
                Log.d("Test", pHeight);
            }
        });

        // weight spinner
        String [] items_weight = new String [90];
        items_weight[0]="비공개";
        for(int i=1;i<items_weight.length;i++){
            items_weight[i] = 29+i + "kg";
        }

        ArrayAdapter<String> weight = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_item, items_weight);
        weight.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        sp_pWeight.setAdapter(weight);

        sp_pWeight.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pWeight=items_weight[i];
                Log.d("Test", pWeight);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                pWeight="no comment";
                Log.d("Test", pWeight);
            }
        });

        // radio group
        pSex="-1";
        rg_pSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.rb_male:
                        pSex="0"; // 남자
                        break;
                    case R.id.rb_female:
                        pSex="1"; // 여자
                        break;
                    default:
                        break;
                }
            }
        });

        // routine
        String [] items_routine = {"0","0","0","0","0","0","0"};

        mon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[0]="1";
                } else {
                    items_routine[0]="0";
                }
            }
        });
        tue.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[1]="1";
                } else {
                    items_routine[1]="0";
                }
            }
        });
        wed.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[2]="1";
                } else {
                    items_routine[2]="0";
                }
            }
        });
        thr.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[3]="1";
                } else {
                    items_routine[3]="0";
                }
            }
        });
        fri.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[4]="1";
                } else {
                    items_routine[4]="0";
                }
            }
        });
        sat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[5]="1";
                } else {
                    items_routine[5]="0";
                }
            }
        });
        sun.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    items_routine[6]="1";
                } else {
                    items_routine[6]="0";
                }
            }
        });

        // open
        open.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                pOpen="0";
                if(((CheckBox)v).isChecked()){
                    pOpen="1";
                } else {
                    pOpen="0";
                }
            }
        });


        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
        map.put("pId", id);

        Call<ProfileDTO> profile = retrofitClient.profile.profile(token, map);
        profile.enqueue(new Callback<ProfileDTO>() {
            @Override
            public void onResponse(Call<ProfileDTO> call, Response<ProfileDTO> response) {
                try{
                    if(!response.isSuccessful()){
                        Log.d("test","뭔가 잘못됐다");
                    }else {
                        ProfileDTO post = response.body();
                        Glide.with(pImg.getContext()).load("https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/"+post.getpImg()).into(pImg);
                        pNickname.setText(post.getpNickname());
                        pGym.setText(post.getpGym());
                        pAge=post.getpAge();
                        pHeight=post.getpHeight();
                        pWeight=post.getpWeight();
                        pDetail.setText(post.getpDetail());
                        pSex= String.valueOf(post.getpSex());
                        pOpen=String.valueOf(post.getpOpen());

                        // 나이
                        int selectionPosition= age.getPosition(post.getpAge());
                        sp_pAge.setSelection(selectionPosition);

                        // 키
                        int selectionPosition2= height.getPosition(post.getpHeight());
                        sp_pHeight.setSelection(selectionPosition2);

                        // 몸무게
                        int selectionPosition3= weight.getPosition(post.getpWeight());
                        sp_pWeight.setSelection(selectionPosition3);

                        // 루틴
                        char[] arr = new char[7];
                        for (int i = 0; i <7; i++) {
                            items_routine[i] = String.valueOf(post.getpRoutine().charAt(i));
                        }

                        Log.d("Test", String.valueOf(items_routine));
                        if(items_routine[0].equals("1")){ mon.setChecked(true); }
                        if(items_routine[1].equals("1")){ tue.setChecked(true); }
                        if(items_routine[2].equals("1")){ wed.setChecked(true); }
                        if(items_routine[3].equals("1")){ thr.setChecked(true); }
                        if(items_routine[4].equals("1")){ fri.setChecked(true); }
                        if(items_routine[5].equals("1")){ sat.setChecked(true); }
                        if(items_routine[6].equals("1")){ sun.setChecked(true); }

                        // 성별 표시
                        if(post.getpSex()==0){
                            pMale.setChecked(true);
                        }
                        if(post.getpSex()==1){
                            pFemale.setChecked(true);
                        }

                        // open 표시
                        if(post.getpOpen()==0){
                            open.setChecked(false);
                        }
                        if(post.getpOpen()==1){
                            open.setChecked(true);
                        }
                    }
                }catch(Exception e){
                    Log.v("Test", "catch"+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ProfileDTO> call, Throwable t) {
                Log.v("test","failure"+t.toString());
            }
        });

        btn_update.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                pRoutine = String.join("",items_routine);

                Log.d("test",pGym.getText().toString());

                // MAP
                HashMap<String, RequestBody> map = new HashMap<>();
                RequestBody id = RequestBody.create(MediaType.parse("text/plain"), pId);
                RequestBody nickname = RequestBody.create(MediaType.parse("text/plain"), pNickname.getText().toString());
                RequestBody gym = RequestBody.create(MediaType.parse("text/plain"), pGym.getText().toString());
                RequestBody age = RequestBody.create(MediaType.parse("text/plain"), pAge);
                RequestBody height = RequestBody.create(MediaType.parse("text/plain"), pHeight);
                RequestBody weight = RequestBody.create(MediaType.parse("text/plain"), pWeight);
                RequestBody sex = RequestBody.create(MediaType.parse("text/plain"), pSex);
                RequestBody routine = RequestBody.create(MediaType.parse("text/plain"), pRoutine);
                RequestBody detail = RequestBody.create(MediaType.parse("text/plain"), pDetail.getText().toString());
                RequestBody open = RequestBody.create(MediaType.parse("text/plain"), pOpen);
                map.put("pId",id);
                map.put("pNickname",nickname);
                map.put("pGym",gym);
                map.put("pAge",age);
                map.put("pHeight",height);
                map.put("pWeight",weight);
                map.put("pSex",sex);
                map.put("pRoutine",routine);
                map.put("pDetail",detail);
                map.put("pOpen",open);

                //ProfileDTO dto = new ProfileDTO(pId,pNickname.getText().toString(),pGym.getText().toString(),pAge,pHeight,pWeight,pSex,pRoutine,pDetail.getText().toString(),"test",pOpen);
                Log.d("Test", pId + pNickname.getText().toString() + pGym.getText().toString() + pAge + pHeight + pWeight + pSex+pRoutine+pDetail.getText().toString()+"test"+pOpen);

                // 유효성 검사
                if(pNickname.getText().toString().length()==0){
                    Toast.makeText(getActivity(), "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                    pNickname.requestFocus();
                    return;
                }
                if(pGym.getText().toString().length()==0){
                    Toast.makeText(getActivity(), "헬스장을 선택하세요.", Toast.LENGTH_SHORT).show();
                    pGym.requestFocus();
                    return;
                }
                if(pSex=="-1"){
                    Toast.makeText(getActivity(), "성별을 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pDetail.getText().toString().length()==0){
                    Toast.makeText(getActivity(), "상태 메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
                    pDetail.requestFocus();
                    return;
                }

                // img 바꿀 때랑 안바꿀 때 분기 나누어야 할 듯
                MultipartBody.Part Bmp;
                Call<ResponseBody> update;
                if(destFile!=null){
                    Log.d("Test",destFile.getPath());
                    Log.d("Test",String.valueOf(destFile.length()/1024));

                    // pImg
                    RequestBody requestBmp = RequestBody.create(MediaType.parse("multipart/form-data"), destFile);
                    Bmp = MultipartBody.Part.createFormData("pImg", destFile.getName(), requestBmp);
                    update = retrofitClient.profile.update(token,Bmp,map);
                }else {
                    update = retrofitClient.profile.update2(token,map);
                }

                // json으로 던져주기
                update.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try{
                            if(!response.isSuccessful()){
                                Log.v("result", "실패");
                                Log.d("Test", response.toString());
                            } else {
                                Log.v("Test", "성공");

                                Toast.makeText(getActivity(),"프로필 변경 완료", Toast.LENGTH_SHORT).show();
                                Intent intent = null;
                                intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }
                        } catch(Exception e){
                            Log.v("Test", "error");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.v("Test", "접속실패");
                    }
                });
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RetrofitClient retrofitClient = new RetrofitClient();
                String jwt = getPreferenceString("token");
                Call<Void> logout = retrofitClient.login.logout(jwt, pId);
                logout.enqueue(new Callback<Void>(){
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response){
                        if(response.isSuccessful()){
                            Log.d("Test", "로그아웃 성공");
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

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //이미지 뷰를 클릭하면 시작되는 함수

        if(requestCode== IMG_REQUEST && resultCode==RESULT_OK && data!=null) {
            //response에 getData , return data 부분 추가해주어야 한다
            Uri selectedImage = data.getData();
            Uri photoUri = data.getData();
            Bitmap bitmap = null;
            //bitmap 이용
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),photoUri);
                //bitmap = rotateImage(bitmap, 90);
                //사진이 돌아가 있는 경우 rotateImage 함수 이용해서 사진 회전 가능
            } catch (IOException e) {
                e.printStackTrace();
            }

            // bitmap 압축
            compressBitmap(bitmap);

            //이미지뷰에 이미지 불러오기
            pImg.setImageBitmap(bitmap);

            //아래 커서 이용해서 사진의 경로 불러오기
            Cursor cursor = getActivity().getContentResolver().query(Uri.parse(selectedImage.toString()), null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            String mediaPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            Log.d("test ", mediaPath);

            destFile = new File(mediaPath);
            /*
            //uploadImage(mediaPath);
            SendImage();
            */


        }else{
            Toast.makeText(getActivity(), "사진 업로드 실패", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap compressBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,60, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        return compressedBitmap;
    }

    public String getPreferenceString(String key) {
        SharedPreferences pref = this.getActivity().getSharedPreferences("token.txt",MODE_PRIVATE);
        return pref.getString(key, "");
    }

}
