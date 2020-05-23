package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class Board {
    public static final int BOARD_SIZE = 15;

    private final Tile[][] tiles = new Tile[BOARD_SIZE][BOARD_SIZE];

    public Board() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM tile");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                int xCord = result.getInt("x");
                int yCord = result.getInt("y");
                Tile.TileType type = Tile.TileType.fromDatabase(result.getString("tile_type"));

                Tile tile = new Tile(type);

                this.setTile(xCord, yCord, tile);
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void loadLetters(Game game, HashMap<Character, Integer> symbolValues) {
        Connection connection = Singleton.getInstance().getConnection();

        int currentTurnId = game.getCurrentTurn();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `woorddeel`, `turn_id`, `x-waarden`, `y-waarden` FROM `gelegd` WHERE `game_id` = ?");
            statement.setInt(1, game.getGameId());

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                String[] letters = result.getString("woorddeel").split(",");
                int[] xCords = Arrays.stream(result.getString("x-waarden").split(",")).mapToInt(Integer::parseInt).toArray();
                int[] yCords = Arrays.stream(result.getString("y-waarden").split(",")).mapToInt(Integer::parseInt).toArray();

                int turnId = result.getInt("turn_id");

                for (int i = 0; i < letters.length; i++) {
                    char letter = letters[i].charAt(0);
                    int xCord = xCords[i];
                    int yCord = yCords[i];

                    Tile tile = this.getTile(xCord, yCord);

                    tile.setConfirmed(true);
                    tile.setHighlighted((currentTurnId - 1) == turnId);
                    tile.setLetter(letter, symbolValues.get(letter));
                }
            }
        } catch (SQLException e) {
            throw new DbLoadException(e);
        }
    }

    public void setTile(int x, int y, Tile tile) {
        if (x > 0 && x <= BOARD_SIZE && y > 0 && y <= BOARD_SIZE) {
            this.tiles[x - 1][y - 1] = tile;
        }
    }

    public Tile getTile(int x, int y) {
        if (x > 0 && x <= BOARD_SIZE && y > 0 && y <= BOARD_SIZE) {
            return this.tiles[x - 1][y - 1];
        }

        return null;
    }

    public Coordinate getCoordinate(Tile tile) {
        for (int row = 1; row <= BOARD_SIZE; row++) {
            for (int column = 1; column <= BOARD_SIZE; column++) {
                Tile columnTile = this.getTile(row - 1, column - 1);

                if (tile.equals(columnTile)) {
                    return new Coordinate(row, column);
                }
            }
        }

        return null;
    }

    public Tile[][] getTiles() {
        return this.tiles;
    }

    public static class Coordinate {
        private final int x;
        private final int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }
    }
}
