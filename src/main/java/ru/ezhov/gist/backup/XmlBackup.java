package ru.ezhov.gist.backup;

import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XmlBackup implements GistReader {
    private static final Logger LOG = Logger.getLogger(XmlBackup.class.getName());

    private FileOutputStream fileOutputStream;
    private AtomicInteger counter = new AtomicInteger();
    private XMLStreamWriter xsw;
    private BackupConfiguration backupConfiguration;
    private Viewer viewer;

    XmlBackup(BackupConfiguration backupConfiguration, Viewer viewer) throws GistReaderException {
        this.backupConfiguration = backupConfiguration;
        this.viewer = viewer;
        try {
            fileOutputStream = new FileOutputStream(this.backupConfiguration.createBackupFile());
            xsw = XMLOutputFactory.newFactory().createXMLStreamWriter(fileOutputStream);
            xsw.writeStartDocument();
            xsw.writeStartElement("gists");
            LOG.log(Level.FINER, "Start backup ...");
            viewer.show("=> ");
        } catch (Exception e) {
            throw new GistReaderException("Error create backup class", e);
        }
    }

    @Override
    public void read(String name, String content, URL html, URL raw) throws GistReaderException {
        try {
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
                viewer.show(counter.get() + " ");
            }
        } catch (Exception e) {
            throw new GistReaderException("Error create backup XML");
        }
    }

    @Override
    public void close() throws Exception {
        viewer.show("\r\n");
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
        fileOutputStream.close();
        LOG.log(Level.INFO, "Backup created. Count gist ''{0}''. File ''{1}'' created", new Object[]{counter.get(), backupConfiguration.createBackupFile().getAbsolutePath()});
    }

}
