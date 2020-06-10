package sp.yeyu.customeenchants.customenchants.managers;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.enchantments.Boosted;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.enchantments.Focus;
import sp.yeyu.customeenchants.customenchants.enchantments.Persistence;
import sp.yeyu.customeenchants.customenchants.enchantments.Springy;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager implements Listener {

    private static final ArrayList<Persistence> allPersistence = Lists.newArrayList();
    private static final Logger LOGGER = LogManager.getLogger();

    public static void applyEnchants() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyEnchantsOnPlayer(player);
        }
    }

    public static void applyEnchantsOnPlayer(Player player) {
        for (ItemStack item : getEquipments(player)) {
            for (Enchantment ench : item.getEnchantments().keySet()) {
                if (ench instanceof EnchantWrapper && !(ench instanceof Persistence)) {
                    EnchantWrapper customEnchant = (EnchantWrapper) ench;
                    if (customEnchant.hasEffect()) {
                        customEnchant.applyEffect(player);
                    }
                }
            }
        }
        allPersistence.forEach(e -> e.applyEffect(player));
    }

    public static void registerEnchantment(Enchantment enchantment) {
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
            if (enchantment instanceof Persistence) {
                allPersistence.add((Persistence) enchantment);
            }
            LOGGER.info(String.format("(%s) Registered %s", EnchantPlus.PLUGIN_NAME, enchantment.getName()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<ItemStack> getEquipments(Player player) {
        final EntityEquipment equipment = player.getEquipment();
        return Stream.of(equipment.getItemInHand(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent itemEvent) {
        if (itemEvent.getEnchanter().getLevel() < 30) return;
        if (itemEvent.getEnchantsToAdd().size() > 4) return;
        int count = 0;
        final ItemStack item = itemEvent.getItem();
        final Random random = new Random(Instant.now().toEpochMilli());
        for (EnchantEnum ench : EnchantEnum.values()) {
            final EnchantWrapper enchantment = ench.getEnchantment();
            if (enchantment.canEnchantItem(item)) {
                final Double chance = EnchantPlus.getPluginData().getPlayerData(itemEvent.getEnchanter()).getDoubleOrDefault(EnchantUtils.getChanceVariableName(enchantment), 0D);
                final double roll = random.nextDouble() * 100 + 1;
                if (chance > roll) {
                    int level = random.nextInt(enchantment.getMaxLevel()) + 1;
                    EnchantUtils.enchantItem(item, level, enchantment);
                    EnchantUtils.reduceEnchantmentChanceForPlayer(enchantment, itemEvent.getEnchanter(), chance * 0.2);
                    count++;
                }

                if (count + itemEvent.getEnchantsToAdd().size() > 4) {
                    return;
                }
            }
        }
    }


    public enum EnchantEnum {
        FOCUS_ENCHANTMENT(new Focus(131, "FOCUS", EnchantWrapper.Rarity.RARE)),
        SPRINGY_ENCHANTMENT(new Springy(132, "SPRINGY", EnchantWrapper.Rarity.COMMON)),
        BOOSTED_ENCHANTMENT(new Boosted(133, "BOOSTED", EnchantWrapper.Rarity.COMMON));

        private final EnchantWrapper enchantment;

        EnchantEnum(EnchantWrapper enchantment) {
            this.enchantment = enchantment;
            registerEnchantment(this.getEnchantment());
        }

        public EnchantWrapper getEnchantment() {
            return enchantment;
        }
    }
}
