package sp.yeyu.customeenchants.customenchants.utils.storage;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

public class DataStorageInstance {
    private static final Logger LOGGER = LogManager.getLogger(DataStorageInstance.class);
    public final String FILENAME;
    public final String DIRECTORY;
    public final String SEPARATOR = ":";
    private final HashMap<String, String> data = Maps.newHashMap();

    DataStorageInstance(String filename, String directory) {
        this.FILENAME = filename;
        this.DIRECTORY = directory;
        try (Scanner file = new Scanner(new FileInputStream(getFileInstance()))) {
            while (file.hasNext()) {
                final String s = file.nextLine();
                final String[] split = s.split(SEPARATOR, 2);
                data.put(split[0], split[1]);
            }

            LOGGER.info("Reading file: " + Paths.get(directory, filename).toAbsolutePath());
        } catch (IOException io) {
            LOGGER.warn("File " + filename + " does not exists. Creating a blank entry now.");
            if (Paths.get(directory).toFile().mkdir())
                writeDataToFile();
        }
    }

    public String getOrDefault(String key, String def) {
        return data.getOrDefault(key, def);
    }

    public Integer getIntegerOrDefault(String key, Integer def) {
        try {
            return Integer.parseInt(data.getOrDefault(key, def.toString()));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public Double getDoubleOrDefault(String key, Double def) {
        try {
            return Double.parseDouble(data.getOrDefault(key, def.toString()));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public boolean putAttr(String key, Object value) {
        data.put(key, value.toString());
        return writeDataToFile();
    }

    private boolean writeDataToFile() {
        try(FileWriter writer = new FileWriter(getFileInstance())) {
            for (String k: data.keySet()) {
                LOGGER.info("Writing: " + k + SEPARATOR + data.get(k));
                writer.write(k + SEPARATOR + data.get(k));
                writer.write("\n");
            }

            return true;
        } catch (IOException e) {
            LOGGER.error("Unable to write to " + getFileInstance().getAbsolutePath(), e);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("DataStorageInstance{filename:%s, directory:%s, absolute path:%s}",
                FILENAME,
                DIRECTORY,
                Paths.get(DIRECTORY, FILENAME).toAbsolutePath());
    }

    public boolean hasAttr(String key) {
        return data.containsKey(key);
    }

    public Set<String> getKeys() {
        return data.keySet();
    }

    public File getFileInstance() {
        return Paths.get(DIRECTORY, FILENAME).toFile();
    }
}
