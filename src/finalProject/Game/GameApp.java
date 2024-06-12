package finalProject.Game;

import finalProject.Socket.Client;
import javafx.beans.binding.Bindings;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

public class GameApp {
	private static final int BOARD_SIZE = 10;
	private boolean[][] enemyAnswer = new boolean[BOARD_SIZE][BOARD_SIZE];
	private Rectangle[][] myCells = new Rectangle[BOARD_SIZE][BOARD_SIZE];
	private Rectangle[][] enemyCells = new Rectangle[BOARD_SIZE][BOARD_SIZE];
	private boolean[][] myOccupied = new boolean[BOARD_SIZE][BOARD_SIZE];
	private Plane[] planes = { new YellowPlane(), new BluePlane(), new GreenPlane() };
	private int currentPlaneIndex = 0;
	private Plane plane = planes[currentPlaneIndex];
	private int lastRow = -1;
	private int lastCol = -1;
	private boolean allPlanesPlaced = false;
	private boolean enemyPlaced = false;
	private boolean inBombingMode = false;
	private int planesToPlace = planes.length;
	private int planesPlaced = 0;
	private boolean isMyTurn = true;
	private ImageView previewImageView = new ImageView();

	// 遮罩
	private Rectangle enemyOverlay = new Rectangle();
	private Label waitingLabel = new Label("等待對方完成飛機放置 ...");
	private Client client;
	private StackPane myRoot;
	private StackPane enemyRoot;
	private int player;
	private int status = 0;

	public void setStatus(int status) {
		// 0: 未開始, 1: 勝利, 2: 失敗
		this.status = status;
		if (status == 1) {
			showVictoryMessage();
		} else if (status == 2) {
			showLoseMessage();
		}
	}

	public int getStatus() {
		return status;
	}

	public void setEnemyAnswer(boolean[][] enemyAnswer) {
		this.enemyAnswer = enemyAnswer;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public void setAllPlanesPlaced(boolean allPlanesPlaced) {
		this.allPlanesPlaced = allPlanesPlaced;
	}

	public void setEnemyPlaced(boolean enemyPlaced) {
		this.enemyPlaced = enemyPlaced;
	}

	public void initialize(Pane myPane, Pane enemyPane, boolean isInteractive) {
		setupBoard(myPane, myCells, isInteractive);
		setupBoard(enemyPane, enemyCells, false);

		if (isInteractive) {
			// 讓 Pane 可以接收鍵盤事件，故先設定為可取得焦點
			myPane.setFocusTraversable(true);
			// 讓 Pane 取得焦點
			myPane.requestFocus();

			// 將所有飛機的預覽圖像添加到 myRoot
			for (Plane p : planes) {
				ImageView previewImageView = p.getImageView();
				previewImageView.setVisible(false);
				myRoot.getChildren().add(previewImageView);
			}
		}
	}

	private void setupBoard(Pane pane, Rectangle[][] cells, boolean isInteractive) {
		GridPane gridPane = new GridPane();
		gridPane.setMinSize(pane.getPrefWidth(), pane.getPrefHeight());

		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				Rectangle cell = new Rectangle();
				cell.setFill(Color.WHITE);
				cell.setStroke(Color.BLACK);
				int finalRow = row;
				int finalCol = col;

				cell.widthProperty().bind(Bindings.divide(gridPane.widthProperty(), BOARD_SIZE));
				cell.heightProperty().bind(Bindings.divide(gridPane.heightProperty(), BOARD_SIZE));

				if (isInteractive) {
					cell.setOnMouseMoved(
							event -> handleMouseMove(finalRow, finalCol, event.getSceneX(), event.getSceneY()));
					cell.setOnMouseExited(event -> handleMouseExit(finalRow, finalCol));
					cell.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol));
				} else if (getStatus() == 0) {
					cell.setOnMouseClicked(event -> handleBombing(finalRow, finalCol));
				}

