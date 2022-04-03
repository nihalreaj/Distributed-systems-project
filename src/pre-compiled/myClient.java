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

            String username = "AUTH " + System.getProperty("user.name") + "\n";
            output.write(username.getBytes());
            output.flush();

            // Receives a welcome message
            input.readLine();

            output.write("REDY\n".getBytes());
            output.flush();

            // Recevies Job details from server and stored in a String variable
            String rcvd = input.readLine();

            String largestServerType = " "; // String created to store largest server type

            int loopStart = 0; // created an int variable so that largestServerType gets updated only once

            // While-Loop to schedule jobs
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
                            jobSplit[6] + "\n"; // String variable created to store message for GETS
                    output.write(getsMessage.getBytes());
                    output.flush();

                    // Receives a data message for how many servers can handle the current job
                    rcvd = input.readLine();

                    output.write("OK\n".getBytes());
                    output.flush();
                    // Splitting data to find out number of servers that can run job
                    String[] dataSplit = rcvd.split("\\s");

                    // Takes the 2nd arguement,number of servers, and stored in an int variable
                    int serverNum = Integer.parseInt(dataSplit[1]);

                    // Arraylists created to store the server type and coreCount required to
                    // schedule a new job
                    ArrayList<String> serverTypeList = new ArrayList<String>();
                    ArrayList<Integer> cpuCoresList = new ArrayList<Integer>();

                    // intialized a largest index int for server with most CPUCores
                    int largestIndexCore = 0;

                    // for-loop to iterate through all available servers
                    if (loopStart == 0) { // if-statement so largestServerType updates only once
                        for (int i = 0; i < serverNum; i++) {
                            // Receives server details for ones that can handle the job
                            String serverInfo = input.readLine();
                            String[] serverSplit = serverInfo.split("\\s"); // Splits server info and stored in array
                            String serverType = serverSplit[0]; // stores current server type in a String

                            int cpuCores = Integer.parseInt(serverSplit[4]); // Stores server coreCount in an int
                            cpuCoresList.add(i, cpuCores); // adds current coreCount to ArrayList
                            serverTypeList.add(i, serverType); // adds current server type to Arraylist

                            // Checks to see if current server has more cores than one of largestIndex
                            if (cpuCoresList.get(i) > cpuCoresList.get(largestIndexCore)) {
                                largestIndexCore = i;
                                largestServerType = serverTypeList.get(i);
                            }

                            // updates LargestServerType when there is only one server listed
                            if (serverTypeList.size() == 1) {
                                largestServerType = serverTypeList.get(0);
                            }

                        }
                        // When largestServerType has already been found
                    } else {
                        for (int i = 0; i < serverNum; i++) {
                            String serverInfo = input.readLine();
                            String[] serverSplit = serverInfo.split("\\s");
                            String serverType = serverSplit[0];
                            serverTypeList.add(i, serverType);
                        }
                    }
                    // increments loopStart so that largestServerType does not get re-updated
                    loopStart++;

                    // counts number of largest servers
                    int countLargestServer = Collections.frequency(serverTypeList, largestServerType);

                    output.write("OK\n".getBytes());
                    output.flush();

                    // determines which index of largest server will handle the job
                    int schdIndex = jobID % countLargestServer;

                    // receives a "." message to schedule the job
                    input.readLine();

                    String schd = "SCHD " + jobID + " " + largestServerType + " "
                            + schdIndex + "\n"; // SCHD message

                    output.write(schd.getBytes());
                    output.flush();

                    // Receives an "OK" message
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