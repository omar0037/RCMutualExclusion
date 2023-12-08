import java.io.*;
import java.nio.ByteBuffer;

public class Message implements Serializable {
    MessageType messageType;
    int nodeNum;
    ScalarClock clock;
    public Message(MessageType messageType, int nodeNum) {
        this.messageType = messageType;
        this.nodeNum = nodeNum;
    }

    public Message(MessageType messageType, int nodeNum, ScalarClock clock) {
        this.messageType = messageType;
        this.nodeNum = nodeNum;
        this.clock = clock;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getNodeNum() {
        return nodeNum;
    }

    public ScalarClock getClock() {
        return clock;
    }

    public void setClock(ScalarClock clock) {
        this.clock = clock;
    }

    public void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    public ByteBuffer toByteBuffer() throws Exception
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        oos.flush();

        ByteBuffer buf = ByteBuffer.allocateDirect(bos.size());
        buf.put(bos.toByteArray());

        oos.close();
        bos.close();

        // Buffer needs to be flipped after writing
        // Buffer flip should happen only once
        buf.flip();
        return buf;
    }


    public static Message fromByteBuffer(ByteBuffer buf) throws Exception
    {
        // Buffer needs to be flipped before reading
        // Buffer flip should happen only once
        buf.flip();
        byte[] data = new byte[buf.limit()];
        buf.get(data);
        buf.clear();

        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Message msg = (Message) ois.readObject();

        bis.close();
        ois.close();

        return msg;
    }
}
