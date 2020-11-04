package ru.ezhov.gist.backup;

import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;
import ru.ezhov.gist.backup.configuration.repository.SystemPropertiesBackupConfigurationRepository;

import java.util.logging.Level;
import java.util.logging.Logger;


//Если вы хотите использовать другой файл, нужно при запуске приложения установить свойство java.util.logging.config.file:
//java -Djava.util.logging.config.file=конфигурационный_файл класс
public class GistBackup {
    private static final Logger LOG = Logger.getLogger(GistBackup.class.getName());

    public static void main(String[] args) {
        try {
            doBackup();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error", e);
        }
    }

    private static void doBackup() {
        long startTime = System.currentTimeMillis();
        SystemPropertiesBackupConfigurationRepository systemPropertiesBackupConfigurationRepository = new SystemPropertiesBackupConfigurationRepository();
        BackupConfiguration backupConfiguration = systemPropertiesBackupConfigurationRepository.configuration();
        try (GistReader xmlBackup = new XmlBackup(backupConfiguration, new ConsoleViewer())) {
            GistRepository gistRepository = GistRepository.from(backupConfiguration, xmlBackup);
            gistRepository.readGists(xmlBackup);
        } catch (GistReaderException e) {
            LOG.log(Level.SEVERE, "Error with Gist", e);
        } catch (GistRepositoryException e) {
            LOG.log(Level.SEVERE, "Error with Gists repository", e);
        } catch (ReadContentGistRepositoryException e) {
            LOG.log(Level.SEVERE, "Error with Gist content", e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error", e);
        }
        long endTime = System.currentTimeMillis();
        LOG.log(Level.INFO, "Backup time ''{0}'' ms", (endTime - startTime));
    }
}