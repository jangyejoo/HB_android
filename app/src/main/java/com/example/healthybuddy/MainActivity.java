package com.example.healthybuddy;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private frag_user frag_user;
    private frag_chat frag_chat;
    private frag_buddy frag_buddy;
    private frag_setting frag_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frag_user = new frag_user();
        frag_chat = new frag_chat();
        frag_buddy = new frag_buddy();
        frag_setting = new frag_setting();
        setFrag(0); // 첫 프래그먼트 화면 지정

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_member:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, frag_user)
                                .commit();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, frag_chat)
                                .commit();
                        return true;
                    case R.id.action_buddy:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, frag_buddy)
                                .commit();
                        return true;
                    case R.id.action_setting:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, frag_setting)
                                .commit();
                        return true;
                }
                return false;
            }
        });
    }

    //프래그먼트 교체가 일어나는 실행문
    private void setFrag(int n){
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        switch (n) {
            case 0:
                ft.replace(R.id.main_frame, frag_user);
                ft.commit();
                break;
            case 1:
                ft.replace(R.id.main_frame, frag_chat);
                ft.commit();
                break;
            case 2:
                ft.replace(R.id.main_frame, frag_buddy);
                ft.commit();
                break;
            case 3:
                ft.replace(R.id.main_frame, frag_setting);
                ft.commit();
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment).commit();      // Fragment로 사용할 MainActivity내의 layout공간을 선택합니다.
    }

}
