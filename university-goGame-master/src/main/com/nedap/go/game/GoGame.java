package main.com.nedap.go.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.player.Player;
import main.com.nedap.go.server.ClientHandler;

public class GoGame implements Game {

  private Player playerOne;
  private Player playerTwo;
  private Player currentPlayer;
  private Board board;
  private boolean previousPass;
  private boolean gameOver;

  public GoGame(int size, Player playerOne, Player playerTwo) {
    this.playerOne = playerOne;
    this.playerTwo = playerTwo;
    this.board = new Board(size);
    this.playerOne.setStone(Stone.BLACK);
    this.playerTwo.setStone(Stone.WHITE);
    this.currentPlayer = this.playerOne;
  }

  @Override
  public Player getTurn() {
    return this.currentPlayer;
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

  public Player getPlayerOne() {
    return playerOne;
  }

  public Player getPlayerTwo() {
    return playerTwo;
  }

  @Override
  public boolean isGameover() {
    return this.gameOver;
  }

  @Override
  public Player getWinner() {
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

  public String getWinnerWithStones(){
    return this.getWinner().getUsername() + " (" + this.getWinner().getStone() + ") with a score of: " + this.getScore(this.getWinner().getStone());
  }

  @Override
  public List<? extends Move> getValidMoves() {
    List<GoMove> moves = new ArrayList<GoMove>();
    for (int i = 0; i < board.SIZE * board.SIZE; i++) {
      if (board.isEmptyField(i)) {
        moves.add(new GoMove(i, this.currentPlayer.getStone()));
      }
    }
    return moves;
  }

  // return true if field is empty and move is from current player
  @Override
  public boolean isValidMove(Move move) {
    return this.board.isEmptyField(move.getIndex())
        && move.getStone() == this.currentPlayer.getStone();
  }

  @Override
  public boolean doMove(Move move) {
    if (isValidMove(move)) {
      this.board.setField(move.getStone(), move.getIndex());
      return true;
    }
    return false;
  }

  // update the board by capturing the groups, updating the previousPass and switching turns.
  public void updateBoard(boolean playerPassed) {
    if (playerPassed) {
      if (this.previousPass) {
        endGame();
        return;
      } else {
        this.previousPass = true;
      }
    } else {
      this.board.captureGroups(this.currentPlayer.getStone());
      this.previousPass = false;
    }
    switchTurns();
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
}
