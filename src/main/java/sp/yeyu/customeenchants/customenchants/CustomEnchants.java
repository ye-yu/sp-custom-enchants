package sp.yeyu.customeenchants.customenchants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.commands.BuildChance;
import sp.yeyu.customeenchants.customenchants.commands.ShowChance;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantManager;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.enchantments.Focus;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorage;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class CustomEnchants extends JavaPlugin implements Listener {
    private static final Logger LOGGER = LogManager.getLogger(CustomEnchants.class);
    public static final String NAMESPACE = "EnchantPlus";
    public static final String DEV_DATA_FILENAME = "dev.txt";
    public static final DataStorage CHANCE_DATA = new DataStorage(NAMESPACE);
    public static CustomEnchants ce;

    public enum Enchants {
        FOCUS_ENCHANTMENT(new Focus(181, "focus"));

        private final EnchantWrapper enchantment;
        Enchants(EnchantWrapper enchantment) {
            this.enchantment = enchantment;
        }

        public EnchantWrapper getEnchantment() {
            return enchantment;
        }
    }

    public static void registerEnchantment(Enchantment enchantment) {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
            LOGGER.info("Registered " + enchantment.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        ce = this;

        // registering one enchantment
        registerEnchantment(Enchants.FOCUS_ENCHANTMENT.getEnchantment());

        getServer().getPluginManager().registerEvents((Listener) Enchants.FOCUS_ENCHANTMENT.getEnchantment(), this);
        getServer().getPluginManager().registerEvents(this, this);

        final DataStorageInstance data = CHANCE_DATA.getData("dev.txt");
        final int devMode = data.getIntegerOrDefault("devmode", 0);
        if (devMode == 0) {
            data.putAttr("devmode", 0);
        } else {
            LOGGER.info("(CustomEnchants) Developer mode is on.");
        }

        getCommand("showchance").setExecutor(new ShowChance());
        getCommand("buildchance").setExecutor(new BuildChance(devMode != 0));

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, EnchantManager::applyEnchants, 1000, EnchantManager.getEnchantManager().getRefreshRate());
    }

    @SuppressWarnings("unchecked")
    public void onDisable() {
        try {
            Field byIdField = Enchantment.class.getDeclaredField("byId");
            Field byNameField = Enchantment.class.getDeclaredField("byName");

            byIdField.setAccessible(true);
            byNameField.setAccessible(true);

            HashMap<Integer, Enchantment> byId = (HashMap<Integer, Enchantment>) byIdField.get(null);
            HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) byNameField.get(null);

            int removeId = Enchants.FOCUS_ENCHANTMENT.getEnchantment().getRegisteredId();
            byId.remove(removeId);
            byName.remove(Enchants.FOCUS_ENCHANTMENT.getEnchantment().getName());

        } catch (Exception ignored) {
        }
    }

    public static EnchantWrapper getEnchantmentByDisplayName(String displayName) {
        for(Enchants enchantment: Enchants.values()) {
            if (enchantment.getEnchantment().getName().equalsIgnoreCase(displayName))
                return enchantment.getEnchantment();
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent player) {
    }
}
