package com.kd.filmstrip.fragment;

import com.kd.filmstrip.R;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class AboutFragment extends Fragment {
	     
    public static Fragment newInstance(Context context) {
        AboutFragment f = new AboutFragment();
 
        return f;
    }
     
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	  
	        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

         
	        return rootView;
	}
}
