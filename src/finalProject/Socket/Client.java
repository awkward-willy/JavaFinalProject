package finalProject.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import finalProject.GameApp;

public class Client {
	private static final String SERVER_ADDR = "127.0.0.1";
	private static final int SERVER_PORT = 8000;
	private static volatile boolean running = true;
	private int startGame = 0;
	private String message = "";
	private GameApp gameApp;

	public void setGameApp(GameApp gameApp) {
		this.gameApp = gameApp;
	}

	public void closeConnection() {
		running = false;
	}

	public Boolean testServerAvailable() throws RuntimeException {
		// 先不用開 Thread，直接建立 Socket 連線，如果連線失敗，則伺服器不可用
		try (Socket socket = new Socket(SERVER_ADDR, SERVER_PORT)) {
			System.out.println("Server is available");
			return true;
		} catch (Exception e) {
			throw new RuntimeException("Server is not available");
		}
	}

	public void initConnection(int roomNumber, int mode, int difficulty) {
		// 建立與伺服器的連線
		Thread clientThread = new Thread(() -> {
			try (Socket socket = new Socket(SERVER_ADDR, SERVER_PORT)) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				OutputStream out = socket.getOutputStream();

				Boolean EnterRoom = true;
				while (EnterRoom) {
					// 輸入房間號
					String roomNumberStr = Integer.toString(roomNumber);
					out.write((roomNumberStr + "\n").getBytes());
					out.flush();
					String serverMessage = reader.readLine();
					if (!serverMessage.equals("Room is full. Try again...")) {
						if (serverMessage.equals("Start game!")) {
							setStartGame(1);
						}
						System.out.println(serverMessage);
						EnterRoom = false;
						break;
					} else {
						System.out.println(serverMessage);
						setStartGame(-1);
						closeConnection();
						break;
					}
				}

				// 啟動一個執行緒來接收來自伺服器的消息
				Thread readerThread = new Thread(() -> {
					try {
						String serverMessage;
						while (running && (serverMessage = reader.readLine()) != null) {
							if (serverMessage.equals("Server terminate")) {
								System.out.println(serverMessage);
								running = false;
								socket.close();
								break;
							} else if (serverMessage.equals("Start game!")) {
								setStartGame(1);
								System.out.println(serverMessage);
							} else if (serverMessage.equals("All planes placed.")) {
								gameApp.toggleOverlay();
								System.out.println(serverMessage);
							} else {
								System.out.println(serverMessage);
							}
						}
					} catch (Exception e) {
						if (running) {
							System.out.println("Server disconnected");
						}
					}
				});
				readerThread.start();

				// 主執行緒處理用戶輸入並發送消息
				while (running) {
					if (message.equalsIgnoreCase("quit")) {
						// 用戶輸入 exit 時，通知 readerThread 停止運行
						running = false;
						out.write("quit\n".getBytes());
						out.flush();
						socket.close();
						break;
					}
					if (socket.isClosed()) {
						System.out.println("Server disconnected");
						break;
					}
					if (!message.equals("")) {
						out.write((message + "\n").getBytes());
						out.flush();
						message = "";
					}
				}
			} catch (Exception e) {
				System.out.println("Server disconnected");
			} finally {
				System.out.println("Client terminated");
			}
		});
		clientThread.start();
	}

	public void sendMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public int getStartGame() {
		// 1 表示遊戲開始，0 表示遊戲未開始，-1 表示房間已滿
		return startGame;
	}

	public void setStartGame(int _startGame) {
		// 1 表示遊戲開始，0 表示遊戲未開始，-1 表示房間已滿
		startGame = _startGame;
	}

}
