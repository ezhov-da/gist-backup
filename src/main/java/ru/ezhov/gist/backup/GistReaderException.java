package ru.ezhov.gist.backup;

public class GistReaderException extends Exception {
    public GistReaderException() {
    }

    public GistReaderException(String message) {
        super(message);
    }

    public GistReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public GistReaderException(Throwable cause) {
        super(cause);
    }

    public GistReaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
