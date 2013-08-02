/*
 * RHome version 0.1
 * http://rhome.r00li.com
 * Copyright 2012, 2013 Andrej Rolih
 * Licensed under GPLv3 - see LICENSE.txt
 * 
 * Early development build. Not suitable for end-user.
 */

package com.r00li.rhome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;


class Povezava extends Thread{
	DataInputStream dataInputStream;
	DataOutputStream dataOutputStream;

    public Povezava(Socket connSoc) {
        try {
			dataInputStream = new DataInputStream(connSoc.getInputStream());
			dataOutputStream = new DataOutputStream(connSoc.getOutputStream());
			while(true) {
			Log.w("SERVER","ip: " + connSoc.getInetAddress());
			Log.w("SERVER", "message: " + dataInputStream.readUTF());
			}
			//dataOutputStream.writeUTF("Hello!");
        }catch (Exception e) {
            e.printStackTrace();
            try{
                connSoc.close();
            }catch (Exception ex) {
                e.printStackTrace();
            }
        }
        finally {

			if (dataInputStream != null) {
				try {
					dataInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (dataOutputStream != null) {
				try {
					dataOutputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
        this.start();
    }
}


public class MiniServer {

	RHomeActivity context;
	public boolean status;

	MiniServer(RHomeActivity context) {
		this.context = context;
	}

	void startServer() {
		new Thread(new Runnable() {
			
		public void run() {
		status = true;
		ServerSocket serverSocket = null;
		Socket socket = null;
		DataInputStream dataInputStream = null;
		DataOutputStream dataOutputStream = null;

		try {
			serverSocket = new ServerSocket(7656);
			Log.w("SERVER","Listening");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (true) {
			try {
				socket = serverSocket.accept();
				Povezava pov = new Povezava(socket);
				
				if (status == false)
					break;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
				status = false;
				
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		}
		}).start();

	}
}
