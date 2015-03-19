package com.kd.filmstrip.adapters;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kd.filmstrip.BaseActivity;
import com.kd.filmstrip.DetailActivityV2;
import com.kd.filmstrip.ProfileActivity;
import com.kd.filmstrip.R;
import com.kd.filmstrip.Utils;
import com.kd.filmstrip.customviews.VideoView;
import com.kd.filmstrip.data.HeartPostTask;
import com.kd.filmstrip.image.PicassoRound;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PostHolder> {
	
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    private final static Map<RecyclerView.ViewHolder, AnimatorSet> likeAnimations = new HashMap<>();
    
    private int lastAnimatedPosition = -1;
    private PostHolder holder;
    
    
	private static ArrayList<InstagramImage> instagramPostList;
	private static InstagramImage instagramPost;
	private static Activity activity;

  public RecyclerAdapter(Activity a,ArrayList<InstagramImage> arrayList) {
	  	activity = a;
	    instagramPostList = arrayList;
  }
  public static InstagramImage getItem(int position){
	  return instagramPostList.get(position);
  }
  public void addItem(int position, InstagramImage data) {
	  	instagramPostList.add(position, data);
	    notifyItemInserted(position);
  }

  public void removeItem(int position) {
		instagramPostList.remove(position);
	    notifyItemRemoved(position);
  }
  @Override
  public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
    PostHolder postHolder = new PostHolder(view);
    return postHolder;
  }
  
  @Override
  public void onBindViewHolder(PostHolder pH, int position) {
	holder = pH;
	setAnimation(holder.itemView, position);
    instagramPost = instagramPostList.get(position);
    if (likeAnimations.containsKey(holder)) {
        likeAnimations.get(holder).cancel();
    }
    updateHeartButton(holder, false);
    resetLikeAnimationState(holder);
    holder.username.setText(Html.fromHtml("<b>" + instagramPost.username + "</b> "));

    holder.caption.setText(instagramPost.caption);

    holder.timestamp.setText(instagramPost.taken_at);
    holder.likeCounts.setText(instagramPost.liker_count+" likes");
    holder.commentCounts.setText(instagramPost.comment_count+" comments");

    Picasso.with(holder.profile.getContext()).load(instagramPost.profile_picture).placeholder(R.drawable.circle_white).transform(new PicassoRound()).into(holder.profile);
    Picasso.with(holder.image.getContext()).load(instagramPost.standard_resolution).placeholder(android.R.color.background_light).into(holder.image);

    if(instagramPost.hasLocation){
    	//holder.locationContainer.setVisibility(View.VISIBLE);
    	//holder.locationName.setText(instagramPost.location_name.toString());
    }
    /*
    if(instagramPost.video){
    	//holder.instagramPost.setVisibility(View.GONE);
    	holder.video.setVideoURI(Uri.parse(instagramPost.video_standard_res));

        holder.video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(holder.video.getContext(), "on prepared", Toast.LENGTH_SHORT).show();
                holder.image.setVisibility(View.GONE);
                mp.setLooping(true);
                holder.video.requestFocus();
                holder.video.start();
            }
        });

    	holder.videoIcon.setVisibility(View.VISIBLE);
    }
    else {
    	holder.video.setVisibility(View.GONE);
    	holder.videoIcon.setVisibility(View.INVISIBLE);
    }
    */
    if(instagramPost.user_has_liked){
        Drawable myIcon = holder.likeBtn.getContext().getResources().getDrawable(R.drawable.ic_like);
        myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        holder.likeBtn.setImageDrawable(myIcon);
    }

    
  }

  @Override
  public int getItemCount() {
    return instagramPostList.size();
  }
  // Start Up Animation 
  public void updateItems(boolean animated) {
      getItemCount();
      notifyDataSetChanged();
  }
  
  /**
   * Here is the key method to apply the animation
   */
  private void setAnimation(View viewToAnimate, int position)
  {
      // If the bound view wasn't previously displayed on screen, it's animated
      if (position > lastAnimatedPosition)
      {
    	  viewToAnimate.setTranslationY(Utils.getScreenHeight(viewToAnimate.getContext()));
    	  viewToAnimate.animate().translationY(0).setInterpolator(new DecelerateInterpolator(3.f)).setDuration(1000).start();
          //Animation animation = AnimationUtils.loadAnimation(activity, android.support.v7.appcompat.R.anim.abc_slide_in_bottom);
          //viewToAnimate.startAnimation(animation);
          lastAnimatedPosition = position;
      }
  }
  // Reset Like Animation 
  private static void resetLikeAnimationState(PostHolder holder) {
      likeAnimations.remove(holder);
      holder.vBgLike.setVisibility(View.GONE);
      holder.ivLike.setVisibility(View.GONE);
  }
  // Like Button Animation 
  private static void updateHeartButton(final PostHolder holder, boolean animated) {
      if (animated) {
          if (!likeAnimations.containsKey(holder)) {
              AnimatorSet animatorSet = new AnimatorSet();
              likeAnimations.put(holder, animatorSet);

              ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(holder.likeBtn, "rotation", 0f, 360f);
              rotationAnim.setDuration(300);
              rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

              ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(holder.likeBtn, "scaleX", 0.2f, 1f);
              bounceAnimX.setDuration(300);
              bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

              ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(holder.likeBtn, "scaleY", 0.2f, 1f);
              bounceAnimY.setDuration(300);
              bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
              bounceAnimY.addListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationStart(Animator animation) {
                      holder.likeBtn.setImageResource(R.drawable.ic_heart_red);
                  }
              });

              animatorSet.play(rotationAnim);
              animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);

              animatorSet.addListener(new AnimatorListenerAdapter() {
                  @Override
                  public void onAnimationEnd(Animator animation) {
                      resetLikeAnimationState(holder);
                  }
              });

              animatorSet.start();
          }
      } else {
          if (getItem(holder.getPosition()).user_has_liked) {
              Drawable myIcon = holder.likeBtn.getContext().getResources().getDrawable(R.drawable.ic_like);
              myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
              holder.likeBtn.setImageDrawable(myIcon);
          } else {
              holder.likeBtn.setImageResource(R.drawable.ic_like);
          }
      }
  }
  // Like Animation
  private static void animatePhotoLike(final PostHolder holder) {
      if (!likeAnimations.containsKey(holder)) {
          holder.vBgLike.setVisibility(View.VISIBLE);
          holder.ivLike.setVisibility(View.VISIBLE);

          holder.vBgLike.setScaleY(0.1f);
          holder.vBgLike.setScaleX(0.1f);
          holder.vBgLike.setAlpha(1f);
          holder.ivLike.setScaleY(0.1f);
          holder.ivLike.setScaleX(0.1f);

          AnimatorSet animatorSet = new AnimatorSet();
          likeAnimations.put(holder, animatorSet);

          ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
          bgScaleYAnim.setDuration(200);
          bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
          ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
          bgScaleXAnim.setDuration(200);
          bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
          ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
          bgAlphaAnim.setDuration(200);
          bgAlphaAnim.setStartDelay(150);
          bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

          ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
          imgScaleUpYAnim.setDuration(300);
          imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
          ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
          imgScaleUpXAnim.setDuration(300);
          imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

          ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
          imgScaleDownYAnim.setDuration(300);
          imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
          ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
          imgScaleDownXAnim.setDuration(300);
          imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

          animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
          animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);


          animatorSet.start();
      }
  }


