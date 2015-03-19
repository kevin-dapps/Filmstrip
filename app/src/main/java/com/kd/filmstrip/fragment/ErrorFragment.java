package com.kd.filmstrip.fragment;


import com.kd.filmstrip.R;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ErrorFragment extends Fragment {
	     
    public static Fragment newInstance(Context context) {
        ErrorFragment f = new ErrorFragment();
 
        return f;
    }
     
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	  
	        View rootView = inflater.inflate(R.layout.fragment_error, container, false);

         
	        return rootView;
	}
}

