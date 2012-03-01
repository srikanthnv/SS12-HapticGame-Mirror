package com.acm.dijkstrasden;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Vibrator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

/**
 * Primary game loop - implements a Surface onto which the game is rendered.  
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback,
		SensorEventListener {

	/** Object representing NodeMan */
	Player playerObj = new Player(new int[2], OrientationEnum.ORIENT_EAST,
			OrientationEnum.ORIENT_EAST);

	/** Object controlling notifications (vibrate, sound, etc) */
	Notification notificationObj = new Notification(getContext());

	private boolean tSInputAvail;

	private SensorManager mSensorManager;
	private Sensor mSensor;
	private PowerManager pm;
	private PowerManager.WakeLock wakeLock;

	/** The thread that actually draws the animation */
	private AnimationThread thread;

	/** Object controlling levels layouts etc */
	private Levels levelsObj;
	
	/** Time at which level was started */
	private long levelStartTimeMs;
	
	/** Setup and start the game */
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		
		notificationObj.mVibrator = (Vibrator) getContext().getSystemService(
				Context.VIBRATOR_SERVICE);

		pm = (PowerManager) getContext()
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"My wakelook");

		// This will make the screen and power stay on
		wakeLock.acquire();

		// Get an instance of the SensorManager
		mSensorManager = (SensorManager) getContext().getSystemService(
				Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_FASTEST);

		// set up levels
		
		///Set up game levels
		if(0 == MyProperties.getInstance().levelType) {
			levelsObj = new Levels(0, 3);
			levelsObj.setup_levels_game();
			levelsObj.readMaze(playerObj, 0);
			
		}
		//Set up tutorial levels
		else {
			levelsObj = new Levels(0, 2);
			levelsObj.setup_levels_tutorial();
			levelsObj.readMaze(playerObj, 0);
		}
		
		levelStartTimeMs = System.currentTimeMillis();
		notificationObj.do_notify = NotifyTypeEnum.NOTIFY_LEVELSTART;
		notificationObj.giveFeedback();

		// create thread only; it's started in surfaceCreated()
		thread = new AnimationThread(holder);

	}

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Do Nothing for now
	}
	
	/** Set the game to pause when screen is removed. Called from Activity */
	public void pause() {
		wakeLock.release();
		notificationObj.pause();
	}

	/** Handle sensor change and set orientation */
	public void onSensorChanged(SensorEvent event) {
		float x_ang = event.values[0];

		if (x_ang > 170 && x_ang < 190) {
			playerObj.sensorOrientation = OrientationEnum.ORIENT_NORTH;
			//Log.d("Game", "" + x_ang);
		}
		else if (x_ang > 260 && x_ang < 280) {
			playerObj.sensorOrientation = OrientationEnum.ORIENT_EAST;
			//Log.d("Game", "" + x_ang);
		}
		else if (x_ang > 350 || x_ang < 10) {
			playerObj.sensorOrientation = OrientationEnum.ORIENT_SOUTH;
			//Log.d("Game", "" + x_ang);
		}
		else if (x_ang > 80 && x_ang < 100){
			playerObj.sensorOrientation = OrientationEnum.ORIENT_WEST;
			//Log.d("Game", "" + x_ang);
		}
	}

	/** Class to handle the animations being drawn on screen */
	class AnimationThread extends Thread {

		/* Are we running ? */
		private boolean mRun;

		/* Handle to the surface manager object we interact with */
		private SurfaceHolder mSurfaceHolder;

		Paint paint = new Paint();
		Bitmap rotated0;
		Bitmap rotated90;
		Bitmap rotated180;
		Bitmap rotated270;
		Bitmap bmp;

		/** Animation thread constructor precreates bitmaps for later use */
		public AnimationThread(SurfaceHolder surfaceHolder) {
			mSurfaceHolder = surfaceHolder;
			tSInputAvail = false;
			
		    Matrix matrix = new Matrix();
		    
		    rotated0 = BitmapFactory.decodeResource(getResources(), R.drawable.arrow);
		    
		    matrix.reset();
		    matrix.postRotate(90);
		    rotated90 = Bitmap.createBitmap(rotated0, 0, 0, rotated0.getWidth(), rotated0.getHeight(), matrix, true);
		    matrix.reset();
		    matrix.postRotate(180);
		    rotated180 = Bitmap.createBitmap(rotated0, 0, 0, rotated0.getWidth(), rotated0.getHeight(), matrix, true);
		    matrix.reset();
		    matrix.postRotate(270);
		    rotated270 = Bitmap.createBitmap(rotated0, 0, 0, rotated0.getWidth(), rotated0.getHeight(), matrix, true);
			
		}

		/**
		 * The actual game loop
		 */
		@Override
		public void run() {
			while (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						calcState();
						playerObj.updatePlayerPosition();
						notificationObj.giveFeedback();
						updateUI(c);
					}
				} finally {
					if (c != null) {
						mSurfaceHolder.unlockCanvasAndPost(c);
					}
				}
			}
		}

		/** Draw the map on screen */
		private void updateUI(Canvas canvas) {
			canvas.drawColor(Color.BLACK);

			/*
			 * Clear the background
			 */
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);

			/*
			 * Draw nodes
			 */
			for (int i = 0; i < levelsObj.num_nodes; i++) {
				paint.setColor(Color.RED);
				paint.setAntiAlias(true);
				canvas.drawCircle(levelsObj.PositionArray[i][0],
						levelsObj.PositionArray[i][1], 26, paint);
				
				paint.setColor(Color.WHITE);
				paint.setAntiAlias(true);
				canvas.drawCircle(levelsObj.PositionArray[i][0],
						levelsObj.PositionArray[i][1], 24, paint);
			}

			/*
			 * Draw links
			 */
			for (int i = 0; i < levelsObj.num_nodes; i++)
				for (int j = 0; j < levelsObj.num_nodes; j++)
					if (levelsObj.LinkArray[i][j] == 1) {
						
						paint.setStrokeWidth(16);
						canvas.drawLine(levelsObj.PositionArray[i][0],
								levelsObj.PositionArray[i][1],
								levelsObj.PositionArray[j][0],
								levelsObj.PositionArray[j][1], paint);
					}

			/*
			 * Draw destination
			 */
			paint.setColor(Color.RED);
			canvas.drawCircle(levelsObj.PositionArray[levelsObj.dest_node][0],
					levelsObj.PositionArray[levelsObj.dest_node][1], 26, paint);
			
			paint.setColor(Color.YELLOW);
			canvas.drawCircle(levelsObj.PositionArray[levelsObj.dest_node][0],
					levelsObj.PositionArray[levelsObj.dest_node][1], 24, paint);

			/*
			 * Draw player character
			 */
			/*canvas.drawCircle(playerObj.playerPosition[0],
					playerObj.playerPosition[1], 10, paint);*/
			switch (playerObj.playerOrientation) {
			case ORIENT_NORTH:
				bmp = rotated0;
				break;
			case ORIENT_SOUTH:
				bmp = rotated180;
				break;
			case ORIENT_EAST:
				bmp = rotated90;
				break;
			case ORIENT_WEST:
				bmp = rotated270;
				break;
			}
			
	        //canvas.drawColor(Color.BLACK);
	        canvas.drawBitmap(bmp, playerObj.playerPosition[0] - bmp.getWidth()/2, playerObj.playerPosition[1] - bmp.getHeight()/2, null);
		}

		/**
		 * Update the state of the game - collision detection, level change notification, etc
		 */
		private void calcState() {

			switch (playerObj.playerState) {
			case STATE_RUNNING:
				for (int i = 0; i < levelsObj.num_nodes; i++) {
					if (playerObj.playerPosition[0] == levelsObj.PositionArray[i][0]
							&& playerObj.playerPosition[1] == levelsObj.PositionArray[i][1]) {

						playerObj.playerNode = i;
						if (playerObj.playerNode == levelsObj.dest_node) {
							notificationObj
									.sayScore((System.currentTimeMillis() - levelStartTimeMs) / 1000);
							playerObj.playerState = PlayerStateEnum.STATE_LEVELDONE;
							notificationObj.do_notify = NotifyTypeEnum.NOTIFY_LEVELDONE;
						} else {
							playerObj.playerState = PlayerStateEnum.STATE_IDLE;
							notificationObj.do_notify = NotifyTypeEnum.NOTIFY_ATNODE;
						}

						break;
					}
				}
				break;
			case STATE_IDLE:
				// Check if we received a command to move
				if (tSInputAvail == true) {
					tSInputAvail = false;
					// if we did, then ensure we are on a path between two nodes
					if (levelsObj.OrientationArray[playerObj.playerNode][playerObj.playerOrientation
							.ordinal()] == true) {
						playerObj.playerState = PlayerStateEnum.STATE_RUNNING;
						notificationObj.do_notify = NotifyTypeEnum.NOTIFY_MOVE;
					} else {
						notificationObj.do_notify = NotifyTypeEnum.NOTIFY_BUMP;
					}

				}
				if (playerObj.sensorOrientation != playerObj.playerOrientation) {
					playerObj.playerOrientation = playerObj.sensorOrientation;
					notificationObj.do_notify = NotifyTypeEnum.NOTIFY_ORIENT;
				}
				break;
			case STATE_LEVELDONE:
				// Check if we received a command to move to the next level
				if (tSInputAvail == true) {
					tSInputAvail = false;
					levelsObj.currlevel++;

					if (levelsObj.currlevel >= levelsObj.numlevels) {
						// Game over!
						notificationObj.do_notify = NotifyTypeEnum.NOTIFY_GAMEDONE;
						break;
					}
					/*
					 * Load the next level map into memory.
					 */
					notificationObj.do_notify = NotifyTypeEnum.NOTIFY_LEVELSTART;
					levelsObj.readMaze(playerObj, levelsObj.currlevel);
					playerObj.playerState = PlayerStateEnum.STATE_IDLE;
				}
				if (playerObj.sensorOrientation != playerObj.playerOrientation) {
					playerObj.playerOrientation = playerObj.sensorOrientation;
					notificationObj.do_notify = NotifyTypeEnum.NOTIFY_ORIENT;
				}
				break;

			}
		}

		/**
		 * stop/pause the game loop - unused.
		 */
		public void setRunning(boolean b) {
			mRun = b;
		}
	}

	/*
	 * Obligatory method to implement SurfaceHolder.Callback
	 */

	/* Callback invoked when the surface dimensions change. */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/*
	 * Callback invoked when the Surface has been created and is ready to be
	 * used.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
	}

	/*
	 * Callback invoked when the Surface has been destroyed and must no longer
	 * be tSInputAvail. WARNING: after this method returns, the Surface/Canvas
	 * must never be tSInputAvail again!
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		// we have to tell thread to shut down & wait for it to finish, or else
		// it might touch the Surface after we return and explode
		boolean retry = true;
		thread.setRunning(false);
		while (retry) {
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * Called when a touchscreen event is detected
	 * 
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	public boolean onTouchEvent(MotionEvent event) {

		/*
		 * Ignore everything except touch down action
		 */
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			tSInputAvail = true;
			Log.d("Game", "Touch event called");
			break;
		}

		/*
		 * Debounce touch-down.
		 */

		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

}
