package com.kd.filmstrip.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import br.com.dina.oauth.instagram.InstagramApp;

import com.kd.filmstrip.FollowersActivity;
import com.kd.filmstrip.FollowingActivity;
import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.adapters.ProfileImagesAdapter;
import com.kd.filmstrip.customviews.HeaderGridView;
import com.kd.filmstrip.data.User;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.Comment;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import android.widget.TextView;

public class ProfileFragment extends Fragment {
    private static ProfileImagesAdapter adapter;
	public static ArrayList<InstagramImage> profileImageGrid;
	public static RecyclerView userPhotos;
	public static InstagramApp mApp = null;
	public static String userid = null;
	public static Button followControlBtn;
	public static TextView following_number,follower_number,post_number;
	public static ImageButton switchGrid, switchList;
	public static RelativeLayout following_container,follower_container,post_container;
	public static int following = 0,follower = 0;
	public static String next_url = null;
	public static User data;
	private int preLast;

    public static Fragment newInstance(Context context) {
        ProfileFragment f = new ProfileFragment();
        
        return f;
    }
    private void toggleInformationView(View view) {
        final View infoContainer = view.findViewById(R.id.profile_layout);

        int cx = 0;
        int cy = view.getTop();
        float radius = Math.max(infoContainer.getWidth(), infoContainer.getHeight()) * 2.0f;

        if (infoContainer.getVisibility() == View.INVISIBLE) {
            infoContainer.setVisibility(View.VISIBLE);
            ViewAnimationUtils.createCircularReveal(infoContainer, cx, cy, 0, radius).start();
        } else {
            Animator reveal = ViewAnimationUtils.createCircularReveal(
                    infoContainer, cx, cy, radius, 0);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    infoContainer.setVisibility(View.INVISIBLE);
                }
            });
            reveal.start();
        }
    }
	     
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
			mApp = HomeActivity.mApp;
			
	        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
	        
	        // previously invisible view
	        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
	        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar);
	        
	        final View myView = rootView.findViewById(R.id.profile_layout);

        	userPhotos = (RecyclerView) rootView.findViewById(R.id.profileImages);
        	GridLayoutManager glm = new GridLayoutManager(getActivity(),3);
	        profileImageGrid = new ArrayList<InstagramImage>();
	        adapter = new ProfileImagesAdapter(getActivity(), profileImageGrid);
	        userPhotos.setHasFixedSize(true);
        	userPhotos.setLayoutManager(glm);


        	final View header = getActivity().getLayoutInflater().inflate(R.layout.profile_grid_header, userPhotos, false); 
 	
        	LinearLayout profileInfo = (LinearLayout) header.findViewById(R.id.profile_info);
        	profileInfo.setBackgroundColor(Color.parseColor(HomeActivity.themePrimaryColors[HomeActivity.selectedThemeInt]));
        	followControlBtn = (Button) header.findViewById(R.id.profile_btn);
        	ImageView profile_image = (ImageView) header.findViewById(R.id.profile_image);
        	TextView profile_name = (TextView) header.findViewById(R.id.profile_name);
        	TextView profile_uname = (TextView) header.findViewById(R.id.profile_uname);
        	post_number = (TextView) header.findViewById(R.id.post_count);
        	follower_number = (TextView) header.findViewById(R.id.follower_count);
        	following_number =(TextView) header.findViewById(R.id.following_count);
        	
        	post_container = (RelativeLayout) header.findViewById(R.id.post_container);
        	follower_container = (RelativeLayout) header.findViewById(R.id.follower_container);
        	
        	following_container = (RelativeLayout) header.findViewById(R.id.following_container);

            //userPhotos.addView(header);
	        userPhotos.setAdapter(adapter);
	       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
	            myView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
	                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	                @Override
	                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
	                    v.removeOnLayoutChangeListener(this);
	                    toggleInformationView(myView);
	                }
	            });
	        }
	        else {
	        myView.setVisibility(View.VISIBLE);
	        }

	        final Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
	        final Animation enlarge = AnimationUtils.loadAnimation(getActivity(), R.anim.enlarge_anim);
	        header.findViewById(R.id.profile_image).startAnimation(slide);
	        header.findViewById(R.id.profile_controls).setVisibility(View.INVISIBLE);
	        header.findViewById(R.id.profile_bottom).startAnimation(enlarge);
	        new Handler().postDelayed(new Runnable()
	        {
	           @Override
	           public void run()
	           {
	        	   header.findViewById(R.id.profile_controls).setVisibility(View.VISIBLE);
	        	   header.findViewById(R.id.profile_controls).startAnimation(slide);
	           }
	        }, 400);
	        


	        Bundle extras = getArguments();
	        String profile_page = extras.getString("profile_page");
	        boolean isMine = extras.getBoolean("profile_mine");
    		userid = profile_page;

	        if(isMine || extras.getString("profile_uname") == mApp.getUserName()){
	        	followControlBtn.setText("Edit Your Profile");
	        	post_number.setText(String.valueOf(extras.getInt("profile_posts")));
	        	follower_number.setText(String.valueOf(extras.getInt("profile_follows")));
	        	following_number.setText(String.valueOf(extras.getInt("profile_following")));
	        	follower = extras.getInt("profile_follows");
	        	following = extras.getInt("profile_following");
	        }
	        else {

	        	new GetUserInfo(getActivity(),mApp.mAccessToken).execute();
	        	followControlBtn.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						new EditRelationShip(getActivity(),mApp.mAccessToken).execute();						
					}
				});

	        }
        	follower_container.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getActivity(), FollowersActivity.class);   
					i.putExtra("user_id", userid);
					i.putExtra("title", "Followers " + "(" + follower + ")");
					startActivity(i);
					
				}
			});
        	following_container.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i = new Intent(getActivity(), FollowingActivity.class);   
					i.putExtra("user_id", userid);
					i.putExtra("title", "Following " + "(" + following + ")");
					startActivity(i);
					
				}
			});

	        profile_name.setText(extras.getString("profile_name"));
	        profile_uname.setText("@"+extras.getString("profile_uname"));
	        Picasso.with(getActivity()).load(extras.getString("profile_picture")).transform(new PicassoRound()).into(profile_image);
	        
	        switchGrid = (ImageButton) header.findViewById(R.id.switchGrid);
	        switchList = (ImageButton) header.findViewById(R.id.switchList);
	        switchGrid.setColorFilter(Color.argb(255, 255, 255, 255)); 
	        switchList.setColorFilter(Color.argb(255, 255, 255, 255));
	        switchGrid.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					
				}
			});
	        switchList.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {

					
				}
			});

	        userPhotos.setAdapter(adapter);

	        new FetchUserPhotos().execute();
	        return rootView;

	}
	private static String streamToString(InputStream is) throws IOException {
		String str = "";

		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			str = sb.toString();
		}

		return str;
	}
	public static class GetUserInfo extends AsyncTask<Void, String, User> {
		private String accessToken;
	    private Activity activity;
		public GetUserInfo(Activity a, String _accessToken){
			setActivity(a);
			accessToken = _accessToken;
		}
		@Override 
		protected void onPreExecute(){
			
			return;
		}

		@Override
	    protected User doInBackground(Void... voids) {
	        try {
	        	URL url = new URL("https://api.instagram.com/v1/users/"+userid+"/?access_token="+accessToken);

	            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	            urlConnection.setRequestMethod("GET");
	            urlConnection.setDoInput(true);
	            urlConnection.connect();
	            
	        	URL url2 = new URL("https://api.instagram.com/v1/users/"+userid+"/relationship?access_token="+accessToken);

	            HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
	            urlConnection2.setRequestMethod("GET");
	            urlConnection2.setDoInput(true);
	            urlConnection2.connect();
	            
	            String response = streamToString(urlConnection.getInputStream());
	            
	            String response2 = streamToString(urlConnection2.getInputStream());

	            JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
	            JSONObject jsonObj2 = (JSONObject) new JSONTokener(response2).nextValue();
	            data = new User();
	            data.user_id = jsonObj.getJSONObject("data").getString("id");
	            data.username = jsonObj.getJSONObject("data").getString("username");
	            data.full_name = jsonObj.getJSONObject("data").getString("full_name");
	            data.profile_picture = jsonObj.getJSONObject("data").getString("profile_picture");
	            data.media = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("media");
	            data.follows = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("follows");
	            data.followed_by = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("followed_by");

	            if(jsonObj2.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("follows")){
		            data.outgoing_status = "Following";
		            data.unfollow_user = true;
	        	}
	            else if(jsonObj2.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("requested")){
		            data.outgoing_status = "Requested";
		            data.unfollow_user = true;
	        	}
	            else {
	            	data.outgoing_status = "Follow";
	            	data.unfollow_user = false;
	            	
	            }
	        	return data;
	        }

	        catch (Exception ex) {
	            ex.printStackTrace();
	        }
			return null;
	        
	    }
		@Override
	    protected void onPostExecute(User result) {
			if(result != null){
			followControlBtn.setText(result.outgoing_status);
        	post_number.setText(String.valueOf(result.media));
        	follower_number.setText(String.valueOf(result.followed_by));
        	following_number.setText(String.valueOf(result.follows));
        	follower = result.followed_by;
        	following = result.follows;
			}

	    }
		public Activity getActivity() {
			return activity;
		}
		public void setActivity(Activity activity) {
			this.activity = activity;
		}


	}
	public static class EditRelationShip extends AsyncTask<Void, String, User> {
		private String accessToken;
	    private String action;
		public EditRelationShip(Activity a, String _accessToken){
			accessToken = _accessToken;
		}
		@Override 
		protected void onPreExecute(){
			if(data.unfollow_user){
			action = "unfollow";
			}
			else {
			action = "follow";
			}
			
			return;
		}

		@Override
	    protected User doInBackground(Void... voids) {
	        try {
	        	URL url = new URL("https://api.instagram.com/v1/users/"+userid+"/relationship?access_token="+accessToken);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("POST");
				urlConnection.setDoInput(true);
				urlConnection.setDoOutput(true);
				//urlConnection.connect();
				OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
				writer.write("action="+action);
			    writer.flush();
				String response = streamToString(urlConnection.getInputStream());
	            
	            JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();


	            if(jsonObj.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("follows")){
		            data.outgoing_status = "Following";
		            data.unfollow_user = true;
	        	}
	            else if(jsonObj.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("requested")){
		            data.outgoing_status = "Requested";
		            data.unfollow_user = true;
	        	}
	            else {
	            	data.outgoing_status = "Follow";
	            	data.unfollow_user = false;
	            	
	            }
	        	return data;
	        }

	        catch (Exception ex) {
	            ex.printStackTrace();
	        }
			return null;
	        
	    }
		@Override
	    protected void onPostExecute(User result) {
			if(result != null){
			followControlBtn.setText(result.outgoing_status);
			}

	    }
	}
	    public static class FetchUserPhotos extends AsyncTask<Void, String, Boolean> {
	    	private static String streamToString(InputStream is) throws IOException {
	    		String str = "";

	    		if (is != null) {
	    			StringBuilder sb = new StringBuilder();
	    			String line;

	    			try {
	    				BufferedReader reader = new BufferedReader(
	    						new InputStreamReader(is));

	    				while ((line = reader.readLine()) != null) {
	    					sb.append(line);
	    				}

	    				reader.close();
	    			} finally {
	    				is.close();
	    			}

	    			str = sb.toString();
	    		}

	    		return str;
	    	}
	    	@Override 
	    	protected void onPreExecute(){
	    		profileImageGrid.clear();
	    		return;
	    	}

	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	            	URL url = new URL("https://api.instagram.com/v1/users/"+userid+"/media/recent/?access_token=" + mApp.mAccessToken);

	                Log.i("InstagramAPI", "Opening URL " + url.toString());
	                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	                urlConnection.setRequestMethod("GET");
	                urlConnection.setDoInput(true);
	                urlConnection.connect();
	                String response = streamToString(urlConnection.getInputStream());
	                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
	                try {
	                JSONObject more_data = jsonObj.getJSONObject("pagination");
	                next_url = more_data.getString("next_url");
	                }
	                catch(JSONException j){}
	                // parse the activity feed
	                JSONArray data = jsonObj.getJSONArray("data");
	 
	                // get image URLs and commentary
	                for( int i=0; i< data.length(); i++ ) {
	                    // create a new instance
	                    InstagramImage instagramImage = new InstagramImage();

	                    // image
	                    JSONObject image = (JSONObject) data.getJSONObject(i);

	                    JSONObject images = image.getJSONObject("images");
	                    if(images == null){
	                    	images = image.getJSONObject("videos");
	                    }
	                    //Log.i("JSONObjectReturn", images.toString());
	                    JSONObject thumbnailImage = images.getJSONObject("thumbnail");
	                    JSONObject lowResolutionImage = images.getJSONObject("low_resolution");
	                    JSONObject standardResolutionImage = images.getJSONObject("standard_resolution");
	                    instagramImage.id = image.getString("id");
	                    instagramImage.permalink = image.getString("link");

	                    instagramImage.user_has_liked = image.getBoolean("user_has_liked");

	                    // permalinks
	                    instagramImage.thumbnail = thumbnailImage.getString("url");
	                    instagramImage.low_resolution = lowResolutionImage.getString("url");
	                    instagramImage.standard_resolution = standardResolutionImage.getString("url");

	                    // Videos
	                    if(image.getString("type").equals("video")){
	                    JSONObject videos = image.getJSONObject("videos");
	                    JSONObject lowResolutionVideo = videos.getJSONObject("low_resolution");
	                    JSONObject standardResolutionVideo = videos.getJSONObject("standard_resolution");
	                    instagramImage.video = true;
	                    instagramImage.video_low_res = lowResolutionVideo.getString("url");
	                    instagramImage.video_standard_res = standardResolutionVideo.getString("url");
	                    }
	                    
                    	try {
	                    JSONObject location = image.getJSONObject("location");
	                    if(location != null){
	                    instagramImage.location_name = location.getString("name");
	                    instagramImage.hasLocation = true;
                        }
                    	} catch (JSONException e) {}
                    	
	                    // user
	                    JSONObject user = image.getJSONObject("user");
	                    instagramImage.username = user.getString("username");
	                    instagramImage.user_id = user.getString("id");
	                    instagramImage.profile_picture = user.getString("profile_picture");
	                    instagramImage.full_name = user.getString("full_name");

                        // date taken_at
                        Long dateLong = image.getLong("created_time");
                        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy HH:mm");
                        instagramImage.taken_at = formatter.format(new Date(dateLong * 1000L));
                        instagramImage.taken_time = dateLong * 1000L;
	                    
                        // comments
                        instagramImage.comment_count = image.getJSONObject("comments").getInt("count");
                        JSONArray comments = image.getJSONObject("comments").getJSONArray("data");
                        if( comments != null ) {
                            ArrayList<Comment> commentList = new ArrayList<Comment>();
                            for( int c=0; c < comments.length(); c++ ) {
                                JSONObject comment = comments.getJSONObject(c);
                                JSONObject from = comment.getJSONObject("from");
                                commentList.add(new Comment(from.getString("username"), from.getString("full_name"),
                                        comment.getString("text"), from.getString("profile_picture"), from.getString("id")));
                            }
                            instagramImage.comment_list = commentList;
                        }
                        
	                    // caption

	                    try {
	                        JSONObject caption = image.getJSONObject("caption");
	                        if( caption != null ) {
	                            instagramImage.caption = caption.getString("text");
	                        }
	                    } catch (JSONException e) {}

	                    // likers
	                    try {
	                        instagramImage.liker_count = image.getJSONObject("likes").getInt("count");
	                        JSONArray likes = image.getJSONObject("likes").getJSONArray("data");
	                        if( likes != null ) {
	                            ArrayList<String> likerList = new ArrayList<String>();
	                            if( likes.length() > 0 ) {
	                                for( int l=0; l < likes.length(); l++ ) {
	                                    JSONObject like = likes.getJSONObject(l);
	                                    likerList.add(like.getString("username"));
	                                }
	                                instagramImage.liker_list = likerList;
	                            }
	                        }
	                    } catch( JSONException j ) {}

	                    profileImageGrid.add(instagramImage);
	                }
	                return true;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
				return false;
	            
	        }
	    	@Override
	        protected void onPostExecute(Boolean result) {

	                if(result) {
	                	adapter.notifyDataSetChanged(); 
	                }
	        }
	    }
	    public static class LoadMorePhotos extends AsyncTask<Void, String, Boolean> {
	    	public static String load_url;
	    	private static String streamToString(InputStream is) throws IOException {
	    		String str = "";

	    		if (is != null) {
	    			StringBuilder sb = new StringBuilder();
	    			String line;

	    			try {
	    				BufferedReader reader = new BufferedReader(
	    						new InputStreamReader(is));

	    				while ((line = reader.readLine()) != null) {
	    					sb.append(line);
	    				}

	    				reader.close();
	    			} finally {
	    				is.close();
	    			}

	    			str = sb.toString();
	    		}

	    		return str;
	    	}
	    	public LoadMorePhotos(String url){
	    		load_url = url;
	    	}
	    	@Override 
	    	protected void onPreExecute(){
	    		return;
	    	}
	    	

	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	            	URL url = new URL(load_url);

	                Log.i("InstagramAPI", "Opening URL " + url.toString());
	                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	                urlConnection.setRequestMethod("GET");
	                urlConnection.setDoInput(true);
	                urlConnection.connect();
	                String response = streamToString(urlConnection.getInputStream());
	                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
	                try {
	                JSONObject more_data = jsonObj.getJSONObject("pagination");
	                next_url = more_data.getString("next_url");
	                }
	                catch(JSONException j){
	                	next_url= null;
	                }
	                // parse the activity feed
	                JSONArray data = jsonObj.getJSONArray("data");
	 
	                // get image URLs and commentary
	                for( int i=0; i< data.length(); i++ ) {
	                    // create a new instance
	                    InstagramImage instagramImage = new InstagramImage();

	                    // image
	                    JSONObject image = (JSONObject) data.getJSONObject(i);

	                    JSONObject images = image.getJSONObject("images");
	                    if(images == null){
	                    	images = image.getJSONObject("videos");
	                    }
	                    //Log.i("JSONObjectReturn", images.toString());
	                    JSONObject thumbnailImage = images.getJSONObject("thumbnail");
	                    JSONObject lowResolutionImage = images.getJSONObject("low_resolution");
	                    JSONObject standardResolutionImage = images.getJSONObject("standard_resolution");
	                    instagramImage.id = image.getString("id");
	                    instagramImage.permalink = image.getString("link");

	                    instagramImage.user_has_liked = image.getBoolean("user_has_liked");

	                    // permalinks
	                    instagramImage.thumbnail = thumbnailImage.getString("url");
	                    instagramImage.low_resolution = lowResolutionImage.getString("url");
	                    instagramImage.standard_resolution = standardResolutionImage.getString("url");

	                    // user
	                    JSONObject user = image.getJSONObject("user");
	                    instagramImage.username = user.getString("username");
	                    instagramImage.user_id = user.getString("id");
	                    instagramImage.profile_picture = user.getString("profile_picture");
	                    instagramImage.full_name = user.getString("full_name");

                        // date taken_at
                        Long dateLong = image.getLong("created_time");
                        SimpleDateFormat formatter = new SimpleDateFormat("MMMM d, yyyy HH:mm");
                        instagramImage.taken_at = formatter.format(new Date(dateLong * 1000L));
                        instagramImage.taken_time = dateLong * 1000L;
	                    
                        // comments
                        instagramImage.comment_count = image.getJSONObject("comments").getInt("count");
                        JSONArray comments = image.getJSONObject("comments").getJSONArray("data");
                        if( comments != null ) {
                            ArrayList<Comment> commentList = new ArrayList<Comment>();
                            for( int c=0; c < comments.length(); c++ ) {
                                JSONObject comment = comments.getJSONObject(c);
                                JSONObject from = comment.getJSONObject("from");
                                commentList.add(new Comment(from.getString("username"), from.getString("full_name"),
                                        comment.getString("text"), from.getString("profile_picture"), from.getString("id")));
                            }
                            instagramImage.comment_list = commentList;
                        }
                        
	                    // caption

	                    try {
	                        JSONObject caption = image.getJSONObject("caption");
	                        if( caption != null ) {
	                            instagramImage.caption = caption.getString("text");
	                        }
	                    } catch (JSONException e) {}

	                    // likers
	                    try {
	                        instagramImage.liker_count = image.getJSONObject("likes").getInt("count");
	                        JSONArray likes = image.getJSONObject("likes").getJSONArray("data");
	                        if( likes != null ) {
	                            ArrayList<String> likerList = new ArrayList<String>();
	                            if( likes.length() > 0 ) {
	                                for( int l=0; l < likes.length(); l++ ) {
	                                    JSONObject like = likes.getJSONObject(l);
	                                    likerList.add(like.getString("username"));
	                                }
	                                instagramImage.liker_list = likerList;
	                            }
	                        }
	                    } catch( JSONException j ) {}

	                    profileImageGrid.add(instagramImage);
	                }
	                return true;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
				return false;
	            
	        }
	    	@Override
	        protected void onPostExecute(Boolean result) {
	                if(result) {
	                	adapter.notifyDataSetChanged(); 
	                }
	        }
	    }
}
