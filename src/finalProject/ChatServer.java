package finalProject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private static final String ADDR = "127.0.0.1";
    private static final int PORT = 8000;
    private static final int BACKLOG = 10;
    private static final int MAX_THREADS = 10;
    private static final Map<String, Set<Socket>> rooms = new HashMap<>();
    private static boolean terminateServer = false;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT, BACKLOG)) {
            System.out.println("[*] Listening on " + ADDR + ":" + PORT);

            while (!terminateServer) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[*] Accepted connection from: " + clientSocket.getInetAddress());

                new Thread(() -> handleClient(clientSocket)).start();
            }

            for (Set<Socket> roomClients : rooms.values()) {
                for (Socket clientSocket : roomClients) {
                    sendMessage(clientSocket, "Server terminate");
                    clientSocket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server terminated.");
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            while (true) {
                String roomNumber = reader.readLine();
                if (roomNumber == null)
                    break;

                System.out.println("Room number: " + roomNumber);
                rooms.computeIfAbsent(roomNumber, k -> new HashSet<>());

                if (rooms.get(roomNumber).size() < 2) {
                    rooms.get(roomNumber).add(clientSocket);
                    if (Thread.activeCount() <= MAX_THREADS) {
                        if (rooms.get(roomNumber).size() == 1) {
                            sendMessage(clientSocket, "Success! Waiting for other player...");
                        } else {
                            for (Socket s : rooms.get(roomNumber)) {
                                sendMessage(s, "Start game!");
                            }
                        }
                        handleClientMessages(clientSocket, roomNumber);
                    } else {
                        System.out.println("Max threads reached. Connection rejected.");
                        sendMessage(clientSocket, "Server is busy. Try again later.");
                        clientSocket.close();
                    }
                    break;
                } else {
                    sendMessage(clientSocket, "Room is full. Try again...");
                }
            }
        } catch (Exception e) {
            if (e instanceof java.net.SocketException) {
                System.out.println("A socket has left the room. Socket ID: " + clientSocket);
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleClientMessages(Socket clientSocket, String roomNumber) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String message;
            while ((message = reader.readLine()) != null) {
                for (Socket otherClient : rooms.get(roomNumber)) {
                    if (otherClient != clientSocket) {
                        sendMessage(otherClient, message);
                    }
                }
            }
        } catch (Exception e) {
            if (e instanceof java.net.SocketException) {
                System.out.println("A socket has left the room. Socket ID: " + clientSocket);
            } else {
                e.printStackTrace();
            }
        } finally {
            rooms.get(roomNumber).remove(clientSocket);
        }
    }

    private static void sendMessage(Socket socket, String message) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write((message + "\n").getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
