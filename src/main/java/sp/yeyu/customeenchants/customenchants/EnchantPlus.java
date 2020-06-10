package sp.yeyu.customeenchants.customenchants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.commands.BuildChance;
import sp.yeyu.customeenchants.customenchants.commands.Enchants;
import sp.yeyu.customeenchants.customenchants.commands.ShowChance;
import sp.yeyu.customeenchants.customenchants.managers.AnvilManager;
import sp.yeyu.customeenchants.customenchants.managers.DataManager;
import sp.yeyu.customeenchants.customenchants.managers.EnchantManager;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorage;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class EnchantPlus extends JavaPlugin implements Listener {
    public static final String DEV_DATA_FILENAME = "dev.txt";
    public static final Logger LOGGER = LogManager.getLogger(EnchantPlus.class);
    public static final String PLUGIN_NAME = "EnchantPlus";
    private static DataStorage PLUGIN_DATA;

    public static DataStorage getPluginData() {
        return PLUGIN_DATA;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        PLUGIN_DATA = new DataStorage(getName());

        getServer().getPluginManager().registerEvents((Listener) EnchantManager.EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new AnvilManager(), this);
        getServer().getPluginManager().registerEvents(new EnchantManager(), this);


        final int devMode = DataManager.IntAttributes.DEV_MODE.getValue();
        if (devMode != 0) {
            LOGGER.info(String.format("(%s) Developer mode is on.", PLUGIN_NAME));
        }

        getCommand("chance").setExecutor(new ShowChance());
        getCommand("buildchance").setExecutor(new BuildChance(devMode != 0));
        getCommand("enchants").setExecutor(new Enchants());

        final int repeatingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, EnchantManager::applyEnchants, 0, DataManager.IntAttributes.REFRESH_RATE.getValue());
        if (repeatingTask == -1) {
            LOGGER.error("Unable to schedule enchantments effect!");
        }
    }

    @SuppressWarnings("unchecked")
    public void onDisable() {
        for (EnchantManager.EnchantEnum enchantEnum : EnchantManager.EnchantEnum.values()) {
            try {
                Field byIdField = Enchantment.class.getDeclaredField("byId");
                Field byNameField = Enchantment.class.getDeclaredField("byName");

                byIdField.setAccessible(true);
                byNameField.setAccessible(true);

                HashMap<Integer, Enchantment> byId = (HashMap<Integer, Enchantment>) byIdField.get(null);
                HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) byNameField.get(null);

                int removeId = enchantEnum.getEnchantment().getRegisteredId();
                byId.remove(removeId);
                byName.remove(enchantEnum.getEnchantment().getName());
            } catch (Exception e) {
                LOGGER.error(String.format("Unable to unregister %s", enchantEnum.getEnchantment().getName()), e);
            }
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        EnchantManager.applyEnchantsOnPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent player) {
    }
}
