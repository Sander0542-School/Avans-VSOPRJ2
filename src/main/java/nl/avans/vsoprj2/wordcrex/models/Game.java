package nl.avans.vsoprj2.wordcrex.models;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Game extends Model {

    private int gameId;
    private String gameState;
    private String lettersetCode;
    private String usernamePlayer1;
    private String usernamePlayer2;
    private String answerPlayer2;
    private String usernameWinner;

    public Game(ResultSet resultSet) throws SQLException {
        super(resultSet);

        this.setGameId(resultSet.getInt("game_id"));
        this.setGameState(resultSet.getString("game_state"));
        this.setLettersetCode(resultSet.getString("letterset_code"));
        this.setUsernamePlayer1(resultSet.getString("username_player1"));
        this.setUsernamePlayer2(resultSet.getString("username_player2"));
        this.setAnswerPlayer2(resultSet.getString("answer_player2"));
        this.setUsernameWinner(resultSet.getString("username_winner"));
    }

    public int getGameId() {
        return gameId;
    }

    private void setGameId(int gameId) {
        this.gameId = gameId;
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

    public void setLettersetCode(String lettersetCode) {
        this.lettersetCode = lettersetCode;
    }

    public String getUsernamePlayer1() {
        return usernamePlayer1;
    }

    public void setUsernamePlayer1(String usernamePlayer1) {
        this.usernamePlayer1 = usernamePlayer1;
    }

    public String getUsernamePlayer2() {
        return usernamePlayer2;
    }

    public void setUsernamePlayer2(String usernamePlayer2) {
        this.usernamePlayer2 = usernamePlayer2;
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
}
