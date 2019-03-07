package ru.ezhov.gist.backup.processing;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import ru.ezhov.gist.backup.GistBackup;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GistRename {
    private static final Logger LOG = Logger.getLogger(GistBackup.class.getName());

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String gistToken = System.getProperty("gist.token");
        if (gistToken == null || "".equals(gistToken)) {
            throw new IllegalArgumentException("Не указан токен пользователя '-Dgist.token'");
        } else {
            LOG.log(Level.INFO, "Токен пользователя '" + gistToken.replaceAll(".", "*") + "'");
            System.out.println("Токен пользователя '" + gistToken.replaceAll(".", "*") + "'");
        }
        String username = System.getProperty("gist.username");
        if (username == null || "".equals(username)) {
            LOG.log(Level.WARNING, "Не указан логин пользователя '-Dgist.username'");
            throw new IllegalArgumentException("Не указан логин пользователя '-Dgist.username'");
        } else {
            LOG.log(Level.INFO, "Имя пользователя '" + username + "'");
            System.out.println("Имя пользователя '" + username + "'");
        }

        try {
            GitHubClient gitHubClient = new GitHubClient();
            gitHubClient.setOAuth2Token(gistToken);
            GistService gistService = new GistService(gitHubClient);
            List<Gist> gistList = null;
            gistList = gistService.getGists(username);
            for (Gist gist : gistList) {
                Map<String, GistFile> fileMap = gist.getFiles();
                for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                    String name = fileEntry.getKey();
                    GistFile gistFile = fileEntry.getValue();
                    if ("test-file".equals(name)) {
                        gistFile.setFilename("test-file-from-java");
                        gistService.updateGist(gist);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
