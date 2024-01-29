package main.com.nedap.go.server;

import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.game.GoMove;
import main.com.nedap.go.protocol.Protocol;

public class ClientHandler {

  private ServerConnection serverConnection;
  private GameServer server;
  private String username;
  private final String TEMP_USER = "temp_user";

  private GoGame game;

  public ClientHandler(ServerConnection sc, GameServer server) {
    this.serverConnection = sc;
    this.server = server;
    this.username = TEMP_USER;
  }

  public String getUsername() {
    return username;
  }

  public void receiveUsername(String username) {
    if (server.isValidUsername(username) && !username.equals(TEMP_USER)) {
      this.username = username;
      this.sendGameMessage(Protocol.ACCEPTED + Protocol.SEPARATOR + username);
    } else {
      this.sendGameMessage(Protocol.REJECTED + Protocol.SEPARATOR + username);
    }

  }

  public void setGame(GoGame game) {
    this.game = game;
  }

  public void receiveChatMessage(String msg) {
    this.server.handleChatMessage(this, msg);
  }

  public void handleDisconnect() {
    if (game != null) {
      doResign();
    }
    System.out.println("Connection lost to client: " + this.username);
    server.removeClient(this);
  }

  public void sendGameMessage(String msg) {
    this.serverConnection.sendGameMessage(msg);
  }

  public void queuePlayer() {
    if (!username.equals(TEMP_USER)) {
      this.server.addToQueue(this);
    } else {
      this.serverConnection.sendGameMessage(
          Protocol.ERROR + Protocol.SEPARATOR + "You need to login before you can queue.");
    }
  }

  public void doMove(int index) {
    if (game.getTurn().getClientHandler().equals(this)) {
      Stone stone = game.getMyStone(this);
      // Do move and check if it is executed
      if (game.doMove(new GoMove(index, stone))) {
        this.server.informClientsMessages(game, Protocol.MOVE + Protocol.SEPARATOR + index);
        game.updateBoard(false);
        this.server.informClientsMessages(game, Protocol.MAKE_MOVE);
      } else {
        // invalid move, do nothing (according to protocol)
      }
    } else {
      // Not your turn
    }

  }

  // Transform 2D move into 1D, get game/board to get the board size.
  public void do2DMove(int ind1, int ind2) {
    int index1D = game.getBoard().index(ind1, ind2);
    doMove(index1D);
  }

  public void doPass() {
    if (game.getTurn().getClientHandler().equals(this)) {
      this.server.informClientsMessages(game, Protocol.PASS);
      game.updateBoard(true);
      if (game.isGameover()) {
        // Remove game from server and update Clients
        server.removeGame(game);
        game = null;
      } else {
        this.server.informClientsMessages(game, Protocol.MAKE_MOVE);
      }
    }
  }

  public void doResign() {
    if (game.getTurn().getClientHandler().equals(this)) {
      game.endGame();
      server.removeGameByResign(game, username);
      game = null;
    }
  }


}
