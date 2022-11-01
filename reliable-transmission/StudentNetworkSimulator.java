import java.util.*;
import java.util.zip.Checksum;
import java.io.*;

public class StudentNetworkSimulator extends NetworkSimulator {
    /*
     * Predefined Constants (static member variables):
     *
     * int MAXDATASIZE : the maximum size of the Message data and
     * Packet payload
     *
     * int A : a predefined integer that represents entity A
     * int B : a predefined integer that represents entity B
     *
     * Predefined Member Methods:
     *
     * void stopTimer(int entity):
     * Stops the timer running at "entity" [A or B]
     * void startTimer(int entity, double increment):
     * Starts a timer running at "entity" [A or B], which will expire in
     * "increment" time units, causing the interrupt handler to be
     * called. You should only call this with A.
     * void toLayer3(int callingEntity, Packet p)
     * Puts the packet "p" into the network from "callingEntity" [A or B]
     * void toLayer5(String dataSent)
     * Passes "dataSent" up to layer 5
     * double getTime()
     * Returns the current time in the simulator. Might be useful for
     * debugging.
     * int getTraceLevel()
     * Returns TraceLevel
     * void printEventList()
     * Prints the current event list to stdout. Might be useful for
     * debugging, but probably not.
     *
     *
     * Predefined Classes:
     *
     * Message: Used to encapsulate a message coming from layer 5
     * Constructor:
     * Message(String inputData):
     * creates a new Message containing "inputData"
     * Methods:
     * boolean setData(String inputData):
     * sets an existing Message's data to "inputData"
     * returns true on success, false otherwise
     * String getData():
     * returns the data contained in the message
     * Packet: Used to encapsulate a packet
     * Constructors:
     * Packet (Packet p):
     * creates a new Packet that is a copy of "p"
     * Packet (int seq, int ack, int check, String newPayload)
     * creates a new Packet with a sequence field of "seq", an
     * ack field of "ack", a checksum field of "check", and a
     * payload of "newPayload"
     * Packet (int seq, int ack, int check)
     * chreate a new Packet with a sequence field of "seq", an
     * ack field of "ack", a checksum field of "check", and
     * an empty payload
     * Methods:
     * boolean setSeqnum(int n)
     * sets the Packet's sequence field to "n"
     * returns true on success, false otherwise
     * boolean setAcknum(int n)
     * sets the Packet's ack field to "n"
     * returns true on success, false otherwise
     * boolean setChecksum(int n)
     * sets the Packet's checksum to "n"
     * returns true on success, false otherwise
     * boolean setPayload(String newPayload)
     * sets the Packet's payload to "newPayload"
     * returns true on success, false otherwise
     * int getSeqnum()
     * returns the contents of the Packet's sequence field
     * int getAcknum()
     * returns the contents of the Packet's ack field
     * int getChecksum()
     * returns the checksum of the Packet
     * int getPayload()
     * returns the Packet's payload
     *
     */

    /*
     * Please use the following variables in your routines.
     * int WindowSize : the window size
     * double RxmtInterval : the retransmission timeout
     * int LimitSeqNo : when sequence number reaches this value, it wraps around
     */

    public static final int FirstSeqNo = 0;
    private int WindowSize;
    private double RxmtInterval;
    private int LimitSeqNo;
    private Queue<Packet> packets;
    private StatTracker stats;

    // Add any necessary class variables here. Remember, you cannot use
    // these variables to send messages error free! They can only hold
    // state information for A or B.
    // Also add any necessary methods (e.g. checksum of a String)

    // This is the constructor. Don't touch!
    public StudentNetworkSimulator(int numMessages,
            double loss,
            double corrupt,
            double avgDelay,
            int trace,
            int seed,
            int winsize,
            double delay) {
        super(numMessages, loss, corrupt, avgDelay, trace, seed);
        WindowSize = winsize;
        LimitSeqNo = winsize * 2; // set appropriately
        RxmtInterval = delay;

        stats = new StatTracker();

    }

    int currentSequenceNumber = FirstSeqNo;

    // This routine will be called whenever the upper layer at the sender [A]
    // has a message to send. It is the job of your protocol to insure that
    // the data in such a message is delivered in-order, and correctly, to
    // the receiving upper layer.
    protected void aOutput(Message message) {
        int dummyAck = 0; // Does nothing except offset checksum
        int seqNum = currentSequenceNumber + packets.size();
        int checksum = calculateChecksum(message.getData(), seqNum, dummyAck);
        Packet packet = new Packet(seqNum, dummyAck, checksum, message.getData());
        // System.out.println("RCV: message");
        packets.add(packet);
        send2Layer3();
        
        stats.originalPackets++;
    }

    /**
     * Handles logic for which packet to release to B
     */
    private void send2Layer3() {
        if (outgoing < WindowSize) {
            int count = 0;
            for (Packet pkt : packets) {
                /* Send as many packets as possible */
                if (outgoing == WindowSize) {
                    break;
                } else if (count < outgoing) {
                    // Make sure that we are sending out packets that are not currently outgoing
                    count++;
                    continue;
                } else {
                    if (outgoing == 0) {
                        startTimer(A, RxmtInterval);
                    }
                    pkt.setSendTime(this.getTime());
                    toLayer3(A, pkt);
                    outgoing++;       
                    // System.out.println("AOUT: " +pkt);             
                }
            }
        }else{
            ;
        }
        
    }

