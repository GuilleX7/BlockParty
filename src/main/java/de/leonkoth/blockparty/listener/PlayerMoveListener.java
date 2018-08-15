package de.leonkoth.blockparty.listener;

import de.leonkoth.blockparty.BlockParty;
import de.leonkoth.blockparty.arena.Arena;
import de.leonkoth.blockparty.player.PlayerInfo;
import de.leonkoth.blockparty.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Created by Leon on 15.03.2018.
 * Project Blockparty2
 * © 2016 - Leon Koth
 */
public class PlayerMoveListener implements Listener {

    private BlockParty blockParty;

    public PlayerMoveListener(BlockParty blockParty) {
        this.blockParty = blockParty;

        Bukkit.getPluginManager().registerEvents(this, blockParty.getPlugin());
    }

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PlayerInfo playerInfo = PlayerInfo.getFromPlayer(player);

        if (playerInfo == null)
            return;

        if (playerInfo.getPlayerState() == PlayerState.INGAME) {
            Arena arena = Arena.getByName(playerInfo.getCurrentArena());

            if (player.getLocation().getBlockY() <= arena.getFloor().getBounds()[0].getBlockY() - arena.getDistanceToOutArea()) {
                arena.eliminate(playerInfo);
            }
        }

    }

}
