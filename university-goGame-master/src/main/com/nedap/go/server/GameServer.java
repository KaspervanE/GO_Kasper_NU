package main.com.nedap.go.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.networking.SocketServer;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.player.Player;
import main.com.nedap.go.protocol.Protocol;

public class GameServer extends SocketServer {

  private Queue<ClientHandler> gameQueue;
  private List<GoGame> gamesList;
  private List<ClientHandler> clientList;
  private final int BOARDSIZE = 9;

  public GameServer(int port) throws IOException {
    super(port);
    System.out.println("Server connected on port: " + this.getPort());
    this.clientList = new ArrayList<>();
    this.gameQueue = new LinkedBlockingQueue<>();
    this.gamesList = new ArrayList<>();
  }

  /**
   * Returns the port on which this server is listening for connections.
   */
  @Override
  public int getPort() {
    return super.getPort();
  }

  public List<GoGame> getGamesList() {
    return gamesList;
  }


  /**
   * Accepts connections and starts a new thread for each connection. This method will block until
   * the server socket is closed, for example by invoking closeServerSocket.
   */
  @Override
  public void acceptConnections() throws IOException {
    super.acceptConnections();
  }

  /**
   * Closes the server socket. This will cause the server to stop accepting new connections. If
   * called from a different thread than the one running acceptConnections, then that thread will
   * return from acceptConnections.
   */
  @Override
  public synchronized void close() {
    super.close();
  }

  /**
   * Creates a new connection handler for the given socket.
   */
  @Override
  protected void handleConnection(Socket socket) {
    try {
      ServerConnection sc = new ServerConnection(socket);
      ClientHandler ch = new ClientHandler(sc, this);
      sc.setClientHandler(ch);
      this.addClient(ch);
      sc.start();
      this.helloMessage(ch);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
    }
  }

  public void addClient(ClientHandler client) {
    this.clientList.add(client);
  }

  public void removeClient(ClientHandler client) {
    this.clientList.remove(client);
  }

  public void removeGameByResign(GoGame game, String username) {
    String winner;
    if (game.getPlayerOne().getUsername().equals(username)) {
      winner = game.getPlayerTwo().getUsername();
    } else {
      winner = game.getPlayerOne().getUsername();
    }
    game.getPlayerOne().getClientHandler().sendGameMessage(
        Protocol.GAME_OVER + Protocol.SEPARATOR + "Winner " + winner + ", Because " + username
            + " could not take it anymore and resigned...");
    game.getPlayerTwo().getClientHandler().sendGameMessage(
        Protocol.GAME_OVER + Protocol.SEPARATOR + "Winner " + winner + ", Because " + username
            + " could not take it anymore and resigned...");
  }

  public void removeGame(GoGame game) {
    for (GoGame listInGame : gamesList) {
      if (listInGame.equals(game)) {
        informClientsMessages(game, Protocol.GAME_OVER);
        gamesList.remove(game);
        return;
      }
    }
  }

  public void informClientsMessages(GoGame game, String protocolMsg) {
    String[] split = protocolMsg.split(Protocol.SEPARATOR);
    for (ClientHandler ch : clientList) {
      if (ch.equals(game.getPlayerOne().getClientHandler()) ||
          ch.equals(game.getPlayerTwo().getClientHandler())) {
        switch (split[0]) {
          case Protocol.GAME_OVER:
            ch.sendGameMessage(
                split[0] + Protocol.SEPARATOR + game.getWinnerWithStones());
            break;
          case Protocol.MAKE_MOVE:
            if (ch.equals(game.getTurn().getClientHandler())) {
              System.out.println(game.getBoard().toString());
            }
            break;
          case Protocol.PASS:
            ch.sendGameMessage(split[0] + Protocol.SEPARATOR + game.getCurrentStone().getName());
            break;
          case Protocol.MOVE:
            ch.sendGameMessage(split[0] + Protocol.SEPARATOR + split[1] + Protocol.SEPARATOR
                + game.getCurrentStone().getName());
            break;
        }

      }
    }
  }

  // Add client handler/player to the queue, if more than 1 player is in the queue, start game
  public synchronized void addToQueue(ClientHandler ch) {
    // If client is already in the queue, remove from queue
    if (ch.equals(this.gameQueue.peek())) {
      this.gameQueue.remove();
      ch.sendGameMessage("You are removed from the queue, to queue again type: QUEUE");
      return;
    }
    this.gameQueue.offer(ch);
    if (this.gameQueue.size() > 1) {
      startGame(this.gameQueue.poll(), this.gameQueue.poll());
    } else {
      ch.sendGameMessage(Protocol.QUEUED);
    }
  }

  public void startGame(ClientHandler ch1, ClientHandler ch2) {
    GoGame game = new GoGame(this.BOARDSIZE, new GamePlayer(ch1.getUsername(), ch1),
        new GamePlayer(ch2.getUsername(), ch2));
    this.gamesList.add(game);
    ch1.setGame(game);
    ch2.setGame(game);
    ch1.sendGameMessage(
        Protocol.GAME_STARTED + Protocol.SEPARATOR + ch1.getUsername() + "," + ch2.getUsername()
            + Protocol.SEPARATOR + game.getBoardSize());
    ch2.sendGameMessage(
        Protocol.GAME_STARTED + Protocol.SEPARATOR + ch1.getUsername() + "," + ch2.getUsername()
            + Protocol.SEPARATOR + game.getBoardSize());
  }

  public void helloMessage(ClientHandler ch) {
    String helloMsg = "To log in, please respond with: LOGIN~<username>";
    ch.sendGameMessage(Protocol.HELLO + Protocol.SEPARATOR + helloMsg);
  }

  public void handleChatMessage(ClientHandler client, String msg) {
    System.out.println(client.getUsername() + " said: " + msg);
    for (ClientHandler ch : this.clientList) {
      ch.sendGameMessage(msg);
    }
  }

  public boolean isValidUsername(String newUser) {
    for (ClientHandler ch : clientList) {
      if (newUser.equalsIgnoreCase(ch.getUsername())) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
    Scanner input = new Scanner(System.in);
    try {
      System.out.println("Enter port: ");
      int port = input.nextInt();
      GameServer server = new GameServer(port);
      new Thread(() -> {
        try {
          server.acceptConnections();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }).start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
