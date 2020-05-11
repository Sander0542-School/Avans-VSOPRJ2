package nl.avans.vsoprj2.wordcrex.models;

enum TileType {
    EMTPY,
    TWOLETTER,
    THREEWORD,
    FOURLETTER,
    FOURWORD,
    SIXLETTER
}


public class Board {
    Tile[][] grid;

    public Board() {
        grid = newBoard();
    }

    private Tile[][] newBoard() {
        return new Tile[14][14];
    }

    public Character getValue(int x, int y) {
        Character returnValue;
        returnValue = grid[x][y].getValue();
        return returnValue;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public void setTile(int x, int y, Character Value) {
        grid[x][y].setValue(Value);
    }

}

class Tile {
    private Character value;
    private final TileType tileType;

    public Tile(TileType type) {
        tileType = type;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public TileType getTileType() {
        return tileType;
    }
}
