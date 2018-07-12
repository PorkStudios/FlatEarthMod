package net.daporkchop.realmapcc.data;

import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.lib.binary.PorkBuf;
import net.daporkchop.lib.binary.util.RequiredBits;
import net.daporkchop.lib.db.object.serializer.ValueSerializer;

/**
 * @author DaPorkchop_
 */
public class CompactedHeightData {
    public final short baseHeight;
    public final int width;
    public final NBitArray array;

    public CompactedHeightData(short baseHeight, NBitArray array, int width) {
        this.baseHeight = baseHeight;
        this.array = array;
        this.width = width;
    }

    public static CompactedHeightData getFrom(short[] rawHeights, int width) {
        //calculate maximum variation
        int variation;
        short min = Short.MAX_VALUE;
        short max = Short.MIN_VALUE;
        {
            for (int i = rawHeights.length - 1; i >= 0; i--) {
                short s = (short) Math.max(0, rawHeights[i]);
                if (s < min) {
                    min = s;
                }
                if (s > max) {
                    max = s;
                }
            }
            variation = max - min;
        }
        if (min == Short.MAX_VALUE || max == Short.MIN_VALUE || max == 0) {
            System.out.println("No data!");
            return null;
        }
        if (variation == 0) {
            System.out.println("min: " + min + ", max: " + max + ", no variation!");
            return new CompactedHeightData(min, null, width);
        } else {
            int requiredBits = RequiredBits.getNumBitsNeededFor(variation);
            NBitArray array = new NBitArray(width * width, requiredBits);
            for (int i = width * width - 1; i >= 0; i--) {
                array.set(i, rawHeights[i] - min);
            }
            System.out.println("min: " + min + ", max: " + max + ", required bits: " + requiredBits);
            return new CompactedHeightData(min, array, width);
        }
    }

    public int getHeight(int x, int z) {
        return this.baseHeight + (this.array == null ? 0 : this.array.get(x * this.width + z));
    }

    public static class Serializer extends ValueSerializer<CompactedHeightData> {
        @Override
        public void write(CompactedHeightData value, PorkBuf buf) {
            buf.putShort(value.baseHeight);
            buf.putInt(value.width);
            buf.putByte((byte) (value.array == null ? 0 : 1));
            if (value.array != null) {
                buf.putInt(value.array.getData().length);
                buf.putInt(value.array.getBitsPer());
                for (long l : value.array.getData()) {
                    buf.putLong(l);
                }
            }
        }

        @Override
        public CompactedHeightData read(PorkBuf buf) {
            short baseHeight = buf.getShort();
            int width = buf.getInt();
            byte flags = buf.getByte();

            NBitArray array = null;
            if (flags == 1) {
                long[] l = new long[buf.getInt()];
                array = new NBitArray(l, buf.getInt());
                for (int i = 0; i < l.length; i++) {
                    l[i] = buf.getLong();
                }
            }
            return new CompactedHeightData(baseHeight, array, width);
        }
    }
}
