package com.example.healthybuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.healthybuddy.DTO.ChatModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarActivity extends Activity {

    private EditText cal_title;
    private Spinner start_day, start_ampm, start_hour, start_min, end_day, end_ampm, end_hour, end_min;
    private Button btn;
    private String st_start_day, st_start_ampm, st_start_hour, st_start_min, st_end_day, st_end_ampm, st_end_hour, st_end_min, chatroomid;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년");
    private SimpleDateFormat today = new SimpleDateFormat("M월 d일");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        setContentView(R.layout.activity_calendar);

        cal_title = (EditText) findViewById(R.id.calendaractivity_edittext_title);
        start_day = (Spinner) findViewById(R.id.calendaractivity_spinner_start_day);
        start_ampm = (Spinner) findViewById(R.id.calendaractivity_spinner_start_ampm);
        start_hour = (Spinner) findViewById(R.id.calendaractivity_spinner_start_hour);
        start_min = (Spinner) findViewById(R.id.calendaractivity_spinner_start_min);
        end_day = (Spinner) findViewById(R.id.calendaractivity_spinner_end_day);
        end_ampm = (Spinner) findViewById(R.id.calendaractivity_spinner_end_ampm);
        end_hour = (Spinner) findViewById(R.id.calendaractivity_spinner_end_hour);
        end_min = (Spinner) findViewById(R.id.calendaractivity_spinner_end_min);
        btn = (Button) findViewById(R.id.calendaractivity_button_send);
        chatroomid = getIntent().getStringExtra("chatRoomId");

        // day
        ArrayAdapter<CharSequence> day = ArrayAdapter.createFromResource(this, R.array.day, android.R.layout.simple_spinner_item);
        day.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_day.setAdapter(day);
        end_day.setAdapter(day);

        String st_today = today.format(new Date());
        int selectionPosition = day.getPosition(st_today);
        start_day.setSelection(selectionPosition);
        end_day.setSelection(selectionPosition);

        start_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_start_day = String.valueOf(adapterView.getSelectedItem());
                int position = day.getPosition(st_start_day);
                end_day.setSelection(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        end_day.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_end_day = String.valueOf(adapterView.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // ampm
        String [] items_ampm = new String[2];
        items_ampm[0] = "오전";
        items_ampm[1] = "오후";
        ArrayAdapter<String> ampm = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items_ampm);
        ampm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_ampm.setAdapter(ampm);
        end_ampm.setAdapter(ampm);

        start_ampm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_start_ampm = items_ampm[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        end_ampm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_end_ampm = items_ampm[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // hour
        String [] items_hour = new String[12];
        for(int i=0;i<items_hour.length;i++){
            items_hour[i]=String.valueOf(i+1);
        }
        ArrayAdapter<String> hour = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items_hour);
        hour.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_hour.setAdapter(hour);
        end_hour.setAdapter(hour);

        start_hour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_start_hour = items_hour[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        end_hour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_end_hour = items_hour[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // min
        String [] items_min = new String[60];
        items_min[0] = "00";
        items_min[1] = "01";
        items_min[2] = "02";
        items_min[3] = "03";
        items_min[4] = "04";
        items_min[5] = "05";
        items_min[6] = "06";
        items_min[7] = "07";
        items_min[8] = "08";
        items_min[9] = "09";
        for(int i=10;i<items_min.length;i++){
            items_min[i]=String.valueOf(i+1);
        }
        ArrayAdapter<String> min = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, items_min);
        min.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        start_min.setAdapter(min);
        end_min.setAdapter(min);

        start_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_start_min = items_min[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        end_min.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                st_end_min = items_min[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // firebase에 저장해서 채팅방 상단 고정, 알림 설정하면 fcm으로 알림 보내기 (30분 전에?)
                // 나는 안드로이드 자체 알람, 너는 fcm?
                String year = simpleDateFormat.format(new Date());
                String start = year+" "+st_start_day+" "+st_start_ampm+" "+st_start_hour+":"+st_start_min;
                String end = year+" "+st_end_day+" "+st_end_ampm+" "+st_end_hour+":"+st_end_min;
                String title = cal_title.getText().toString();
                Log.d("test","start : "+start);
                Log.d("test","end : "+end);
                Log.d("test","title : "+title);

                if (cal_title.getText().toString().length()==0){
                    Toast.makeText(CalendarActivity.this, "일정 제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ChatModel chatModel = new ChatModel();
                chatModel.calendar.put("title",title);
                chatModel.calendar.put("start",start);
                chatModel.calendar.put("end",end);

                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatroomid).child("calendar").updateChildren(chatModel.calendar).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("test","성공");
                    }
                });

                finish();
                ((MessageActivity_firebase)MessageActivity_firebase.context).onRestart();

            }
        });
    }
}