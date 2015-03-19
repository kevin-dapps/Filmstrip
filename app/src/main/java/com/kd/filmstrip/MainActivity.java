package com.kd.filmstrip;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import br.com.dina.oauth.instagram.InstagramApp;
import br.com.dina.oauth.instagram.InstagramApp.OAuthAuthenticationListener;


public class MainActivity extends Activity {

	private InstagramApp mApp;
	private OAuthDialogListener mListener;
	private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mApp = new InstagramApp(this, ApplicationData.CLIENT_ID,
				ApplicationData.CLIENT_SECRET, ApplicationData.CALLBACK_URL);
		mListener = new OAuthDialogListener() {
			
			@Override
			public void onComplete(String code) {
				mApp.getAccessToken(code);
			}

			@Override
			public void onError(String error) {
			}
		};

		if (mApp.hasAccessToken()) {
			mApp.fetchUserName();
			Intent homeIntent = new Intent(this, HomeActivity.class);
			startActivity(homeIntent);
			finish();
		}
		else {
		mApp.setListener(listener);
		setUpWebView();
		}

	}
	
	private void setUpWebView() {
		WebView mWebView = (WebView) findViewById(R.id.filmstrip_loginwebview);
		mWebView.setVerticalScrollBarEnabled(false);
		mWebView.setHorizontalScrollBarEnabled(false);
		mWebView.setWebViewClient(new OAuthWebViewClient());
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.loadUrl(AUTH_URL + "?client_id=" + ApplicationData.CLIENT_ID + "&redirect_uri="
				+ ApplicationData.CALLBACK_URL + "&response_type=code&display=touch&scope=likes+comments+relationships");
	}

	private class OAuthWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			if (url.startsWith(InstagramApp.mCallbackUrl)) {
				String urls[] = url.split("=");
				mListener.onComplete(urls[1]);
				return true;
			}
			return false;
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			mListener.onError(description);

		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}

	}

	public interface OAuthDialogListener {
		public abstract void onComplete(String accessToken);
		public abstract void onError(String error);
	}

	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {

		@Override
		public void onSuccess() {
			mApp.fetchUserName();
			Intent homeIntent = new Intent(MainActivity.this, HomeActivity.class);
			startActivity(homeIntent);
			finish();
		}

		@Override
		public void onFail(String error) {
			Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
		}
	};
}