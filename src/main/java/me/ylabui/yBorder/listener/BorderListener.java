package me.ylabui.yBorder.listener;

import me.ylabui.yBorder.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class BorderListener implements Listener {

    private Main plugin;

    public BorderListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        if (to == null) return;

        World world = to.getWorld();
        WorldBorder border = world.getWorldBorder();

        double borderSize = border.getSize() / 2.0;
        Location center = border.getCenter().clone();

        double minX = center.getX() - borderSize;
        double maxX = center.getX() + borderSize;
        double minZ = center.getZ() - borderSize;
        double maxZ = center.getZ() + borderSize;

        double x = to.getX();
        double z = to.getZ();

        if (x < minX || x > maxX || z < minZ || z > maxZ) {

            double safeX = Math.max(minX + 1, Math.min(x, maxX - 1));
            double safeZ = Math.max(minZ + 1, Math.min(z, maxZ - 1));

            Location safeLocation = new Location(world, safeX, to.getY(), safeZ);
            safeLocation.setY(world.getHighestBlockYAt(safeLocation) + 1);

            player.teleport(safeLocation);

            player.sendMessage(plugin.getString("messages.border_enter"));
        }
    }

    @EventHandler
    public void onTntExplosion(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        World world = entity.getWorld();
        WorldBorder border = world.getWorldBorder();

        double borderSize = border.getSize() / 2.0;
        Location center = border.getCenter();

        double minX = center.getX() - borderSize;
        double maxX = center.getX() + borderSize - 1;
        double minZ = center.getZ() - borderSize;
        double maxZ = center.getZ() + borderSize - 1;

        event.blockList().removeIf(block -> {
            double x = block.getX();
            double z = block.getZ();
            return x < minX || x > maxX || z < minZ || z > maxZ;
        });
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent event) {
        Block fromBlock = event.getBlock();
        Block toBlock = event.getToBlock();

        if (!fromBlock.isLiquid()) return;

        World world = fromBlock.getWorld();
        WorldBorder border = world.getWorldBorder();

        double borderSize = border.getSize() / 2.0;
        Location center = border.getCenter();

        double minX = center.getX() - borderSize;
        double maxX = center.getX() + borderSize - 1;
        double minZ = center.getZ() - borderSize;
        double maxZ = center.getZ() + borderSize - 1;

        double x = toBlock.getX();
        double z = toBlock.getZ();

        if (x < minX || x > maxX || z < minZ || z > maxZ) {
            event.setCancelled(true);
        }
    }
}