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
    private boolean readContent;

    private GistRepository(BackupConfiguration backupConfiguration, boolean readContent) {
        this.backupConfiguration = BackupConfiguration.from(backupConfiguration);
        this.readContent = readContent;
    }


    public static GistRepository from(BackupConfiguration backupConfiguration) {
        return from(backupConfiguration, true);
    }

    public static GistRepository from(BackupConfiguration backupConfiguration, boolean readContent) {
        return new GistRepository(backupConfiguration, readContent);
    }

    public void readGists(GistReader gistReader) throws GistReaderException, GistRepositoryException, ReadContentGistRepositoryException {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(backupConfiguration.getGistToken());
        GistService gistService = new GistService(gitHubClient);
        List<Gist> gistList = null;
        try {
            gistList = gistService.getGists(backupConfiguration.getUsername());
        } catch (IOException e) {
            throw new GistRepositoryException("Error catch list Gist for user '" + backupConfiguration.getUsername() + "'", e);
        }
        for (Gist gist : gistList) {
            Map<String, GistFile> fileMap = gist.getFiles();
            for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                String name = fileEntry.getKey();
                String text = "";
                String rawLink = fileEntry.getValue().getRawUrl();
                try {
                    URL url = new URL(fileEntry.getValue().getRawUrl());
                    if (readContent) {
                        text = getContent(url);
                    }
                    gistReader.read(name, text, new URL(gist.getHtmlUrl()), url);
                } catch (MalformedURLException e) {
                    throw new GistRepositoryException("Error retrieve content '" + backupConfiguration.getUsername() + "'. Invalid URL '" + rawLink + "'", e);
                }
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
            throw new ReadContentGistRepositoryException("Error retrieve content by URL '" + url.toString() + "'", e);
        }
    }
}
