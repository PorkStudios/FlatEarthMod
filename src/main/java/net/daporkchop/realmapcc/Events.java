package net.daporkchop.realmapcc;

import net.daporkchop.lib.math.vector.d.Vec2d;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.util.CoordUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@Mod.EventBusSubscriber(modid = RealmapCC.MOD_ID)
public class Events {
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer player = mc.player;
            Vec2i pos = new Vec2i(floorI(player.posX), floorI(player.posZ));
            Vec2d globalPos = CoordUtils.blockToGlobal(pos);
            mc.fontRenderer.drawStringWithShadow(
                    String.format("%.6f, %.6f", globalPos.getX(), globalPos.getY()),
                    2.0f, 2.0f, Color.WHITE.getRGB()
            );
        }
    }
}
