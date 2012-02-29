package com.acm.dijkstrasden;

import java.util.Scanner;

import android.util.Log;

public class Levels {
	public int num_nodes;
	public int num_links;
	public int dest_node;
	public int[][] LinkArray;
	public int[][] PositionArray;
	public boolean[][] OrientationArray;
	public int currlevel;
	public int numlevels;
	public String[] level;

	public Levels(int currlevel, int numlevels) {
		this.currlevel = currlevel;
		this.numlevels = numlevels;
		level = new String[numlevels];
	}

	void readMaze(Player playerObj, int mylevel) {
		// int index;
		Scanner scanner = new Scanner(level[mylevel]).useDelimiter(" ");
		// Log.i("readMaze", "Entering.");
	
		// index = map.indexOf("Numnodes ");
	
		if (scanner.next().equalsIgnoreCase("numnodes") != true) {
			Log.e("readMaze", "Didn't find Numnodes! Something wrong.");
			return;
		}
		num_nodes = scanner.nextInt();
		LinkArray = new int[num_nodes][num_nodes];
		PositionArray = new int[num_nodes][2];
		OrientationArray = new boolean[num_nodes][4];
	
		// read node positions from map
		for (int i = 0; i < num_nodes; i++) {
			// index = map.indexOf(subString, start)("Numnodes ");
			if (scanner.next().equalsIgnoreCase("node") != true) {
				Log.e("readMaze", "Didn't find a node! Something wrong.");
				break;
			}
			scanner.nextInt();
			PositionArray[i][0] = scanner.nextInt();
			PositionArray[i][1] = scanner.nextInt();
		}
	
		// read links from map
		if (scanner.next().equalsIgnoreCase("NumLinks") != true) {
			Log.e("readMaze", "Didn't find NumLinks! Something wrong.");
			return;
		}
	
		for (int i = 0; i < num_nodes; i++)
			for (int j = 0; j < 4; j++)
				OrientationArray[i][j] = false;
	
		for (int i = 0; i < num_nodes; i++)
			for (int j = 0; j < num_nodes; j++)
				LinkArray[i][j] = 0;
	
		num_links = scanner.nextInt();
	
		for (int i = 0; i < num_links; i++) {
			if (scanner.next().equalsIgnoreCase("link") != true) {
				Log.e("readMaze", "Didn't find a link! Something wrong.");
				break;
			}
			int start = scanner.nextInt();
			int stop = scanner.nextInt();
			Log.i("readMaze", "Filling at " + start + "," + stop);
			LinkArray[start][stop] = LinkArray[stop][start] = 1;
	
			int xdiff = PositionArray[start][0] - PositionArray[stop][0];
			int ydiff = PositionArray[start][1] - PositionArray[stop][1];
	
			if (xdiff < 0 && ydiff == 0) {
				OrientationArray[start][OrientationEnum.ORIENT_EAST.ordinal()] = true;
				OrientationArray[stop][OrientationEnum.ORIENT_WEST.ordinal()] = true;
			} else if (xdiff > 0 && ydiff == 0) {
				OrientationArray[start][OrientationEnum.ORIENT_WEST.ordinal()] = true;
				OrientationArray[stop][OrientationEnum.ORIENT_EAST.ordinal()] = true;
			} else if (xdiff == 0 && ydiff < 0) {
				OrientationArray[start][OrientationEnum.ORIENT_SOUTH.ordinal()] = true;
				OrientationArray[stop][OrientationEnum.ORIENT_NORTH.ordinal()] = true;
			} else {
				OrientationArray[start][OrientationEnum.ORIENT_NORTH.ordinal()] = true;
				OrientationArray[stop][OrientationEnum.ORIENT_SOUTH.ordinal()] = true;
			}
		}
	
		if (scanner.next().equalsIgnoreCase("player") != true) {
			Log.e("readMaze",
					"Didn't find player start position! Something wrong.");
			return;
		}
	
		playerObj.playerNode = scanner.nextInt();
		playerObj.playerPosition[0] = PositionArray[playerObj.playerNode][0];
		playerObj.playerPosition[1] = PositionArray[playerObj.playerNode][1];
	
		if (scanner.next().equalsIgnoreCase("destination") != true) {
			Log.e("readMaze",
					"Didn't find player destination node! Something wrong.");
			return;
		}
	
		dest_node = scanner.nextInt();
	
		playerObj.playerState = PlayerStateEnum.STATE_IDLE;
	
		// Log.i("readMaze", "Exiting.");
	}
	
	void setup_levels_tutorial() {
		level[0] = "Numnodes 2 " + 
				"Node 0 50 200 " + 
				"Node 1 200 200 " + 
				
				"NumLinks 1 " + 
				"Link 0 1 " +
				"Player 0 " + 
				"Destination 1 ";
	
		level[1] = "Numnodes 3 " + 
				"Node 0 50 200 " + 
				"Node 1 200 200 " + 
				"Node 1 200 50 " + 
				
				"NumLinks 2 " + 
				"Link 0 1 " +
				"Link 1 2 " +
				"Player 0 " + 
				"Destination 2 ";
	
	}	

	void setup_levels_game() {
		level[0] = "Numnodes 5 " + 
				"Node 0 50 200 " + 
				"Node 1 200 200 " + 
				"Node 2 200 50 " + 
				"Node 3 200 350 " + 
				"Node 4 350 50 " + 
				
				"NumLinks 4 " + 
				"Link 0 1 " + 
				"Link 1 2 " + 
				"Link 1 3 " + 
				"Link 2 4 " + 
				"Player 0 " + 
				"Destination 4 ";
	
		level[1] = "Numnodes 6 " + 
				"Node 0 50 200 " + 
				"Node 1 200 200 " + 
				"Node 2 350 200 " + 
				"Node 3 200 350 " + 
				"Node 4 350 350 " + 
				"Node 5 500 350 " + 

				"NumLinks 6 " + 
				"Link 0 1 " + 
				"Link 1 2 " + 
				"Link 1 3 " + 
				"Link 2 4 " + 
				"Link 3 4 " + 
				"Link 4 5 " + 

				"Player 0 " + "Destination 5 ";

		level[2] = "Numnodes 10 " + 
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
				
				"Player 0 " + "Destination 9 ";
	}
}