package com.kd.filmstrip;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import br.com.dina.oauth.instagram.InstagramApp;
import br.com.dina.oauth.instagram.InstagramApp.OAuthAuthenticationListener;

import com.kd.filmstrip.adapters.DrawerAdapter;
import com.kd.filmstrip.adapters.DrawerAdapter.ClickListener;
import com.kd.filmstrip.fragment.FeedFragment2;
import com.kd.filmstrip.fragment.SearchFragment;
import com.kd.filmstrip.nav.NavItems;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;


public class HomeActivity extends BaseActivity implements ClickListener {
	private static View mDrawerHeader;
    public static DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    // slide menu items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    

    private DrawerAdapter adapter;
	public static InstagramApp mApp;
	private static InstagramImage lastPhoto;
	private String[] fragments;
	// Theme Info
	public static int selectedThemeInt;
	public static String selectedTheme;
	public static String[] themePrimaryColors;
	public static String[] themePrimaryDarkColors;
	
	public static String background_url;
	
	public static SearchFragment searchFragment;
	
@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarIcon(R.drawable.ic_menu);
		mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
		mApp.setListener(listener);
        // Set User Profile Picture
		lastPhoto = new InstagramImage();
		
	    searchFragment = new SearchFragment();
        fragments = new String[]{
                "com.kd.filmstrip.fragment.FeedFragment2",
                "com.kd.filmstrip.fragment.NewsFragment",
        		"com.kd.filmstrip.fragment.PopularFragment",
        		"com.kd.filmstrip.fragment.NearbyFragment",
        		"com.kd.filmstrip.fragment.LikedPhotosFragment",
        		"com.kd.filmstrip.fragment.SettingsFragment",
        		"com.kd.filmstrip.fragment.AboutFragment",
        		"com.kd.filmstrip.fragment.LogoutFragment"};

        // load slide menu items
        navMenuTitles = getResources().getStringArray(R.array.drawer_items);
 
