import java.io.*;
import java.net.*;
import java.util.*;

public class myClient {
    public static void main(String[] args) throws IOException {
        try {
            Socket socket = new Socket("127.0.0.1", 50000);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            // 3 way handshake
            output.write("HELO\n".getBytes());
            output.flush();

            String str = input.readLine();
            System.out.println(str);

            output.write("AUTH Nihal\n".getBytes());
            output.flush();

            str = input.readLine();
            System.out.println(str);

            output.write("REDY\n".getBytes());
            output.flush();

            String rcvd = input.readLine();
            System.out.println(rcvd);

            while (!rcvd.contains("NONE")) {
                if (rcvd.contains("JCPL")) {
                    output.write("REDY\n".getBytes());
                    output.flush();

                    rcvd = input.readLine();
                } else {
                    String[] jobSplit = rcvd.split("\\s");
                    int jobID = Integer.parseInt(jobSplit[2]);
                    String getsMessage = "GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " +
                            jobSplit[6] + "\n";
                    output.write(getsMessage.getBytes());
                    output.flush();
                    rcvd = input.readLine();
                    output.write("OK\n".getBytes());
                    output.flush();
                    String[] dataSplit = rcvd.split("\\s");
                    int jobNum = Integer.parseInt(dataSplit[1]);

                    ArrayList<Integer> serverIDList = new ArrayList<Integer>();
                    ArrayList<String> serverTypeList = new ArrayList<String>();
                    ArrayList<Integer> cpuCoresList = new ArrayList<Integer>();

                    int largestIndex = 0;
                    // int count = 0;
                    for (int i = 0; i < jobNum; i++) {

                        String serverInfo = input.readLine();
                        String[] serverSplit = serverInfo.split("\\s");
                        String serverType = serverSplit[0];
                        int serverID = Integer.parseInt(serverSplit[1]);
                        int cpuCores = Integer.parseInt(serverSplit[4]);
                        cpuCoresList.add(i, cpuCores);
                        serverTypeList.add(i, serverType);
                        serverIDList.add(i, serverID);
                        if (cpuCoresList.get(i) > cpuCoresList.get(largestIndex)) {
                            largestIndex = i;
                        }
                    }

                    output.write("OK\n".getBytes());
                    output.flush();

                    input.readLine();
                    String schd = "SCHD " + jobID + " " + serverTypeList.get(largestIndex) + " "
                            + serverIDList.get(largestIndex) + "\n";
                    output.write(schd.getBytes());
                    output.flush();
                    input.readLine();
                    output.write("REDY\n".getBytes());
                    output.flush();
                    rcvd = input.readLine();

                }

            }

            output.write("QUIT\n".getBytes());
            output.flush();
            input.readLine();

            output.close();
            input.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}