/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */

package com.r00li.rhome;

public class Room {
	
	public String name;
	public boolean light1_on;
	public boolean light2_on;
	public float temperature;
	
	public int blind_l_status;
	public int blind_r_status;
	public int blind_l_openV;
	public int blind_l_cloveV;
	public int blind_l_midV;
	public int blind_r_openV;
	public int blind_r_cloveV;
	public int blind_r_midV;
	public boolean blind_manual;
	
	Room() {
		
	}
	
	Room(String name) {
		this.name = name;
		
		this.light1_on = false;
		this.light2_on = false;
		this.blind_l_status = 1;
		this.blind_r_status = 1;
		this.temperature = -1;
	}
	
	int blindPlus(int blind) {
		
		if (blind == 0) {
			int pos = blind_l_status+1;
			
			if (pos > 2)
				pos = 2;
			
			return pos;
		}
		else if (blind == 1) {
			int pos = blind_r_status+1;
			
			if (pos > 2)
				pos = 2;
			
			return pos;
		}
		
		return 0;
	}
	
	int blindMinus(int blind) {
		
		if (blind == 0) {
			int pos = blind_l_status-1;
			
			if (pos < 0)
				pos = 0;
			
			return pos;
		}
		else if (blind == 1) {
			int pos = blind_r_status-1;
			
			if (pos < 0)
				pos = 0;
			
			return pos;
		}
		
		return 0;
	}

}
