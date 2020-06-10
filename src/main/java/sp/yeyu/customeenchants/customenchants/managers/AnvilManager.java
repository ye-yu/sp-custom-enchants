package sp.yeyu.customeenchants.customenchants.managers;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnvilManager implements Listener {

    private static final Logger LOGGER = LogManager.getLogger(AnvilManager.class);
    private static final HashMap<Player, ItemStack> pickUpSchedule = Maps.newHashMap();

    /*
     * Obtained from: https://bukkit.org/threads/inventory-anvil-events.142990/
     * */
    @EventHandler
    public static void onInventoryClick(InventoryClickEvent e) {
        // check whether the event has been cancelled by another plugin
        if (e.isCancelled()) return;
        HumanEntity ent = e.getWhoClicked();

        // not really necessary
        if (!(ent instanceof Player)) return;
        Player player = (Player) ent;
        Inventory inv = e.getInventory();

        // see if we are talking about an anvil here
        if (!(inv instanceof AnvilInventory)) return;

        AnvilInventory anvil = (AnvilInventory) inv;
        if (e.getRawSlot() == 2) {
            e.setResult(Event.Result.DENY);
            whenClickingResultingItem(anvil, player);
            player.setLevel(player.getLevel());
        } else {
            whenClickingAnvilSlot(e, anvil, player);
        }
    }

    private static void whenClickingResultingItem(AnvilInventory anvil, Player player) {
        if (!AnvilRepairEnchantScheduler.hasData(player)) return;
        final AnvilRepairEnchantScheduler scheduleData = AnvilRepairEnchantScheduler.getScheduleData(player);
        if (!scheduleData.hasDisplayedItem()) {
            // display the third slot yet
            ItemStack leftItem = scheduleData.getFirstSlot();
            ItemStack rightItem = scheduleData.getSecondSlot();
            if (Objects.nonNull(anvil.getItem(2))) {
                final ItemStack repairedItem = scheduleRepairItem(scheduleData, leftItem, rightItem, anvil.getItem(2));
                anvil.setItem(2, repairedItem);
                scheduleData.setRepair(true);
                scheduleData.setHasDisplayedItem(true);
            } else if (rightItem.getType().equals(Material.ENCHANTED_BOOK)) {
                final ItemStack enchantedItem = scheduleEnchantItemFromBook(scheduleData, leftItem, rightItem);
                if (Objects.nonNull(enchantedItem)) {
                    anvil.setItem(2, enchantedItem);
                    scheduleData.setRepair(false);
                    scheduleData.setHasDisplayedItem(true);
                    LOGGER.info("Item is being enchanted.");
                }
            }

            LOGGER.info("Resulting item lore:\n" + StringUtils.join(anvil.getItem(2).getItemMeta().getLore(), "\n"));
        } else {
            // prepare to return item
            ItemStack resultingItem = scheduleData.constructItem();
            LOGGER.info(String.format("Resulting lores:\n%s", StringUtils.join(resultingItem.getItemMeta().getLore(), "\n")));
            final int cost = scheduleData.getCost();
            if (player.getLevel() >= cost) {
                player.setLevel(player.getLevel() - cost);
                anvil.setItem(0, new ItemStack(Material.AIR));
                anvil.setItem(1, new ItemStack(Material.AIR));
                anvil.setItem(2, new ItemStack(Material.AIR));
                AnvilRepairEnchantScheduler.removeScheduleData(player);
                player.playSound(player.getLocation(), Sound.ANVIL_USE, 1, 1);
                player.setItemOnCursor(resultingItem);
                player.updateInventory();
            }
        }
    }

    private static int getCustomEnchantmentCost(ItemStack item) {
        int cost = 0;
        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (ench instanceof EnchantWrapper)
                cost += item.getEnchantmentLevel(ench);
        }
        LOGGER.info(String.format("%s has the additional repair cost of %d", item.getType(), cost));
        return cost;
    }

    private static int getRepairCost(ItemStack repairItem) {
        if (!(repairItem.getItemMeta() instanceof Repairable)) return 1;
        final Repairable itemMeta = (Repairable) repairItem.getItemMeta();
        LOGGER.info(String.format("%s has the vanilla repair cost of %d", repairItem.getType(), itemMeta.getRepairCost()));
        return itemMeta.getRepairCost();
    }

    private static void whenClickingAnvilSlot(InventoryClickEvent e, AnvilInventory anvil, Player player) {
        AnvilRepairEnchantScheduler.removeScheduleData(player); // always reset when user perform inventory change
        if (e.getRawSlot() < 2) {
            LOGGER.info(String.format("Player is clicking from the anvil slots. %s", e.getAction()));
            if (Arrays.asList(InventoryAction.PLACE_ALL, InventoryAction.PLACE_SOME, InventoryAction.PLACE_ONE, InventoryAction.SWAP_WITH_CURSOR).contains(e.getAction())) {
                insertItemInAnvilThirdSlot(anvil.getItem(0), anvil.getItem(1), e.getRawSlot(), player);
            } else if (Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE).contains(e.getAction())) {
                pickUpSchedule.put(player, e.getCurrentItem());
            }
        } else {
            LOGGER.info("Player is clicking from their inventory slots.");
            if (e.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                insertItemInAnvilThirdSlotFromShiftClick(e, anvil, player);
            } else if (Arrays.asList(InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE).contains(e.getAction())) {
                pickUpSchedule.put(player, e.getCurrentItem());
            }
        }
    }

    private static ItemStack scheduleEnchantItemFromBook(AnvilRepairEnchantScheduler scheduleData, ItemStack leftItem, ItemStack rightItem) {
        final List<Enchantment> validEnchants = rightItem.getEnchantments().keySet().stream().filter(e -> e.canEnchantItem(leftItem)).collect(Collectors.toList());
        if (validEnchants.isEmpty()) return null;
        for (Enchantment enchantment : leftItem.getEnchantments().keySet()) {
            scheduleData.addEnchantment(enchantment, leftItem.getEnchantmentLevel(enchantment));
        }

        for (Enchantment validEnchant : validEnchants) {
            scheduleData.addEnchantment(validEnchant, rightItem.getEnchantmentLevel(validEnchant));
        }

        int cost = validEnchants.size() * 5;
        scheduleData.setCost(cost);
        scheduleData.setDisplayName(leftItem.getItemMeta().hasDisplayName() ? leftItem.getItemMeta().getDisplayName() : getName(leftItem));

        return scheduleData.constructDisplayItem();
    }

    private static ItemStack scheduleRepairItem(AnvilRepairEnchantScheduler scheduleData, ItemStack leftItem, ItemStack rightItem, ItemStack resultingItem) {
        scheduleData.setTargetSlot(resultingItem);
        // register vanilla enchantment
        for (Enchantment enchantment : resultingItem.getEnchantments().keySet()) {
            if (!(enchantment instanceof EnchantWrapper)) {
                scheduleData.addEnchantment(enchantment, resultingItem.getEnchantmentLevel(enchantment));
            }
        }
        // register custom enchantments
        HashMap<EnchantWrapper, Integer> customEnchantments = mergeCustomEnchantments(leftItem, rightItem);
        for (EnchantWrapper customEnch : customEnchantments.keySet()) {
            scheduleData.addEnchantment(customEnch, customEnchantments.get(customEnch));
        }
        // calculate repair cost
        int cost = getRepairCost(resultingItem) + getCustomEnchantmentCost(resultingItem) + 1;
        scheduleData.setCost(cost);
        scheduleData.setDisplayName(resultingItem.getItemMeta().hasDisplayName() ? resultingItem.getItemMeta().getDisplayName() : getName(resultingItem));

        scheduleData.setDamage(resultingItem.getDurability());
        return scheduleData.constructDisplayItem();
    }

    private static HashMap<EnchantWrapper, Integer> mergeCustomEnchantments(ItemStack leftItem, ItemStack rightItem) {
        final HashMap<EnchantWrapper, Integer> leftEnchantments = getCustomEnchants(leftItem);
        final HashMap<EnchantWrapper, Integer> rightEnchantments = getCustomEnchants(rightItem);
        for (EnchantWrapper rightEnch : rightEnchantments.keySet()) {
            if (leftEnchantments.containsKey(rightEnch)) {
                if (leftEnchantments.get(rightEnch).equals(rightEnchantments.get(rightEnch))) {
                    leftEnchantments.put(
                            rightEnch,
                            rightEnchantments.get(rightEnch) >= rightEnch.getMaxLevel() ? rightEnch.getMaxLevel() : rightEnchantments.get(rightEnch) + 1);
                } else {
                    leftEnchantments.put(
                            rightEnch,
                            Integer.max(leftItem.getEnchantmentLevel(rightEnch), rightItem.getEnchantmentLevel(rightEnch)));
                }
            } else if (leftEnchantments.keySet().stream().noneMatch(e -> e.conflictsWith(rightEnch))) { // only all nonc conflicting enchants
                leftEnchantments.put(rightEnch, rightEnchantments.get(rightEnch));
            }
        }
        return leftEnchantments;
    }

    private static HashMap<EnchantWrapper, Integer> getCustomEnchants(ItemStack itemStack) {
        final List<EnchantWrapper> collect = itemStack.getEnchantments().keySet().stream().filter(e -> e instanceof EnchantWrapper).map(e -> (EnchantWrapper) e).collect(Collectors.toList());
        final HashMap<EnchantWrapper, Integer> customEnchants = Maps.newHashMap();
        for (EnchantWrapper customEnch : collect) {
            customEnchants.put(customEnch, itemStack.getEnchantmentLevel(customEnch));
            LOGGER.info(String.format("%s has the custom enchantment: %s", itemStack.getType(), EnchantUtils.convertToDisplayName(customEnch)));
        }
        return customEnchants;
    }

    private static void insertItemInAnvilThirdSlot(ItemStack leftItem, ItemStack rightItem, int rawSlot, Player player) {
        if (Objects.isNull(leftItem) && Objects.isNull(rightItem)) return;
        if (!pickUpSchedule.containsKey(player)) return;
        ItemStack currentItem = pickUpSchedule.remove(player);
        if (rawSlot == 1) {
            AnvilRepairEnchantScheduler.newScheduleData(player, leftItem, currentItem);
            LOGGER.info(String.format("Anvil now has %s in the left slot and %s in the right slot.", leftItem, currentItem));
        } else {
            AnvilRepairEnchantScheduler.newScheduleData(player, currentItem, rightItem);
            LOGGER.info(String.format("Anvil now has %s in the left slot and %s in the right slot.", currentItem, rightItem));
        }
        LOGGER.info(String.format("Anvil actually has %s in the left slot and %s in the right slot.", leftItem, rightItem));
    }

    public static String getName(ItemStack itemStack) {
        final String[] split = itemStack.getType().name().toLowerCase().split("_");
        return WordUtils.capitalize(StringUtils.join(split, " "));
    }

    private static void insertItemInAnvilThirdSlotFromShiftClick(InventoryClickEvent e, AnvilInventory anvil, Player player) {
        if (Objects.isNull(anvil.getItem(0)) && Objects.isNull(anvil.getItem(1))) {
            AnvilRepairEnchantScheduler.removeScheduleData(player);
            return;
        }
        ItemStack leftItem;
        ItemStack rightItem;
        if (Objects.nonNull(anvil.getItem(0)) && Objects.nonNull(anvil.getItem(1))) {
            leftItem = anvil.getItem(0);
            rightItem = anvil.getItem(1);
        } else {
            if (Objects.isNull(anvil.getItem(0))) {
                leftItem = e.getCurrentItem();
                rightItem = anvil.getItem(1);
            } else {
                leftItem = anvil.getItem(0);
                rightItem = e.getCurrentItem();
            }
        }
        AnvilRepairEnchantScheduler.newScheduleData(player, leftItem, rightItem);
    }

    private static class AnvilRepairEnchantScheduler {
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
            enchantments.keySet().forEach(enchantment -> EnchantUtils.enchantItemWithoutLore(item, enchantments.get(enchantment), enchantment));
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(this.displayName);
            List<String> lores = enchantments.keySet().stream().filter(e -> e instanceof EnchantWrapper).map(e -> EnchantUtils.getEnchantmentLoreName(e, enchantments.get(e))).collect(Collectors.toList());
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
            enchantments.keySet().forEach(enchantment -> EnchantUtils.enchantItemWithoutLore(item, enchantments.get(enchantment), enchantment));
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(this.displayName);
            List<String> lores = enchantments.keySet().stream().filter(e -> e instanceof EnchantWrapper).map(e -> EnchantUtils.getEnchantmentLoreName(e, enchantments.get(e))).collect(Collectors.toList());
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
}
