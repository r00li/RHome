/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */


package com.r00li.rhome;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;
import org.xbmc.android.remote.business.Command;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.INotifiableManager;
import org.xbmc.jsonrpc.Connection;
import org.xbmc.jsonrpc.client.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.*;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;



/**
 * Main class for the RHome system. This class controls all the main system functions.
 * @author Andrej Rolih
 *
 */
public class RHomeActivity extends Activity {
	
	//DECLARATION OF THE CONTROL/UI ELEMENTS:
	TextView temp; //TextView showing current room temperature (HOME)
	TextView nowPlayingLabel; //TextView showing the currently playing media (HOME)
	RadioButton secondaryLightIndicator; //RadioButton showing if the secondary light is turned on (HOME)
	RadioButton primaryLightIndicator; //RadioButton showing if the primary light is turned on (HOME)
	Button buttonBothBlindsPlus; //Button for selecting blind position for both blinds (HOME)
	Button buttonBothBlindsMinus; //Button for selecting blind position for both blinds (HOME)
	ProgressBar rightBlindStatus; //Progressbar showing current right blind position (three states only) (HOME)
	ProgressBar leftBlindStatus; //Progressbar showing current left blind position (three states only) (HOME)
	CheckBox blindsManualCheck; //CheckBox for toggling automatic (tri-state) or manual blind control (HOME)
	TextView date; //TextView showing current date (HOME)
	TextView bedarijaLabel;
	ListView mediaList; //ListView showing the list of media for curernt player (MEDIA) 
	SeekBar playerSeek; //Seekbar for seeking the currently playing media (HOME)
	SeekBar playerVolume; //SeekBar enabling player volume control (HOME)
	MediaPlayer play; //Media player currently selected - for now only supports one player
	TextView totalPlayingTime; //Textview showing the length of the currently playing content (HOME)
	TextView currentPlayingTime; //Textview showing current time indicator for the player (HOME)
	HashMap<String, String> IRCodes; //HashMap contationing IR codes for different devices - this will be made user changable in future
	MiniServer remoteControlServer;
	EditText debugtext; //Textbox showing raw messages from arduino
	Spinner playerSelector; //Spinner for selecting which media player to control (HOME)
	
	String address, library;
	
	//MyAdapter ada;
	//XBMCMediaSelectedListenter poslusalec1;
	//ArrayList<String> dirLevel;
	
	boolean conn = false;
	
	FTDriver mSerial;
	private boolean mStop=false;
	private boolean mStopped=true;
	String lala = "";
	Handler mHandler = new Handler();
	Room myRoom;
	   
