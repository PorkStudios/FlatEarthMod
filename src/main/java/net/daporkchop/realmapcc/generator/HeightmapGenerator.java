package net.daporkchop.realmapcc.generator;

import it.unibo.elevation.srtm.SrtmElevationAPI;
import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.lib.encoding.compression.EnumCompression;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.daporkchop.realmapcc.util.RealWorldData;
import net.daporkchop.realmapcc.util.RealWorldDataSerializer;
import net.minecraft.util.math.ChunkPos;

import java.io.File;

/**
 * @author DaPorkchop_
 */
public class HeightmapGenerator {
    public static void main(String... args) {
        File root = new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/actualData/");
        SrtmElevationAPI api = new SrtmElevationAPI(root);
        PorkDB<ChunkPos, RealWorldData> db = new DBBuilder<ChunkPos, RealWorldData>()
                .setCompression(EnumCompression.GZIP)
                .setFormat(DatabaseFormat.ZIP_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new RealWorldDataSerializer())
                .setRootFolder(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/worldData"))
                .build();
    }
}
