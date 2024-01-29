package main.com.nedap.go.client;


import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.networking.SocketConnection;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.protocol.Protocol;

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
              new GoGame(boardSize, new GamePlayer(usernames[0]), new GamePlayer(usernames[1])));
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.MOVE:
          if (isInteger(split[2])) {
            this.gameClient.doMove(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
          } else {
            this.gameClient.doMove(Integer.parseInt(split[1]));
          }
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.MAKE_MOVE:
          if (split[1].split(" ")[0].equals(gameClient.getUsername())) {
            if (this.gameClient.isPlayerAIOn()) {
              this.gameClient.doAIMove();
            } else {
              this.gameClient.showBoard();
              this.gameClient.receiveMessage("Make a move human: ");
            }
          } else {
            this.gameClient.receiveMessage(msg);
          }
          break;

        case Protocol.PASS:
          this.gameClient.doPass();
          this.gameClient.receiveMessage(msg);
          break;
        default:
          this.gameClient.receiveMessage(msg);
          break;
      }
    }
    lock.unlock();
  }

  public static boolean isInteger(String str) {
    try {
      Integer.parseInt(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
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

  public GameClient getChatClient() {
    return gameClient;
  }
}

