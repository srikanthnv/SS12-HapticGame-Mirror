package com.acm.dijkstrasden;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;

/**
 * Activity for the game itself - creates a GameView for playing the game. 
 */
public class GameActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 
        
    }

    @Override
	protected void onPause() {

	    super.onPause();
	    GameView gv = (GameView) findViewById(R.id.gameview);
	    gv.pause();
	}
	
    @Override
	protected void onResume() {

	    super.onResume();
	}
	
    
}
