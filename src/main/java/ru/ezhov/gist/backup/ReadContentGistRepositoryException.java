package ru.ezhov.gist.backup;

public class ReadContentGistRepositoryException extends Exception {
    public ReadContentGistRepositoryException() {
    }

    public ReadContentGistRepositoryException(String message) {
        super(message);
    }

    public ReadContentGistRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReadContentGistRepositoryException(Throwable cause) {
        super(cause);
    }

    public ReadContentGistRepositoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
