package nl.avans.vsoprj2.wordcrex;

import junit.framework.TestCase;
import nl.avans.vsoprj2.wordcrex.models.Account;

public class AccountModelTest extends TestCase {
    public void testFromUsername() {
        String username = "jagermeester";

        Account account = Account.fromUsername(username);

        assertNotNull(account);
        assertEquals(username, account.getUsername());
    }

    public void testFromUsernamePassword() {
        String username = "jagermeester";
        String password = "123";

        Account account = Account.fromUsernamePassword(username, password);

        assertNotNull(account);
        assertEquals(username, account.getUsername());
    }
}
