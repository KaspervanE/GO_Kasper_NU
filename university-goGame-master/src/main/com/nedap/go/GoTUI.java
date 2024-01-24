package main.com.nedap.go;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import main.com.nedap.go.client.GameClient;

public class GoTUI {

  boolean isRunning;
  GameClient gameClient;

  public GoTUI(Socket socket) {
    this.gameClient = new GameClient(socket, "TempName_1");
  }

  public void run() {
    this.isRunning = true;
    Scanner sc = new Scanner(System.in);
    while (this.isRunning) {
      //System.out.println("Provide input:");
      this.readMessage(sc);
    }
  }

  private void readMessage(Scanner sc) {
    String input = sc.nextLine();
    if (input.equalsIgnoreCase("quit")) {
      this.isRunning = false;
    } else {
      this.gameClient.handleInput(input);
    }
  }

  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    System.out.println("Provide port number:");
    int portNum = sc.nextInt();
    sc.nextLine(); //Catch the \n from the next int
    try {
      GoTUI goTUI = new GoTUI(new Socket(InetAddress.getLocalHost(), portNum));
      System.out.println("Connection successful.");
      goTUI.run();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
