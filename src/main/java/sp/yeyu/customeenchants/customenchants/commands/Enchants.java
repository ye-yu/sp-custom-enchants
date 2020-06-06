package sp.yeyu.customeenchants.customenchants.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import sp.yeyu.customeenchants.customenchants.CustomEnchants;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;

import java.util.Objects;

public class Enchants implements CommandExecutor {
    private static final String HEADER = "List of enchantments";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0)
            return listEnchantsBasedOnItem(sender, args);
        return listEnchants(sender);
    }

    private boolean listEnchants(CommandSender sender) {
        StringBuilder message = new StringBuilder(HEADER + ":\n");
        for (CustomEnchants.EnchantEnum ench : CustomEnchants.EnchantEnum.values()) {
            message.append(ench.getEnchantment().getDescription()).append("\n");
        }
        sender.sendMessage(message.toString());
        return true;
    }

    private boolean listEnchantsBasedOnItem(CommandSender sender, String[] args) {
        String itemName = StringUtils.join(args, " ");
        final Material material = Material.matchMaterial(itemName);
        if (Objects.isNull(material)) {
            sender.sendMessage("Unable to find item " + itemName);
            return false;
        }
        final ItemStack itemStack = new ItemStack(material);
        StringBuilder message = new StringBuilder(HEADER + " for " + itemName + ":\n");
        for (CustomEnchants.EnchantEnum ench : CustomEnchants.EnchantEnum.values()) {
            final EnchantWrapper enchantment = ench.getEnchantment();
            if (enchantment.canEnchantItem(itemStack))
                message.append(enchantment.getDescription()).append("\n");
        }
        sender.sendMessage(message.toString());
        return true;
    }
}
