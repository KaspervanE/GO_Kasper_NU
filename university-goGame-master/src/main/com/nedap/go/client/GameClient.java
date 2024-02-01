package main.com.nedap.go.client;

import com.sun.scenario.effect.impl.state.LinearConvolveRenderState;
import java.io.IOException;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.gui.GoGuiIntegrator;
import main.com.nedap.go.protocol.Protocol;

/*
  Client for the game GO, is connected to a client connection.
  Handles the moves and stores the game/board to keep track of the game.
  Also handles input from the TUI to provide extra information.
 */


public class GameClient {

  private ClientConnection clientConnection;

  private String username;
  private Stone currentStone;
  private GoGame game;
  private boolean playerAIOn;
  private boolean GUIActive;
  private GoGuiIntegrator gogui;

  public GameClient(Socket socket, String username) {
    try {
      this.clientConnection = new ClientConnection(socket, this);
      this.clientConnection.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.username = username;
    this.playerAIOn = false;
  }

  public void sendGameMessage(String msg) {
    this.clientConnection.sendGameMessage(msg);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void receiveMessage(String msg) {
    System.out.println(msg);
  }

  public void doMove(int ind1, Stone stone) {
    GoMove move = new GoMove(ind1, stone);
    this.game.doMove(move);
    this.game.updateBoard(false);
  }

  public void pass() {
    game.updateBoard(true);
  }

  public void showBoard() {
    System.out.println(game.toString());
  }


  public void displayHelpMessage() {
    String str = "The following are relevant commands:\n";
    str += "LOGIN~<username> to login with a username";
    str += "QUEUE to enter a queue\n";
    str += "MOVE~<index> to make a move at index\n";
    str += "PASS to pass\n";
    str += "RESIGN to resign\n";
    str += "BOARD to print the board with score";
    System.out.println(str);
  }

  // Handles user input for more information
  public void handleInput(String msg) {
    String[] split;
    try {
      if (!msg.isEmpty()) {
        split = msg.split(Protocol.SEPARATOR);
        switch (split[0].toUpperCase()) {
          case Protocol.LOGIN:
            this.sendUsername(split[1]);
            break;
          case "HELP":
            displayHelpMessage();
            break;
          case "BOARD":
            showBoard();
            break;
          case "AION":
            this.setAIplayer(true);
            if (game != null && this.currentStone == game.getTurn().getStone()) {
              doAIMove();
            }
            this.receiveMessage("AI player is activated.");
            break;
          case "AIOFF":
            this.setAIplayer(false);
            this.receiveMessage("AI player is deactivated.");
            break;
          case "AIMOVE":
            doAIMove();
            this.receiveMessage("AI does move!");
            break;
          default:
            sendGameMessage(msg);
            break;
        }
      }
    } catch (Exception e) {
      System.out.println(Protocol.ERROR + Protocol.SEPARATOR
          + "Something went wrong, make sure to follow the protocol. (HELP)");
    }

  }

  public void startGame(GoGame game) {
    this.game = game;
    this.currentStone = game.getMyStone(this.username);
  }

  public void handleDisconnect() {
    this.clientConnection.handleDisconnect();
  }

  public void close() {
    this.clientConnection.close();
  }

  public void sendUsername(String username) {
    this.clientConnection.sendUsername(username);
  }

  public void setAIplayer(boolean b) {
    this.playerAIOn = b;
  }

  public boolean isPlayerAIOn() {
    return playerAIOn;
  }

  public Stone getStone() {
    return this.currentStone;
  }

  public boolean clientIsWinning() {
    return game.getWinner().getUsername().equals(username);
  }

  public boolean isGUIActive() {
    return GUIActive;
  }

  public void setGUIActive(boolean GUIActive) {
    this.GUIActive = GUIActive;
  }

  public GoGuiIntegrator getGogui() {
    return gogui;
  }

  // Start the GUI
  public void createAndStartGUI(int boardSize) {
    gogui = new GoGuiIntegrator(true, true, boardSize);
    gogui.startGUI();
  }

  // Update the GUI with the move and territory
  public void updateGUI(int index, Stone stone) {
    moveGui(index, stone);
    updateTerritoryGUI();
  }

  public void moveGui(int index, Stone stone) {
    if (stone.getName().equals("BLACK")) {
      gogui.addStone(game.getBoard().getColFromIndex(index), game.getBoard().getRowFromIndex(index),
          false);
    } else if (stone.getName().equals("WHITE")) {
      gogui.addStone(game.getBoard().getColFromIndex(index), game.getBoard().getRowFromIndex(index),
          true);
    }
  }

  public void updateTerritoryGUI() {
    Map<Stone, Set<Integer>> area = this.game.getBoard().getAreaIndexes();
    for (Stone stone : area.keySet()) {
      for (int index : area.get(stone)) {
        if (stone.getName().equals("BLACK")) {
          gogui.addAreaIndicator(game.getBoard().getColFromIndex(index),
              game.getBoard().getRowFromIndex(index), false);
        } else if (stone.getName().equals("WHITE")) {
          gogui.addAreaIndicator(game.getBoard().getColFromIndex(index),
              game.getBoard().getRowFromIndex(index), true);
        } else {
          // No function provided for removing the areaIndicator, so this...
          gogui.addStone(game.getBoard().getColFromIndex(index),
              game.getBoard().getRowFromIndex(index), true);
          gogui.removeStone(game.getBoard().getColFromIndex(index),
              game.getBoard().getRowFromIndex(index));
        }
      }
    }
  }

  public void resetGUI() {
    gogui.clearBoard();
  }


  // Let the AI do the move by calling AI Strategy determine index
  public void doAIMove() {
    List<GoMove> validMoves = game.getValidMoves();
    if (game.isPreviousPass() && clientIsWinning()) {
      // When you are ahead and opponent passes, pass for the win
      sendGameMessage(Protocol.PASS);
      return;
    }
    if (validMoves.isEmpty()) {
      sendGameMessage(Protocol.PASS);
    } else {
      int moveIndex = AIstrategy.determineBestIndex(this.game.getBoard(), getStone(),
          validMoves);
      if (moveIndex == -1) {
        sendGameMessage(Protocol.PASS);
      } else {
        sendGameMessage(Protocol.MOVE + Protocol.SEPARATOR + moveIndex);
      }
    }
  }

  public void doPass() {
    game.updateBoard(true);
  }
}


