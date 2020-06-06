package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

public abstract class EnchantWrapper extends Enchantment {

    final String name;
    final int registeredId;

    public EnchantWrapper(int id, String name) {
        super(id);
        this.name = name;
        this.registeredId = id;
    }

    public static String getChanceVariableName(EnchantWrapper enchant) {
        return enchant.getVariableName() + "Chance";
    }

    public static double increasePlayerChance(EnchantWrapper enchantment, Player player, double chance) {
        String enchantId = EnchantWrapper.getChanceVariableName(enchantment);
        final DataStorageInstance playerData = CustomEnchants.CHANCE_DATA.getPlayerData(player);
        double newChance = playerData.getDoubleOrDefault(enchantId, 0D) + chance;
        newChance = Double.min(newChance, 100D);
        playerData.putAttr(enchantId, newChance);
        return newChance;
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
