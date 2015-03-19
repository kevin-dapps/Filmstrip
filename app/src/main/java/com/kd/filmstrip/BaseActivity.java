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

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public abstract class BaseActivity extends ActionBarActivity {

    public static Toolbar toolbar, toolbar_detail,toolbar_other;
    public static ImageView toolbar_image;
    private int toolbar_icon;
    private static final int NUM_OF_ITEMS = 100;
    private static final int NUM_OF_ITEMS_FEW = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutResource());
        toolbar = (Toolbar) findViewById(R.id.toolbar_container);
        toolbar_detail = (Toolbar) findViewById(R.id.toolbar_detail);
        toolbar_other = (Toolbar) findViewById(R.id.toolbar_other);
        if (toolbar != null) {
        	toolbar_image = (ImageView) toolbar.findViewById(R.id.toolbar_image);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        else if(toolbar_detail != null){
            setSupportActionBar(toolbar_detail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        else {
            setSupportActionBar(toolbar_other);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

    }
    @Override 
    public void onResume(){
    	super.onResume();
        toolbar = (Toolbar) findViewById(R.id.toolbar_container);
        toolbar_detail = (Toolbar) findViewById(R.id.toolbar_detail);
        
        if (toolbar != null) {
        	toolbar_image = (ImageView) toolbar.findViewById(R.id.toolbar_image);
            setSupportActionBar(toolbar);
            if(toolbar_icon != 0) {
                toolbar.setNavigationIcon(toolbar_icon);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        else if(toolbar_detail != null){
            setSupportActionBar(toolbar_detail);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        else {
            setSupportActionBar(toolbar_other);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        getToolbar().animate().translationY(0).alpha(1).setDuration(500).setInterpolator(new DecelerateInterpolator());
    }
    protected abstract int getLayoutResource();
    protected void setDummyData(ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS);
    }

    protected void setDummyDataFew(ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS_FEW);
    }

    protected void setDummyData(ListView listView, int num) {
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getDummyData(num)));
    }
    public static ArrayList<String> getDummyData(int num) {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 1; i <= num; i++) {
            items.add("Item " + i);
        }
        return items;
    }
    protected void setActionBarIcon(int iconRes) {
    	toolbar_icon = iconRes;
        getToolbar().setNavigationIcon(iconRes);
    }
    public static Toolbar getToolbar() {
    	if(toolbar != null){
        return toolbar;
    	}
    	else if(toolbar_detail != null){
    	return toolbar_detail;
    	}
        else {
        return toolbar_other;
        }

    }
    public static ImageView getToolbarLogo() {
    	return toolbar_image;
    }
    
}
