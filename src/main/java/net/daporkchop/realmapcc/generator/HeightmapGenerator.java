package net.daporkchop.realmapcc.generator;

import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.lib.encoding.compression.EnumCompression;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.data.CompactedHeightData;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.daporkchop.realmapcc.util.srtm.SrtmElevationAPI;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author DaPorkchop_
 */
public class HeightmapGenerator {
    public static void main(String... args) throws Exception {
        File root = new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/actualData/");
        int samples = Constants.width;
        double sampleStep = 1.0d / (double) samples;
        SrtmElevationAPI api = new SrtmElevationAPI(root, samples, false);
        PorkDB<ChunkPos, CompactedHeightData> db = new DBBuilder<ChunkPos, CompactedHeightData>()
                .setCompression(EnumCompression.GZIP)
                .setForceOpen(true)
                .setFormat(DatabaseFormat.ZIP_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new CompactedHeightData.Serializer())
                .setRootFolder(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/worldData"))
                .build();

        {
            System.out.println("Wiping existing database...");
            Set<ChunkPos> toRemove = new HashSet<>();
            db.forEach((k, v) -> toRemove.add(k));
            toRemove.forEach(db::remove);
            System.out.println("Done!");
        }

        AtomicBoolean cont = new AtomicBoolean(true);

        new Thread(() -> {
            Scanner s = new Scanner(System.in);
            s.nextLine();
            s.close();
            cont.set(false);
        }).start();

        short[] heights = new short[samples * samples];
        for (int tileX = -56; cont.get() && tileX < 60; tileX++) {
            for (int tileZ = -180; cont.get() && tileZ < 180; tileZ++) {
                System.out.println("Processing tile at " + tileX + ',' + tileZ);
                for (int x = 0; x < samples; x++) {
                    for (int z = 0; z < samples; z++) {
                        heights[x * samples + z] = (short) MathHelper.clamp(api.getElevation(
                                tileX + x * sampleStep,
                                tileZ + z * sampleStep), -1.0d, Short.MAX_VALUE);
                    }
                }
                api.getHelper().flushCache();

                CompactedHeightData data = CompactedHeightData.getFrom(heights);
                db.put(new ChunkPos(tileX, tileZ), data);
            }
        }

        System.out.println("Complete!");
        db.shutdown();
    }
}
