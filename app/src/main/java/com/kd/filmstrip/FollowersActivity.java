package com.kd.filmstrip;

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

import com.kd.filmstrip.adapters.UserAdapter;
import com.kd.filmstrip.posts.UserList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class FollowersActivity extends BaseActivity {
	static UserAdapter userListAdapter;
	private static ListView userListView;
	static ArrayList<UserList> arrayUserList;
	static String userListID;
	private int preLast;
	public static String next_url;
	public static View load_more_footer;
	
	// Theme Info
	public static int selectedThemeInt;
	public static String selectedTheme;
	public static String[] themePrimaryColors;
	public static String[] themePrimaryDarkColors;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        selectedTheme = prefs.getString("theme_color", "0");
        selectedThemeInt = Integer.parseInt(selectedTheme);
        themePrimaryColors = getResources().getStringArray(R.array.themeColorsPrimary);
        themePrimaryDarkColors = getResources().getStringArray(R.array.themeColorsPrimaryDark);
		if(Build.VERSION.SDK_INT >= 21){
        window.setStatusBarColor(Color.parseColor(themePrimaryDarkColors[selectedThemeInt]));
		}
		getToolbar().setBackgroundColor(Color.parseColor(themePrimaryColors[selectedThemeInt]));
        getToolbar().setTitle(getIntent().getExtras().getString("title"));
        load_more_footer =  getLayoutInflater().inflate(R.layout.load_more_footer, null, false);
		userListID = getIntent().getExtras().getString("user_id");
		arrayUserList = new ArrayList<UserList>();
		userListAdapter = new UserAdapter(this, arrayUserList);
		userListView = (ListView) findViewById(R.id.users_list);
		userListView.addFooterView(load_more_footer);
		userListView.setAdapter(userListAdapter);
        userListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
	            // Make your calculation stuff here. You have all your
	            // needed info from the parameters of this function.

	            // Sample calculation to determine if the last 
	            // item is fully visible.
	             final int lastItem = firstVisibleItem + visibleItemCount;
	           if(lastItem == totalItemCount) {
	              if(preLast!=lastItem){ //to avoid multiple calls for last item
	                preLast = lastItem;
	                if(next_url != null)
	                new LoadMore(next_url, FollowersActivity.this).execute();
	              }
	           }
				
			}
		});
        new FetchUsers().execute();
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
        }
        return super.onOptionsItemSelected(item);
    }
	@Override
	protected int getLayoutResource() {
		return R.layout.user_list;
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
	                next_url = null;
	                next_url = more_data.getString("next_url");
	                }
	                catch(JSONException j){}
                    // parse the activity feed
                    JSONArray data = jsonObj.getJSONArray("data");
     
                    // get image URLs and commentary
                    for( int i=0; i< data.length(); i++ ) {
                        // create a new instance
                        UserList userListData = new UserList();


                        JSONObject image = (JSONObject) data.getJSONObject(i);
                        
                        String userListUname = image.getString("username");
                        String userListFname = image.getString("full_name");
                        String userProfilePicture = image.getString("profile_picture");
                        String userId = image.getString("id");
                        userListData.username = userListUname;
                        userListData.fullname = userListFname;
                        userListData.profile_pic = userProfilePicture;
                        userListData.user_id = userId;
                    

                        arrayUserList.add(userListData);
                    }
                    return true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    			return false;
            
        }
    	@Override
        protected void onPostExecute(Boolean result) {
    			loading.setVisibility(View.GONE);
                if(result) {
                    userListAdapter.notifyDataSetChanged();
                }
        }


    }
	
	// Get List of Data
    public static class FetchUsers extends AsyncTask<Void, String, Boolean> {
    	public static ProgressBar loading;
    	@Override 
    	protected void onPreExecute(){
    		loading = (ProgressBar) load_more_footer.findViewById(R.id.loadMoreProgress);
    		loading.setVisibility(View.VISIBLE);
    		return;
    	}

    	@Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("https://api.instagram.com/v1/users/"+userListID+"/followed-by?access_token=" + HomeActivity.mApp.mAccessToken);

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
                    UserList userListData = new UserList();


                    JSONObject image = (JSONObject) data.getJSONObject(i);
                    
                    String userListUname = image.getString("username");
                    String userListFname = image.getString("full_name");
                    String userProfilePicture = image.getString("profile_picture");
                    String userId = image.getString("id");
                    userListData.username = userListUname;
                    userListData.fullname = userListFname;
                    userListData.profile_pic = userProfilePicture;
                    userListData.user_id = userId;
                

                    arrayUserList.add(userListData);
                }
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
			return false;
            
        }
    	@Override
        protected void onPostExecute(Boolean result) {
    		load_more_footer.findViewById(R.id.loadMoreProgress).setVisibility(View.GONE);
                if(result) {
                    userListAdapter.notifyDataSetChanged();
                }
        }
    }
}
