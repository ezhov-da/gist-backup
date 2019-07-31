package ru.ezhov.gist.backup.configuration.repository;

import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemPropertiesBackupConfigurationRepository {
    private static final Logger LOG = Logger.getLogger(SystemPropertiesBackupConfigurationRepository.class.getName());

    public BackupConfiguration configuration() {
        String gistToken = System.getProperty("gist.token");
        if (gistToken == null || "".equals(gistToken)) {
            throw new IllegalArgumentException("Не указан токен пользователя '-Dgist.token'");
        } else {

            LOG.log(Level.FINER, "Токен пользователя ''{0}''", gistToken.replaceAll(".", "*"));
        }

        String username = System.getProperty("gist.username");
        if (username == null || "".equals(username)) {
            LOG.log(Level.WARNING, "Не указан логин пользователя '-Dgist.username'");
            throw new IllegalArgumentException("Не указан логин пользователя '-Dgist.username'");
        } else {

            LOG.log(Level.FINER, "Имя пользователя ''{0}''", username);
        }

        String bkpFolder = System.getProperty("gist.bkp.folder", System.getProperty("user.dir"));
        LOG.log(Level.INFO, "Не указана папка для бэкапа '-Dgist.bkp.folder' установлена ''{0}''", bkpFolder);
        return BackupConfiguration.from(gistToken, username, bkpFolder);
    }
}
