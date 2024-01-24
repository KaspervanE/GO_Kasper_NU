package main.com.nedap.go.client;

import java.io.IOException;
import java.net.Socket;
import main.com.nedap.go.protocol.Protocol;

public class GameClient {

  private ClientConnection clientConnection;

  private String username;

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

  public void handleInput(String msg){
    String[] split;
    if (!msg.isEmpty()) {
      split = msg.split(Protocol.SEPARATOR);
      switch (split[0]) {
        case Protocol.LOGIN:
          this.sendUsername(split[1]);
          break;
        default:
          sendGameMessage(msg);
          break;
      }
    }
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


