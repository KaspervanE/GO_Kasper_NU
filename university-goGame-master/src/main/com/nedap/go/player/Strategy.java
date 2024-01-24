package main.com.nedap.go.player;

import main.com.nedap.go.game.Game;
import main.com.nedap.go.game.Move;

public interface Strategy {

  public String getName();

  public Move determineMove(Game game);
}