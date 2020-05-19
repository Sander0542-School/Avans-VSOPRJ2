package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private final Tile[][] grid;
    private final Map<String, TileType> predefinedTileTypes = new HashMap<String, TileType>();

    public Board() {
        this.grid = this.newBoard();
    }

    private Tile[][] newBoard() {
        int gridSize = 15;
        Tile[][] newGrid = new Tile[gridSize][gridSize];

        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM Tile");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int xCord = result.getInt(1);
                int YCord = result.getInt(2);
                String type = result.getString(3);
                newGrid[xCord][YCord] = new Tile(this.getTileType(type));
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

    public void updateBoard(int GameId) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM `gelegd` WHERE game_id = ?");
            statement.setInt(1, GameId);
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
                    xValues[i] = Integer.parseInt(xValue[i]);
                }

                String[] yValue = yValuesString.split(",");
                for (int i = 0; i < yValue.length; i++) {
                    yValues[i] = Integer.parseInt(yValue[i]);
                }

                for (int i = 0; i < characters.length; i++) {
                    this.grid[xValues[i]][yValues[i]].setValue(characters[i].charAt(0));
                }
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }


    public Character getValue(int x, int y) {
        return this.grid[x][y].getValue();
    }

    public Tile[][] getGrid() {
        return this.grid;
    }

    public void setValue(int x, int y, Character Value) {
        this.grid[x][y].setValue(Value);
    }

}
