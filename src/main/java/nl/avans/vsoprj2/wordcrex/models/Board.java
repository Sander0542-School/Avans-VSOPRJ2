package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static final int BOARD_SIZE = 15;


    private final Tile[][] grid;

    public Board(int gameId) {
        this.grid = this.newBoard();
        this.updateBoard(gameId);
    }

    private Tile[][] newBoard() {
        Tile[][] newGrid = new Tile[BOARD_SIZE][BOARD_SIZE];

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tile");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int xCord = result.getInt(1) - 1;
                int yCord = result.getInt(2) - 1;
                String type = result.getString(3);
                newGrid[xCord][yCord] = new Tile(this.getTileType(type));
            }
            return newGrid;
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    private TileType getTileType(String type) {
        switch (type) {
            case "*":
                return TileType.START;
            case "2L":
                return TileType.TWOLETTER;
            case "3W":
                return TileType.THREEWORD;
            case "4L":
                return TileType.FOURLETTER;
            case "4W":
                return TileType.FOURWORD;
            case "6L":
                return TileType.SIXLETTER;
            default:
                return TileType.NORMAL;
        }
    }

    public void updateBoard(int gameId) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT woorddeel, `x-waarden`, `y-waarden` FROM gelegd WHERE game_id = ?");
            statement.setInt(1, gameId);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String word = result.getString(1);
                String xValuesString = result.getString(2);
                String yValuesString = result.getString(3);

                String[] characters = word.split(",");
                int[] xValues = new int[characters.length];
                int[] yValues = new int[characters.length];

                String[] xValue = xValuesString.split(",");
                for (int i = 0; i < xValue.length; i++) {
                    xValues[i] = Integer.parseInt(xValue[i]) - 1;
                }

                String[] yValue = yValuesString.split(",");
                for (int i = 0; i < yValue.length; i++) {
                    yValues[i] = Integer.parseInt(yValue[i]) - 1;
                }

                for (int i = 0; i < characters.length; i++) {
                    System.out.println(characters[i]);
                    this.grid[xValues[i]][yValues[i]].setValue(characters[i].charAt(0));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void setValue(int x, int y, Character value) {
        this.grid[x][y].setValue(value);
    }

    public Tile getTile(int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
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
}
