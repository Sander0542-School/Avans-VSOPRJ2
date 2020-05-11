package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class Statistic extends Model {
    @Column("games_won")
    private String gamesWon;
    @Column("games_lost")
    private String gamesLost;
    @Column("games_tied")
    private String gamesTied;
    @Column("games_left")
    private String gamesLeft;
    @Column("top_game_score")
    private String topGameScore;
    @Column("top_word_score")
    private String topWordScore;

    public Statistic(ResultSet resultSet) {
        super(resultSet);
    }

    public String getGamesWon() {
        return this.gamesWon;
    }

    public String getGamesLost() {
        return this.gamesLost;
    }

    public String getGamesTied() {
        return this.gamesTied;
    }

    public String getGamesLeft() {
        return this.gamesLeft;
    }

    public String getTopGameScore() {
        return this.topGameScore;
    }

    public String getTopWordScore() {
        return this.topWordScore;
    }
}
