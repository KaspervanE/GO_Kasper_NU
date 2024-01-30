package main.com.nedap.go.board;

public enum Stone {
  // Enum to use the different options on the board: [Empty, black or white stone]
  EMPTY(" ", "EMPTY"), BLACK("○", "BLACK"), WHITE("●", "WHITE");

  private final String displayName;
  private final String fullName;

  // Initialize Stone with display name
  private Stone(String s, String name) {
    displayName = s;
    fullName = name;
  }

  public String getName() {
    return this.fullName;
  }

  public static Stone retrieveByName(String name) {
    if (Stone.BLACK.getName().equals(name)) {
      return Stone.BLACK;
    } else if (Stone.WHITE.getName().equals(name)) {
      return Stone.WHITE;
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    return this.displayName;
  }
}