package net.daporkchop.realmapcc.generator;

import it.unibo.elevation.ElevationAPI;
import it.unibo.elevation.srtm.SrtmElevationAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author DaPorkchop_
 */
public class HeightmapGenerator {
    public static void main(String... args) throws Exception {
        File root = new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/actualData/");
        ElevationAPI api = new SrtmElevationAPI(root);
        /*PorkDB<ChunkPos, RealWorldData> db = new DBBuilder<ChunkPos, RealWorldData>()
                .setCompression(EnumCompression.GZIP)
                .setFormat(DatabaseFormat.ZIP_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new RealWorldDataSerializer())
                .setRootFolder(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/worldData"))
                .build();*/

        int size = 1024;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = size - 1; x >= 0; x--) {
            for (int y = size - 1; y >= 0; y--) {
                image.setRGB(x, y, (int) (api.getElevation(x * 0.05d, y * 0.05d)));
            }
        }

        JFrame frame = new JFrame();
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(new JLabel(new ImageIcon(image)));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
