package net.daporkchop.realmapcc.generator;

import it.unibo.elevation.ElevationAPI;
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
        ElevationAPI api = new SrtmElevationAPI(root, 1200);
        PorkDB<ChunkPos, RealWorldData> db = new DBBuilder<ChunkPos, RealWorldData>()
                .setCompression(EnumCompression.GZIP)
                .setFormat(DatabaseFormat.ZIP_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new RealWorldDataSerializer())
                .setRootFolder(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/worldData"))
                .build();

        //SRTM sampled between 60°N and 56°S, so there's going to be a total of

        /*int size = 1024;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int x = size - 1; x >= 0; x--) {
            for (int y = size - 1; y >= 0; y--) {
                //int col = MathHelper.clamp((int) (api.getElevation(x * 0.05d, y * 0.05d) / 20), 0, 255);
                //image.setRGB(y, x ^ (size - 1), (col << 16) | (col << 8) | col);
                int col = (int) (api.getElevation(x * 0.05d, y * 0.05d));
                if (col < 0)    {
                    col = 0xFF5555;
                }
                image.setRGB(y, x ^ (size - 1), 0xFF000000 | col);
            }
        }

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);*/
    }
}
