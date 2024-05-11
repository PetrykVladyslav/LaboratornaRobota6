package task3.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private static final int PORT = 12345;
    private static final String ADDRESS = "224.0.0.1";
    private static MulticastSocket socket;
    private static InetAddress group;
    private static Set<String> receivedMessages = new HashSet<>();

    public static void main(String[] args) {
        try {
            group = InetAddress.getByName(ADDRESS);
            socket = new MulticastSocket(PORT);
            socket.joinGroup(group);

            System.out.println("Сервер запущений. Введіть 'exit', щоб завершити.");

            Thread commandThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        String command = reader.readLine();
                        if (command.equalsIgnoreCase("exit")) {
                            socket.leaveGroup(group);
                            socket.close();
                            break;
                        } else {
                            System.out.println("Невідома команда: " + command);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Сервер завершує свою роботу...");
                }
            });
            commandThread.start();
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (!receivedMessages.contains(message)) {
                    System.out.println("Повідомлення від клієнта: " + message);
                    receivedMessages.add(message);
                }

                broadcast(message, packet.getAddress(), packet.getPort());
            }
        } catch (IOException e) {
            System.out.println("Сервер завершує свою роботу...");
        }
    }
    private static void broadcast(String message, InetAddress address, int port) throws IOException {
        byte[] responseData = message.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
        socket.send(responsePacket);
    }
}