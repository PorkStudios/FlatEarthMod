package net.daporkchop.realmapcc.generator;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import net.daporkchop.realmapcc.Constants;
import net.daporkchop.realmapcc.capability.HeightsCapability;
import net.daporkchop.realmapcc.capability.ITerrainHeightHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author DaPorkchop_
 */
public class RealTerrainGenerator implements ICubeGenerator, Constants {
    private final World world;
    //private final SrtmHelperDB api = new SrtmHelperDB(RealmapCC.worldDataDB);

    public RealTerrainGenerator(World world) {
        this.world = world;
    }

    @Override
    public void generateColumn(Chunk column) {
        ITerrainHeightHolder heightHolder = column.getCapability(HeightsCapability.TERRAIN_HEIGHT_CAPABILITY, null);
        if (heightHolder == null) {
            throw new RuntimeException(String.format("Column (%d,%d) does not have the terrain height capability!", column.x, column.z));
        }
        //heightHolder.setHeights(this.api.getDataForChunk(column.getPos()));
        //TODO: set biomes
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        short[] heights;
        {
            Chunk column = this.world.getChunkFromChunkCoords(cubeX, cubeZ);
            ITerrainHeightHolder heightHolder = column.getCapability(HeightsCapability.TERRAIN_HEIGHT_CAPABILITY, null);
            if (heightHolder == null) {
                throw new RuntimeException(String.format("Column (%d,%d) does not have the terrain height capability!", column.x, column.z));
            }
            heights = heightHolder.getHeights();
        }
        if (heights == null) {
            throw new RuntimeException(String.format("Column (%d,%d) has no elevation data!", cubeX, cubeZ));
        }

        CubePrimer primer = new CubePrimer();
        IBlockState stone = Blocks.STONE.getDefaultState();
        for (int x = 15; x >= 0; x--) {
            for (int z = 15; z >= 0; z--) {
                int height = heights[(x << 4) | z];
                for (int y = 0; y < 16; y++) {
                    if (((cubeY << 4) | y) > height) {
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
            //CubicBiome cubicBiome = CubicBiome.getCubic(cube.getWorld().getBiome(Coords.getCubeCenter(cube)));

            CubePos pos = cube.getCoords();

            Random rand = Coords.coordsSeedRandom(cube.getWorld().getSeed(), cube.getX(), cube.getY(), cube.getZ());

            //ICubicPopulator decorator = cubicBiome.getDecorator();
            //decorator.generate(this.world, rand, pos, cubicBiome.getBiome());
            //CubeGeneratorsRegistry.generateWorld(this.world, rand, pos, cubicBiome.getBiome());
        }
    }

    @Override
    public Box getFullPopulationRequirements(ICube cube) {
        return RECOMMENDED_FULL_POPULATOR_REQUIREMENT;
    }

    @Override
    public Box getPopulationPregenerationRequirements(ICube cube) {
        return RECOMMENDED_GENERATE_POPULATOR_REQUIREMENT;
    }

    @Override
    public void recreateStructures(ICube cube) {
    }

    @Override
    public void recreateStructures(Chunk column) {
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType type, BlockPos pos) {
        return new ArrayList<>();
    }

    @Nullable
    @Override
    public BlockPos getClosestStructure(String name, BlockPos pos, boolean findUnexplored) {
        return null;
    }
}
