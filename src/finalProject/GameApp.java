package finalProject;

import finalProject.Socket.Client;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

abstract class Plane {
	protected Color[][] shape;
	protected Color color;
	private boolean destroyed = false;

	public Plane(Color[][] shape, Color color) {
		this.shape = shape;
		this.color = color;
	}

	public void rotate() {
		int size = shape.length;
		Color[][] newShape = new Color[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				newShape[j][size - 1 - i] = shape[i][j];
			}
		}
		shape = newShape;
	}

	public Color[][] getShape() {
		return shape;
	}

	public Color getColor() {
		return color;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
}

class BluePlane extends Plane {
	public BluePlane() {
		super(new Color[][] {
				{ null, Color.BLUE, Color.BLUE },
				{ Color.BLUE, null, Color.BLUE },
				{ null, Color.BLUE, null } }, Color.BLUE);
	}
}

class RedPlane extends Plane {
	public RedPlane() {
		super(new Color[][] {
				{ null, Color.RED, Color.RED },
				{ Color.RED, null, Color.RED },
				{ null, Color.RED, null } }, Color.RED);
	}
}

class YellowPlane extends Plane {
	public YellowPlane() {
		super(new Color[][] {
				{ null, Color.YELLOW, Color.YELLOW },
				{ Color.YELLOW, null, Color.YELLOW },
				{ null, Color.YELLOW, null } }, Color.YELLOW);
	}
}

class GreenPlane extends Plane {
	public GreenPlane() {
		super(new Color[][] {
				{ null, Color.GREEN, Color.GREEN },
				{ Color.GREEN, null, Color.GREEN },
				{ null, Color.GREEN, null } }, Color.GREEN);
	}
}

public class GameApp {
	private static final int BOARD_SIZE = 10;
	private Rectangle[][] cells = new Rectangle[BOARD_SIZE][BOARD_SIZE];
	private boolean[][] occupied = new boolean[BOARD_SIZE][BOARD_SIZE];
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