    // This routine will be called whenever a packet sent from the B-side
    // (i.e. as a result of a toLayer3() being done by a B-side procedure)
    // arrives at the A-side. "packet" is the (possibly corrupted) packet
    // sent from the B-side.
    protected void aInput(Packet packet) {
        int actualChecksum = calculateChecksum(packet.getPayload(), packet.getSeqnum(), packet.getAcknum());
        // System.out.println("AIN:  " + packet);

        if (packet.getChecksum() != actualChecksum) {
            stats.corrupted++;
            return;
        } else {
            // Packet not corrupted
            if (packet.getAcknum() < currentSequenceNumber) {
                // do nothing and return
                return;
            } else {
                // non-duplicate ack.
                // remove all packets with sequence numbers less than recieved from window
                while (!packets.isEmpty() && packets.peek().getSeqnum() <= packet.getAcknum()){
                    Packet toRem = packets.poll();
                    toRem.setAckTime(this.getTime());
                    stats.updateTimes(toRem);


                    outgoing--;
                }
                currentSequenceNumber = packet.getAcknum() + 1;
                send2Layer3();
            }
        }
    }

    // This routine will be called when A's timer expires (thus generating a
    // timer interrupt). You'll probably want to use this routine to control
    // the retransmission of packets. See startTimer() and stopTimer(), above,
    // for how the timer is started and stopped.
    protected void aTimerInterrupt() {
        // start back from scratch
        stats.retrans += outgoing;
        outgoing = 0;
        send2Layer3();
    }

    int outgoing;

    // This routine will be called once, before any of your other A-side
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity A).
    protected void aInit() {
        packets = new LinkedList<Packet>();
        outgoing = 0;
    }

    private int calculateChecksum(String payload, int seq, int ackNum) {
        int check = seq + ackNum;
        for (int i = 0; i < payload.length(); i++) {
            check += (int) payload.charAt(i);
        }

        return check;
    }

    // This routine will be called whenever a packet sent to the B-side
    // (i.e. as a result of a toLayer3() being done by an A-side procedure)
    // arrives at the B-side. "packet" is the (possibly corrupted) packet
    // sent from the A-side.
    int nextSequenceNum;

    protected void bInput(Packet packet) {
        // System.out.println("BIN:  " + packet);
        String payload = packet.getPayload();
        int dummyAckNum = packet.getAcknum();
        int seq = packet.getSeqnum();
        int actualChecksum = calculateChecksum(payload, seq, dummyAckNum);

        // Which sequence # packet we are acknowledging, default = fail case
        int ackedPacket;
        if (actualChecksum == packet.getChecksum()) {
            if (seq == nextSequenceNum) {
                toLayer5(packet.getPayload());
                ackedPacket = nextSequenceNum;
                nextSequenceNum++;

                stats.deliveredToLayer5++;
            } else if (seq < nextSequenceNum) {
                ackedPacket = seq;
            } else{
                // Packet out of order, re-acknowledge previous packet
                ackedPacket = nextSequenceNum -1;
            }
            String payloadOut = "00000000000000000000";
            int checksum = calculateChecksum(payloadOut, ackedPacket, ackedPacket);
            Packet ack = new Packet(ackedPacket, ackedPacket, checksum, payloadOut);
            toLayer3(B, ack);

        } // Else fail silently
        else{
            stats.corrupted++;
        }
        stats.acks++;
        
    }

    // This routine will be called once, before any of your other B-side
    // routines are called. It can be used to do any required
    // initialization (e.g. of member variables you add to control the state
    // of entity B).
    protected void bInit() {
        nextSequenceNum = FirstSeqNo;
    }

    // Use to print final statistics
    protected void Simulation_done() {
        stats.totalPackets = stats.originalPackets + stats.retrans;
        int lost = stats.retrans - stats.corrupted;
        stats.finalize();

        // TO PRINT THE STATISTICS, FILL IN THE DETAILS BY PUTTING VARIABLE NAMES. DO
        // NOT CHANGE THE FORMAT OF PRINTED OUTPUT
        System.out.println("\n\n===============STATISTICS=======================");
        System.out.println("Number of original packets transmitted by A:" + (stats.originalPackets));
        System.out.println("Number of retransmissions by A:" + (stats.retrans));
        System.out.println("Number of data packets delivered to layer 5 at B:" + stats.deliveredToLayer5);
        System.out.println("Number of ACK packets sent by B:" + (stats.acks));
        System.out.println("Number of corrupted packets:" + (stats.corrupted));
        System.out.println("Ratio of lost packets:" + (1.0 * lost / stats.totalPackets));
        System.out.println("Ratio of corrupted packets:" + (1.0 * stats.corrupted / stats.totalPackets));
        System.out.println("Average RTT:" + stats.avgRTT);
        System.out.println("Average communication time:" + stats.avgCommTime);
        System.out.println("==================================================");

        // PRINT YOUR OWN STATISTIC HERE TO CHECK THE CORRECTNESS OF YOUR PROGRAM
        System.out.println("\nEXTRA:");
        // EXAMPLE GIVEN BELOW
        // System.out.println("Example statistic you want to check e.g. number of ACK
        // packets received by A :" + "<YourVariableHere>");
        System.out.println("Queue Status:\n"+packets);
    }

}
