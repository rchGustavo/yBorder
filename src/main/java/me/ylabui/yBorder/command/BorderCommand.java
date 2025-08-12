package me.ylabui.yBorder.command;

import me.ylabui.yBorder.Main;
import org.bukkit.Bukkit;
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
            p.sendMessage("§b - /border remove");
            p.sendMessage("§b - /border update [new size]");
            p.sendMessage("§b - /border timed remove [time]");
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
            case "remove":
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
            case "timed":
                if (args.length < 3) {
                    p.sendMessage(plugin.getString("messages.invalid_usage"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("remove")) {
                    if (plugin.getConfig().contains("barrier." + world.getName() + ".size")) {
                        int timeInSeconds;
                        try {
                            timeInSeconds = parseTimeToSeconds(args[2]);
                        } catch (IllegalArgumentException e) {
                            p.sendMessage(e.getMessage());
                            return true;
                        }
                        String textConvert = formatSecondsToText(timeInSeconds);
                        Map<String, String> placeholders = new HashMap<>();
                        placeholders.put("time", textConvert);
                        p.sendMessage(plugin.getString("messages.border_removed_timed", placeholders));

                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            if (plugin.getConfig().contains("barrier." + world.getName() + ".size")) {
                                plugin.getConfig().set("barrier." + world.getName(), null);
                                plugin.saveConfig();
                                border.reset();
                                String title = plugin.getString("messages.border_removed_title");
                                String subtitle = plugin.getString("messages.border_removed_subtitle");
                                for (Player jogador : world.getPlayers()) {
                                    jogador.sendTitle( title, subtitle, 10, 70, 20);
                                    p.sendMessage(plugin.getString("messages.border_removed"));
                                }
                            }
                        }, 20L * timeInSeconds);
                    } else {
                        p.sendMessage(plugin.getString("messages.world_not_have_border"));
                    }
                }
                break;
            default:
                p.sendMessage(plugin.getString("messages.invalid_usage"));
                break;
        }
        return true;
    }

    public int parseTimeToSeconds(String input) throws IllegalArgumentException {
        if (input == null || input.length() < 2) {
            throw new IllegalArgumentException(plugin.getString("messages.invalid_format_time"));
        }

        String unit = input.substring(input.length() - 1).toLowerCase();
        String numberPart = input.substring(0, input.length() - 1);

        int value;
        try {
            value = Integer.parseInt(numberPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(plugin.getString("messages.invalid_number_time"));
        }

        switch (unit) {
            case "s":
                return value;
            case "m":
                return value * 60;
            case "h":
                return value * 3600;
            case "d":
                return value * 86400;
            default:
                throw new IllegalArgumentException(plugin.getString("messages.invalid_unit_time"));
        }
    }

    public String formatSecondsToText(int totalSeconds) {
        if (totalSeconds < 0) return plugin.getString("messages.invalid_time");

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        int count = 0;

        if (days > 0) {
            String dtxt = plugin.getString("messages.day_unit");
            sb.append(days).append(days == 1 ? " " + dtxt : " " + dtxt + "s");
            count++;
        }
        if (hours > 0) {
            String htxt = plugin.getString("messages.hour_unit");
            if (count > 0) sb.append(count == 1 && minutes == 0 && seconds == 0 ? " e " : ", ");
            sb.append(hours).append(hours == 1 ? " " + htxt : " " + htxt + "s");
            count++;
        }
        if (minutes > 0) {
            String mtxt = plugin.getString("messages.minute_unit");
            if (count > 0) sb.append(count == 1 && seconds == 0 ? " e " : ", ");
        sb.append(minutes).append(minutes == 1 ? " " + mtxt : " " + mtxt + "s");
            count++;
        }
        if (seconds > 0 || count == 0) {
            if (count > 0) sb.append(" e ");
            String stxt = plugin.getString("messages.second_unit");
            sb.append(seconds).append(seconds == 1 ? " " + stxt : " " + stxt + "s");
        }
        return sb.toString();
    }
}
