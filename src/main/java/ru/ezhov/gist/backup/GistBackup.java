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
            LOG.log(Level.SEVERE, "Непредвиденная ошибка", e);
        }
    }

    private static void doBackup() {
        long startTime = System.currentTimeMillis();
        SystemPropertiesBackupConfigurationRepository systemPropertiesBackupConfigurationRepository = new SystemPropertiesBackupConfigurationRepository();
        BackupConfiguration backupConfiguration = systemPropertiesBackupConfigurationRepository.configuration();
        try (XmlBackup xmlBackup = new XmlBackup(backupConfiguration, new ConsoleViewer())) {
            GistRepository gistRepository = GistRepository.from(backupConfiguration, xmlBackup);
            gistRepository.readGists(xmlBackup);
        } catch (GistReaderException e) {
            LOG.log(Level.SEVERE, "Ошибка при обработке Gist", e);
        } catch (GistRepositoryException e) {
            LOG.log(Level.SEVERE, "Ошибка при работе с Gists репозиторием", e);
        } catch (ReadContentGistRepositoryException e) {
            LOG.log(Level.SEVERE, "Ошибка при чтении контента Gist", e);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Ошибка при закрытии ресурсов", e);
        }
        long endTime = System.currentTimeMillis();
        LOG.log(Level.INFO, "Время создания бэкапа ''{0}'' ms", (endTime - startTime));
    }
}