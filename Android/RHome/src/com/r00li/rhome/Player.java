/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */

package com.r00li.rhome;

public class Player {
	
	int playerid;
	String type;
	
	public int getPlayerid() {
		return playerid;
	}

	public void setPlayerid(int playerid) {
		this.playerid = playerid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	Player() {
		playerid = -1;
		type = "";
	}

}
