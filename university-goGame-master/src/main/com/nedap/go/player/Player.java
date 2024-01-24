package main.com.nedap.go.player;

import main.com.nedap.go.board.Stone;

/**
 * A player of a turn-based game. The interface on purpose does not contain any methods.
 * If an object represents a player for a game, it should implement this interface.
 */
public interface Player {

  public Stone getStone();

  public void setStone(Stone stone);

  public String getUsername();
}