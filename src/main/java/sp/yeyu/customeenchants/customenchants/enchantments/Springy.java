package sp.yeyu.customeenchants.customenchants.enchantments;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import sp.yeyu.customeenchants.customenchants.managers.EnchantManager;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.util.Objects;

public class Springy extends EnchantWrapper implements Listener {

    public Springy(int id, String name, Rarity rarity) {
        super(id, name, rarity);
        this.description = "gives a jump boost effect based on the level of enchant";
    }

    @Override
    public void applyEffect(Player player) {
        final ItemStack boots = player.getEquipment().getBoots();
        if (Objects.isNull(boots)) return;
        final int level = boots.getEnchantmentLevel(EnchantManager.EnchantEnum.SPRINGY_ENCHANTMENT.getEnchantment()) - 1;
        player.addPotionEffect(PotionEffectType.JUMP.createEffect(EnchantUtils.calculateTotalEffectDuration(), level), true);
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
        return getItemTarget().includes(item) || EnchantUtils.isBook(item);
    }
}
