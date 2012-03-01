package com.acm.dijkstrasden;

/**
 * Class representing the player's current state
 */
public class Player {
	public int[] playerPosition;
	public int playerNode;
	public OrientationEnum playerOrientation;
	public OrientationEnum sensorOrientation;
	public PlayerStateEnum playerState;

	/** Initialize class with startup details */
	public Player(int[] playerPosition, OrientationEnum playerOrientation,
			OrientationEnum sensorOrientation) {
		this.playerPosition = playerPosition;
		this.playerOrientation = playerOrientation;
		this.sensorOrientation = sensorOrientation;
	}
	
	/** Set whether the player has started, is still moving, has stopped, etc */
	public void updatePlayerPosition() {
		// long now = System.currentTimeMillis();

		switch (playerState) {
		case STATE_RUNNING:
			switch (playerOrientation) {
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
	
}