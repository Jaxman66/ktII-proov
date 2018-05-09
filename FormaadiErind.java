public class FormaadiErind extends Exception{
    //alt+insert ja constructor ja kaks valikut
    public FormaadiErind(String message) {
        super("Vigane sisestus! " + message);
    }

    public FormaadiErind(String message, Throwable cause) {
        super("vigane sisestust!" + message, cause);
    }
}
