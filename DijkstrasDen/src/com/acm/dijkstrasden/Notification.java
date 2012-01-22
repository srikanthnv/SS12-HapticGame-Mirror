package com.acm.dijkstrasden;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;


public class Notification {
	public Vibrator mVibrator;
	public long[] vib_pattern_bump;
	public long[] vib_pattern_atnode;
	public long[] vib_pattern_leveldone;
	/** Sound variables */
	public SoundPool sounds;
	public int sBump;
	public int sNode;
	public int sOrient;
	public int sMove;
	public int stopMove;
	public int sLevel;
	public NotifyTypeEnum do_notify;

	public Notification(Context context, long[] vib_pattern_bump, long[] vib_pattern_atnode,
			long[] vib_pattern_leveldone, NotifyTypeEnum do_notify) {
		this.vib_pattern_bump = vib_pattern_bump;
		this.vib_pattern_atnode = vib_pattern_atnode;
		this.vib_pattern_leveldone = vib_pattern_leveldone;
		this.do_notify = do_notify;
		
		// Set up sounds
		sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		sBump = sounds.load(context, R.raw.bump, 1);
		sNode = sounds.load(context, R.raw.node, 1);
		sOrient = sounds.load(context, R.raw.orient, 1);
		sMove = sounds.load(context, R.raw.move, 1);
		sLevel = sounds.load(context, R.raw.level, 1);
	}
	
	/**
	 * Provide the necessary feedback
	 */
	public void giveFeedback() {
		if (do_notify != NotifyTypeEnum.NOTIFY_NONE) {
			notifyEvent(do_notify);
			do_notify = NotifyTypeEnum.NOTIFY_NONE;
		}
	}

	private void notifyEvent(NotifyTypeEnum type) {
		switch (type) {
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