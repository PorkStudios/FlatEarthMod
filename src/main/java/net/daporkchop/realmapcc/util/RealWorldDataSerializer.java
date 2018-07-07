package net.daporkchop.realmapcc.util;

import net.daporkchop.lib.binary.PorkBuf;
import net.daporkchop.lib.db.object.serializer.ValueSerializer;

/**
 * @author DaPorkchop_
 */
public class RealWorldDataSerializer extends ValueSerializer<RealWorldData> {
    @Override
    public void write(RealWorldData value, PorkBuf buf) {
        value.write(buf);
    }

    @Override
    public RealWorldData read(PorkBuf buf) {
        return RealWorldData.read(buf);
    }
}
