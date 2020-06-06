package sp.yeyu.customeenchants.customenchants.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.utils.RomanNumeral;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager implements Listener {

    private static final String REFRESH_RATE_ATTR = "refreshTickRate";
    private static final int DEFAULT_REFRESH_RATE = 5; // apply enchants every 5 server ticks
    private static final EnchantManager MANAGER = new EnchantManager(getRefreshRateFromData());
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
                    if (customEnchant.hasEffect()) {
                        customEnchant.applyEffect(player);
                    }
                }
            }
        }
    }

    public static List<ItemStack> getEquipments(Player player) {
        final EntityEquipment equipment = player.getEquipment();
        return Stream.of(equipment.getItemInHand(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent itemEvent) {
        final ItemStack item = itemEvent.getItem();
        final Random random = new Random();
        for (CustomEnchants.EnchantEnum ench : CustomEnchants.EnchantEnum.values()) {
            final EnchantWrapper enchantment = ench.getEnchantment();
            if (enchantment.canEnchantItem(item)) {
                final Double chance = CustomEnchants.CHANCE_DATA.getPlayerData(itemEvent.getEnchanter()).getDoubleOrDefault(EnchantWrapper.getChanceVariableName(enchantment), 0D);
                final double roll = random.nextDouble() * 100 + 1;
                if (chance > roll) {
                    int level = random.nextInt(enchantment.getMaxLevel()) + 1;
                    ItemMeta meta = item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<>();

                    if (enchantment.getMaxLevel() > 1)
                        lore.add(String.format("%s%s %s", ChatColor.GRAY, enchantment.getName(), RomanNumeral.toRoman(level)));
                    else
                        lore.add(String.format("%s%s", ChatColor.GRAY, enchantment.getName()));
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    item.addUnsafeEnchantment(enchantment, level);
                }
            }
        }
    }
}
