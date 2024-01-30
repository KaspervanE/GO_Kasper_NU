package main.com.nedap.go.client;

import java.util.List;
import java.util.Random;
import main.com.nedap.go.board.Board;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.game.Move;
import org.testng.internal.EclipseInterface;

public class AIstrategy {

  public static int determineBestIndex(Board board, Stone currentStone, List<GoMove> validMoves) {
    String msgString = createMessage(board, currentStone, validMoves);
    String responseString = ChatGPTAPIClient.chatGPT(msgString);
    // DEBUGGING: System.out.println("First search result: " + responseString);
    // Check if the answer can be parsed to an integer.
    boolean exitLoop = false;
    int counter = 0;
    while (!exitLoop && counter < 5) {
      try {
        Integer.parseInt(responseString);
        exitLoop = true;
      } catch (Exception e) { // Sometimes ChatGPT answers with a sentence rather than a number.
        counter++;
        if (getIntFromString(responseString)!=null) {
          System.out.println(responseString);
          responseString=getIntFromString(responseString);
          break;
        }
        // Try one more time to get only an integer back.
        responseString = ChatGPTAPIClient.chatGPT(
            msgString + " Only the number! (index that is suggested)");
        // DEBUGGING: System.out.println("Second search result: " + responseString);
      }
    }

    return interpretResponse(responseString, validMoves);
  }

  private static int interpretResponse(String responseString, List<GoMove> validMoves) {
    int response;
    try {
      response = Integer.parseInt(responseString);
      System.out.println("AI++ " + response);
      if (response == -1) {
        return response;
      }
      if (!isResponseInList(response, validMoves)) {
        int newResponse = validMoves.get(validMoves.size()/2).getIndex();
        System.out.println("NotValid++ " + response);
        return newResponse;
      }
    } catch (Exception e) {
      response = validMoves.get(validMoves.size()/2).getIndex();
      System.out.println("Random++ " + e);
    }
    return response;
  }

  public static String getIntFromString(String input) {
    String[] inputArray = input.split("[ .,]");
    for (String text : inputArray){
      try {
        Integer.parseInt(text);
        return text;
      } catch (NumberFormatException e) {
        // Continue loop
      }
    }
    return null;

  }

  public static boolean isResponseInList(int response, List<GoMove> validMoves) {
    for (GoMove move : validMoves) {
      if (move.getIndex() == response) {
        return true;
      }
    }
    return false;
  }

  public static String createMessage(Board board, Stone currentStone, List<GoMove> validMoves) {
    String msgString =
        "In the game GO a " + board.SIZE + " by " + board.SIZE + " board is given. "
            + "The index of the top left is 0 and top right is " + new String(
            String.valueOf(board.SIZE - 1)) + ". "
            + "The Chinees rules are applied and suicide moves are allowed, we are only interested in the territory and the stones on the board, captures are NOT counted. "
            + "When Black has its stones at the following indexes: " + getIndexesStones(board,
            Stone.BLACK) + ". "
            + "and white has its stones at indexes: " + getIndexesStones(board,
            Stone.WHITE) + ". "
            + "Where would the best place be for " + nameStone(currentStone)
            + " to play the stone if you would believe AlphaGo Zero. "
            + "It is okay if you need to take a second to think, i prefer a close estimate over no answer at all. "
            + "If a pass would be the best option, answer with -1."
            + "The only indexes allowed are: " + getValidIndexes(validMoves) + " and -1 ."
            + "Provide your answer with only one number (the index). Only one number!";

    return msgString;
  }

  private static String getValidIndexes(List<GoMove> validMoves) {
    String indexString = "";
    for (GoMove move : validMoves) {
      if (!indexString.equals("")) {
        indexString += ", ";
      }
      indexString += move.getIndex() + " ";
    }
    return indexString;
  }

  public static String nameStone(Stone stone) {
    if (stone.toString().equals("○")) {
      return "black";
    } else {
      return "white";
    }
  }

  public static String getIndexesStones(Board board, Stone stone) {
    String indexesStone = "";
    if (board.getStonesOnBoard(stone) > 0) {
      for (int ind = 0; ind < board.SIZE * board.SIZE; ind++) {
        if (board.getField(ind) == stone) {
          indexesStone += ind + ", ";
        }
      }
    } else {
      indexesStone += "No stones on the board yet";
    }

    return indexesStone;
  }

  public static void main(String[] args) {
    AIstrategy strat = new AIstrategy();
    Board board = new Board(3);

    strat.determineBestIndex(board, Stone.BLACK, null);
    System.out.println(Stone.BLACK.toString());
  }


}
