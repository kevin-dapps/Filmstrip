package com.kd.filmstrip.fragment;

import br.com.dina.oauth.instagram.InstagramApp;

import com.kd.filmstrip.ApplicationData;
import com.kd.filmstrip.R;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class LogoutFragment extends Fragment {
	InstagramApp mApp;
    public static Fragment newInstance(Context context) {
        LogoutFragment f = new LogoutFragment();
 
        return f;
    }

	     
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	  
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		mApp = new InstagramApp(getActivity(), ApplicationData.CLIENT_ID,
				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
		builder.setMessage("Disconnect from Instagram?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								mApp.resetAccessToken();
								getActivity().finish();
							}
						})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog, int id) {
								dialog.cancel();
							    FragmentTransaction tx = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
							    tx.replace(R.id.fragment_container,Fragment.instantiate(getActivity(), "com.kd.filmstrip.fragment.FeedFragment"), "fragment_selected");
							    tx.commit();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
		return null;
	}
}