// Custom ViewHolder 
public static class PostHolder extends RecyclerView.ViewHolder implements OnClickListener{
		public int position;
	    public TextView username,caption,locationName,timestamp, likeCounts,commentCounts;
	    public ImageView profile,image, ivLike;
	    public VideoView video;
	    public ImageButton videoIcon,likeBtn, commentBtn, shareBtn;
	    public LinearLayout top,locationContainer;
	    public View vBgLike;

	    public PostHolder(View view) {
	      super(view);
          profile = (ImageView) view.findViewById(R.id.post_profile);
          image = (ImageView) view.findViewById(R.id.post_image);
          //video = (VideoView) view.findViewById(R.id.post_video);
          username = (TextView)view.findViewById(R.id.post_username);
          caption = (TextView)view.findViewById(R.id.post_caption);
          timestamp = (TextView)view.findViewById(R.id.post_timestamp);
          likeBtn = (ImageButton) view.findViewById(R.id.likeButton);
          commentBtn = (ImageButton) view.findViewById(R.id.commentButton);
          shareBtn = (ImageButton) view.findViewById(R.id.shareButton);
          videoIcon = (ImageButton) view.findViewById(R.id.is_video);
          //locationContainer = (LinearLayout) view.findViewById(R.id.location_container);
          //locationName = (TextView) view.findViewById(R.id.location_name);
          // Counts
          likeCounts = (TextView) view.findViewById(R.id.likeCounts);
          commentCounts = (TextView) view.findViewById(R.id.commentCounts);
          vBgLike = (View) view.findViewById(R.id.vBgLike);
          ivLike = (ImageView) view.findViewById(R.id.ivLike);
          top = (LinearLayout)view.findViewById(R.id.post_top);
          
          
          username.setOnClickListener(this);
          image.setOnClickListener(this);
          profile.setOnClickListener(this);
          likeCounts.setOnClickListener(this);
          
          likeBtn.setOnClickListener(this);
          commentBtn.setOnClickListener(this);
          shareBtn.setOnClickListener(this);
   
          /*
          video.setOnTouchListener(new OnTouchListener() {
              private GestureDetector gestureDetector = new GestureDetector(video.getContext(), new GestureDetector.SimpleOnGestureListener() {
                  @Override
                  public boolean onDoubleTap(MotionEvent e) {
          			if(!instagramPost.user_has_liked){ 			
          				animatePhotoLike(PostHolder.this);	
          				FeedFragment2.like(instagramPost);
          		        Drawable myIcon = likeBtn.getContext().getResources().getDrawable(R.drawable.ic_like);
          		        myIcon.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
          		        likeBtn.setImageDrawable(myIcon);
          		        //holder.likeCounts.setText(instagramPost.liker_count);
          			}
                      return super.onDoubleTap(e);
                  }
                   // implement here other callback methods like onFling, onScroll as necessary
                  @Override
                  public boolean onSingleTapConfirmed(MotionEvent e) {
                  	DetailActivity.launch(activity, image, instagramPost,instagramPost.standard_resolution);
                      return true;
                  }
              });

              @Override
              public boolean onTouch(View v, MotionEvent event) {
                  gestureDetector.onTouchEvent(event);
                  return true;
              }
          	});*/

	    }
	    // Override and set on click listeners 
	    @Override 
	    public void onClick(View v){
	    	Intent profileIntent;
	    	ActivityOptionsCompat options;
	    	InstagramImage postData = instagramPostList.get(getPosition());
	    	switch(v.getId()) {
                case R.id.post_profile:
                    profileIntent = new Intent(activity, ProfileActivity.class);
                    profileIntent.putExtra("profile_page", postData.user_id);
                    profileIntent.putExtra("profile_picture", postData.profile_picture);
                    profileIntent.putExtra("profile_name", postData.full_name);
                    profileIntent.putExtra("profile_uname", postData.username);
                    profileIntent.putExtra("profile_posts", 0);
                    profileIntent.putExtra("profile_follows", 0);
                    profileIntent.putExtra("profile_following", 0);

                    options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(activity, profile, "robot");
                    // start the new activity
                    if (Build.VERSION.SDK_INT >= 16) {
                        activity.startActivity(profileIntent, options.toBundle());
                    } else {
                        activity.startActivity(profileIntent);
                    }

                    break;
                case R.id.post_username:
                    profileIntent = new Intent(activity, ProfileActivity.class);
                    profileIntent.putExtra("profile_page", postData.user_id);
                    profileIntent.putExtra("profile_picture", postData.profile_picture);
                    profileIntent.putExtra("profile_name", postData.full_name);
                    profileIntent.putExtra("profile_uname", postData.username);
                    profileIntent.putExtra("profile_posts", 0);
                    profileIntent.putExtra("profile_follows", 0);
                    profileIntent.putExtra("profile_following", 0);

                    options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(activity, profile, "robot");
                    // start the new activity
                    if (Build.VERSION.SDK_INT >= 16) {
                        activity.startActivity(profileIntent, options.toBundle());
                    } else {
                        activity.startActivity(profileIntent);
                    }

                    break;
                case R.id.post_image:
                    BaseActivity.getToolbar().animate().translationY(-BaseActivity.getToolbar().getBottom()).alpha(0).setDuration(320).setInterpolator(new DecelerateInterpolator());
                    DetailActivityV2.launch(activity, image, postData, postData.standard_resolution);
                    break;
                case R.id.likeButton:
                    if (!postData.user_has_liked) {
                        HeartPostTask liker = new HeartPostTask(postData, true, 0);
                        liker.execute();
                        try {
                            postData = liker.get();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        instagramPostList.set(getPosition(),postData);
                        instagramPostList.notify();
                    }
                    else {
                        HeartPostTask liker = new HeartPostTask(postData, false, 0);
                        liker.execute();
                        try {
                            postData = liker.get();
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        instagramPostList.set(getPosition(),postData);
                    }
                    break;
                case R.id.shareButton:
                    Intent shareIntent = new Intent();

                    // Create share intent as described above

                    Drawable mDrawable = image.getDrawable();
                    Bitmap mBitmap = ((BitmapDrawable)mDrawable).getBitmap();

                    String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), mBitmap, postData.full_name, postData.caption);

                    Uri uri = Uri.parse(path);
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, postData.caption);
                    shareIntent.setType("image/*");
                    activity.startActivity(Intent.createChooser(shareIntent, "Share To"));
            }
	    }

	    
}


}
