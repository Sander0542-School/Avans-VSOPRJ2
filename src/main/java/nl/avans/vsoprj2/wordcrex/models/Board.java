package nl.avans.vsoprj2.wordcrex.models;

public class Board {
    Tile[][] grid;

    public Board() {
        grid = new Tile[14][14];
    }

}

class Tile{
    public int x;
    public int y;
    public Character value;
    public int tileType;
}
