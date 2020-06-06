package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager {

    private static final String REFRESH_RATE_ATTR = "refreshTickRate";
    private static final int DEFAULT_REFRESH_RATE = 5; // apply enchants every 5 server ticks
    private static final EnchantManager MANAGER = new EnchantManager(getRefreshRateFromData());
    private static final Logger LOGGER = LogManager.getLogger(EnchantManager.class);
    private final int refreshRate;

    public EnchantManager(int refreshRate) {
        this.refreshRate = refreshRate;
    }

    private static int getRefreshRateFromData() {
        final DataStorageInstance data = CustomEnchants.CHANCE_DATA.getData(CustomEnchants.DEV_DATA_FILENAME);
        if (!data.hasAttr(REFRESH_RATE_ATTR))
            data.putAttr(REFRESH_RATE_ATTR, DEFAULT_REFRESH_RATE);
        return data.getIntegerOrDefault(REFRESH_RATE_ATTR, DEFAULT_REFRESH_RATE);
    }

    public static EnchantManager getEnchantManager() {
        return MANAGER;
    }

    public static void applyEnchants() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyEnchantsOnPlayer(player);
        }
    }

    public static void applyEnchantsOnPlayer(Player player) {
        for (ItemStack item : getEquipments(player)) {
            for (Enchantment ench : item.getEnchantments().keySet()) {
                if (ench instanceof EnchantWrapper) {
                    EnchantWrapper customEnchant = (EnchantWrapper) ench;
                    LOGGER.info(String.format("Player %s has equiped %s.", player.getDisplayName(), customEnchant.getName()));
                    if (customEnchant.hasEffect()) {
                        customEnchant.applyEffect(player);
                    }
                }
            }
        }
    }

    public static List<ItemStack> getEquipments(Player player) {
        final EntityEquipment equipment = player.getEquipment();
        final List<ItemStack> collect = Stream.of(equipment.getItemInHand(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots()).filter(Objects::nonNull).collect(Collectors.toList());
        LOGGER.info(String.format("Player %s has %d equipments.", player.getDisplayName(), collect.size()));
        return collect;
    }

    public int getRefreshRate() {
        return refreshRate;
    }
}
