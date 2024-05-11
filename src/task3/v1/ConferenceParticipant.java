package task3.v1;

import java.io.*;
import java.net.*;

public class ConferenceParticipant {
    final MulticastSocket socket;
    private final InetAddress group;
    private final int port;
    private final String username;
    public ConferenceParticipant(String ipAddress, int port, String username) throws IOException {
        this.port = port;
        this.group = InetAddress.getByName(ipAddress);
        this.socket = new MulticastSocket(port);
        this.username = username;
        this.socket.joinGroup(group);
    }
    public void send(String message) throws IOException {
        String messageWithUsername = "[" + username + "]: " + message;
        byte[] buffer = messageWithUsername.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        socket.send(packet);
    }
    public void receive() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String receivedMessage = new String(packet.getData(), 0, packet.getLength());

        System.out.println(receivedMessage);
    }
    public void close() {
        try {
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            System.out.println("Завершено роботу користувача...");
        }
    }
    public static void main(String[] args) {
        ConferenceParticipant participant = null;
        Thread receiverThread = null;
        try {
            String username = args.length > 0 ? args[0] : "Anonymous";
            participant = new ConferenceParticipant("224.0.0.1", 8888, username);
            System.out.println(username + " підключився до чату. Для виходу напишіть exit.\n");
            ConferenceParticipant finalParticipant = participant;
            receiverThread = new Thread(() -> {
                try {
                    while (!finalParticipant.socket.isClosed()) {
                        finalParticipant.receive();
                    }
                } catch (IOException e) {
                    System.out.println("Ви покинули чат.");
                }
            });
            receiverThread.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = reader.readLine()) != null) {
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                participant.send(input);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (participant != null) {
                participant.close();
            }
            try {
                if (receiverThread != null) {
                    receiverThread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}