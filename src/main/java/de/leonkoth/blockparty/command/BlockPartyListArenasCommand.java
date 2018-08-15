package de.leonkoth.blockparty.command;

import de.leonkoth.blockparty.BlockParty;
import de.leonkoth.blockparty.arena.Arena;
import de.leonkoth.blockparty.locale.Locale;
import de.leonkoth.blockparty.manager.MessageManager;
import org.bukkit.command.CommandSender;

public class BlockPartyListArenasCommand extends SubCommand {

    public BlockPartyListArenasCommand(BlockParty blockParty) {
        super(false, 1, "listarenas", "blockparty.admin", blockParty);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {

        if (!super.onCommand(sender, args)) {
            return false;
        }

        sender.sendMessage("§8§m----------§e All arenas §8§m----------");

        if (blockParty.getArenas().isEmpty()) {
            MessageManager.messageWithoutPrefix(sender, Locale.NO_ARENAS);
        }

        for (Arena arena : blockParty.getArenas()) {
            sender.sendMessage("§8 • §7" + arena.getName() + ": " + arena.getArenaState().name());
        }

        sender.sendMessage("§8§m----------------------------");

        return true;
    }

}
