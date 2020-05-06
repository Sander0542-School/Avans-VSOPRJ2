package nl.avans.vsoprj2.wordcrex.controllers.game;

import nl.avans.vsoprj2.wordcrex.Singleton;
import nl.avans.vsoprj2.wordcrex.controllers.Controller;
import nl.avans.vsoprj2.wordcrex.exceptions.DbLoadException;

import java.sql.Connection;

public class NewController extends Controller {
        public NewController() {
            Connection connection = Singleton.getInstance().getConnection();
            Singleton.getInstance().getUser();
            



            // get all players
            // return to page
        }

        public void randomGameRequest() {
            this.createGame("NL", "Lars");
        }

        public void createNewGame() {
            this.createGame("NL", "Lars");
        }

        private boolean createGame(String letterset, String username2) {
            Boolean gameCreated = false;

            try {
                Connection connection = Singleton.getInstance().getConnection();
                Singleton.getInstance().getUser();

            } catch (Exception e) {
                throw new DbLoadException(e);
            }

            return gameCreated;
        }
}
