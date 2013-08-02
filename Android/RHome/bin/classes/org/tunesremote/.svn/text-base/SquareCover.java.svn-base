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
package org.tunesremote;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Author: aml.curran Date: 16/07/2012 Time: 16:13
 */
public class SquareCover extends ImageView {

   public SquareCover(Context context) {
      super(context);
   }

   public SquareCover(Context context, AttributeSet attrs) {
      super(context, attrs);
   }

   public SquareCover(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

      int widthMode = MeasureSpec.getMode(widthMeasureSpec);
      int widthSize = MeasureSpec.getSize(widthMeasureSpec);

      int heightMode = MeasureSpec.getMode(heightMeasureSpec);
      int heightSize = MeasureSpec.getSize(heightMeasureSpec);

      int chosenWidth = chooseDimension(widthMode, widthSize);
      int chosenHeight = chooseDimension(heightMode, heightSize);

      int chosenDimension = Math.min(chosenWidth, chosenHeight);

      setMeasuredDimension(chosenDimension, chosenDimension);
   }

   private int chooseDimension(int mode, int size) {
      if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.EXACTLY) {
         return size;
      } else { // (mode == MeasureSpec.UNSPECIFIED)
         return 300;
      }
   }

}
