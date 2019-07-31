package ru.ezhov.gist.backup;

import java.io.IOException;

public class GistRepositoryException extends Exception {
    public GistRepositoryException() {
    }

    public GistRepositoryException(String message) {
        super(message);
    }

    public GistRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public GistRepositoryException(Throwable cause) {
        super(cause);
    }

    public GistRepositoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
