package ru.ezhov.gist.backup;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GistRepository {
    private BackupConfiguration backupConfiguration;

    private GistRepository(BackupConfiguration backupConfiguration) {
        this.backupConfiguration = BackupConfiguration.from(backupConfiguration);
    }

    public static GistRepository from(BackupConfiguration backupConfiguration, GistReader gistReader) {
        return new GistRepository(backupConfiguration);
    }

    public void readGists(GistReader gistReader) throws GistReaderException, GistRepositoryException, ReadContentGistRepositoryException {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(backupConfiguration.getGistToken());
        GistService gistService = new GistService(gitHubClient);
        List<Gist> gistList = null;
        try {
            gistList = gistService.getGists(backupConfiguration.getUsername());
        } catch (IOException e) {
            throw new GistRepositoryException("Не удалось получить список Gists для пользователя '" + backupConfiguration.getUsername() + "'", e);
        }
        for (Gist gist : gistList) {
            Map<String, GistFile> fileMap = gist.getFiles();
            for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                String name = fileEntry.getKey();
                String text = null;
                String rawLink = fileEntry.getValue().getRawUrl();
                try {
                    text = getContent(new URL(fileEntry.getValue().getRawUrl()));
                } catch (MalformedURLException e) {
                    throw new GistRepositoryException("Не удалось получить контент для пользователя '" + backupConfiguration.getUsername() + "', так как ссылка невалидная '" + rawLink + "'", e);
                }
                gistReader.read(name, text);
            }
        }
    }

    private String getContent(final URL url) throws ReadContentGistRepositoryException {
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            StringBuilder stringBuilder = new StringBuilder();
            try (Scanner scanner = new Scanner(httpURLConnection.getInputStream(), "UTF-8")) {
                while (scanner.hasNextLine()) {
                    stringBuilder.append(scanner.nextLine()).append("\n");
                }
                return stringBuilder.toString();
            }
        } catch (Exception e) {
            throw new ReadContentGistRepositoryException("Не удалось получить контент по ссылке '" + url.toString() + "'", e);
        }
    }
}
