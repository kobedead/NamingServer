package ds.namingserver.Multicast;


import ds.namingserver.Config.NSConf;
import ds.namingserver.service.NamingService;

import java.io.IOException;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MulticastListener implements Runnable {

    private MulticastSocket socket;
    private InetAddress group;

    private NamingService namingService;


    // Constructor
    public MulticastListener() {

        namingService = new NamingService();

        try {
            // Create the multicast socket
            socket = new MulticastSocket(NSConf.MULTICAST_PORT);

            // Create a multicast group address
            group = InetAddress.getByName(NSConf.MULTICAST_GROUP);

            SocketAddress sockaddr = new InetSocketAddress(group, NSConf.MULTICAST_PORT);


            // Join the multicast group
            socket.joinGroup(sockaddr ,null);           //check

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[1024];  // Buffer for receiving messages
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        System.out.println("Listening for multicast messages on group: " + NSConf.MULTICAST_GROUP + " and port: " + NSConf.MULTICAST_PORT);

        while (true) {
            try {
                // Receive the incoming packet
                socket.receive(packet);

                // Filter based on the source IP, port, and content of the message
                InetAddress sourceAddress = packet.getAddress();
                String message = new String(packet.getData(), 0, packet.getLength());

                // Check if the message contains the specific keyword
                if (message.startsWith(NSConf.PREFIX)) {
                  //if message has prefix we need to process it
                    String name = extractName(message);                 //check
                    if (name != null){
                        namingService.processIncomingMulticast(sourceAddress.toString() ,name);
                    }
                    else
                        System.out.println("Name not found in multicast message");
                } else {
                    System.out.println("Filtered out message with incorrect prefix: " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Close the socket when done
    public void close() {
        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static String extractName(String input) {
        // Regular expression to match text inside curly braces
        String regex = "\\{([^}]+)\\}";  // Match content inside {}

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // Check if the pattern matches
        if (matcher.find()) {
            // Return the first captured group (the name inside the braces)
            return matcher.group(1);
        }

        // Return null or empty string if no match is found
        return null;
    }




    public static void main(String[] args) {
        // Create and start the listener thread
        MulticastListener listener = new MulticastListener();
        Thread listenerThread = new Thread(listener);
        listenerThread.start();
    }
}
