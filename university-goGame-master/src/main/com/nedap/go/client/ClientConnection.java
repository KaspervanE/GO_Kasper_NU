package main.com.nedap.go.client;


import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import main.com.nedap.go.board.Stone;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.networking.SocketConnection;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.protocol.Protocol;

// Class listens to the socket and handles the input and output of the socket.
// Input is handled according to the protocol.
public class ClientConnection extends SocketConnection {

  private Lock lock = new ReentrantLock();
  private GameClient gameClient;

  protected ClientConnection(Socket socket, GameClient gameClient) throws IOException {
    super(socket);
    this.gameClient = gameClient;
  }

  @Override
  protected void handleMessage(String msg) {
    lock.lock();
    String[] split;
    if (!msg.isEmpty()) {
      split = msg.split(Protocol.SEPARATOR);
      switch (split[0]) {
        case Protocol.HELLO:
        case Protocol.QUEUED:
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.ACCEPTED:
          this.gameClient.setUsername(split[1]);
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.REJECTED:
          this.gameClient.receiveMessage(msg + "~LOGIN with another username.");
          break;
        case Protocol.GAME_STARTED:
          String[] usernames = split[1].split(",");
          int boardSize = Integer.parseInt(split[2]);
          this.gameClient.startGame(
              new GoGame(boardSize, new GamePlayer(usernames[0]), new GamePlayer(usernames[1]),false));
          this.gameClient.receiveMessage(msg);
          if (this.gameClient.isGUIActive()) {
            if (this.gameClient.getGogui()!=null) {
              // Reset the previous GUI if it exists.
              this.gameClient.resetGUI();
              this.gameClient.getGogui().setBoardSize(boardSize);
            } else {
              this.gameClient.createAndStartGUI(boardSize);
            }
          }
          break;
        case Protocol.MOVE:
          this.gameClient.doMove(Integer.parseInt(split[1]), Stone.retrieveByName(split[2]));
          if (this.gameClient.isGUIActive()) {
            this.gameClient.updateGUI(Integer.parseInt(split[1]),Stone.retrieveByName(split[2]));
          }
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.MAKE_MOVE:
          this.gameClient.receiveMessage(msg);
          if (this.gameClient.isPlayerAIOn()) {
            this.gameClient.doAIMove();
          } else {
            this.gameClient.showBoard();
            this.gameClient.receiveMessage("Make a move human: ");
          }
          break;
        case Protocol.PASS:
          this.gameClient.doPass();
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.GAME_OVER:
          // Do not remove game object to have a chance to watch the board after the game
        default:
          this.gameClient.receiveMessage(msg);
          break;

      }
    }
    lock.unlock();
  }


  @Override
  protected void handleDisconnect() {
    System.out.println("Disconnected");
  }


  public void sendGameMessage(String msg) {
    super.sendMessage(msg);
  }

  public void sendUsername(String username) {
    super.sendMessage(Protocol.LOGIN + Protocol.SEPARATOR + username);
  }

}

