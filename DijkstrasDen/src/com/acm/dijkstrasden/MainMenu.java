package com.acm.dijkstrasden;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.GestureDetector;


/**
 * Activity for the first menu visible when the game is launched. 
 * The main menu allows users to choose either a tutorial or to 
 * actually play the game. 
 * 
 * The menu does not rely on absolute touch locations. Instead it 
 * uses swipe gestures to figure out the user's choices.
 * 
 * BLV users are also aided by the TTS - the main menu "speaks" 
 * instructions when it is launched. Users can repeat instructions by long-press
 * on the main menu.
 */
public class MainMenu extends Activity 
                      implements GestureDetector.OnGestureListener,
                                 GestureDetector.OnDoubleTapListener,
                                 TextToSpeech.OnInitListener {
	
	private GestureDetector gesturedetector = null;
	public Vibrator mVibrator;
	private TextToSpeech mTts;

		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /**
         * Uses Vibrator to indicate command registration
         */
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        
        gesturedetector = new GestureDetector(this, this);
        gesturedetector.setOnDoubleTapListener(this);
        
        TouchView view = new TouchView(this);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(view);
        mTts = new TextToSpeech(this.getApplicationContext(), this);
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
            return gesturedetector.onTouchEvent(event);
    }
	
	public boolean onDoubleTap(MotionEvent e) {
		//Log.d("Gesture", "onDoubleTap");
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
		//Log.d("Gesture", "onDoubleTapEvent");
		return false;
	}

	public boolean onSingleTapConfirmed(MotionEvent e) {
		//Log.d("Gesture", "onSingleTapConfirmed");
		return false;
	}

	public boolean onDown(MotionEvent e) {
		//Log.d("Gesture", "onDown");
		return false;
	}
	
	private static final int low_move_thresh = 300;
	private static final int high_move_thresh = 800;

	/**
	 * Handles the swipe gesture movements needed to navigate the menu
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Log.d("Gesture", "onFling " + velocityX + "," + velocityY);
		
		if(velocityX > high_move_thresh && velocityY < low_move_thresh && velocityY > -low_move_thresh) {
			mTts.stop();
			Log.d("Gesture", "Fling Right");
			mVibrator.vibrate(30);
			MyProperties.getInstance().levelType = 0;
			startActivity(new Intent(this, GameActivity.class));
		}
		if(velocityX < -high_move_thresh  && velocityY < low_move_thresh && velocityY > -low_move_thresh) {
			mTts.stop();
			Log.d("Gesture", "Fling Left");
			mVibrator.vibrate(30);
			MyProperties.getInstance().levelType = 1;
			startActivity(new Intent(this, GameActivity.class));
		}
		if(velocityY > high_move_thresh && velocityX < low_move_thresh && velocityX > -low_move_thresh) {
			mTts.stop();
			Log.d("Gesture", "Fling Down");
			mVibrator.vibrate(30);
            finish();

		}
		if(velocityY < -high_move_thresh  && velocityX < low_move_thresh && velocityX > -low_move_thresh)
			Log.d("Gesture", "Fling Up");
		return false;
	}

	/**
	 * Speak instructions on demand
	 */
	public void onLongPress(MotionEvent e) {
		giveInstructions();
		
	}

	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.d("Gesture", "onScroll x:" + distanceX + " y:" + distanceY);
		return false;
	}

	public void onShowPress(MotionEvent e) {
		//Log.d("Gesture", "onShowPress");
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		//Log.d("Gesture", "onSingleTapUp");
		return false;
	}
	
	
	void giveInstructions() {
		mTts.speak("Welcome to Dykstra's Den!",
                TextToSpeech.QUEUE_FLUSH,  null);		
		mTts.speak("Please hold your phone in landscape orientation.",
                TextToSpeech.QUEUE_ADD,  null);		
		mTts.speak("Swipe left to learn the game.",
                TextToSpeech.QUEUE_ADD,  null);		
		mTts.speak("Swipe right to play the game.",
                TextToSpeech.QUEUE_ADD,  null);		
		mTts.speak("Swipe down to exit.",
                TextToSpeech.QUEUE_ADD,  null);		
		mTts.speak("Remember, your player faces in the direction that you face.",
                TextToSpeech.QUEUE_ADD,  null);		
	}

	/**
	 * Speak instructions on startup
	 */
	public void onInit(int status) {
		giveInstructions();
		
	}
	
	@Override
    protected void onDestroy() {
		super.onDestroy();
		if(mTts != null)
		{
			mTts.stop();
			mTts.shutdown();
		}
	}
	
}
