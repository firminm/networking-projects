import java.util.Arrays;

/**
 * This is the class that students need to implement. The code skeleton is provided.
 * Students need to implement rtinit(), rtupdate() and linkhandler().
 * printdt() is provided to pretty print a table of the current costs for reaching
 * other nodes in the network.
 */ 
public class Node { 
    
    public static final int INFINITY = 9999;
    
    int[] lkcost;		/*The link cost between this node and other nodes*/
    int nodename;               /*Name of this node*/
	int[][] cost;		/* cost[i][j]: cost to get to i via j from current node */
	int numNodes;
//	int[] distances;	/* Stores minimum known distance to other nodes, info that is passed to network */

    /* Class constructor */
    public Node() { }
    
    /* students to write the following two routines, and maybe some others */
    void rtinit(int nodename, int[] initial_lkcost) {
		this.nodename = nodename;
		this.numNodes = initial_lkcost.length;
		this.lkcost = initial_lkcost.clone();


		this.cost = new int[numNodes][numNodes];
		// Initialize route table to known values
		for (int i = 0; i < numNodes; i++){
			for (int j = 0; j < numNodes; j++){
				this.cost[i][j] = INFINITY;
			}
		}
		this.cost[nodename] = initial_lkcost.clone();

//		for (int i = 0; i < numNodes; i++){
//			System.out.println(Arrays.toString(cost[i]));
//		}


		for (int i = 0; i < numNodes; i++) {
			if (nodename != i && lkcost[i] != INFINITY) {
				Packet sndpkt = new Packet(nodename, i, this.cost[nodename].clone());
				NetworkSimulator.tolayer2(sndpkt);
			}
		}
	}

	/**
	 * rcvdpkt.mincost[] = DISTANCE VECTOR as input
	 * Uses neighbor costs var lkcost[] to compute new distance vector
	 * If change to distance vector -> send out the distance vector to neighbors
	 *
	 * Recall: costs[i][] stores the distance vector of node i
	 */
	void rtupdate(Packet rcvdpkt) {
		boolean changed = false;
		this.cost[rcvdpkt.sourceid] = rcvdpkt.mincost;

		// Update costs
//		for (int i = 0; i < numNodes)

		/* direct neighbor link change detected */
		// if conditional doesn't work? what if rcvdpkt just unlocked a new route?
//		if (lkcost[rcvdpkt.sourceid] != INFINITY && rcvdpkt.mincost[nodename] != costs[nodename][rcvdpkt.sourceid]){
//			System.out.println("==========LINK HANDLER ISSUED===========");
//			linkhandler(rcvdpkt.sourceid, rcvdpkt.mincost[nodename]);
//		}

		/* Check if the update to incoming node's distance vector affects this node */
		for (int i = 0; i < numNodes; i++){
			// Update distance vector
			this.cost[rcvdpkt.sourceid][i] = Math.min(rcvdpkt.mincost[i], this.cost[rcvdpkt.sourceid][i]);

			/* For each node i in incoming's distance vector, compare if this->incoming->i is shorter than current */
			int newValue = rcvdpkt.mincost[i] + lkcost[rcvdpkt.sourceid];
			if (newValue < cost[nodename][i]) {
				this.cost[nodename][i] = newValue;
				changed = true;
			}
		}

		if (changed){
			for (int i = 0; i < numNodes; i++){
				if (i != nodename && this.lkcost[i] != INFINITY) {
					Packet sndpkt = new Packet(nodename, i, cost[nodename]);
					NetworkSimulator.tolayer2(sndpkt);
				}
			}
		}
	}
    
    
    /* called when cost from the node to linkid changes from current value to newcost*/
    void linkhandler(int linkid, int newcost) {
//		System.out.println("0-0-0-0-0-0-0-0-0-0--0-0-0-0-0-0-0-0-0-0");
//		if (lkcost[linkid] == newcost) return;
//		lkcost[linkid] = newcost;
//
//		// Distance vectors become unstable, reset distance vectors
//		distances[nodename] = lkcost.clone();
//
//		for (int i = 0; i < numNodes; i++){
//			if (lkcost[i] != INFINITY && i != nodename){
//				Packet sndpkt = new Packet(nodename, i, distances[i]);
//				NetworkSimulator.tolayer2(sndpkt);
//			}
//		}
	}

    /* Prints the current costs to reaching other nodes in the network */
    void printdt() {
        switch(nodename) {
	
	case 0:
	    System.out.printf("                via     \n");
	    System.out.printf("   D0 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
		System.out.printf("     0|  %3d   %3d \n", cost[0][1], cost[0][2]);
		System.out.printf("     1|  %3d   %3d \n", cost[1][1], cost[1][2]);
	    System.out.printf("dest 2|  %3d   %3d \n", cost[2][1], cost[2][2]);
	    System.out.printf("     3|  %3d   %3d \n", cost[3][1], cost[3][2]);
	    break;
	case 1:
	    System.out.printf("                via     \n");
	    System.out.printf("   D1 |    0     2    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n", cost[0][0], cost[0][2], cost[0][3]);
		System.out.printf("     1|  %3d   %3d   %3d\n", cost[1][0], cost[1][2], cost[0][3]);
		System.out.printf("dest 2|  %3d   %3d   %3d\n", cost[2][0], cost[2][2], cost[2][3]);
	    System.out.printf("     3|  %3d   %3d   %3d\n", cost[3][0], cost[3][2], cost[3][3]);
	    break;    
	case 2:
	    System.out.printf("                via     \n");
	    System.out.printf("   D2 |    0     1    3 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d   %3d\n", cost[0][0], cost[0][1], cost[0][3]);
	    System.out.printf("dest 1|  %3d   %3d   %3d\n", cost[1][0], cost[1][1], cost[1][3]);
		System.out.printf("dest 2|  %3d   %3d   %3d\n", cost[2][0], cost[2][1], cost[2][3]);
		System.out.printf("     3|  %3d   %3d   %3d\n", cost[3][0], cost[3][1], cost[3][3]);
	    break;
	case 3:
	    System.out.printf("                via     \n");
	    System.out.printf("   D3 |    1     2 \n");
	    System.out.printf("  ----|-----------------\n");
	    System.out.printf("     0|  %3d   %3d\n", cost[0][1], cost[0][2]);
	    System.out.printf("dest 1|  %3d   %3d\n", cost[1][1], cost[1][2]);
	    System.out.printf("     2|  %3d   %3d\n", cost[2][1], cost[2][2]);
		System.out.printf("     3|  %3d   %3d\n", cost[3][1], cost[3][2]);
		break;
        }
    }
    
}
