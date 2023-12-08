import java.io.*;

public class FileUtil {

    public static void updateFile(String fileName, double value) {
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bfr = new BufferedReader(reader);
            Double read = Double.parseDouble((String)bfr.readLine());
            bfr.close();
            reader.close();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            PrintWriter writer = new PrintWriter(fileOutputStream);
            writer.print(read+value);
            writer.close();
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("Writing to file failed");
        }
    }
    public static void updateSysThroughput(String fileName, double value) {
        try {
            FileReader reader = new FileReader(fileName);
            BufferedReader bfr = new BufferedReader(reader);
            Double read = Double.parseDouble((String)bfr.readLine());
            bfr.close();
            reader.close();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            PrintWriter writer = new PrintWriter(fileOutputStream);
            writer.print(Math.max(read,value));
            writer.close();
            fileOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException("updating sys throughput failed");
        }
    }
}
