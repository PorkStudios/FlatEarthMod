package net.daporkchop.realmapcc.data;

import net.daporkchop.lib.binary.NBitArray;
import net.daporkchop.lib.binary.stream.DataIn;
import net.daporkchop.lib.binary.stream.DataOut;
import net.daporkchop.lib.binary.util.RequiredBits;
import net.daporkchop.lib.db.object.serializer.ValueSerializer;

import java.io.IOException;

/**
 * @author DaPorkchop_
 */
public class CompactedHeightData {
    public static final ValueSerializer<CompactedHeightData> serializer = new Serializer();

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
            //System.out.println("No data!");
            return null;
        }
        if (variation == 0) {
            //System.out.println("min: " + min + ", max: " + max + ", no variation!");
            return new CompactedHeightData(min, null, width);
        } else {
            int requiredBits = RequiredBits.getNumBitsNeededFor(variation);
            NBitArray array = new NBitArray(width * width, requiredBits);
            for (int i = width * width - 1; i >= 0; i--) {
                array.set(i, rawHeights[i] - min);
            }
            //System.out.println("min: " + min + ", max: " + max + ", required bits: " + requiredBits);
            return new CompactedHeightData(min, array, width);
        }
    }

    public int getHeight(int x, int z) {
        return this.baseHeight + (this.array == null ? 0 : this.array.get(x * this.width + z));
    }

    private static class Serializer extends ValueSerializer<CompactedHeightData> {
        @Override
        public void write(CompactedHeightData value, DataOut out) throws IOException {
            if (value instanceof EmptyCompactedHeightData) {
                throw new IllegalArgumentException();
            }
            out.writeShort(value.baseHeight);
            out.writeInt(value.width);
            out.writeByte((byte) (value.array == null ? 0 : 1));
            if (value.array != null) {
                out.writeInt(value.array.getData().length);
                out.writeInt(value.array.getBitsPer());
                for (long l : value.array.getData()) {
                    out.writeLong(l);
                }
            }
        }

        @Override
        public CompactedHeightData read(DataIn in) throws IOException {
            short baseHeight = in.readShort();
            int width = in.readInt();
            byte flags = in.readByte();

            NBitArray array = null;
            if (flags == 1) {
                long[] l = new long[in.readInt()];
                array = new NBitArray(l, in.readInt());
                for (int i = 0; i < l.length; i++) {
                    l[i] = in.readInt();
                }
            }
            return new CompactedHeightData(baseHeight, array, width);
        }
    }
}
