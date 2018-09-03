package net.daporkchop.realmapcc.data.capability;

import net.daporkchop.lib.encoding.ToBytes;
import net.daporkchop.realmapcc.RealmapCC;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author DaPorkchop_
 */
public class HeightsCapability {
    @CapabilityInject(ITerrainHeightHolder.class)
    public static final Capability<ITerrainHeightHolder> TERRAIN_HEIGHT_CAPABILITY = null;

    private static final ResourceLocation ID = new ResourceLocation(RealmapCC.MOD_ID, "heights");

    private static final String ID_STRING = ID.toString();

    public static void register() {
        CapabilityManager.INSTANCE.register(ITerrainHeightHolder.class, new Capability.IStorage<ITerrainHeightHolder>() {
            @Override
            public NBTBase writeNBT(Capability<ITerrainHeightHolder> capability, ITerrainHeightHolder instance, EnumFacing side) {
                return new NBTTagCompound();
            }

            @Override
            public void readNBT(Capability<ITerrainHeightHolder> capability, ITerrainHeightHolder instance, EnumFacing side, NBTBase nbt) {

            }
        }, TerrainHeightsImpl::new);
    }

    @Mod.EventBusSubscriber
    @SuppressWarnings("unused")
    public static class EventHandler {

        @SubscribeEvent
        public static void attachCapabilities(AttachCapabilitiesEvent<Chunk> event) {
            //ITerrainHeightHolder chunkEnergyHolder = new TerrainHeightsImpl();
            event.addCapability(ID, new ICapabilitySerializable<NBTTagByteArray>() {
                private final ITerrainHeightHolder inst = TERRAIN_HEIGHT_CAPABILITY.getDefaultInstance();

                @Override
                public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
                    return capability == TERRAIN_HEIGHT_CAPABILITY;
                }

                @Nullable
                @Override
                public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
                    return capability == TERRAIN_HEIGHT_CAPABILITY ? TERRAIN_HEIGHT_CAPABILITY.cast(this.inst) : null;
                }

                @Override
                public NBTTagByteArray serializeNBT() {
                    short[] s = this.inst.getHeights();
                    if (s == null) {
                        return new NBTTagByteArray(new byte[0]);
                    } else {
                        return new NBTTagByteArray(ToBytes.toBytes(s));
                    }
                }

                @Override
                public void deserializeNBT(NBTTagByteArray nbt) {
                    byte[] b = nbt.getByteArray();
                    if (b.length == 0) {
                        this.inst.setHeights(null);
                    } else {
                        this.inst.setHeights(ToBytes.toShorts(b));
                    }
                }
            });
        }
    }
}
