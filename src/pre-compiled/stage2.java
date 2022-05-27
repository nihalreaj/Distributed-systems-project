import java.io.*;
import java.net.*;
import java.util.*;

public class stage2 {
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
            int loopstart = 0;

            String schdServer = "none"; // String created to store largest server type
            int schdIndex = 0;

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
                    int jobCore = Integer.parseInt(jobSplit[4]);

                    String getsMessage = "GETS Capable " + jobCore + " " + jobSplit[5] + " " +
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
                    ArrayList<Integer> fitnessScoreList = new ArrayList<Integer>();
                    ArrayList<Integer> serverIdList = new ArrayList<Integer>();

                    int bfIndex = 0;

                    // intialized a largest index int for server with most CPUCores

                    // for-loop to iterate through all available servers
                    // if-statement so largestServerType updates only once
                    int counter = 0;
                    for (int i = 0; i < serverNum; i++) {
                        // Receives server details for ones that can handle the job
                        String serverInfo = input.readLine();
                        String[] serverSplit = serverInfo.split("\\s"); // Splits server info and stored in array

                        String serverType = serverSplit[0]; // stores current server type in a String
                        int serverID = Integer.parseInt(serverSplit[1]);
                        int serverCore = Integer.parseInt(serverSplit[4]);
                        serverTypeList.add(i, serverType);
                        serverIdList.add(i, serverID);

                        String serverStatus = serverSplit[2]; // stores current server status in a string
                        if (serverStatus.contains("inactive")) {

                            int fitnessValue = serverCore - jobCore;
                            if (schdServer.contains("none")) {
                                schdServer = serverTypeList.get(i);
                                schdIndex = serverIdList.get(i);
                                fitnessScoreList.add(counter, fitnessValue);
                            } else {
                                fitnessScoreList.add(counter, fitnessValue);
                                if (fitnessScoreList.get(counter) < fitnessScoreList.get(bfIndex)) {
                                    schdServer = serverTypeList.get(i);
                                    schdIndex = serverIdList.get(i);
                                    bfIndex = counter;
                                }
                            }
                            counter++;

                        }
                        // String serverType = serverSplit[0]; // stores current server type in a String
                        // serverTypeList.add(i, serverType);

                        // schdServer = serverTypeList.get(0);

                    }
                    // When largestServerType has already been found

                    // increments loopStart so that largestServerType does not get re-update

                    // counts number of largest servers

                    output.write("OK\n".getBytes());
                    output.flush();

                    // determines which index of largest server will handle the job

                    // receives a "." message to schedule the job
                    input.readLine();

                    String schd = "SCHD " + jobID + " " + schdServer + " "
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
