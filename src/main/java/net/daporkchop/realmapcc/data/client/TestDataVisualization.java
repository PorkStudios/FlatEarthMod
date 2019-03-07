package net.daporkchop.realmapcc.data.client;

import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.graphics.impl.image.DirectImage;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.InterpolationEngine;
import net.daporkchop.lib.math.interpolation.LinearInterpolationEngine;
import net.daporkchop.lib.math.interpolation.NearestNeighborInterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;
import org.apache.commons.imaging.ImageWriteException;

import java.io.IOException;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
public class TestDataVisualization implements Constants {
    public static void main(String... args) throws IOException, ImageWriteException {
        if (true)   {
            RealmapCC.Conf.dataCacheDir = "/home/daporkchop/192.168.1.119/Public/minecraft/mods/realworldcc/data/";
            RealmapCC.Conf.maxTileCacheSize = 2048L;
        }

        if (true)   {
            InterpolationEngine engine = ENGINE_CUBIC;
            DataProcessor processor = new DataProcessor(engine);

            //centered precisely on Switzerland!
            double minLon = 5.0d;
            double minLat = 48.0d;
            double realWidth = 5.0d;
            int w = 1024;
            double hMult = 3.0d / realWidth;

            if (false)   {
                //centered on Zug, Switzerland
                minLon = 8.455d;
                minLat = 47.15d;
                realWidth = 0.1d;
                hMult = 0.1d / realWidth;
                //hMult = 1.0d;
            }

            int h = floorI(w * hMult);
            double scale = 1.0d / w * realWidth;

            PImage img = new DirectImage(w, h, false);
            Grid2d otherGrid = Grid2d.of(w, h, false);
            Grid2d finalGrid = Grid2d.of(w, h, false);

            processor.forEachValueInRange(
                    minLon,
                    minLat,
                    w,
                    h,
                    scale,
                    (xStep, yStep, height, waterPresence, biome) -> {
                        otherGrid.setI(xStep, h - 1 - yStep, height);
                        finalGrid.setI(xStep, h - 1 - yStep, waterPresence ? 1 : 0);
                    },
                    null
            );

            int max = Integer.MIN_VALUE;
            int min = Integer.MAX_VALUE;
            for (int x = w - 1; x >= 0; x--) {
                for (int y = h - 1; y >= 0; y--) {
                    int val = otherGrid.getI(x, y);
                    if (val > max)  {
                        max = val;
                    }
                    if (val < min)  {
                        min = val;
                    }
                }
            }

            System.out.println("Finishing up...");
            for (int x = w - 1; x >= 0; x--) {
                for (int y = h - 1; y >= 0; y--) {
                    if (finalGrid.getI(x, y) == 1)   {
                        img.setRGB(x, y, 0xFF0000);
                    } else {
                        img.setBW(x, y, (otherGrid.getI(x, y) - min) * 256 / max);
                    }
                }
            }

            PorkUtil.simpleDisplayImage(img.getAsBufferedImage(), true);
            //Imaging.writeImage(img.getAsBufferedImage(), new File("./cool.png"), ImageFormats.PNG, null);
        }
    }
}
