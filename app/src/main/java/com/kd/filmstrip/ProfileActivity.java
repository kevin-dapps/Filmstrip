package com.kd.filmstrip;

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
import com.kd.filmstrip.R;
import com.kd.filmstrip.adapters.DrawerAdapter;
import com.kd.filmstrip.adapters.PopularImagesAdapter;
import com.kd.filmstrip.adapters.ProfileImagesAdapter;
import com.kd.filmstrip.customviews.HeaderGridView;
import com.kd.filmstrip.data.User;
import com.kd.filmstrip.fragment.FeedFragment2.FetchFeed;
import com.kd.filmstrip.fragment.ProfileFragment.GetUserInfo;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.Comment;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends BaseActivity {
    private static ProfileImagesAdapter adapter;
	public static ArrayList<InstagramImage> profileImageGrid;
	public static RecyclerView userPhotos;
	public static InstagramApp mApp;
	public String userid = null;
	public static Button followControlBtn;
	public static TextView following_number,follower_number,post_number;
	public static ImageButton switchGrid, switchList;
	public static RelativeLayout following_container,follower_container,post_container;
	//public static int following = 0,follower = 0, posts = 0;
	public static Activity a;
	public static String next_url = null;
	public static User data;
	private int preLast;
	private static Bundle extras;
	// Theme Info
	public static int selectedThemeInt;
	public static String selectedTheme;
	public static String[] themePrimaryColors;
	public static String[] themePrimaryDarkColors;
	private static SwipeRefreshLayout swipeRefreshLayout;
	private static boolean isMine = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        	a = this;
    		mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
    				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);    
	        
	        final View myView = findViewById(R.id.profile_layout);
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
        	userPhotos = (RecyclerView) findViewById(R.id.profileImages);
        	GridLayoutManager glm = new GridLayoutManager(this,3);
        	glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch(position){
                        case ProfileImagesAdapter.TYPE_HEADER:
                            return 3;
                        default:
                            return 1;
                    }
                }
            });
 


	        if(savedInstanceState == null) {
                extras = getIntent().getExtras();
                String profile_page = extras.getString("profile_page");
                isMine = extras.getBoolean("profile_mine");
                userid = profile_page.toString();
                if (isMine || extras.getString("profile_uname") == mApp.getUserName()) {
                    new GetUserInfo(this, mApp.mAccessToken, userid).execute();
                } else {
                    new GetUserInfo(this, mApp.mAccessToken, userid).execute();
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

                selectedTheme = prefs.getString("theme_color", "0");
                selectedThemeInt = Integer.parseInt(selectedTheme);
                themePrimaryColors = getResources().getStringArray(R.array.themeColorsPrimary);
                themePrimaryDarkColors = getResources().getStringArray(R.array.themeColorsPrimaryDark);

                profileImageGrid = new ArrayList<InstagramImage>();
                adapter = new ProfileImagesAdapter(
                        this,
                        profileImageGrid, extras.getString("profile_name"),
                        "@" + extras.getString("profile_uname"),
                        extras.getString("profile_picture"),
                        themePrimaryDarkColors[selectedThemeInt],
                        String.valueOf(extras.getInt("profile_posts")),
                        String.valueOf(extras.getInt("profile_followers")),
                        String.valueOf(extras.getInt("profile_following")),
                        "Loading Bio",
                        "Please Wait...");
                userPhotos.setHasFixedSize(true);
                userPhotos.setLayoutManager(glm);
                userPhotos.setAdapter(adapter);

                swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.profile_refresh);
                swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        profileImageGrid.clear();
                        new FetchUserPhotos(userid).execute();
                        new GetUserInfo(ProfileActivity.this, mApp.mAccessToken, userid).execute();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
                new FetchUserPhotos(userid).execute();
            }

	}
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                String result=data.getStringExtra("result");
                new GetUserInfo(this,mApp.mAccessToken,userid).execute();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
	public static void updateProfile(){
		if(data.full_name.isEmpty()){
			data.full_name = " ";
		}
		if(isMine){
		data.outgoing_status = "Edit Your Profile";
		}
        adapter = new ProfileImagesAdapter( 
        		a, 
        		profileImageGrid,
        		data.full_name,
        		"@"+data.username,
        		data.profile_picture, 
        		themePrimaryDarkColors[selectedThemeInt],
        		Integer.toString(data.media),
        		Integer.toString(data.followed_by),
        		Integer.toString(data.follows),
        		data.bio,
        		data.outgoing_status);
    	GridLayoutManager glm = new GridLayoutManager(a,3);
    	glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(position){
                    case ProfileImagesAdapter.TYPE_HEADER:
                        return 3;
                    default:
                        return 1;
                }
            }
        });
        userPhotos.setHasFixedSize(true);
        userPhotos.setLayoutManager(glm);
        userPhotos.setAdapter(adapter);
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
    	
        int id = item.getItemId();
        switch (id) {
        
    	case android.R.id.home:
        ActivityCompat.finishAfterTransition(this);
        return true;
    	case R.id.block_user:
    	Toast.makeText(getApplicationContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show();
    	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	@Override
	protected int getLayoutResource() {
		// TODO Auto-generated method stub
		return R.layout.activity_profile;
	}
    private void toggleInformationView(View view) {
        final View infoContainer = view;

// get the center for the clipping circle
        int cx = (infoContainer.getLeft() + infoContainer.getRight()) / 2;
        int cy = (infoContainer.getTop() + infoContainer.getBottom()) / 2;
        float radius = Math.max(Utils.getScreenWidth(this), Utils.getScreenHeight(this)) * 2.0f;

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
		private Activity mContext;
		private String userid;
	    public GetUserInfo(Activity a, String _accessToken, String id){
			accessToken = _accessToken;
			mContext = a;
			userid = id;
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
	            data.bio = jsonObj.getJSONObject("data").getString("bio");
	            data.profile_picture = jsonObj.getJSONObject("data").getString("profile_picture");
	            data.media = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("media");
	            data.follows = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("follows");
	            data.followed_by = jsonObj.getJSONObject("data").getJSONObject("counts").getInt("followed_by");

	            if(jsonObj2.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("follows")){
		            data.outgoing_status = mContext.getResources().getString(R.string.following_user);;
		            data.unfollow_user = true;
	        	}
	            else if(jsonObj2.getJSONObject("data").getString("outgoing_status").equalsIgnoreCase("requested")){
		            data.outgoing_status = mContext.getResources().getString(R.string.follow_requested);
		            data.unfollow_user = true;
	        	}
	            else {
	            	data.outgoing_status = mContext.getResources().getString(R.string.follow_user);
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
			updateProfile();

	    }
	}
	public static class EditRelationShip extends AsyncTask<Void, String, User> {
		private String accessToken;
	    private String action;
	    private String userid;
		public EditRelationShip(Activity a, String _accessToken, String id){
			accessToken = _accessToken;
			userid = id;
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
	    	private String userid;
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
	    	public FetchUserPhotos (String id){
	    		userid = id;
	    	}
	    	@Override 
	    	protected void onPreExecute(){
	    		profileImageGrid.clear();
	    		return;
	    	}

	    	@Override
	        protected Boolean doInBackground(Void...voids) {
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
            next_url = null;
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

