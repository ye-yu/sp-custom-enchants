package sp.yeyu.customeenchants.customenchants.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class EntityUtils {
    public static boolean isClimbing(Entity entity) {
        final Location location = entity.getLocation();
        final Material blockType = entity.getWorld().getBlockAt(location).getType();
        return Arrays.asList(Material.LADDER, Material.VINE).contains(blockType);
    }

    public static boolean isInFluid(Entity entity) {
        final Location location = entity.getLocation();
        return entity.getWorld().getBlockAt(location).isLiquid();
    }

    public static boolean isValidCritical(LivingEntity entity) {
        return entity.getFallDistance() > 0.0F && !entity.isOnGround() && !isClimbing(entity) && !isInFluid(entity) && !entity.hasPotionEffect(PotionEffectType.BLINDNESS) && entity.getVehicle() == null;
    }

}
