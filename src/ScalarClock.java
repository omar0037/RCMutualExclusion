import java.io.Serializable;

public class ScalarClock implements Serializable {
    Integer clock;
    Integer nodeNum;

    public ScalarClock(Integer clock, Integer nodeNum) {
        this.clock = clock;
        this.nodeNum = nodeNum;
    }

    public Integer getClock() {
        return clock;
    }

    public void setClock(Integer clock) {
        this.clock = clock;
    }

    public Integer getNodeNum() {
        return nodeNum;
    }

    public void setNodeNum(Integer nodeNum) {
        this.nodeNum = nodeNum;
    }

}
