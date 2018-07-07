package net.daporkchop.realmapcc.util;

import net.daporkchop.lib.db.object.key.KeyHasher;
import net.minecraft.util.math.ChunkPos;

/**
 * @author DaPorkchop_
 */
public class KeyHasherChunkPos extends KeyHasher<ChunkPos> {
    @Override
    public boolean canGetKeyFromHash() {
        return true;
    }

    @Override
    public int getKeyLength() {
        return 8;
    }

    @Override
    public void hash(ChunkPos key, byte[] hash) {
        hash[0] = (byte) (key.x & 0xFF);
        hash[1] = (byte) ((key.x >> 8) & 0xFF);
        hash[2] = (byte) ((key.x >> 16) & 0xFF);
        hash[3] = (byte) ((key.x >> 24) & 0xFF);
        hash[4] = (byte) (key.z & 0xFF);
        hash[5] = (byte) ((key.z >> 8) & 0xFF);
        hash[6] = (byte) ((key.z >> 16) & 0xFF);
        hash[7] = (byte) ((key.z >> 24) & 0xFF);
    }

    @Override
    public ChunkPos getKeyFromHash(byte[] hash) {
        int x = (hash[0] & 0xFF) |
                ((hash[1] & 0xFF) << 8) |
                ((hash[2] & 0xFF) << 16) |
                ((hash[3] & 0xFF) << 24);
        int z = (hash[4] & 0xFF) |
                ((hash[5] & 0xFF) << 8) |
                ((hash[6] & 0xFF) << 16) |
                ((hash[7] & 0xFF) << 24);
        return new ChunkPos(x, z);
    }
}
