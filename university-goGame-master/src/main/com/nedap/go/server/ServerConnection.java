package main.com.nedap.go.server;

import java.io.IOException;
import java.net.Socket;
import main.com.nedap.go.networking.SocketConnection;
import main.com.nedap.go.protocol.Protocol;

public class ServerConnection extends SocketConnection {
  private ClientHandler clientHandler;

  protected ServerConnection(Socket socket) throws IOException {
    super(socket);
  }

  public void handleMessage(String msg) {
    System.out.println("FROM "+ this.clientHandler.getUsername()+ ": " + msg);
    String[] split;
    try {
      if (!msg.isEmpty()) {
        split = msg.split(Protocol.SEPARATOR);
        switch (split[0].toUpperCase()) {
          case Protocol.LOGIN:
            this.clientHandler.receiveUsername(split[1]);
            break;
          case Protocol.QUEUE:
            this.clientHandler.queuePlayer();
            break;
          case Protocol.MOVE:
            if (split.length>1) {
              if (split.length>2) {
                this.clientHandler.do2DMove(Integer.parseInt(split[1]),Integer.parseInt(split[2]));
              } else {
                this.clientHandler.doMove(Integer.parseInt(split[1]));
              }
            }
            break;
          case Protocol.PASS:
            this.clientHandler.doPass();
            break;
          case Protocol.RESIGN:
            this.clientHandler.doResign();
            break;
          default:
            break;
        }
      }
    } catch (Exception e) {
      sendGameMessage(Protocol.ERROR+Protocol.SEPARATOR+"Something went wrong, make sure to follow the protocol.");
    }


  }

  public void sendGameMessage(String msg) {
    super.sendMessage(msg);
  }

  public void handleDisconnect() {
    this.clientHandler.handleDisconnect();
  }

  public ClientHandler getClientHandler() {
    return clientHandler;
  }

  public void setClientHandler(ClientHandler clientHandler) {
    this.clientHandler = clientHandler;
  }
}
