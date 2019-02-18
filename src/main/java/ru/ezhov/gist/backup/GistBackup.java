package ru.ezhov.gist.backup;

import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class GistBackup {
    private static final Logger LOG = Logger.getLogger(GistBackup.class.getName());

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String gistToken = System.getProperty("gist.token");
        if (gistToken == null || "".equals(gistToken)) {
            throw new IllegalArgumentException("Не указан токен пользователя '-Dgist.token'");
        } else {
            System.out.println("Токен пользователя '" + gistToken.replaceAll(".", "*") + "'");
        }
        String username = System.getProperty("gist.username");
        if (username == null || "".equals(username)) {
            throw new IllegalArgumentException("Не указан логин пользователя '-Dgist.username'");
        } else {
            System.out.println("Имя пользователя '" + username + "'");
        }
        String bkpFolder = System.getProperty("gist.bkp.folder", System.getProperty("user.dir"));
        System.out.println("Не указана папка для бэкапа '-Dgist.bkp.folder' установлена '" + bkpFolder + "'");

        File file = new File(
                bkpFolder,
                String.format(
                        "gist_%s_%s.bkp.xml",
                        username,
                        new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                )
        );
        System.out.println("Абсолютный путь файла бэкапа '" + file.getAbsolutePath() + "'");
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            XMLStreamWriter xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(fileOutputStream);
            xsw.writeStartDocument();
            xsw.writeStartElement("gists");
            System.out.println("Начато создание бэкапа...");
            System.out.print("=> ");
            AtomicInteger counter = new AtomicInteger();
            readGists(gistToken, username, (name, content) -> {
                xsw.writeStartElement("gist");
                xsw.writeStartElement("name");
                xsw.writeCharacters(name);
                xsw.writeEndElement();
                xsw.writeStartElement("content");
                xsw.writeCharacters(content);
                xsw.writeEndElement();
                xsw.writeEndElement();
                counter.getAndIncrement();
                if ((counter.get() % 10) == 0) {
                    xsw.flush();
                    System.out.print(counter.get() + " ");
                }
            });
            System.out.println();
            xsw.writeEndElement();
            xsw.writeEndDocument();
            xsw.close();
            long endTime = System.currentTimeMillis();
            System.out.println("Бэкап gist создан. Количество gist '" + counter.get() + "'. Файл '" + file.getAbsolutePath() + "'. Время создания бэкапа '" + (endTime - startTime) + " ms'");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getContent(URL url) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        StringBuilder stringBuilder = new StringBuilder();
        try (Scanner scanner = new Scanner(httpURLConnection.getInputStream(), "UTF-8")) {
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            return stringBuilder.toString();
        }
    }

    private static void readGists(String token, String username, GistReader gistReader) throws Exception {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);
        GistService gistService = new GistService(gitHubClient);
        List<Gist> gistList = gistService.getGists(username);
        for (Gist gist : gistList) {
            Map<String, GistFile> fileMap = gist.getFiles();
            for (Map.Entry<String, GistFile> fileEntry : fileMap.entrySet()) {
                String name = fileEntry.getKey();
                String text = getContent(new URL(fileEntry.getValue().getRawUrl()));
                gistReader.read(name, text);
            }
        }
    }
}

interface GistReader {
    void read(String name, String content) throws Exception;
}
