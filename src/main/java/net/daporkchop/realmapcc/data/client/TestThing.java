package net.daporkchop.realmapcc.data.client;

import net.daporkchop.lib.common.util.PorkUtil;
import net.daporkchop.lib.graphics.PImage;
import net.daporkchop.lib.graphics.impl.image.DirectImage;
import net.daporkchop.lib.math.arrays.grid.Grid2d;
import net.daporkchop.lib.math.interpolation.CubicInterpolationEngine;
import net.daporkchop.lib.math.interpolation.InterpolationEngine;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.RealmapCC;

import static net.daporkchop.lib.math.primitive.PMath.floorI;

/**
 * @author DaPorkchop_
 */
public class TestThing implements Constants {
    public static void main(String... args) {
        if (true)   {
            RealmapCC.Conf.dataCacheDir = "/home/daporkchop/192.168.1.119/Public/minecraft/mods/realworldcc/data/";
        }

        if (true)   {
            Grid2d grid = new DataProcessor().getGrid();
            InterpolationEngine engine = new CubicInterpolationEngine();

            double hMult = 0.5d;
            int w = 1600;

            int h = floorI(w * hMult);
            double scaleBase = 6.0d;
            double scaleX = 1.0d / w * scaleBase;
            double scaleY = 1.0d / h * scaleBase * hMult;

            PImage img = new DirectImage(w, h, true);
            Grid2d otherGrid = Grid2d.of(w, h, true);

            for (int x = w - 1; x >= 0; x--) {
                for (int y = h - 1; y >= 0; y--) {
                    double height = engine.getInterpolated((5.0d + x * scaleX) * ARCSECONDS_PER_DEGREE, (43.0d + y * scaleY) * ARCSECONDS_PER_DEGREE, grid);
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
        }
    }
}
