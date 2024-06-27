package net.countercraft.movecraft.movecraftoverheat.listener;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.combat.features.tracking.events.CraftFireWeaponEvent;
import net.countercraft.movecraft.combat.features.tracking.types.Fireball;
import net.countercraft.movecraft.combat.features.tracking.types.TNTCannon;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.craft.CraftManager;
import net.countercraft.movecraft.movecraftoverheat.Keys;
import net.countercraft.movecraft.movecraftoverheat.MovecraftOverheat;
import net.countercraft.movecraft.movecraftoverheat.config.Settings;
import net.countercraft.movecraft.movecraftoverheat.tracking.CraftHeat;
import net.countercraft.movecraft.util.MathUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WeaponFireListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWeaponFire (CraftFireWeaponEvent event) {
        Craft craft = event.getCraft();
        CraftHeat craftHeat = MovecraftOverheat.getInstance().getHeatManager().getHeat(craft);
        if (craftHeat == null) {
            return;
        }
        if (event.getWeaponType() instanceof Fireball) {
            double multiplier;
            try {
                multiplier = craft.getType().getDoubleProperty(Keys.FIREBALL_HEAT_MULTIPLIER);
            } catch (IllegalStateException e) {
                multiplier = 1.0;
            }
            craftHeat.addHeat(Settings.HeatPerFireball *multiplier);
        } else if (event.getWeaponType() instanceof TNTCannon) {
            double multiplier;
            try {
                multiplier = craft.getType().getDoubleProperty(Keys.TNT_HEAT_MULTIPLIER);
            } catch (IllegalStateException e) {
                multiplier = 1.0;
            }
            craftHeat.addHeat(Settings.HeatPerTNT*multiplier);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode (EntityExplodeEvent event) {
        // Don't run this check if there's no per shot heat cost
        if (Settings.HeatPerGunShot <= 0) return;
        // Only detect explosions in ballistic water or lava for the purposes of catching cannon shots
        Location location = event.getLocation();
        if (!(location.getBlock().isLiquid())) return;
        Craft craft = MathUtils.fastNearestCraftToLoc(CraftManager.getInstance().getCrafts(), location);
        // Check if the location is within the craft's hitbox

        if (!craft.getHitBox().contains(new MovecraftLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ())))
            return;
        CraftHeat craftHeat = MovecraftOverheat.getInstance().getHeatManager().getHeat(craft);
        if (craftHeat == null) {
            return;
        }
        if (!craftHeat.hasFiredThisTick()) {
            craftHeat.setFiredThisTick(true);
            craftHeat.addHeat(Settings.HeatPerGunShot);
        }
    }
}
