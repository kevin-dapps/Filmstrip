package com.kd.filmstrip.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kd.filmstrip.ProfileActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.UserList;
import com.squareup.picasso.Picasso;

public class UserAdapter extends BaseAdapter {
	 	private Activity activity;
	 	private LayoutInflater inflater;
	    private ArrayList<UserList> userList;

	    public UserAdapter (Activity a, ArrayList<UserList> i){
		activity = a;
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		userList = i;
	    }
 
   @Override
    public int getCount() {
            return userList.size();
    }
 
    @Override
    public UserList getItem(int position) {
            return userList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
            return position;
    }
 
    public static class ViewHolder {
        public ImageView listPhoto;
        public TextView listUsername, listFullname;
    }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        final ViewHolder holder;
        if(view == null){
        	
        	vi = inflater.inflate(R.layout.user_list_item, viewGroup, false);
            holder = new ViewHolder();
        	holder.listPhoto = (ImageView) vi.findViewById(R.id.listPhoto);
        	holder.listUsername = (TextView) vi.findViewById(R.id.listUsername);
        	holder.listFullname = (TextView) vi.findViewById(R.id.listFullname);

            vi.setTag(holder);
        }
        else {
            holder = (ViewHolder)vi.getTag();
        }
          final UserList userData = userList.get(i);
          
          vi.setOnClickListener(new View.OnClickListener() {
  			
			@Override
			public void onClick(View v) {
				    Intent profile = new Intent(activity, ProfileActivity.class);
				    profile.putExtra("profile_page", userData.user_id);
				    profile.putExtra("profile_picture", userData.profile_pic);
				    profile.putExtra("profile_name", userData.fullname);
				    profile.putExtra("profile_uname", userData.username);
				    profile.putExtra("profile_posts", 0);
				    profile.putExtra("profile_follows", 0);
				    profile.putExtra("profile_following", 0);
			        ActivityOptionsCompat options = ActivityOptionsCompat
			                .makeSceneTransitionAnimation(activity, holder.listPhoto, "robot");
			            // start the new activity
			        activity.startActivity(profile, options.toBundle());
			}
		  });

          Picasso.with(vi.getContext()).load(userData.profile_pic).placeholder(R.drawable.person_image_empty).transform(new PicassoRound()).into(holder.listPhoto);

          holder.listUsername.setText(userData.username);
          holder.listFullname.setText(userData.fullname);

 
          return vi;
        }
}
