package com.acm.dijkstrasden;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.GestureDetector;
import android.view.View;
import android.view.WindowManager;

/**
 * Implements the swipe gestures used on the home screen. 
 */
public class TouchView extends View implements GestureDetector.OnGestureListener {
    private Drawable mGfx;    
    private GestureDetector gestureDetector;

    public TouchView(Context context) {
        this(context, null, 0);
    }

    public TouchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mGfx = context.getResources().getDrawable(R.drawable.dijkstra);
        
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        
        Display display = wm.getDefaultDisplay();
        display.getMetrics(metrics);

        double scale = 0.8;

        int pad = metrics.widthPixels - (int)(mGfx.getIntrinsicWidth()*scale);

        mGfx.setBounds(pad/2, 0, pad/2+(int)(mGfx.getIntrinsicWidth()*scale), (int)(mGfx.getIntrinsicHeight()*scale));
        
        gestureDetector = new GestureDetector(this);
        //gestureDetector.setOnDoubleTapListener((OnDoubleTapListener) context);
        

        //mDetector = VersionedGestureDetector.newInstance(context, new GestureCallback());
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        //mDetector.onTouchEvent(ev);
//        return true;
//    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.save();
//        canvas.translate(mPosX, mPosY);
//        canvas.scale(mScaleFactor, mScaleFactor);
        mGfx.draw(canvas);
//        canvas.restore();
    }

//    private class GestureCallback implements VersionedGestureDetector.OnGestureListener {
//        public void onDrag(float dx, float dy) {
//            mPosX += dx;
//            mPosY += dy;
//            invalidate();
//        }
//
//        public void onScale(float scaleFactor) {
//            mScaleFactor *= scaleFactor;
//
//            // Don't let the object get too small or too large.
//            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
//
//            invalidate();
//        }
//    }

	public boolean onDown(MotionEvent e) {
		Log.d("Gesture", "onDown");

		return true;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d("Gesture", "onFling");

		return true;
	}

	public void onLongPress(MotionEvent e) {

		Log.d("Gesture", "onLongPress");
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.d("Gesture", "onScroll");

		return true;
	}

	public void onShowPress(MotionEvent e) {
		Log.d("Gesture", "onShowPress");

		
	}

	public boolean onSingleTapUp(MotionEvent e) {

		Log.d("Gesture", "onSingleTapUp");
		return true;
	}
}
