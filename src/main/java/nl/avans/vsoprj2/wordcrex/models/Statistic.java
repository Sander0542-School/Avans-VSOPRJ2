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

    @Override
    public String getTable() {
        return null;
    }

    public String getGamesWon() {
        return gamesWon;
    }

    public String getGamesLost() {
        return gamesLost;
    }

    public String getGamesTied() {
        return gamesTied;
    }

    public String getGamesLeft() {
        return gamesLeft;
    }

    public String getTopGameScore() {
        return topGameScore;
    }

    public String getTopWordScore() {
        return topWordScore;
    }
}
