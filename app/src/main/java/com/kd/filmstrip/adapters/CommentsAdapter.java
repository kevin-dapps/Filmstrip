package com.kd.filmstrip.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kd.filmstrip.ProfileActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.fragment.ProfileFragment;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.Comment;
import com.squareup.picasso.Picasso;

public class CommentsAdapter extends BaseAdapter {
	 	private Activity activity;
	 	private LayoutInflater inflater;
	    private ArrayList<Comment> photoComments;

	    public CommentsAdapter (Activity a, ArrayList<Comment> i){
		activity = a;
    inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    photoComments = i;
	    }
 
   @Override
    public int getCount() {
            return photoComments.size();
    }
 
    @Override
    public Comment getItem(int position) {
            return photoComments.get(position);
    }
 
    @Override
    public long getItemId(int position) {
            return position;
    }
 
    public static class ViewHolder {
        public ImageView commentPhoto;
        public TextView commentUsername, commentText;
    }

    @Override public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        final ViewHolder holder;
        if(vi == null){
        	
        	vi = inflater.inflate(R.layout.comment_item, viewGroup, false);
            holder = new ViewHolder();
        	holder.commentPhoto = (ImageView) vi.findViewById(R.id.commentPhoto);
        	holder.commentUsername = (TextView) vi.findViewById(R.id.commentUsername);
        	holder.commentText = (TextView) vi.findViewById(R.id.commentText);

            vi.setTag(holder);
        }
        else {
            holder = (ViewHolder)vi.getTag();
        }
          vi.setAlpha(0);
          vi.animate().alpha(1).setDuration(1000).start();
          final Comment comment = photoComments.get(i);
          if(comment.user_id != null){
          holder.commentPhoto.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				    Intent profile = new Intent(activity, ProfileActivity.class);
				    profile.putExtra("profile_page", comment.user_id);
				    profile.putExtra("profile_picture", comment.profile_pic);
				    profile.putExtra("profile_name", comment.fullname);
				    profile.putExtra("profile_uname", comment.username);
				    profile.putExtra("profile_posts", 0);
				    profile.putExtra("profile_follows", 0);
				    profile.putExtra("profile_following", 0);

			        ActivityOptionsCompat options = ActivityOptionsCompat
			                .makeSceneTransitionAnimation(activity, holder.commentPhoto, "robot");
			            // start the new activity
			        if(Build.VERSION.SDK_INT >= 16){
			        activity.startActivity(profile, options.toBundle());
			        }
			        else {
			        activity.startActivity(profile);
			        }
					
				}
		  });
          }
          Picasso.with(vi.getContext()).load(comment.profile_pic).transform(new PicassoRound()).into(holder.commentPhoto);

          holder.commentUsername.setText(comment.username);
          holder.commentText.setText(comment.comment);

 
          return vi;
        }
}