import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.game.Move;
import main.com.nedap.go.player.GamePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {

  private GoGame game;
  private final int boardSize = 9;
  private final GamePlayer playerOne = new GamePlayer("Black");
  private final GamePlayer playerTwo = new GamePlayer("White");


  // setup
  @BeforeEach
  public void setUp() {
    this.game = new GoGame(boardSize, playerOne, playerTwo);
  }

  @Test
  public void testGameStartsWithBlack() {
    assertEquals(game.getTurn(), playerOne);
  }

  @Test
  public void testSwitchTurnAfterMove() {
    game.doMove(new GoMove(1, Stone.BLACK));
    assertEquals(game.getTurn(), playerOne);
  }

  @Test
  public void testGetBoardSize() {
    assertEquals(game.getBoardSize(), boardSize);
  }

  @Test
  public void testIsGameOver2passes() {
    game.updateBoard(true);
    assertFalse(game.isGameover());

    game.doMove(new GoMove(1, Stone.BLACK));
    game.updateBoard(false);
    assertFalse(game.isGameover());

    game.updateBoard(true);
    assertFalse(game.isGameover());

    game.updateBoard(true);
    assertTrue(game.isGameover());
  }

  @Test
  public void testWinnerBlackAndWhite() {
    game.doMove(new GoMove(1, Stone.BLACK));
    game.updateBoard(false);
    assertEquals(game.getWinner(), playerOne);

    game.doMove(new GoMove(2, Stone.WHITE));
    game.doMove(new GoMove(3, Stone.WHITE));
    assertEquals(game.getWinner(), playerTwo);
  }

  @Test
  public void testDraw() {
    game.doMove(new GoMove(1, Stone.BLACK));
    game.updateBoard(false);
    game.doMove(new GoMove(2, Stone.WHITE));
    assertNull(game.getWinner());
  }

  @Test
  public void testValidMoves() {
    for (Move move : game.getValidMoves()) {
      assertEquals(move.getStone(), Stone.BLACK);
    }
    game.updateBoard(false);
    for (Move move : game.getValidMoves()) {
      assertEquals(move.getStone(), Stone.WHITE);
    }
    assertNotEquals(game.getValidMoves().get(0).getStone(), Stone.BLACK);

    game.doMove(new GoMove(0, Stone.WHITE));
    game.updateBoard(false);
    assertNotEquals(game.getValidMoves().get(0).getIndex(), 0);
  }
}
