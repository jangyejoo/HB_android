package com.example.healthybuddy;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthybuddy.DTO.messageData;

import org.w3c.dom.Text;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private ArrayList<messageData> data = null;
    private int nListCnt = 0;

    public MessageAdapter(ArrayList<messageData> cData){
        data = cData;
        nListCnt = data.size();
    }

    /*
    @Override
    public int getCount() {
        return nListCnt;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }


     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(viewType == Code.ViewType.RIGHT_CONTENT){
            view=inflater.inflate(R.layout.message_item_right, parent, false);
            return new RightViewHolder(view);
        } else {
            view=inflater.inflate(R.layout.message_item_left, parent, false);
            return new LeftViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof  LeftViewHolder){
            Glide.with(((LeftViewHolder)holder).img.getContext()).load(data.get(position).pImg).into(((LeftViewHolder)holder).img);
            //((LeftViewHolder)holder).img.setText(data.get(position).getpImg());
            ((LeftViewHolder)holder).tv_nick.setText(data.get(position).pNickname);
            ((LeftViewHolder)holder).tv_content.setText(data.get(position).mgDetail);
            ((LeftViewHolder)holder).tv_time.setText(data.get(position).mgDate);
        } else {
            ((RightViewHolder)holder).tv_content.setText(data.get(position).mgDetail);
            ((RightViewHolder)holder).tv_time.setText(data.get(position).mgDate);
        }
    }

    @Override
    public int getItemCount() {
        return nListCnt;
    }

    @Override
    public  int getItemViewType(int position) { return data.get(position).getViewType();}

    public class LeftViewHolder extends RecyclerView.ViewHolder{
        CircleImageView img;
        TextView tv_nick;
        TextView tv_content;
        TextView tv_time;

        public LeftViewHolder(@NonNull View itemView){
            super(itemView);
            img = (CircleImageView) itemView.findViewById(R.id.item_messagebox_ImageView_profile);
            tv_nick = (TextView) itemView.findViewById(R.id.item_messagebox_TextView_name);
            tv_content = (TextView) itemView.findViewById(R.id.item_messagebox_textview_msg);
            tv_time = (TextView) itemView.findViewById(R.id.item_messagebox_textview_timestamp);
        }
    }

    public class RightViewHolder extends RecyclerView.ViewHolder{
        TextView tv_content;
        TextView tv_time;

        public RightViewHolder(@NonNull View itemView){
            super(itemView);
            tv_content = (TextView) itemView.findViewById(R.id.item_messagebox_textview_msg);
            tv_time = (TextView) itemView.findViewById(R.id.item_messagebox_textview_timestamp);
        }
    }

}
