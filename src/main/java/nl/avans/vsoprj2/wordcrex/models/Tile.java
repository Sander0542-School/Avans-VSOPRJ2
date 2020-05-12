package nl.avans.vsoprj2.wordcrex.models;

class Tile {
    private Character value;
    private final Board.TileType tileType;

    public Tile(Board.TileType type) {
        tileType = type;
    }

    public Character getValue() {
        return value;
    }

    public void setValue(Character value) {
        this.value = value;
    }

    public Board.TileType getTileType() {
        return tileType;
    }
}

