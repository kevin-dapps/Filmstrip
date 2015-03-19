package com.kd.filmstrip.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kd.filmstrip.DetailActivityV2;
import com.kd.filmstrip.R;
import com.kd.filmstrip.posts.InstagramImage;
import com.squareup.picasso.Picasso;

public class PopularImagesAdapter extends RecyclerView.Adapter<PopularImagesAdapter.PopularHolder> {
	 	private static Activity activity;
	 	private LayoutInflater inflater;
	    private static ArrayList<InstagramImage> profileImageGrid;
	    public PopularImagesAdapter(){}
	    public InstagramImage image;
	    
	public PopularImagesAdapter (Activity a, ArrayList<InstagramImage> i){
		activity = a;
		setInflater((LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		profileImageGrid = i;
	}
 

	@Override
	public int getItemCount() {
		return profileImageGrid.size();
	}
	@Override
	public void onBindViewHolder(PopularHolder holder, int i) {
      image = profileImageGrid.get(i);        
      Picasso.with(holder.itemView.getContext()).load(image.thumbnail).placeholder(android.R.color.background_light).into(holder.gridThumbnail);
		
	}
	@Override
	public PopularHolder onCreateViewHolder(ViewGroup parent, int viewType) {
	    View view = getInflater().inflate(R.layout.profile_grid_item, parent, false);
	    PopularHolder postHolder = new PopularHolder(view);
	    return postHolder;
	}
	public LayoutInflater getInflater() {
		return inflater;
	}

	public void setInflater(LayoutInflater inflater) {
		this.inflater = inflater;
	}
	public static class PopularHolder extends ViewHolder implements View.OnClickListener {
		public ImageView gridThumbnail;
        public View gridSelector;
        public PopularHolder(View vi) {
			super(vi);
			gridThumbnail = (ImageView) vi.findViewById(R.id.profile_gridImage);
            gridSelector = (View) itemView.findViewById(R.id.profile_selector);
            gridSelector.setOnClickListener(this);
		}
		@Override
		public void onClick(View v) {
			InstagramImage gridItem = profileImageGrid.get(getPosition());
			DetailActivityV2.launch(activity, gridThumbnail, gridItem, gridItem.thumbnail);	
		}
	}
  }