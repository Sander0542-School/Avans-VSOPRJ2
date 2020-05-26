package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;
import nl.avans.vsoprj2.wordcrex.models.annotations.PrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Game extends DbModel {

    @PrimaryKey
    @Column("game_id")
    private int gameId;
    @Column("game_state")
    private String gameState;
    @Column("letterset_code")
    private String lettersetCode;
    @Column("username_player1")
    private String usernamePlayer1;
    @Column("username_player2")
    private String usernamePlayer2;
    @Column("answer_player2")
    private String answerPlayer2;
    @Column("username_winner")
    private String usernameWinner;

    public Game(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getTable() {
        return "game";
    }

    public int getGameId() {
        return this.gameId;
    }

    public GameState getGameState() {
        return GameState.valueOf(this.gameState.toUpperCase());
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState.toString().toLowerCase();
    }

    public String getLettersetCode() {
        return this.lettersetCode;
    }

    public String getUsernamePlayer1() {
        return this.usernamePlayer1;
    }

    public String getUsernamePlayer2() {
        return this.usernamePlayer2;
    }

    public Answer getAnswerPlayer2() {
        return Answer.valueOf(this.answerPlayer2.toUpperCase());
    }

    public void setAnswerPlayer2(Answer answerPlayer2) {
        this.answerPlayer2 = answerPlayer2.toString().toLowerCase();
    }

    public String getUsernameWinner() {
        return this.usernameWinner;
    }

    public void setWinner(Account winner) {
        this.usernameWinner = winner.getUsername();
    }

    public String getMessage() {
        Account user = Singleton.getInstance().getUser();
        switch (this.getGameState()) {
            case REQUEST:
                if (this.getUsernamePlayer1().equals(user.getUsername())) {
                    return "Wacht op acceptatie van speler";
                }
                return "Uitgenodigd door speler";
            case PLAYING:
                return "Aan het spelen";
            case FINISHED:
            case RESIGNED:
                return "Spel afgelopen";
            default:
                return "Unknown";
        }
    }

    public int getCurrentTurn() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT MAX(turn_id) as turn FROM `turn` WHERE game_id = ? LIMIT 1");
            statement.setInt(1, this.getGameId());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("turn");
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        return 0;
    }

    public int getPlayerScore(boolean isPlayer1) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            String query = String.format("SELECT SUM(IFNULL(`%s`,0) + IFNULL(`%s`, 0)) as `calculated_score` FROM `score` WHERE `game_id` = ?", (isPlayer1 ? "score1" : "score2"), (isPlayer1 ? "bonus1" : "bonus2"));
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
                preparedStatement.setString(i, String.valueOf(this.gameId));
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return resultSet.getInt("calculated_score");
        } catch (SQLException ex) {
            throw new DbLoadException(ex);
        }
    }

    public enum GameState {
        REQUEST,
        PLAYING,
        FINISHED,
        RESIGNED,
    }

    public enum Answer {
        ACCEPTED,
        REJECTED,
        UNKNOWN,
    }
}
