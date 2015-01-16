/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Robin Chutaux
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.andexert.library;

import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ListView;

/**
 * Author :    Chutaux Robin, Hannes Dorfmann
 * Date :      10/8/2014
 *
 * Delegate to the following methods:
 * <ul>
 *   <li>draw()</li>
 *   <li>onTouch()</li>
 *   <li>onSizeChanged()</li>
 * </ul>
 */
public class RippleDelegate {
  private int WIDTH;
  private int HEIGHT;
  private int FRAME_RATE = 10;
  private int DURATION = 400;
  private int PAINT_ALPHA = 90;
  private Handler canvasHandler;
  private float radiusMax = 0;
  private boolean animationRunning = false;
  private int timer = 0;
  private int timerEmpty = 0;
  private int durationEmpty = -1;
  private float x = -1;
  private float y = -1;
  private int zoomDuration;
  private float zoomScale;
  private ScaleAnimation scaleAnimation;
  private Boolean hasToZoom;
  private Boolean isCentered;
  private Integer rippleType;
  private Paint paint;
  private Bitmap originBitmap;
  private int rippleColor;
  private int ripplePadding;
  private GestureDetector gestureDetector;
  private final Runnable runnable = new Runnable() {
    @Override
    public void run() {
      view.invalidate();
    }
  };

  private RippleView view;

  public RippleDelegate(RippleView view, AttributeSet attrs) {
    this.view = view;
    init(attrs);
  }

  private void init(final AttributeSet attrs) {

    final TypedArray typedArray =
        view.getContext().obtainStyledAttributes(attrs, R.styleable.RippleReleativeLayout);
    rippleColor = typedArray.getColor(R.styleable.RippleReleativeLayout_rv_color,
        view.getContext().getResources().getColor(R.color.rippelColor));
    rippleType = typedArray.getInt(R.styleable.RippleReleativeLayout_rv_type, 0);
    hasToZoom = typedArray.getBoolean(R.styleable.RippleReleativeLayout_rv_zoom, false);
    isCentered = typedArray.getBoolean(R.styleable.RippleReleativeLayout_rv_centered, false);
    DURATION = typedArray.getInteger(R.styleable.RippleReleativeLayout_rv_rippleDuration, DURATION);
    FRAME_RATE = typedArray.getInteger(R.styleable.RippleReleativeLayout_rv_framerate, FRAME_RATE);
    PAINT_ALPHA = typedArray.getInteger(R.styleable.RippleReleativeLayout_rv_alpha, PAINT_ALPHA);
    ripplePadding =
        typedArray.getDimensionPixelSize(R.styleable.RippleReleativeLayout_rv_ripplePadding, 0);
    canvasHandler = new Handler();
    zoomScale = typedArray.getFloat(R.styleable.RippleReleativeLayout_rv_zoomScale, 1.03f);
    zoomDuration = typedArray.getInt(R.styleable.RippleReleativeLayout_rv_zoomDuration, 200);
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(rippleColor);
    paint.setAlpha(PAINT_ALPHA);

    // TODO is this needed?
    // view.setWillNotDraw(false);

    gestureDetector =
        new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
          @Override
          public void onLongPress(MotionEvent event) {
            super.onLongPress(event);
            animateRipple(event);
            sendClickEvent(true);
          }

          @Override
          public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
          }

