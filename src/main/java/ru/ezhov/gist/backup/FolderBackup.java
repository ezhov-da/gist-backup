package ru.ezhov.gist.backup;

import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FolderBackup implements GistReader {
    private Viewer viewer;

    private File rootFolder;
    private File backupFolder;

    public FolderBackup(BackupConfiguration backupConfiguration, Viewer viewer) {
        this.viewer = viewer;

        rootFolder = new File(backupConfiguration.getBkpFolder());
        rootFolder.mkdirs();
        backupFolder = new File(rootFolder, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        backupFolder.mkdirs();
    }

    @Override
    public void read(String name, String content, URL html, URL raw) throws GistReaderException {
        String resultName = name
                .replaceAll("'\\*'", "звёздочка");

        try {
            viewer.show(resultName + "\r\n");
            File file = new File(backupFolder, resultName);
            if (file.exists()) {
                file = new File(backupFolder, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + resultName);
            }
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        } catch (Exception ex) {
            throw new GistReaderException("Error write file '" + resultName + "'", ex);
        }
    }

    @Override
    public void close() throws Exception {

    }
}
