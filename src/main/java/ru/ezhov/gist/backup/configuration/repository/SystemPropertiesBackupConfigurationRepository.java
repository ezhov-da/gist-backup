package ru.ezhov.gist.backup.configuration.repository;

import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemPropertiesBackupConfigurationRepository {
    private static final Logger LOG = Logger.getLogger(SystemPropertiesBackupConfigurationRepository.class.getName());

    public BackupConfiguration configuration() {
        String gistToken = System.getProperty("gist.token");
        if (gistToken == null || "".equals(gistToken)) {
            throw new IllegalArgumentException("User token not found. Use '-Dgist.token'");
        } else {

            LOG.log(Level.FINER, "User token ''{0}''", gistToken.replaceAll(".", "*"));
        }

        String username = System.getProperty("gist.username");
        if (username == null || "".equals(username)) {
            LOG.log(Level.WARNING, "Not found username. Use '-Dgist.username'");
            throw new IllegalArgumentException("Not found username. Use '-Dgist.username'");
        } else {

            LOG.log(Level.FINER, "Username ''{0}''", username);
        }

        String bkpFolder = System.getProperty("gist.bkp.folder", System.getProperty("user.dir"));
        LOG.log(Level.INFO, "Not found backup folder. Use '-Dgist.bkp.folder'. Set ''{0}''", bkpFolder);
        return BackupConfiguration.from(gistToken, username, bkpFolder);
    }
}
