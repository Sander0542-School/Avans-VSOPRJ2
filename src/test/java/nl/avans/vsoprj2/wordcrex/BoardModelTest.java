package nl.avans.vsoprj2.wordcrex;

import junit.framework.TestCase;
import nl.avans.vsoprj2.wordcrex.models.Board;
import nl.avans.vsoprj2.wordcrex.models.Tile;

public class BoardModelTest extends TestCase {

    public void testStartTile() {
        Board board = new Board();
        Tile centerTile = board.getTile(8, 8);

        assertEquals(Tile.TileType.START, centerTile.getTileType());
    }

    public void testNonExistingTile() {
        Board board = new Board();
        Tile tile = board.getTile(0, 0);

        assertNull(tile);
    }

    public void testSetTile() {
        Board board = new Board();

        int x = 12;
        int y = 4;

        board.setTile(x, y, null);

        Tile tile = board.getTile(x, y);

        assertNull(tile);
    }

    public void testTileCount() {
        Board board = new Board();
        Tile[][] tiles = board.getTiles();

        int count = Board.BOARD_SIZE * Board.BOARD_SIZE;

        int tileCount = 0;

        for (Tile[] subTiles : tiles) {
            for (Tile tile : subTiles) {
                tileCount++;
            }
        }

        assertEquals(count, tileCount);
    }

    public void testCoordinateOfTile() {
        Board board = new Board();

        int x = 3;
        int y = 5;

        Tile tile = board.getTile(x, y);

        Board.Coordinate coordinate = board.getCoordinate(tile);

        assertEquals(x, coordinate.getX());
        assertEquals(y, coordinate.getY());
    }

    public void testTestTest() {
        assertEquals(0, 1);
    }

    public void testConfirmedTile() {
        Board board = new Board();

        Tile tile = board.getTile(8, 2);

        tile.setConfirmed(true);

        assertTrue(tile.isConfirmed());
    }

    public void testHighlightedTile() {
        Board board = new Board();

        Tile tile = board.getTile(14, 3);

        tile.setHighlighted(true);

        assertTrue(tile.isHighlighted());
    }

    public void testLetterTile() {
        Board board = new Board();

        Tile tile = board.getTile(8, 9);

        Character character = 'C';

        tile.setLetter(character, 1);

        assertTrue(tile.hasLetter());
        assertEquals(character, tile.getLetter());
    }

    public void testWorthTile() {
        Board board = new Board();

        Tile tile = board.getTile(13, 2);

        Integer worth = 18;

        tile.setLetter('A', worth);

        assertTrue(tile.hasWorth());
        assertEquals(worth, tile.getWorth());
    }

    public void testConfirmedSurroundingTile() {
        Board board = new Board();

        Tile tile = board.getTile(4, 9);
        tile.setLetter('A', 1);
        tile.setConfirmed(true);

        boolean result = board.hasConfirmedSurroundingTile(4, 10);

        assertTrue(result);
    }

    public void testLeftTopCornerExists() {
        Board board = new Board();

        Tile tile = board.getTile(1, 1);

        assertNotNull(tile);
    }

    public void testRightTopCornerExists() {
        Board board = new Board();

        Tile tile = board.getTile(15, 1);

        assertNotNull(tile);
    }

    public void testLeftBottomCornerExists() {
        Board board = new Board();

        Tile tile = board.getTile(1, 15);

        assertNotNull(tile);
    }

    public void testRightBottomCornerExists() {
        Board board = new Board();

        Tile tile = board.getTile(15, 15);

        assertNotNull(tile);
    }
}
