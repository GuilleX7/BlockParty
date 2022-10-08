package de.leonkoth.blockparty.phase;

import cloud.timo.TimoCloud.api.TimoCloudAPI;
import de.leonkoth.blockparty.BlockParty;
import de.leonkoth.blockparty.arena.Arena;
import de.leonkoth.blockparty.arena.GameState;
import de.leonkoth.blockparty.display.DisplayScoreboard;
import de.leonkoth.blockparty.event.*;
import de.leonkoth.blockparty.player.PlayerInfo;
import de.leonkoth.blockparty.player.PlayerState;
import de.leonkoth.blockparty.util.ColorBlock;
import de.leonkoth.blockparty.util.Util;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.util.ArrayList;

import static de.leonkoth.blockparty.locale.BlockPartyLocale.ACTIONBAR_DANCE;
import static de.leonkoth.blockparty.locale.BlockPartyLocale.ACTIONBAR_STOP;

/**
 * Created by Leon on 15.03.2018.
 * Project Blockparty2
 * © 2016 - Leon Koth
 */
public class GamePhase implements Runnable {

    private boolean firstStopEnter = true, firstDanceEnter = true, firstPrepareEnter = true, firstEnter = true;
    private double timeToSearch, timeReductionPerLevel, timeModifier, currentTimeToSearch, currentTime;
    private int levelAmount, currentLevel;

    @Getter
    private int stopTime = 4;

    private int preparingTime = 5;
    private BlockParty blockParty;
    private Arena arena;
    private ColorBlock colorBlock;

    @Deprecated
    public GamePhase(BlockParty blockParty, String name) {
        this(blockParty, Arena.getByName(name));
    }

    public GamePhase(BlockParty blockParty, Arena arena) {
        this.blockParty = blockParty;
        this.arena = arena;
        this.timeToSearch = arena.getTimeToSearch();
        this.timeReductionPerLevel = arena.getTimeReductionPerLevel();
        this.levelAmount = arena.getLevelAmount();
        this.timeModifier = arena.getTimeModifier();
        this.currentTimeToSearch = timeToSearch;
    }

    private int getActivePlayerAmount() {
        int amount = 0;
        for (PlayerInfo playerInfo : arena.getPlayersInArena()) {
            if (playerInfo.getPlayerState() == PlayerState.INGAME) {
                amount++;
            }
        }
        return amount;
    }

    public void initialize() {
        this.firstDanceEnter = true;
        this.firstStopEnter = true;
        this.firstPrepareEnter = true;
        this.firstEnter = true;

        GameStartEvent event = new GameStartEvent(arena);
        Bukkit.getPluginManager().callEvent(event);

        if (blockParty.isTimoCloud()) {
            TimoCloudAPI.getBukkitAPI().getThisServer().setState("INGAME");
        }
    }

    public void checkForWin() {
        if (this.getActivePlayerAmount() == 1) {
            this.finishGame();
        }
    }

    public void finishGame() {
        ArrayList<PlayerInfo> winners = new ArrayList<>();
        for (PlayerInfo playerInfo : arena.getPlayersInArena()) {
            if (playerInfo.getPlayerState() == PlayerState.INGAME) {
                winners.add(playerInfo);
            }
        }

        PlayerWinEvent event = new PlayerWinEvent(arena, winners, winners.get(0).asPlayer());
        Bukkit.getPluginManager().callEvent(event);

        if (blockParty.isTimoCloud()) {
            TimoCloudAPI.getBukkitAPI().getThisServer().setState("RESTART");
        }
    }

    @Override
    public void run() {

        if (arena.isEnableParticles()) {
            arena.getFloor().playParticles(5, 3, 10);
        }

        if (currentTime == 0) {
            if (firstEnter) {
                firstEnter = false;
            } else {
                FloorPlaceEvent event = new FloorPlaceEvent(arena, arena.getFloor());
                Bukkit.getPluginManager().callEvent(event);
            }
        }

        if (currentTime < preparingTime) {
            if (firstPrepareEnter) {
                RoundStartEvent event = new RoundStartEvent(arena);
                Bukkit.getPluginManager().callEvent(event);
                firstPrepareEnter = false;
            }
            Util.showActionBar(ACTIONBAR_DANCE.toString(), arena, true);
            currentTime += 0.1;
        } else {
            if (currentTime < (currentTimeToSearch + preparingTime)) {
                if (firstDanceEnter) {
                    arena.getFloor().pickBlock();

                    Block pickedBlock = arena.getFloor().getCurrentBlock();
                    colorBlock = ColorBlock.get(pickedBlock);
                    BlockPickEvent event = new BlockPickEvent(arena, pickedBlock, colorBlock);
                    Bukkit.getPluginManager().callEvent(event);

                    firstDanceEnter = false;
                }

                int seconds = (int) (currentTimeToSearch + preparingTime - currentTime + 1);

                RoundPrepareEvent event = new RoundPrepareEvent(seconds, arena, colorBlock);
                Bukkit.getPluginManager().callEvent(event);

                this.blockParty.getDisplayScoreboard().setScoreboard((int)(currentTimeToSearch + preparingTime - currentTime + 1), currentLevel + 1, arena);
                currentTime += 0.1;

            } else {
                if (currentTime < (currentTimeToSearch + preparingTime + stopTime)) {
                    if (firstStopEnter) {
                        /*if (arena.getSongManager().getVotedSong() != null) {
                            arena.getSongManager().getVotedSong().pause(this.blockParty, arena);
                        }*/

                        arena.getSongManager().pause(this.blockParty);
                        arena.setGameState(GameState.STOP);

                        arena.getFloor().removeBlocks();
                        firstStopEnter = false;
                    }

                    //STOP
                    Util.showActionBar(ACTIONBAR_STOP.toString(), arena, true);
                    currentTime += 0.1;
                } else {
                    if (currentLevel < levelAmount) {
                        currentLevel++;
                    } else {
                        this.finishGame();
                        return;
                    }

                    currentTime = 0;
                    currentTimeToSearch = currentTimeToSearch - (timeReductionPerLevel / (1 + timeModifier * currentLevel));
                    firstStopEnter = true;
                    firstDanceEnter = true;
                    firstPrepareEnter = true;

                    /*if (arena.getSongManager().getVotedSong() != null) {
                        arena.getSongManager().getVotedSong().continuePlay(blockParty, arena);
                    }*/

                    arena.getSongManager().continuePlay(this.blockParty);
                    arena.setGameState(GameState.PLAY);

                    arena.getFloor().clearInventories();
                }
            }
        }

    }

    public double getTimeRemaining() {
        return this.currentTimeToSearch + this.preparingTime - this.currentTime;
    }

    private void sendNetworkMessage(String message) {
        for (PlayerInfo playerInfo : arena.getPlayersInArena()) {
            if (playerInfo.getPlayerState() == PlayerState.INGAME) {
                if (this.blockParty.getWebServer() != null) {
                    this.blockParty.getWebServer().send(playerInfo.asPlayer().getAddress().getHostName(), arena.getName(), "song", message);
                }
            }
        }
    }

}
