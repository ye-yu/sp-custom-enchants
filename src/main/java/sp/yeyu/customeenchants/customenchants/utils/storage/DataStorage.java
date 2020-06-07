package sp.yeyu.customeenchants.customenchants.utils.storage;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;

import java.io.File;
import java.util.UUID;

public class DataStorage {
    public final String namespace;

    public DataStorage(String namespace) {
        this.namespace = namespace;
    }


    public DataStorageInstance getPlayerData(Player p) {
        final UUID uniqueId = p.getUniqueId();
        return getData(uniqueId.toString());
    }

    public DataStorageInstance getData(String filename) {
        final File dataFolder = JavaPlugin.getPlugin(EnchantPlus.class).getDataFolder();
        if (!dataFolder.exists()) {
            if (dataFolder.mkdir()) {
                return new DataStorageInstance(filename, dataFolder.toPath().toAbsolutePath().toString());
            }
        }
        return new DataStorageInstance(filename, dataFolder.toPath().toAbsolutePath().toString());
    }
}
