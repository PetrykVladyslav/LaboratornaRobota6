package task3.v1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MulticastSocket;

public class User2 {
    private MulticastSocket socket;
    public static void main(String[] args) {
        ConferenceParticipant participant = null;
        Thread receiverThread = null;
        try {
            String username = args.length > 0 ? args[0] : "Anonymous2";
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