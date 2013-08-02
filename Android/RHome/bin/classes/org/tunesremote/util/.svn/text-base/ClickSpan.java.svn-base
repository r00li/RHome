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

import org.tunesremote.LibraryActivity;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Uses a fudge method to allow us to override clicks in a text view. Used in
 * {@link LibraryActivity}. I found this on StackOverflow at
 * http://stackoverflow
 * .com/questions/1697084/handle-textview-link-click-in-my-android-app so I
 * cannot take the credit for this
 * @author Jonathan S.
 */
public class ClickSpan extends ClickableSpan {

   private final OnClickListener mListener;

   public ClickSpan(OnClickListener listener) {
      mListener = listener;
   }

   @Override
   public void onClick(View widget) {
      if (mListener != null)
         mListener.onClick();
   }

   public interface OnClickListener {
      /**
       * What to do when a link is clicked.
       */
      void onClick();
   }

   public static void clickify(TextView view, final String clickableText, final ClickSpan.OnClickListener listener) {

      CharSequence text = view.getText();
      String string = text.toString();
      ClickSpan span = new ClickSpan(listener);

      int start = string.indexOf(clickableText);
      int end = start + clickableText.length();
      if (start == -1)
         return;

      if (text instanceof Spannable) {
         ((Spannable) text).setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      } else {
         SpannableString s = SpannableString.valueOf(text);
         s.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
         view.setText(s);
      }

      MovementMethod m = view.getMovementMethod();
      if ((m == null) || !(m instanceof LinkMovementMethod)) {
         view.setMovementMethod(LinkMovementMethod.getInstance());
      }
   }

}
