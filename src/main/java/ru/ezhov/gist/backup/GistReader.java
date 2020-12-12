package ru.ezhov.gist.backup;

import java.net.URL;

public interface GistReader extends AutoCloseable {
    void read(String name, String content, URL html, URL raw) throws GistReaderException;
}
