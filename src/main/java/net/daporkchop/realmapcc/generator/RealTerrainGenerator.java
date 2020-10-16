/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2018-2020 DaPorkchop_ and contributors
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it. Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original authors of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.realmapcc.generator;

import io.github.opencubicchunks.cubicchunks.api.util.Box;
import io.github.opencubicchunks.cubicchunks.api.util.Coords;
import io.github.opencubicchunks.cubicchunks.api.util.CubePos;
import io.github.opencubicchunks.cubicchunks.api.world.ICube;
import io.github.opencubicchunks.cubicchunks.api.worldgen.CubePrimer;
import io.github.opencubicchunks.cubicchunks.api.worldgen.ICubeGenerator;
import io.github.opencubicchunks.cubicchunks.api.worldgen.populator.CubePopulatorEvent;
import net.minecraft.entity.EnumCreatureType;
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
public class RealTerrainGenerator implements ICubeGenerator {
    private final World world;

    public RealTerrainGenerator(World world) {
        this.world = world;
    }

    @Override
    public void generateColumn(Chunk column) {
    }

    @Override
    public CubePrimer generateCube(int cubeX, int cubeY, int cubeZ) {
        return new CubePrimer();
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
