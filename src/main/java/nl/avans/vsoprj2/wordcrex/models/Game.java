package nl.avans.vsoprj2.wordcrex.models;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.WordCrex;
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

    public void setWinner(String winner) {
        this.usernameWinner = winner;
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

    public GameState getCurrentState() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `game_state` FROM `game` WHERE game_id = ?;");
            statement.setInt(1, this.getGameId());

            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return GameState.valueOf(rs.getString("game_state").toUpperCase());
            }

        } catch (SQLException e) {
            throw new DbLoadException(e);
        }

        return GameState.REQUEST;
    }

    public int getPlayerScore(boolean isPlayer1) {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            String query = String.format("SELECT SUM(IFNULL(score, 0) + IFNULL(bonus, 0)) total_score FROM `%s` WHERE `game_id` = ? AND `turn_id` < (SELECT MAX(`turn_id`) FROM `turn` WHERE `game_id` = ?)", (isPlayer1 ? "turnplayer1" : "turnplayer2"));
            PreparedStatement preparedStatement = connection.prepareStatement(query);

            for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
                preparedStatement.setString(i, String.valueOf(this.gameId));
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            resultSet.next();
            return resultSet.getInt("total_score");
        } catch (SQLException ex) {
            throw new DbLoadException(ex);
        }
    }

    /**
     * Checks if the user is allowed to place letters or pass the turn
     *
     * @return returns true if game should be locked
     */
    public boolean getTurnLocked() {
        final Connection connection = Singleton.getInstance().getConnection();
        final String currentUsername = Singleton.getInstance().getUser().getUsername();

        // If game is finished lock turn
        if (this.getGameState() != GameState.PLAYING) {
            if (WordCrex.DEBUG_MODE)
                System.out.println("Game: Game is not in the playing state");
            return true;
        }

        // If the current logged in user is not one of the 2 playing users in this game. Lock the game.
        if (!currentUsername.equals(this.getUsernamePlayer1()) && !currentUsername.equals(this.getUsernamePlayer2())) {
            if (WordCrex.DEBUG_MODE)
                System.out.println("Game: Current user is not an owner of this game. Locking turn...");
            return true;
        }

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT `t`.`turn_id`, " +
                    "`tp1`.`username_player1`, " +
                    "`tp2`.`username_player2` " +
                    "FROM `turn` `t` " +
                    "LEFT OUTER JOIN `turnplayer1` `tp1` ON t.`turn_id` = `tp1`.`turn_id` AND `t`.`game_id` = `tp1`.`game_id` " +
                    "LEFT OUTER JOIN `turnplayer2` `tp2` ON t.`turn_id` = `tp2`.`turn_id` AND `t`.`game_id` = `tp2`.`game_id` " +
                    "WHERE `t`.`game_id` = ? AND " +
                    "`t`.`turn_id` = (SELECT MAX(turn_id) FROM turn WHERE game_id = `t`.`game_id`) GROUP BY `t`.`game_id`");
            statement.setInt(1, this.getGameId());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                final String usernamePlayer1 = resultSet.getString("username_player1");
                final String usernamePlayer2 = resultSet.getString("username_player2");
                return (currentUsername.equals(usernamePlayer1) && usernamePlayer2 == null) ||
                        (currentUsername.equals(usernamePlayer2) && usernamePlayer1 == null);
            }
            return false;
        } catch (SQLException e) {
            if (WordCrex.DEBUG_MODE) System.err.println("Game: couldn't determine if turn is locked or not.");
            return true;
        }
    }

    /**
     * Checks if you are one of the two participants in this game
     *
     * @return if you are a participant this is true
     */
    public boolean getOwnGame() {
        final String currentUsername = Singleton.getInstance().getUser().getUsername();
        return this.getUsernamePlayer1().equals(currentUsername) || this.getUsernamePlayer2().equals(currentUsername);
    }

    /**
     * Sets the game state to resigned and the opponent as the winner
     */
    public void resignGame() {
        this.setGameState(GameState.RESIGNED);
        if (this.usernamePlayer1.equals(Singleton.getInstance().getUser().getUsername())) {
            this.setWinner(this.usernamePlayer2);
        } else {
            this.setWinner(this.usernamePlayer1);
        }
        this.save();
    }

    /**
     * Fetches the amount of pool letters left in the game
     *
     * @return amount of pool letters in this game
     */
    public int getAmountOfPoolLetters() {
        Connection connection = Singleton.getInstance().getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT COUNT(l.letter_id) AS amountOfPoolLetters FROM pot p INNER JOIN letter l ON p.letter_id = l.letter_id AND p.game_id = l.game_id INNER JOIN symbol s ON l.symbol_letterset_code = s.letterset_code AND l.symbol = s.symbol WHERE p.game_id = ?");
            statement.setInt(1, this.getGameId());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt("amountOfPoolLetters");
            }
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Checks if both players have passed 3 times in a row
     *
     * @return if both players passsed 3 times in a row this returns true
     */
    public boolean passedThreeTimesInARow() {
        Connection connection = Singleton.getInstance().getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT t1.turnaction_type as playerOneTurnType, t2.turnaction_type as playerTwoTurnType FROM `turn` t INNER JOIN `turnplayer1` t1 ON t.turn_id = t1.turn_id AND t.game_id = t1.game_id INNER JOIN `turnplayer2` t2 ON t.turn_id = t2.turn_id AND t.game_id = t2.game_id WHERE t.game_id = ? ORDER BY t.turn_id DESC LIMIT 3");
            statement.setInt(1, this.getGameId());
            ResultSet resultSet = statement.executeQuery();

            int times = 0;
            while (resultSet.next()) {
                if (resultSet.getString("playerOneTurnType").equalsIgnoreCase("pass") || resultSet.getString("playerTwoTurnType").equalsIgnoreCase("pass")) {
                    times++;
                }
            }
            return times == 3;
        } catch (SQLException e) {
            return false;
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
