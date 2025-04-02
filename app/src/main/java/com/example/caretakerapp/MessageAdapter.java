//package com.example.caretakerapp;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.PorterDuff;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.RecyclerView;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    private static final int TYPE_SENT = 0;
//    private static final int TYPE_RECEIVED = 1;
//
//    private Context context;
//    private List<Message> messageList;
//    private String currentUserEmail;
//
//    public MessageAdapter(Context context, List<Message> messageList) {
//        this.context = context;
//        this.messageList = messageList;
//        this.currentUserEmail = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//                .getString("user_email", "");
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view;
//        if (viewType == TYPE_SENT) {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_sent, parent, false);
//            return new SentMessageHolder(view);
//        } else {
//            view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_message_received, parent, false);
//            return new ReceivedMessageHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Message message = messageList.get(position);
//        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
//
//        if (getItemViewType(position) == TYPE_SENT) {
//            SentMessageHolder sentHolder = (SentMessageHolder) holder;
//
//            sentHolder.messageText.setText(message.getText());
//            sentHolder.messageTime.setText(sdf.format(new Date(message.getTimestamp())));
//
//            // Set message status indicator
//            switch (message.getStatus()) {
//                case "sent":
//                    sentHolder.messageStatus.setImageResource(R.drawable.ic_done);
//                    sentHolder.messageStatus.setColorFilter(ContextCompat.getColor(context, R.color.message_time_color),
//                            PorterDuff.Mode.SRC_IN);
//                    break;
//                case "delivered":
//                    sentHolder.messageStatus.setImageResource(R.drawable.ic_done_all);
//                    sentHolder.messageStatus.setColorFilter(ContextCompat.getColor(context, R.color.message_time_color),
//                            PorterDuff.Mode.SRC_IN);
//                    break;
//                case "read":
//                    sentHolder.messageStatus.setImageResource(R.drawable.ic_done_all);
//                    sentHolder.messageStatus.setColorFilter(ContextCompat.getColor(context, R.color.message_status_read),
//                            PorterDuff.Mode.SRC_IN);
//                    break;
//            }
//
//            // Animate only if not already animated
//            if (!message.isAnimated()) {
//                sentHolder.itemView.startAnimation(
//                        AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
//                message.setAnimated(true);
//            }
//
//        } else {
//            ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
//
//            receivedHolder.messageText.setText(message.getText());
//            receivedHolder.messageTime.setText(sdf.format(new Date(message.getTimestamp())));
//
//            if (!message.isAnimated()) {
//                receivedHolder.itemView.startAnimation(
//                        AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down));
//                message.setAnimated(true);
//            }
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return messageList.size();
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return messageList.get(position).getSender().equals(currentUserEmail) ? TYPE_SENT : TYPE_RECEIVED;
//    }
//
//    // ViewHolder for sent messages
//    private static class SentMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText;
//        TextView messageTime;
//        ImageView messageStatus;
//
//        SentMessageHolder(View itemView) {
//            super(itemView);
//            messageText = itemView.findViewById(R.id.messageText);
//            messageTime = itemView.findViewById(R.id.messageTime);
//            messageStatus = itemView.findViewById(R.id.messageStatus);
//        }
//    }
//
//    // ViewHolder for received messages
//    private static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
//        TextView messageText;
//        TextView messageTime;
//
//        ReceivedMessageHolder(View itemView) {
//            super(itemView);
//            messageText = itemView.findViewById(R.id.messageText);
//            messageTime = itemView.findViewById(R.id.messageTime);
//        }
//    }
//
//    // Update message status and refresh
//    public void updateMessageStatus(String messageId, String status) {
//        for (int i = 0; i < messageList.size(); i++) {
//            if (messageList.get(i).getId().equals(messageId)) {
//                messageList.get(i).setStatus(status);
//                notifyItemChanged(i);
//                break;
//            }
//        }
//    }
//
//    // Add new message to the list
//    public void addMessage(Message message) {
//        messageList.add(message);
//        notifyItemInserted(messageList.size() - 1);
//    }
//}

package com.example.caretakerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private Context context;
    private List<Message> messageList;
    private String currentUserEmail;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserEmail = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("user_email", "");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);
        String time = new SimpleDateFormat("h:mm a", Locale.getDefault())
                .format(new Date(message.getTimestamp()));

        if (holder.getItemViewType() == TYPE_SENT) {
            SentMessageHolder sentHolder = (SentMessageHolder) holder;
            sentHolder.messageText.setText(message.getText());
            sentHolder.messageTime.setText(time);

            // Set status icon based on message status
            switch (message.getStatus()) {
                case "sent":
                    sentHolder.messageStatus.setImageResource(R.drawable.ic_done);
                    break;
                case "delivered":
                case "read":
                    sentHolder.messageStatus.setImageResource(R.drawable.ic_done_all);
                    break;
            }
        } else {
            ReceivedMessageHolder receivedHolder = (ReceivedMessageHolder) holder;
            receivedHolder.messageText.setText(message.getText());
            receivedHolder.messageTime.setText(time);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getSender().equals(currentUserEmail)
                ? TYPE_SENT : TYPE_RECEIVED;
    }

    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;
        ImageView messageStatus;

        SentMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageStatus = itemView.findViewById(R.id.messageStatus);
        }
    }

    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText, messageTime;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
        }
    }
}