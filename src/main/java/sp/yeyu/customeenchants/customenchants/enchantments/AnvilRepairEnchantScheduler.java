package sp.yeyu.customeenchants.customenchants.enchantments;

import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnvilRepairEnchantScheduler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ACTUAL_COST_PREFIX = ChatColor.YELLOW + "Actual cost: ";
    private static final HashMap<Player, AnvilRepairEnchantScheduler> schedule = Maps.newHashMap();
    private final ItemStack firstSlot;
    private final ItemStack secondSlot;
    private final HashMap<Enchantment, Integer> enchantments = Maps.newHashMap();
    private ItemStack targetSlot;
    private String displayName = "";
    private int cost = 0;
    private boolean isRepair;
    private boolean displayedItem = false;
    private Short damage = null;

    private AnvilRepairEnchantScheduler(ItemStack firstSlot, ItemStack secondSlot) {
        this.firstSlot = firstSlot;
        this.secondSlot = secondSlot;
    }

    public static AnvilRepairEnchantScheduler getScheduleData(Player player) {
        return schedule.get(player);
    }

    public static boolean removeScheduleData(Player player) {
        return Objects.nonNull(schedule.remove(player));
    }

    public static AnvilRepairEnchantScheduler newScheduleData(Player player, ItemStack firstSlot, ItemStack secondSlot) {
        schedule.put(player, new AnvilRepairEnchantScheduler(firstSlot, secondSlot));
        return getScheduleData(player);
    }

    public static boolean hasData(Player player) {
        return schedule.containsKey(player);
    }

    public boolean hasEnchantments() {
        return !enchantments.isEmpty();
    }

    public boolean addEnchantment(Enchantment enchantment, int level) {
        return Objects.isNull(enchantments.put(enchantment, level));
    }

    public boolean removeEnchantment(Enchantment enchantment) {
        return Objects.nonNull(enchantments.remove(enchantment));
    }

    public ItemStack constructDisplayItem() {
        final ItemStack item = new ItemStack(firstSlot.getType());
        if (Objects.nonNull(damage)) {
            item.setDurability(damage);
        }
        enchantments.keySet().forEach(enchantment -> EnchantWrapper.enchantItemWithoutLore(item, enchantments.get(enchantment), enchantment));
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.displayName);
        List<String> lores = enchantments.keySet().stream().filter(e -> e instanceof EnchantWrapper).map(e -> EnchantWrapper.getEnchantmentLoreName(e, enchantments.get(e))).collect(Collectors.toList());
        lores.add(ACTUAL_COST_PREFIX + cost);
        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack constructItem() {
        final ItemStack item = new ItemStack(firstSlot.getType());
        if (Objects.nonNull(damage)) {
            item.setDurability(damage);
        }
        enchantments.keySet().forEach(enchantment -> EnchantWrapper.enchantItemWithoutLore(item, enchantments.get(enchantment), enchantment));
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(this.displayName);
        List<String> lores = enchantments.keySet().stream().filter(e -> e instanceof EnchantWrapper).map(e -> EnchantWrapper.getEnchantmentLoreName(e, enchantments.get(e))).collect(Collectors.toList());
        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getFirstSlot() {
        return firstSlot;
    }

    public ItemStack getSecondSlot() {
        return secondSlot;
    }

    public HashMap<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    public boolean hasDisplayedItem() {
        return displayedItem;
    }

    public void setHasDisplayedItem(boolean displayedItem) {
        this.displayedItem = displayedItem;
    }

    public boolean isRepair() {
        return isRepair;
    }

    public void setRepair(boolean repair) {
        isRepair = repair;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public ItemStack getTargetSlot() {
        return targetSlot;
    }

    public void setTargetSlot(ItemStack targetSlot) {
        this.targetSlot = targetSlot;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setDamage(Short damage) {
        this.damage = damage;
    }
}
