package com.example.healthybuddy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.itemData;

import java.util.ArrayList;

public class MateAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    private ArrayList<itemData> data = null;
    private int nListCnt=0;

    public MateAdapter(ArrayList<itemData> mData){
        data = mData;
        nListCnt = data.size();
    }

    @Override
    public int getCount() {
        return nListCnt;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.mate_item, parent, false);
        }

        ImageView img = (ImageView) convertView.findViewById(R.id.iv_img);
        TextView nickname = (TextView) convertView.findViewById(R.id.tv_nick);
        TextView info = (TextView) convertView.findViewById(R.id.tv_info);
        TextView detail = (TextView) convertView.findViewById(R.id.tv_detail);
        Button btn = (Button)convertView.findViewById(R.id.btn_delete);

        Glide.with(img.getContext()).load(data.get(position).img).into(img);
        nickname.setText(data.get(position).Nickname);
        info.setText(data.get(position).Info);
        detail.setText(data.get(position).Detail);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChatActivity.class);
                intent.putExtra("id2",data.get(position).id2);
                view.getContext().startActivity(intent);
            }
        });


        return convertView;
    }
}