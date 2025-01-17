package main;

import gui.GameFrame;
import reader.FileReader;
import reader.MapData;

public class Driver {
  public static void main(String[] args) {

    String mapName = "threeboxes1";
    String mode = "bot";

    FileReader fileReader = new FileReader();
    MapData mapData = fileReader.readFile(mapName);

    GameFrame gameFrame = new GameFrame(mapData);

    if (mode.equals("fp")) {
      gameFrame.initiateFreePlay();
    } else if (mode.equals("bot")) {
      gameFrame.initiateSolution();
    }
  }
}