	public void initialize(Pane pane) {
		GridPane gridPane = new GridPane();
		// GridPane 的 minSize 與 pane 的 prefWidth 與 prefHeight 相同
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

				cell.setOnMouseMoved(event -> handleMouseMove(finalRow, finalCol));
				cell.setOnMouseExited(event -> handleMouseExit(finalRow, finalCol));
				cell.setOnMouseClicked(event -> handleMouseClick(finalRow, finalCol));

				cells[row][col] = cell;
				gridPane.add(cell, col, row);
			}
		}

		StackPane root = new StackPane(gridPane);
		pane.getChildren().add(root);
		root.prefWidthProperty().bind(gridPane.widthProperty());
		root.prefHeightProperty().bind(gridPane.heightProperty());

		pane.setOnKeyPressed(event -> {
			if (!inBombingMode && event.getCode() == KeyCode.R) {
				plane.rotate();
				if (lastRow != -1 && lastCol != -1) {
					updatePreview(lastRow, lastCol, true);
				}
			}
		});

		pane.setOnMouseClicked(event -> {
			if (!inBombingMode && event.isSecondaryButtonDown()) {
				plane.rotate();
				if (lastRow != -1 && lastCol != -1) {
					updatePreview(lastRow, lastCol, true);
				}
			}
		});

	}

	public void toggleOverlay() {
		turnOverlay.setVisible(!turnOverlay.isVisible());
	}

	private void handleMouseMove(int row, int col) {
		if (!allPlanesPlaced) {
			lastRow = row;
			lastCol = col;
			updatePreview(row, col, true);
		}
	}

	private void handleMouseExit(int row, int col) {
		if (!allPlanesPlaced) {
			updatePreview(row, col, false);
		}
	}

	private void handleMouseClick(int row, int col) {
		if (!allPlanesPlaced && canPlaceShape(row, col)) {
			markShape(row, col);
			planesPlaced++;
			if (planesPlaced == planesToPlace) {
				allPlanesPlaced = true;
				System.out.println("全部飛機放置完畢");
				client.sendMessage("All planes placed.");
			} else {
				currentPlaneIndex++;
				plane = planes[currentPlaneIndex];
			}
		} else if (inBombingMode && isMyTurn) {
			handleBombing(row, col);
		}
	}

	private void updatePreview(int row, int col, boolean show) {
		boolean canPlace = canPlaceShape(row, col);
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
							setPreviewCell(newRow, newCol, previewColor);
						} else {
							restoreCell(newRow, newCol);
						}
					}
				}
			}
		}
	}

	private boolean canPlaceShape(int row, int col) {
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

	private boolean isInBounds(int row, int col) {
		return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
	}

	private void setPreviewCell(int row, int col, Color color) {
		if (isInBounds(row, col)) {
			cells[row][col].setFill(color);
		}
	}

	private void restoreCell(int row, int col) {
		if (isInBounds(row, col)) {
			if (!occupied[row][col]) {
				cells[row][col].setFill(Color.WHITE);
			} else {
				cells[row][col].setFill(plane.getColor());
			}
		}
	}

	private void markShape(int row, int col) {
		Color[][] shape = plane.getShape();
		for (int i = 0; i < shape.length; i++) {
			for (int j = 0; j < shape[i].length; j++) {
				if (shape[i][j] != null) {
					markCell(row + i - 1, col + j - 1, plane.getColor());
				}
			}
		}
	}

	private void markCell(int row, int col, Color color) {
		if (isInBounds(row, col)) {
			occupied[row][col] = true;
			cells[row][col].setFill(color);
		}
	}

	private void showWaitingAndBombingButton() {
		Rectangle overlay = new Rectangle();
		overlay.setFill(new Color(0, 0, 0, 0.5));
		overlay.widthProperty().bind(cells[0][0].getScene().widthProperty());
		overlay.heightProperty().bind(cells[0][0].getScene().heightProperty());

		Label waitingLabel = new Label("等待對方");
		waitingLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

		Button bombingButton = new Button("開始遊戲");
		bombingButton.setOnAction(e -> enterBombingMode());

		StackPane root = (StackPane) cells[0][0].getScene().getRoot();
		root.getChildren().addAll(overlay, waitingLabel, bombingButton);

		StackPane.setAlignment(bombingButton, javafx.geometry.Pos.BOTTOM_CENTER);

		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				cells[row][col].setDisable(true);
			}
		}
	}

	private void enterBombingMode() {
		inBombingMode = true;
		isMyTurn = true;
		StackPane root = (StackPane) cells[0][0].getScene().getRoot();
		root.getChildren().removeIf(node -> !(node instanceof GridPane || node instanceof Button));
		turnOverlay.setVisible(!isMyTurn);

		for (int row = 0; row < BOARD_SIZE; row++) {
			for (int col = 0; col < BOARD_SIZE; col++) {
				cells[row][col].setDisable(false);
			}
		}
	}

	private void handleBombing(int row, int col) {
		if (occupied[row][col]) {
			cells[row][col].setFill(Color.BLACK);
			checkPlaneDestruction(row, col);
		}
		endTurn();
	}

	private void checkPlaneDestruction(int row, int col) {
		for (Plane p : planes) {
			boolean planeDestroyed = true;
			Color[][] shape = p.getShape();
			outerLoop: for (int i = 0; i < shape.length; i++) {
				for (int j = 0; j < shape[i].length; j++) {
					if (shape[i][j] != null) {
						for (int r = 0; r < BOARD_SIZE; r++) {
							for (int c = 0; c < BOARD_SIZE; c++) {
								if (cells[r][c].getFill() == p.getColor() && cells[r][c].getFill() != Color.BLACK) {
									planeDestroyed = false;
									break outerLoop;
								}
							}
						}
					}
				}
			}
			if (planeDestroyed && !p.isDestroyed()) {
				p.setDestroyed(true);
				planesDestroyed++;
				System.out.println("飛機被摧毀，顏色：" + p.getColor());
			}
		}

		if (planesDestroyed == planes.length) {
			showVictoryMessage();
		}
	}

	private void showVictoryMessage() {
		StackPane root = (StackPane) cells[0][0].getScene().getRoot();
		root.getChildren().clear();

		Label victoryLabel = new Label("WIN！");
		victoryLabel.setStyle("-fx-font-size: 36px; -fx-text-fill: green;");

		root.getChildren().add(victoryLabel);
	}

	private void endTurn() {
		isMyTurn = !isMyTurn;

		StackPane root = (StackPane) cells[0][0].getScene().getRoot();
		root.getChildren().remove(turnOverlay);
		root.getChildren().add(turnOverlay);
		root.getChildren().remove(endTurnButton);
		root.getChildren().add(endTurnButton);

		turnOverlay.setVisible(!isMyTurn);
		endTurnButton.setVisible(!isMyTurn);
	}

}
