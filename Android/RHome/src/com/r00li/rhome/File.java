/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */

package com.r00li.rhome;

public class File {
	
	String filetype;
	String label;
	String file;
	String type;
	
	File() {
		filetype = "";
		label = "";
		file = "";
		type = "";
	}
	
	File(String file, String ft) {
		this.file = file;
		filetype = ft;
		label = "bla";
		type = "";
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getFiletype() {
		return filetype;
	}
	public void setFiletype(String filetype) {
		this.filetype = filetype;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

}
