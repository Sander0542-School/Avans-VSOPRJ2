package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class Letter extends Model {

    @Column("letter_id")
    private int letterId;
    @Column("game_id")
    private int gameId;
    @Column("symbol_letterset_code")
    private String lettersetCode;
    @Column("symbol")
    private String symbol;
    @Column("value")
    private int value;

    public Letter(ResultSet resultSet) {
        super(resultSet);
    }

    public int getLetterId() {
        return this.letterId;
    }

    public int getGameId() {
        return this.gameId;
    }

    public String getLettersetCode() {
        return this.lettersetCode;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public int getValue() {
        return this.value;
    }
}