/*
 * Copyright 2011, Mark L. Chang <mark.chang@gmail.com>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Mark L. Chang ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL MARK L. CHANG OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Mark L. Chang.
 */

package com.kd.filmstrip.posts;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mchang
 * Date: 3/25/11
 * Time: 11:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstagramImage implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String thumbnail;
    public String low_resolution;
    public String standard_resolution;
    public String permalink;
    public String username;
    public String user_id;
    public String profile_picture;
    public String full_name = "";
    public String caption = "";
    public String taken_at;
    

	public Long taken_time;
    public String id;
    public String location_name = "";
    public boolean hasLocation = false;

    public boolean user_has_liked;
    
    public boolean video = false;
    public String video_low_res;
    public String video_standard_res;

    public int liker_count;
    public int comment_count;

    public ArrayList<String> liker_list;
    public ArrayList<Comment> comment_list;
    
    // Define Constructors
    public InstagramImage(){}
    public InstagramImage(String thumbnail, String low_resolution,
			String standard_resolution, String permalink, String username,
			String user_id, String profile_picture, String full_name,
			String caption, String taken_at, Long taken_time, String id,
			String location_name, boolean hasLocation, boolean user_has_liked,
			boolean video, String video_low_res, String video_standard_res,
			int liker_count, int comment_count, ArrayList<String> liker_list,
			ArrayList<Comment> comment_list) {
		super();
		this.thumbnail = thumbnail;
		this.low_resolution = low_resolution;
		this.standard_resolution = standard_resolution;
		this.permalink = permalink;
		this.username = username;
		this.user_id = user_id;
		this.profile_picture = profile_picture;
		this.full_name = full_name;
		this.caption = caption;
		this.taken_at = taken_at;
		this.taken_time = taken_time;
		this.id = id;
		this.location_name = location_name;
		this.hasLocation = hasLocation;
		this.user_has_liked = user_has_liked;
		this.video = video;
		this.video_low_res = video_low_res;
		this.video_standard_res = video_standard_res;
		this.liker_count = liker_count;
		this.comment_count = comment_count;
		this.liker_list = liker_list;
		this.comment_list = comment_list;
	}
}
