package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;
import java.util.StringJoiner;

public class ScoreboardRound extends Model {
    @Column("game_id")
    private int gameId;
    @Column("turn_id")
    private int turnId;
    @Column("hand_inhoud")
    private String handContent;
    @Column("username_player1")
    private String usernamePlayerOne;
    @Column("woorddeel1")
    private String wordPartOne;
    @Column("bonus1")
    private int bonusPlayerOne;
    @Column("score1")
    private int scorePlayerOne;
    @Column("turntype1")
    private String turnActionTypeOne;
    @Column("username_player2")
    private String usernamePlayerTwo;
    @Column("woorddeel2")
    private String wordPartTwo;
    @Column("bonus2")
    private int bonusPlayerTwo;
    @Column("score2")
    private int scorePlayerTwo;
    @Column("turntype2")
    private String turnActionTypeTwo;

    public ScoreboardRound(ResultSet resultSet) {
        super(resultSet);
    }

    public int getGameId() {
        return this.gameId;
    }

    public int getTurnId() {
        return this.turnId;
    }

    public String getHandContent() {
        return this.handContent;
    }

    public String getUsernamePlayerOne() {
        return this.usernamePlayerOne;
    }

    public String getUsernamePlayerTwo() {
        return this.usernamePlayerTwo;
    }

    public String getWordPartPlayerOne() {
        return this.wordPartOne;
    }

    public String getWordPartPlayerTwo() {
        return this.wordPartTwo;
    }

    public TurnActionType getTurnActionTypeOne() {
        return TurnActionType.valueOf(this.turnActionTypeOne.toUpperCase());
    }

    public String getTurnActionTypeTwo() {
        return this.turnActionTypeTwo;
    }

    public int getBonusPlayerOne() {
        return this.bonusPlayerOne;
    }

    public int getBonusPlayerTwo() {
        return this.bonusPlayerTwo;
    }

    public int getScorePlayerOne() {
        return this.scorePlayerOne;
    }

    public int getScorePlayerTwo() {
        return this.scorePlayerTwo;
    }

    public String getWordPlayerOne() {
        return this.commaSeparatedToString(this.getWordPartPlayerOne());
    }

    public String getWordPlayerTwo() {
        return this.commaSeparatedToString(this.getWordPartPlayerTwo());
    }

    private String commaSeparatedToString(String input) {
        String[] split = input.split(",");
        StringJoiner stringJoiner = new StringJoiner("");
        for (String character : split) stringJoiner.add(character);
        return stringJoiner.toString();
    }

    public enum TurnActionType {
        PLAY,
        PASS,
        RESIGN
    }
}
