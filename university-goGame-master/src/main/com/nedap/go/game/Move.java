package main.com.nedap.go.game;

import main.com.nedap.go.board.Stone;

// This interface has been used from another game (TicTacToe) from the university.

/**
 * A move in a turn-based game.
 */
public interface Move {
  public Stone getStone();

  public int getIndex();
}
