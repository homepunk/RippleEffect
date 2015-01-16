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
 *
 */

package com.andexert.library;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 *
 */
public class RippleLinearLayout extends LinearLayout implements RippleView {

  private RippleDelegate delegate;

  public RippleLinearLayout(Context context) {
    super(context);
  }

  public RippleLinearLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }


  private void init(final Context context, final AttributeSet attrs) {
    if (isInEditMode()) return;
    delegate = new RippleDelegate(this, attrs);
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
    delegate.draw(canvas);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    delegate.onSizeChanged(w, h, oldw, oldh);
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    delegate.onTouchEvent(event);
    return super.onTouchEvent(event);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    this.onTouchEvent(event);
    return super.onInterceptTouchEvent(event);
  }
}
