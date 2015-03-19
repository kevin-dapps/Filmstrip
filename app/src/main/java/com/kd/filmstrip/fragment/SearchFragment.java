package com.kd.filmstrip.fragment;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.kd.filmstrip.R;
import com.kd.filmstrip.adapters.UserAdapter;
import com.kd.filmstrip.posts.UserList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SearchFragment extends Fragment {
    public static ArrayList<UserList> searchResults = new ArrayList<UserList>();
    public static UserAdapter adapter;
    public static ListView listResults;
    public static TextView searchText;
    public static String query, access_token;
    public static Fragment newInstance(Context context) {
        SearchFragment f = new SearchFragment();
 
        return f;
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	  
	        final View rootView = inflater.inflate(R.layout.search_list, container, false);
	        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) container.getLayoutParams();
	        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_container);
	        Bundle extras = getArguments();
	        if(extras != null){
	        query = extras.getString("query");
	        access_token = extras.getString("access_token");
	        }
	        searchText = (TextView) rootView.findViewById(R.id.result_info);
	        listResults = (ListView) rootView.findViewById(R.id.result_list);
        	adapter = new UserAdapter(getActivity(), searchResults);
        	listResults.setAdapter(adapter);
        	if(query != null){
        	new SearchText(getActivity(),access_token,query).execute();
        	}
	          
	        return rootView;
	}
    public static void setUIArguments(final Activity activity, final Bundle args) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
    	        query = args.getString("query");
    	        access_token = args.getString("access_token");
            	if(query != null){
                	new SearchText(activity,access_token,query).execute();
                }
            	else {
            		searchResults.clear();
    				searchText.setVisibility(View.VISIBLE);
    				listResults.setVisibility(View.GONE);
            	}
            }
        });
    }
	public static class SearchText extends AsyncTask<Void, String, Boolean> {
		public static String accessToken, searchTerm;
		public static UserList data;
		public static String next_url;
		public static Activity activity;
		public SearchText(Activity a, String _accessToken, String _searchTerm){
			accessToken = _accessToken;
			searchTerm = _searchTerm;
			activity = a;
		}
		@Override 
		protected void onPreExecute(){
			if(!searchResults.isEmpty()){
          	searchResults.clear();
			}
			return;
		}

		@Override
	    protected Boolean doInBackground(Void... voids) {
	        try {
	        	URL url = new URL("https://api.instagram.com/v1/users/search?q="+searchTerm+"&access_token="+accessToken);

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
	            JSONArray resultList = jsonObj.getJSONArray("data");

	            // get image URLs and commentary
	            for( int i=0; i< resultList.length(); i++ ) {
	                // image
	            JSONObject result = (JSONObject) resultList.getJSONObject(i);
	            data = new UserList();
	            data.user_id = result.getString("id");
	            data.username = result.getString("username");
	            data.fullname = result.getString("full_name");
	            data.profile_pic = result.getString("profile_picture");
	            searchResults.add(data);
	            }
	            return true;
	        }
	        catch (Exception ex) {
	            ex.printStackTrace();
	        }
			return false;
	        
	    }
		@Override
	    protected void onPostExecute(Boolean result) {
			if(result){
				adapter.notifyDataSetChanged();
				searchText.setVisibility(View.GONE);
				listResults.setVisibility(View.VISIBLE);
			}
		}
	    private String streamToString(InputStream is) throws IOException {
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
}
