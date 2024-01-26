package main.com.nedap.go.client;

import java.io.IOException;
import java.net.Socket;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.protocol.Protocol;

public class GameClient {

  private ClientConnection clientConnection;

  private String username;
  private GoGame game;

  public GameClient(Socket socket, String username) {
    try {
      this.clientConnection = new ClientConnection(socket, this);
      this.clientConnection.start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.username=username;
  }

  public void sendGameMessage(String msg){
    this.clientConnection.sendGameMessage(msg);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void receiveMessage(String msg){
    System.out.println(msg);
  }

  public void doMove(int ind1) {
    GoMove move = new GoMove(ind1, game.getTurn().getStone());
    this.game.doMove(move);
    this.game.updateBoard(false);
  }

  public void doMove(int col, int row) {
    doMove(game.getBoard().index(col,row));
  }

  public void pass() {
    game.updateBoard(true);
  }

  public void showBoard() {
    System.out.println(game.toString());
  }


  public void displayHelpMessage(){
    String str = "The following are relevant commands:\n";
    str += "QUEUE to enter a queue\n";
    str += "MOVE~<index> to make a move at index\n";
    str += "PASS to pass\n";
    str += "RESIGN to resign\n";
    str += "BOARD to print the board with score";
    System.out.println(str);
  }

  public void handleInput(String msg){
    String[] split;
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
        default:
          sendGameMessage(msg);
          break;
      }
    }
  }

  public void startGame(GoGame game){
    this.game=game;
  }

  public void handleDisconnect(){
    this.clientConnection.handleDisconnect();
  }

  public void close(){
    this.clientConnection.close();
  }

  public void sendUsername(String username){
    this.clientConnection.sendUsername(username);
  }

}


