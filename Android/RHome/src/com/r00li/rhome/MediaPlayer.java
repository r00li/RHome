/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */

package com.r00li.rhome;

import java.util.ArrayList;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.xbmc.jsonrpc.Connection;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


/**
 * Media player Class. Currently handles XBMC media player support.
 * Itunes and other players should be added later. Based on the official XBMC remote for android.
 * To be replaced with a custom system at a later date.
 * @author Andrej Rolih
 *
 */
public class MediaPlayer {
	
	int type;
	String name;
	String username;
	String password;
	String adress;
	int port;
	boolean poll;
	Thread poller;
	Handler mHandler = new Handler();
	RHomeActivity context;
	
	mojManager manager;
	MyAdapter ada;
	XBMCMediaSelectedListenter poslusalec1;
	ArrayList<String> dirLevel;
	Connection c;
	
	boolean playing;
	boolean displayHold = false;
	String playingTitle;
	int playingPercentage;
	Player activePlayer;
	int playingVolume;
	int time_p_seconds;
	int time_p_hours;
	int time_p_minutes;
	int time_a_seconds;
	int time_a_hours;
	int time_a_minutes;
	boolean paused;
	
	TextView nowPlayingLabel;
	
	MediaPlayer (int type, String name, String username, String password, String adress, int port, RHomeActivity context) {
		this.type = type;
		this.name = name;
		this.username = username;
		this.password = password;
		this.adress = adress;
		this.port = port;
		this.context = context;
		
		poslusalec1 = new XBMCMediaSelectedListenter();
		dirLevel = new ArrayList<String>();     
		ada = new MyAdapter();

		nowPlayingLabel = (TextView)this.context.nowPlayingLabel;

		
		poslusalec1.onItemClick(null, null, 0, 0);
		
        c = Connection.getInstance(this.adress, this.port);
        c.setAuth(this.username, this.password);
        c.setTimeout(0);


        context.playerSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
        	boolean usermode = false;
        	int myprogress = 0;
        	
			public void onStopTrackingTouch(SeekBar seekBar) {
				displayHold = false;
				if (usermode)
					sendCommand("seek", myprogress);
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				displayHold = true;
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					usermode = true;
					myprogress = progress;
				}
			}
		});
        
        context.playerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
        	boolean usermode = false;
        	int myprogress = 0;
        	
			public void onStopTrackingTouch(SeekBar seekBar) {
				displayHold = false;
				if (usermode)
					sendCommand("volume", myprogress);
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
				displayHold = true;
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					usermode = true;
					myprogress = progress;
				}
			}
		});
	}
	
	
	
	public void reconnect() {
        c = Connection.getInstance(this.adress, this.port);
        c.setAuth(this.username, this.password);
        c.setTimeout(0);
	}
	
	
	void startPolling() {
		if (type == 0) {
			poll = true;
			poller = new Thread(playerStatXBMC);
			poller.start();
		}
	}
	
	void stopPolling() {
		poll = false;
	}
	
	void updateDisplay() {
		
		if (displayHold)
			return;
		
		mHandler.post(new Runnable() {
			public void run() {
				if (playing) {
					
					if (!nowPlayingLabel.getText().equals(playingTitle)) {
						nowPlayingLabel.setText(playingTitle);
					}
					
					context.playerSeek.setProgress(playingPercentage);
					context.playerVolume.setProgress(playingVolume);
					
					String currentPlayingTime = String.format("%02d:%02d:%02d", time_p_hours, time_p_minutes, time_p_seconds);
					String totalPlayingTime = String.format("%02d:%02d:%02d", time_a_hours, time_a_minutes, time_a_seconds);;

					context.currentPlayingTime.setText(currentPlayingTime);
					context.totalPlayingTime.setText(totalPlayingTime);
					

				}
				else {
					nowPlayingLabel.setText("< Not active >");
					context.playerSeek.setProgress(0);
					context.playerVolume.setProgress(0);
					context.currentPlayingTime.setText("");
					context.totalPlayingTime.setText("");
				}
			}
		});
		
	}
	
	
	void sendCommand(final String command, final int parameter) {
		
		new Thread(new Runnable() {
			public void run() {

				try {
					
					mojManager m = new mojManager();
					ObjectMapper mapper = new ObjectMapper();
					JsonNode rootNode = mapper.createObjectNode(); // will be of type ObjectNode
					
					if (command.equals("pause")) {
					
						((ObjectNode)rootNode).put("playerid", activePlayer.playerid);
						JsonNode bla = c.getJson(m, "Player.PlayPause", rootNode);
					}
					else if (command.equals("stop")) {
						
						((ObjectNode)rootNode).put("playerid", activePlayer.playerid);
						JsonNode bla = c.getJson(m, "Player.Stop", rootNode);
					}
					else if (command.equals("seek")) {
						
						((ObjectNode)rootNode).put("playerid", activePlayer.playerid);
						((ObjectNode)rootNode).put("value", parameter);
						JsonNode bla = c.getJson(m, "Player.Seek", rootNode);
					}
					else if (command.equals("volume")) {
						
						((ObjectNode)rootNode).put("volume", parameter);
						JsonNode bla = c.getJson(m, "Application.SetVolume", rootNode);
					}
					else if (command.equals("left")) {
						
						JsonNode bla = c.getJson(m, "Input.Left", null);
					}
					else if (command.equals("right")) {
						
						JsonNode bla = c.getJson(m, "Input.Right", null);
					}
					else if (command.equals("select")) {
						
						JsonNode bla = c.getJson(m, "Input.Select", null);
					}
					else if (command.equals("back")) {
						
						JsonNode bla = c.getJson(m, "Input.Back", null);
					}
					else if (command.equals("up")) {
						
						JsonNode bla = c.getJson(m, "Input.Up", null);
					}
					else if (command.equals("down")) {
						
						JsonNode bla = c.getJson(m, "Input.Down", null);
					}

				}
				catch(Exception e) {
					context.reportError();
					//Log.w("MEDIA_ERROR", e.toString());
					Log.w("MEDIA_ERROR", e.toString());
					//e.printStackTrace();
				}
			}
		}).start();
		
	}
	
	
	private Runnable playerStatXBMC = new Runnable() {
		public void run() {
			while(poll) {
	        mojManager m = new mojManager();
	        
	        try {
		        ObjectMapper mapper = new ObjectMapper();
		        JsonNode rootNode = mapper.createObjectNode(); // will be of type ObjectNode
		        ((ObjectNode)rootNode).put("media", "video");
	        	JsonNode bla = c.getJson(m, "Player.GetActivePlayers", null);
	        	ObjectMapper mp = new ObjectMapper();
	        	final ArrayList<Player> neggg = mp.readValue(bla.traverse(), new TypeReference<ArrayList<Player>>() {});
	        	
	        	Player pl = null;
	        	for (Player p : neggg) {
	        		if (p.type.equals("video") || p.type.equals("music")) {
	        			pl = p;
	        		}
	        	}
	        	
	        	final Player pp = pl;
	        	activePlayer = pp;
	        	
	        	if (pp != null) {
	        		
			        JsonNode requestNode = mapper.createObjectNode(); // will be of type ObjectNode
			        ((ObjectNode)requestNode).put("playerid", pp.playerid);
			        ((ObjectNode)requestNode).putArray("properties");
		        	JsonNode nowPlay = c.getJson(m, "Player.GetItem", requestNode);
		        	JsonNode nowPlayNode = nowPlay.get("item").get("label");
		        			        	
		        	playing = true;
		        	playingTitle = "► " + nowPlayNode.getTextValue();
		        	
		        	
			        requestNode = mapper.createObjectNode(); // will be of type ObjectNode
			        ((ObjectNode)requestNode).put("playerid", pp.playerid);
			        ((ObjectNode)requestNode).putArray("properties").add("percentage").add("time").add("totaltime").add("speed");
		        	JsonNode nowPercentage = c.getJson(m, "Player.GetProperties", requestNode);
		        	JsonNode percentageNode = nowPercentage.get("percentage");
		        	
		        	playingPercentage = percentageNode.getIntValue();
		        	
		        	//get the current playing time
		        	time_p_seconds = nowPercentage.get("time").get("seconds").getIntValue();
		        	time_p_hours = nowPercentage.get("time").get("hours").getIntValue();
		        	time_p_minutes = nowPercentage.get("time").get("minutes").getIntValue();
		        	
		        	//get the total time
		        	time_a_seconds = nowPercentage.get("totaltime").get("seconds").getIntValue();
		        	time_a_hours = nowPercentage.get("totaltime").get("hours").getIntValue();
		        	time_a_minutes = nowPercentage.get("totaltime").get("minutes").getIntValue();
		        	
		        	paused = (nowPercentage.get("speed").getIntValue() == 0) ? true:false;
		        	
		        	if (paused) {
		        		playingTitle = "║ " + nowPlayNode.getTextValue();
		        	}
		        	
		        	
			        requestNode = mapper.createObjectNode(); // will be of type ObjectNode
			        ((ObjectNode)requestNode).putArray("properties").add("volume");
		        	JsonNode nowVolume = c.getJson(m, "Application.GetProperties", requestNode);
		        	JsonNode volumeNode = nowVolume.get("volume");
		        			        	
		        	playingVolume = volumeNode.getIntValue();
		        		        		
	        	}
	        	else {
	        		playing = false;
	        	}
	        	
	        	updateDisplay();

	        }
	        catch(Exception e) {
	        	Log.w("MEDIA_ERROR", e.toString());
	        	//Log.w("BABABA", e.toString());
	        	//e.printStackTrace();
	        }
	        
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}
	};
	
	
	class XBMCMediaSelectedListenter implements OnItemClickListener {

		public void onItemClick(AdapterView<?> a, View v,int position, long id) 
		{
			File izbrano_temp;
			if (a != null) {
				izbrano_temp = (File) a.getItemAtPosition(position);
			}
			else {
				if (!dirLevel.isEmpty())
					dirLevel.remove(dirLevel.size()-1);
				
				if (!dirLevel.isEmpty())
					izbrano_temp = new File(dirLevel.get(dirLevel.size()-1), "back1");
				else
					izbrano_temp = new File("", "top");


			}
			final File izbrano = izbrano_temp;

			if (izbrano.getFiletype().equals("file")) {
				new Thread(new Runnable() {
					public void run() {
						
						mojManager m = new mojManager();
						ObjectMapper mapper = new ObjectMapper();
										        
				        JsonNode fileNode = mapper.createObjectNode(); // will be of type ObjectNode
				        ((ObjectNode)fileNode).put("file", izbrano.file);
				        ArrayNode request = mapper.createArrayNode();
				        request.add(fileNode);
				        
			        	JsonNode nowPlay = c.getJson(m, "Player.Open", request);
					}
				}).start();
			}
			else if (izbrano.getFiletype().equals("directory") || izbrano.getFiletype().equals("") || izbrano.getFiletype().equals("back1")) {
				new Thread(new Runnable() {
					public void run() {
						mojManager m = new mojManager();

						ObjectMapper mapper = new ObjectMapper();
						JsonNode rootNode = mapper.createObjectNode(); // will be of type ObjectNode
						((ObjectNode)rootNode).put("directory", izbrano.file);
						try {
							JsonNode bla = c.getJson(m, "Files.GetDirectory", rootNode);
							ObjectMapper mp = new ObjectMapper();
							JsonNode list = bla.get("files");
							final ArrayList<File> neggg = mp.readValue(list.traverse(), new TypeReference<ArrayList<File>>() {});

							if (!izbrano.getFiletype().equals("back1"))
								dirLevel.add(izbrano.file);

							mHandler.post(new Runnable() {
								public void run() {
									ada.setItems(neggg);
									ada.notifyDataSetChanged();
								}});

						}
						catch(Exception e) {
							Log.w("MEDIA_ERROR", e.toString());
							//Log.w("BABABA", e.toString());
							//e.printStackTrace();
						}
					}
				}).start();
			}
			else if (izbrano.getFiletype().equals("top")) {
				new Thread(new Runnable() {
					public void run() {
				        mojManager m = new mojManager();
				        
				        ObjectMapper mapper = new ObjectMapper();
				        JsonNode rootNode = mapper.createObjectNode(); // will be of type ObjectNode
				        ((ObjectNode)rootNode).put("media", "video");
				        try {
				        	JsonNode bla = c.getJson(m, "Files.GetSources", rootNode);
				        	JsonParser a = bla.traverse();
				        	ObjectMapper mp = new ObjectMapper();
				        	JsonNode list = bla.get("sources");
				        	final ArrayList<File> neggg = mp.readValue(list.traverse(), new TypeReference<ArrayList<File>>() {});
				        	
				        	mHandler.post(new Runnable() {
								public void run() {
						        	ada.setItems(neggg);
						        	ada.notifyDataSetChanged();
								}});

				        }
				        catch(Exception e) {
				        	Log.w("MEDIA_ERROR", e.toString());
				        	//Log.w("BABABA", e.toString());
				        	//e.printStackTrace();
				        }
					}
				}).start();
			}
		}

	}
	
	class MyAdapter extends BaseAdapter {
	    
		   //Context context;
		    
		   ArrayList<File> itemsArray;
		    
		   MyAdapter(){
		    //context = c;
		    itemsArray = new ArrayList<File>();
		   }
		   
		   public void setItems(ArrayList<File> items) {
			   if (items != null)
				   itemsArray = items;
		   }
		 
		  public int getCount() {
		   // TODO Auto-generated method stub
		   return itemsArray.size();
		  }
		 
		  public Object getItem(int position) {
		   // TODO Auto-generated method stub
		   return itemsArray.get(position);
		  }
		 
		  
		  public long getItemId(int position) {
		   // TODO Auto-generated method stub
		   return position;
		  }
		 
		  
		  public View getView(int position, View convertView, ViewGroup parent) {
		   // TODO Auto-generated method stub
		    
		   View rowView = LayoutInflater
		     .from(parent.getContext())
		     .inflate(R.layout.row, null);
		   TextView listTextView = (TextView)rowView.findViewById(R.id.itemtext);
		   listTextView.setText(itemsArray.get(position).getLabel());
		   if (itemsArray.get(position).getFiletype().equals("file")) {
			   TextView ch = (TextView)rowView.findViewById(R.id.movie);
			   ch.setText("X");
		   }
		    
		   return rowView;
		  }
		   
		   
		  }



	

}
