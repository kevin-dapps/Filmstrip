package com.kd.filmstrip.nav;


import java.util.ArrayList;

import com.kd.filmstrip.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class NavDrawerListAdapter extends BaseAdapter {
     
    private Context mContext;
    private ArrayList<NavItems> NavItems;
    private LayoutInflater inflater;
    private int mSelectedItem = 0;
	// Theme Info
	public static int selectedThemeInt;
	public static String selectedTheme;
	public static String[] themePrimaryColors;
	public static String[] themePrimaryDarkColors;
     
    public NavDrawerListAdapter(Context context, ArrayList<NavItems> NavItemss){
        this.mContext = context;
        this.NavItems = NavItemss;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    public NavDrawerListAdapter() {

	}
	public int getSelectedItem() {
        return mSelectedItem;
    }
    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
    }
    @Override
    public int getCount() {
        return NavItems.size();
    }
 
    @Override
    public Object getItem(int position) {       
        return NavItems.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
    public static class ViewHolder{
    	public int position;
        public TextView navLabel;
        public ImageView navIcon;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder;
        
        
        if (convertView == null) {
        	vi = inflater.inflate(R.layout.drawer_list_item, parent, false);
            holder = new ViewHolder();
            holder.position = position;
            holder.navLabel = (TextView) vi.findViewById(R.id.drawer_item_title);
            holder.navIcon = (ImageView) vi.findViewById(R.id.drawer_item_icon);
            vi.setTag(holder);
        }
        else {
        holder = (ViewHolder) vi.getTag();
        }
        

        if(position == mSelectedItem && position < 5){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        selectedTheme = prefs.getString("theme_color", "0");
        selectedThemeInt = Integer.parseInt(selectedTheme);
        themePrimaryColors = mContext.getResources().getStringArray(R.array.themeColorsPrimary);
        themePrimaryDarkColors = mContext.getResources().getStringArray(R.array.themeColorsPrimaryDark);
        ((View) holder.navIcon.getParent()).setBackgroundColor(R.color.transparent_gray);
        Drawable myIcon = mContext.getResources().getDrawable(NavItems.get(mSelectedItem).getIcon());
        myIcon.setColorFilter(Color.parseColor(themePrimaryColors[selectedThemeInt]), PorterDuff.Mode.SRC_ATOP);
        holder.navLabel.setTextColor(Color.parseColor(themePrimaryColors[selectedThemeInt]));
        holder.navIcon.setImageDrawable(myIcon);
        
        }
        else {
        holder.navIcon.setTag(NavItems.get(position).getIcon());
        holder.navIcon.setImageResource(NavItems.get(position).getIcon());
        holder.navLabel.setTextColor(mContext.getResources().getColor(R.color.navdrawer_text_color));
        }
        
        
        holder.navLabel.setText(NavItems.get(position).getTitle());

        if(!NavItems.get(position).isShowIcon()){
        	holder.navIcon.setVisibility(View.GONE);
        }
         
         
        return vi;
    }
 
}