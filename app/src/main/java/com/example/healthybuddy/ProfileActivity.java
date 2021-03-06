package com.example.healthybuddy;

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
import android.view.MotionEvent;
import android.view.View;
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
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.healthybuddy.DTO.RegisterDTO;
import com.example.healthybuddy.DTO.UserModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity  extends AppCompatActivity {
    private ImageView pImg;
    private EditText pNickname, pGym, pDetail;
    private Button btn_update, btn_find, btn_finish;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("????????? ??????");

        // ??? ????????????
        pImg = (ImageView) findViewById(R.id.iv_img);
        pNickname = (EditText) findViewById(R.id.et_nickname);
        pGym = (EditText)findViewById(R.id.et_gym);
        //btn_update = (Button)findViewById(R.id.btn_image);
        btn_find = (Button)findViewById(R.id.btn_find);
        sp_pAge = (Spinner) findViewById(R.id.spinner_age);
        sp_pHeight = (Spinner) findViewById(R.id.spinner_height);
        sp_pWeight = (Spinner) findViewById(R.id.spinner_weight);
        pMale = (RadioButton)findViewById(R.id.rb_male);
        pFemale = (RadioButton)findViewById(R.id.rb_female);
        rg_pSex = (RadioGroup)findViewById(R.id.rg_sex);
        mon = (CheckBox) findViewById(R.id.cb_mon);
        tue = (CheckBox) findViewById(R.id.cb_tue);
        wed = (CheckBox) findViewById(R.id.cb_wed);
        thr = (CheckBox) findViewById(R.id.cb_thr);
        fri = (CheckBox) findViewById(R.id.cb_fri);
        sat = (CheckBox) findViewById(R.id.cb_sat);
        sun = (CheckBox) findViewById(R.id.cb_sun);
        open = (CheckBox) findViewById(R.id.cb_open);
        pDetail = (EditText) findViewById(R.id.et_msg);
        btn_finish = (Button) findViewById(R.id.btn_finish);


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
                            Log.d("hap", "??????"+Gym+"asdf");
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
                    Log.d("Test", "????????????");
                    Intent intent = new Intent(ProfileActivity.this, AutoLoginActivity.class);
                    startActivity(intent);
                }

            }
            @Override
            public void onFailure(Call<List<RegisterDTO>> call, Throwable t){
                t.printStackTrace();
            }

        });

        // image

        Glide.with(pImg.getContext())
                .load("https://elasticbeanstalk-ap-northeast-2-355785572273.s3.ap-northeast-2.amazonaws.com/test/premium-icon-add-photo-4146794.png")
                .apply(new RequestOptions().circleCrop())
                .into(pImg);

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
                intent = new Intent(ProfileActivity.this, GymActivity.class);
                resultLauncher.launch(intent);
            }
        });

        // age spinner
        String [] items_age = new String [50];
        items_age[0]="?????????";
        for(int i=1;i<items_age.length;i++){
            items_age[i] = 1969+i + "???";
        }

        ArrayAdapter<String> age = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items_age);
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
        items_height[0]="?????????";
        for(int i=1;i<items_height.length;i++){
            items_height[i] = 139+i + "cm";
        }

        ArrayAdapter<String> height = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items_height);
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
        items_weight[0]="?????????";
        for(int i=1;i<items_weight.length;i++){
            items_weight[i] = 29+i + "kg";
        }

        ArrayAdapter<String> weight = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, items_weight);
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
                        pSex="0"; // ??????
                        break;
                    case R.id.rb_female:
                        pSex="1"; // ??????
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
        pOpen="0";
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

        btn_finish.setOnClickListener(new View.OnClickListener() {
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

                // ????????? ??????
                if(pNickname.getText().toString().length()==0){
                    Toast.makeText(ProfileActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    pNickname.requestFocus();
                    return;
                }
                if(pGym.getText().toString().length()==0){
                    Toast.makeText(ProfileActivity.this, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    pGym.requestFocus();
                    return;
                }
                if(pSex=="-1"){
                    Toast.makeText(ProfileActivity.this, "????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(pDetail.getText().toString().length()==0){
                    Toast.makeText(ProfileActivity.this, "?????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    pDetail.requestFocus();
                    return;
                }

                if(destFile==null){
                    //Toast.makeText(ProfileActivity.this, "????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                    //return;
                    Call<ResponseBody> create = retrofitClient.profile.create(token, null, map);
                    create.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try{
                                if(!response.isSuccessful()){
                                    Log.v("result", "??????");
                                    Log.d("Test", response.toString());
                                } else {
                                    Log.v("Test", "??????");

                                    // gym, nick ??????
                                    setPreference("gym",pGym.getText().toString());
                                    setPreference("nick",pNickname.getText().toString());

                                    Toast.makeText(ProfileActivity.this,"????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                    Intent intent = null;
                                    intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            } catch(Exception e){
                                Log.v("Test", "error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.v("Test", "????????????");
                        }
                    });

                } else {

                    Log.d("Test", destFile.getPath());
                    Log.d("Test", String.valueOf(destFile.length() / 1024));

                    // pImg
                    RequestBody requestBmp = RequestBody.create(MediaType.parse("multipart/form-data"), destFile);
                    MultipartBody.Part Bmp = MultipartBody.Part.createFormData("pImg", destFile.getName(), requestBmp);

                    Log.d("Test", pId + pNickname.getText().toString() + pGym.getText().toString() + pAge + pHeight + pWeight + pSex + pRoutine + pDetail.getText().toString() + "test" + pOpen);

                    // json?????? ????????????
                    Call<ResponseBody> create = retrofitClient.profile.create(token, Bmp, map);
                    create.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try{
                                if(!response.isSuccessful()){
                                    Log.v("result", "??????");
                                    Log.d("Test", response.toString());
                                } else {
                                    Log.v("Test", "??????");

                                    // gym, nick ??????
                                    setPreference("gym",pGym.getText().toString());
                                    setPreference("nick",pNickname.getText().toString());

                                    Toast.makeText(ProfileActivity.this,"????????? ?????? ??????", Toast.LENGTH_SHORT).show();
                                    Intent intent = null;
                                    intent = new Intent(ProfileActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            } catch(Exception e){
                                Log.v("Test", "error");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.v("Test", "????????????");
                        }
                    });

                }


            }
        });

    }

    //?????? ???????????? ????????? ????????? ????????????
    public String getPreferenceString(String key) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        return pref.getString(key, "");
    }

    //???????????? ?????? ???????????? ????????????
    public void setPreference(String key, String value) {
        SharedPreferences pref = getSharedPreferences("token.txt", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    //?????? ?????? ??? ????????? ?????????
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

    private String getRealPathFromURI(Uri contentUri) {
        if (contentUri.getPath().startsWith("/storage")) {
            Log.d("test", contentUri.getPath());
            return contentUri.getPath();
        }
        String id = DocumentsContract.getDocumentId(contentUri).split(":")[1];
        String[] columns = { MediaStore.Files.FileColumns.DATA };
        String selection = MediaStore.Files.FileColumns._ID + " = " + id;
        Cursor cursor = getContentResolver().query(MediaStore.Files.getContentUri("external"), columns, selection, null, null);
        try {
            int columnIndex = cursor.getColumnIndex(columns[0]);
            if (cursor.moveToFirst()) {
                Log.d("test", cursor.getString(columnIndex));
                return cursor.getString(columnIndex);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private void SendImage(){
        Log.d("test", "2");
        // RequestBody??? ?????? ??? MultipartBody.Part??? ?????? ?????????
        RequestBody requestBmp = RequestBody.create(MediaType.parse("multipart/form-data"), destFile);
        MultipartBody.Part Bmp = MultipartBody.Part.createFormData("pImg", destFile.getName(), requestBmp);

        pId = ((LoginActivity) LoginActivity.context).userID;
        token = "Bearer " + getPreferenceString(pId);
        Log.d("test", token);
        Log.d("test", destFile.getName());

        // Api ??????
        RetrofitClient retrofitClient = new RetrofitClient();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //????????? ?????? ???????????? ???????????? ??????

        if(requestCode== IMG_REQUEST && resultCode==RESULT_OK && data!=null) {
            //response??? getData , return data ?????? ?????????????????? ??????
            Uri selectedImage = data.getData();
            Uri photoUri = data.getData();
            Bitmap bitmap = null;
            //bitmap ??????
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),photoUri);
                //bitmap = rotateImage(bitmap, 90);
                //????????? ????????? ?????? ?????? rotateImage ?????? ???????????? ?????? ?????? ??????
            } catch (IOException e) {
                e.printStackTrace();
            }

            // bitmap ??????
            compressBitmap(bitmap);

            //??????????????? ????????? ????????????
            pImg.setImageBitmap(bitmap);

            //?????? ?????? ???????????? ????????? ?????? ????????????
            Cursor cursor = getContentResolver().query(Uri.parse(selectedImage.toString()), null, null, null, null);
            assert cursor != null;
            cursor.moveToFirst();
            String mediaPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            Log.d("test ", mediaPath);

            destFile = new File(mediaPath);

        }else{
            //Toast.makeText(this, "?????? ????????? ??????", Toast.LENGTH_LONG).show();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap compressBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,60, stream);
        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        return compressedBitmap;
    }


}
