package main.com.nedap.go.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.player.Player;
import main.com.nedap.go.server.ClientHandler;
import java.time.Instant;

public class GoGame implements Game {

  private GamePlayer playerOne;
  private GamePlayer playerTwo;
  private GamePlayer currentPlayer;
  private Board board;
  private boolean previousPass;
  private boolean gameOver;

  private boolean useTimer;
  private Timer timer;
  private Lock lock = new ReentrantLock();

  public GoGame(int size, GamePlayer playerOne, GamePlayer playerTwo) {
    this(size, playerOne, playerTwo,true);
  }

  public GoGame(int size, GamePlayer playerOne, GamePlayer playerTwo, boolean useTimer) {
    this.playerOne = playerOne;
    this.playerTwo = playerTwo;
    this.board = new Board(size);
    this.playerOne.setStone(Stone.BLACK);
    this.playerTwo.setStone(Stone.WHITE);
    this.currentPlayer = this.playerOne;
    this.useTimer = useTimer;
    setTimer();
  }


  @Override
  public GamePlayer getTurn() {
    return this.currentPlayer;
  }

  public void setTimer() {
    GoGame currentGameObject = this;
    timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          currentGameObject.getTurn().getClientHandler().doResign();
        } catch (NullPointerException e) {
          // Game has already been removed
        }

      }
    }, 60000); // 1 minute
  }

  public void cancelTimer() {
    timer.cancel();
  }

  public String getTurnAndStone() {
    return this.currentPlayer.getUsername() + " (" + this.currentPlayer.getStone() + ")";
  }

  public int getBoardSize() {
    return board.SIZE;
  }

  public Stone getMyStone(String username) {
    if (this.playerOne.getUsername().equalsIgnoreCase(username)) {
      return playerOne.getStone();
    } else if (this.playerTwo.getUsername().equalsIgnoreCase(username)) {
      return playerTwo.getStone();
    } else {
      return null;
    }
  }

  public Stone getCurrentStone() {
    return getMyStone(getTurn().getUsername());
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
    if (this.useTimer) {
      setTimer();
    }
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
    if (this.useTimer) {
      cancelTimer();
    }
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
