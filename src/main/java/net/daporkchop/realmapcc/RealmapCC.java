package net.daporkchop.realmapcc;

import io.github.opencubicchunks.cubicchunks.api.util.GeneratorSettingsFix;
import io.github.opencubicchunks.cubicchunks.api.util.NotCubicChunksWorldException;
import net.daporkchop.lib.db.DBBuilder;
import net.daporkchop.lib.db.DatabaseFormat;
import net.daporkchop.lib.db.PorkDB;
import net.daporkchop.lib.encoding.compression.EnumCompression;
import net.daporkchop.realmapcc.util.KeyHasherChunkPos;
import net.daporkchop.realmapcc.util.RealWorldData;
import net.daporkchop.realmapcc.util.RealWorldDataSerializer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

@Mod(
        modid = RealmapCC.MOD_ID,
        name = RealmapCC.MOD_NAME,
        version = RealmapCC.VERSION,
        dependencies = "required-after:depmanager@[0.0.2,);"
)
public class RealmapCC {

    public static final String MOD_ID = "realmapcc";
    public static final String MOD_NAME = "Realmap - Cubic Chunks";
    public static final String VERSION = "1.0-SNAPSHOT";

    @Mod.Instance(MOD_ID)
    public static RealmapCC INSTANCE;

    public static PorkDB<ChunkPos, RealWorldData> worldDataDB;

    static {
        Runtime.getRuntime().addShutdownHook(
                new Thread("Realmap World Data DB Closer Thread") {
                    @Override
                    public void run() {
                        if (worldDataDB != null) {
                            worldDataDB.shutdown();
                        }
                    }
                }
        );
    }

    public static File getWorkingFolder() {
        File toBeReturned;
        try {
            if (FMLCommonHandler.instance().getSide().isClient()) {
                toBeReturned = Minecraft.getMinecraft().mcDataDir;
            } else {
                toBeReturned = FMLCommonHandler.instance().getMinecraftServerInstance().getFile("");
            }
            return toBeReturned;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Mod.EventHandler
    public void construct(FMLConstructionEvent event) {
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Preparing world data", 2);

        progressBar.step("Opening database");
        worldDataDB = new DBBuilder<ChunkPos, RealWorldData>()
                .setForceOpen(true)
                .setCompression(EnumCompression.GZIP)
                .setFormat(DatabaseFormat.ZIP_TREE)
                .setKeyHasher(new KeyHasherChunkPos())
                .setValueSerializer(new RealWorldDataSerializer())
                .setRootFolder(new File(getWorkingFolder(), "realMap/worldData"))
                .build();

        progressBar.step("Fetching map data");
        //TODO: download map data

        ProgressManager.pop(progressBar);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (!Loader.isModLoaded("cubicchunks")) {
            throw new NotCubicChunksWorldException();
        }

        GeneratorSettingsFix.addFixableWorldType(new RealWorldType());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
