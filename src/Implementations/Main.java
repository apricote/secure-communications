package Implementations;

/**
 * Created by julian on 12/9/15.
 */
public class Main {
    public static void main(String[] args) {
        Client c1 = new Client();
        Client c2 = new Client();

        c1.sendRawMessage(c2, "Hiii", true);
        c1.sendSecureMessage(c2, "Blub");
    }
}
