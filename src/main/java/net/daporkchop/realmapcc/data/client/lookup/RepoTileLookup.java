package net.daporkchop.realmapcc.data.client.lookup;

import lombok.NonNull;
import net.daporkchop.lib.http.SimpleHTTP;
import net.daporkchop.realmapcc.RealmapCC;
import net.daporkchop.realmapcc.data.DataConstants;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.TilePos;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author DaPorkchop_
 */
public class RepoTileLookup implements TileLookup {
    @Override
    public Tile getTile(@NonNull TilePos pos) {
        Tile tile = new Tile();
        try {
            String url = String.format("%s%s", RealmapCC.Conf.dataBaseUrl, pos.getSubpath());
            if (RealmapCC.logger == null) {
                System.out.printf("Downloading tile: %s\n", url);
            } else {
                RealmapCC.logger.debug(String.format("Downloading tile: %s", url));
            }
            byte[] b = SimpleHTTP.get(url);
            DataConstants.loadImage(b, tile.getWrapper());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            return tile;
        }
    }
}
