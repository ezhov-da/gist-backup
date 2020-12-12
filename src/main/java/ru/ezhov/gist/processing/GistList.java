package ru.ezhov.gist.processing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ru.ezhov.gist.backup.GistReader;
import ru.ezhov.gist.backup.GistReaderException;
import ru.ezhov.gist.backup.GistRepository;
import ru.ezhov.gist.backup.configuration.domain.BackupConfiguration;
import ru.ezhov.gist.backup.configuration.repository.SystemPropertiesBackupConfigurationRepository;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GistList {
    private static final Logger LOG = Logger.getLogger(GistList.class.getName());

    public static void main(String[] args) {
        try {
            List<Gist> gists = gists();
            createYml(gists);
//                    createYml(Arrays.asList(g));

//            Gist.create("sql-test", "", new URL("http://test")).ifPresent(g ->
//            {
//                try {
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error", e);
        }
    }

    private static List<Gist> gists() throws Exception {
        SystemPropertiesBackupConfigurationRepository systemPropertiesBackupConfigurationRepository = new SystemPropertiesBackupConfigurationRepository();
        BackupConfiguration backupConfiguration = systemPropertiesBackupConfigurationRepository.configuration();
        List<Gist> gists = new ArrayList<>();
        try (GistReader reader = new GistReaderImpl(gists)) {
            GistRepository gistRepository = GistRepository.from(backupConfiguration, true);
            gistRepository.readGists(reader);

            return gists;
        }
    }

    private static void createYml(List<Gist> gists) throws JsonProcessingException {
        List<String> blocks = gists
                .stream()
                .map(Gist::getBlock).distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<String, List<Entity>> ent = gists
                .stream()
                .map(g -> {
                    Entity entity = new Entity();
                    entity.block = g.block;
                    entity.name = g.name;
                    entity.tags = g.tags;
                    entity.htmlUrl = g.htmlUrl;
                    entity.rawUrl = g.rawUrl;
                    return entity;
                })
                .collect(Collectors.groupingBy(Entity::getBlock));

        List<Group> groups = ent
                .entrySet()
                .stream()
                .map(e ->
                        {
                            Group group = new Group();
                            group.name = e.getKey();
                            group.entities = e
                                    .getValue()
                                    .stream()
                                    .sorted(Comparator.comparing(Entity::getName))
                                    .collect(Collectors.toList());
                            return group;
                        }
                )
                .sorted(Comparator.comparing(Group::getName))
                .collect(Collectors.toList());
        Data data = new Data();
        data.blocks = blocks;
        data.groups = groups;

        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        System.out.println(om.writeValueAsString(data));
    }

    static class Data {
        List<String> blocks;
        List<Group> groups;

        public List<String> getBlocks() {
            return blocks;
        }

        public void setBlocks(List<String> blocks) {
            this.blocks = blocks;
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }
    }

    static class Group {
        String name;
        List<Entity> entities;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Entity> getEntities() {
            return entities;
        }

        public void setEntities(List<Entity> entities) {
            this.entities = entities;
        }
    }

    static class Entity {
        String block;
        String name;
        List<String> tags;
        String htmlUrl;
        String rawUrl;

        public String getBlock() {
            return block;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public void setBlock(String block) {
            this.block = block;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public void setHtmlUrl(String htmlUrl) {
            this.htmlUrl = htmlUrl;
        }

        public String getRawUrl() {
            return rawUrl;
        }

        public void setRawUrl(String rawUrl) {
            this.rawUrl = rawUrl;
        }
    }

    private static class GistReaderImpl implements GistReader {
        private List<Gist> gists;

        public GistReaderImpl(List<Gist> gists) {
            this.gists = gists;
        }

        @Override
        public void read(String name, String content, URL html, URL raw) throws GistReaderException {
            Gist.create(name, content, html, raw).ifPresent(g -> gists.add(g));
        }

        @Override
        public void close() throws Exception {

        }
    }

    private static class Gist {
        private String block;
        private List<String> tags;
        private String name;
        private String htmlUrl;
        private String rawUrl;
        private String type;

        private Gist(String block, List<String> tags, String name, String htmlUrl, String rawUrl, String type) {
            this.block = block;
            this.tags = tags;
            this.name = name;
            this.htmlUrl = htmlUrl;
            this.rawUrl = rawUrl;
            this.type = type;
        }

        public static Optional<Gist> create(String name, String content, URL html, URL raw) {
            Gist gist = null;

            if (name.contains("-")) {
                String[] array = name.split("-");
                String block = array[0];
                String gistName = array[array.length - 1];
                String type = null;
                if (gistName.contains(".")) {
                    String[] nameArray = gistName.split("\\.");
                    if (nameArray.length > 1) {
                        type = nameArray[1];
                    }
                    gistName = nameArray[0];
                    gistName = (gistName == null || gistName.equals("")) ? type : gistName;
                }

                if (array.length == 2) {
                    gist = new Gist(block, new ArrayList<>(), gistName, html.toString(), raw.toString(), type);
                } else {
                    final List<String> tags = Arrays.asList(array).subList(1, array.length - 1);
                    gist = new Gist(block, tags, gistName, html.toString(), raw.toString(), type);
                }
            } else {
                LOG.log(Level.WARNING, "Name '" + name + "' not contains '-'");
            }
            return Optional.ofNullable(gist);
        }

        public String getBlock() {
            return block;
        }

        public List<String> getTags() {
            return tags;
        }

        public String getName() {
            return name;
        }

        public String getHtmlUrl() {
            return htmlUrl;
        }

        public String getRawUrl() {
            return rawUrl;
        }

        public String getType() {
            return type;
        }
    }
}
