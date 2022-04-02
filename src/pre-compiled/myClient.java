import java.io.*;
import java.net.*;
import java.util.*;

public class myClient {
    public static void main(String[] args) throws IOException {
        try {
            // Initialising Socket, Client output to server and input from server
            Socket socket = new Socket("127.0.0.1", 50000);
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            // 3 way handshake
            output.write("HELO\n".getBytes());
            output.flush();

            // Receives an OK message
            input.readLine();

            output.write("AUTH Nihal\n".getBytes());
            output.flush();

            // Receives a welcome message
            input.readLine();

            output.write("REDY\n".getBytes());
            output.flush();

            // Recevies Job details from server and stored in a String variable
            String rcvd = input.readLine();
            System.out.println(rcvd);

            // Loop to schedule jobs
            while (!rcvd.contains("NONE")) {
                if (rcvd.contains("JCPL")) {
                    output.write("REDY\n".getBytes());
                    output.flush();

                    // Receives next Job details
                    rcvd = input.readLine();
                }
                if (rcvd.contains("JOBN")) {
                    // Splitting the Job details to find out jobID, and number of cores, memory and
                    // disk space required
                    String[] jobSplit = rcvd.split("\\s");

                    int jobID = Integer.parseInt(jobSplit[2]); // Storing jobID as an int

                    String getsMessage = "GETS Capable " + jobSplit[4] + " " + jobSplit[5] + " " +
                            jobSplit[6] + "\n";
                    output.write(getsMessage.getBytes());
                    output.flush();

                    // Receives a data message for how many servers can handle the current job
                    rcvd = input.readLine();
                    output.write("OK\n".getBytes());
                    output.flush();
                    String[] dataSplit = rcvd.split("\\s");

                    // Takes the 2nd arguement,number of servers, and stored in an int variable
                    int jobNum = Integer.parseInt(dataSplit[1]);

                    // Arraylists created to store the details required to schedule a new job
                    ArrayList<Integer> serverIDList = new ArrayList<Integer>();
                    ArrayList<String> serverTypeList = new ArrayList<String>();
                    ArrayList<Integer> cpuCoresList = new ArrayList<Integer>();

                    // intialized a largest index int for server with most CPUCores
                    int largestIndexCore = 0;
                    // initialized largest servertype string
                    String largestServerType = " ";

                    int count = 0;
                    for (int i = 0; i < jobNum; i++) {
                        // Receives server details for ones that can handle the job
                        String serverInfo = input.readLine();
                        String[] serverSplit = serverInfo.split("\\s");
                        String serverType = serverSplit[0];

                        int serverID = Integer.parseInt(serverSplit[1]);

                        int cpuCores = Integer.parseInt(serverSplit[4]);
                        cpuCoresList.add(i, cpuCores);
                        serverTypeList.add(i, serverType);
                        serverIDList.add(i, serverID);

                        if (!largestServerType.contains(serverType)) {
                            if (cpuCoresList.get(i) > cpuCoresList.get(largestIndexCore)) {
                                largestIndexCore = i;
                                largestServerType = serverTypeList.get(i);
                            }
                        }
                    }

                    output.write("OK\n".getBytes());
                    output.flush();

                    // receives a "." message to schedule the job
                    input.readLine();
                    String schd = "SCHD " + jobID + " " + largestServerType + " "
                            + serverIDList.get(largestIndexCore) + "\n";

                    System.out.println(schd);
                    output.write(schd.getBytes());
                    output.flush();
                    input.readLine();
                    output.write("REDY\n".getBytes());
                    output.flush();
                    // receives next job details
                    rcvd = input.readLine();

                }

            }

            output.write("QUIT\n".getBytes());
            output.flush();
            // Receives QUIT message from server
            input.readLine();

            output.close();
            input.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }
}