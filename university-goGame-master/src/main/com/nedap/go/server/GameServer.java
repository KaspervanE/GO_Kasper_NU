package main.com.nedap.go.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import main.com.nedap.go.game.GoGame;
import main.com.nedap.go.networking.SocketServer;
import main.com.nedap.go.player.GamePlayer;
import main.com.nedap.go.player.Player;
import main.com.nedap.go.protocol.Protocol;

public class GameServer extends SocketServer {

  private Queue<ClientHandler> gameQueue;
  private List<GoGame> gamesList;
  private List<ClientHandler> clientList;
  private int boardSize;

  private Lock lock = new ReentrantLock();

  public GameServer(int port) throws IOException {
    super(port);
    System.out.println("Server connected on port: " + this.getPort());
    this.clientList = new ArrayList<>();
    this.gameQueue = new LinkedBlockingQueue<>();
    this.gamesList = new ArrayList<>();
    this.boardSize= 13;
  }

  /**
   * Returns the port on which this server is listening for connections.
   */
  @Override
  public int getPort() {
    return super.getPort();
  }

  public void setBoardSize(int boardSize) {
    this.boardSize = boardSize;
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
    gamesList.remove(game);
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
              ch.sendGameMessage(Protocol.MAKE_MOVE);
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
  public void addToQueue(ClientHandler ch) {
    lock.lock();
    if (clientHandlerIsInGame(ch)) {
      ch.sendGameMessage("You cant queue when you are in a game.");
      lock.unlock();
      return;
    }
    // If client is already in the queue, remove from queue
    if (ch.equals(this.gameQueue.peek())) {
      this.gameQueue.remove();
      ch.sendGameMessage("You are removed from the queue, to queue again type: QUEUE");
      lock.unlock();
      return;
    }
    this.gameQueue.offer(ch);
    if (this.gameQueue.size() > 1) {
      startGame(this.gameQueue.poll(), this.gameQueue.poll());
    } else {
      ch.sendGameMessage(Protocol.QUEUED);
    }
    lock.unlock();
  }

  private boolean clientHandlerIsInGame(ClientHandler ch) {
    for (GoGame game : gamesList) {
      if (ch.equals(game.getPlayerOne().getClientHandler()) || ch.equals(
          game.getPlayerTwo().getClientHandler())) {
        return true;
      }
    }
    return false;
  }

  public void startGame(ClientHandler ch1, ClientHandler ch2) {
    GoGame game = new GoGame(this.boardSize, new GamePlayer(ch1.getUsername(), ch1),
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

  public void run() {
    Scanner sc = new Scanner(System.in);
    while (true) {
      //System.out.println("Provide input:");
      this.readMessage(sc);
    }
  }

  private void readMessage(Scanner sc) {
    String input = sc.nextLine();
    this.handleInput(input);
  }

  public void handleInput(String input) {
    String[] split;
    try {
      if (!input.isEmpty()) {
        split = input.split(Protocol.SEPARATOR);
        switch (split[0].toUpperCase()) {
          case "SIZE":
            int size = Integer.parseInt(split[1]);
            if (size>3 && size < 25) {
              this.setBoardSize(Integer.parseInt(split[1]));
              System.out.println("Board size is set to: "+ size);
            } else {
              System.out.println(size+" is not a valid board size 4~25");
            }
            break;
          case "QUEUE":
            System.out.println("QUEUE: ");
            for(ClientHandler ch : gameQueue) {
              System.out.println("   - " +ch.getUsername());
            }
            break;
          case "GAMES":
            System.out.println("Current games: ");
            int counter = 0;
            for(GoGame game : gamesList) {
              System.out.println("   - " +counter+ ": "+game.getPlayerOne().getUsername() + " vs " + game.getPlayerTwo().getUsername());
              counter++;
            }
            break;
          case "BOARD":
            System.out.println(gamesList.get(Integer.parseInt(split[1])).toString());
            break;
        }
      }
    } catch (Exception e) {
      System.out.println("Unknown command");
    }
  }

  public static void main(String[] args) {
    GameServer server;
    while (true) {
      try {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter port: ");
        int port = input.nextInt();
        server = new GameServer(port);
        break;
      } catch (Exception e) {
        System.out.println("Cannot connect on this port");
      }
    }
    GameServer finalServer = server;
    new Thread(() -> {
      try {
        finalServer.acceptConnections();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).start();
    server.run();
  }

}