        // nav drawer icons from resources
        navMenuIcons = getResources().obtainTypedArray(R.array.drawer_icons);
 
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        selectedTheme = prefs.getString("theme_color", "0");
        selectedThemeInt = Integer.parseInt(selectedTheme);
        themePrimaryColors = getResources().getStringArray(R.array.themeColorsPrimary);
        themePrimaryDarkColors = getResources().getStringArray(R.array.themeColorsPrimaryDark);

        
        mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor(themePrimaryDarkColors[selectedThemeInt]));
        getToolbar().setBackgroundColor(Color.parseColor(themePrimaryColors[selectedThemeInt]));
        
        mDrawerList = (RecyclerView) findViewById(R.id.drawer_recyclerview);
        mDrawerList.setHasFixedSize(true);
		new GetBackgroundUrl().execute();
		/*
		mDrawerProfilePic = (ImageView) mDrawerHeader.findViewById(R.id.drawer_profile_image);
       
        Picasso.with(this).load(mApp.getProfilePic()).transform(new PicassoRound()).into(mDrawerProfilePic);


   
         //image2.setImageDrawable(roundedImage);
         // Setup DrawerView TextViews
         TextView drawer_fname = (TextView) mDrawerHeader.findViewById(R.id.drawer_header_fullname);
         drawer_fname.setText(mApp.getName());
         TextView drawer_uname = (TextView) mDrawerHeader.findViewById(R.id.drawer_header_uname);
         drawer_uname.setText(mApp.getUserName());
         */



       // mDrawerLinearList = (LinearLayout) mDrawerLayout.findViewById(R.id.navdrawer_items_list);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        new ArrayList<NavItems>();
 
        // adding nav drawer items to array
        // Home

         
 
        // Recycle the typed array
        navMenuIcons.recycle();
 
        // setting the nav drawer list adapter
        adapter = new DrawerAdapter(getApplicationContext(), navMenuTitles, mApp.getName(),mApp.getUserName(), mApp.getProfilePic(), background_url);
        adapter.setClickListener(this);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        mDrawerList.setAdapter(adapter);
        /*
        for (int i = 0; i < adapter.getCount(); i++) {
        	  final int pos = i;
        	  final View item = adapter.getView(i, null, mDrawerLinearList);
        	  item.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
	            	mChangeContentFragment = true;
	            	Boolean shouldChangeFragment = true;
	            	switch (pos){
	            	case 1: 
	            	Toast.makeText(getApplicationContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show();
	            	shouldChangeFragment = false;
	            	break;
	            	
	            	case 3:
	            	Toast.makeText(getApplicationContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show();
	            	shouldChangeFragment = false;
	            	break;
	            	}
	            	if(shouldChangeFragment){
	            		

	                    if(navDrawerItems.get(pos).isShowIcon()){
		                Drawable myIcon = getResources().getDrawable(navDrawerItems.get(pos).getIcon());
		                myIcon.setColorFilter(Color.parseColor(themePrimaryColors[selectedThemeInt]), PorterDuff.Mode.SRC_ATOP);
		                ((TextView) item.findViewById(R.id.drawer_item_title)).setTextColor(Color.parseColor(themePrimaryColors[selectedThemeInt]));
	                    ((ImageView) item.findViewById(R.id.drawer_item_icon)).setImageDrawable(myIcon);
	                    }
	                    mDrawerLayout.setDrawerListener( new DrawerLayout.SimpleDrawerListener(){
                         @Override
                         public void onDrawerClosed(View drawerView){
                                 super.onDrawerClosed(drawerView);
                                 if (mChangeContentFragment) {
                                 FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                 tx.replace(R.id.fragment_container, Fragment.instantiate(HomeActivity.this, fragments[pos]));
                                 tx.commit();
                                 if(pos != 1){
                                	 RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams();
                                	 layoutParams.addRule(RelativeLayout.BELOW, R.id.getToolbar());
                                 }
                                 }
                            	 mChangeContentFragment = false;
                         }
	               	 });
	            	}
					getToolbar().animate().translationY(0).setDuration(5).setInterpolator(new DecelerateInterpolator());
	            	mDrawerLayout.closeDrawer(Gravity.START);
	            	}
			});
        	  mDrawerLinearList.addView(item);
        	} 
        */
        // Set the adapter for the list view
        /*
        mDrawerList.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int pos,long id){
            	
            	mChangeContentFragment = true;
            	adapter.setSelectedItem(pos);
            	Boolean shouldChangeFragment = true;
            	switch (pos){
            	case 2: 
            	Toast.makeText(getApplicationContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show();
            	shouldChangeFragment = false;
            	break;
            	
            	case 4:
            	Toast.makeText(getApplicationContext(), "Feature Coming Soon", Toast.LENGTH_SHORT).show();
            	shouldChangeFragment = false;
            	break;
            	}
            	if(shouldChangeFragment){
            	 mDrawerLayout.setDrawerListener( new DrawerLayout.SimpleDrawerListener(){
                            @Override
                            public void onDrawerClosed(View drawerView){
                                    super.onDrawerClosed(drawerView);
                                    if (mChangeContentFragment) {
                                    FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                    tx.replace(R.id.fragment_container, Fragment.instantiate(HomeActivity.this, fragments[pos]));
                                    tx.commit();
                                    if(pos != 1){
                                   	 RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams();
                                   	 layoutParams.addRule(RelativeLayout.BELOW, R.id.getToolbar());
                                    }
                                    }
                               	 	mChangeContentFragment = false;
                               	 	
                            }
                    });
            	}
            	mDrawerList.setItemChecked(pos, true);
                mDrawerLayout.closeDrawer(Gravity.START);

            }
    });
	*/
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDrawerSlide(View arg0, float arg1) {
				getToolbar().animate().translationY(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
				
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
				getToolbar().animate().translationY(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
				
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
				getToolbar().animate().translationY(0).setDuration(0).setInterpolator(new DecelerateInterpolator());
				
			}
	});
    if (savedInstanceState == null) {
    FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    tx.replace(R.id.fragment_container,Fragment.instantiate(HomeActivity.this, fragments[0]));
    tx.commit();
    if(Build.VERSION.SDK_INT < 17){
      	 RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams();
      	 layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_container);
   	}
    }
    /*
    else {
    Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_internet), Toast.LENGTH_LONG).show();
    FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
    tx.replace(R.id.fragment_container,Fragment.instantiate(HomeActivity.this, "com.kd.filmstrip.fragment.ErrorFragment"));
    tx.commit();
    }

   mDrawerProfilePic.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		    Bundle bundle = new Bundle();
		    bundle.putBoolean("profile_mine", true);
		    bundle.putString("profile_page", "self");
		    bundle.putString("profile_picture", mApp.getProfilePic());
		    bundle.putString("profile_name", mApp.getName());
		    bundle.putString("profile_uname", mApp.getUserName());
		    bundle.putInt("profile_posts", mApp.getMediaNumber());
		    bundle.putInt("profile_follows", mApp.getFollowedBy());
		    bundle.putInt("profile_following", mApp.getFollows());
		    
		    ProfileFragment mFrag = new ProfileFragment();
		    mFrag.setArguments(bundle);
		    tx.replace(R.id.fragment_container, mFrag);
		    tx.commit();
            mDrawerLayout.closeDrawer(Gravity.START);
		}
	});*/
}
	public void refreshBackground(){
        adapter = new DrawerAdapter(getApplicationContext(), navMenuTitles, mApp.getName(),mApp.getUserName(), mApp.getProfilePic(), background_url);
        mDrawerList.setLayoutManager(new LinearLayoutManager(this));
        adapter.setClickListener(this);
        mDrawerList.setAdapter(adapter);
	}
	@SuppressWarnings("unused")
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}
	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

		@Override
		public void onSuccess() {


		}
		@Override
		public void onFail(String error) {
			Toast.makeText(HomeActivity.this, error, Toast.LENGTH_SHORT).show();
		}
	};
	public static void updateTheme(int i){
		selectedThemeInt = i;
        getToolbar().setBackgroundColor(Color.parseColor(themePrimaryColors[i]));
        mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor(themePrimaryDarkColors[i]));
	}
	@Override
	public void onBackPressed() {

	    int count = getFragmentManager().getBackStackEntryCount();

	    if (count == 0) {
	        super.onBackPressed();
	        //additional code
	    } else {
	        getFragmentManager().popBackStack();
	    }

	}
    @Override 
    protected int getLayoutResource() {
        return R.layout.activity_home;
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    switch (requestCode) {
	    case FeedFragment2.RESULT_GALLERY :
	        if (data != null) {
	        	try {
	        	String PACKAGE_NAME = "com.instagram.android";
	        	getPackageManager().getApplicationInfo(PACKAGE_NAME, 0);
	            Uri imageUri = data.getData();
	            Bitmap  mBitmap = null;
	            try {
					 mBitmap = Media.getBitmap(this.getContentResolver(), imageUri);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            if(mBitmap != null)
	            mBitmap = createSquaredBitmap(mBitmap);
	            imageUri = getImageUri(getBaseContext(), mBitmap);
	            String type = "image/*";
	            
	            Intent share = new Intent(Intent.ACTION_SEND);

	            // Set the MIME type
	            share.setType(type);
	            share.setPackage(PACKAGE_NAME);

	            // Add the URI and the caption to the Intent.
	            share.putExtra(Intent.EXTRA_STREAM, imageUri);
	            share.putExtra(Intent.EXTRA_TEXT, "Uploaded via Filmstrip");

	            // Broadcast the Intent.
	            startActivity(Intent.createChooser(share, "Share to"));
	        	}
	        	  catch (PackageManager.NameNotFoundException e)
	        	{
	        	   Toast.makeText(getBaseContext(), "Instagram not installed.", Toast.LENGTH_SHORT);
	        	}

	        }
	        break;
	    default:
	        break;
	    }
	}
	public Uri getImageUri(Context inContext, Bitmap inImage) {
		  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		  inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
		  String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
		  return Uri.parse(path);
		}
    private static Bitmap createSquaredBitmap(Bitmap srcBmp) {
        int dim = Math.max(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Config.ARGB_8888);

        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(srcBmp, (dim - srcBmp.getWidth()) / 2, (dim - srcBmp.getHeight()) / 2, null);

        return dstBmp;
    }
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Get the SearchView and set the searchable configuration

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        // Assumes current activity is the searchable activity
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener( ) {
            @Override
            public boolean   onQueryTextChange( String newText ) {
                Bundle bundle = new Bundle();
    		    bundle.putString("query", newText);
    		    bundle.putString("access_token", mApp.mAccessToken);
  

    		    SearchFragment.setUIArguments(HomeActivity.this,bundle);

                return true;
            }

            @Override
            public boolean   onQueryTextSubmit(String query) {

                return true;
                //textView.setText(query);
            }
        });
        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),
                new OnActionExpandListener()
                {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item)
                    {
                        // Do something when collapsed
                    	getFragmentManager().popBackStack();
                        //getToolbar().setBackgroundColor(Color.parseColor(themePrimaryColors[selectedThemeInt]));
                        //mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor(themePrimaryDarkColors[selectedThemeInt]));
                        return true; // Return true to collapse action view
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item)
                    {
                    	// Do something when expanded
                    	
                    	//getToolbar().setBackgroundColor(Color.parseColor("#ffffff"));
                        //mDrawerLayout.setStatusBarBackgroundColor(Color.parseColor("#bdbdbd"));
                        return true; // Return true to expand action view
                    }
                });
 
        return true;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.action_search:
                FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        		tx.replace(R.id.fragment_container, searchFragment);
        		tx.addToBackStack("Search");
        		tx.commit();
        }

        return super.onOptionsItemSelected(item);
    }
    public class GetBackgroundUrl extends AsyncTask<Void, String, Boolean> {
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
    	@Override 
    	protected void onPreExecute(){
 
    		return;
    	}
    	@Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL("https://api.instagram.com/v1/users/"+mApp.getId()+"/media/recent/?access_token=" + mApp.mAccessToken+"&count=1");
                Log.i("DownloadLastPost", "Opening URL " + url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoInput(true);
                urlConnection.connect();
                String response = streamToString(urlConnection.getInputStream());
                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
               
                // parse the activity feed
                JSONArray data = jsonObj.getJSONArray("data");
 
                // get image URLs and commentary
                for( int i=0; i< data.length(); i++ ) {
                    // create a new instance
                    // image
                    JSONObject image = (JSONObject) data.getJSONObject(i);

                    JSONObject images = image.getJSONObject("images");
                    JSONObject lowResolutionImage = images.getJSONObject("low_resolution");

                    lastPhoto.low_resolution = lowResolutionImage.getString("url");
               
                }
                return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
			return false;
            
        }
    	@Override
        protected void onPostExecute(Boolean result) {
    		if(lastPhoto.low_resolution != null){
    		background_url = lastPhoto.low_resolution;
    		getApplicationContext();
			SharedPreferences preferences = getSharedPreferences("localimages", Context.MODE_PRIVATE);
    		Editor editor = preferences.edit();
    		editor.putString("url", lastPhoto.low_resolution);
    		editor.commit();
    		}
    		else {
    		getApplicationContext();
			SharedPreferences preferences= getSharedPreferences("localimages", Context.MODE_PRIVATE);
    		String url = preferences.getString("url", "R.color.colorPrimaryDark");
    		background_url = url;
    		}
    		refreshBackground();
    	}


    }
    public class DownloadLastPost extends AsyncTask<Void, String, Boolean> {
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
	    	@Override 
	    	protected void onPreExecute(){
	            ImageView image1 = (ImageView) mDrawerHeader.findViewById(R.id.drawer_header_image);
	            image1.setImageBitmap(BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.placeholder_image) );
	            image1.setColorFilter(Color.argb(90, 0, 0, 0));
	    		return;
	    	}
	    	@Override
	        protected Boolean doInBackground(Void... voids) {
	            try {
	                URL url = new URL("https://api.instagram.com/v1/users/"+mApp.getId()+"/media/recent/?access_token=" + mApp.mAccessToken+"&count=1");

	                Log.i("DownloadLastPost", "Opening URL " + url.toString());
	                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	                urlConnection.setRequestMethod("GET");
	                urlConnection.setDoInput(true);
	                urlConnection.connect();
	                String response = streamToString(urlConnection.getInputStream());
	                JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
	               
	                // parse the activity feed
	                JSONArray data = jsonObj.getJSONArray("data");
	 
	                // get image URLs and commentary
	                for( int i=0; i< data.length(); i++ ) {
	                    // create a new instance

	                    // image
	                    JSONObject image = (JSONObject) data.getJSONObject(i);

	                    JSONObject images = image.getJSONObject("images");
	                    //Log.i("JSONObjectReturn", images.toString());
	                    JSONObject thumbnailImage = images.getJSONObject("thumbnail");
	                    JSONObject lowResolutionImage = images.getJSONObject("low_resolution");
	                    JSONObject standardResolutionImage = images.getJSONObject("standard_resolution");
	                    lastPhoto.id = image.getString("id");
	                    lastPhoto.permalink = image.getString("link");

	                    lastPhoto.user_has_liked = image.getBoolean("user_has_liked");

	                    // permalinks
	                    lastPhoto.thumbnail = thumbnailImage.getString("url");
	                    lastPhoto.low_resolution = lowResolutionImage.getString("url");
	                    lastPhoto.standard_resolution = standardResolutionImage.getString("url");

	                    // user
	                    JSONObject user = image.getJSONObject("user");
	                    lastPhoto.username = user.getString("username");
	                    lastPhoto.user_id = user.getString("id");
	                    lastPhoto.profile_picture = user.getString("profile_picture");
	                    lastPhoto.full_name = user.getString("full_name");

	                    // date taken_at
	                    Long dateLong = image.getLong("created_time");
	                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
	                    lastPhoto.taken_at = formatter.format(new Date(dateLong * 1000L));
	                    
	                    lastPhoto.taken_time = dateLong * 1000L;
	                    // caption

	                    try {
	                        JSONObject caption = image.getJSONObject("caption");
	                        if( caption != null ) {
	                        	lastPhoto.caption = caption.getString("text");
	                        }
	                    } catch (JSONException e) {}

	                    // likers
	                    try {
	                    	lastPhoto.liker_count = image.getJSONObject("likes").getInt("count");
	                        JSONArray likes = image.getJSONObject("likes").getJSONArray("data");
	                        if( likes != null ) {
	                            ArrayList<String> likerList = new ArrayList<String>();
	                            if( likes.length() > 0 ) {
	                                for( int l=0; l < likes.length(); l++ ) {
	                                    JSONObject like = likes.getJSONObject(l);
	                                    likerList.add(like.getString("username"));
	                                }
	                                lastPhoto.liker_list = likerList;
	                            }
	                        }
	                    } catch( JSONException j ) {}
	                    
	                }
	                return true;
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
				return false;
	            
	        }
	    	@Override
	        protected void onPostExecute(Boolean result) {
	            ImageView image1 = (ImageView) mDrawerHeader.findViewById(R.id.drawer_header_image);
	            Picasso.with(getBaseContext()).load(lastPhoto.low_resolution).placeholder(R.drawable.placeholder_image).into(image1);
	            image1.setColorFilter(Color.argb(90, 0, 0, 0));
	    	}


	    }
	@Override
	public void itemClicked(View v, int pos) {
    	Boolean shouldChangeFragment = true;

    	switch (pos){
    	case 2: 
    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
    	shouldChangeFragment = false;
    	break;
    	
    	case 4:
    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
    	shouldChangeFragment = false;
    	break;
    	
    	case 5:
    	Toast.makeText(getApplicationContext(), getResources().getString(R.string.in_development), Toast.LENGTH_SHORT).show();
    	}
    	if(shouldChangeFragment){
        FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        tx.replace(R.id.fragment_container, Fragment.instantiate(HomeActivity.this, fragments[pos-1]));
        tx.commit();
        if(pos != 1 || Build.VERSION.SDK_INT < 17){
       	 RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) findViewById(R.id.fragment_container).getLayoutParams();
       	 layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_container);
        }
    	}
        mDrawerLayout.closeDrawer(Gravity.START);

	}
     
}


