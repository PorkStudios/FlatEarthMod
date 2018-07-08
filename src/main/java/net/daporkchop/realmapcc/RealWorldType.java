package net.daporkchop.realmapcc;

import io.github.opencubicchunks.cubicchunks.api.util.IntRange;
import io.github.opencubicchunks.cubicchunks.api.world.ICubicWorldType;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldType;

import javax.annotation.Nullable;

/**
 * @author DaPorkchop_
 */
public class RealWorldType extends WorldType implements ICubicWorldType {
    public RealWorldType() {
        super("Real");
    }

    @Override
    public boolean hasCubicGeneratorForWorld(World world) {
        return world.provider.getDimensionType() == DimensionType.OVERWORLD;
    }

    @Nullable
    @Override
    public ICubeGenerator createCubeGenerator(World world) {
        return new RealTerrainGenerator(world);
    }

    @Override
    public IntRange calculateGenerationHeightRange(WorldServer world) {
        return new IntRange(0, Short.MAX_VALUE);
    }
}
