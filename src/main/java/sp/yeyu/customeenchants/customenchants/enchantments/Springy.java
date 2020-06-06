package sp.yeyu.customeenchants.customenchants.enchantments;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;

import java.util.Objects;

public class Springy extends EnchantWrapper implements Listener {

    public Springy(int id, String name) {
        super(id, name);
        this.description = "gives a permanent jump boost effect based on the level of enchant";
    }

    @Override
    public void applyEffect(Player player) {
        final ItemStack boots = player.getEquipment().getBoots();
        if (Objects.isNull(boots)) return;
        final int level = boots.getEnchantmentLevel(CustomEnchants.EnchantEnum.SPRINGY_ENCHANTMENT.getEnchantment()) - 1;
        player.addPotionEffect(PotionEffectType.JUMP.createEffect(EnchantManager.getEnchantManager().getRefreshRate() + 40, level));
    }

    @Override
    public boolean hasEffect() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_FEET;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return getItemTarget().includes(item) || EnchantWrapper.isBook(item);
    }
}
