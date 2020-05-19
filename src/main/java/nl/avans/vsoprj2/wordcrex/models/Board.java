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


    private Tile[][] grid;

    public Board(int gameId) {
        this.grid = this.newBoard();
        this.updateBoard(gameId);
    }

    private Tile[][] newBoard() {
        int gridSize = 15;
        Tile[][] newGrid = new Tile[gridSize][gridSize];

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
        } catch (SQLException e) {
            System.out.println(e.toString());
            throw new DbLoadException(e);
        }
    }

    public void setValue(int x, int y, Character Value) {
        this.grid[x][y].setValue(Value);
    }

    public Character getValue(int x, int y) {
        return this.grid[x][y].getValue();
    }

    public Tile[][] getGrid() {
        return this.grid;
    }
}
