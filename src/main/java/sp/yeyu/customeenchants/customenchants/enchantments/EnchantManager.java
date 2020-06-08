package sp.yeyu.customeenchants.customenchants.enchantments;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.utils.storage.DataStorageInstance;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager implements Listener {

    private static final Logger LOGGER = LogManager.getLogger(EnchantManager.class);
    private static final EnchantManager MANAGER = new EnchantManager(getRefreshRateFromData());
    private static final HashMap<Player, ArrayList<ItemStack>> enchantSchedule = Maps.newHashMap();
    private static final HashMap<Player, ItemStack> pickUpSchedule = Maps.newHashMap();
    private static final String ACTUAL_COST_PREFIX = "Actual cost: ";

    private final int refreshRate;
    private final int effectDuration;

    private EnchantManager(HashMap<String, Integer> attributes) {
        this.refreshRate = attributes.get(Attributes.REFRESH_RATE.attrName);
        this.effectDuration = attributes.get(Attributes.EFFECT_DURATION.attrName);
    }

    private static HashMap<String, Integer> getRefreshRateFromData() {
        final DataStorageInstance data = EnchantPlus.getChanceData().getData(EnchantPlus.DEV_DATA_FILENAME);
        if (!data.hasAttr(Attributes.REFRESH_RATE.attrName))
            data.putAttr(Attributes.REFRESH_RATE.attrName, Attributes.REFRESH_RATE.defaultValue);

        if (!data.hasAttr(Attributes.EFFECT_DURATION.attrName))
            data.putAttr(Attributes.EFFECT_DURATION.attrName, Attributes.EFFECT_DURATION.defaultValue);

        HashMap<String, Integer> attributes = Maps.newHashMap();
        Integer refreshRate = data.getIntegerOrDefault(Attributes.REFRESH_RATE.attrName, Attributes.REFRESH_RATE.defaultValue);
        if (refreshRate < 1) {
            refreshRate = Attributes.REFRESH_RATE.defaultValue;
            data.putAttr(Attributes.REFRESH_RATE.attrName, Attributes.REFRESH_RATE.defaultValue);
            LOGGER.error(String.format("Refresh rate of less than 1 is too quick or unrealistic! Setting refresh rate to %d ticks.", refreshRate));
        }

        Integer effectDuration = data.getIntegerOrDefault(Attributes.EFFECT_DURATION.attrName, Attributes.EFFECT_DURATION.defaultValue);
        if (effectDuration < 1) {
            effectDuration = Attributes.EFFECT_DURATION.defaultValue;
            data.putAttr(Attributes.EFFECT_DURATION.attrName, Attributes.EFFECT_DURATION.defaultValue);
            LOGGER.error(String.format("Effect duration cannot be zero ticks or less. Setting effect duration to %d ticks.", effectDuration));
        }

        attributes.put(Attributes.REFRESH_RATE.attrName, refreshRate);
        attributes.put(Attributes.EFFECT_DURATION.attrName, effectDuration);
        return attributes;
    }

    public static EnchantManager getEnchantManager() {
        return MANAGER;
    }

    public static void applyEnchants() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            applyEnchantsOnPlayer(player);
        }
    }

    public static void applyEnchantsOnPlayer(Player player) {
        for (ItemStack item : getEquipments(player)) {
            for (Enchantment ench : item.getEnchantments().keySet()) {
                if (ench instanceof EnchantWrapper) {
                    EnchantWrapper customEnchant = (EnchantWrapper) ench;
                    if (customEnchant.hasEffect()) {
                        customEnchant.applyEffect(player);
                    }
                }
            }
        }
    }

    public static List<ItemStack> getEquipments(Player player) {
        final EntityEquipment equipment = player.getEquipment();
        return Stream.of(equipment.getItemInHand(), equipment.getHelmet(), equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots()).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static int calculateTotalEffectDuration() {
        return getEnchantManager().getRefreshRate() + getEnchantManager().getEffectDuration();
    }

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
            whenClickingResultingItem(e, anvil, player);
        } else {
            whenClickingAnvilSlot(e, anvil, player);
        }
    }

    private static void whenClickingResultingItem(InventoryClickEvent e, AnvilInventory anvil, Player player) {
        final ArrayList<ItemStack> enchantSlots = enchantSchedule.get(player);
        if (Objects.isNull(enchantSlots)) return;
        if (enchantSlots.size() == 2) {
            // display the third slot yet
            ItemStack leftItem = enchantSlots.get(0);
            ItemStack rightItem = enchantSlots.get(1);
            if (Objects.nonNull(anvil.getItem(2))) {
                final ItemStack repairedItem = scheduleRepairItem(leftItem, rightItem, anvil.getItem(2));
                anvil.setItem(2, repairedItem);
                enchantSlots.add(2, repairedItem);
                enchantSlots.add(3, e.getCurrentItem());
                LOGGER.info(String.format("Item in the first slot is being repaired: %s", anvil.getItem(2)));
            } else if (rightItem.getType().equals(Material.ENCHANTED_BOOK)) {
                final ItemStack enchantedItem = scheduleEnchantItemFromBook(leftItem, rightItem);
                anvil.setItem(2, enchantedItem);
                enchantSlots.add(2, enchantedItem);
                LOGGER.info("Item is being enchanted.");
            }
        } else if (enchantSlots.size() > 2) {
            // prepare to return item
            final ItemStack item = enchantSlots.get(0);
            final ItemStack rightItem = enchantSlots.get(1);
            boolean hasFinishedRepaired = false;
            ItemStack resultingItem = null;
            if (enchantSlots.size() == 3) {
                // return enchanted item
                LOGGER.info("Giving player enchanted item.");
                resultingItem = enchantItemFromAnvil(item, rightItem);
                hasFinishedRepaired = true;
            } else if (enchantSlots.size() == 4) {
                // return repaired item
                final ItemStack repairItem = enchantSlots.get(2);
                int cost = getCostFromMetaBook(enchantSlots.get(2));
                if (player.getLevel() >= cost) {
                    resultingItem = repairItemFromAnvil(item, repairItem);
                }
                hasFinishedRepaired = true;
            }

            if (hasFinishedRepaired) {
                anvil.setItem(0, new ItemStack(Material.AIR));
                anvil.setItem(1, new ItemStack(Material.AIR));
                anvil.setItem(2, new ItemStack(Material.AIR));
                enchantSchedule.remove(player);
                player.setItemOnCursor(resultingItem);
                player.updateInventory();
            }
        }
    }

    private static int getCostFromMetaBook(ItemStack itemStack) {
        int cost = 0;
        final ItemMeta itemMeta = itemStack.getItemMeta();
        final List<String> lore = itemMeta.getLore();
        final String s = lore.get(lore.size() - 1);
        cost += Integer.parseInt(s.substring(ACTUAL_COST_PREFIX.length()));
        return cost;
    }

    private static int getCustomEnchantmentCost(ItemStack item) {
        int cost = 0;
        final int PER_ENCH_COST = 5;
        for (Enchantment ench: item.getEnchantments().keySet()) {
            if (ench instanceof EnchantWrapper) {
                int perLevelCost = (int)Math.ceil((double)PER_ENCH_COST / ench.getMaxLevel());
                cost += perLevelCost * item.getEnchantmentLevel(ench);
            }
        }
        return cost;
    }

    private static int getRepairCost(ItemStack repairItem) {
        if (!(repairItem.getItemMeta() instanceof Repairable)) return 0;
        final Repairable itemMeta = (Repairable) repairItem.getItemMeta();
        return itemMeta.getRepairCost();
    }

    private static void whenClickingAnvilSlot(InventoryClickEvent e, AnvilInventory anvil, Player player) {
        enchantSchedule.remove(player); // always reset when user perform inventory change
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

    private static ItemStack repairItemFromAnvil(ItemStack targetItem, ItemStack repairItem) {
        ItemStack newItem = new ItemStack(repairItem);
        final ItemMeta itemMeta = newItem.getItemMeta();
        itemMeta.setDisplayName("Repaired");
        newItem.setItemMeta(itemMeta);
        return newItem;
    }

    private static ItemStack enchantItemFromAnvil(ItemStack targetItem, ItemStack enchantedBook) {
        ItemStack newItem = new ItemStack(targetItem);
        final ItemMeta itemMeta = newItem.getItemMeta();
        itemMeta.setDisplayName("Enchanted");
        newItem.setItemMeta(itemMeta);
        return newItem;
    }

    private static ItemStack scheduleEnchantItemFromBook(ItemStack leftItem, ItemStack rightItem) {
        // method is invoked only when the enchantment book only contains custom enchants
        final ItemStack itemStack = new ItemStack(leftItem);
        final ItemMeta meta = itemStack.getItemMeta();

        // add enchantment tag to the meta item to be appear in the third slot
        EnchantWrapper.enchantItem(itemStack, 1, EnchantPlus.EnchantEnum.ANVIL_TAG.getEnchantment());

        // add lore
        ArrayList<String> lores = Lists.newArrayList();

        // merge enchantment
        for (Enchantment enchantment : rightItem.getEnchantments().keySet()) {
            if (itemStack.getEnchantments().keySet().stream().noneMatch(e -> e.conflictsWith(enchantment))) {
                itemStack.addUnsafeEnchantment(enchantment, rightItem.getEnchantmentLevel(enchantment));
            }
        }

        // calculate repair cost
        int cost = getCustomEnchantmentCost(itemStack);

        // add enchantment lore
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            lores.add(ChatColor.GRAY + EnchantWrapper.getEnchantmentLoreName(enchantment, itemStack.getEnchantmentLevel(enchantment)));
        }

        // add actual enchantment cost
        lores.add(ChatColor.YELLOW + ACTUAL_COST_PREFIX + cost);
        meta.setDisplayName(ChatColor.AQUA + (meta.hasDisplayName() ? (ChatColor.ITALIC + meta.getDisplayName()) : getName(itemStack)));
        meta.setLore(lores);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private static ItemStack scheduleRepairItem(ItemStack leftItem, ItemStack rightItem, ItemStack resultingItem) {
        final ItemStack itemStack = new ItemStack(leftItem);
        final ItemMeta meta = itemStack.getItemMeta();

        // log enchant list
        for (Enchantment ench : leftItem.getEnchantments().keySet()) {
            LOGGER.info("Left item has the enchantment: " + ench.getName());
        }

        for (Enchantment ench : rightItem.getEnchantments().keySet()) {
            LOGGER.info("right item has the enchantment: " + ench.getName());
        }


        // add enchantment tag to the meta item to be appear in the third slot
        EnchantWrapper.enchantItem(itemStack, 1, EnchantPlus.EnchantEnum.ANVIL_TAG.getEnchantment());

        // get custom enchantments
        HashMap<EnchantWrapper, Integer> customEnchantments = mergeCustomEnchantments(leftItem, rightItem);

        // prepare lore
        ArrayList<String> lores = Lists.newArrayList();

        // re-add vanilla enchantment lore
        for (Enchantment enchantment : resultingItem.getEnchantments().keySet()) {
            lores.add(ChatColor.GRAY + EnchantWrapper.getEnchantmentLoreName(enchantment, resultingItem.getEnchantmentLevel(enchantment)));
        }

        // add custom enchantment lore
        for (EnchantWrapper customEnch : customEnchantments.keySet()) {
            lores.add(ChatColor.GRAY + EnchantWrapper.getEnchantmentLoreName(customEnch, customEnchantments.get(customEnch)));
            resultingItem.addUnsafeEnchantment(customEnch, customEnchantments.get(customEnch));
        }

        // calculate repair cost
        int cost = getRepairCost(resultingItem) + getCustomEnchantmentCost(resultingItem);

        // put in the actual cost in the lore
        lores.add(ChatColor.YELLOW + ACTUAL_COST_PREFIX + cost);
        meta.setDisplayName(ChatColor.AQUA + (meta.hasDisplayName() ? (ChatColor.ITALIC + meta.getDisplayName()) : getName(itemStack)));
        meta.setLore(lores);
        itemStack.setItemMeta(meta);
        return itemStack;
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
                } else if (leftEnchantments.keySet().stream().noneMatch(e -> e.conflictsWith(rightEnch))) { // only all nonc conflicting enchants
                    leftEnchantments.put(rightEnch, rightEnchantments.get(rightEnch));
                }
            } else {
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
        }
        return customEnchants;
    }

    private static void insertItemInAnvilThirdSlot(ItemStack leftItem, ItemStack rightItem, int rawSlot, Player player) {
        if (Objects.isNull(leftItem) && Objects.isNull(rightItem)) return;
        if (!pickUpSchedule.containsKey(player)) return;
        ItemStack currentItem = pickUpSchedule.remove(player);
        if (rawSlot == 1) {
            enchantSchedule.put(player, Lists.newArrayList(leftItem, currentItem));
            LOGGER.info(String.format("Anvil now has %s in the left slot and %s in the right slot.", leftItem, currentItem));
        } else {
            enchantSchedule.put(player, Lists.newArrayList(currentItem, rightItem));
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
            enchantSchedule.remove(player);
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
        enchantSchedule.put(player, Lists.newArrayList(leftItem, rightItem));
    }

    public int getEffectDuration() {
        return effectDuration;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent itemEvent) {
        if (itemEvent.getEnchanter().getLevel() < 30) return;
        if (itemEvent.getEnchantsToAdd().size() > 4) return;
        int count = 0;
        final ItemStack item = itemEvent.getItem();
        final Random random = new Random(Instant.now().toEpochMilli());
        for (EnchantPlus.EnchantEnum ench : EnchantPlus.EnchantEnum.values()) {
            final EnchantWrapper enchantment = ench.getEnchantment();
            if (enchantment.canEnchantItem(item)) {
                final Double chance = EnchantPlus.getChanceData().getPlayerData(itemEvent.getEnchanter()).getDoubleOrDefault(EnchantWrapper.getChanceVariableName(enchantment), 0D);
                final double roll = random.nextDouble() * 100 + 1;
                if (chance > roll) {
                    int level = random.nextInt(enchantment.getMaxLevel()) + 1;
                    EnchantWrapper.enchantItem(item, level, enchantment);
                    EnchantWrapper.reduceEnchantmentChanceForPlayer(enchantment, itemEvent.getEnchanter(), chance * 0.2);
                    count++;
                }

                if (count + itemEvent.getEnchantsToAdd().size() > 4) {
                    return;
                }
            }
        }
    }

    private enum Attributes {
        REFRESH_RATE("refreshTickRate", 5),
        EFFECT_DURATION("effectDurationTick", 60);

        public final String attrName;
        public final int defaultValue;

        Attributes(String attrName, int value) {
            this.attrName = attrName;
            this.defaultValue = value;
        }
    }
}