          @Override
          public boolean onSingleTapUp(MotionEvent e) {
            return true;
          }
        });

    view.setDrawingCacheEnabled(true);
    view.setClickable(true);
  }

  public void draw(Canvas canvas) {
    if (animationRunning) {
      if (DURATION <= timer * FRAME_RATE) {
        animationRunning = false;
        timer = 0;
        durationEmpty = -1;
        timerEmpty = 0;
        canvas.restore();
        view.invalidate();
        return;
      } else {
        canvasHandler.postDelayed(runnable, FRAME_RATE);
      }

      if (timer == 0) canvas.save();

      canvas.drawCircle(x, y, (radiusMax * (((float) timer * FRAME_RATE) / DURATION)), paint);

      paint.setColor(view.getContext().getResources().getColor(android.R.color.holo_red_light));

      if (rippleType == 1
          && originBitmap != null
          && (((float) timer * FRAME_RATE) / DURATION) > 0.4f) {
        if (durationEmpty == -1) durationEmpty = DURATION - timer * FRAME_RATE;

        timerEmpty++;
        final Bitmap tmpBitmap = getCircleBitmap(
            (int) ((radiusMax) * (((float) timerEmpty * FRAME_RATE) / (durationEmpty))));
        canvas.drawBitmap(tmpBitmap, 0, 0, paint);
        tmpBitmap.recycle();
      }

      paint.setColor(rippleColor);

      if (rippleType == 1) {
        if ((((float) timer * FRAME_RATE) / DURATION) > 0.6f) {
          paint.setAlpha((int) (PAINT_ALPHA - ((PAINT_ALPHA) * (((float) timerEmpty * FRAME_RATE)
              / (durationEmpty)))));
        } else {
          paint.setAlpha(PAINT_ALPHA);
        }
      } else {
        paint.setAlpha(
            (int) (PAINT_ALPHA - ((PAINT_ALPHA) * (((float) timer * FRAME_RATE) / DURATION))));
      }

      timer++;
    }
  }

  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    WIDTH = w;
    HEIGHT = h;

    scaleAnimation = new ScaleAnimation(1.0f, zoomScale, 1.0f, zoomScale, w / 2, h / 2);
    scaleAnimation.setDuration(zoomDuration);
    scaleAnimation.setRepeatMode(Animation.REVERSE);
    scaleAnimation.setRepeatCount(1);
  }

  public void animateRipple(MotionEvent event) {
    createAnimation(event.getX(), event.getY());
  }

  public void animateRipple(final float x, final float y) {
    createAnimation(x, y);
  }

  private void createAnimation(final float x, final float y) {
    if (!animationRunning) {
      if (hasToZoom) view.startAnimation(scaleAnimation);

      radiusMax = Math.max(WIDTH, HEIGHT);

      if (rippleType != 2) radiusMax /= 2;

      radiusMax -= ripplePadding;

      if (isCentered || rippleType == 1) {
        this.x = view.getMeasuredWidth() / 2;
        this.y = view.getMeasuredHeight() / 2;
      } else {
        this.x = x;
        this.y = y;
      }

      animationRunning = true;

      if (rippleType == 1 && originBitmap == null) originBitmap = view.getDrawingCache(true);

      view.invalidate();
    }
  }

  public void onTouchEvent(MotionEvent event) {
    if (gestureDetector.onTouchEvent(event)) {
      animateRipple(event);
      sendClickEvent(false);
    }
  }



  private void sendClickEvent(final Boolean isLongClick) {
    if (view.getParent() instanceof ListView) {
      final int position = ((ListView) view.getParent()).getPositionForView((View) view);
      final long id = ((ListView) view.getParent()).getItemIdAtPosition(position);
      if (isLongClick) {
        if (((ListView) view.getParent()).getOnItemLongClickListener() != null) {
          ((ListView) view.getParent()).getOnItemLongClickListener()
              .onItemLongClick(((ListView) view.getParent()), (View) view, position, id);
        }
      } else {
        if (((ListView) view.getParent()).getOnItemClickListener() != null) {
          ((ListView) view.getParent()).getOnItemClickListener()
              .onItemClick(((ListView) view.getParent()), (View) view, position, id);
        }
      }
    }
  }

  private Bitmap getCircleBitmap(final int radius) {
    final Bitmap output = Bitmap.createBitmap(originBitmap.getWidth(), originBitmap.getHeight(),
        Bitmap.Config.ARGB_8888);
    final Canvas canvas = new Canvas(output);
    final Paint paint = new Paint();
    final Rect rect =
        new Rect((int) (x - radius), (int) (y - radius), (int) (x + radius), (int) (y + radius));

    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    canvas.drawCircle(x, y, radius, paint);

    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(originBitmap, rect, rect, paint);

    return output;
  }
}
