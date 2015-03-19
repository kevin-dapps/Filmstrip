package com.kd.filmstrip.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mchang
 * Date: 3/25/11
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class User implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int media;
    public int followed_by;
    public int follows;
    public String full_name;
    public String username;
    public String user_id;
    public String profile_picture;
    public String bio;
    public String outgoing_status;
	public boolean unfollow_user;

}
