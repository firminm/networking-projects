import java.io.*;
import java.util.Arrays;

/**
 * This is the packet that is sent from one routing update process to another
 * via the call tolayer2() in the network simulator entitled
 * NetworkSimulator.java
 */
public class Packet {

    // ID of router sending this packet
    public int sourceid;

    // ID of router to which packet is being sent
    public int destid;

    // Min cost to neighbors 0 ... 3
    public int[] mincost;

    // Sequence number of packet to distinguish new info
    public int seqNo;

    // name of node whose mincost we are looking at
    public int nodename;

    /**
     * Class constructor with all attributes set
     */
    public Packet(int sourceid, int destid, int nodename, int[] mincost, int seqNo) {

        this.sourceid = sourceid;
        this.destid = destid;
        this.seqNo = seqNo;
        this.nodename = nodename;

        if (mincost.length != 4) {
            System.out.printf("mincost array is invalid\n");
            System.out.printf("Unable to construct new packet properly\n");
            this.mincost = new int[4];
            this.mincost[0] = -1;
            this.mincost[1] = -1;
            this.mincost[2] = -1;
            this.mincost[3] = -1;
        } else {
            this.mincost = new int[4];
            this.mincost[0] = mincost[0];
            this.mincost[1] = mincost[1];
            this.mincost[2] = mincost[2];
            this.mincost[3] = mincost[3];
        }
    }


    /**
     * Class constructor with no attributes set
     */
    public Packet() {
        this.sourceid = -1;
        this.destid = -1;
        this.seqNo = -1;
        this.nodename = -1;
        this.mincost = new int[4];
        this.mincost[0] = -1;
        this.mincost[1] = -1;
        this.mincost[2] = -1;
        this.mincost[3] = -1;
    }

    /**
     * Constructor that takes in a Packet as an input argument
     */
    public Packet(Packet p) {
        if (p == null) {
            new Packet();
        } else {
            this.sourceid = p.sourceid;
            this.destid = p.destid;
            this.seqNo = p.seqNo;
            this.nodename = p.nodename;
            if (p.mincost.length != 4) {
                System.out.printf("mincost array is invalid\n");
                System.out.printf("Unable to construct new packet properly\n");
                this.mincost = new int[4];
                this.mincost[0] = -1;
                this.mincost[1] = -1;
                this.mincost[2] = -1;
                this.mincost[3] = -1;
            } else {
                this.mincost = new int[4];
                this.mincost[0] = p.mincost[0];
                this.mincost[1] = p.mincost[1];
                this.mincost[2] = p.mincost[2];
                this.mincost[3] = p.mincost[3];
            }
        }
    }

    public String toString(){
        return "Source:" + sourceid + " Dest: " + destid+ " prevNode: " + nodename + " mincost: " + Arrays.toString(mincost);
    }
}
