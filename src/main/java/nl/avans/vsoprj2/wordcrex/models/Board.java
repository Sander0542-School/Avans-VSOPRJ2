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

    public static final int BOARD_SIZE = 14;

    private Tile[][] grid;
    private Map<String, TileType> predefinedTileTypes = new HashMap<>();

    public Board() {
        this.populatePredefinedTileTypes();
        this.grid = this.newBoard();
    }

    private void populatePredefinedTileTypes() {
        String[] TWOLETTER = {"2,1", "6,1", "8,1", "12,1", "8,3", "6,6", "8,6", "2,7", "12,7", "6,8", "8,8", "8,11", "2,13", "6,13", "8,13", "12,13"};
        String[] THREEWORD = {"4,0", "10,0", "0,2", "14,2", "3,4", "11,4", "3,10", "11,10", "0,12", "14,12", "4,14", "10,14"};
        String[] FOURLETTER = {"7,0", "3,2", "11,2", "5,3", "9,3", "1,4", "13,4", "7,5", "7,9", "1,10", "13,10", "5,11", "9,11", "3,12", "11,12", "7,14"};
        String[] FOURWORD = {"0,7", "14,7"};
        String[] SIXLETTER = {"0,0", "14,0", "4,5", "10,5", "1,6", "13,6", "1,8", "13,8", "4,9", "10,9", "14,14", "0,14"};

        this.predefinedTileTypes.put("6,6", TileType.START);

        for (String key : TWOLETTER) {
            this.predefinedTileTypes.put(key, TileType.TWOLETTER);
        }

        for (String key : THREEWORD) {
            this.predefinedTileTypes.put(key, TileType.THREEWORD);
        }

        for (String key : FOURLETTER) {
            this.predefinedTileTypes.put(key, TileType.FOURLETTER);
        }

        for (String key : FOURWORD) {
            this.predefinedTileTypes.put(key, TileType.FOURWORD);
        }

        for (String key : SIXLETTER) {
            this.predefinedTileTypes.put(key, TileType.SIXLETTER);
        }
    }

    private Tile[][] newBoard() {
        Tile[][] newGrid = new Tile[BOARD_SIZE][BOARD_SIZE];

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                newGrid[x][y] = new Tile(x, y, this.getTileType(x, y));
            }
        }
        return newGrid;
    }

    private TileType getTileType(int x, int y) {
        return this.predefinedTileTypes.getOrDefault(x + "," + y, TileType.NORMAL);
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x > BOARD_SIZE || y < 0 || y > BOARD_SIZE) {
            return null;
        }

        return this.grid[x][y];
    }

    public boolean hasValue(int x, int y) {
        return this.getValue(x, y) != null;
    }

    public Character getValue(int x, int y) {
        Tile tile = this.getTile(x, y);

        return tile == null ? null : tile.getValue();
    }

    public Tile[][] getGrid() {
        return this.grid;
    }

    public void setValue(int x, int y, Character Value) {
        this.grid[x][y].setValue(Value);
    }

}
