package com.kd.filmstrip.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kd.filmstrip.DetailActivityV2;
import com.kd.filmstrip.EditProfileActivity;
import com.kd.filmstrip.FollowersActivity;
import com.kd.filmstrip.FollowingActivity;
import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.ProfileActivity.EditRelationShip;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;

public class ProfileImagesAdapter extends RecyclerView.Adapter<ProfileImagesAdapter.ProfileHolder> {
    
	public static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
                                              // IF the view under inflation and population is header or Item
   	public static final int TYPE_ITEM = 1;
	
   	private static Activity activity;
	private LayoutInflater inflater;
	private static ArrayList<InstagramImage> profileImageGrid;

	public InstagramImage image;
	public static String id;
	public static String string_fullname, string_username, string_profilepic, string_bannercolor;
	public static String string_followers, string_following, string_posts, string_bio, string_profile_btn;
	    
	public static class ProfileHolder extends ViewHolder implements View.OnClickListener {
			int Holderid;
			public ImageView gridThumbnail, profile;

            public View gridSelector;
			public LinearLayout banner, followersLink, followingLink;
			public TextView fullname,username,following_number,follower_number,post_number, bio;
			public Button profileBtn;
	        public ProfileHolder(View itemView, int itemType) {
				super(itemView);
	             
	            if(itemType == TYPE_ITEM) {
					gridThumbnail = (ImageView) itemView.findViewById(R.id.profile_gridImage);
                    gridSelector = (View) itemView.findViewById(R.id.profile_selector);
					gridSelector.setOnClickListener(this);
	                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
	            }
	            else{
	            	followersLink= (LinearLayout) itemView.findViewById(R.id.followers);
	            	followingLink = (LinearLayout) itemView.findViewById(R.id.following);
	                fullname = (TextView) itemView.findViewById(R.id.profile_fullname);         // Creating Text View object from header.xml for name
	                username = (TextView) itemView.findViewById(R.id.profile_username);       // Creating Text View object from header.xml for email
	                post_number = (TextView) itemView.findViewById(R.id.profile_posts);
	                follower_number = (TextView) itemView.findViewById(R.id.profile_followers);
	                following_number = (TextView) itemView.findViewById(R.id.profile_following);
	    	        profileBtn = (Button) itemView.findViewById(R.id.profile_btn);
	                bio = (TextView) itemView.findViewById(R.id.profile_bio);
	                banner = (LinearLayout) itemView.findViewById(R.id.profile_banner);
	                profile = (ImageView) itemView.findViewById(R.id.profile_image);
	                // Creating Image view object from header.xml for profile pic
	                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
	                profileBtn.setOnClickListener(this);
	                followersLink.setOnClickListener(this);
	                followingLink.setOnClickListener(this);
	            }

			}
			@Override
			public void onClick(View v) {
				if(Holderid == 1){
				InstagramImage gridItem = profileImageGrid.get(getPosition()-1);
				DetailActivityV2.launch(activity, gridThumbnail, gridItem, gridItem.thumbnail);
				}
				else {
				switch(v.getId()){
				case R.id.following:
					Intent i = new Intent(activity, FollowingActivity.class);   
					i.putExtra("user_id", id);
					i.putExtra("title", "Following " + "(" + string_following + ")");
					activity.startActivity(i);
				break;
				case R.id.followers:
					Intent e = new Intent(activity, FollowersActivity.class);   
					e.putExtra("user_id", id);
					e.putExtra("title", "Followers " + "(" + string_followers + ")");
					activity.startActivity(e);
				break;
				case R.id.profile_btn:
					new EditRelationShip(activity,HomeActivity.mApp.mAccessToken,id).execute();
				}
				}
			}
	}    
	public ProfileImagesAdapter (Activity a, ArrayList<InstagramImage> i){
		activity = a;
		setInflater((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		profileImageGrid = i;
	}
	public ProfileImagesAdapter (
			Activity a, 
			ArrayList<InstagramImage> i, 
			String fullname, 
			String username, 
			String profile, 
			String bannercolor,
			String posts,
			String followers,
			String following,
			String bio,
			String follow_status){
		activity = a;
		setInflater((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		profileImageGrid = i;
		string_fullname = fullname;
		string_username = username;
		string_profilepic = profile;
		string_bannercolor = bannercolor;
		
		string_posts = posts;
		string_followers = followers;
		string_following = following;
		string_bio = bio;
		string_profile_btn = follow_status;
	}
 

	@Override
	public int getItemCount() {
		return profileImageGrid.size()+1;
	}
	@Override
	public void onBindViewHolder(ProfileHolder holder, int i) {
       if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
    	    image = profileImageGrid.get(i-1);        
    	    id = image.user_id;
    	    Picasso.with(holder.itemView.getContext()).load(image.thumbnail).placeholder(android.R.color.background_light).into(holder.gridThumbnail);

       }
       else	{
    	   	holder.banner.setBackgroundColor(Color.parseColor(string_bannercolor));
    	   	holder.profileBtn.setBackgroundColor(Color.parseColor(string_bannercolor));
		    Picasso.with(holder.profile.getContext()).load(string_profilepic).placeholder(android.R.color.background_light).transform(new PicassoRound()).into(holder.profile);
            holder.profileBtn.setText(string_profile_btn);
            if(string_profile_btn == "Edit Your Profile") {

                holder.profileBtn.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {
                                  Intent edit = new Intent(activity,EditProfileActivity.class);
                                  activity.startActivityForResult(edit, 1);
                                  }
                       }
                );
            }
		    holder.fullname.setText(string_fullname);
    	   	holder.username.setText(string_username);
    	   	holder.post_number.setText(string_posts);
    	   	holder.follower_number.setText(string_followers);
    	   	holder.following_number.setText(string_following);
            if(!string_bio.isEmpty()){
               holder.bio.setText(string_bio);
            }
            else {
               holder.bio.setVisibility(View.GONE);
            }
       }	
	}
	@Override
	public ProfileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_grid_item,parent,false); //Inflating the layout
 
            ProfileHolder proItem = new ProfileHolder(v,viewType); //Creating ViewHolder and passing the object of type view
 
            return proItem; // Returning the created object
 
            //inflate your layout and pass it to view holder
 
        } else if (viewType == TYPE_HEADER) {
 
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_grid_header,parent,false); //Inflating the layout
 
            ProfileHolder proHeader = new ProfileHolder(v,viewType); //Creating ViewHolder and passing the object of type view
 
            return proHeader; //returning the object created
 
             
        }
        return null;
	}
    @Override
    public int getItemViewType(int position) { 
        if (isPositionHeader(position))
            return TYPE_HEADER;
 
        return TYPE_ITEM;
    }
 
    private boolean isPositionHeader(int position) {
        return position == 0;
    }
	public LayoutInflater getInflater() {
		return inflater;
	}

	public void setInflater(LayoutInflater inflater) {
		this.inflater = inflater;
	}

  }