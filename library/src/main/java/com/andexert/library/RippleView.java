package com.andexert.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.ViewParent;
import android.view.animation.Animation;

/**
 * @author Hannes Dorfmann
 */
public interface RippleView {
  Context getContext();

  void setDrawingCacheEnabled(boolean enabled);

  void setClickable(boolean clickable);

  void invalidate();

  void startAnimation(Animation animation);

  int getMeasuredHeight();

  int getMeasuredWidth();

  Bitmap getDrawingCache(boolean b);

  ViewParent getParent();
}
