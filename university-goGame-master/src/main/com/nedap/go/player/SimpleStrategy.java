package main.com.nedap.go.player;

import java.util.List;
import main.com.nedap.go.game.Game;
import main.com.nedap.go.game.Move;

public class SimpleStrategy implements Strategy {

  @Override
  public String getName() {
    return "Naïve";
  }

  @Override
  public Move determineMove(Game game) {
    List<? extends Move> validMoves = game.getValidMoves();
    return validMoves.get((int) (Math.random() * validMoves.size()));
  }

}
