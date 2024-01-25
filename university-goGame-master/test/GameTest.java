import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.Game;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.player.GamePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameTest {

  private GoGame game;
  private int boardSize = 9;
  private GamePlayer playerOne = new GamePlayer("Black");
  private GamePlayer playerTwo = new GamePlayer("White");

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
    game.doMove(new GoMove(1,Stone.BLACK));
    assertEquals(game.getTurn(), playerOne);
  }

  @Test
  public void testGetBoardSize() {
    assertEquals(game.getBoardSize(),boardSize);

  }



}
