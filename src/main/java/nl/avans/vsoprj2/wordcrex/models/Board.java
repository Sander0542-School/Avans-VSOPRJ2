package nl.avans.vsoprj2.wordcrex.models;

public class Board {
    Tile[][] grid;

    public Board() {
        grid = newBoard();
    }

    private Tile[][] newBoard(){
        return new Tile[14][14];
    }

    public Character getValue(int x, int y){
        Character returnValue;
        returnValue = grid[x][y].getValue();
        return returnValue;
    }

    public Tile[][] getGrid(){
        return grid;
    }

    public void setTile(int x, int y, Character Value){
        grid[x][y].setValue(Value);
    }

}

class Tile{
    private Character value;
    private int tileType;

    public Tile(int type){
        tileType = type;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public int getTileType() {
        return tileType;
    }
}
