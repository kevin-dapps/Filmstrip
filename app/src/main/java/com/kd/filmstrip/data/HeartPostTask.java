package com.kd.filmstrip.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.kd.filmstrip.HomeActivity;
import com.kd.filmstrip.posts.InstagramImage;

import android.os.AsyncTask;

public class HeartPostTask extends AsyncTask<String, Void, InstagramImage> {
	private InstagramImage post;
	private int index;
	private boolean like;
	public HeartPostTask(InstagramImage i, boolean liking, int pos){
		post = i;
		like = liking;
		index = pos;
	}
	@Override
	protected InstagramImage doInBackground(String... params) {
		if(like){
			try {
	        String like_url = "https://api.instagram.com/v1/media/"+post.id+"/likes";
			URL url = new URL(like_url);

			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("POST");
			urlConnection.setDoInput(true);
			urlConnection.setDoOutput(true);
			//urlConnection.connect();
			OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
			writer.write("access_token="+HomeActivity.mApp.mAccessToken);
		    writer.flush();
			String response = streamToString(urlConnection.getInputStream());
	        if( response != null ) {
	            if( post.liker_list == null ) post.liker_list = new ArrayList<String>();
	            post.liker_list.add(HomeActivity.mApp.getUserName());
	            post.liker_count++;
	            post.user_has_liked = true;
	        }
	    } 
		catch (Exception ex) {
	    	ex.printStackTrace();
	    }	
		return post;
		}
		
		else {
			try {
		        String like_url = "https://api.instagram.com/v1/media/"+post.id+"/likes?access_token="+HomeActivity.mApp.mAccessToken;
				URL url = new URL(like_url);

				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setRequestMethod("DELETE");
				urlConnection.getResponseCode();
		        if( urlConnection != null ) {
		            post.liker_list.remove(HomeActivity.mApp.getUserName());
		            post.liker_count--;
		            post.user_has_liked = false;
		        }
		    } catch (Exception ex) {
		    	ex.printStackTrace();
		    }	
			return post;
		}
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
}
