package pedroMartinsMJ.MyStreaming.exception;

public class VideoNotFoundException extends RuntimeException {
    public VideoNotFoundException(String id) {
        super("Vídeo não encontrado: " + id);
    }
}
