package main.com.nedap.go.player;

import main.com.nedap.go.board.Stone;

public class GamePlayer implements Player{

  private String username;
  private Stone stone;

  public GamePlayer(String username) {
    this.username = username;
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
}
