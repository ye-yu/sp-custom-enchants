package sp.yeyu.customeenchants.customenchants.enchantments;

import com.google.common.collect.Maps;
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
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager implements Listener {

    private static final EnchantManager MANAGER = new EnchantManager(getRefreshRateFromData());
    private final int refreshRate;
    private final int effectDuration;

    public EnchantManager(HashMap<String, Integer> attributes) {
        this.refreshRate = attributes.get(Attributes.REFRESH_RATE.attrName);
        this.effectDuration = attributes.get(Attributes.EFFECT_DURATION.attrName);
    }

    private static HashMap<String, Integer> getRefreshRateFromData() {
        final DataStorageInstance data = CustomEnchants.CHANCE_DATA.getData(CustomEnchants.DEV_DATA_FILENAME);
        if (!data.hasAttr(Attributes.REFRESH_RATE.attrName))
            data.putAttr(Attributes.REFRESH_RATE.attrName, Attributes.REFRESH_RATE.defaultValue);

        if (!data.hasAttr(Attributes.EFFECT_DURATION.attrName))
            data.putAttr(Attributes.EFFECT_DURATION.attrName, Attributes.EFFECT_DURATION.defaultValue);

        HashMap<String, Integer> attributes = Maps.newHashMap();
        attributes.put(Attributes.REFRESH_RATE.attrName, data.getIntegerOrDefault(Attributes.REFRESH_RATE.attrName, Attributes.REFRESH_RATE.defaultValue));
        attributes.put(Attributes.EFFECT_DURATION.attrName, data.getIntegerOrDefault(Attributes.EFFECT_DURATION.attrName, Attributes.EFFECT_DURATION.defaultValue));
        return attributes;
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

    public static int calculateTotalEffectDuration() {
        return getEnchantManager().getRefreshRate() + getEnchantManager().getEffectDuration();
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent itemEvent) {
        if (itemEvent.getEnchanter().getLevel() < 30) return;
        if (itemEvent.getEnchantsToAdd().size() > 4) return;
        int count = 0;
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
                    EnchantWrapper.reduceEnchantmentChanceForPlayer(enchantment, itemEvent.getEnchanter(), chance * 0.2);
                    count++;
                }

                if (count + itemEvent.getEnchantsToAdd().size() > 4) {
                    return;
                }
            }
        }
    }

    private enum Attributes {
        REFRESH_RATE("refreshTickRate", 5),
        EFFECT_DURATION("effectDurationTick", 60);

        public final String attrName;
        public final int defaultValue;

        Attributes(String attrName, int value) {
            this.attrName = attrName;
            this.defaultValue = value;
        }
    }
}
