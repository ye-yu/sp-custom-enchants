package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.utils.EntityUtils;

public class Focus extends EnchantWrapper implements Listener {
    private static final Logger LOGGER = LogManager.getLogger(Focus.class);

    public Focus(int id, String name) {
        super(id, name);
        this.description = "applies critical hit on each sword swing";
    }

    @Override
    public void applyEffect(Player player) {

    }

    @Override
    public boolean hasEffect() {
        return false;
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
        return getItemTarget().includes(item) || EnchantWrapper.isBook(item);
    }

    @EventHandler
    public void onLivingEntityHit(EntityDamageByEntityEvent target) {
        if (target.getDamager() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) target.getDamager();
            if (attacker.getEquipment().getItemInHand().containsEnchantment(EnchantPlus.EnchantEnum.FOCUS_ENCHANTMENT.getEnchantment())) {
                if (!EntityUtils.isValidCritical(attacker)) {
                    LOGGER.info(String.format("Applying critial hits to %s. Damage before: %.02f. Damage after: %.02f", target.getEntity().getName(), target.getDamage(), target.getDamage() * 1.5));
                    double damage = target.getDamage() * 1.5;
                    target.setDamage(damage);
                }
            }

        }
    }
}
