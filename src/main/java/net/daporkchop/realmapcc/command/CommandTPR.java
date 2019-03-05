package net.daporkchop.realmapcc.command;

import net.daporkchop.lib.math.vector.d.Vec2d;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.capability.HeightsCapability;
import net.daporkchop.realmapcc.capability.ITerrainHeightHolder;
import net.daporkchop.realmapcc.util.CoordUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;

/**
 * @author DaPorkchop_
 */
public class CommandTPR extends CommandBase {
    @Override
    public String getName() {
        return "tpr";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/tpr <latitude> <longitude> [target]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2)    {
            sender.sendMessage(new TextComponentString("§cUsage: " + this.getUsage(sender)));
            return;
        }

        double lat;
        try {
            lat = Double.parseDouble(args[0]);
        } catch (NumberFormatException e)   {
            sender.sendMessage(new TextComponentString(String.format("§cCouldn't parse number: %s", args[0])));
            return;
        }

        double lon;
        try {
            lon = Double.parseDouble(args[1]);
        } catch (NumberFormatException e)   {
            sender.sendMessage(new TextComponentString(String.format("§cCouldn't parse number: %s", args[1])));
            return;
        }

        Vec2i targetPos = CoordUtils.globalToBlock(new Vec2d(lon, lat));
        Chunk chunk = server.getWorld(0).getChunkFromChunkCoords(targetPos.getX() >> 4, targetPos.getY() >> 4);
        ITerrainHeightHolder heightHolder = chunk.getCapability(HeightsCapability.TERRAIN_HEIGHT_CAPABILITY, null);

        ((EntityPlayer) sender).setPositionAndUpdate(targetPos.getX(), heightHolder.getHeights()[0], targetPos.getY());
    }
}
