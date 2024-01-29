package main.com.nedap.go.client;

import com.sun.scenario.effect.impl.state.LinearConvolveRenderState;
import java.io.IOException;
import java.net.Socket;
import java.rmi.server.ExportException;
import java.util.List;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.protocol.Protocol;

public class GameClient {

  private ClientConnection clientConnection;

  private String username;
  private GoGame game;
  private boolean playerAIOn;

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

  public void doMove(int ind1) {
    GoMove move = new GoMove(ind1, game.getTurn().getStone());
    this.game.doMove(move);
    this.game.updateBoard(false);
  }

  public void doMove(int col, int row) {
    doMove(game.getBoard().index(col, row));
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
            this.receiveMessage("I have to do a move, if its is my turn! \n\n\n\n Implement \n\n");
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

  public Stone getThisStoneClient() {
    if (this.game.getPlayerOne().getUsername().equals(this.username)) {
      return game.getPlayerOne().getStone();
    } else {
      return game.getPlayerTwo().getStone();
    }
  }

  public boolean clientIsWinning() {
    return game.getWinner().getUsername().equals(username);
  }

  public void doAIMove() {
    List<GoMove> validMoves = game.getValidMoves();
    if (game.isPreviousPass() && clientIsWinning()) {
      sendGameMessage(Protocol.PASS);
      return;
    }
    if (validMoves.isEmpty()) {
      sendGameMessage(Protocol.PASS);
    } else {
      int moveIndex = AIstrategy.determineBestIndex(this.game.getBoard(), getThisStoneClient(),
          validMoves);
      if (moveIndex==-1) {
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


