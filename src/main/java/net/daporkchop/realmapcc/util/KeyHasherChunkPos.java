package net.daporkchop.realmapcc.util;

import net.daporkchop.lib.db.object.key.KeyHasher;
import net.minecraft.util.math.ChunkPos;

/**
 * @author DaPorkchop_
 */
public class KeyHasherChunkPos extends KeyHasher<ChunkPos> {
    public static final KeyHasher<ChunkPos> instance = new KeyHasherChunkPos();

    private KeyHasherChunkPos() {
        super();
    }

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
        long l = ((key.x & 0xFFFFFFF0L) << 4) | ((key.z & 0xFFFFFFF0L) << 32) | (key.x & 0xF) | ((key.z & 0xF) << 4);
        hash[7] = (byte) (l & 0xFF);
        hash[0] = (byte) ((l >> 8) & 0xFF);
        hash[1] = (byte) ((l >> 16) & 0xFF);
        hash[2] = (byte) ((l >> 24) & 0xFF);
        hash[3] = (byte) ((l >> 32) & 0xFF);
        hash[4] = (byte) ((l >> 40) & 0xFF);
        hash[5] = (byte) ((l >> 48) & 0xFF);
        hash[6] = (byte) ((l >> 56) & 0xFF);
    }

    @Override
    public ChunkPos getKeyFromHash(byte[] hash) {
        long l = ((hash[7] & 0xFF) |
                ((hash[0] & 0xFF) << 8) |
                ((hash[1] & 0xFF) << 16) |
                ((hash[2] & 0xFF) << 24) |
                ((hash[3] & 0xFFL) << 32) |
                ((hash[4] & 0xFFL) << 40) |
                ((hash[5] & 0xFFL) << 48) |
                ((hash[6] & 0xFFL) << 56));
        return new ChunkPos(
                (int) ((l & 0xF) | ((l >> 4) & 0xFFFFFFF0)),
                (int) (((l >> 4) & 0xF) | ((l >> 32) & 0xFFFFFFF0))
        );
    }
}
