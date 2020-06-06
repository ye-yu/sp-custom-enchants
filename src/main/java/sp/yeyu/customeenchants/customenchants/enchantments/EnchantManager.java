package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

public class EnchantManager {

    private static final EnchantManager MANAGER = new EnchantManager(getRefreshRateFromData());
    private static final String REFRESH_RATE_ATTR = "refreshTickRate";
    private static final int DEFAULT_REFRESH_RATE = 100; // apply enchants every 50 server ticks
    private static final Logger LOGGER = LogManager.getLogger(EnchantManager.class);

    private static int getRefreshRateFromData() {
        final DataStorageInstance data = CustomEnchants.CHANCE_DATA.getData(CustomEnchants.DEV_DATA_FILENAME);
        if (!data.hasAttr(REFRESH_RATE_ATTR))
            data.putAttr(REFRESH_RATE_ATTR, DEFAULT_REFRESH_RATE);
        return data.getIntegerOrDefault(REFRESH_RATE_ATTR, DEFAULT_REFRESH_RATE);
    }

    private final int refreshRate;

    public EnchantManager(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    public static EnchantManager getEnchantManager() {
        return MANAGER;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public static void applyEnchants() {
        for(CustomEnchants.Enchants enchantEnum: CustomEnchants.Enchants.values()) {
            EnchantWrapper enchantment = enchantEnum.getEnchantment();
            if (enchantment.hasEffect()) {
                final DataStorageInstance data = CustomEnchants.CHANCE_DATA.getData(enchantment.getVariableName());
                for(String uuid: data.getKeys()) {
                    final Player player = JavaPlugin.getPlugin(CustomEnchants.class).getServer().getPlayer(uuid);
                    enchantment.applyEffect(player);
                }
            }
        }
    }
}
