package net.daporkchop.realmapcc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.daporkchop.lib.math.vector.d.Vec2d;
import net.daporkchop.lib.math.vector.i.Vec2i;
import net.daporkchop.realmapcc.Constants;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoordUtils implements Constants {
    public static Vec2i globalToBlock(@NonNull Vec2d global)  {
        return new Vec2i(
                floorI(global.getX() * ARCSECONDS_PER_DEGREE * METERS_PER_ARCSECOND),
                floorI(global.getY() * ARCSECONDS_PER_DEGREE * METERS_PER_ARCSECOND)
        );
    }

    public static Vec2d blockToGlobal(@NonNull Vec2i block)   {
        return new Vec2d(
                block.getX() * ARCSECONDS_PER_METER / ARCSECONDS_PER_DEGREE,
                block.getY() * ARCSECONDS_PER_METER / ARCSECONDS_PER_DEGREE
        );
    }
}
