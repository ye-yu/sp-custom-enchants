package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.WordUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;
import sp.yeyu.customeenchants.customenchants.utils.RomanNumeral;

public abstract class EnchantWrapper extends Enchantment {

    final String name;
    final int registeredId;
    protected String description;

    public EnchantWrapper(int id, String name) {
        super(id);
        this.name = name;
        this.registeredId = id;
    }

    public String getDescription() {
        if (getMaxLevel() > 1)
            return String.format("%d: %s [I-%s] - %s", this.registeredId, EnchantUtils.convertToDisplayName(this), RomanNumeral.toRoman(getMaxLevel()), description);
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
