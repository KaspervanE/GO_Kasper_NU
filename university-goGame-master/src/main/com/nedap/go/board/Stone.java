package main.com.nedap.go.board;

public enum Stone {
  // Enum to use the different options on the board: [Empty, black or white stone]
  EMPTY(" "), BLACK("○"), WHITE("●");

  private final String displayName;

  // Initialize Stone with display name
  private Stone(String s) {
    displayName = s;
  }

  @Override
  public String toString() {
    return this.displayName;
  }
}