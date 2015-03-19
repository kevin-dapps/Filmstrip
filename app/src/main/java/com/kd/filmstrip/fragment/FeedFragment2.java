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

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import br.com.dina.oauth.instagram.InstagramApp;

import com.kd.filmstrip.ApplicationData;
import com.kd.filmstrip.BaseActivity;
import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.Utils;
import com.kd.filmstrip.adapters.RecyclerAdapter;
import com.kd.filmstrip.adapters.RecyclerAdapter.PostHolder;
import com.kd.filmstrip.adapters.RecyclerAdapterV2;
import com.kd.filmstrip.posts.Comment;
import com.kd.filmstrip.posts.InstagramImage;


public class FeedFragment2 extends Fragment {
	public static RecyclerView recyclerView;
	private static Adapter recyclerAdapter;
	
	public static InstagramApp mApp;
	public static ArrayList<InstagramImage> instagramImageList;
	public static Activity mActivity;
	public static String next_url;
	public static View load_more_footer;
	public static final int RESULT_GALLERY = 0;
	
    private static final int ANIM_DURATION_TOOLBAR = 500;
    private static final int ANIM_DURATION_FAB = 600;
    
	public LinearLayoutManager llm;
	public GridLayoutManager glm;
	
	private static View rootView;
	private static ProgressBar loading;
	private static SwipeRefreshLayout swipeRefreshLayout;
    public static Fragment newInstance(Context context) {
        FeedFragment2 f = new FeedFragment2();
 
        return f;
    }

	     
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	    	
			mApp = new InstagramApp(getActivity(), ApplicationData.CLIENT_ID,
					ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
			mActivity = getActivity();
	        rootView = inflater.inflate(R.layout.fragment_feed2, container, false);
	        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
	        
	        if(Build.VERSION.SDK_INT >= 17){
	        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
	        layoutParams.removeRule(RelativeLayout.BELOW);
	        }
	        else {
	        recyclerView.setPadding(0,0,0,0);
	        }

	        int actionbarSize = Utils.dpToPx(56);
	        BaseActivity.getToolbar().setTranslationY(-actionbarSize);
	        if(BaseActivity.getToolbarLogo() != null)
	        BaseActivity.getToolbarLogo().setTranslationY(-actionbarSize);
	        //Outline
	        if(Build.VERSION.SDK_INT >= 21){
	        	ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
	                @Override
	                public void getOutline(View view, Outline outline) {
	                    // Or read size directly from the view's width/height
	                    int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
	                    outline.setOval(0, 0, size, size);
	                }
	            };

	        
	        }

	        beginAnimations();

	        recyclerView.setHasFixedSize(true);
	         
	        llm = new LinearLayoutManager(getActivity());

