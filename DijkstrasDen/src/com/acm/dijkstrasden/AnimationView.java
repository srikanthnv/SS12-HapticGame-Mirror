package com.acm.dijkstrasden;

import java.util.Scanner;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.util.Log;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.os.PowerManager;

class AnimationView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener{
	
	public enum Orientation
	{
		ORIENT_NORTH,
		ORIENT_SOUTH,
		ORIENT_EAST,
		ORIENT_WEST
	};

	public enum PlayerState
	{
		STATE_IDLE,
		STATE_RUNNING,
		STATE_LEVELDONE
	};
    
	private boolean touched;
	Vibrator mVibrator;
	long vib_pattern_bump[] = {0,100,50,100,50,100};
	long vib_pattern_atnode[] = {0,30,30,40};
	long vib_pattern_leveldone[] = {0,50,100,50,50,50,50,50,50,100 };
	//long vib_pattern_orient[] = {0,100};
	
	/** Sound variables */
	private SoundPool sounds;
	private int sBump;
	private int sNode;
	private int sOrient;
	private int sMove, stopMove;
	private int sLevel;

	
	public enum notify_type {
		NOTIFY_BUMP,
		NOTIFY_ATNODE,
		NOTIFY_ORIENT,
		NOTIFY_MOVE,
		NOTIFY_LEVELDONE,
		NOTIFY_NONE
	}

    private SensorManager mSensorManager;
    private Sensor mSensor;
    PowerManager pm;
    PowerManager.WakeLock wakeLock;
	
    public void onAccuracyChanged(Sensor arg0, int arg1) {
		// Do Nothing for now
	}

    public void onSensorChanged(SensorEvent event) {
		float x_ang = event.values[0];
		
		
		if(x_ang > 160 && x_ang < 200)
			sensorOrientation = Orientation.ORIENT_NORTH;
		else if(x_ang > 250 && x_ang < 290)
			sensorOrientation = Orientation.ORIENT_EAST;
		else if(x_ang > 340 || x_ang < 20)
			sensorOrientation = Orientation.ORIENT_SOUTH;
		else if(x_ang > 70 && x_ang < 110)
			sensorOrientation = Orientation.ORIENT_WEST;
	}
	
	class AnimationThread extends Thread {
    	
		/** Are we running ? */
    	private boolean mRun;
    	        
        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;

        Paint paint = new Paint();
                
