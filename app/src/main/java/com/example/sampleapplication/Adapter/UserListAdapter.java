package com.example.sampleapplication.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.sampleapplication.Models.UserList;
import com.example.sampleapplication.R;
import com.example.sampleapplication.UI.Activities.SignupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    FirebaseAuth auth;
    private ArrayList<UserList> userListItems;
    private Context context;
    private OnItemClickListener xListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        //used to simulate the onItemClick of a listview
        xListener = listener;
    }

    public UserListAdapter(ArrayList<UserList> userListItems, Context context) {
        this.userListItems = userListItems;
        this.context = context;
    }

    @Override
    public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        //final UserList userList = userListItems.get(position);

        auth = FirebaseAuth.getInstance();
        String currentId = auth.getCurrentUser().getUid();

        if (currentId.equals(userListItems.get(position).getId())) {
            holder.userLayout.setVisibility(View.GONE);
            holder.userView.setVisibility(View.GONE);
            holder.name_text.setVisibility(View.GONE);
            holder.contact_text.setVisibility(View.GONE);
            holder.profileImage.setVisibility(View.GONE);
        }
        holder.name_text.setText(userListItems.get(position).getName());
        //holder.email_text.setText(userListItems.get(position).getEmail());
        holder.contact_text.setText(userListItems.get(position).getId());
        //holder.password_text.setText(userListItems.get(position).getPassword());

        //Picasso.with(context).load(userListItems.get(position).getImageUrl()).error(R.drawable.logo).placeholder(R.drawable.ic_send).into(holder.profileImage);
        Glide.with(context).load(userListItems.get(position).getImageUrl()).into(holder.profileImage);
//        Picasso.with(context).load(userListItems.get(position).getImageUrl()).centerCrop().resize(500, 500).into(holder.profileImage);
        /*holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isClick) {

                Intent in = new Intent(context, UserChatActivity.class);
                view.getContext().startActivity(in);
            }
        });*/

    }

    @Override
    public int getItemCount() {
        return userListItems.size();
    }

    public void updateList(ArrayList<UserList> newList) {

//        userListItems = new ArrayList<>();
//        userListItems.addAll(newList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name_text;
        public TextView email_text;
        public TextView contact_text;
        public TextView password_text;
        public ImageView profileImage;
        public LinearLayout userLayout;
        public View userView;
        //private ItemClickListener itemClickListener;

        public ViewHolder(final View itemView) {
            super(itemView);

            name_text = itemView.findViewById(R.id.name_txtview);
            //email_text = itemView.findViewById(R.id.email_txtview);
            contact_text = itemView.findViewById(R.id.contact_txtview);
            //password_text = itemView.findViewById(R.id.password_txtview);
            profileImage = itemView.findViewById(R.id.image_profile);
            userLayout = itemView.findViewById(R.id.user_linerLayout);
            userView = itemView.findViewById(R.id.user_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (xListener != null) {
                        int position = getAdapterPosition();
                        //noposition to make sure the position is still valid
                        if (position != RecyclerView.NO_POSITION) {
                            //onItemclick is the method that we created on the interface
                            //UserList ul = userListItems.get(position);
                            xListener.onItemClick(position);
                        }
                    }
                }
            });

        }

        /*public void setItemClickListener(ItemClickListener itemClickListener)
        {
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {

            itemClickListener.onClick(v, getAdapterPosition(), false);

        }*/
    }
}
