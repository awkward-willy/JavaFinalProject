package finalProject;

import finalProject.Game.BluePlane;
import finalProject.Game.GreenPlane;
import finalProject.Game.Plane;
import finalProject.Game.YellowPlane;
import finalProject.Socket.Client;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameApp {
	private static final int BOARD_SIZE = 10;
	private Rectangle[][] myCells = new Rectangle[BOARD_SIZE][BOARD_SIZE];
	private Rectangle[][] enemyCells = new Rectangle[BOARD_SIZE][BOARD_SIZE];
	private boolean[][] myOccupied = new boolean[BOARD_SIZE][BOARD_SIZE];
	private boolean[][] enemyOccupied = new boolean[BOARD_SIZE][BOARD_SIZE];
	private Plane[] planes = { new BluePlane(), new YellowPlane(), new GreenPlane() };
	private int currentPlaneIndex = 0;
	private Plane plane = planes[currentPlaneIndex];
	private int lastRow = -1;
	private int lastCol = -1;
	private boolean allPlanesPlaced = false;
	private boolean enemyPlaced = false;
	private boolean inBombingMode = false;
	private int planesToPlace = planes.length;
	private int planesPlaced = 0;
	private int planesDestroyed = 0;
	private boolean isMyTurn = true;
	private Rectangle turnOverlay = new Rectangle();
	private Button endTurnButton = new Button("End Turn");
	private Client client;
	private StackPane myRoot;
	private StackPane enemyRoot;

	public void setClient(Client client) {
		this.client = client;
		System.out.println(this.client);
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
					cell.setOnMouseMoved(event -> handleMouseMove(finalRow, finalCol));
					cell.setOnMouseExited(event -> handleMouseExit(finalRow, finalCol));
					cell.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol));
				}

				cells[row][col] = cell;
				gridPane.add(cell, col, row);
			}
		}

		StackPane root = new StackPane(gridPane);
		if (!isInteractive) {
			Rectangle overlay = new Rectangle();
			overlay.setFill(new Color(0, 0, 0, 0.5));
			overlay.widthProperty().bind(root.widthProperty());
			overlay.heightProperty().bind(root.heightProperty());
			root.getChildren().add(overlay);
		}
		pane.getChildren().add(root);
		root.prefWidthProperty().bind(gridPane.widthProperty());
		root.prefHeightProperty().bind(gridPane.heightProperty());

		if (isInteractive) {
			this.myRoot = root;
			pane.setOnKeyPressed(event -> {
				if (!inBombingMode && event.getCode() == KeyCode.R) {
					plane.rotate();
					if (lastRow != -1 && lastCol != -1) {
						updatePreview(lastRow, lastCol, true, myCells, myOccupied);
					}
				}
			});

			pane.setOnMouseClicked(event -> {
				if (!inBombingMode && event.isSecondaryButtonDown()) {
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

	public void toggleOverlay() {
		turnOverlay.setVisible(!turnOverlay.isVisible());
	}

	private void handleMouseMove(int row, int col) {
		if (!allPlanesPlaced) {
			lastRow = row;
			lastCol = col;
			updatePreview(row, col, true, myCells, myOccupied);
		}
	}

	private void handleMouseExit(int row, int col) {
		if (!allPlanesPlaced) {
			updatePreview(row, col, false, myCells, myOccupied);
		}
	}

	private void handleMouseClick(int row, int col) {
		if (!allPlanesPlaced && canPlaceShape(row, col, myCells, myOccupied)) {
			markShape(row, col, myCells, myOccupied, plane.getColor());
			planesPlaced++;
			if (planesPlaced == planesToPlace) {
				allPlanesPlaced = true;
				System.out.println("全部飛機放置完畢");
				if (client != null) {
					client.sendMessage("All planes placed.");
				}
				showWaitingMessage();
				checkIfBothPlayersReady();
			} else {
				currentPlaneIndex++;
				plane = planes[currentPlaneIndex];
			}
		} else if (inBombingMode && isMyTurn) {
			handleBombing(row, col);
		}
	}

	private void checkIfBothPlayersReady() {
		if (allPlanesPlaced && enemyPlaced) {
			startBombingMode();
		}
	}

	private void startBombingMode() {
		inBombingMode = true;
		isMyTurn = true;
		myRoot.getChildren().removeIf(node -> node instanceof Rectangle && node != turnOverlay);
		turnOverlay.setVisible(!isMyTurn);
	}

	private void updatePreview(int row, int col, boolean show, Rectangle[][] cells, boolean[][] occupied) {
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
		if (!isInBounds(row, col))
			return;

		if (enemyOccupied[row][col]) {
			enemyCells[row][col].setFill(Color.BLACK);
			planesDestroyed++;
			if (planesDestroyed == planesToPlace) {
				System.out.println("遊戲結束，你贏了！");
				client.sendMessage("You win!");
			} else {
				System.out.println("命中！");
				client.sendMessage("Hit at " + row + "," + col);
			}
		} else {
			enemyCells[row][col].setFill(Color.GRAY);
			System.out.println("未命中！");
			client.sendMessage("Miss at " + row + "," + col);
		}
		endTurn();
	}

	private void endTurn() {
		isMyTurn = false;
		toggleOverlay();
		endTurnButton.setVisible(false);
		client.sendMessage("End turn");
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
		Label waitingLabel = new Label("等待對方完成飛機放置 ...");
		myRoot.getChildren().add(waitingLabel);
	}

	public void onEnemyReady() {
		enemyPlaced = true;
		if (allPlanesPlaced) {
			startBombingMode();
		}
	}

}
