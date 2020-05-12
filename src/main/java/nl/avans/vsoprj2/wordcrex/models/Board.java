package nl.avans.vsoprj2.wordcrex.models;

import java.util.HashMap;
import java.util.Map;

public class Board {
    public enum TileType {
        NORMAL,
        START,
        TWOLETTER,
        THREEWORD,
        FOURLETTER,
        FOURWORD,
        SIXLETTER
    }

    private Tile[][] grid;
    private Map<String, TileType> predefinedTileTypes = new HashMap<String, TileType>();

    public Board() {
        populatePredefinedTileTypes();
        grid = newBoard();
    }

    private void populatePredefinedTileTypes() {
        String[] TWOLETTER = {"2,1", "6,1", "8,1", "12,1", "8,3", "6,6", "8,6", "2,7", "12,7", "6,8", "8,8", "8,11", "2,13", "6,13", "8,13", "12,13"};
        String[] THREEWORD = {"4,0", "10,0", "0,2", "14,2", "3,4", "11,4", "3,10", "11,10", "0,12", "14,12", "4,14", "10,14"};
        String[] FOURLETTER = {"7,0", "3,2", "11,2", "5,3", "9,3", "1,4", "13,4", "7,5", "7,9", "1,10", "13,10", "5,11", "9,11", "3,12", "11,12", "7,14"};
        String[] FOURWORD = {"0,7", "14,7"};
        String[] SIXLETTER = {"0,0", "14,0", "4,5", "10,5", "1,6", "13,6", "1,8", "13,8", "4,9", "10,9", "14,14", "0,14"};

        predefinedTileTypes.put("6,6", TileType.START);

        for (String key : TWOLETTER) {
            predefinedTileTypes.put(key, TileType.TWOLETTER);
        }

        for (String key : THREEWORD) {
            predefinedTileTypes.put(key, TileType.THREEWORD);
        }

        for (String key : FOURLETTER) {
            predefinedTileTypes.put(key, TileType.FOURLETTER);
        }

        for (String key : FOURWORD) {
            predefinedTileTypes.put(key, TileType.FOURWORD);
        }

        for (String key : SIXLETTER) {
            predefinedTileTypes.put(key, TileType.SIXLETTER);
        }
    }

    private Tile[][] newBoard() {
        int gridSize = 14;
        Tile[][] newGrid = new Tile[gridSize][gridSize];

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                newGrid[x][y] = new Tile(getTileType(x, y));
            }
        }
        return newGrid;
    }

    private TileType getTileType(int x, int y) {
        return predefinedTileTypes.containsKey(x + "," + y) ? predefinedTileTypes.get(x + "," + y) : TileType.NORMAL;
    }


    public Character getValue(int x, int y) {
        Character returnValue;
        returnValue = grid[x][y].getValue();
        return returnValue;
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public void setValue(int x, int y, Character Value) {
        grid[x][y].setValue(Value);
    }

}