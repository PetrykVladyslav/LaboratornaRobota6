package task2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPEchoServer implements Runnable {
    private DatagramSocket serverSocket;
    private boolean isShutDown = false;
    private final int SERVER_PORT;
    private final String serverIP;
    private final int BUFFER_SIZE = 1024;
    public UDPEchoServer(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.SERVER_PORT = serverPort;
    }
    public void run() {
        try {
            serverSocket = new DatagramSocket(SERVER_PORT);
            System.out.println("UDP Ехо-сервер запущено...");

            byte[] receiveData = new byte[BUFFER_SIZE];

            while (!isShutDown) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);

                    String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("Отримано від клієнта: " + message + ". Зворотню відповідь відправлено успішно.");

                    DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(), receivePacket.getLength(),
                            receivePacket.getAddress(), receivePacket.getPort());
                    serverSocket.send(sendPacket);
                } catch (SocketException e) {
                    if (isShutDown) {
                        System.out.println("Сервер вимикається...");
                        break;
                    } else {
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }
    public void shutDown() {
        isShutDown = true;
        serverSocket.close();
    }
    public static void main(String[] args) {
        UDPEchoServer server = new UDPEchoServer("localhost", 7);
        Thread t = new Thread(server);
        t.start();

        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.shutDown();
    }
}