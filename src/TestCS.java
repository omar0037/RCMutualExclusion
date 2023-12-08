import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.net.InetAddress;
import java.sql.SQLOutput;

public class TestCS {
    public static void main(String[] args) {
        double minTime = Double.MAX_VALUE, maxTime = Double.MIN_VALUE, numOfReq=0;
        int numOfMessage=0;
        double responseTime=0;
        int count=0;
        try {
            Reader reader = new FileReader("common.txt");
            BufferedReader br = new BufferedReader(reader);
            String line,prev=null;
            String prevNode= "-1";
            double prevTime=0;
            while((line = br.readLine()) != null) {
                numOfReq++;
                //System.out.println(line + " " + numOfReq);
                String[] split = line.split(" ");
                String node = split[3];
                if(line.contains("Enter")) {
                    minTime = Math.min(minTime,Double.parseDouble(split[4]));
                    if(prev != null && !prev.contains("leaving")) {
                        System.out.println("Failed");
                        //br.close();
                        //reader.close();
                        count++;
                    }
                } else {
                    maxTime = Math.max(maxTime, Double.parseDouble(split[4]));
                    numOfMessage+=Double.parseDouble(split[5]);
                    if((prev != null && !prev.contains("Enter")) || !prevNode.equals(node)) {
                        System.out.println("Failed");
                        count++;
                        //br.close();
                        //reader.close();
                        //return;
                    }
                    responseTime+=(Double.parseDouble(split[4]) - prevTime);
                }
                prev=line;
                prevNode=node;
                prevTime=Double.parseDouble(split[4]);
                //System.out.println(maxTime + " " + minTime + " " + numOfReq + " " + responseTime);
            }
            br.close();
            reader.close();

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Num of lines " + numOfReq);
        numOfReq=numOfReq/2;
        //System.out.println(maxTime + " " + minTime + " " + numOfReq + " " + responseTime);
        double systemThroughput = (numOfReq*1000)/(maxTime-minTime);
        System.out.println(count);
        System.out.println("Valid Outcome");
        System.out.println("System Throughput " + systemThroughput);
        System.out.println("Message Complexity " + numOfMessage);
        System.out.print("Average Response Time " + responseTime/numOfReq);
    }
}
