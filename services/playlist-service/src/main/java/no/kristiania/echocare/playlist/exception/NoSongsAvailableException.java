package no.kristiania.echocare.playlist.exception;

/**
 * Exception thrown when no songs are available in the database for playlist generation
 */
public class NoSongsAvailableException extends RuntimeException {
    public NoSongsAvailableException(String message) {
        super(message);
    }
}

