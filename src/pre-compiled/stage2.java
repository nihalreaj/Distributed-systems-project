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

            // While-Loop to schedule jobs
            while (!rcvd.contains("NONE")) {
                if (rcvd.contains("JCPL")) {
                    output.write("REDY\n".getBytes());
                    output.flush();

                    // Receives next Job details
                    rcvd = input.readLine();
                }
                if (rcvd.contains("JOBN")) {

                    String schdServer = "none"; // String created to store server type that will be scheduled
                    int schdIndex = 0; // int created to store index of scheduled server

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

                    // Arraylists created to store the fitness values required to compare for
                    // finding lowest core server
                    ArrayList<Integer> fitnessScoreList = new ArrayList<Integer>();
                    int smallestIndex = 0; //

                    // for-loop to iterate through all available servers

                    int idleCount = 0; // int created for condition to check for idle servers
                    int inactiveCount = 0; // int created for condition to check for inactive servers
                    int bootCount = 0; // int created for condition to check for booting servers
                    for (int i = 0; i < serverNum; i++) {
                        // Receives server details for ones that can handle the job

                        String serverInfo = input.readLine();
                        String[] serverSplit = serverInfo.split("\\s"); // Splits server info and stored in array

                        String serverType = serverSplit[0]; // stores current server type in a String
                        int serverID = Integer.parseInt(serverSplit[1]);
                        int serverCore = Integer.parseInt(serverSplit[4]);

                        String serverStatus = serverSplit[2]; // stores current server status in a string

                        int fitnessValue = serverCore - jobCore; // int created to store difference of Server core by
                                                                 // Job core

                        // condition to check if fitnessValue is negative
                        if (fitnessValue < 0) {
                            fitnessValue = 350; // fitness value increased so it doesn't get scheduled when it is
                                                // negative
                        }

                        fitnessScoreList.add(i, fitnessValue); // added current server's fitnessValue to arrayList

                        // if-condition to make sure jobs are scheduled to idle servers first
                        if (serverStatus.equals("idle")) {

                            idleCount++; // increment idleCount

                            if (schdServer.contains("none")) { // if no servers are selected to be scheduled
                                schdServer = serverType;
                                schdIndex = serverID;

                            }
                            // comparing fitnessValues to select new server for scheduling
                            if (fitnessScoreList.get(i) < fitnessScoreList.get(smallestIndex)) {
                                schdServer = serverType;
                                schdIndex = serverID;
                                smallestIndex = i;

                            }
                        }

                        // if-condition to make sure jobs are scheduled to booting servers if no idle
                        // servers are found
                        else if ((serverStatus.equals("booting")) && (idleCount < 1)) {

                            bootCount++;

                            if (bootCount == 1) { // if inactive servers are found before booting servers
                                smallestIndex = i;
                                schdServer = "none";
                            }

                            if (schdServer.contains("none")) { // if no servers are selected to be scheduled
                                schdServer = serverType;
                                schdIndex = serverID;

                            }

                            // comparing fitnessValues to select new server for scheduling
                            if (fitnessScoreList.get(i) < fitnessScoreList.get(smallestIndex)) {
                                schdServer = serverType;
                                schdIndex = serverID;
                                smallestIndex = i;

                            }
                        }

                        // if-condition to make sure jobs are scheduled to inactive servers if no idle
                        // or booting servers are found
                        else if (serverStatus.equals("inactive") && (bootCount < 1)) {

                            inactiveCount++; // increment

                            if (inactiveCount == 1) { // if active servers are found before inactive servers
                                smallestIndex = i;
                                schdServer = "none";
                            }

                            if (schdServer.contains("none")) { // if no servers are selected to be scheduled
                                schdServer = serverType;
                                schdIndex = serverID;

                            }
                            // comparing fitnessValues to select new server for scheduling
                            if (fitnessScoreList.get(i) < fitnessScoreList.get(smallestIndex)) {
                                schdServer = serverType;
                                schdIndex = serverID;
                                smallestIndex = i;

                            }
                        }

                        // if-condition to make sure jobs are scheduled to active servers if other
                        // servers are found
                        if (inactiveCount < 1) {
                            if (schdServer.contains("none")) { // if no servers are selected to be scheduled
                                schdServer = serverType;
                                schdIndex = serverID;

                            } else {
                                // comparing fitnessValues to select new server for scheduling
                                if (fitnessScoreList.get(i) < fitnessScoreList.get(smallestIndex)) {
                                    schdServer = serverType;
                                    schdIndex = serverID;
                                    smallestIndex = i;
                                }
                            }
                        }

                    }

                    // write an OK message to server
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