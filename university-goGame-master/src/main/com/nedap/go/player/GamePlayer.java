package main.com.nedap.go.player;

import main.com.nedap.go.board.Stone;
import main.com.nedap.go.server.ClientHandler;

// Class which represent a player with a client handler (for the server), a username and a stone
public class GamePlayer {

  private String username;
  private Stone stone;

  private ClientHandler clientHandler;

  public GamePlayer(String username) {
    this.username = username;
  }

  public GamePlayer(String username, ClientHandler ch) {
    this.username = username;
    this.clientHandler = ch;
  }

  public Stone getStone() {
    return this.stone;
  }

  public void setStone(Stone stone) {
    this.stone = stone;
  }

  public String getUsername() {
    return this.username;
  }

  public ClientHandler getClientHandler() {
    return clientHandler;
  }
}
