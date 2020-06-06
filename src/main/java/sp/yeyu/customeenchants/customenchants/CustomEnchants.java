package sp.yeyu.customeenchants.customenchants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.enchantments.Focus;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorage;

import java.lang.reflect.Field;
import java.util.HashMap;

public final class CustomEnchants extends JavaPlugin implements Listener {
    public static final Enchantment FOCUS_ENCHANTMENT = new Focus(181, "focus");
    private static final Logger LOGGER = LogManager.getLogger(CustomEnchants.class);
    public static final String NAMESPACE = "EnchantPlus";
    public static final DataStorage CHANCE_DATA = new DataStorage(NAMESPACE);

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
        // registering one enchantment
        registerEnchantment(FOCUS_ENCHANTMENT);

        getServer().getPluginManager().registerEvents((Listener) FOCUS_ENCHANTMENT, this);
        getServer().getPluginManager().registerEvents(this, this);
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

            int removeId = ((EnchantWrapper) FOCUS_ENCHANTMENT).getRegisteredId();
            byId.remove(removeId);
            byName.remove(FOCUS_ENCHANTMENT.getName());

        } catch (Exception ignored) {
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent player) {
    }
}
