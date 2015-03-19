/*
 * Copyright (C) 2014 Antonio Leiva Gordillo.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kd.filmstrip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import br.com.dina.oauth.instagram.InstagramApp;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.kd.filmstrip.adapters.CommentsAdapter;
import com.kd.filmstrip.adapters.RecyclerAdapter.PostHolder;
import com.kd.filmstrip.data.HeartPostTask;
import com.kd.filmstrip.fragment.LikedPhotosFragment;
import com.kd.filmstrip.fragment.PopularFragment;
import com.kd.filmstrip.fragment.ProfileFragment;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;



public class DetailActivityV2 extends BaseActivity implements OnClickListener {
	
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);
	// Transition Ints 
	// The height of your fully expanded header view (same than in the xml layout)
	int headerHeight;
	// The height of your fully collapsed header view. Actually the Toolbar height (56dp)
	int minHeaderHeight;
	// The left margin of the Toolbar title (according to specs, 72dp)
	int toolbarTitleLeftMargin;
	// Added after edit
	int minHeaderTranslation;
	private ProgressDialog simpleWaitDialog;
	int initialScroll = 0;
    public static final String EXTRA_IMAGE = "DetailActivityV2:image";
    public static InstagramApp mApp;
    public static AsyncTask<Bitmap, Void, Palette> palette;
    public static CommentsAdapter commentsAdapter;
    
    public static LinearLayout detailShare, detailComments, detailLike;
    public static RelativeLayout detailUserInfo;
    public static ObservableScrollView detailScrollView;
    public static VideoView detailVideoV2;
    public static ImageView detailImageV2,detailProfilePic, detailLikeStatus;
    public static TextView detailFullName, detailUserName, detailDate, detailCaption,detailLikeCounts,detailCommentCounts;
    public static InstagramImage detailImageLoaderv2;

    
    // Double Tap to like 
    public static View detailvBgLike;
    public static ImageView detailivLike;

    //Theme Info
    public static int selectedThemeInt;
    public static String selectedTheme;
    public static String[] themePrimaryColors;
    public static String[] themePrimaryDarkColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mApp = new InstagramApp(getBaseContext(), ApplicationData.CLIENT_ID,
				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
        // Get Theme Info
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        selectedTheme = prefs.getString("theme_color", "0");
        selectedThemeInt = Integer.parseInt(selectedTheme);
        themePrimaryColors = getResources().getStringArray(R.array.themeColorsPrimary);
        themePrimaryDarkColors = getResources().getStringArray(R.array.themeColorsPrimaryDark);

		headerHeight = getResources().getDimensionPixelSize(R.dimen.action_bar_max_height);
		minHeaderHeight  = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
		toolbarTitleLeftMargin  = getResources().getDimensionPixelSize(R.dimen.toolbar_left_margin);

        detailImageLoaderv2 = (InstagramImage) getIntent().getSerializableExtra("EXTRA_DATA");

        detailVideoV2 = (VideoView) findViewById(R.id.post_video);
        detailImageV2 = (ImageView) findViewById(R.id.post_image);
        ViewCompat.setTransitionName(detailImageV2, EXTRA_IMAGE);
        ViewCompat.setTransitionName(detailVideoV2, EXTRA_IMAGE);
        
        detailScrollView = (ObservableScrollView) findViewById(R.id.detail_scrollview);
        detailUserInfo = (RelativeLayout) findViewById(R.id.detail_userinfo);
        detailShare = (LinearLayout) findViewById(R.id.detail_share);
        detailComments = (LinearLayout) findViewById(R.id.detail_comments);
        detailLike = (LinearLayout) findViewById(R.id.detail_like);
        
        detailFullName = (TextView) findViewById(R.id.detail_fullname);
        detailUserName = (TextView) findViewById(R.id.detail_username);
        detailDate = (TextView) findViewById(R.id.detail_date);
        detailCaption = (TextView) findViewById(R.id.detail_postcaption);
        
        detailLikeStatus = (ImageView) findViewById(R.id.detail_likeStatus);
        detailLikeCounts = (TextView) findViewById(R.id.detail_likeCounts);
        detailCommentCounts = (TextView) findViewById(R.id.detail_commentCounts);
        		
        detailProfilePic = (ImageView) findViewById(R.id.detail_user_pic);      

        detailvBgLike = (View) findViewById(R.id.vBgLike);
        detailivLike = (ImageView) findViewById(R.id.ivLike);
        
        Picasso.with(this).load(detailImageLoaderv2.profile_picture).transform(new PicassoRound()).into(detailProfilePic);


        detailFullName.setText(detailImageLoaderv2.full_name);
        detailUserName.setText(detailImageLoaderv2.username+", ");
        detailDate.setText(detailImageLoaderv2.taken_at);
        
        if(detailImageLoaderv2.user_has_liked){
            Drawable myIcon = getResources().getDrawable(R.drawable.ic_like);
            myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            detailLikeStatus.setImageDrawable(myIcon);
        }
        
        if(detailImageLoaderv2.caption != null){
        detailCaption.setText(detailImageLoaderv2.caption);
        }
        else {
        detailCaption.setVisibility(View.GONE);
        }
        detailLikeCounts.setText(String.valueOf(detailImageLoaderv2.liker_count));
        detailCommentCounts.setText(String.valueOf(detailImageLoaderv2.comment_count));
        
        detailImageV2.setOnTouchListener(new OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(DetailActivityV2.this, new GestureDetector.SimpleOnGestureListener() {
            	boolean isTouchFirst = true;
            	@Override
                public boolean onDoubleTap(MotionEvent eV) {
        				animatePhotoLike();
        				if(!detailImageLoaderv2.user_has_liked){
        				HeartPostTask liker = new HeartPostTask(detailImageLoaderv2, true, 0);
        				liker.execute();
        				try {
        					detailImageLoaderv2 = liker.get();
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (ExecutionException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        		        if(detailImageLoaderv2.user_has_liked){
        		            Drawable myIcon = getResources().getDrawable(R.drawable.ic_like);
        		            myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        		            detailLikeStatus.setImageDrawable(myIcon);
        		        }
        				detailLikeCounts.setText(String.valueOf(detailImageLoaderv2.liker_count));
        				}
        			return super.onDoubleTap(eV);
                }
                 // implement here other callback methods like onFling, onScroll as necessary
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
    		        if (isTouchFirst) {
    		            isTouchFirst = false;

    		            getToolbar().animate().translationY(-getToolbar().getBottom()).alpha(0).setDuration(320).setInterpolator(new DecelerateInterpolator());

    		        } else {
    					isTouchFirst = true;
    					getToolbar().animate().translationY(0).alpha(1).setDuration(400).setInterpolator(new DecelerateInterpolator());

    		            //mSystemUiHider.show();
    		            Log.d("Here isTouch is false", ">");  

    		        }
					return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
        detailShare.setOnClickListener(this);
        detailLike.setOnClickListener(this);
        detailProfilePic.setOnClickListener(this);
        detailScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

			@Override
			public void onScrollChanged() {
				
				int scroll = detailScrollView.getScrollY();
				saveInitialScroll(scroll);
				/*
				if(detailUserInfo != null){
					if (getAlpha(-scroll - getToolbar().getHeight()) >= 1.0f) {
						detailUserInfo.setTranslationY(-detailUserInfo.getHeight() + getToolbar().getHeight());
					}
					else {
						detailUserInfo.setTranslationY(-scroll - detailUserInfo.getHeight());
					}
				}
				*/
				findViewById(R.id.detail_image_container).setTranslationY(Math.max(0, detailScrollView.getScrollY() + minHeaderTranslation));
				
                float offset = 1 - Math.max(
                        (float) (-minHeaderTranslation - detailScrollView.getScrollY()) / -minHeaderTranslation, 0f);

	
                    // Now that we have this ratio, we only have to apply translations, scales,
                    // alpha, etc. to the header views

                    // For instance, this will move the toolbar title & subtitle on the X axis 
                    // from its original position when the ListView will be completely scrolled
                    // down, to the Toolbar title position when it will be scrolled up.
                    //detailFullName.setTranslationX(toolbarTitleLeftMargin * offset);
                    //detailUserName.setTranslationX(toolbarTitleLeftMargin * offset);
					//detailProfilePic.animate().alpha(1.0f * offset).translationX(-toolbarTitleLeftMargin * offset);
			}
        });
        commentsAdapter = new CommentsAdapter(this, detailImageLoaderv2.comment_list);
    	if(detailImageLoaderv2.comment_count > 8){
    	  Button load_more_button = new Button(this);
    	  load_more_button.setText(getResources().getString(R.string.load_old_comments));
    	  load_more_button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.coming_soon), Toast.LENGTH_SHORT).show();
				
			}
		});
    	  detailComments.addView(load_more_button, 0);
    	}
        for (int i = 0; i < commentsAdapter.getCount(); i++) {

      	  View item = commentsAdapter.getView(i, null, detailComments);
 
      	  detailComments.addView(item);
      	} 
    }
    private float getAlpha(int scrollY) {
        double slope = 1.00 / (double) (Math.abs(initialScroll) - getToolbar().getHeight());
        return (float) (-slope * scrollY) + 1;
    }
    private void saveInitialScroll(int scroll) {
    	
		if (scroll <= initialScroll) {
           initialScroll = scroll;
       }
   }
    //Handle all view clicks
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.detail_like:
			if(detailImageLoaderv2.user_has_liked){
			HeartPostTask liker = new HeartPostTask(detailImageLoaderv2, false, 0);
			liker.execute();
			try {
					detailImageLoaderv2 = liker.get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		    if(!detailImageLoaderv2.user_has_liked){
	        Drawable myIcon = getResources().getDrawable(R.drawable.ic_like);
	        myIcon.setColorFilter(0, PorterDuff.Mode.SRC_ATOP);
	        detailLikeStatus.setImageDrawable(myIcon);
		    }
			detailLikeCounts.setText(String.valueOf(detailImageLoaderv2.liker_count));	
			}
			else {
			animatePhotoLike();
			HeartPostTask liker = new HeartPostTask(detailImageLoaderv2, true, 0);
			liker.execute();
			try {
				detailImageLoaderv2 = liker.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        if(detailImageLoaderv2.user_has_liked){
	            Drawable myIcon = getResources().getDrawable(R.drawable.ic_like);
	            myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
	            detailLikeStatus.setImageDrawable(myIcon);
	        }
			detailLikeCounts.setText(String.valueOf(detailImageLoaderv2.liker_count));
			
			}
		break;
		case R.id.detail_likeCounts:
			Intent i = new Intent(getBaseContext(), LikersActivity.class);   
			i.putExtra("media_id", detailImageLoaderv2.id);
			i.putExtra("title", "Likers " + "(" + detailImageLoaderv2.liker_count + ")");			
			startActivity(i);
		break;
		case R.id.detail_share:
			Intent shareIntent = new Intent();
            Uri uri = null;

		    // Create share intent as described above
            if(!detailImageLoaderv2.video) {
                Drawable mDrawable = detailImageV2.getDrawable();
                Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                String path = Images.Media.insertImage(getContentResolver(), mBitmap, detailImageLoaderv2.full_name, detailImageLoaderv2.caption);
                uri = Uri.parse(path);
            }
            else {

            }
		    shareIntent.setAction(Intent.ACTION_SEND);
		    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		    shareIntent.putExtra(Intent.EXTRA_TEXT, detailImageLoaderv2.caption + " Posted by @" + detailImageLoaderv2.username);
		    startActivity(Intent.createChooser(shareIntent, "Share To"));
		break;
		case R.id.detail_user_pic:
			viewProfile(this, detailImageLoaderv2);
		    
		}
		
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
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
        
    	case R.id.save_photo:
    	    
    		try {
    			new ImageDownloader().execute(detailImageLoaderv2.standard_resolution);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT);
			}
        return true;
        
    	case R.id.copy_link:
    	  if(detailImageLoaderv2.permalink != null){
  		  ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
  		  ClipData clip = ClipData.newPlainText("Post Link", detailImageLoaderv2.permalink);
  		  clipboard.setPrimaryClip(clip); 
  	      Toast.makeText(this, "Link Copied", Toast.LENGTH_SHORT).show();
    	  }
    	  else {
    	  Toast.makeText(this, "Cannot copy link", Toast.LENGTH_SHORT);
    	  }
  	    return true;
        }
        return super.onOptionsItemSelected(item);
    }
	@Override
	public void onBackPressed() {
        super.onBackPressed();
		ActivityCompat.finishAfterTransition(this);


	}
    @Override
    public void onResume(){
    	super.onResume();
        loadImage();
    }


    public void viewProfile(Activity activity, InstagramImage data){
	    Intent profile = new Intent(activity, ProfileActivity.class);
	    profile.putExtra("profile_page", data.user_id);
	    profile.putExtra("profile_picture", data.profile_picture);
	    profile.putExtra("profile_name", data.full_name);
	    profile.putExtra("profile_uname", data.username);
	    profile.putExtra("profile_posts", 0);
	    profile.putExtra("profile_follows", 0);
	    profile.putExtra("profile_following", 0);
            // start the new activity
        activity.startActivity(profile);
    }

    public void postComment(String comment, InstagramImage image) {
        List<NameValuePair> postParams = new ArrayList<NameValuePair>();
        postParams.add(new BasicNameValuePair("text", comment));
        postParams.add(new BasicNameValuePair("access_token", mApp.mAccessToken));
        String url = "https://api.instagram.com/v1/media/"+image.id+"/comments/";
        DefaultHttpClient httpClient = new DefaultHttpClient();
/*
        JSONObject jsonResponse = Utils.doRestfulPut(httpClient,
                url,
                postParams,
                this);
                
        if( jsonResponse != null ) {
            image.comment_list.add(new Comment(mApp.getUserName(),comment, mApp.getProfilePic()));
            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this,
                    "Comment failed", Toast.LENGTH_SHORT).show();
        }*/
        
    }
    @Override protected int getLayoutResource() {
        return R.layout.activity_detail_v2;
    }


    private void applyPalette(Palette palette) {
        if(palette.getVibrantSwatch() != null) {
            int paletteColor = palette.getVibrantColor(Color.parseColor(themePrimaryDarkColors[selectedThemeInt]));
            detailUserInfo.setBackgroundColor(paletteColor);
        }
        else {
            detailUserInfo.setBackgroundColor(Color.parseColor(themePrimaryDarkColors[selectedThemeInt]));
        }

    }

    /**
     * Load the item's image into our {@link ImageView}.
     */
    private void loadImage() {

        Picasso.with(this)
                .load(getIntent().getStringExtra(EXTRA_IMAGE))
                .noFade()
                .placeholder(android.R.color.background_light)
                .into(detailImageV2, new Callback() {

                    @Override
                    public void onError() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onSuccess() {
                        Bitmap bitmap = ((BitmapDrawable) detailImageV2.getDrawable()).getBitmap();
                        Palette.generateAsync(bitmap,
                                new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        Palette.Swatch vibrant =
                                                palette.getVibrantSwatch();
                                        if (vibrant != null) {
                                            // If we have a vibrant color
                                            // update the title TextView
                                            detailUserInfo.setBackgroundColor(vibrant.getRgb());
                                        }
                                    }
                                });

                        Picasso.with(getApplicationContext())
                                .load(detailImageLoaderv2.standard_resolution)
                                .noFade()
                                .placeholder(detailImageV2.getDrawable())
                                .into(detailImageV2);
                        LinearLayout detailSlideUp = (LinearLayout) findViewById(R.id.detail_slide_up);
                        detailSlideUp.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.support.v7.appcompat.R.anim.abc_slide_in_bottom));
                        if (detailImageLoaderv2.video) {
                            PlayVideo();
                        }
                    }

                });
    }
    // Playing Video
    private void PlayVideo()
    {
        try
        {
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(detailVideoV2);
            detailVideoV2.setVisibility(View.VISIBLE);
            Uri video = Uri.parse(detailImageLoaderv2.video_standard_res);
            detailVideoV2.setMediaController(mediaController);
            detailVideoV2.setVideoURI(video);
            detailVideoV2.requestFocus();
            detailVideoV2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    detailImageV2.setVisibility(View.GONE);
                    detailVideoV2.start();
                }
            });


        }
        catch(Exception e)
        {
            System.out.println("Video Play Error :"+e.toString());
        }

    }
    // Like Animation
    private static void animatePhotoLike() {
            detailvBgLike.setVisibility(View.VISIBLE);
            detailivLike.setVisibility(View.VISIBLE);

            detailvBgLike.setScaleY(0.1f);
            detailvBgLike.setScaleX(0.1f);
            detailvBgLike.setAlpha(1f);
            detailivLike.setScaleY(0.1f);
            detailivLike.setScaleX(0.1f);

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(detailvBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(200);
            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(detailvBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(200);
            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(detailvBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(200);
            bgAlphaAnim.setStartDelay(150);
            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(detailivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(300);
            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(detailivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(300);
            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(detailivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(300);
            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(detailivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(300);
            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

            animatorSet.start();
    }
    public static void launch(Activity activity, View transitionView, InstagramImage image, String url) {
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                        activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, DetailActivityV2.class);
        intent.putExtra("EXTRA_DATA", image);
        intent.putExtra(EXTRA_IMAGE, url);
        if(Build.VERSION.SDK_INT >= 16){
        activity.startActivity(intent, options.toBundle());
        }
        else {
        activity.startActivity(intent);
        }
    }
    private class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
    	boolean completed = false;
		@Override
		protected Bitmap doInBackground(String... param) {
			// TODO Auto-generated method stub
			Bitmap bp = downloadBitmap(param[0]);
			if(bp != null)
			saveImage(bp);
			
			return null;
			
			
		}

		@Override
		protected void onPreExecute() {
			Log.i("Async-Example", "onPreExecute Called");
			simpleWaitDialog = ProgressDialog.show(DetailActivityV2.this,
					"Please Wait", "Downloading Image");

		}

		@Override
		protected void onPostExecute(Bitmap result) {
			Log.i("Async-Example", "onPostExecute Called");
			simpleWaitDialog.dismiss();
			if(completed){
				Toast.makeText(DetailActivityV2.this, getResources().getString(R.string.download_error), Toast.LENGTH_SHORT).show();
			}
			else {
				Toast.makeText(DetailActivityV2.this, getResources().getString(R.string.download_success), Toast.LENGTH_SHORT).show();
			}

		}
		private void saveImage(Bitmap finalBitmap) {
		    String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
		    File myDir = new File(root + "/Filmstrip/");
		    myDir.mkdirs();
		    Random generator = new Random();
		    int n = 10000;
		    n = generator.nextInt(n);
		    String fname = "image_" + n + ".jpg";
		    File file = new File(myDir, fname);
		    if (file.exists())
		        file.delete();
		    try {
		        FileOutputStream out = new FileOutputStream(file);
		        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		        out.flush();
		        out.close();
		    }
		    catch (Exception e) {
		        e.printStackTrace();
		    }


		    // Tell the media scanner about the new file so that it is
		    // immediately available to the user.
		    MediaScannerConnection.scanFile(DetailActivityV2.this, new String[] { file.toString() }, null,
		            new MediaScannerConnection.OnScanCompletedListener() {
		                public void onScanCompleted(String path, Uri uri) {
		                	completed = true;
		                    Log.i("ExternalStorage", "Scanned " + path + ":");
		                    Log.i("ExternalStorage", "-> uri=" + uri);
		                }
		    });

		}
		private Bitmap downloadBitmap(String url) {
			// initilize the default HTTP client object
			final DefaultHttpClient client = new DefaultHttpClient();

			//forming a HttoGet request 
			final HttpGet getRequest = new HttpGet(url);
			try {

				HttpResponse response = client.execute(getRequest);

				//check 200 OK for success
				final int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != HttpStatus.SC_OK) {
					Log.w("ImageDownloader", "Error " + statusCode + 
							" while retrieving bitmap from " + url);
					return null;

				}

				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream inputStream = null;
					try {
						// getting contents from the stream 
						inputStream = entity.getContent();

						// decoding stream data back into image Bitmap that android understands
						final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

						return bitmap;
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}
						entity.consumeContent();
					}
				}
			} catch (Exception e) {
				// You Could provide a more explicit error message for IOException
				getRequest.abort();
				Log.e("ImageDownloader", "Something went wrong while" +
						" retrieving bitmap from " + url + e.toString());
			} 

			return null;
		}
    }
}
