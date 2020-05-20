package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.Board.TileType;

public class Tile {
    private int x;
    private int y;
    private boolean confirmed = false;

    private Character value;

    private TileType tileType;

    public Tile(int x, int y, TileType type) {
        this.x = x;
        this.y = y;

        this.tileType = type;
    }

    public Character getValue() {
        return this.value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public TileType getTileType() {
        return this.tileType;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void confirm() {
        this.confirmed = true;
    }

    public boolean getConfirmed() {
        return this.confirmed;
    }
}
