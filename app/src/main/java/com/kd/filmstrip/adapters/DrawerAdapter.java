package com.kd.filmstrip.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.ProfileActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.image.PicassoRound;
import com.squareup.picasso.Picasso;
 
/**
 * Created by hp1 on 28-12-2014.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
     
    private static final int TYPE_HEADER = 0;  // Declaring Variable to Understand which View is being worked on
                                               // IF the view under inflation and population is header or Item
    private static final int TYPE_ITEM = 1;
 
    private String mNavTitles[]; // String Array to store the passed titles Value from MainActivity.java
    private TypedArray mIcons;       // Int Array to store the passed icons resource value from MainActivity.java
     
    private static Context mContext;
    private String fullname;        //String Resource for header View Name
    private String profile;        //int Resource for header view profile picture
    private String username;       //String Resource for header view email 
    private String background;
    private static ClickListener clicklistener;
 
    // Creating a ViewHolder which extends the RecyclerView View Holder
    // ViewHolder are used to to store the inflated views in order to recycle them
 
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int Holderid;      
         
        TextView textView; 
        ImageView imageView;
        ImageView profile,background;
        TextView username,fullname;
        
 
        public ViewHolder(View itemView,int ViewType) {                 // Creating ViewHolder Constructor with View and viewType As a parameter
            super(itemView);
             
            
            // Here we set the appropriate view in accordance with the the view type as passed when the holder object is created
             
            if(ViewType == TYPE_ITEM) {
            	itemView.setOnClickListener(this);
                textView = (TextView) itemView.findViewById(R.id.drawer_item_title); // Creating TextView object with the id of textView from item_row.xml
                imageView = (ImageView) itemView.findViewById(R.id.drawer_item_icon);// Creating ImageView object with the id of ImageView from item_row.xml
                Holderid = 1;                                               // setting holder id as 1 as the object being populated are of type item row
            }
            else{
 
            	LinearLayout accountSwitcher = (LinearLayout) itemView.findViewById(R.id.account_switcher);
                fullname = (TextView) itemView.findViewById(R.id.drawer_header_fullname);         // Creating Text View object from header.xml for name
                username = (TextView) itemView.findViewById(R.id.drawer_header_uname);       // Creating Text View object from header.xml for email
                profile = (ImageView) itemView.findViewById(R.id.drawer_profile_image);
                background = (ImageView) itemView.findViewById(R.id.drawer_header_image);
                background.setColorFilter(Color.argb(90, 0, 0, 0));
                // Creating Image view object from header.xml for profile pic
                Holderid = 0;                                                // Setting holder id = 0 as the object being populated are of type header view
            	profile.setOnClickListener(this);
            	accountSwitcher.setOnClickListener(this);
            	
            }
        }


		@Override
		public void onClick(View v) {
			if(Holderid != 0){
			if(clicklistener != null){
				clicklistener.itemClicked(v, getPosition());
			}
			}
			else {
				Intent profile = new Intent(mContext, ProfileActivity.class);
				profile.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				profile.putExtra("profile_mine", true);
				profile.putExtra("profile_page", HomeActivity.mApp.getId());
				profile.putExtra("profile_picture", HomeActivity.mApp.getProfilePic());
				profile.putExtra("profile_name", HomeActivity.mApp.getName());
				profile.putExtra("profile_uname", HomeActivity.mApp.getUserName());
				profile.putExtra("profile_posts", HomeActivity.mApp.getMediaNumber());
			    profile.putExtra("profile_followers", HomeActivity.mApp.getFollowedBy());
			    profile.putExtra("profile_following", HomeActivity.mApp.getFollows());
		        mContext.startActivity(profile);
			}
		}
 
         
    }
 
 
 
    public DrawerAdapter(Context context, String Titles[], String FullName,String UserName, String Profile, String Background){ // MyAdapter Constructor with titles and icons parameter
            

    	// titles, icons, name, email, profile pic are passed from the main activity as we
        mNavTitles = Titles;                //have seen earlier
        mContext = context;
        mIcons =   mContext.getResources().obtainTypedArray(R.array.drawer_icons);
        fullname = FullName;
        username = UserName;
        profile = Profile;  
        background = Background;
        //here we assign those passed values to the values we declared here
        //in adapter
 
 
 
    }
 
 
 
    //Below first we ovverride the method onCreateViewHolder which is called when the ViewHolder is
    //Created, In this method we inflate the item_row.xml layout if the viewType is Type_ITEM or else we inflate header.xml
    // if the viewType is TYPE_HEADER
    // and pass it to the view holder
 
    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
 
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item,parent,false); //Inflating the layout
 
            ViewHolder vhItem = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view
 
            return vhItem; // Returning the created object
 
            //inflate your layout and pass it to view holder
 
        } else if (viewType == TYPE_HEADER) {
 
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_header,parent,false); //Inflating the layout
 
            ViewHolder vhHeader = new ViewHolder(v,viewType); //Creating ViewHolder and passing the object of type view
 
            return vhHeader; //returning the object created
 
             
        }
        return null;
 
    }
 
    //Next we override a method which is called when the item in a row is needed to be displayed, here the int position
    // Tells us item at which position is being constructed to be displayed and the holder id of the holder object tell us
    // which view type is being created 1 for item row
    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder holder, int position) {
        if(holder.Holderid ==1) {                              // as the list view is going to be called after the header view so we decrement the
                                                              // position by 1 and pass it to the holder while setting the text and image
            holder.textView.setText(mNavTitles[position - 1]); // Setting the Text with the array of our Titles
            if(position < 6){
            holder.imageView.setImageResource(mIcons.getResourceId(position - 1, -1));// Settimg the image with array of our icons
            }
            if(position == 5){
            holder.itemView.findViewById(R.id.drawer_divider).setVisibility(View.VISIBLE);
        	}
        }
        else{
        	Picasso.with(holder.background.getContext()).load(background).placeholder(android.R.color.background_light).into(holder.background);
            Picasso.with(holder.profile.getContext()).load(profile).transform(new PicassoRound()).into(holder.profile);
            holder.fullname.setText(fullname);
            holder.username.setText(username);
        }
    }
 
    // This method returns the number of items present in the list
    @Override
    public int getItemCount() {
        return mNavTitles.length+1; // the number of items in the list will be +1 the titles including the header view.
    }
 
     
    // Witht the following method we check what type of view is being passed
    @Override
    public int getItemViewType(int position) { 
        if (isPositionHeader(position))
            return TYPE_HEADER;
 
        return TYPE_ITEM;
    }
 
    private boolean isPositionHeader(int position) {
        return position == 0;
    }
    public void setClickListener(ClickListener listener){
    	clicklistener = listener;
    }
    public interface ClickListener {
    	public void itemClicked(View v, int pos);
    }

}
