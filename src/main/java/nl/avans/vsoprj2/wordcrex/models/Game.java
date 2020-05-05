package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.models.annotations.Column;

import java.sql.ResultSet;

public class Game extends Model {

    @Column("game_id")
    private int gameId;
    @Column("game_state")
    private gamestate gameState;
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

    public int getGameId() {
        return gameId;
    }

    private void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public gamestate getGameState() {
        return gameState;
    }

    public void setGameState(gamestate gameState) {
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

    public enum gamestate
    {
        request,
        playing,
        finished,
        resigned,
    }
}
