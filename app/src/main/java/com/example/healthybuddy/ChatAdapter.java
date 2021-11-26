package com.example.healthybuddy;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.chatData;

import java.util.ArrayList;

public class ChatAdapter extends BaseAdapter {
    LayoutInflater inflater = null;
    private ArrayList<chatData> data = null;
    private int nListCnt = 0;

    public ChatAdapter(ArrayList<chatData> cData){
        data = cData;
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
            convertView = inflater.inflate(R.layout.chat_item, parent, false);
        }

        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layout);
        ImageView img = (ImageView) convertView.findViewById(R.id.item_chat_imageView);
        TextView nickname = (TextView) convertView.findViewById(R.id.item_chat_tv_title);
        TextView comment = (TextView) convertView.findViewById(R.id.item_chat_tv_comment);

        Glide.with(img.getContext()).load(data.get(position).pImg).into(img);
        nickname.setText(data.get(position).pNickname);
        layout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MessageActivity.class);
                //intent.putExtra("cIdx",data.get(position).cIdx);
                intent.putExtra("cId",data.get(position).cId);
                intent.putExtra("crId",data.get(position).crId);
                view.getContext().startActivity(intent);
            }
        });
        return convertView;
    }
}
