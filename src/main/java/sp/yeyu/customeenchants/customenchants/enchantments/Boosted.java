package sp.yeyu.customeenchants.customenchants.enchantments;

import com.google.common.collect.Lists;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import sp.yeyu.customeenchants.customenchants.managers.EnchantManager;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Boosted extends EnchantWrapper implements Listener, Persistence {

    private static final ArrayList<Player> healthBoostSchedule = Lists.newArrayList();

    public Boosted(int id, String name, Rarity rarity) {
        super(id, name, rarity);
        this.description = "gives a health boost effect based on the level of enchant (does not stack; highest level is chosen)";
    }

    @Override
    public void applyEffect(Player player) {
        final Enchantment ench = EnchantManager.EnchantEnum.BOOSTED_ENCHANTMENT.getEnchantment();
        int level = Stream.of(player.getEquipment().getArmorContents()).map(e -> e.getEnchantmentLevel(ench)).reduce(Integer::max).orElse(0);
        player.setMaxHealth(20 + 4 * level);
        if (level == 0) {
            healthBoostSchedule.remove(player);
        } else {
            if (!healthBoostSchedule.contains(player)) {
                player.addPotionEffect(PotionEffectType.REGENERATION.createEffect(100, 2));
                healthBoostSchedule.add(player);
            }
        }
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
        return EnchantmentTarget.ARMOR;
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