				cells[row][col] = cell;
				gridPane.add(cell, col, row);
			}
		}

		StackPane root = new StackPane(gridPane);
		if (!isInteractive) {
			enemyOverlay = new Rectangle();
			enemyOverlay.setFill(new Color(0, 0, 0, 0.5));
			enemyOverlay.widthProperty().bind(root.widthProperty());
			enemyOverlay.heightProperty().bind(root.heightProperty());
			root.getChildren().add(enemyOverlay);
		}
		pane.getChildren().add(root);
		root.prefWidthProperty().bind(gridPane.widthProperty());
		root.prefHeightProperty().bind(gridPane.heightProperty());

		if (isInteractive) {
			this.myRoot = root;
			pane.setOnKeyPressed(event -> {
				if (!inBombingMode && !allPlanesPlaced && event.getCode() == KeyCode.R) {
					plane.rotate();
					if (lastRow != -1 && lastCol != -1) {
						updatePreview(lastRow, lastCol, true, myCells, myOccupied);
					}
				}
			});
		} else {
			this.enemyRoot = root;
		}
	}

	private void handleMouseMove(int row, int col, double mouseX, double mouseY) {
		if (!allPlanesPlaced) {
			lastRow = row;
			lastCol = col;
			updatePreview(row, col, true, myCells, myOccupied);

			// 更新目前飛機的預覽圖像的位置
			previewImageView = plane.getImageView();
			previewImageView.setVisible(true);
			previewImageView.setImage(plane.getImage());

			// 將滑鼠座標轉換成相對於 myRoot 的座標
			Point2D mouseSceneCoords = new Point2D(mouseX, mouseY);
			Point2D mouseLocalCoords = myRoot.sceneToLocal(mouseSceneCoords);

			previewImageView
					.setTranslateX(mouseLocalCoords.getX() - previewImageView.getBoundsInLocal().getWidth() / 2);
			previewImageView
					.setTranslateY(mouseLocalCoords.getY() - previewImageView.getBoundsInLocal().getHeight() / 2);
		}
	}

	private void handleMouseExit(int row, int col) {
		if (!allPlanesPlaced) {
			updatePreview(row, col, false, myCells, myOccupied);
			previewImageView.setVisible(false);
		}
	}

	private void handleMouseClick(int row, int col) {
		// 如果飛機已經放置完畢，則不做任何事
		if (!allPlanesPlaced && canPlaceShape(row, col, myCells, myOccupied)) {
			// 標記飛機的位置
			markShape(row, col, myCells, myOccupied, plane.getColor());
			planesPlaced++;

			// 確保預覽圖片消失
			previewImageView.setVisible(false);

			// 如果所有飛機都已經放置完畢，則顯示等待訊息
			if (planesPlaced == planesToPlace) {
				allPlanesPlaced = true;
				if (client != null) {
					client.sendMessage("All planes placed. Position: " + getPositionString(myOccupied));
					System.out.println("已發送飛機放置訊息");
				}
				showWaitingMessage();
				checkIfBothPlayersReady();
			} else {

				// 更新成顯示下一架飛機的預覽圖像
				currentPlaneIndex++;
				plane = planes[currentPlaneIndex];
				previewImageView = plane.getImageView();
			}
		}
	}

	// 將佔據的位置轉換成字串的二維陣列
	private String getPositionString(boolean[][] occupied) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				sb.append(occupied[i][j] ? "1" : "0");
			}
			sb.append(",");
		}
		return sb.toString();
	}

	public void checkIfBothPlayersReady() {
		if (allPlanesPlaced && enemyPlaced) {
			System.out.println("Both players are ready!");
			startBombingMode();
		} else {
			System.out.println("Waiting ...");
		}
	}

	private void startBombingMode() {
		inBombingMode = true;

		waitingLabel.setText("開始攻擊！");

		enemyOverlay.setVisible(false);

		if (player == 1) {
			isMyTurn = true;
		} else {
			isMyTurn = false;
		}
		// 移除 myRoot 上的所有鼠標事件
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				myCells[row][col].setOnMouseMoved(null);
				myCells[row][col].setOnMouseExited(null);
				myCells[row][col].setOnMouseClicked(null);
			}
		}
	}

	private void updatePreview(int row, int col, boolean show, Rectangle[][] cells, boolean[][] occupied) {
		// 先清除之前的預覽
		clearPreview(cells, occupied);

		boolean canPlace = canPlaceShape(row, col, cells, occupied);
		Color previewColor = canPlace ? Color.GREEN : Color.RED;

		Color[][] shape = plane.getShape();
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length; j++) {
				if (shape[i][j] != null) {
					int newRow = row + i - 1;
					int newCol = col + j - 1;
					if (isInBounds(newRow, newCol)
							&& (!occupied[newRow][newCol] || cells[newRow][newCol].getFill() == previewColor)) {
						if (show) {
							setPreviewCell(newRow, newCol, previewColor, cells);
						} else {
							restoreCell(newRow, newCol, cells, occupied);
						}
					}
				}
			}
		}
	}

	private void clearPreview(Rectangle[][] cells, boolean[][] occupied) {
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				if (cells[row][col].getFill() == Color.GREEN || cells[row][col].getFill() == Color.RED) {
					restoreCell(row, col, cells, occupied);
				}
			}
		}
	}

	private boolean canPlaceShape(int row, int col, Rectangle[][] cells, boolean[][] occupied) {
		Color[][] shape = plane.getShape();
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length; j++) {
				if (shape[i][j] != null) {
					int newRow = row + i - 1;
					int newCol = col + j - 1;
					if (!isInBounds(newRow, newCol) || occupied[newRow][newCol]) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void markShape(int row, int col, Rectangle[][] cells, boolean[][] occupied, Color color) {
		Color[][] shape = plane.getShape();
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length; j++) {
				if (shape[i][j] != null) {
					int newRow = row + i - 1;
					int newCol = col + j - 1;
					if (isInBounds(newRow, newCol)) {
						cells[newRow][newCol].setFill(color);
						occupied[newRow][newCol] = true;
					}
				}
			}
		}
	}

	private void handleBombing(int row, int col) {
		if (!isInBounds(row, col) || !isMyTurn)
			return;

		// 如果已經點擊過，則不做任何事
		Color currentColor = (Color) enemyCells[row][col].getFill();
		if (currentColor == Color.GRAY || currentColor == Color.BLACK) {
			return;
		}

		// 播放音效
		MusicPlayer.playBombMusic();

		client.sendMessage("Bomb: " + row + "," + col);
		if (enemyAnswer[row][col]) {
			enemyCells[row][col].setFill(Color.BLACK);
			checkPlaneDestruction(row, col);
		} else {
			enemyCells[row][col].setFill(Color.GRAY);
		}
		toggleTurn();
	}

	private void checkPlaneDestruction(int row, int col) {
		// 把 Answer 陣列中的位置設為 false，表示已經被擊中
		enemyAnswer[row][col] = false;

		// 如果 answer 陣列中的所有位置都是 false，表示飛機被擊落
		boolean allDestroyed = true;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (enemyAnswer[i][j]) {
					allDestroyed = false;
					break;
				}
			}
		}

		if (allDestroyed) {
			setStatus(1);
		}
	}

	private void showVictoryMessage() {
		// enemy 遮罩文字
		Label enemyLabel = new Label("好欸! 贏了!");
		enemyLabel.setFont(Font.font(16));
		enemyLabel.setTextFill(Color.GREEN);
		enemyLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");
		enemyRoot.getChildren().add(enemyLabel);

		// 取消 enemy board 的點擊事件
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				enemyCells[row][col].setOnMouseClicked(null);
			}
		}
	}

	private void showLoseMessage() {
		// enemy 遮罩文字
		Label enemyLabel = new Label("可惜... 輸了...");
		enemyLabel.setFont(Font.font(16));
		enemyLabel.setTextFill(Color.RED);
		enemyLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");
		enemyRoot.getChildren().add(enemyLabel);

		// 取消 enemy board 的點擊事件
		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				enemyCells[row][col].setOnMouseClicked(null);
			}
		}
	}

	public void receiveBombing(int row, int col) {
		if (!isInBounds(row, col) || isMyTurn)
			return;

		if (myOccupied[row][col]) {
			myCells[row][col].setFill(Color.BLACK);
		} else {
			myCells[row][col].setFill(Color.GRAY);
		}

		// 如果全部都是黑色或灰色，表示所有飛機都被擊中
		boolean allDestroyed = true;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				Color currentColor = (Color) myCells[i][j].getFill();
				if (currentColor != Color.BLACK && currentColor != Color.GRAY && myOccupied[i][j]) {
					allDestroyed = false;
					break;
				}
			}
		}
		System.out.println("Enemy bombed at: " + row + ", " + col + ", allDestroyed: " + allDestroyed);
		if (allDestroyed) {
			setStatus(2);
		}

		toggleTurn();
	}

	public void toggleTurn() {
		isMyTurn = !isMyTurn;
	}

	private void setPreviewCell(int row, int col, Color color, Rectangle[][] cells) {
		cells[row][col].setFill(color);
	}

	private void restoreCell(int row, int col, Rectangle[][] cells, boolean[][] occupied) {
		if (!occupied[row][col]) {
			cells[row][col].setFill(Color.WHITE);
		} else {
			cells[row][col].setFill(plane.getColor());
		}
	}

	private boolean isInBounds(int row, int col) {
		return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
	}

	private void showWaitingMessage() {
		waitingLabel.setVisible(true);
		// 字體大小設為 16
		waitingLabel.setFont(Font.font(16));
		// 背景有半透明效果
		waitingLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 1);");
		// 設定 ID 方便之後修改樣式
		myRoot.getChildren().add(waitingLabel);
	}

	public void setPlayer(int player) {
		this.player = player;
	}
}