	        if(getActivity().getResources().getBoolean(R.bool.isTablet)){
                glm = new GridLayoutManager(getActivity(),3);
                recyclerView.setLayoutManager(glm);
            }
	        else if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
                glm = new GridLayoutManager(getActivity(),2);
	        	recyclerView.setLayoutManager(glm);
	        }
	        else {
		    recyclerView.setLayoutManager(llm);
	        }

	        instagramImageList = new ArrayList<InstagramImage>();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if(prefs.getBoolean("post_layout", false) == false) {
                recyclerAdapter = new RecyclerAdapter(getActivity(), instagramImageList);
            }
            else {
                recyclerAdapter = new RecyclerAdapterV2(getActivity(), instagramImageList);
            }

	        recyclerView.setAdapter(recyclerAdapter);
	        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
	        	int oldScroll = recyclerView.getScrollY();
	        	@Override
	        	public void onScrolled (RecyclerView rView, int dx, int dy){
	        		if(recyclerView.getScrollY() > oldScroll){
	        	    	BaseActivity.getToolbar().animate()
	                    .translationY(-BaseActivity.getToolbar().getHeight())
	                    .setDuration(150)
	                    .start();

	        		}
	        		else {
	        	    	BaseActivity.getToolbar().animate()
	                    .translationY(0)
	                    .setDuration(ANIM_DURATION_TOOLBAR)
	                    .start();

	        		}
	        		oldScroll = recyclerView.getScrollY();
	        	}
			});
	        new FetchFeed().execute();

	        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
	        TypedValue typed_value = new TypedValue();
	        getActivity().getTheme().resolveAttribute(android.support.v7.appcompat.R.attr.actionBarSize, typed_value, true);
	        swipeRefreshLayout.setProgressViewOffset(false, 0, getResources().getDimensionPixelSize(typed_value.resourceId));
	        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener(){
	          @Override
	          public void onRefresh()
	            {
	        	  instagramImageList.clear();
	        	  recyclerAdapter.notifyDataSetChanged();
	        	  new FetchFeed().execute();
	        	  swipeRefreshLayout.setRefreshing(false);
	            }
	        });
	        return rootView;
	    }
	    public void beginAnimations(){
	    	BaseActivity.getToolbar().animate()
            .translationY(0)
            .setDuration(ANIM_DURATION_TOOLBAR)
            .setStartDelay(300);
	    	if(BaseActivity.getToolbarLogo() != null){
	    	BaseActivity.getToolbarLogo().animate()
            .translationY(0)
            .setDuration(ANIM_DURATION_TOOLBAR)
            .setStartDelay(400);
	    	}

	    }
	    @Override
	    public boolean onContextItemSelected(MenuItem item) {
	      switch (item.getItemId()){
	    	  case 0:
	    	       Toast.makeText(getActivity(), "Feature Coming Soon!", Toast.LENGTH_SHORT).show();
	    	       return true;
	    	  case 1:
	    		  Intent galleryIntent = new Intent(
	    		                      Intent.ACTION_PICK,
	    		                      android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	    		  getActivity().startActivityForResult(galleryIntent , RESULT_GALLERY );
	    		  return true;
	      }
	      return true;
	    }
	    
	    // Fetching all the feed data
	    public static class FetchFeed extends AsyncTask<Void, String, Boolean> {

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
	    		return;
	    	}

	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	                URL url = new URL("https://api.instagram.com/v1/users/self/feed?access_token=" + HomeActivity.mApp.mAccessToken);

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
	                    // image
	                    JSONObject image = (JSONObject) data.getJSONObject(i);
	                    
	                    JSONObject images = image.getJSONObject("images");

	                    InstagramImage instagramImage = new InstagramImage();	                    
	                    
	                    JSONObject thumbnailImage = images.getJSONObject("thumbnail");
	                    JSONObject lowResolutionImage = images.getJSONObject("low_resolution");
	                    JSONObject standardResolutionImage = images.getJSONObject("standard_resolution");
	                    instagramImage.id = image.getString("id");
	                    instagramImage.permalink = image.getString("link");

	                    instagramImage.user_has_liked = image.getBoolean("user_has_liked");
	                    



	                    // permalinks


	                    // Images 
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
                        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy HH:mm");
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

	                    instagramImageList.add(instagramImage);
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
	                	loading = (ProgressBar) rootView.findViewById(R.id.loadMoreProgress);
	    	    		loading.setVisibility(View.GONE);
	    	    		swipeRefreshLayout.setVisibility(View.VISIBLE);
	                    recyclerAdapter.notifyDataSetChanged();
	                }
	        }


	    }
	    public static void like(final InstagramImage image) {
	new Thread(){
		@Override
		public void run(){

		try {
	        String like_url = "https://api.instagram.com/v1/media/"+image.id+"/likes";
			URL url = new URL(like_url);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			//urlConnection.connect();
			OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
			writer.write("access_token="+mApp.mAccessToken);
		    writer.flush();
			String response = streamToString(urlConnection.getInputStream());
	        if( response != null ) {
	            if( image.liker_list == null ) image.liker_list = new ArrayList<String>();
	            image.liker_list.add(mApp.getUserName());
	            image.liker_count++;
	            image.user_has_liked = true;
	            //gridViewAdapter.notifyDataSetChanged();
	            //Toast.makeText(mActivity, "Post Liked", Toast.LENGTH_SHORT);
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
            //Toast.makeText(mActivity, "Post Liking Failed", Toast.LENGTH_SHORT);
	    }	
		}
	}.start();
}
public static void unlike(final InstagramImage image) {
	new Thread() {
		@Override
		public void run() {
		try {
	        String like_url = "https://api.instagram.com/v1/media/"+image.id+"/likes?access_token="+mApp.mAccessToken;
			URL url = new URL(like_url);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("DELETE");
			urlConnection.getResponseCode();
	        if( urlConnection != null ) {
	            image.liker_list.remove(mApp.getUserName());
	            image.liker_count--;
	            image.user_has_liked = false;
	        }
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }	
		}
	}.start();
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
}



