package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.Board.TileType;

public class Tile {
    private Character value;
    private final TileType tileType;

    public Tile(TileType type) {
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
}
