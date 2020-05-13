package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class ScoreboardRound extends Model {
    @Column("game_id")
    private int gameId;
    @Column("game_state")
    private String gameState;
    @Column("username_player_1")
    private String usernamePlayerOne;
    @Column("username_player_2")
    private String usernamePlayerTwo;
    @Column("score1")
    private int scorePlayerOne;
    @Column("score2")
    private int scorePlayerTwo;
    @Column("bonus1")
    private int bonusPlayerOne;
    @Column("bonus2")
    private int bonusPlayerTwo;

    public ScoreboardRound(ResultSet resultSet) {
        super(resultSet);
    }


}
