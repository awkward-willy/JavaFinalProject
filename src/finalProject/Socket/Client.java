package finalProject.Socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import finalProject.Game.GameApp;
import javafx.application.Platform;

public class Client {
	private static final String SERVER_ADDR = "127.0.0.1";
	private static final int SERVER_PORT = 8000;
	private static volatile boolean running = true;
	private int startGame = 0;
	private int player = 0;
	private String message = "";
	private GameApp gameApp;

	public void setPlayer(int player) {
		this.player = player;
	}

	public int getPlayer() {
		return player;
	}

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
						if (serverMessage.startsWith("Start game!")) {
							setStartGame(1);
							setPlayer(serverMessage.contains("player 1") ? 1 : 2);
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
							} else if (serverMessage.startsWith("Start game!")) {
								setStartGame(1);
								setPlayer(serverMessage.contains("player 1") ? 1 : 2);
								System.out.println(serverMessage);
							} else if (serverMessage.startsWith("All planes placed. Position: ")) {
								// 把訊息中 Position: 之後的部分取出來
								// 因為 Position: 字串長度 10，所以取出的部分就是飛機位置的字串
								String position = serverMessage.substring(serverMessage.indexOf("Position: ") + 10);
								if (position.endsWith(",")) {
									position = position.substring(0, position.length() - 1);
								}
								// 把 position 字串轉換成 boolean[][] 陣列
								boolean[][] enemyAnswer = new boolean[10][10];
								String[] rows = position.split(",");
								for (int i = 0; i < 10; i++) {
									for (int j = 0; j < 10; j++) {
										enemyAnswer[i][j] = rows[i].charAt(j) == '1';
									}
								}
								Platform.runLater(() -> {
									gameApp.setEnemyAnswer(enemyAnswer);
									gameApp.setEnemyPlaced(true);
									gameApp.checkIfBothPlayersReady();
								});
							} else if (serverMessage.startsWith("Bomb: ")) {
								// 切分出裡面的座標
								String[] bombPosition = serverMessage.substring(6).split(",");
								int x = Integer.parseInt(bombPosition[0]);
								int y = Integer.parseInt(bombPosition[1]);
								Platform.runLater(() -> {
									gameApp.receiveBombing(x, y);
								});
								System.out.println(serverMessage);
							} else {
								System.out.println("Not handled message: " + serverMessage);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
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
						running = false;
						break;
					}
					if (!message.equals("")) {
						out.write((message + "\n").getBytes());
						out.flush();
						message = "";
					}
				}
			} catch (Exception e) {
				if (e instanceof java.net.ConnectException) {
					System.out.println("Server is not available");
				} else {
					e.printStackTrace();
				}
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

	public void setStartGame(int startGame) {
		// 1 表示遊戲開始，0 表示遊戲未開始，-1 表示房間已滿
		this.startGame = startGame;
	}

}
