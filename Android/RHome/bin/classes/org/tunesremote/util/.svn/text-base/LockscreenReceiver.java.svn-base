/*
    TunesRemote+ - http://code.google.com/p/tunesremote-plus/
    
    Copyright (C) 2008 Jeffrey Sharkey, http://jsharkey.org/
    Copyright (C) 2010 TunesRemote+, http://code.google.com/p/tunesremote-plus/
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    The Initial Developer of the Original Code is Jeffrey Sharkey.
    Portions created by Jeffrey Sharkey are
    Copyright (C) 2008. Jeffrey Sharkey, http://jsharkey.org/
    All Rights Reserved.
 */
package org.tunesremote.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Author: aml.curran Date: 14/07/2012 Time: 21:59
 */
public class LockscreenReceiver extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, Intent intent) {
      KeyEvent pressed = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
      Intent nService = new Intent(context, NotificationService.class);
      if (pressed.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
         context.startService(nService.setAction(NotificationService.ACTION_PAUSE));
      } else {
         context.startService(nService.setAction(NotificationService.ACTION_NEXT_TRACK));
      }
   }
}
