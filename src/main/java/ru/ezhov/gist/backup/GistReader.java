package ru.ezhov.gist.backup;

public interface GistReader extends AutoCloseable {
    void read(String name, String content) throws GistReaderException;
}