        public AnimationThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
            touched = false;
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
                    	updatePlayerPosition();
                    	giveFeedback();
						updateUI(c);
                    }
                }finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    	
        private void updateUI(Canvas canvas) {
        	canvas.drawColor(Color.BLACK);
            
        	paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

        	for(int i = 0; i < num_nodes; i++)
        		canvas.drawCircle(PositionArray[i][0], PositionArray[i][1], 10, paint);
        	
        	for(int i = 0; i < num_nodes; i++)
        		for(int j = 0; j < num_nodes; j++)
        			if(LinkArray[i][j] == 1)  {
        				canvas.drawLine(PositionArray[i][0], PositionArray[i][1], PositionArray[j][0], PositionArray[j][1], paint);
        			}
        	
        	paint.setColor(Color.YELLOW);
        	canvas.drawCircle(PositionArray[dest_node][0], PositionArray[dest_node][1], 10, paint);
        	
        	paint.setColor(Color.GREEN);
        	canvas.drawCircle(playerPosition[0], playerPosition[1], 10, paint);
        	int or_x = playerPosition[0];
        	int or_y = playerPosition[1];
        	switch(playerOrientation)
        	{
        	case ORIENT_NORTH:
        		or_y -= 10;
        		break;
        	case ORIENT_SOUTH:
        		or_y += 10;
        		break;
        	case ORIENT_EAST:
        		or_x += 10;
        		break;
        	case ORIENT_WEST:
        		or_x -= 10;
        		break;
        		
        	}
        	paint.setColor(Color.RED);
        	canvas.drawCircle(or_x, or_y, 5, paint);
        	
        	//playerPosition[0] += 1;
        	
        	try {
				sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


        private void updatePlayerPosition() {
            //long now = System.currentTimeMillis();
        	
        	switch(playerState)
        	{
        	case STATE_RUNNING:
        		switch(playerOrientation)
        		{
        		case ORIENT_NORTH:
        			playerPosition[1] -= 1;
        			break;
        		case ORIENT_SOUTH:
        			playerPosition[1] += 1;
        			break;
        		case ORIENT_EAST:
        			playerPosition[0] += 1;        			
        			break;
        		case ORIENT_WEST:
        			playerPosition[0] -= 1;
        			break;
        		}
        		break;
        	}
        	
        }
        
        /**
         * Updates the state of the player
         */
        private void calcState() {
        	
        	switch(playerState)
        	{
        	case STATE_RUNNING:
	        	for(int i = 0; i < num_nodes; i++)
	        	{
	        		if(playerPosition[0] == PositionArray[i][0] && playerPosition[1] == PositionArray[i][1])
	        		{
	        			
	        			playerNode = i;
	        			if(playerNode == dest_node)
	        			{
	        				playerState = PlayerState.STATE_LEVELDONE;
	        				do_notify = notify_type.NOTIFY_LEVELDONE;	        				
	        			}
	        			else
	        			{
	        				playerState = PlayerState.STATE_IDLE;
	        				do_notify = notify_type.NOTIFY_ATNODE;
	        			}
	        			
	        			touched = false;
	        			break;
	        		}
	        	}
	        	break;
        	case STATE_IDLE:
        		//Check if we received a command to move
        		if(touched == true)
        		{
        			touched = false;
        			//if we did, then ensure we are on a path between two nodes
        			if(OrientationArray[playerNode][playerOrientation.ordinal()] == true)
        			{
        				playerState = PlayerState.STATE_RUNNING;
        				do_notify = notify_type.NOTIFY_MOVE;
        			}
        			else 
        			{
        				do_notify = notify_type.NOTIFY_BUMP;
        			}
        			
        			
        		}
        		if(sensorOrientation != playerOrientation)
        		{
        			playerOrientation = sensorOrientation;
        			do_notify = notify_type.NOTIFY_ORIENT;
        		}
        		break;
        	case STATE_LEVELDONE:
        		//Check if we received a command to move to the next level
        		if(touched == true)
        		{
        			touched = false;
        			currlevel++;
        			if(currlevel >= numlevels)
        			{
        				//Game over!
        				break;
        			}
        			readMaze(currlevel);
        			playerState = PlayerState.STATE_IDLE;
        		}
        		if(sensorOrientation != playerOrientation)
        		{
        			playerOrientation = sensorOrientation;
        			do_notify = notify_type.NOTIFY_ORIENT;
        		}
        		break;

        	}
        }
        
        /**
         * Provide the necessary feedback
         */
        private void giveFeedback() {
        	if(do_notify != notify_type.NOTIFY_NONE)
        	{
        		notifyEvent(do_notify);
        		do_notify = notify_type.NOTIFY_NONE;
        	}
        }

        /**
         * So we can stop/pause the game loop
         */
        public void setRunning(boolean b) {
            mRun = b;
        }      
    }
	
	/* player properties */
	private int[] playerPosition = new int[2];
	private int playerNode;
	
	private Orientation playerOrientation = Orientation.ORIENT_EAST;
	private Orientation sensorOrientation = Orientation.ORIENT_EAST;
	private PlayerState playerState;

	private int num_nodes;
	private int num_links;
	
	private int dest_node;

	private int[][] LinkArray;
	private int[][] PositionArray;
	private boolean[][] OrientationArray;
	
	private int currlevel = 1;
	private int numlevels = 2;
	
	private String[] level = new String[2];
	
    /**
     * Read the input maze
     */
    private void readMaze(int mylevel) {
	    //int index;
	    Scanner scanner = new Scanner(level[mylevel]).useDelimiter(" ");
	    //Log.i("readMaze", "Entering.");
	    
	    //index = map.indexOf("Numnodes ");
	    
	    if(scanner.next().equalsIgnoreCase("numnodes") != true) {
    		Log.e("readMaze", "Didn't find Numnodes! Something wrong.");
    		return;
	    }
	    num_nodes = scanner.nextInt();
	    LinkArray = new int[num_nodes][num_nodes];
	    PositionArray = new int[num_nodes][2];
	    OrientationArray = new boolean[num_nodes][4]; 
	    
	    //read node positions from map
	    for (int i = 0; i < num_nodes; i++)
	    {
	    	//index = map.indexOf(subString, start)("Numnodes ");
	    	if(scanner.next().equalsIgnoreCase("node") != true)
	    	{
	    		Log.e("readMaze", "Didn't find a node! Something wrong.");
	    		break;
	    	}
	    	scanner.nextInt();
	    	PositionArray[i][0] = scanner.nextInt();
	    	PositionArray[i][1] = scanner.nextInt();
	    }
	    
	    //read links from map
	    if(scanner.next().equalsIgnoreCase("NumLinks") != true) {
    		Log.e("readMaze", "Didn't find NumLinks! Something wrong.");
    		return;
	    }
	    
	    for(int i = 0; i < num_nodes; i++)
	    	for(int j = 0; j < 4; j++)
	    		OrientationArray[i][j] = false;

	    for(int i = 0; i < num_nodes; i++)
	    	for(int j = 0; j < num_nodes; j++)
	    		LinkArray[i][j] = 0;
 
	    num_links = scanner.nextInt();
	    
	    for (int i = 0; i < num_links; i++)
	    {
		    if(scanner.next().equalsIgnoreCase("link") != true) {
	    		Log.e("readMaze", "Didn't find a link! Something wrong.");
	    		break;
		    }
		    int start = scanner.nextInt();
		    int stop = scanner.nextInt();
		    Log.i("readMaze", "Filling at " + start + "," + stop);
    		LinkArray[start][stop] = LinkArray[stop][start] = 1;
    		
    		int xdiff = PositionArray[start][0] - PositionArray[stop][0];
    		int ydiff = PositionArray[start][1] - PositionArray[stop][1];
    		
    		if(xdiff < 0 && ydiff == 0) {
    			OrientationArray[start][Orientation.ORIENT_EAST.ordinal()] = true;    			
    			OrientationArray[stop][Orientation.ORIENT_WEST.ordinal()] = true;
    		} else if(xdiff > 0 && ydiff == 0) {
    			OrientationArray[start][Orientation.ORIENT_WEST.ordinal()] = true;
    			OrientationArray[stop][Orientation.ORIENT_EAST.ordinal()] = true;
    		} else if(xdiff == 0 && ydiff < 0) {
    			OrientationArray[start][Orientation.ORIENT_SOUTH.ordinal()] = true;
    			OrientationArray[stop][Orientation.ORIENT_NORTH.ordinal()] = true;
    		} else {
    			OrientationArray[start][Orientation.ORIENT_NORTH.ordinal()] = true;
    			OrientationArray[stop][Orientation.ORIENT_SOUTH.ordinal()] = true;
    		}
	    }
	    
	    if(scanner.next().equalsIgnoreCase("player") != true) {
    		Log.e("readMaze", "Didn't find player start position! Something wrong.");
    		return;
	    }
	    
	    playerNode = scanner.nextInt();
	    playerPosition[0] = PositionArray[playerNode][0]; 
	    playerPosition[1] = PositionArray[playerNode][1];
	    
	    if(scanner.next().equalsIgnoreCase("destination") != true) {
    		Log.e("readMaze", "Didn't find player destination node! Something wrong.");
    		return;
	    }
	    
	    dest_node = scanner.nextInt();
	    
	    playerState = PlayerState.STATE_IDLE;
	    
	    //Log.i("readMaze", "Exiting.");
    }

    /** The thread that actually draws the animation */
    private AnimationThread thread;
    
    public AnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        
        mVibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
		
        pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My wakelook");
		// This will make the screen and power stay on
		// This will release the wakelook after 1000 ms
		wakeLock.acquire();
       
	    // Get an instance of the SensorManager
        mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        
        //Set up sounds
        sounds = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
        sBump = sounds.load(context, R.raw.bump, 1);
        sNode = sounds.load(context, R.raw.node, 1);
        sOrient = sounds.load(context, R.raw.orient, 1);
        sMove = sounds.load(context, R.raw.move, 1);
        sLevel = sounds.load(context, R.raw.level, 1);
        
        //set up levels
        setup_levels();

        readMaze(currlevel);
        
        // create thread only; it's started in surfaceCreated()
        thread = new AnimationThread(holder);

    }
    
    
    private void setup_levels() {
    	level[0] = "Numnodes 5 "+
    			"Node 0 20 100 " + 
    			"Node 1 100 100 " +
    			"Node 2 100 20 " +
    			"Node 3 100 200 " +
    			"Node 4 200 20 " +
    			"NumLinks 4 " +
    			"Link 0 1 " +
    			"Link 1 2 " +
    			"Link 1 3 " +
    			"Link 2 4 " + 
    			"Player 0 " +
    			"Destination 4 ";
    						
    				level[1] = "Numnodes 10 "+
    			"Node 0 80 240 " + 
    			"Node 1 160 240 " +
    			"Node 2 160 160 " +
    			"Node 3 160 80 " +
    			"Node 4 320 80 " +
    			"Node 5 320 160 " +
    			"Node 6 320 240 " +
    			"Node 7 320 320 " +
    			"Node 8 400 240 " +
    			"Node 9 400 160 " +
    			"NumLinks 12 " +
    			"Link 0 1 " +
    			"Link 1 2 " +
    			"Link 1 6 " +
    			"Link 2 3 " + 
    			"Link 2 5 " +
    			"Link 3 4 " +
    			"Link 4 5 " +
    			"Link 5 6 " +
    			"Link 5 9 " +
    			"Link 6 8 " +
    			"Link 6 7 " +
    			"Link 8 9 " +
    			"Player 0 " +
    			"Destination 9 ";		
	}

	/**
     * Obligatory method that belong to the:implements SurfaceHolder.Callback
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
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
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

	public boolean onTouchEvent(MotionEvent event) {
		
		//playerState = PlayerState.STATE_RUNNING;
		
		switch(event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			touched=true;
			Log.d("Game", "Touch event called");
			break;
		}
		
		
		try {
			Thread.sleep(16);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}

	/* 
	 * Notification of events (feedback with vibrator and sound) 
	 * 
	 * */
	private notify_type do_notify = notify_type.NOTIFY_NONE;
	
    private void notifyEvent(notify_type type) {
    	switch(type) {
    	case NOTIFY_BUMP:
    		mVibrator.vibrate(vib_pattern_bump, -1);
    		sounds.play(sBump, 1.0f, 1.0f, 0, 0, 1.0f);
    		try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		break;
    	case NOTIFY_ATNODE:
    		Log.d("Game", "Stopping movement");
    		sounds.stop(stopMove);
    		mVibrator.vibrate(vib_pattern_atnode, -1);
    		sounds.play(sNode, 1.0f, 1.0f, 0, 0, 1.0f);
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		break;
    	case NOTIFY_LEVELDONE:
    		Log.d("Game", "Level done");
    		sounds.stop(stopMove);
    		mVibrator.vibrate(vib_pattern_leveldone, -1);
    		sounds.play(sLevel, 1.0f, 1.0f, 0, 0, 1.0f);
    		try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    		break;
    	case NOTIFY_ORIENT:
    		sounds.play(sOrient, 1.0f, 1.0f, 0, 0, 1.0f);
    		break;
    	case NOTIFY_MOVE:
    		stopMove = sounds.play(sMove, 1.0f, 1.0f, 0, -1, 1.0f);
    		break;
    		
    	}
    }
	
}
