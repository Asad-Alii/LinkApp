package com.example.sampleapplication.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.sampleapplication.Listeners.ItemClickListener;
import com.example.sampleapplication.MessageService.ChatQueries;
import com.example.sampleapplication.Models.Messages;
import com.example.sampleapplication.R;
import com.example.sampleapplication.UI.Activities.UserActivity;
import com.example.sampleapplication.UI.Activities.UserChatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class MessagesListAdapter extends RecyclerView.Adapter<MessagesListAdapter.ViewHolder> {

    FirebaseAuth auth;
    private ArrayList<Messages> messageListItems;
    private Context context;
    private MessagesListAdapter.OnItemClickListener xListener;
    UserChatActivity chat;
    FirebaseFirestore fireBaseApp = FirebaseFirestore.getInstance();
    SharedPreferences sharedMemory;

    private static final int VIEW_TYPE_LOADING = 0;
    private static final int VIEW_TYPE_NORMAL = 1;
    private boolean isLoaderVisible = false;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnClickListener(MessagesListAdapter.OnItemClickListener listener) {
        //used to simulate the onItemClick of a listview
        xListener = listener;
    }

    public MessagesListAdapter(ArrayList<Messages> messageListItems, Context context) {
        this.messageListItems = messageListItems;
        this.context = context;
    }

    @Override
    public MessagesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (messageListItems.get(position).getMessage().equals("This message is deleted!"))
        {
            holder.msgTextView.setTextColor(Color.parseColor("#D3D3D3"));
        }

        chat = new UserChatActivity();
        sharedMemory = context.getSharedPreferences("testing", Context.MODE_PRIVATE);
        SimpleDateFormat simpleTime = new SimpleDateFormat("hh:mm a");
        SimpleDateFormat simpleDate = new SimpleDateFormat("dd MMM");
        String time = simpleTime.format(messageListItems.get(position).getTime());
        String date = simpleDate.format(messageListItems.get(position).getTime());

        String senderId = sharedMemory.getString("senderId", "");
        String receiverImage = sharedMemory.getString("receiverImage", "");
        String senderImage = sharedMemory.getString("senderImage", "");

        Glide.with(context).load(receiverImage).into(holder.chatReceiverImage);
        Glide.with(context).load(senderImage).into(holder.chatSenderImage);

        if (senderId.equals(messageListItems.get(position).getSenderId())) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mychatlayout.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.mychatlayout.setLayoutParams(params);
            holder.chatReceiverImage.setVisibility(View.GONE);
            holder.chatReceiverCardView.setVisibility(View.GONE);
        }
        else
        {
            holder.chatSenderImage.setVisibility(View.GONE);
            holder.chatSenderCardView.setVisibility(View.GONE);
        }

        if (messageListItems.get(position).getMessage().startsWith("https:")) {
            holder.msgTextView.setVisibility(View.GONE);
            //holder.msgImageView.setVisibility(View.VISIBLE);

            if (messageListItems.get(position).getMessage().contains(".jpg") || messageListItems.get(position).getMessage().contains(".png")){

                holder.msgImageView.setVisibility(View.VISIBLE);
                holder.msgVideoView.setVisibility(View.GONE);
                Glide.with(context).load(messageListItems.get(position).getMessage()).into(holder.msgImageView);
            }

            if (messageListItems.get(position).getMessage().contains(".mp4")){

                holder.msgVideoView.setVisibility(View.VISIBLE);
                holder.msgImageView.setVisibility(View.GONE);

                Uri video = Uri.parse(messageListItems.get(position).getMessage());

                holder.msgVideoView.setVideoURI(video);
                holder.msgVideoView.start();
            }
            else {

                if (messageListItems.get(position).getMessage().contains(".pdf")){
                    holder.msgImageView.setImageResource(R.drawable.pdf);
                    holder.msgVideoView.setVisibility(View.GONE);
                }

                if (messageListItems.get(position).getMessage().contains(".docx")){
                    holder.msgImageView.setImageResource(R.drawable.docx);
                    holder.msgVideoView.setVisibility(View.GONE);
                }

                if (messageListItems.get(position).getMessage().contains(".txt")){
                    holder.msgImageView.setImageResource(R.drawable.txt);
                    holder.msgVideoView.setVisibility(View.GONE);
                }

                if (messageListItems.get(position).getMessage().contains(".xlsx")){
                    holder.msgImageView.setImageResource(R.drawable.xlsx);
                    holder.msgVideoView.setVisibility(View.GONE);
                }
            }
            holder.timeTextView.setText(date + ", " + time);

        } else {
            holder.msgImageView.setVisibility(View.GONE);
            holder.msgVideoView.setVisibility(View.GONE);
            holder.msgTextView.setVisibility(View.VISIBLE);
            holder.msgTextView.setText(messageListItems.get(position).getMessage());
            holder.timeTextView.setText(date + ", " + time);
        }
    }

    @Override
    public int getItemCount() {
        return messageListItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView msgTextView;
        public TextView timeTextView;
        public RelativeLayout mychatlayout;
        public ImageView msgImageView;
        public VideoView msgVideoView;
        public ImageView chatReceiverImage;
        public ImageView chatSenderImage;
        public CardView chatSenderCardView;
        public CardView chatReceiverCardView;

        //private ItemClickListener itemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            msgTextView = itemView.findViewById(R.id.msg_textview);
            timeTextView = itemView.findViewById(R.id.time_textview);
            mychatlayout = itemView.findViewById(R.id.mychat_layout);
            msgImageView = itemView.findViewById(R.id.msg_imageview);
            msgVideoView = itemView.findViewById(R.id.msg_videoview);
            chatReceiverImage = itemView.findViewById(R.id.chat_receiverImage);
            chatSenderImage = itemView.findViewById(R.id.chat_senderImage);
            chatSenderCardView = itemView.findViewById(R.id.chat_senderCardView);
            chatReceiverCardView = itemView.findViewById(R.id.chat_receiverCardView);

            //itemView.setOnClickListener(this);
            //itemView.setOnLongClickListener(this);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (xListener != null) {
                        int position = getAdapterPosition();
                        //noposition to make sure the position is still valid
                        if (position != RecyclerView.NO_POSITION) {
                            //onItemclick is the method that we created on the interface
                            xListener.onItemClick(position);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (xListener != null) {
                        int position = getAdapterPosition();
                        //noposition to make sure the position is still valid
                        if (position != RecyclerView.NO_POSITION) {
                            //onItemclick is the method that we created on the interface
                            //xListener.onItemClick(position);
                            xListener.onItemLongClick(position);
                        }
                    }
                    return true;
                }
            });

        }

        /*public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {

            itemClickListener.onClick(view, getAdapterPosition(), false);

        }

        @Override
        public boolean onLongClick(View view) {

            itemClickListener.onClick(view, getAdapterPosition(), true);
            return true;
        }*/
    }

}
