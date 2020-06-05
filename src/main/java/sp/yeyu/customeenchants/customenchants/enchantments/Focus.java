package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.utils.EntityUtils;

import java.util.Arrays;
import java.util.List;

public class Focus extends EnchantWrapper implements Listener {
    private static final Logger LOGGER = LogManager.getLogger(Focus.class);

    private static final List<Material> VALID_ITEMS = Arrays.asList(
            Material.WOOD_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.GOLD_SWORD,
            Material.DIAMOND_SWORD);

    public Focus(int id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getStartLevel() {
        return 5;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return VALID_ITEMS.contains(item.getType());
    }

    @EventHandler
    public void onLivingEntityHit(EntityDamageByEntityEvent target) {
        if (target.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) target.getDamager();
            if (attacker.getEquipment().getItemInHand().containsEnchantment(CustomEnchants.FOCUS_ENCHANTMENT)) {
                LOGGER.info(String.format("%s attacking %s with Focus enchantment.", attacker.getName(), target.getEntity().getName()));
                if (!EntityUtils.isValidCritical(attacker)) {
                    LOGGER.info(String.format("Applying critial hits to %s. Damage before: %.02f. Damage after: %.02f", target.getEntity().getName(), target.getDamage(), target.getDamage() * 1.5));
                    double damage = target.getDamage() * 1.5;
                    target.setDamage(damage);
                } else {
                    LOGGER.info(String.format("Already performing critial hits on %s.Damage: %.02f", target.getEntity().getName(), target.getDamage()));
                }
            }

        }
    }
}
