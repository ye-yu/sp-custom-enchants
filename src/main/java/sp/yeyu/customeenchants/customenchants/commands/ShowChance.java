package sp.yeyu.customeenchants.customenchants.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.util.Objects;

public class ShowChance implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }
        final String enchantmentName = StringUtils.join(args, " ");
        EnchantWrapper enchantment = EnchantUtils.getEnchantmentByDisplayName(enchantmentName);
        if (Objects.isNull(enchantment)) {
            sender.sendMessage("Cannot find enchantment of " + enchantmentName);
            return false;
        }

        String enchantId = EnchantUtils.getChanceVariableName(enchantment);
        final double chance = EnchantPlus.getPluginData().getPlayerData((Player) sender).getDoubleOrDefault(enchantId, 0D);
        sender.sendMessage(String.format("You have %.02f%% chance of getting %s from the enchantment table.",
                chance,
                EnchantUtils.convertToDisplayName(enchantment)));
        return true;
    }
}
