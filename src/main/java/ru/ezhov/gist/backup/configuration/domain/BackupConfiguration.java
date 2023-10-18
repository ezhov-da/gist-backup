package ru.ezhov.gist.backup.configuration.domain;

import ru.ezhov.gist.backup.BackupType;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackupConfiguration {
    private static final Logger LOG = Logger.getLogger(BackupConfiguration.class.getName());
    private String gistToken;
    private String username;
    private String bkpFolder;

    private BackupType backupType;

    private BackupConfiguration(String gistToken, String username, String bkpFolder, BackupType backupType) {
        this.setGistToken(gistToken);
        this.setUsername(username);
        this.setBkpFolder(bkpFolder);
        this.setBackupType(backupType);
    }

    public static BackupConfiguration from(String gistToken, String username, String bkpFolder, BackupType backupType) {
        return new BackupConfiguration(gistToken, username, bkpFolder, backupType);
    }

    public static BackupConfiguration from(BackupConfiguration backupConfiguration) {
        return new BackupConfiguration(
                backupConfiguration.getGistToken(),
                backupConfiguration.getUsername(),
                backupConfiguration.getBkpFolder(),
                backupConfiguration.getBackupType()
        );
    }

    private void setGistToken(String gistToken) {
        this.gistToken = gistToken;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private void setBkpFolder(String bkpFolder) {
        this.bkpFolder = bkpFolder;
    }

    public void setBackupType(BackupType backupType) {
        this.backupType = backupType;
    }

    public String getGistToken() {
        return gistToken;
    }

    public String getUsername() {
        return username;
    }

    public String getBkpFolder() {
        return bkpFolder;
    }

    public BackupType getBackupType() {
        return backupType;
    }

    public File createBackupFile() {
        File file = new File(
                bkpFolder,
                String.format(
                        "gist_%s_%s.bkp.xml",
                        username,
                        new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                )
        );

        LOG.log(Level.FINER, "Absolute path to file backup '" + file.getAbsolutePath() + "'");

        return file;
    }
}
