package main.com.nedap.go.player;

import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.Game;
import main.com.nedap.go.game.Move;

public class ComputerPlayer extends AbstractPlayer {
  private Stone stone;
  private Strategy strategy;
  public ComputerPlayer(Strategy strategy, Stone stone){
    super(strategy.getName() + "-" + stone.toString());
    this.stone = stone;
    this.strategy = strategy;
  }
  @Override
  public Move determineMove(Game game) {
    return this.strategy.determineMove(game);
  }

  @Override
  public Stone getStone() {
    return this.stone;
  }

  @Override
  public void setStone(Stone stone) {
    this.stone=stone;
  }

  @Override
  public String getUsername() {
    return this.strategy.getName();
  }

  public Strategy getStrategy() {
    return strategy;
  }

  public void setStrategy(Strategy strategy) {
    this.strategy = strategy;
  }

  @Override
  public String toString() {
    return super.toString();
  }
}