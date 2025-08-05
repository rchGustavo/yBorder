package me.ylabui.yBorder.command;

import me.ylabui.yBorder.Main;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorderCommand implements CommandExecutor {

    private Main plugin;

    public BorderCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String perm = plugin.getCustomConfig().getString("admin-permission");
        if (!sender.isOp() && !sender.hasPermission(perm) || !(sender instanceof Player p)) {
            sender.sendMessage(plugin.getString("messages.not_permission"));
            return false;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            p.sendMessage("§b- Border Commands -");
            p.sendMessage("§b - /border help");
            p.sendMessage("§b - /border set [size]");
            p.sendMessage("§b - /border list");
            p.sendMessage("§b - /border cancel");
            p.sendMessage("§b - /border update [new size]");
            p.sendMessage("§b - /border reload");
            return true;
        }

        World world = p.getWorld();
        WorldBorder border = world.getWorldBorder();

        switch (args[0].toLowerCase()) {
            case "set":
                if (args.length == 2) {
                    if (plugin.getConfig().contains("barrier." + world.getName() + ".size")) {
                        p.sendMessage(plugin.getString("messages.world_have_border"));
                    } else {
                        int distance;
                        try {
                            distance = Integer.parseInt(args[1]);
                            if (distance <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            p.sendMessage(plugin.getString("messages.invalid_size"));
                            return true;
                        }

                        border.setCenter(p.getLocation().getX(), p.getLocation().getZ());
                        border.setSize(distance * 2);

                        plugin.getConfig().set("barrier." + world.getName() + ".size", distance);
                        plugin.saveConfig();

                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("size", String.valueOf(distance));

                        p.sendMessage(plugin.getString("messages.border_set", placeholders));
                    }
                }
                break;
            case "cancel":
                if (plugin.getConfig().contains("barrier." + world.getName() + ".size")) {
                    plugin.getConfig().set("barrier." + world.getName(), null);
                    plugin.saveConfig();
                    border.reset();
                    p.sendMessage(plugin.getString("messages.border_removed"));
                } else {
                    p.sendMessage(plugin.getString("messages.world_not_have_border"));
                }
                break;
            case "list":
                ConfigurationSection section = plugin.getConfig().getConfigurationSection("barrier");

                if (section == null || section.getKeys(false).isEmpty()) {
                    p.sendMessage(plugin.getString("messages.not_border_found"));
                    return true;
                }

                List<String> borders = new ArrayList<>();

                for (String worldName : section.getKeys(false)) {
                    int size = plugin.getConfig().getInt("barrier." + worldName + ".size");
                    String sizeStr = String.valueOf(size);
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("world", worldName);
                    placeholders.put("size", sizeStr);
                    String msg = plugin.getString("messages.border_list", placeholders);
                    borders.add(msg);
                }

                p.sendMessage(" ");
                for (String borderInfo : borders) {
                    p.sendMessage(borderInfo);
                }
                break;
            case "update":
                if (plugin.getConfig().contains("barrier." + world.getName() + ".size")) {
                    int distance;
                    try {
                        distance = Integer.parseInt(args[1]);
                        if (distance <= 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        p.sendMessage(plugin.getString("messages.invalid_size"));
                        return true;
                    }

                    border.setCenter(p.getLocation().getX(), p.getLocation().getZ());
                    border.setSize(distance * 2);

                    plugin.getConfig().set("barrier." + world.getName() + ".size", distance);
                    plugin.saveConfig();

                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("size", String.valueOf(distance));
                    p.sendMessage(plugin.getString("messages.border_update", placeholders));
                } else {
                    p.sendMessage(plugin.getString("messages.world_not_have_border"));
                }
                break;
            case "reload":
                plugin.reloadConfigFile();
                p.sendMessage(plugin.getString("messages.reload_sucess"));
                break;
            default:
                p.sendMessage(plugin.getString("messages.invalid_usage"));
                break;
        }
        return true;
    }
}