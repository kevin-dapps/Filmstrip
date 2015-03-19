package com.kd.filmstrip.nav;

public class NavItems {
     
    private String title;
    private int icon;
    private boolean showIcon;
     
    public NavItems(){}
 
    public NavItems(String title, int icon, boolean showIcon){
        this.title = title;
        this.icon = icon;
        this.showIcon = showIcon;
    }
     
    public String getTitle(){
        return this.title;
    }
     
    public int getIcon(){
        return this.icon;
    }

    public void setTitle(String title){
        this.title = title;
    }
     
    public void setIcon(int icon){
        this.icon = icon;
    }

	public boolean isShowIcon() {
		return showIcon;
	}

     

}