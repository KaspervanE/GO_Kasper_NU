package main.com.nedap.go.game;

import main.com.nedap.go.board.Stone;

public class GoMove implements Move{

  private int index;
  private Stone stone;

  public GoMove(int ind, Stone stone) {
    this.index = ind;
    this.stone = stone;
  }

  @Override
  public Stone getStone() {
    return this.stone;
  }

  public int getIndex() {
    return this.index;
  }


}
