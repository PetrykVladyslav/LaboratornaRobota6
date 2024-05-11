package task3.v2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SecondClient extends JFrame {
    private MulticastSocket socket;
    private InetAddress group;
    private int port;
    private String username;
    private static JTextArea serverMessageArea = null;
    private final JTextField messageField, serverField, portField, usernameField;
    private final JButton sendButton, connectButton, disconnectButton, clearButton, exitButton;
    public SecondClient() {
        setTitle("Груповий чат (Клієнт 2)");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());

        serverMessageArea = new JTextArea(10, 30);
        serverMessageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(serverMessageArea);
        inputPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel topInputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField(25);
        topInputPanel.add(messageField, BorderLayout.CENTER);
        sendButton = new JButton("Надіслати");
        sendButton.addActionListener(new SendListener());
        topInputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(topInputPanel, BorderLayout.NORTH);

        JPanel centerInputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        serverField = new JTextField(10);
        portField = new JTextField(5);
        usernameField = new JTextField(10);
        centerInputPanel.add(new JLabel("Сервер:"));
        centerInputPanel.add(serverField);
        centerInputPanel.add(new JLabel("Порт:"));
        centerInputPanel.add(portField);
        centerInputPanel.add(new JLabel("Ім'я користувача:"));
        centerInputPanel.add(usernameField);
        inputPanel.add(centerInputPanel, BorderLayout.WEST);

        connectButton = new JButton("З'єднати");
        connectButton.addActionListener(new ConnectListener());
        buttonPanel.add(connectButton);

        disconnectButton = new JButton("Роз'єднати");
        disconnectButton.addActionListener(new DisconnectListener());
        buttonPanel.add(disconnectButton);

        clearButton = new JButton("Очистити");
        clearButton.addActionListener(new ClearListener());
        buttonPanel.add(clearButton);

        exitButton = new JButton("Завершити");
        exitButton.addActionListener(new ExitListener());
        buttonPanel.add(exitButton);

        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(inputPanel, BorderLayout.EAST);
        add(mainPanel);
    }
    private boolean isConnected() {
        return socket != null && !socket.isClosed();
    }
    private void receive() {
        Thread receiveThread = new Thread(() -> {
            try {
                while (isConnected()) {
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    serverMessageArea.append(receivedMessage + "\n");
                }
            } catch (SocketException ex) {
                serverMessageArea.append(username + " відключився.\n");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            }
        });
        receiveThread.start();
    }
    private class SendListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!isConnected()) {
                serverMessageArea.append("Спочатку підключіться до сервера!\n");
                return;
            }
            String message = messageField.getText();
            if (!message.isEmpty() && socket != null) {
                try {
                    String messageWithUsername = username + ": " + message;
                    byte[] buffer = messageWithUsername.getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
                    socket.send(packet);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                messageField.setText("");
            }
        }
    }
    private class ConnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String server = serverField.getText();
            String portStr = portField.getText();
            username = usernameField.getText();
            if (!server.isEmpty() && !portStr.isEmpty() && !username.isEmpty()) {
                try {
                    if (isConnected()) {
                        serverMessageArea.append("Ви вже підключені до сервера!\n");
                        return;
                    }
                    port = Integer.parseInt(portStr);
                    group = InetAddress.getByName(server);
                    socket = new MulticastSocket(port);
                    socket.joinGroup(group);

                    String connectMessage = username + " приєднався до чату";
                    byte[] connectBuffer = connectMessage.getBytes();
                    DatagramPacket connectPacket = new DatagramPacket(connectBuffer, connectBuffer.length, group, port);
                    socket.send(connectPacket);

                    receive();
                } catch (NumberFormatException | IOException ex) {
                    ex.printStackTrace();
                    serverMessageArea.append("Помилка при з'єднанні з сервером.\n");
                }
            } else {
                serverMessageArea.append("Будь ласка, заповніть всі поля для з'єднання.\n");
            }
        }
    }
    private class DisconnectListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                if (isConnected()) {
                    socket.leaveGroup(group);
                    socket.close();
                    serverMessageArea.append("Ви від'єднані від сервера.\n");
                } else {
                    serverMessageArea.append("Ви не підключені до сервера.\n");
                }
            } catch (IOException ex) {
                if (!(ex instanceof SocketException)) {
                    ex.printStackTrace();
                }
            }
        }
    }
    private static class ClearListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            serverMessageArea.setText("");
        }
    }
    private class ExitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dispose();
            System.exit(0);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SecondClient user = new SecondClient();
            user.setLocationRelativeTo(null);
            user.setVisible(true);
        });
    }
}