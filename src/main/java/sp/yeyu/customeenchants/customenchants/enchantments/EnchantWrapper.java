package sp.yeyu.customeenchants.customenchants.enchantments;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import sp.yeyu.customeenchants.customenchants.utils.EnchantUtils;
import sp.yeyu.customeenchants.customenchants.utils.RomanNumeral;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class EnchantWrapper extends Enchantment {

    final String name;
    final int registeredId;
    final Rarity rarity;
    protected String description;

    public EnchantWrapper(int id, String name, Rarity rarity) {
        super(id);
        this.name = name;
        this.registeredId = id;
        this.rarity = rarity;
    }

    public String getDescription() {
        return String.format("[%s%s] - %s",
                rarity.colorCode + "" + ChatColor.BOLD + EnchantUtils.convertToDisplayName(this),
                (getMaxLevel() > 1 ? String.format(" I - %s", RomanNumeral.toRoman(getMaxLevel())) : "") + ChatColor.WHITE,
                description);
    }

    public Rarity getRarity() {
        return rarity;
    }

    public int getRegisteredId() {
        return this.registeredId;
    }

    public String getVariableName() {
        final String[] split = name.toLowerCase().split("_");
        return IntStream.range(0, split.length).mapToObj(i -> {
            if (i == 0) return split[i];
            else return StringUtils.capitalize(split[i]);
        }).collect(Collectors.joining());
    }

    @Override
    public String getName() {
        return name;
    }

    public abstract void applyEffect(Player player);

    public abstract boolean hasEffect();

    public enum Rarity {
        COMMON(ChatColor.GRAY),
        RARE(ChatColor.YELLOW),
        HEROIC(ChatColor.LIGHT_PURPLE);

        public final ChatColor colorCode;

        Rarity(ChatColor colCode) {
            this.colorCode = colCode;
        }

        @Override
        public String toString() {
            return name();
        }
    }

}
