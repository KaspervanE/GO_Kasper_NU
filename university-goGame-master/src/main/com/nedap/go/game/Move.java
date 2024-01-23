package main.com.nedap.go.game;

import main.com.nedap.go.board.Stone;

/**
 * A move in a turn-based game.
 */
public interface Move {
  public Stone getStone();

  public int getIndex();
}
