package net.daporkchop.realmapcc;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubeGeneratorsRegistry;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.ICubicPopulator;
import io.github.opencubicchunks.cubicchunks.cubicgen.BasicCubeGenerator;
import io.github.opencubicchunks.cubicchunks.cubicgen.common.biome.CubicBiome;
import it.unibo.elevation.ElevationAPI;
import it.unibo.elevation.srtm.SrtmElevationAPI;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static net.daporkchop.realmapcc.Constants.space_between_blocks;

/**
 * @author DaPorkchop_
 */
public class RealTerrainGenerator extends BasicCubeGenerator {
    private final ElevationAPI api = new SrtmElevationAPI(new File("/media/daporkchop/TooMuchStuff/PortableIDE/RealWorldCC/mapData/actualData/"), 1200);
    private final LoadingCache<ChunkPos, short[]> terrainData = CacheBuilder.newBuilder()
            .expireAfterAccess(10L, TimeUnit.MINUTES)
            .build(new CacheLoader<ChunkPos, short[]>() {
                @Override
                public short[] load(ChunkPos key) throws Exception {
                    short[] s = new short[16 * 16];
                    for (int x = 15; x >= 0; x--) {
                        for (int z = 15; z >= 0; z--) {
                            s[(x << 4) | z] = (short) RealTerrainGenerator.this.api.getElevation(((key.x << 4) | x) * space_between_blocks, ((key.z << 4) | z) * space_between_blocks);
                        }
                    }
                    return s;
                }
            });

    public RealTerrainGenerator(World world) {
        super(world);
    }

    @Override
    public void generateColumn(Chunk column) {
        //TODO: set biomes
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        CubePrimer primer = new CubePrimer();
        short[] heights = this.terrainData.getUnchecked(new ChunkPos(cubeX, cubeZ));
        IBlockState stone = Blocks.STONE.getDefaultState();
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                int height = heights[(x << 4) | z];
                for (int y = 0; y < 16; y++) {
                    if (((cubeY << 4) | y) < height) {
                        break;
                    }
                    primer.setBlockState(x, y, z, stone);
                }
            }
        }
        return primer;
    }

    @Override
    public void populate(ICube cube) {
        if (!MinecraftForge.EVENT_BUS.post(new CubePopulatorEvent(this.world, cube))) {
            CubicBiome cubicBiome = CubicBiome.getCubic(cube.getWorld().getBiome(Coords.getCubeCenter(cube)));

            CubePos pos = cube.getCoords();

            Random rand = Coords.coordsSeedRandom(cube.getWorld().getSeed(), cube.getX(), cube.getY(), cube.getZ());

            ICubicPopulator decorator = cubicBiome.getDecorator();
            decorator.generate(this.world, rand, pos, cubicBiome.getBiome());
            CubeGeneratorsRegistry.generateWorld(this.world, rand, pos, cubicBiome.getBiome());
        }
    }
}