    /**
     * Standard android onCreate method. UI setup, serial communication setup, other important things
     */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.main);

		//Handle tabs
		TabHost tabs=(TabHost)findViewById(R.id.tabhost); 
		tabs.setup();
		TabHost.TabSpec spec=tabs.newTabSpec("tag1");
		spec.setContent(R.id.tab1); 
		spec.setIndicator("Home");
		tabs.addTab(spec);
		spec=tabs.newTabSpec("tag2");
		spec.setContent(R.id.tab2);
		spec.setIndicator("Actions");
		tabs.addTab(spec);
		spec=tabs.newTabSpec("tag3");
		spec.setContent(R.id.tab3);
		spec.setIndicator("Media");
		tabs.addTab(spec);
		spec=tabs.newTabSpec("tag4");
		spec.setContent(R.id.tab4);
		spec.setIndicator("Settings");
		tabs.addTab(spec);/*
        spec=tabs.newTabSpec("tag5");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Button4");
        tabs.addTab(spec);
        spec=tabs.newTabSpec("tag6");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Button5");
        tabs.addTab(spec);
        spec=tabs.newTabSpec("tag7");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Button6fgfg");
        tabs.addTab(spec);
        spec=tabs.newTabSpec("tag8");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Button7gf");
        tabs.addTab(spec);*/


		mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));

		// listen for new devices
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		if(mSerial.begin(FTDriver.BAUD9600)) {
			mainloop();
		}

		//UI setup
		temp = (TextView)findViewById(R.id.temp_label);
		nowPlayingLabel = (TextView)findViewById(R.id.nowPlayingLabel);
		secondaryLightIndicator = (RadioButton)findViewById(R.id.secondaryLightIndicator);
		primaryLightIndicator = (RadioButton)findViewById(R.id.primaryLightIndicator);
		buttonBothBlindsPlus = (Button)findViewById(R.id.buttonBothBlindsPlus);
		buttonBothBlindsMinus = (Button)findViewById(R.id.buttonBothBlindsMinus);
		rightBlindStatus = (ProgressBar)findViewById(R.id.rightBlindStatus);
		leftBlindStatus = (ProgressBar)findViewById(R.id.leftBlindStatus);
		blindsManualCheck = (CheckBox)findViewById(R.id.blindsManualCheck);
		date = (TextView)findViewById(R.id.date);
		mediaList = (ListView)findViewById(R.id.mediaList);
		playerSeek = (SeekBar)findViewById(R.id.playerSeek);
		playerVolume = (SeekBar)findViewById(R.id.playerVolume);
		currentPlayingTime = (TextView)findViewById(R.id.currentPlayingTime);
		totalPlayingTime = (TextView)findViewById(R.id.totalPlayingTime);
		debugtext = (EditText)findViewById(R.id.debugtext);
		playerSelector = (Spinner)findViewById(R.id.player_selector);

		play = new MediaPlayer(0, "XBMC Player", "xbmc", "password", "192.168.1.9", 8080, this);

		//  ada = new MyAdapter(RHomeActivity.this);
		mediaList.setAdapter(play.ada);
		play.startPolling();

		//poslusalec1 = new XBMCMediaSelectedListenter();
		mediaList.setOnItemClickListener(play.poslusalec1);



		myRoom = new Room("Andrejeva soba");
		messageRecv("");

		//enableDisableView(findViewById(R.id.tab1), false);

		bedarijaLabel = (TextView)findViewById(R.id.bedarija_label);


		//new Thread(network).start();

		//Fill in IR codes for devices
		IRCodes = new HashMap<String, String>();
		IRCodes.put("sound+", "FF7887");
		IRCodes.put("sound-", "FFA05F");
		IRCodes.put("TV_on", "B14E00FF");
		IRCodes.put("TV_input_dvia", "B14E02FD");
		IRCodes.put("TV_input_dvid", "B14E1AE5");
		IRCodes.put("sound_channel1", "FF40BF");
		IRCodes.put("sound_channel2", "FFC837");
		IRCodes.put("sound_channel3", "FF30CF");
		IRCodes.put("sound_channel4", "FF708F");
		IRCodes.put("sound_su-", "FF609F");
		IRCodes.put("sound_su+", "FFC03F");

		String playerji[] = {"XBMC"};

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, playerji);
		playerSelector.setAdapter(adapter);


		/*
		 * IN DEVELOPMENT - REMOTE CONTROL VIA DESKTOP/MOBILE APP
		 */
		Server server = new Server();
		try {
			server.addListener(new Listener() {
				public void connected(com.esotericsoftware.kryonet.Connection connection) {
					Log.w("SERVER", connection.toString());
					connection.setTimeout(0);
					connection.setKeepAliveTCP(8000);
				}
				public void disconnected(com.esotericsoftware.kryonet.Connection connection) {
					Log.w("SERVER", connection.toString());
				}
				public void received (com.esotericsoftware.kryonet.Connection connection, Object object) {
					if (object instanceof SomeRequest) {
						SomeRequest request = (SomeRequest)object;
						Log.w("SERVER", request.text);

						SomeResponse response = new SomeResponse();
						response.text = "Thanks!";
						connection.sendTCP(response);
					}
					else {
						Log.w("SERVER", "krneki smo dobil");
					}
				}
			});
			//server.bind(7656);
			server.bind(7656);
			Kryo kryo = server.getKryo();
			kryo.register(SomeRequest.class);
			kryo.register(SomeResponse.class);
			server.start();

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}


    /**
     * Listener that gets called when someone presses one of the player buttons on media screen
     * To be implemented completley
     * @param butt Button that was pressed
     */
    public void toolBarButton(View butt) {
   
    	play.poslusalec1.onItemClick(null, null, 0, 0);
    	
    }
    
    /**
     * Listener that gets called when someone presses one of the player buttons on home screen
     * @param butt Button that was pressed
     */
    public void buttonPlayer(View butt) {
    	   
    	if (butt.getId() == R.id.playButt) {
    		play.sendCommand("pause", -1);
    	}
    	else if (butt.getId() == R.id.stopButt) {
    		play.sendCommand("stop", -1);
    	}
    	else if (butt.getId() == R.id.Seekleft) {
    		//play.sendCommand("seek", playerSeek.getProgress()-1);
    		play.sendCommand("left", -1);
    	}
    	else if (butt.getId() == R.id.Seekright) {
    		//play.sendCommand("seek", playerSeek.getProgress()+1);
    		play.sendCommand("right", -1);
    	}
    	
    }
    
    
    /**
     * Listener that gets called when someone presses one of the light buttons on home screen
     * @param butt Button that was pressed
     */
    public void buttonLight(View butt) {
    	
    	if (butt.getId() == R.id.secondaryLightButtonOn) {
    		sendMessage("LU=21\0");
    	}
    	else if (butt.getId() == R.id.secondaryLightButtonOff) {
    		sendMessage("LU=20\0");
    	}/*
    	else if (butt.getId() == R.id.secondaryLightButtonDimm) {
    		sendMessage("LU=23\0");
    	}*/
    	else if (butt.getId() == R.id.primaryLightButtonOn) {
    		sendMessage("LU=11\0");
    	}
    	else if (butt.getId() == R.id.primaryLightButtonOff) {
    		sendMessage("LU=10\0");
    	}
    	
    }
    
    /**
     * Listener that gets called when someone presses one of the blind buttons on home screen
     * @param butt Button that was pressed
     */
    public void buttonBlind(View butt) {
    	
    	if (butt.getId() == R.id.blindsManualCheck) {
			
    		myRoom.blind_manual = blindsManualCheck.isChecked();
    		messageRecv("");
    		    		
    		return;
    	}
    	
    	if (blindsManualCheck.isChecked()) {
    		if (butt.getId() == R.id.buttonleftBlindPlus) {
    			sendMessage("ZA=L1M\0");
    		}
    		else if (butt.getId() == R.id.buttonLeftBlindMinus) {
    			sendMessage("ZA=L0M\0");
    		}
    		else if (butt.getId() == R.id.buttonRightBlindPlus) {
    			sendMessage("ZA=R1M\0");
    		}
    		else if (butt.getId() == R.id.buttonRightBlindMinus) {
    			sendMessage("ZA=R0M\0");
    		}
    	}
    	else {
    	
    		if (butt.getId() == R.id.buttonleftBlindPlus) {
    			String new_pos = String.valueOf(myRoom.blindPlus(0));
    			sendMessage("ZA=L" + new_pos + "N\0");
    		}
    		else if (butt.getId() == R.id.buttonLeftBlindMinus) {
    			String new_pos = String.valueOf(myRoom.blindMinus(0));
    			sendMessage("ZA=L" + new_pos + "N\0");
    		}
    		else if (butt.getId() == R.id.buttonRightBlindPlus) {
    			String new_pos = String.valueOf(myRoom.blindPlus(1));
    			sendMessage("ZA=R" + new_pos + "N\0");
    		}
    		else if (butt.getId() == R.id.buttonRightBlindMinus) {
    			String new_pos = String.valueOf(myRoom.blindMinus(1));
    			sendMessage("ZA=R" + new_pos + "N\0");
    		}
    		else if (butt.getId() == R.id.buttonBothBlindsPlus) {
    			String new_pos = String.valueOf(myRoom.blindPlus(1));
    			sendMessage("ZA=R" + new_pos + "N\0");
    			sendMessage("ZA=L" + new_pos + "N\0");
    		}
    		else if (butt.getId() == R.id.buttonBothBlindsMinus) {
    			String new_pos = String.valueOf(myRoom.blindMinus(1));
    			sendMessage("ZA=R" + new_pos + "N\0");
    			sendMessage("ZA=L" + new_pos + "N\0");
    		}
    	}
    }
    
    
    /**
     * Listener that gets called when one of the buttons on the action screen was pressed
     * @param butt Button that was pressed
     */
    public void buttonAction(View butt) {
    	
    	if (butt.getId() == R.id.controls_projON) {
    		sendMessage("IR=" + IRCodes.get("TV_on") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_projinputdvia) {
    		sendMessage("IR=" + IRCodes.get("TV_input_dvia") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_projinputdvid) {
    		sendMessage("IR=" + IRCodes.get("TV_input_dvid") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_volplus) {
    		sendMessage("IR=" + IRCodes.get("sound+") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_volminus) {
    		sendMessage("IR=" + IRCodes.get("sound-") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_chan1) {
    		sendMessage("IR=" + IRCodes.get("sound_channel1") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_chan2) {
    		sendMessage("IR=" + IRCodes.get("sound_channel2") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_chan3) {
    		sendMessage("IR=" + IRCodes.get("sound_channel3") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_chan4) {
    		sendMessage("IR=" + IRCodes.get("sound_channel4") +"\0");
    	}
    	else if (butt.getId() == R.id.controls_speak_reset) {
    		sendMessage("IR=" + IRCodes.get("sound_channel1") +"\0");
    		try { Thread.sleep(1000); } catch(Exception e) {}
    		sendMessage("IR=" + IRCodes.get("sound_su-") +"\0");
    		try { Thread.sleep(1000); } catch(Exception e) {}
    		sendMessage("IR=" + IRCodes.get("sound-") +"\0");
    		try { Thread.sleep(500); } catch(Exception e) {}
    		sendMessage("IR=" + IRCodes.get("sound-") +"\0");
    		try { Thread.sleep(500); } catch(Exception e) {}
    		sendMessage("IR=" + IRCodes.get("sound-") +"\0");
    		try { Thread.sleep(500); } catch(Exception e) {}
    		sendMessage("IR=" + IRCodes.get("sound-") +"\0");
    		try { Thread.sleep(500); } catch(Exception e) {}
    	}
    	
    	
    }
    
    
    
    /**
     * Listener that gets called when someone presses one of the mode selector buttons
     * @param butt Button pressed
     */
    public void modeSelectorListener(View butt) {
    	
    	if (butt.getId() == R.id.modeSelector_sleep) {
    		engageMode(1);
    	}
    	else if (butt.getId() == R.id.modeSelector_movie) {
    		engageMode(2);
    	}
    	
    }
    
    
    
    /**
     * Flashes an LED, beeps piezo speaker on the controller
     */
    public void reportError() {
    	sendMessage("LO=1\0");
    	sendMessage("BE=1\0");
    	sendMessage("BE=1\0");
    	sendMessage("LO=0\0");
    }
    
    
    /**
     * Method that sends a message to the arduino controller
     * @param message Message to send to arduino controller. Must be formated correctly! DO NOT FORGET NULL TERMINATOR CHARACTER! Read more about the "protocol" in the rhome_serial.txt 
     */
    private void sendMessage(String message) {
    	
    	if (mSerial.isConnected()) {
    		byte toSend[] = message.getBytes();
    		mSerial.write(toSend, toSend.length);
    	}
    	else {
    		Toast.makeText(RHomeActivity.this, "Could not send data to controller! Is controller plugged in?", Toast.LENGTH_SHORT).show();
    	}
    	
    }
    
    
    
    /**
     * Call this when you want to engage one of the automatic modes.
     * @param mode Selected mode - 1=sleep, 2=movie
     */
    public void engageMode(int mode) {
    	
    	if (mode == 1) {
    		new Thread(new Runnable() {
				public void run() {
					sendMessage("LO=1\0");
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {
					}
					sendMessage("LU=20\0");
					try { Thread.sleep(1000); } catch(Exception e) {}
					sendMessage("LU=10\0");
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
					}
					sendMessage("LO=0\0");
				}}).start();
    	}
    	else if (mode == 2) {
    		new Thread(new Runnable() {
				public void run() {
					sendMessage("LO=1\0");
					try { Thread.sleep(500); } catch(Exception e) {}
					sendMessage("LU=20\0");
					try { Thread.sleep(500); } catch(Exception e) {}
					sendMessage("LU=10\0");
					try { Thread.sleep(500); } catch(Exception e) {}
					sendMessage("IR=" + IRCodes.get("TV_on") +"\0");
					try { Thread.sleep(500); } catch(Exception e) {}
					sendMessage("LO=0\0");
				}}).start();
    	}
    }
    
    
    
    /**
     * Method called when a message is received from arduino. You can implement different actions/responses here
     * @param message message received from the arduino. Using the format XX=YYYYY (XX - action, YYYYY - parameters). Read more about the "protocol" in the rhome_serial.txt 
     */
    private void messageRecv(final String message) {
    	//bla.setText("bu");

    	Log.w("msg: ", message);
    	
    	//New temperature measurement
    	if (message.startsWith("TM=")) {
    		float tmp = Float.parseFloat(message.substring(3));
    		if (tmp > -80 && tmp < 80)
    			myRoom.temperature = tmp;
    	}
    	//Change in the light status (confirms light on/light off)
    	else if (message.startsWith("LU=")) {
    		
    		boolean status = message.charAt(4) == '1' ? true : false;
    		
    		if (message.charAt(3) == '1')
    			myRoom.light1_on = status;
    		else if (message.charAt(3) == '2')
    			myRoom.light2_on = status;
    		
    	}
    	//Change in the window blind position (confirmation message)
    	else if (message.startsWith("ZA=")) {
    		
    		boolean leva = (message.charAt(3) == 'L') ? true : false;
    		int status = Integer.parseInt("" + message.charAt(4));
    		
    		if (leva)
    			myRoom.blind_l_status = status;
    		else
    			myRoom.blind_r_status = status;
    	}
    	//Remote events - currently RF remote events, IR remote events are handled separately.
    	else if (message.startsWith("RE=")) {
    		
    		int koda = Integer.parseInt(message.substring(3));
    		
    		switch(koda) {
    		
    		case 26640: sendMessage("LU=21\0"); break;
    		case 26638: sendMessage("LU=20\0"); break;
    		case 31014: String new_pos = String.valueOf(myRoom.blindPlus(1));
						sendMessage("ZA=R" + new_pos + "N\0");
						sendMessage("ZA=L" + new_pos + "N\0");break;
    		case 31012: String nw_pos = String.valueOf(myRoom.blindMinus(1));
						sendMessage("ZA=R" + nw_pos + "N\0");
						sendMessage("ZA=L" + nw_pos + "N\0");break;
    		case 28098: sendMessage("LU=11\0"); break;
    		case 28096: sendMessage("LU=10\0"); break;
    		case 32472: engageMode(1); break;
    		case 32470: engageMode(2); break;
    		
    		case 27126: play.sendCommand("up", -1); break;
    		case 27124: play.sendCommand("select", -1); break;
    		case 31500: play.sendCommand("down", -1); break;
    		case 31498: play.sendCommand("back", -1); break;
    		case 28584: play.sendCommand("pause", -1);break;
    		case 28582: play.sendCommand("stop", -1); break;
    		case -32578: sendMessage("IR=" + IRCodes.get("sound+") +"\0"); break;
    		case -32580: sendMessage("IR=" + IRCodes.get("sound-") +"\0"); break;
    		
    		case 26802: sendMessage("IR=" + IRCodes.get("TV_on") +"\0"); break;
    		case 26800: sendMessage("IR=" + IRCodes.get("TV_input_dvia") +"\0"); break;
    		case 31176: play.reconnect(); break;
    		case 31174: sendMessage("IR=" + IRCodes.get("sound_channel1") +"\0"); break;
    		case 28260: play.sendCommand("left", -1); break;
    		case 28258: play.sendCommand("right", -1); break;
    		case 32634: break;
    		case 32632: break;
    		}
    	}
    	
    	//Handle the UI updates. Change current temperature display, ... 	
    	mHandler.post(new Runnable() {
    		public void run() {
    			debugtext.setText(/*debugtext.getText() + "\n" +*/message);

    			temp.setText(String.format("Temperature: %.1f°C", myRoom.temperature-3));

    			secondaryLightIndicator.setChecked(myRoom.light2_on);
    			primaryLightIndicator.setChecked(myRoom.light1_on);
    			leftBlindStatus.setProgress(myRoom.blind_l_status);
    			rightBlindStatus.setProgress(myRoom.blind_r_status);

    			if (myRoom.blind_manual) {
    				buttonBothBlindsPlus.setEnabled(false);
    				buttonBothBlindsMinus.setEnabled(false);
    			}
    			else {
    				if (myRoom.blind_r_status != myRoom.blind_l_status) {
    					buttonBothBlindsPlus.setEnabled(false);
    					buttonBothBlindsMinus.setEnabled(false);
    				}
    				else {
    					buttonBothBlindsPlus.setEnabled(true);
    					buttonBothBlindsMinus.setEnabled(true);
    				}
    			}

    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, dd.MM.yyyy");
    			String dateAsString = simpleDateFormat.format(new Date());
    			date.setText(dateAsString);

    			if (mSerial.isConnected() && conn == false) {
    				//enableDisableView(findViewById(R.id.tab1), true);
    				Toast.makeText(RHomeActivity.this, "USB connected!", Toast.LENGTH_LONG).show();
    				conn = true;
    			}
    		}
    	});
    	
    }
    
    
    
    
    /*///////////////////////////////////////////
     * PRIVATE METHODS
     *///////////////////////////////////////////
    
    //Method that creates a new main thread - used for arduino communication
	private void mainloop() {
		new Thread(mLoop).start();
	}
	
	//Thread handling raw character communication with the arduino, when a message is received it calls a messageRecv method with a completed message
	private Runnable mLoop = new Runnable() {
		public void run() {
			int i;
			int len;
			byte[] rbuf = new byte[4096];

			for(;;){//this is the main loop for transferring

				//////////////////////////////////////////////////////////
				// Read and Display to Terminal
				//////////////////////////////////////////////////////////
				len = mSerial.read(rbuf);

				//mText = (String) mTvSerial.getText();
				for(i=0;i<len;++i) {


					if (((char)rbuf[i]) == '\0') {
						String ms = new String(lala);
						lala = "";
						messageRecv(ms);
						lala = "";
					}
					else {
						lala = lala + (char)rbuf[i];
						//bla.setText("juhu");
						//bla.setText(bla.getText()+ "" +(char)rbuf[i]);
					}


				}

				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				if(mStop) {
					mStopped = true;
					return;
				}
			}
		}
	};
	
	//To be removed
	private void enableDisableView(View view, boolean enabled) {
		view.setEnabled(enabled);

		if ( view instanceof ViewGroup ) {
			ViewGroup group = (ViewGroup)view;

			for ( int idx = 0 ; idx < group.getChildCount() ; idx++ ) {
				enableDisableView(group.getChildAt(idx), enabled);
			}
		}
	}


	// BroadcastReceiver when insert/remove the device USB plug into/from a USB port  
	BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				mSerial.usbAttached(intent);
				mSerial.begin(FTDriver.BAUD9600);

				mainloop();

			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				Toast.makeText(RHomeActivity.this, "USB disconnected! Some functions are disabled...", Toast.LENGTH_LONG).show();
				//enableDisableView(findViewById(R.id.tab1), false);
				conn = false;
				mSerial.usbDetached(intent);
				mSerial.end();
				mStop=true;
			}
		}
	};
}




//For setting automatic actions - TODO finish implementation
class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, 0, 0,true);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getFragmentManager(), "timePicker");
	}

}

//Manager for handling XBMC media player - TODO fix XBMC implementation
class mojManager implements INotifiableManager {

	public void onFinish(DataResponse<?> response) {
		// TODO Auto-generated method stub
	}

	public void onWrongConnectionState(int state, Command<?> cmd) {
		// TODO Auto-generated method stub

	}

	public void onError(Exception e) {
		//Log.w("Bla", e.getMessage());
		Log.w("MEDIA_ERROR", e.toString());
		// TODO Auto-generated method stub

	}

	public void onMessage(String message) {
		Log.w("traaalal", message);
		// TODO Auto-generated method stub

	}

	public void onMessage(int code, String message) {
		// TODO Auto-generated method stub

	}

	public void retryAll() {
		// TODO Auto-generated method stub

	}

}

class SomeResponse {
	public String text;
}

class SomeRequest {
	public String text;
}
