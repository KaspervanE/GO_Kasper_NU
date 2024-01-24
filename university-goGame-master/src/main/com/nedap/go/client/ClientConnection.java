package main.com.nedap.go.client;


import java.io.IOException;
import java.net.Socket;
import main.com.nedap.go.networking.SocketConnection;
import main.com.nedap.go.protocol.Protocol;

public class ClientConnection extends SocketConnection {

  private GameClient gameClient;

  protected ClientConnection(Socket socket, GameClient gameClient) throws IOException {
    super(socket);
    this.gameClient = gameClient;
  }

  @Override
  protected void handleMessage(String msg) {
    String[] split;
    if (!msg.isEmpty()) {
      split = msg.split(Protocol.SEPARATOR);
      switch (split[0]) {
        case Protocol.HELLO:
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.ACCEPTED:
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.REJECTED:
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.QUEUED:
          this.gameClient.receiveMessage(msg);
          break;
        case Protocol.GAME_STARTED:
          this.gameClient.receiveMessage(msg);
          break;
        default:
          this.gameClient.receiveMessage(msg);
          break;
      }
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

