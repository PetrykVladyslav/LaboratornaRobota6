package task2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPEchoClient {
    private final int SERVER_PORT;
    private final String serverIP;
    public UDPEchoClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.SERVER_PORT = serverPort;
    }
    public void connect() {
        final int TIMEOUT = 3000;

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            clientSocket.setSoTimeout(TIMEOUT);
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String input;

            System.out.println("Введіть повідомлення для надсилання на сервер або «exit», щоб вийти.");

            int attempts = 0;
            while (true) {

                input = reader.readLine();

                if ("exit".equals(input)) {
                    break;
                }

                byte[] sendData = input.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);

                while (attempts < 5) {
                    clientSocket.send(sendPacket);
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    try {
                        clientSocket.receive(receivePacket);
                        String echoMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        if (echoMessage.length() > 0) {
                            System.out.println("Отримано відповідь від сервера: " + echoMessage);
                            attempts = 0;
                            break;
                        }
                    } catch (SocketTimeoutException e) {
                        attempts++;
                        System.out.println("Спроба #" + attempts + ": сервер не відповідає.");
                    }
                }

                if (attempts >= 5) {
                    System.out.println("Сервер не відповідає після 5 спроб. Завершення роботи...");
                    break;
                }
            }

            reader.close();
            System.out.println("Клієнт завершує роботу...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        UDPEchoClient client = new UDPEchoClient("localhost", 7);
        client.connect();
    }
}