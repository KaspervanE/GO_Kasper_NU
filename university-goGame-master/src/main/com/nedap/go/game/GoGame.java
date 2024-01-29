package main.com.nedap.go.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.player.Player;
import main.com.nedap.go.server.ClientHandler;

public class GoGame implements Game {

  private GamePlayer playerOne;
  private GamePlayer playerTwo;
  private GamePlayer currentPlayer;
  private Board board;
  private boolean previousPass;
  private boolean gameOver;

  private Lock lock = new ReentrantLock();

  public GoGame(int size, GamePlayer playerOne, GamePlayer playerTwo) {
    this.playerOne = playerOne;
    this.playerTwo = playerTwo;
    this.board = new Board(size);
    this.playerOne.setStone(Stone.BLACK);
    this.playerTwo.setStone(Stone.WHITE);
    this.currentPlayer = this.playerOne;
  }

  @Override
  public GamePlayer getTurn() {
    return this.currentPlayer;
  }

  public String getTurnAndStone() {
    return this.currentPlayer.getUsername() + " (" + this.currentPlayer.getStone() + ")";
  }

  public int getBoardSize() {
    return board.SIZE;
  }

  public Stone getMyStone(ClientHandler ch) {
    if (this.playerOne.getUsername().equalsIgnoreCase(ch.getUsername())) {
      return playerOne.getStone();
    } else if (this.playerTwo.getUsername().equalsIgnoreCase(ch.getUsername())) {
      return playerTwo.getStone();
    } else {
      return null;
    }
  }

  public Board getBoard() {
    return this.board;
  }

  public GamePlayer getPlayerOne() {
    return playerOne;
  }

  public GamePlayer getPlayerTwo() {
    return playerTwo;
  }

  @Override
  public boolean isGameover() {
    return this.gameOver;
  }

  @Override
  public GamePlayer getWinner() {
    int scoreBlack = getScore(Stone.BLACK);
    int scoreWhite = getScore(Stone.WHITE);
    if (scoreBlack > scoreWhite) {
      return this.playerOne;
    } else if (scoreWhite > scoreBlack) {
      return this.playerTwo;
    } else {
      return null;
    }

  }

  public GamePlayer getLoser() {
    int scoreBlack = getScore(Stone.BLACK);
    int scoreWhite = getScore(Stone.WHITE);
    if (scoreBlack > scoreWhite) {
      return this.playerTwo;
    } else if (scoreWhite > scoreBlack) {
      return this.playerOne;
    } else {
      return null;
    }

  }

  public String getWinnerWithStones() {
    if (this.getWinner() != null) {
      return "Winner " + this.getWinner().getUsername() + " (" + this.getWinner().getStone()
          + ") with a score of: " + this.getScore(this.getWinner().getStone()) + " versus: "
          + this.getScore(this.getLoser().getStone());
    } else {
      return "DRAW";
    }

  }

  @Override
  public List<GoMove> getValidMoves() {
    List<GoMove> moves = new ArrayList<GoMove>();
    for (int i = 0; i < board.SIZE * board.SIZE; i++) {
      if (board.isValidField(this.currentPlayer.getStone(), i)) {
        moves.add(new GoMove(i, this.currentPlayer.getStone()));
      }
    }
    return moves;
  }

  // return true if field is empty and move is from current player
  @Override
  public boolean isValidMove(Move move) {
    return this.board.isValidField(move.getStone(), move.getIndex())
        && move.getStone() == this.currentPlayer.getStone();
  }

  @Override
  public boolean doMove(Move move) {
    lock.lock();
    if (isValidMove(move)) {
      this.board.setField(move.getStone(), move.getIndex());
      lock.unlock();
      return true;
    }
    lock.unlock();
    return false;

  }

  // update the board by capturing the groups, updating the previousPass and switching turns.
  public void updateBoard(boolean playerPassed) {
    lock.lock();
    if (playerPassed) {
      if (this.previousPass) {
        endGame();
        lock.unlock();
        return;
      } else {
        this.previousPass = true;
      }
    } else {
      this.board.captureGroups(this.currentPlayer.getStone());
      this.previousPass = false;
    }
    switchTurns();
    lock.unlock();
  }

  // method to end the game when two consecutive passes have been played
  public void endGame() {
    this.gameOver = true;
  }

  public int getScore(Stone stone) {
    return board.determineTerritories().get(stone) + board.getStonesOnBoard(stone);
  }

  public void switchTurns() {
    if (this.currentPlayer.equals(this.playerOne)) {
      this.currentPlayer = this.playerTwo;
    } else {
      this.currentPlayer = this.playerOne;
    }
  }

  public boolean isPreviousPass() {
    return this.previousPass;
  }

  public String toString() {
    String str = board.toString();
    str += "\n" + playerOne.getUsername() + " (" + playerOne.getStone() + ") has a score of: "
        + getScore(playerOne.getStone());
    str += "\n" + playerTwo.getUsername() + " (" + playerTwo.getStone() + ") has a score of: "
        + getScore(playerTwo.getStone());
    return str;
  }
}
