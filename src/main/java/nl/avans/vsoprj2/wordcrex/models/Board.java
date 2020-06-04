package nl.avans.vsoprj2.wordcrex.models;

import javafx.scene.control.Alert;
import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Board {
    public static final int BOARD_SIZE = 15;

    private final Tile[][] tiles = new Tile[BOARD_SIZE][BOARD_SIZE];

    public Board() {
        this.loadTiles();
    }

    public void loadTiles() {
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
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de verschillende tiles.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
        }
    }

    public void loadLetters(Game game, HashMap<Character, Integer> symbolValues) {
        Connection connection = Singleton.getInstance().getConnection();

        this.loadTiles();

        String table = Singleton.getInstance().getUser().getUsername().equals(game.getUsernamePlayer1()) ? "gelegdplayer1" : "gelegdplayer2";

        int currentTurnId = game.getCurrentTurn();
        int playerLastTurn = 0;
        int turnId = 0;

        try {
            PreparedStatement playerStatement = connection.prepareStatement(String.format("SELECT `woorddeel`, `turn_id`, `x-waarden`, `y-waarden` FROM `%s` WHERE `game_id` = ? ORDER BY `turn_id` DESC LIMIT 1;", table));
            playerStatement.setInt(1, game.getGameId());

            ResultSet playerResult = playerStatement.executeQuery();

            if (playerResult.next()) {
                playerLastTurn = playerResult.getInt("turn_id");
            }

            PreparedStatement gelegdstatement = connection.prepareStatement("SELECT `woorddeel`, `turn_id`, `x-waarden`, `y-waarden` FROM `gelegd` WHERE `game_id` = ?");
            gelegdstatement.setInt(1, game.getGameId());

            ResultSet gelegdResult = gelegdstatement.executeQuery();

            while (gelegdResult.next()) {
                String[] letters = gelegdResult.getString("woorddeel").split(",");
                int[] xCords = Arrays.stream(gelegdResult.getString("x-waarden").split(",")).mapToInt(Integer::parseInt).toArray();
                int[] yCords = Arrays.stream(gelegdResult.getString("y-waarden").split(",")).mapToInt(Integer::parseInt).toArray();

                turnId = gelegdResult.getInt("turn_id");

                for (int i = 0; i < letters.length; i++) {
                    char letter = letters[i].charAt(0);
                    int xCord = xCords[i];
                    int yCord = yCords[i];

                    Tile tile = this.getTile(xCord, yCord);

                    tile.setConfirmed(true);
                    tile.setHighlighted(gelegdResult.isLast() && turnId == playerLastTurn);
                    tile.setLetter(letter, symbolValues.get(letter));
                }
            }

            if (playerLastTurn > turnId) {
                String[] letters = playerResult.getString("woorddeel").split(",");
                int[] xCords = Arrays.stream(playerResult.getString("x-waarden").split(",")).mapToInt(Integer::parseInt).toArray();
                int[] yCords = Arrays.stream(playerResult.getString("y-waarden").split(",")).mapToInt(Integer::parseInt).toArray();

                for (int i = 0; i < letters.length; i++) {
                    char letter = letters[i].charAt(0);
                    int xCord = xCords[i];
                    int yCord = yCords[i];

                    Tile tile = this.getTile(xCord, yCord);

                    tile.setConfirmed(true);
                    tile.setHighlighted(true);
                    tile.setLetter(letter, symbolValues.get(letter));
                }
            }
        } catch (SQLException e) {
            WordCrex.handleException(e);

            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Er is iets fout gegaan bij het ophalen van de gelegde letters.");
            errorAlert.setHeaderText(null);
            errorAlert.showAndWait();
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
                Tile columnTile = this.getTile(row, column);

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

    public boolean hasConfirmedSurroundingTile(int x, int y) {
        List<Tile> surroundTiles = new ArrayList<>();

        surroundTiles.add(this.getTile(x, y - 1));
        surroundTiles.add(this.getTile(x, y + 1));
        surroundTiles.add(this.getTile(x - 1, y));
        surroundTiles.add(this.getTile(x + 1, y));

        surroundTiles.removeIf(tile -> tile == null || !tile.hasLetter() || !tile.isConfirmed());

        return surroundTiles.size() > 0;
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
