package de.leonkoth.blockparty.command;

import de.leonkoth.blockparty.BlockParty;
import de.leonkoth.blockparty.arena.Arena;
import de.leonkoth.blockparty.locale.Locale;
import de.leonkoth.blockparty.manager.MessageManager;
import de.leonkoth.blockparty.schematic.FloorSchematic;
import de.leonkoth.blockparty.schematic.SchematicLoader;
import org.bukkit.command.CommandSender;

public class BlockPartyAddFloorCommand extends SubCommand {

    public BlockPartyAddFloorCommand(BlockParty blockParty) {
        super(false, 3, "addfloor", "blockparty.admin", blockParty);
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {

        if (!super.onCommand(sender, args)) {
            return false;
        }

        Arena arena;
        try {
            arena = Arena.getByName(args[1]);
        } catch (NullPointerException e) {
            MessageManager.message(sender, Locale.ARENA_DOESNT_EXIST, "%ARENA%", args[1]);
            return false;
        }

        if (!SchematicLoader.exists(args[2])) {
            MessageManager.message(sender, Locale.FLOOR_FILE_DOESNT_EXIST, "%FLOOR%", args[2] + ".schematic");
            return false;
        }

        FloorSchematic schematic = new FloorSchematic(name, arena.getFloor().getBounds());

        if (schematic.getSize().getX() != arena.getFloor().getWidth() || schematic.getSize().getZ() != arena.getFloor().getLength()) {
            MessageManager.message(sender, Locale.FLOOR_ISNT_CORRECT_SIZE);
            return false;
        }

        if (arena.addFloor(schematic)) {
            for (String message : Locale.FLOOR_ADDED_TO_ARENA) {
                MessageManager.message(sender, message, "%ARENA%", args[1], "%FLOOR%", args[2]);
            }
        }

        return true;

    }
}
