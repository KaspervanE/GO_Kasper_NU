package main.com.nedap.go;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import main.com.nedap.go.client.GameClient;

public class GoTUI {

  boolean isRunning;
  GameClient gameClient;

  private boolean GUIActive;

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

  public static void main(String[] args) throws UnknownHostException {
    Scanner sc = new Scanner(System.in);
    System.out.println("Do you want to play with a GUI? [y/n]");
    String withGui = sc.next();
    System.out.println("Provide IP address:");
    String ipAddress = sc.next();
    InetAddress address;
    if (ipAddress.equalsIgnoreCase("l")) {
      address = InetAddress.getLocalHost();
    } else {
      address = InetAddress.getByName(ipAddress);
    }

    System.out.println("Provide port number:");
    int portNum = sc.nextInt();
    sc.nextLine(); //Catch the \n from the next int
    try {
      GoTUI goTUI = new GoTUI(new Socket(address, portNum));
      System.out.println("Connection successful.");
      if (withGui.equalsIgnoreCase("y") || withGui.equalsIgnoreCase("yes")) {
        goTUI.activateGUI();
      }
      goTUI.run();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  private void activateGUI() {
    this.GUIActive = true;
  }

}
