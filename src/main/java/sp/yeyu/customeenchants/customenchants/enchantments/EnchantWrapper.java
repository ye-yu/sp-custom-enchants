package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;

public abstract class EnchantWrapper extends Enchantment {

    final String name;
    final int registeredId;

    public EnchantWrapper(int id, String name) {
        super(id);
        this.name = name;
        this.registeredId = id;
    }

    public int getRegisteredId() {
        return this.registeredId;
    }

    @Override
    public String getName() {
        return StringUtils.capitalize(name);
    }
}
