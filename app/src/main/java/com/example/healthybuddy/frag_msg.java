package com.example.healthybuddy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class frag_msg extends Fragment {
    private RecyclerView c_RecyclerView = null;
    private String pId, token, cId, crId, cNick;
    private Button btn, btn_friend, btn_accept;

    public frag_msg() {
        // Required empty public constructor
    }

    public static frag_msg newInstance(String param1, String param2) {
        frag_msg fragment = new frag_msg();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_message, container, false);

        return view;
    }
}