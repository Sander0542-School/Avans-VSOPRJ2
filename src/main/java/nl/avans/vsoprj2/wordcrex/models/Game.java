package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;
import nl.avans.vsoprj2.wordcrex.models.annotations.PrimaryKey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Game extends Model {

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
        return gameId;
    }

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getLettersetCode() {
        return lettersetCode;
    }

    public String getUsernamePlayer1() {
        return usernamePlayer1;
    }

    public String getUsernamePlayer2() {
        return usernamePlayer2;
    }

    public String getAnswerPlayer2() {
        return answerPlayer2;
    }

    public void setAnswerPlayer2(String answerPlayer2) {
        this.answerPlayer2 = answerPlayer2;
    }

    public String getUsernameWinner() {
        return usernameWinner;
    }

    public void setUsernameWinner(String usernameWinner) {
        this.usernameWinner = usernameWinner;
    }

    public String getMessage() {
        Object user = Singleton.getInstance().getUser();
        switch (this.getGameState()) { //TODO(Sander) replace wth Enum
            case "request":
                if (this.getUsernamePlayer1() == user) {
                    return String.format("Waiting for %s to", this.getUsernamePlayer2());
                }
                return String.format("Invited by %s", this.getUsernamePlayer1());
            case "playing":
                return "Playing";
            case "finished":
            case "resigned":
                return "Game ended";
            default:
                return "Unknown";
        }
    }


    public int getPlayerScore(boolean isPlayer1) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            String query = "SELECT SUM(IFNULL(`"+ (isPlayer1 ? "score1" : "score2") +"`,0) + IFNULL(`"+ (isPlayer1 ? "bonus1" : "bonus2") +"`, 0)) as `calculated_score` FROM `score` WHERE `game_id` = ?";

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


}

