package Implementations;

/**
 * Created by julian on 12/9/15.
 */
public class Main {
    public static void main(String[] args) {
        Client c1 = new Client();
        Client c2 = new Client();

        c1.sendSecureMessage(c2, "Guten Tag");
        c2.sendSecureMessage(c1, "Wie geht es dir?");


        try {
            Thread.sleep(200l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
