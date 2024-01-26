import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoardTest {

  private Board board;

  // setup
  @BeforeEach
  public void setUp() {
    board = new Board(9);
  }

  //-------------------------- Test general board functions ------------------------

  @Test
  public void testIsFieldOnBoardAndEmptyIndex() {
    assertFalse(board.isEmptyField(-1));
    assertTrue(board.isEmptyField(0));
    assertTrue(board.isEmptyField(board.SIZE * board.SIZE - 1));
    assertFalse(board.isEmptyField(board.SIZE * board.SIZE));
  }

  @Test
  public void testIsFieldRowCol() {
    assertFalse(board.isEmptyField(board.index(-1, 0)));
    assertTrue(board.isEmptyField(board.index(0, 0)));
  }

  @Test
  public void testSetAndGetFieldIndex() {
    board.setField(Stone.BLACK, 0);
    assertEquals(Stone.BLACK, board.getField(0));
    assertEquals(Stone.EMPTY, board.getField(1));
  }

  @Test
  public void testIsValidField() {
    board.setField(Stone.BLACK, 0);
    assertFalse(board.isValidField(Stone.BLACK,board.index(-1, 0)));
    assertFalse(board.isValidField(Stone.BLACK,board.index(0, 0)));
    assertFalse(board.isValidField(Stone.WHITE,board.index(0, 0)));
    assertTrue(board.isValidField(Stone.BLACK ,board.index(1, 0)));
  }

  @Test
  public void testIsPreviousBoard() {
    board.setField(Stone.BLACK, 0,1);
    board.setField(Stone.BLACK, 1,0);
    board.setField(Stone.BLACK, 1,2);
    board.setField(Stone.BLACK, 2,1);
    board.setField(Stone.WHITE, 1,1);
    board.captureGroups(Stone.BLACK);
    // When white is captured you get a previous board
    assertTrue(board.isFormerBoard(board));
    // Therefore white is not allowed to place a stone there
    assertFalse(board.isValidField(Stone.WHITE, board.index(1,1)));
    // Black is, since there is a new board position
    assertTrue(board.isValidField(Stone.BLACK, board.index(1,1)));


  }

  @Test
  public void testSetup() {
    for (int i = 0; i < board.SIZE * board.SIZE; i++) {
      assertEquals(Stone.EMPTY, board.getField(i));
    }
  }

  @Test
  public void testReset() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, board.SIZE * board.SIZE - 1);
    board.reset();
    assertEquals(Stone.EMPTY, board.getField(0));
    assertEquals(Stone.EMPTY, board.getField(board.SIZE * board.SIZE - 1));
  }

  @Test
  public void testDeepCopy() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, board.SIZE * board.SIZE - 1);
    Board deepCopyBoard = board.deepCopy();

    // Test if all the fields are the same
    for (int i = 0; i < board.SIZE * board.SIZE; i++) {
      assertEquals(board.getField(i), deepCopyBoard.getField(i));
    }

    // Check if a field in the deep copied board the original remains the same
    deepCopyBoard.setField(Stone.BLACK, 5);

    assertEquals(Stone.EMPTY, board.getField(5));
    assertEquals(Stone.BLACK, deepCopyBoard.getField(5));
  }

  @Test
  public void testIsEmptyFieldIndex() {
    board.setField(Stone.BLACK, 0);
    assertFalse(board.isEmptyField(-1));
    assertFalse(board.isEmptyField(0));
    assertTrue(board.isEmptyField(1));
  }

  @Test
  public void testSetFieldBothWays() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, 1, 0);
    assertEquals(Stone.BLACK, board.getField(0, 0));
    assertEquals(Stone.WHITE, board.getField(1));
  }

  //-------------------------- Test Capture related functions ------------------------

  @Test
  public void testNoCaptureGroup() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, 1);
    board.captureGroups(Stone.WHITE);
    assertEquals(Stone.BLACK, board.getField(0));
    assertEquals(Stone.WHITE, board.getField(1));
    assertEquals(Stone.EMPTY, board.getField(2));
  }

  @Test
  public void testCaptureGroupSingle() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, 1);
    board.setField(Stone.WHITE, 9);
    board.captureGroups(Stone.WHITE);
    assertEquals(Stone.EMPTY, board.getField(0));
  }

  @Test
  public void testCaptureGroup() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.BLACK, 9);
    board.setField(Stone.WHITE, 1);
    board.setField(Stone.WHITE, 10);
    board.setField(Stone.WHITE, 18);
    board.captureGroups(Stone.WHITE);
    assertEquals(Stone.EMPTY, board.getField(0));
    assertEquals(Stone.EMPTY, board.getField(9));
  }

  @Test
  public void testMultipleCaptures() {
    board.setField(Stone.BLACK, 0, 1);
    board.setField(Stone.BLACK, 1, 0);
    board.setField(Stone.BLACK, 2, 1);
    board.setField(Stone.BLACK, 1, 2);
    board.setField(Stone.BLACK, 1, 4);
    board.setField(Stone.BLACK, 2, 3);
    board.setField(Stone.BLACK, 0, 3);
    board.setField(Stone.WHITE, 1, 3);
    board.setField(Stone.WHITE, 1, 1);
    board.captureGroups(Stone.BLACK);
    assertEquals(Stone.EMPTY, board.getField(1, 1));
    assertEquals(Stone.EMPTY, board.getField(1, 3));
  }

  @Test
  public void testCaptureOverSuicide() {
    board.setField(Stone.BLACK, 0, 1);
    board.setField(Stone.BLACK, 1, 0);
    board.setField(Stone.BLACK, 2, 1);
    board.setField(Stone.BLACK, 1, 2);
    board.setField(Stone.WHITE, 0, 2);
    board.setField(Stone.WHITE, 1, 3);
    board.setField(Stone.WHITE, 2, 2);
    board.setField(Stone.WHITE, 1, 1);
    board.captureGroups(Stone.WHITE);
    assertEquals(Stone.WHITE, board.getField(1, 1));
    assertEquals(Stone.EMPTY, board.getField(1, 2));
  }

  //-------------------------- Test Score related functions ------------------------

  @Test
  public void testScoresZeroStones() {
    Map<Stone, Integer> territoriesScores = board.determineTerritories();
    assertEquals(territoriesScores.get(Stone.BLACK),0);
    assertEquals(territoriesScores.get(Stone.WHITE),0);
    assertEquals(board.getStonesOnBoard(Stone.WHITE),0);
    assertEquals(board.getStonesOnBoard(Stone.BLACK),0);
  }

  @Test
  public void testScoresOneStone() {
    board.setField(Stone.BLACK,0);
    Map<Stone, Integer> territoriesScores = board.determineTerritories();
    assertEquals(territoriesScores.get(Stone.BLACK),80);
    assertEquals(territoriesScores.get(Stone.WHITE),0);
    assertEquals(board.getStonesOnBoard(Stone.WHITE),0);
    assertEquals(board.getStonesOnBoard(Stone.BLACK),1);
  }

  @Test
  public void testScoresTwoStones() {
    board.setField(Stone.BLACK,0);
    board.setField(Stone.WHITE,1);
    Map<Stone, Integer> territoriesScores = board.determineTerritories();
    assertEquals(territoriesScores.get(Stone.BLACK),0);
    assertEquals(territoriesScores.get(Stone.WHITE),0);
    assertEquals(board.getStonesOnBoard(Stone.WHITE),1);
    assertEquals(board.getStonesOnBoard(Stone.BLACK),1);
    assertEquals(board.getStonesOnBoard(Stone.EMPTY),0);
  }

  @Test
  public void testScoresStonesTerritory() {
    board.setField(Stone.BLACK, 29);
    board.setField(Stone.WHITE, 1);
    board.setField(Stone.WHITE, 10);
    board.setField(Stone.WHITE, 18);
    board.captureGroups(Stone.WHITE);
    assertEquals(Stone.EMPTY, board.getField(0));
    assertEquals(Stone.EMPTY, board.getField(9));
    Map<Stone, Integer> territoriesScores = board.determineTerritories();
    assertEquals(territoriesScores.get(Stone.BLACK),0);
    assertEquals(territoriesScores.get(Stone.WHITE),2);
    assertEquals(board.getStonesOnBoard(Stone.WHITE),3);
    assertEquals(board.getStonesOnBoard(Stone.BLACK),1);
  }

  @Test
  public void testToString() {
    board.setField(Stone.BLACK, 0, 1);
    board.setField(Stone.BLACK, 1, 0);
    board.setField(Stone.BLACK, 2, 1);
    board.setField(Stone.BLACK, 1, 2);
    board.setField(Stone.WHITE, 0, 2);
    board.setField(Stone.WHITE, 1, 3);
    board.setField(Stone.WHITE, 2, 2);
    board.setField(Stone.WHITE, 1, 1);
    assertTrue(board.toString().contains(String.valueOf(board.SIZE* board.SIZE-1)));
    assertTrue(board.toString().contains(String.valueOf(0)));
    assertTrue(board.toString().contains("+"));
    assertTrue(board.toString().contains("-"));
    assertTrue(board.toString().contains("|"));
    assertTrue(board.toString().contains("○"));
    assertTrue(board.toString().contains("●"));
  }

  @Test
  // Can be tested by the equals method but this but emphasis on the equals method
  public void testEquals() {
    assertTrue(board.equals(board));
    assertFalse(board.equals(Stone.BLACK));
    assertTrue(board.equals(board.deepCopy()));
    assertFalse(board.equals(null));

  }


}
