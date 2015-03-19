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

import com.kd.filmstrip.ApplicationData;
import com.kd.filmstrip.R;
import com.kd.filmstrip.adapters.PopularImagesAdapter;
import com.kd.filmstrip.posts.Comment;
import com.kd.filmstrip.posts.InstagramImage;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

public class PopularFragment extends Fragment {
    private static Adapter adapter;
	public static ArrayList<InstagramImage> profileImageGrid;
	public static InstagramApp mApp;
	public static String userid;
	public static TextView following_number,follower_number,post_number;
	private int preLast;
	public static String next_url;
	public PopularFragment(){
		setRetainInstance(true);
	}
    public static Fragment newInstance(Context context) {
        PopularFragment f = new PopularFragment();
        
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
			mApp = new InstagramApp(getActivity(), ApplicationData.CLIENT_ID,
				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
	        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
	        // previously invisible view

	        final View myView = rootView.findViewById(R.id.profile_layout);

        	RecyclerView popularPhotos = (RecyclerView) rootView.findViewById(R.id.profileImages);
        	popularPhotos.setPadding(0,0,0,0);
        	GridLayoutManager glm = new GridLayoutManager(getActivity(),3);
        	
            
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

	        profileImageGrid = new ArrayList<InstagramImage>();
	        adapter = new PopularImagesAdapter(getActivity(), profileImageGrid);
	        container.setPadding(container.getPaddingLeft(), 0, container.getPaddingRight(), container.getPaddingBottom());
	        popularPhotos.setHasFixedSize(true);
	        popularPhotos.setLayoutManager(glm);
	        popularPhotos.setAdapter(adapter);
	        popularPhotos.setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrolled(RecyclerView view, int horizontalScroll, int verticalScroll) {
    	            // Make your calculation stuff here. You have all your
    	            // needed info from the parameters of this function.

    	            // Sample calculation to determine if the last 
    	            // item is fully visible.
    	           if(view.getHeight() == verticalScroll) {
    	                new FetchUserPhotos().execute();
    	              }
    	           }
			});
	        new FetchUserPhotos().execute();
	        return rootView;

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
	    		return;
	    	}

	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	            	URL url = new URL("https://api.instagram.com/v1/media/popular?access_token="+mApp.mAccessToken);

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
}
