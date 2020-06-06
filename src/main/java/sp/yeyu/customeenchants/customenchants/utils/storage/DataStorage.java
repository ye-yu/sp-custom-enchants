package sp.yeyu.customeenchants.customenchants.utils.storage;

import org.bukkit.entity.Player;

import java.util.UUID;

public class DataStorage {
    public final String namespace;

    public DataStorage(String namespace) {
        this.namespace = namespace;
    }


    public DataStorageInstance getPlayerData(Player p) {
        final UUID uniqueId = p.getUniqueId();
        return new DataStorageInstance(uniqueId.toString(), namespace);
    }


}
