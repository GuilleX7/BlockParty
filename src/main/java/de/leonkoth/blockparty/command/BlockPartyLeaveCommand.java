package de.leonkoth.blockparty.command;

import de.leonkoth.blockparty.BlockParty;
import de.leonkoth.blockparty.arena.Arena;
import de.leonkoth.blockparty.locale.Locale;
import de.leonkoth.blockparty.manager.MessageManager;
import de.leonkoth.blockparty.player.PlayerInfo;
import de.leonkoth.blockparty.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlockPartyLeaveCommand extends SubCommand {

    public BlockPartyLeaveCommand(BlockParty blockParty) {
        super(true, 1, "leave", "blockparty.user", blockParty);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {

        if (!super.onCommand(sender, args)) {
            return false;
        }

        Player player = (Player) sender;
        PlayerInfo playerInfo = PlayerInfo.getFromPlayer(player);

        if (playerInfo == null || playerInfo.getCurrentArena() == null || playerInfo.getPlayerState() == PlayerState.DEFAULT) {
            MessageManager.message(sender, Locale.LEAVE_ERROR_NOT_IN_AN_ARENA);
            return false;
        }

        Arena arena = Arena.getByName(playerInfo.getCurrentArena());

        if (!arena.removePlayer(player)) {
            Bukkit.getLogger().severe("[BlockParty] " + player.getName() + " couldn't leave arena " + arena.getName());
            return false;
        }

        return true;

    }
}