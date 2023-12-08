import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class Service {
    Integer inCriticalSection;
    Hashtable<Integer, Boolean> keyMap;
    ScalarClock requestClock;
    SctpServerChannel serverChannel;
    Integer clock;
    Integer nodeNum;
    ArrayList<Integer> keysToBeRequested = new ArrayList<>();
    HashMap<Integer,Integer> portMap;
    HashMap<Integer,String> nodeMap;
    ArrayList<Requester> requesters;
    Boolean outstandingRequestExist;
    int totalMessage =0;

    int message = 0;
    int numOfKeys=0;

    public int getTotalMessage() {
        return totalMessage;
    }

    public void setTotalMessage(int totalMessage) {
        this.totalMessage = totalMessage;
    }

    public int getMessage() {
        return message;
    }

    public void setMessage(int message) {
        this.message = message;
    }

    Hashtable<Integer, Boolean> requestSent = new Hashtable<>();

    public Hashtable<Integer, Boolean> getRequestSent() {
        return requestSent;
    }

    public void setRequestSent(Hashtable<Integer, Boolean> requestSent) {
        this.requestSent = requestSent;
    }

    public Boolean getOutstandingRequestExist() {
        return outstandingRequestExist;
    }

    public void setOutstandingRequestExist(Boolean outstandingRequestExist) {
        this.outstandingRequestExist = outstandingRequestExist;
    }

    public Integer getInCriticalSection() {
        return inCriticalSection;
    }

    public void setInCriticalSection(Integer inCriticalSection) {
        this.inCriticalSection = inCriticalSection;
    }

    public Hashtable<Integer, Boolean> getKeyMap() {
        return keyMap;
    }

    public void setKeyMap(Hashtable<Integer, Boolean> keyMap) {
        this.keyMap = keyMap;
    }

    public ScalarClock getRequestClock() {
        return requestClock;
    }

    public void setRequestClock(ScalarClock requestClock) {
        this.requestClock = requestClock;
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

    public ArrayList<Integer> getKeysToBeRequested() {
        return keysToBeRequested;
    }

    public void setKeysToBeRequested(ArrayList<Integer> keysToBeRequested) {
        this.keysToBeRequested = keysToBeRequested;
    }

    public SctpServerChannel getServerChannel() {
        return serverChannel;
    }

    public void setServerChannel(SctpServerChannel serverChannel) {
        this.serverChannel = serverChannel;
    }

    public HashMap<Integer, Integer> getPortMap() {
        return portMap;
    }

    public void setPortMap(HashMap<Integer, Integer> portMap) {
        this.portMap = portMap;
    }

    public HashMap<Integer, String> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(HashMap<Integer, String> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public ArrayList<Requester> getRequesters() {
        return requesters;
    }

    public void setRequesters(ArrayList<Requester> requesters) {
        this.requesters = requesters;
    }

    public int getNumOfKeys() {
        return numOfKeys;
    }

    public void setNumOfKeys(int numOfKeys) {
        this.numOfKeys = numOfKeys;
    }

    public void cs_enter() {
        String method_name = "cs_enter";
        System.out.println(method_name + ": cs_enter called");

        //System.out.printlnn(method_name + ": setting outstanding request exists from cs_enter");
        synchronized (getKeyMap()) {
            int c = getClock();
            setOutstandingRequestExist(true);
            setRequestClock(new ScalarClock(c, getNodeNum()));
            //System.out.println(method_name + ": Obtained lock on map in cs_enter");
            Iterator<Map.Entry<Integer, Boolean>> itr = getKeyMap().entrySet().iterator();
            while(itr.hasNext()) {
                Map.Entry<Integer, Boolean> entry = itr.next();
                if(!entry.getValue() && !getRequestSent().get(entry.getKey())) {
                    Requester r = getRequesters().get(entry.getKey());
                    Thread t = new Thread(r);
                    t.start();
                    //System.out.println(method_name + ": Sent request for from cs_enter " + entry.getKey());
                    getRequestSent().put(entry.getKey(),true);
                }
            }
        }
        //System.out.println(method_name + ": released lock on map");
        while(!recievedAllKeys()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
       //System.out.println(method_name + ": Received all the keys");

    }

    private boolean recievedAllKeys() {
        synchronized (getKeyMap()) {
            if (getNumOfKeys() >= getKeyMap().size()/2) {
                setInCriticalSection(1);
                FileOutputStream fileOut = null;
                try {
                    fileOut = new FileOutputStream("common.txt", true);
                    PrintWriter writer = new PrintWriter(fileOut);
                    writer.println("Entered critical section " + getNodeNum() + " " +System.currentTimeMillis());
                    writer.close();
                    fileOut.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public void cs_leave() {
        String method_name = "cs_leave";
        setTotalMessage(getTotalMessage()+getMessage());
        FileOutputStream fileOut = null;
        System.out.println(method_name + ": cs_leave called");
        synchronized (getKeyMap()) {
            try {
                fileOut = new FileOutputStream("common.txt", true);
                PrintWriter writer = new PrintWriter(fileOut);
                writer.println("leaving critical section " + getNodeNum() + " " +System.currentTimeMillis() + " " + getMessage());
                writer.close();
                fileOut.close();
                setInCriticalSection(0);
                setOutstandingRequestExist(false);
                setClock(getClock()+1);
                setMessage(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(method_name + ": cs_leave done");
    }

    public Service(Hashtable<Integer, Boolean> keyMap) {
        this.keyMap = keyMap;
    }

    public Service(Hashtable<Integer, Boolean> keyMap, Integer clock, Integer nodeNum) {
        this.keyMap = keyMap;
        this.clock = clock;
        this.nodeNum = nodeNum;
    }

    public Service(Hashtable<Integer, Boolean> keyMap, Integer clock, Integer nodeNum, HashMap<Integer, Integer> portMap, HashMap<Integer, String> nodeMap) {
        this.keyMap = keyMap;
        this.clock = clock;
        this.nodeNum = nodeNum;
        this.portMap = portMap;
        this.nodeMap = nodeMap;
        setInCriticalSection(0);
        setOutstandingRequestExist(false);
        this.numOfKeys = keyMap.size()-nodeNum;
        for(int i=0;i<keyMap.size();i++) {
            getRequestSent().put(i,false);
        }
    }

    public void createListener() {
        String method_name = "createListener";
        try {
            InetSocketAddress address = new InetSocketAddress(getPortMap().get(getNodeNum()));
            setServerChannel(SctpServerChannel.open());
            getServerChannel().bind(address);
            System.out.println(method_name + ":Created Listener ");
            while (getServerChannel().isOpen()) {
                SctpChannel clientChannel = getServerChannel().accept();
                //System.out.println(method_name + ": -- new connection accepted -- ");
                Service.ListenerHandler listenerHandler = new Service.ListenerHandler(clientChannel);
                Thread thread = new Thread(listenerHandler);
                thread.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRequesters() {
        String method_name = "createRequesters";
        System.out.println(method_name + ": Create Requester called");
        int n = getKeyMap().size();
        setRequesters(new ArrayList<Requester>(n));
        for(int i=0;i<n;i++) {
            if(i != getNodeNum()) {
                try {
                    SctpChannel channel = SctpChannel.open(new InetSocketAddress(getNodeMap().get(i), getPortMap().get(i)), 0, 0);
                    getRequesters().add(i, new Requester(channel,i));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                getRequesters().add(i,null);
            }
        }
        System.out.println(method_name + ": Created Requesters ");
    }

    class ListenerHandler implements Runnable{
        SctpChannel listenerChannel;
        private ByteBuffer byteBuffer;

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public void setByteBuffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }

        public SctpChannel getListenerChannel() {
            return listenerChannel;
        }

        public void setListenerChannel(SctpChannel listenerChannel) {
            this.listenerChannel = listenerChannel;
        }

        public ListenerHandler(SctpChannel listenerChannel) {
            this.listenerChannel = listenerChannel;
            setByteBuffer(ByteBuffer.allocate(8192));
        }
        public boolean check1() {
            System.out.print("");
            synchronized (getKeyMap()) {
                return getInCriticalSection() == 1;
            }
        }
        public boolean check2(int c, int n) {
            System.out.print("");
            synchronized (getKeyMap()) {
                return getOutstandingRequestExist() && ((getRequestClock().getClock()<c) || ((getRequestClock().getClock() == c) && getNodeNum()<n ) );
            }
        }
        @Override
        public void run() {
            //String method_name = "listenerChannel " +
            while(listenerChannel.isOpen()) {
                try {
                    listenerChannel.receive(getByteBuffer(), null, null);
                    Message messageReceived = Message.fromByteBuffer(getByteBuffer());
                    int nn = messageReceived.getNodeNum();
                    String method_name = "listenerChannel " + nn;
                    while (check1()) ;
                    int n = getNodeNum();
                    int c2 = messageReceived.getClock().getClock();
                    while(check2(c2,nn));
                    synchronized (getKeyMap()) {
                        setNumOfKeys(getNumOfKeys()-1);
                        getKeysToBeRequested().add(nn);
                        getKeyMap().put(nn,false);
                        Message m = new Message(MessageType.RELEASE,getNodeNum(), getRequestClock());
                        listenerChannel.send(m.toByteBuffer(), MessageInfo.createOutgoing(null, 0));
                        getRequestSent().put(nn,false);
                        if(getOutstandingRequestExist()) {
                            Requester r = getRequesters().get(nn);
                            Thread t=new Thread(r);
                            t.start();
                            getRequestSent().put(nn,true);
                        }
                    }
                    System.out.println(method_name + ": sent key  " + nn);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }

        }
    }

    class Requester implements Runnable {
        SctpChannel requesterChannel;
        Integer listenerNodeNum;

        public Integer getListenerNodeNum() {
            return listenerNodeNum;
        }

        public void setListenerNodeNum(Integer listenerNodeNum) {
            this.listenerNodeNum = listenerNodeNum;
        }

        public SctpChannel getRequesterChannel() {
            return requesterChannel;
        }

        public void setRequesterChannel(SctpChannel requesterChannel) {
            this.requesterChannel = requesterChannel;
        }

        public Requester(SctpChannel requesterChannel) {
            this.requesterChannel = requesterChannel;
        }

        public Requester(SctpChannel requesterChannel, Integer listenerNodeNum) {
            this.requesterChannel = requesterChannel;
            this.listenerNodeNum = listenerNodeNum;
        }

        @Override
        public void run() {
            String method_name = "SenderChannel " + getListenerNodeNum();
            Message m = new Message(MessageType.REQUEST,getNodeNum(), getRequestClock());
            ByteBuffer buf = ByteBuffer.allocate(8192);;
            try {
                requesterChannel.send(m.toByteBuffer(), MessageInfo.createOutgoing(null, 0));
                requesterChannel.receive(buf, null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //System.out.println(method_name + ": Received key from node " + getListenerNodeNum());
            synchronized (getKeyMap()) {
                setMessage(getMessage()+2);
                setNumOfKeys(getNumOfKeys()+1);
                getKeyMap().put(getListenerNodeNum(),true);
                getRequestSent().put(getListenerNodeNum(),false);
                //System.out.println(method_name + ": set keymap value to true for node " + getListenerNodeNum());
            }
            // TODO : send request, once received remove from keyToBeRequestedList
        }
    }
}
