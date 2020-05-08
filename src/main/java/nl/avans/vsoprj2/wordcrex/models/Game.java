package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.models.annotations.Column;
import nl.avans.vsoprj2.wordcrex.models.annotations.PrimaryKey;

import java.sql.ResultSet;

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

    public void setGameState(GameState gameState) {
        this.gameState = gameState.toString();
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

    public enum GameState
    {
        REQUEST,
        PLAYING,
        FINISHED,
        RESIGNED,
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
}
