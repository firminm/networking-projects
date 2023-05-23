import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */
public class Node {

	public static final int INFINITY = 9999;

	int[] lkcost;				/*The link cost between node 0 and other nodes*/
	int nodename;           	/*Name of this node*/
	int[][] costs;				/*forwarding table, where index is destination node, [i][0] is cost to destination node and
  	  							  [i][1] is the next hop towards the destination node */

	int[][] graph;				/*Adjacency metric for the network, where (i,j) is cost to go from node i to j */
	ShortestPath t;             /*Have Dijkstra's implementation */
	int numNodes;
	Set<Integer> seenSeq;

	/* Class constructor */
	public Node() { }

	/* students to write the following two routines, and maybe some others */
	void rtinit(int nodename, int[] initial_lkcost) {
		this.seenSeq = new HashSet<>();

		this.numNodes = initial_lkcost.length;
		this.nodename = nodename;
		this.lkcost = initial_lkcost.clone();

		this.graph = new int[numNodes][numNodes];
		costs = new int[numNodes][2];
		for (int i = 0; i < numNodes; i++){
			graph[nodename][i] = initial_lkcost[i];
			graph[i][nodename] = initial_lkcost[i];

			costs[i][0] = initial_lkcost[i];	// min cost to reach node i
			costs[i][1] = i;					// next hop to get to node i
		}

		// send packet to layer 2???
		for (int i = 0; i < numNodes; i++){
			if (lkcost[i] != INFINITY && i != nodename) {
				Packet newPacket = new Packet(nodename, i, nodename, lkcost, generateSeqNum());
				NetworkSimulator.tolayer2(newPacket);
			}
		}
//		printdt();
	}

	/**
	 * Use the values provided the source node to update the graph
	 * ALL VALUES CORRESPOND TO SOURCE not any others in transit
	 */
	void rtupdate(Packet rcvdpkt) {
		/*Packet
		int sourceid	- ID of router sending packet
		int destid		- ID of router which packet is being sent
		int nodename	- Name of node which packet was sent from
		int[] mincost	- link costs to neighbor nodes
		int seqNo		- Used to distinguish new link state info
		*/

		/* Step 1: Check if packet should be destroyed (duplicate || no change) */
		if (this.seenSeq.contains(rcvdpkt.seqNo)){
			return;
		}
		seenSeq.add(rcvdpkt.seqNo);

		/* Step 2: Update Graph */
		for (int i = 0; i < rcvdpkt.mincost.length; i++){
			this.graph[rcvdpkt.sourceid][i] = rcvdpkt.mincost[i];
			this.graph[i][rcvdpkt.sourceid] = rcvdpkt.mincost[i];
		}

		/* Step 3: Run Dijkstra's algorithm and establish new costs[][] values */
		int[][] tempCost = this.dijkstra();
		this.costs = this.formatRouteTable(tempCost);

		/* Step 4: Create and send new packet outward */
		// Check if direct neighbor links have been changed
		boolean neighborChanged = false;
		for (int i = 0; i < numNodes && !neighborChanged; i++){
			if (lkcost[i] != graph[nodename][i]){
				neighborChanged = true;
			}
		}

		for (int i = 0; i < numNodes && neighborChanged; i++){
			lkcost[i] = graph[nodename][i];
		}

		// Broadcast change to linkstate
		for (int i = 0; i < numNodes; i++) {
			if (i != nodename && lkcost[i] != INFINITY) {
				Packet outpkt = new Packet(rcvdpkt.sourceid, rcvdpkt.destid,
						nodename, rcvdpkt.mincost, rcvdpkt.seqNo);
//				NetworkSimulator.tolayer2(outpkt);
			}
		}
	}

