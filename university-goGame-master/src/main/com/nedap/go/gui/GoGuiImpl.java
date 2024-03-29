package main.com.nedap.go.gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GoGuiImpl extends Application {

	private final static int INITIAL_BOARD_SIZE = 19;
	private final static int INITIAL_SQUARE_SIZE = 50;

	private int currentBoardSize = INITIAL_BOARD_SIZE;
	private final int currentSquareSize = INITIAL_SQUARE_SIZE;

	private Node[][] board = null;
	private final List<Line> boardLines = new ArrayList<>();
	private Group root = null;
	private Stage primaryStage = null;
	private Node hint = null;

	private boolean mode3D = true;
	private boolean showStartupAnimation = false;

	private final PhongMaterial blackMaterial = new PhongMaterial();
	private final PhongMaterial whiteMaterial = new PhongMaterial();
	private final PhongMaterial yellowMaterial = new PhongMaterial();

	private static final CountDownLatch waitForConfigurationLatch = new CountDownLatch(1);
	private static final CountDownLatch initializationLatch = new CountDownLatch(1);

	private static GoGuiImpl instance;

	protected static boolean isInstanceAvailable() {
		return instance != null;
	}

	public static GoGuiImpl getInstance() {
		return instance;
	}

	protected void countDownConfigurationLatch() {
		waitForConfigurationLatch.countDown();
	}

	protected void setShowStartupAnimation(boolean showStartupAnimation) {
		this.showStartupAnimation = showStartupAnimation;
	}

	protected void setMode3D(boolean mode3D) {
		this.mode3D = mode3D;
	}

	@Override
	public void start(Stage primaryStage) {
		instance = this;
		initDrawMaterials();

		try {
			waitForConfigurationLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.primaryStage = primaryStage;

		primaryStage.setTitle("GO");

		initNewBoard();

		if (showStartupAnimation) {
			runStartupAnimation();
		} else {
			initializationLatch.countDown();
		}
	}

	private void initDrawMaterials() {
		blackMaterial.setDiffuseColor(Color.BLACK);
		blackMaterial.setSpecularColor(Color.LIGHTBLUE);
		whiteMaterial.setDiffuseColor(Color.WHITE);
		whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
		yellowMaterial.setDiffuseColor(Color.YELLOW);
		yellowMaterial.setSpecularColor(Color.LIGHTBLUE);
	}

	private void runStartupAnimation() {
		final long startNanoTime = System.nanoTime();

		final AnimationTimer animationTimer = new AnimationTimer() {
			int roundCount = 0;
			int lastX = 0;

			@Override
			public void handle(long currentNanoTime) {
				double t = (currentNanoTime - startNanoTime) / 50000000.0;

				int x = ((int) (t % currentBoardSize));

				if (x < lastX) {
					roundCount++;
				}

				if (x != lastX) {
					if (roundCount >= 2) {
						stop();
						clearBoard();
						initializationLatch.countDown();
					} else {
						clearBoard();
						if (x % 2 != 0) {
							drawDiagonalStoneLine(x - 1, false, roundCount != 0);
							drawDiagonalStoneLine(x, true, roundCount != 0);
							drawDiagonalStoneLine(x + 1, false, roundCount != 0);
						} else {
							drawDiagonalStoneLine(x - 1, true, roundCount != 0);
							drawDiagonalStoneLine(x, false, roundCount != 0);
							drawDiagonalStoneLine(x + 1, true, roundCount != 0);
						}
					}

					lastX = x;
				}
			}
		};
		animationTimer.start();
	}

	private void initNewBoard() {
		root = new Group();
		board = new Node[currentBoardSize][currentBoardSize];

		Scene scene = new Scene(root, (currentBoardSize + 1) * currentSquareSize,
				(currentBoardSize + 1) * currentSquareSize);
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();

		ImagePattern pattern = new ImagePattern(new Image("background_1920.jpg"));
		scene.setFill(pattern);

		initBoardLines();
	}

	private void initBoardLines() {
		root.getChildren().removeAll(boardLines);
		boardLines.clear();

		int squareSize = currentSquareSize;

		// Draw horizontal lines
		for (int i = 1; i <= currentBoardSize; i++) {
			boardLines.add(new Line(squareSize, i * squareSize, currentBoardSize * squareSize, i * squareSize));
		}

		// Draw vertical lines
		for (int i = 1; i <= currentBoardSize; i++) {
			boardLines.add(new Line(i * squareSize, squareSize, i * squareSize, currentBoardSize * squareSize));
		}

		root.getChildren().addAll(boardLines);

		if (mode3D) {
			hint = new Sphere(currentSquareSize / 2);
			((Sphere) hint).setMaterial(yellowMaterial);
		} else {
			hint = new Circle(currentSquareSize / 2);
			((Circle) hint).setFill(Color.YELLOW);
		}
		hint.setVisible(false);
		root.getChildren().add(hint);
	}

	private void drawDiagonalStoneLine(int diagonal, Boolean stoneType, boolean flip) {
		try {
			for (int x = 0; x < currentBoardSize; x++) {
				for (int y = 0; y < currentBoardSize; y++) {
					if (x + y == diagonal * 2) {
						if (!flip) {
							addStone(x, y, stoneType);
						} else {
							addStone(currentBoardSize - 1 - x, y, stoneType);
						}
					}
				}
			}
		} catch (InvalidCoordinateException e) {
			throw new IllegalStateException(e);
		}
	}

	protected void addStone(int x, int y, boolean white) throws InvalidCoordinateException {
		checkCoordinates(x, y);
		removeStone(x, y);

		if (mode3D) {
			Sphere newStone = new Sphere(currentSquareSize / 2);

			if (white) {
				newStone.setMaterial(whiteMaterial);
			} else {
				newStone.setMaterial(blackMaterial);
			}

			newStone.setTranslateX(((x + 1) * currentSquareSize));
			newStone.setTranslateY(((y + 1) * currentSquareSize));
			board[x][y] = newStone;

			root.getChildren().add(newStone);
		} else {
			Circle newStone = new Circle(((x + 1) * currentSquareSize), ((y + 1) * currentSquareSize),
					currentSquareSize / 2);

			if (white) {
				newStone.setFill(Color.WHITE);
			} else {
				newStone.setFill(Color.BLACK);
			}

			board[x][y] = newStone;
			root.getChildren().add(newStone);
		}
	}

	protected void removeStone(int x, int y) throws InvalidCoordinateException {
		checkCoordinates(x, y);

		if (board[x][y] != null) {
			root.getChildren().remove(board[x][y]);
		}
		board[x][y] = null;
	}

	protected void addAreaIndicator(int x, int y, boolean white) throws InvalidCoordinateException {
		checkCoordinates(x, y);
		removeStone(x, y);

		if (mode3D) {
			Box areaStone = new Box(currentSquareSize / 3, currentSquareSize / 3, currentSquareSize / 3);
			areaStone.setMaterial(white ? whiteMaterial : blackMaterial);
			areaStone.setTranslateX(((x + 1) * currentSquareSize));
			areaStone.setTranslateY(((y + 1) * currentSquareSize));
			board[x][y] = areaStone;
			root.getChildren().add(areaStone);
		} else {
			Rectangle areaStone = new Rectangle(((x + 1) * currentSquareSize) - currentSquareSize / 6,
					((y + 1) * currentSquareSize) - currentSquareSize / 6, currentSquareSize / 3,
					currentSquareSize / 3);
			areaStone.setFill(white ? Color.WHITE : Color.BLACK);
			board[x][y] = areaStone;
			root.getChildren().add(areaStone);
		}
	}

	protected void addHintIndicator(int x, int y) throws InvalidCoordinateException {
		hint.setTranslateX(((x + 1) * currentSquareSize));
		hint.setTranslateY(((y + 1) * currentSquareSize));
		hint.setVisible(true);
	}

	protected void removeHintIndicator() {
		hint.setVisible(false);
	}

	private void checkCoordinates(int x, int y) throws InvalidCoordinateException {
		if (x < 0 || x >= currentBoardSize) {
			throw new InvalidCoordinateException("x coordinate is outside of board range. x coordinate: " + x
					+ " board range: 0-" + (currentBoardSize - 1));
		}

		if (y < 0 || y >= currentBoardSize) {
			throw new InvalidCoordinateException("y coordinate is outside of board range. y coordinate: " + y
					+ " board range: 0-" + (currentBoardSize - 1));
		}
	}

	protected void clearBoard() {
		try {
			for (int x = 0; x < currentBoardSize; x++) {
				for (int y = 0; y < currentBoardSize; y++) {
					removeStone(x, y);
				}
			}
		} catch (InvalidCoordinateException e) {
			throw new IllegalStateException(e);
		}
	}

	protected void setBoardSize(final int size) {
		currentBoardSize = size;
		initNewBoard();
	}

	protected int getBoardSize() {
		return currentBoardSize;
	}

	protected void setInitialBoardSize(final int size) {
		currentBoardSize = size;
	}

	protected static void startGUI() {
		new Thread(() -> Application.launch(GoGuiImpl.class)).start();
	}

	protected void waitForInitializationLatch() {
		try {
			System.out.println("Attempting init of the GoGui!");
			if (!initializationLatch.await(30, TimeUnit.SECONDS)) {
				System.out.println("Initialization of the GOGUI failed!");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
