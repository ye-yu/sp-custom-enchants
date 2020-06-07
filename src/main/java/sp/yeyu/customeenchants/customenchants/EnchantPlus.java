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
import sp.yeyu.customeenchants.customenchants.enchantments.Anvil;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantManager;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.enchantments.Focus;
import sp.yeyu.customeenchants.customenchants.enchantments.Springy;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorage;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class EnchantPlus extends JavaPlugin implements Listener {
    public static final String DEV_DATA_FILENAME = "dev.txt";
    public static final Logger LOGGER = LogManager.getLogger(EnchantPlus.class);
    public static EnchantPlus ce;
    private static DataStorage CHANCE_DATA;

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

    public static EnchantWrapper getEnchantmentByDisplayName(String displayName) {
        for (EnchantEnum enchantment : EnchantEnum.values()) {
            if (enchantment.getEnchantment().getName().equalsIgnoreCase(displayName))
                return enchantment.getEnchantment();
        }
        return null;
    }

    public static DataStorage getChanceData() {
        return CHANCE_DATA;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        CHANCE_DATA = new DataStorage(getName());
        ce = this;

        // registering one enchantment
        registerEnchantment(EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment());
        registerEnchantment(EnchantEnum.SPRINGY_ENCHANTMENT.getEnchantment());

        getServer().getPluginManager().registerEvents((Listener) EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(EnchantManager.getEnchantManager(), this);


        final DataStorageInstance data = CHANCE_DATA.getData("dev.txt");
        final int devMode = data.getIntegerOrDefault("devmode", 0);
        if (devMode == 0) {
            data.putAttr("devmode", 0);
        } else {
            LOGGER.info("(CustomEnchants) Developer mode is on.");
        }

        getCommand("chance").setExecutor(new ShowChance());
        getCommand("buildchance").setExecutor(new BuildChance(devMode != 0));
        getCommand("enchants").setExecutor(new Enchants());

        final int repeatingTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, EnchantManager::applyEnchants, 0, EnchantManager.getEnchantManager().getRefreshRate());
        if (repeatingTask == -1) {
            LOGGER.error("Unable to schedule enchantments effect!");
        }
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

            int removeId = EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment().getRegisteredId();
            byId.remove(removeId);
            byName.remove(EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment().getName());

        } catch (Exception ignored) {
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

    public enum EnchantEnum {
        FOCUS_ENCHANTMENT(new Focus(131, "focus")),
        SPRINGY_ENCHANTMENT(new Springy(132, "springy")),
        ANVIL_TAG(new Anvil(130, "anvil"));

        private final EnchantWrapper enchantment;

        EnchantEnum(EnchantWrapper enchantment) {
            this.enchantment = enchantment;
        }

        public EnchantWrapper getEnchantment() {
            return enchantment;
        }
    }
}