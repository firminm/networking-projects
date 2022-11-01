public class StatTracker{
    int originalPackets;
    int totalPackets;
    int retrans;
    int deliveredToLayer5;
    int acks;
    int corrupted;
    double avgRTT;
    double avgCommTime;
    int counted;

    public StatTracker(){
        this.originalPackets = 0;
        this.totalPackets = 0;
        this.retrans = 0;
        this.deliveredToLayer5 = 0;
        this.acks = 0;
        this.corrupted = 0;
        this.avgRTT = 0;
        this.avgCommTime = 0;
        this.counted = 0;
    }

    public void updateTimes(Packet packet){
        this.avgRTT += packet.getRTT();
        this.avgCommTime += packet.getFullCommTime();

        this.counted++;
    }

    public void finalize(){
        this.avgRTT /= counted;
        this.avgCommTime /= counted;
    }

}

