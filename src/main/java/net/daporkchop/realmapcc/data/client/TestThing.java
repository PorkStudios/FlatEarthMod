package net.daporkchop.realmapcc.data.client;

import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.graphics.impl.image.DirectImage;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.CubicInterpolationEngine;
import net.daporkchop.lib.math.interpolation.InterpolationEngine;
import net.daporkchop.lib.math.interpolation.LinearInterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;

import java.io.File;
import java.io.IOException;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
public class TestThing implements Constants {
    public static void main(String... args) throws IOException, ImageWriteException {
        if (true)   {
            RealmapCC.Conf.dataCacheDir = "/home/daporkchop/192.168.1.119/Public/minecraft/mods/realworldcc/data/";
        }

        if (true)   {
            Grid2d grid = new DataProcessor().getGrid();
            InterpolationEngine engine = new LinearInterpolationEngine();

            double minLon = 5.0d;
            double maxLat = 48.0d;
            int w = 1024;
            double hMult = 3.0d / 5.0d;

            int h = floorI(w * hMult);
            double scaleBase = 5.0d;
            double scale = 1.0d / w * scaleBase;

            PImage img = new DirectImage(w, h, true);
            Grid2d otherGrid = Grid2d.of(w, h, true);

            for (int x = w - 1; x >= 0; x--) {
                for (int y = h - 1; y >= 0; y--) {
                    double height = engine.getInterpolated((minLon + x * scale) * ARCSECONDS_PER_DEGREE, (maxLat - y * scale) * ARCSECONDS_PER_DEGREE, grid);
                    /*if (height > 5) {
                        img.setARGB(x, y, 0xFFFFFFFF);
                    } else {
                        img.setBW(x, y, 0xFF000000);
                    }*/
                    //img.setRGB(x, y, floorI(height));
                    otherGrid.setD(x, y, height);
                }
            }

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

            for (int x = w - 1; x >= 0; x--) {
                for (int y = h - 1; y >= 0; y--) {
                    img.setBW(x, y, (otherGrid.getI(x, y) - min) * 256 / max);
                }
            }

            PorkUtil.simpleDisplayImage(img.getAsBufferedImage(), true);
            //Imaging.writeImage(img.getAsBufferedImage(), new File("./cool.png"), ImageFormats.PNG, null);
        }
    }
}
