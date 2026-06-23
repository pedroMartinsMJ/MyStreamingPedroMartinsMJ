package pedroMartinsMJ.MyStreaming.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String identifier) {
        super("Usuário não encontrado: " + identifier);
    }
}
