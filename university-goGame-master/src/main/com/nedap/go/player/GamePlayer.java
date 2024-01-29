package main.com.nedap.go.player;

import main.com.nedap.go.board.Stone;
import main.com.nedap.go.server.ClientHandler;

public class GamePlayer implements Player{

  private String username;
  private Stone stone;

  private ClientHandler clientHandler;

  public GamePlayer(String username) {
    this.username = username;
  }

  public GamePlayer(String username, ClientHandler ch) {
    this.username = username;
    this.clientHandler=ch;
  }

  @Override
  public Stone getStone() {
    return this.stone;
  }

  @Override
  public void setStone(Stone stone) {
    this.stone = stone;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  public ClientHandler getClientHandler() {
    return clientHandler;
  }
}
