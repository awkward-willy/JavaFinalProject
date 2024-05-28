package finalProject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDR = "127.0.0.1";
    private static final int SERVER_PORT = 8000;
    private static volatile boolean running = true;

    public ChatClient() {
        System.out.println("ChatClient initialized.");
    }

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDR, SERVER_PORT)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            OutputStream out = socket.getOutputStream();

            Scanner scanner = new Scanner(System.in);
            Boolean EnterRoom = true;
            while (EnterRoom) {
                // 輸入房間號
                System.out.print("Enter room number: ");
                String roomNumber = scanner.nextLine();
                out.write((roomNumber + "\n").getBytes());
                out.flush();
                String serverMessage = reader.readLine();
                if (!serverMessage.equals("Room is full. Try again...")) {
                    System.out.println(serverMessage);
                    EnterRoom = false;
                    break;
                } else {
                    System.out.println(serverMessage);
                }
            }

            // 啟動一個執行緒來接收來自伺服器的消息
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while (running && (serverMessage = reader.readLine()) != null) {
                        System.out.println("Server: " + serverMessage);
                    }
                } catch (Exception e) {
                    if (running) {
                        System.out.println("Server disconnected");
                    }
                }
            });
            readerThread.start();

            // 主執行緒處理用戶輸入並發送消息
            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("exit")) {
                    // 用戶輸入 exit 時，通知 readerThread 停止運行
                    running = false;
                    out.write("exit\n".getBytes());
                    out.flush();
                    socket.close();
                    break;
                }
                if (socket.isClosed()) {
                    System.out.println("Server disconnected");
                    break;
                }
                out.write((message + "\n").getBytes());
                out.flush();
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("Server disconnected");
        }

    }
}
