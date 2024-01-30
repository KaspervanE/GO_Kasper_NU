package main.com.nedap.go.board;

import java.util.List;

public class Group {
  private List<Integer> indexes;
  private Stone stone;

  public Group(Stone stone, List<Integer> inds){
    this.stone=stone;
    this.indexes=inds;
  }

  public List<Integer> getIndexes() {
    return indexes;
  }

  public Stone getStone() {
    return stone;
  }

}
