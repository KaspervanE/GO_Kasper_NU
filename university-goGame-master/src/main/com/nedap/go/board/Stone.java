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


  @Override
  public String toString() {
    return this.displayName;
  }
}