package main.com.nedap.go.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Board {

  public final int SIZE; // Size of the board
  private Stone[] fields; // Array for storing the stones on the board

  // Initialize board given a size
  public Board(int size) {
    this.SIZE = size;
    this.fields = new Stone[SIZE * SIZE];
    reset();
  }

  // Create a deep copy of the board
  public Board deepCopy() {
    Board newBoard = new Board(SIZE);
    for (int i = 0; i < SIZE * SIZE; i++) {
      newBoard.fields[i] = this.fields[i];
    }
    return newBoard;
  }

  // Get field
  public Stone getField(int index) {
    return fields[index];
  }

  public Stone getField(int row, int col) {
    return getField(index(row, col));
  }

  // Provide the index given the row and column
  public int index(int row, int col) {
    return SIZE * row + col;
  }

  // Set field
  public void setField(Stone stone, int ind) {
    this.fields[ind] = stone;
  }

  public void setField(Stone stone, int row, int col) {
    this.fields[index(row, col)] = stone;
  }

  // Function to check if the field is valid
  public boolean isValidField(int ind) {
    return (ind >= 0 && ind < SIZE * SIZE);
  }

  // Function to check if the field is empty and valid (on the board)
  public boolean isEmptyField(int ind) {
    if (this.isValidField(ind)) {
      return this.fields[ind].equals(Stone.EMPTY);
    }
    return false;
  }

  // Get the row given an index
  public int getRowFromIndex(int ind) {
    return ind / SIZE;
  }

  // get the column given an index
  public int getColFromIndex(int ind) {
    return ind % SIZE;
  }

  // Return the neighbor above (-1 if border)
  public int NTop(int ind) {
    if ((getRowFromIndex(ind) - 1) < 0) {
      return -1;
    } else {
      return ind - SIZE;
    }
  }

  // Return the neighbor to the left (-1 if border)
  public int NLeft(int ind) {
    if ((getColFromIndex(ind) - 1) < 0) {
      return -1;
    } else {
      return ind - 1;
    }
  }

  // Return the neighbor below (-1 if border)
  public int NBottom(int ind) {
    if ((getRowFromIndex(ind) + 1) >= SIZE) {
      return -1;
    } else {
      return ind + SIZE;
    }
  }

  // Return the neighbor to the right (-1 if border)
  public int NRight(int ind) {
    if ((getColFromIndex(ind) + 1) >= SIZE) {
      return -1;
    } else {
      return ind + 1;
    }
  }

  // Check if the two indexes are opposites (other color or border)
  public boolean isOpposite(int ind1, int ind2) {
    if (ind2 == -1) {
      return true;
    } else {
      return getField(ind1) != getField(ind2)
          && getField(ind1) != Stone.EMPTY && getField(ind2) != Stone.EMPTY;
    }
  }

  // Check if all adjacent stones are the opposite (of border)
  public boolean isSingleGroup(int ind) {
    return isOpposite(ind, NTop(ind)) && isOpposite(ind, NRight(ind))
        && isOpposite(ind, NBottom(ind)) && isOpposite(ind, NLeft(ind));
  }

  /* Create groups of stones on the board
    A stones are grouped when the same color stones are adjacent in the y or x direction.
    And surrounded by either a stone or border, for all stones.
    A group can consist of a single stone when it is
      entirely surrounded by the opposite color/border.
   */
  public List<Group> createGroups() {
    List<Group> tempGroups = new ArrayList<Group>();
    for (int i = 0; i < SIZE * SIZE; i++) {
      // Check if the field is non-empty and not in a group already
      if (getField(i) != Stone.EMPTY && !indexInGroups(i, tempGroups)) {
        // Check if the index is a single group
        if (isSingleGroup(i)) {
          tempGroups.add(new Group(getField(i), Arrays.asList(i)));
        } else {
          // Check if the current index is part of a group
          List<Integer> currentIndexes = findGroup(i);
          if (currentIndexes != null) {
            tempGroups.add(new Group(getField(i), currentIndexes));
          }
        }
      }

    }
    return tempGroups;
  }

  public List<Integer> findGroup(int ind) {
    List<Integer> tempInd = new ArrayList<Integer>();
    tempInd.add(ind);
    for (int i = 0; i < tempInd.size(); i++) {
      List<Integer> neighbors = findGroupNeighbors(tempInd.get(i));
      if (neighbors != null) {
        for (int neighbor : neighbors) {
          if (!tempInd.contains(neighbor)) {
            tempInd.add(neighbor);
          }
        }
      } else {
        return null;
      }
    }
    return tempInd;
  }

  /*
  Get adjacent fields
  Return Null if part of the group is not enclosed
   */
  public List<Integer> findGroupNeighbors(int ind) {
    List<Integer> tempIndexes = new ArrayList<Integer>();
    List<Integer> neighbors = Arrays.asList(NTop(ind), NRight(ind), NBottom(ind), NLeft(ind));
    // create a list of neighbor indexes and check if they are part of the group
    for (int neighborIndex : neighbors) {
      int result = getNeighbor(ind, neighborIndex);
      if (result == -1) {
        return null;
      } else if (result >= 0) {
        tempIndexes.add(result);
      }
    }
    return tempIndexes;
  }

  /*
  Evaluate the adjacent field:
    If empty, return -1 --> no group
    If opposite, return -2 --> closed from that side
    If same color, return index --> add to the group
   */
  public int getNeighbor(int ind1, int ind2) {
    if (isOpposite(ind1, ind2)) {
      return -2;
    } else if (getField(ind2).equals(Stone.EMPTY)) {
      return -1;
    } else {
      return ind2;
    }
  }

  // Given an array of groups, check if index 'ind' is already part of a group
  public boolean indexInGroups(int ind, List<Group> groups) {
    if (groups.isEmpty()) {
      return false;
    }
    for (Group group : groups) {
      for (int i : group.getIndexes()) {
        if (ind == i) {
          return true;
        }
      }
    }
    return false;
  }

  // Capture the groups if any
  public void captureGroups(Stone currentStone) {
    List<Group> groups = createGroups();
    if (!groups.isEmpty()) {
      boolean differentGroupsExists = areDifferentGroups(groups);
      for (Group group : groups) {
        // When the stone is different or only when group exists, capture group
        if (group.getStone() != currentStone || !differentGroupsExists) {
          List<Integer> indexes = group.getIndexes();
          for (int index : indexes) {
            this.setField(Stone.EMPTY, index);
          }
        }
      }
    }
  }

  public boolean areDifferentGroups(List<Group> groups) {
    Stone firstStone = groups.get(0).getStone();
    for (Group group : groups) {
      if (group.getStone() != firstStone) {
        return true;
      }
    }
    return false;
  }

  public Map<Stone, Integer> determineTerritories() {
    Map<Stone, Integer> territoriesScores = new HashMap<>();
    territoriesScores.put(Stone.EMPTY, 0);
    territoriesScores.put(Stone.BLACK, 0);
    territoriesScores.put(Stone.WHITE, 0);
    Set<Integer> visited = new HashSet<>();

    for (int ind = 0; ind < SIZE * SIZE; ind++) {
      if (this.getField(ind) == Stone.EMPTY && !visited.contains(ind)) {
        determineTerritory(ind, visited, territoriesScores);
      }
    }

    return territoriesScores;
  }

  public void determineTerritory(int ind, Set<Integer> visited,
      Map<Stone, Integer> territoriesScores) {
    Set<Integer> territoryPoints = new HashSet<>();
    Stone currentTerritory = Stone.EMPTY; // Default to neutral territory

    neighborSearch(ind, visited, territoryPoints);

    // Determine the stone color of the territory based on surrounding stones
    for (int currentIndex : territoryPoints) {
      Stone stone = this.getField(currentIndex);
      if (stone.equals(Stone.BLACK)) {
        if (currentTerritory.equals(Stone.EMPTY)) {
          currentTerritory = Stone.BLACK;
        } else if (currentTerritory.equals(Stone.WHITE)) {
          currentTerritory = Stone.EMPTY;
          break;
        }
      } else if (stone.equals(Stone.WHITE)) {
        if (currentTerritory.equals(Stone.EMPTY)) {
          currentTerritory = Stone.WHITE;
        } else if (currentTerritory.equals(Stone.BLACK)) {
          currentTerritory = Stone.EMPTY;
          break;
        }
      }
    }

    int areaTerritory = 0;
    for (int currentIndex : territoryPoints) {
      if (this.getField(currentIndex).equals(Stone.EMPTY)) {
        areaTerritory++;
      }
    }
    // Add score to relevant stone color
    territoriesScores.put(currentTerritory,
        territoriesScores.get(currentTerritory) + areaTerritory);

  }

  // Looking for neighbors based on the depth-first search algorithm
  public void neighborSearch(int ind, Set<Integer> visited, Set<Integer> territoryPoints) {
    if (ind < 0 || ind >= (SIZE * SIZE) || visited.contains(ind)) {
      return;
    }
    territoryPoints.add(ind);
    // If empty explore the neighbors
    if (this.getField(ind).equals(Stone.EMPTY)) {
      visited.add(ind);
      List<Integer> neighbors = Arrays.asList(NTop(ind), NRight(ind), NBottom(ind), NLeft(ind));
      for (int neighborIndex : neighbors) {
        neighborSearch(neighborIndex, visited, territoryPoints);
      }
    }
  }

  public int getStonesOnBoard(Stone stone) {
    if (stone == Stone.EMPTY) {
      return 0;
    }
    int numStones = 0;
    for (int i = 0; i < SIZE * SIZE; i++) {
      if (this.getField(i) == stone) {
        numStones++;
      }
    }
    return numStones;
  }


  // Resets the board to all empty fields
  public void reset() {
    for (int i = 0; i < SIZE * SIZE; i++) {
      this.fields[i] = Stone.EMPTY;
    }
  }

  @Override
  public String toString() {
    String str = "";
    String splitLine = "---";
    for (int i = 0; i < SIZE - 1; i++) {
      splitLine += "+---";
    }
    str += "|" + splitLine + "|\n";
    for (int i = 0; i < SIZE; i++) {
      str += "|";
      for (int j = 0; j < SIZE; j++) {
        if (this.getField(j + SIZE * i) == Stone.EMPTY) {
          if (j + SIZE * i < 10) {
            str += " " + (j + SIZE * i) + " |";
          } else if (j + SIZE * i < 100) {
            str += " " + (j + SIZE * i) + "|";
          } else {
            str += (j + SIZE * i) + "|";
          }
        } else {
          str += " " + this.getField(j + SIZE * i) + " |";
        }
      }
      str += "\n|" + splitLine + "|\n";
    }
    return str;
  }


  public static void main(String[] args) {
    Board board = new Board(11);
    board.setField(Stone.BLACK, 0);
    board.setField(Stone.BLACK, 9);
    board.setField(Stone.WHITE, 1);
    board.setField(Stone.WHITE, 10);
    board.setField(Stone.WHITE, 18);
    board.captureGroups(Stone.WHITE);
    System.out.println(board.toString());
    Map<Stone, Integer> territoriesScores = board.determineTerritories();
    for (Stone stone : territoriesScores.keySet()) {
      int totalScore = territoriesScores.get(stone) + board.getStonesOnBoard(stone);
      if (stone == Stone.EMPTY) {
        System.out.println("The neutral area consist of: " + totalScore + " points.");
      } else {
        System.out.println(stone + " has a total of: " + totalScore + " points.");
      }
    }
  }

}