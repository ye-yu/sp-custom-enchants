package sp.yeyu.customeenchants.customenchants.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.managers.EnchantManager;

import java.util.Objects;

public class Enchants implements CommandExecutor {
    private static final String HEADER = "List of enchantments";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return listEnchantsBasedOnItem(sender, args);
    }

    private boolean listEnchantsBasedOnItem(CommandSender sender, String... args) {
        String itemName = StringUtils.join(args, " ");
        ItemStack itemStack = null;
        if (args.length > 0) {
            final Material material = Material.matchMaterial(itemName);
            if (Objects.isNull(material)) {
                sender.sendMessage("Unable to find item " + itemName);
                return false;
            }
            itemStack = new ItemStack(material);
        }

        StringBuilder message = new StringBuilder();
        message.append("\n")
                .append("================================").append("\n")
                .append("Enchant Rarity:").append("\n");
        for (EnchantWrapper.Rarity value : EnchantWrapper.Rarity.values()) {
            //noinspection StringConcatenationInsideStringBufferAppend
            message.append("   > ")
                    .append(value.colorCode)
                    .append("" + ChatColor.BOLD)
                    .append(value.toString())
                    .append(ChatColor.WHITE)
                    .append("\n");
        }
        message.append("================================\n");

        message.append(HEADER).append(Objects.nonNull(itemStack) ? " for " + itemName : "").append(":\n");
        for (EnchantManager.EnchantEnum ench : EnchantManager.EnchantEnum.values()) {
            final EnchantWrapper enchantment = ench.getEnchantment();
            if (Objects.isNull(itemStack) || enchantment.canEnchantItem(itemStack))
                message.append(enchantment.getDescription()).append("\n");
        }
        sender.sendMessage(message.toString());
        return true;
    }
}
