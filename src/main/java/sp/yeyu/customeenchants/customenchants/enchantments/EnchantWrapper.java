package sp.yeyu.customeenchants.customenchants.enchantments;

import com.google.common.collect.Maps;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.utils.RomanNumeral;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class EnchantWrapper extends Enchantment {

    public static final HashMap<String, String> vanillaEnchDisplayName = Maps.newHashMap();
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        vanillaEnchDisplayName.put("ARROW_DAMAGE", "Power");
        vanillaEnchDisplayName.put("ARROW_FIRE", "Flame");
        vanillaEnchDisplayName.put("ARROW_INFINITE", "Infinity");
        vanillaEnchDisplayName.put("ARROW_KNOCKBACK", "Punch");
        vanillaEnchDisplayName.put("BINDING_CURSE", "Curse of Binding");
        vanillaEnchDisplayName.put("DAMAGE_ALL", "Sharpness");
        vanillaEnchDisplayName.put("DAMAGE_ARTHROPODS", "Bane of Arthropods");
        vanillaEnchDisplayName.put("DAMAGE_UNDEAD", "Smite");
        vanillaEnchDisplayName.put("DEPTH_STRIDER", "Depth Strider");
        vanillaEnchDisplayName.put("DIG_SPEED", "Efficiency");
        vanillaEnchDisplayName.put("DURABILITY", "Unbreaking");
        vanillaEnchDisplayName.put("FIRE_ASPECT", "Fire Aspect");
        vanillaEnchDisplayName.put("FROST_WALKER", "Frost Walker");
        vanillaEnchDisplayName.put("KNOCKBACK", "Knockback");
        vanillaEnchDisplayName.put("LOOT_BONUS_BLOCKS", "Fortune");
        vanillaEnchDisplayName.put("LOOT_BONUS_MOBS", "Looting");
        vanillaEnchDisplayName.put("LUCK", "Luck of the Sea");
        vanillaEnchDisplayName.put("LURE", "Lure");
        vanillaEnchDisplayName.put("MENDING", "Mending");
        vanillaEnchDisplayName.put("OXYGEN", "Respiration");
        vanillaEnchDisplayName.put("PROTECTION_ENVIRONMENTAL", "Protection");
        vanillaEnchDisplayName.put("PROTECTION_EXPLOSIONS", "Blast Protection");
        vanillaEnchDisplayName.put("PROTECTION_FALL", "Feather Falling");
        vanillaEnchDisplayName.put("PROTECTION_FIRE", "Fire Protection");
        vanillaEnchDisplayName.put("PROTECTION_PROJECTILE", "Projectile Protection");
        vanillaEnchDisplayName.put("SILK_TOUCH", "Silk Touch");
        vanillaEnchDisplayName.put("SWEEPING_EDGE", "Sweeping Edge");
        vanillaEnchDisplayName.put("THORNS", "Thorns");
        vanillaEnchDisplayName.put("VANISHING_CURSE", "Cure of Vanishing");
        vanillaEnchDisplayName.put("WATER_WORKER", "Aqua Affinity");
    }

    final String name;
    final int registeredId;
    protected String description;

    public EnchantWrapper(int id, String name) {
        super(id);
        this.name = name;
        this.registeredId = id;
    }

    public static String getChanceVariableName(EnchantWrapper enchant) {
        return enchant.getVariableName() + "Chance";
    }

    public static String getTickSkipVariableName(EnchantWrapper enchant) {
        return enchant.getVariableName() + "TickSkip";
    }

    public static double increaseEnchantmentChanceForPlayer(EnchantWrapper enchantment, Player player, double chance) {
        String enchantId = EnchantWrapper.getChanceVariableName(enchantment);
        final DataStorageInstance playerData = EnchantPlus.getChanceData().getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) + chance;
        newChance = Double.min(newChance, 100D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
    }

    public static void enchantItem(ItemStack item, int level, Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore;
        if (!meta.hasLore())
            lore = new ArrayList<>();
        else
            lore = (ArrayList<String>) meta.getLore();

        lore.add(EnchantWrapper.getEnchantmentLoreName(enchantment, level));
        meta.setLore(lore);
        item.setItemMeta(meta);
        if (enchantment instanceof EnchantWrapper)
            item.addUnsafeEnchantment(enchantment, level);
        else
            item.addEnchantment(enchantment, level);
    }

    public static void enchantItemWithoutLore(ItemStack item, int level, Enchantment enchantment) {
        if (enchantment instanceof EnchantWrapper)
            item.addUnsafeEnchantment(enchantment, level);
        else
            item.addEnchantment(enchantment, level);
    }

    public static double reduceEnchantmentChanceForPlayer(EnchantWrapper enchantment, Player player, double chance) {
        String enchantId = EnchantWrapper.getChanceVariableName(enchantment);
        final DataStorageInstance playerData = EnchantPlus.getChanceData().getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) - chance;
        newChance = Double.max(newChance, 0D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
    }

    public static boolean isBook(ItemStack item) {
        return item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK;
    }

    public static String getEnchantmentLoreName(Enchantment enchantment, int level) {
        if (enchantment.getMaxLevel() > 1)
            return (String.format("%s%s %s", ChatColor.GRAY, convertToDisplayName(enchantment), RomanNumeral.toRoman(level)));
        else
            return (String.format("%s%s", ChatColor.GRAY, convertToDisplayName(enchantment)));

    }

    public static String convertToDisplayName(Enchantment ench) {
        if (!(ench instanceof EnchantWrapper))
            return WordUtils.capitalize(String.join(" ", vanillaEnchDisplayName.getOrDefault(ench.getName(), "Unknown")));
        return WordUtils.capitalize(String.join(" ", ench.getName().toLowerCase().split("_")));
    }

    public String getDescription() {
        if (getMaxLevel() > 1)
            return String.format("%d: %s [I-%s] - %s", this.registeredId, convertToDisplayName(this), RomanNumeral.toRoman(getMaxLevel()), description);
        return String.format("%d: %s - %s", this.registeredId, getName(), description);
    }

    public int getRegisteredId() {
        return this.registeredId;
    }

    @Override
    public String getName() {
        return name.toUpperCase().replace(" ", "_");
    }

    public String getVariableName() {
        return WordUtils.capitalize(name).replace(" ", "");
    }

    public abstract void applyEffect(Player player);

    public abstract boolean hasEffect();

}
