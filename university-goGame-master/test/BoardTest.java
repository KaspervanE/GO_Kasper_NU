import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  public void testIsFieldIndex() {
    assertFalse(board.isValidField(-1));
    assertTrue(board.isValidField(0));
    assertTrue(board.isValidField(board.SIZE * board.SIZE - 1));
    assertFalse(board.isValidField(board.SIZE * board.SIZE));
  }

  @Test
  public void testIsFieldRowCol() {
    assertFalse(board.isValidField(board.index(-1, 0)));
    assertTrue(board.isValidField(board.index(0, 0)));
  }

  @Test
  public void testSetAndGetFieldIndex() {
    board.setField(Stone.BLACK, 0);
    assertEquals(Stone.BLACK, board.getField(0));
    assertEquals(Stone.EMPTY, board.getField(1));
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

    // Check if a field in the deepcopied board the original remains the same
    deepCopyBoard.setField(Stone.BLACK, 5);

    assertEquals(Stone.EMPTY, board.getField(5));
    assertEquals(Stone.BLACK, deepCopyBoard.getField(5));
  }

  @Test
  public void testIsEmptyFieldIndex() {
    board.setField(Stone.BLACK, 0);
    assertFalse(board.isEmptyField(0));
    assertTrue(board.isEmptyField(1));
  }

  @Test
  public void testSetFieldBothWays() {
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.WHITE, 0, 1);
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
    assertEquals(Stone.EMPTY, board.getField(1,1));
    assertEquals(Stone.EMPTY, board.getField(1,3));
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


}
