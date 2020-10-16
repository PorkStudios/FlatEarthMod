/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2020 DaPorkchop_ and contributors
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it. Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.realmapcc.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString("§cUsage: " + this.getUsage(sender)));
            return;
        }

        double lat;
        try {
            lat = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(String.format("§cCouldn't parse number: %s", args[0])));
            return;
        }

        double lon;
        try {
            lon = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new TextComponentString(String.format("§cCouldn't parse number: %s", args[1])));
            return;
        }

        /*Vec2i targetPos = CoordUtils.globalToBlock(new Vec2d(lon, lat));
        Chunk chunk = server.getWorld(0).getChunkFromChunkCoords(targetPos.getX() >> 4, targetPos.getY() >> 4);
        ITerrainHeightHolder heightHolder = chunk.getCapability(HeightsCapability.TERRAIN_HEIGHT_CAPABILITY, null);

        ((EntityPlayer) sender).setPositionAndUpdate(targetPos.getX(), heightHolder.getHeights()[0], targetPos.getY());*/
    }
}