	/**
	 * Returns NONFINAL costs[][] table
	 * recall:
	 * costs[i][0] = min cost to get to node i
	 * costs[i][1] = LAST hop to get to i
	 */
	private int[][] dijkstra(){
		int[][] costs = new int[numNodes][2];	// LOCAL assignment
		boolean[] visited = new boolean[numNodes];
		// Set all initial cost values to infinity
		for (int i = 0; i < numNodes; i++){
			costs[i][0] = INFINITY;
			visited[i] = false;
		}
		costs[this.nodename][0] = 0;
		costs[this.nodename][1] = this.nodename;

		for (int count = 0; count < numNodes - 1; count++){
			// Find vertex at with minimum distance
			int u = this.minDistance(costs, visited);
			visited[u] = true;

			// process the neighbors (adj nodes) of current vertex u
			for (int v = 0; v < numNodes; v++){
				// If vertex has not yet been visited, update it
				if (!visited[v] && graph[u][v] != 0 && costs[u][0] != INFINITY &&
						costs[u][0] + graph[u][v] < costs[v][0]){
					costs[v][0] = costs[u][0] + graph[u][v];
					costs[v][1] = u;	// set parent to papa u
				}
			}
		}
//		for (int i = 0; i < numNodes; i++)
//			System.out.print(Arrays.toString(costs[i]));
		return costs;
	}

	/**
	 * Called by dijkstra(), used to find next node to travel on
	 */
	private int minDistance(int[][] path, boolean[] visited){
		int min = INFINITY;
		int minIdx = -1;

		for (int i = 0; i < numNodes; i++){
			if (!visited[i] && path[i][0] < min){
				min = path[i][0];
				minIdx = i;
			}
		}
		return minIdx;
	}

	/**
	 * Takes Dijkstra's output cost[][]
	 * Formats cost[][1] from PREV hop to get to destination node to NEXT hop from the source node
	 * ::-> puts costs[][] in terms of this.nodename to create a routing table
	 */
	private int[][] formatRouteTable(int[][] costs) {
		Set<Integer> neighbors = new HashSet<Integer>();

		// Set of neighbors to indicate next hop
		for (int i = 0; i < numNodes; i++) {
			if (graph[nodename][i] != INFINITY && i != nodename)
				neighbors.add(i);
		}

		// trace back until costs[i][1] in neighbors
		for (int i = 0; i < numNodes; i++) {
			// Case 1: destination is neighbor && directly is lowest cost
			if (neighbors.contains(i) && lkcost[i] == costs[i][0]) {
				costs[i][1] = i;
			}
		}
		for (int i = 0; i < numNodes; i++){
			// Case 2: current rounting is
			if (lkcost[costs[i][1]] == INFINITY) {
				int currentHop = costs[i][1];
				costs[i][1] = costs[currentHop][1];
			}
		}
		return costs;
	}


	/* called when cost from the node to linkid changes from current value to newcost*/
	void linkhandler(int linkid, int newcost) {

		/* Step 1: update lkcost */
		this.lkcost[linkid] = newcost;

		/* Step 2: Add changes to lkcost to the graph */
		for (int i = 0; i < numNodes; i++){
			graph[i][nodename] = lkcost[i];
			graph[nodename][i] = lkcost[i];
		}

		/* Step 3: Compute new routing table */
		int[][] tempCost = this.dijkstra();
		this.costs = this.formatRouteTable(tempCost);

		/* Step 4: Build and send outgoing packet to neighbors */
		int nextSeqNum = generateSeqNum();
		for (int i = 0; i < numNodes; i++) {
			if (i != nodename && lkcost[i] != INFINITY) {
				Packet outpkt = new Packet(
						nodename,
						i,
						nodename,
						lkcost,
						nextSeqNum
						);
			}
		}
	}

	private int createdPackets = 0;
	int generateSeqNum(){
		return 1000 * nodename + createdPackets++;
	}

	/* Prints the current costs to reaching other nodes in the network */
	void printdt() {

		System.out.printf("                    \n");
		System.out.printf("   D%d |   cost  next-hop \n", nodename);
		System.out.printf("  ----|-----------------------\n");
		System.out.printf("     0|  %3d   %3d\n",costs[0][0],costs[0][1]);
		System.out.printf("dest 1|  %3d   %3d\n",costs[1][0],costs[1][1]);
		System.out.printf("     2|  %3d   %3d\n",costs[2][0],costs[2][1]);
		System.out.printf("     3|  %3d   %3d\n",costs[3][0],costs[3][1]);
		System.out.printf("                    \n");
	}

}
