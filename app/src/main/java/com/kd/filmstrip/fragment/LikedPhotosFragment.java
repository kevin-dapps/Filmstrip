package com.kd.filmstrip.fragment;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.adapters.PopularImagesAdapter;
import com.kd.filmstrip.adapters.PopularImagesAdapter.PopularHolder;
import com.kd.filmstrip.adapters.RecyclerAdapter;
import com.kd.filmstrip.adapters.RecyclerAdapter.PostHolder;
import com.kd.filmstrip.posts.Comment;
import com.kd.filmstrip.posts.InstagramImage;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


public class LikedPhotosFragment extends Fragment {
	public static RecyclerView recyclerView;
	private static Adapter<PostHolder> recyclerAdapter;
	private static Adapter<PopularHolder> recyclerAdapter2;
	
	public static InstagramApp mApp;
	public static ArrayList<InstagramImage> instagramImageList;
	public static Activity mActivity;
	public static String next_url;
	public static View load_more_footer;
	public static final int RESULT_GALLERY = 0;

    
	public LinearLayoutManager llm;
	public GridLayoutManager glm;
	
    public static Fragment newInstance(Context context) {
        LikedPhotosFragment f = new LikedPhotosFragment();
 
        return f;
    }
	     
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	  
	        final View rootView = inflater.inflate(R.layout.fragment_liked, container, false);
	        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
	        
	        
	        recyclerView.setHasFixedSize(true);
	        
	        instagramImageList = new ArrayList<InstagramImage>();
	        
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        	if(prefs.getString("liked_photos_view", "list").equalsIgnoreCase("list")){
	        llm = new LinearLayoutManager(getActivity());
	        glm = new GridLayoutManager(getActivity(),2);
	        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
	        	recyclerView.setLayoutManager(glm);
	        }
	        else {
	        	recyclerView.setLayoutManager(llm);
	        }
	        recyclerAdapter = new RecyclerAdapter(getActivity(),instagramImageList);
	        recyclerView.setAdapter(recyclerAdapter);
        	}
        	else {
        	glm = new GridLayoutManager(getActivity(),3);	
        	recyclerView.setLayoutManager(glm);
        	recyclerAdapter2 = new PopularImagesAdapter(getActivity(), instagramImageList);
        	recyclerView.setAdapter(recyclerAdapter2);
        	}
	        

	        new FetchFeed(getActivity()).execute();

	        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
	        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener(){
	          @Override
	          public void onRefresh()
	            {
	        	  instagramImageList.clear();
	        	  new FetchFeed(getActivity()).execute();
	        	  swipeRefreshLayout.setRefreshing(false);
	            }
	        });

	          
	        return rootView;
	    }
	    public static class LoadMore extends AsyncTask<Void, String, Boolean> {
	    	public static String load_url;
	    	public static Activity activity;
	    	public static ProgressBar loading;
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
	    		loading = (ProgressBar) load_more_footer.findViewById(R.id.loadMoreProgress);
	    		loading.setVisibility(View.VISIBLE);
	    		return;
	    	}
	    	public LoadMore(String url, Activity a){
	    		load_url = url;
	    		activity = a;
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

	        }


	    }
	    public static class FetchFeed extends AsyncTask<Void, String, Boolean> {
	    	public Activity activity;
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
	    	public FetchFeed(Activity a){
	    		activity = a;
	    	}
	    	@Override 
	    	protected void onPreExecute(){
	    		return;
	    	}

	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	                URL url = new URL("https://api.instagram.com/v1/users/self/media/liked?access_token=" + HomeActivity.mApp.mAccessToken);

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
	                for( int i=0; i< data.length(); i++ ) {
	                    // create a new instance
	                    InstagramImage instagramImage = new InstagramImage();

	                    // image
	                    JSONObject image = (JSONObject) data.getJSONObject(i);
	                    
	                    JSONObject images = image.getJSONObject("images");

	                    	                    
	                    
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
	        	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	                	if(prefs.getString("liked_photos_view", "list").equalsIgnoreCase("list")){
	                    recyclerAdapter.notifyDataSetChanged();
	                	}
	                	else {
	                	recyclerAdapter2.notifyDataSetChanged();
	                	}
	                }
	        }


	    }
}



