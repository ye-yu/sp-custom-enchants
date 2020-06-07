package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.utils.RomanNumeral;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

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
        final DataStorageInstance playerData = EnchantPlus.CHANCE_DATA.getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) + chance;
        newChance = Double.min(newChance, 100D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
    }

    public static double reduceEnchantmentChanceForPlayer(EnchantWrapper enchantment, Player player, double chance) {
        String enchantId = EnchantWrapper.getChanceVariableName(enchantment);
        final DataStorageInstance playerData = EnchantPlus.CHANCE_DATA.getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) - chance;
        newChance = Double.max(newChance, 0D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
    }

    public static boolean isBook(ItemStack item) {
        return item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK;
    }

    public String getDescription() {
        if (getMaxLevel() > 1)
            return String.format("%s [I-%s] - %s", getName(), RomanNumeral.toRoman(getMaxLevel()), description);
        return String.format("%s - %s", getName(), description);
    }

    public int getRegisteredId() {
        return this.registeredId;
    }

    @Override
    public String getName() {
        return StringUtils.capitalize(name);
    }

    public String getVariableName() {
        return StringUtils.capitalize(name).replace(" ", "");
    }

    public abstract void applyEffect(Player player);

    public abstract boolean hasEffect();
}
