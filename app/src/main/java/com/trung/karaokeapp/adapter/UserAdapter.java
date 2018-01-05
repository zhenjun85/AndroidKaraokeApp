package com.trung.karaokeapp.adapter;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.trung.karaokeapp.R;
import com.trung.karaokeapp.entities.User;
import com.trung.karaokeapp.network.ApiService;
import com.trung.karaokeapp.network.AppURL;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by trung on 1/5/2018.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyViewHolder> {
    private Context context;
    private List<User> userList;
    private ApiService service;

    public UserAdapter(Context context, List<User> userList, ApiService service) {
        this.context = context;
        this.userList = userList;
        this.service = service;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rv_friend, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final User user = userList.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvBirthday.setText(user.getBirthday());
        if (user.getAvatar() != null){
            Glide.with(context).load(AppURL.baseUrlPhotos + "/" +
                    user.getAvatar()).into(holder.ivUserAvatar);
        }else {
            holder.ivUserAvatar.setImageDrawable(context.getDrawable(R.drawable.ic_face_black_48px));
        }
        //relation
        Call<Integer> callGetRelation = service.getRelationStatus(user.getId());
        callGetRelation.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
    /*
     * 0: same
     * 1: friend
     * 2: request
     * 3: nofriend
     * */
                int relationStatus = response.body();
                switch (relationStatus){
                    case 0:
                    case 1:
                        holder.btnFriendAction.setVisibility(View.GONE);
                        break;
                    case 2:
                        holder.btnFriendAction.setImageDrawable(context.getDrawable(R.drawable.ic_stopwatch_1));
                        holder.btnFriendAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(context, "Waiting accept", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                    case 3:
                        holder.btnFriendAction.setImageDrawable(context.getDrawable(R.drawable.ic_add_user));
                        holder.btnFriendAction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Call<Integer> callRequestFriend = service.requestFriend(user.getId());
                                callRequestFriend.enqueue(new Callback<Integer>() {
                                    @Override
                                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                                        Log.d("request friend", response.toString());
                                        if (response.body() == 1){
                                            holder.btnFriendAction.setImageDrawable(context.getDrawable(R.drawable.ic_stopwatch_1));
                                            holder.btnFriendAction.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    Toast.makeText(context, "Waiting accept", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Integer> call, Throwable t) {
                                        Log.d("request friend", t.getMessage());
                                    }
                                });
                            }
                        });
                        break;
                    default:break;
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivUserAvatar;
        TextView tvUserName, tvBirthday;
        ImageButton btnFriendAction;
        public MyViewHolder(View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvBirthday = itemView.findViewById(R.id.tvBirthday);
            btnFriendAction = itemView.findViewById(R.id.btnFriendAction);
        }
    }
}
