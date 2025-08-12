package me.ylabui.yBorder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorderTabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof Player)) {
            return suggestions;
        }

        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("set", "list", "remove", "update", "reload", "timed"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("timed")) {
            suggestions.addAll(Arrays.asList("remove"));
        }
        return suggestions;
    }
}
