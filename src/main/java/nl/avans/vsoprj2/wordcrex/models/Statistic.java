package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class Statistic extends Model {
    @Column("games_won")
    private Long gamesWon;
    @Column("games_lost")
    private Long gamesLost;
    @Column("games_tied")
    private Long gamesTied;
    @Column("games_left")
    private Long gamesLeft;
    @Column("top_game_score")
    private BigDecimal topGameScore;
    @Column("top_word_score")
    private Long topWordScore;

    public Statistic(ResultSet resultSet) {
        super(resultSet);
    }

    public Long getGamesWon() {
        return this.gamesWon;
    }

    public Long getGamesLost() {
        return this.gamesLost;
    }

    public Long getGamesTied() {
        return this.gamesTied;
    }

    public Long getGamesLeft() {
        return this.gamesLeft;
    }

    public BigDecimal getTopGameScore() {
        return this.topGameScore;
    }

    public Long getTopWordScore() {
        return this.topWordScore;
    }
}
