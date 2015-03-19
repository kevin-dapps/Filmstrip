package com.kd.filmstrip;

/**
 * Created by Kevin on 2/22/2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;



public class EditProfileActivity extends BaseActivity {
    // Theme Info
    public static int selectedThemeInt;
    public static String selectedTheme;
    public static String[] themePrimaryColors;
    public static String[] themePrimaryDarkColors;

    private static final String AUTH_URL = "https://api.instagram.com/oauth/authorize/";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", 0);
        setResult(RESULT_OK,returnIntent);
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
        getToolbar().setTitle("Edit Your Profile");
        setUpWebView();
    }
    @Override
    protected int getLayoutResource() {
        return R.layout.edit_profile;
    }
    private void setUpWebView() {
        WebView mWebView = (WebView) findViewById(R.id.filmstrip_loginwebview);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl("https://instagram.com/accounts/edit/");
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                ActivityCompat.finishAfterTransition(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}