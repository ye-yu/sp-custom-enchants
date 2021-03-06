package sp.yeyu.customeenchants.customenchants.commands;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import sp.yeyu.customeenchants.customenchants.EnchantPlus;
import sp.yeyu.customeenchants.customenchants.enchantments.EnchantWrapper;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;

import java.util.Arrays;
import java.util.Objects;

public class BuildChance implements CommandExecutor {
    private static final Logger LOGGER = LogManager.getLogger(BuildChance.class);
    private final boolean devMode;

    public BuildChance(boolean b) {
        devMode = b;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!devMode) {
            final EnchantPlus plugin = JavaPlugin.getPlugin(EnchantPlus.class);
            sender.sendMessage(String.format("Development mode is off. Toggle dev mode as non-zero value in %s/%s, and then restart the server", plugin.getName(), EnchantPlus.DEV_DATA_FILENAME));
            return true;
        }

        double increaseChance;
        final double defaultChance = 5;
        String enchantmentName;
        try {
            increaseChance = Double.parseDouble(args[args.length - 1]);
            enchantmentName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length - 1), " ");
        } catch (NumberFormatException e) {
            LOGGER.info(String.format("Using default chance increase: %.02f%%", defaultChance));
            increaseChance = defaultChance;
            enchantmentName = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");
        }

        final EnchantWrapper enchantment = EnchantUtils.getEnchantmentByDisplayName(enchantmentName);
        if (Objects.isNull(enchantment)) {
            sender.sendMessage("Cannot find enchantment of " + enchantmentName);
            return false;
        }

        final Player player = sender.getServer().getPlayer(args[0]);
        if (Objects.isNull(player)) {
            sender.sendMessage("Cannot find player " + args[0]);
            return false;
        }

        final double newChance = EnchantUtils.increaseEnchantmentChanceForPlayer(enchantment, player, increaseChance);
        sender.sendMessage(String.format("Now %s has %.02f%% chance of getting %s", player.getDisplayName(), newChance, enchantmentName));
        return true;
    }
}
