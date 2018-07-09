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
    public final NBitArray array;

    public CompactedHeightData(short baseHeight, NBitArray array) {
        this.baseHeight = baseHeight;
        this.array = array;
    }

    public static CompactedHeightData getFrom(short[] rawHeights) {
        //calculate maximum variation
        int variation;
        short min = Short.MAX_VALUE;
        {
            short max = Short.MIN_VALUE;
            for (short s : rawHeights) {
                if (s < min) {
                    min = s;
                }
                if (s > max) {
                    max = s;
                }
            }
            variation = max - min;
            //System.out.println("min: " + min + ", max: " + max + ", var: " + variation);
        }
        int requiredBits = RequiredBits.getNumBitsNeededFor(variation);
        if (requiredBits == 0) {
            return new CompactedHeightData(min, new NBitArray(1, 0));
        } else {
            System.out.println("Required bits: " + requiredBits);
            NBitArray array = new NBitArray(rawHeights.length, requiredBits);
            for (int i = rawHeights.length - 1; i >= 0; i--) {
                array.set(i, rawHeights[i] - min);
            }
            return new CompactedHeightData(min, array);
        }
    }

    public int getHeight(int x, int z) {

    }

    public static class Serializer extends ValueSerializer<CompactedHeightData> {
        @Override
        public void write(CompactedHeightData value, PorkBuf buf) {
            buf.putShort(value.baseHeight);
            buf.putInt(value.array.getData().length);
            for (long l : value.array.getData()) {
                buf.putLong(l);
            }
            buf.putInt(value.array.getBitsPer());
        }

        @Override
        public CompactedHeightData read(PorkBuf buf) {
            short baseHeight = buf.getShort();
            long[] l = new long[buf.getInt()];
            for (int i = 0; i < l.length; i++) {
                l[i] = buf.getLong();
            }
            return new CompactedHeightData(baseHeight, new NBitArray(l, buf.getInt()));
        }
    }
}
