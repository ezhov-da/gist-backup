package ru.ezhov.gist.processing;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;
import ru.ezhov.gist.backup.GistBackup;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GistRename {
    private static final Logger LOG = Logger.getLogger(GistBackup.class.getName());

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String gistToken = System.getProperty("gist.token");
        if (gistToken == null || "".equals(gistToken)) {
            throw new IllegalArgumentException("Not found user token. Use '-Dgist.token'");
        } else {
            LOG.log(Level.INFO, "User token '" + gistToken.replaceAll(".", "*") + "'");
            System.out.println("User token '" + gistToken.replaceAll(".", "*") + "'");
        }
        String username = System.getProperty("gist.username");
        if (username == null || "".equals(username)) {
            LOG.log(Level.WARNING, "Not found username. Set '-Dgist.username'");
            throw new IllegalArgumentException("Not found username. Set '-Dgist.username'");
        } else {
            LOG.log(Level.INFO, "Username '" + username + "'");
            System.out.println("Username '" + username + "'");
        }

        Map<String, String> map = getMapOldNewNames("/src/test/resources/rename.txt");

        try {
            GitHubClient gitHubClient = new GitHubClient();
            gitHubClient.setOAuth2Token(gistToken);
            GistService gistService = new GistService(gitHubClient);
            List<Gist> gistList = gistService.getGists(username);
            for (Gist gist : gistList) {
                Map<String, GistFile> fileMap = gist.getFiles();
                for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                    String oldFileName = fileEntry.getKey();
                    String newName = map.get(oldFileName);
                    if (newName == null) {
                        System.out.println("New name for GIST '" + oldFileName + "' not found.");
                    } else {
                        updateGistFileName(gist.getUrl(), gistToken, oldFileName, newName);
                        System.out.println("Filename update from '" + oldFileName + "' to '" + newName + "'");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateGistFileName(String gistUrl, String token, String oldName, String newName) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(gistUrl).openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization", "token " + token);
        httpURLConnection.setDoOutput(true);
        String updateFilename = "{\"files\": {\"" + oldName + "\": {\"filename\": \"" + newName + "\"}}}";
        System.out.println("send -> " + updateFilename);
        try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
            outputStream.write(updateFilename.getBytes(StandardCharsets.UTF_8));
        }
        int code = httpURLConnection.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new Exception("Error update GIST: " + oldName + " ->  " + newName);
        }
        httpURLConnection.disconnect();
    }

    private static Map<String, String> getMapOldNewNames(String resourcePath) {
        Map<String, String> map = new HashMap<>();
        try (Scanner scanner = new Scanner(GistRename.class.getResourceAsStream(resourcePath))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] array = line.split("\\|\\|");
                map.put(array[0], array[1]);
            }
        }
        return map;
    }
}
