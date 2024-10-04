package com.example.banktest.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.banktest.R;

import java.util.List;

public class MessageAdapter extends  RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,null);

        return new MyViewHolder(chatView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Message message = messageList.get(position);
        if(message.getSentBy().equals(Message.SENT_BY_ME)){
            holder.leftChatView.setVisibility((View.GONE));
            holder.rightChatView.setVisibility((View.VISIBLE));
            holder.rightTextView.setText(message.getMessage());
            holder.robotIcon.setVisibility(View.GONE);
        }else{
            holder.rightChatView.setVisibility((View.GONE));
            holder.leftChatView.setVisibility((View.VISIBLE));
            holder.leftTextView.setText(message.getMessage());
            holder.robotIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftChatView,rightChatView;
        TextView leftTextView,rightTextView;
        ImageView robotIcon;
        public MyViewHolder(@NonNull View itemView){
            super(itemView);
            leftChatView = itemView.findViewById(R.id.left_chat_view);
            rightChatView = itemView.findViewById(R.id.right_chat_view);
            leftTextView = itemView.findViewById(R.id.left_chat_text_view);
            rightTextView = itemView.findViewById(R.id.right_chat_text_view);
            robotIcon = itemView.findViewById(R.id.robot_image_view);
        }

    }

}
