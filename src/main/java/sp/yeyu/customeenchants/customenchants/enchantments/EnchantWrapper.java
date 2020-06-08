package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
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
import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class EnchantWrapper extends Enchantment {

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

    public static double increaseEnchantmentChanceForPlayer(EnchantWrapper enchantment, Player player, double chance) {
        String enchantId = EnchantWrapper.getChanceVariableName(enchantment);
        final DataStorageInstance playerData = EnchantPlus.getChanceData().getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) + chance;
        newChance = Double.min(newChance, 100D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
    }

    public static void enchantItem(ItemStack item, int level, EnchantWrapper enchantment) {
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        lore.add(EnchantWrapper.getEnchantmentLoreName(enchantment, level));
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(enchantment, level);
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

    public static String convertToDisplayName(Enchantment ench) {
        return WordUtils.capitalize(String.join(" ", ench.getName().toLowerCase().split("_")));
    }


    public abstract void applyEffect(Player player);

    public abstract boolean hasEffect();
}
