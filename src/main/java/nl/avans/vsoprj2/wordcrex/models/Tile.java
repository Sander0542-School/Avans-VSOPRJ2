package nl.avans.vsoprj2.wordcrex.models;

import javafx.scene.paint.Color;

public class Tile {
    private final TileType tileType;

    private Character letter;
    private Integer worth;

    private boolean confirmed = false;
    private boolean highlighted = false;

    public Tile(TileType tileType) {
        this.tileType = tileType;
    }

    public TileType getTileType() {
        return this.tileType;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmed() {
        return this.confirmed;
    }

    public void setLetter(Character letter, Integer worth) {
        this.letter = letter;
        this.worth = worth;
    }

    public boolean hasLetter() {
        return this.letter != null;
    }

    public Character getLetter() {
        return this.letter;
    }

    public boolean hasWorth() {
        return this.worth != null;
    }

    public Integer getWorth() {
        return this.worth;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isHighlighted() {
        return this.highlighted;
    }

    public enum TileType {
        NORMAL("--", Color.rgb(27, 23, 68)),
        START("*", Color.rgb(237, 17, 147)),
        TWOLETTER("2L", Color.rgb(45, 171, 225)),
        THREEWORD("3W", Color.rgb(237, 17, 147)),
        FOURLETTER("4L", Color.rgb(42, 77, 154)),
        FOURWORD("4W", Color.rgb(242, 102, 35)),
        SIXLETTER("6L", Color.rgb(11, 149, 68));

        private final String value;
        private final Color color;

        TileType(String value, Color color) {
            this.value = value;
            this.color = color;
        }

        public static TileType fromDatabase(String databaseValue) {
            for (TileType tileType : values()) {
                if (tileType.value.equals(databaseValue)) {
                    return tileType;
                }
            }
            return null;
        }

        public String getValue() {
            return this.value;
        }

        public Color getColor() {
            return this.color;
        }
    }
}
