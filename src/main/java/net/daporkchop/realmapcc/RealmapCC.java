package net.daporkchop.realmapcc;

import net.daporkchop.realmapcc.capability.HeightsCapability;
import net.daporkchop.realmapcc.command.CommandTPR;
import net.daporkchop.realmapcc.data.Tile;
import net.daporkchop.realmapcc.data.client.DataProcessor;
import net.daporkchop.realmapcc.data.client.LookupGrid2d;
import net.daporkchop.realmapcc.generator.RealWorldType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Objects;
import java.util.stream.Stream;

@Mod(
        modid = RealmapCC.MOD_ID,
        name = RealmapCC.MOD_NAME,
        version = RealmapCC.VERSION/*,
        dependencies = "required-after:cubicgen"*/
)
public class RealmapCC implements Constants {
    public static final String MOD_ID = "realmapcc";
    public static final String MOD_NAME = "Realmap - Cubic Chunks";
    public static final String VERSION = "1.0-SNAPSHOT";
    @Mod.Instance(MOD_ID)
    public static RealmapCC INSTANCE;
    public static Logger logger;

    static {
        //hey let's go overboard
        Stream.of(
                null
                , Tile.class
                , LookupGrid2d.class
                , DataProcessor.class
        ).filter(Objects::nonNull).forEach(Compiler::compileClass);
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
    public void construct(FMLConstructionEvent event) throws MalformedURLException {
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();

        //GeneratorSettingsFix.addFixableWorldType(new RealWorldType());
        new RealWorldType();

        HeightsCapability.register();

        MinecraftForge.EVENT_BUS.register(new Events());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTPR());
    }

    @Config(modid = MOD_ID)
    public static class Conf {
        @Config.Comment({
                "The scale multiplier on the horizontal axes (X/Z)."
        })
        public static double scaleHoriz = 1.0d;

        @Config.Comment({
                "The scale multiplier for the vertical (Y) axis."
        })
        public static double scaleVert = 1.0d;

        @Config.Comment({
                "Whether or not to fail (crash the game) if a specific terrain tile couldn't be obtained from either the disk cache or remote server.",
                "If false, unobtainable tiles will simply be filled with ocean at y=0"
        })
        public static boolean failIfTileNotFound = false;

        @Config.Comment({
                "The maximum number of tiles to keep cached in memory at once.",
                "Each tile is about 220kb once fully loaded, so keep this in mind when configuring this value.",
                "Setting this too low can lead to horrible performance."
        })
        public static long maxTileCacheSize = 256L;

        @Config.Comment({
                "The base URL of the server from which will terrain data will be obtained.",
                "Must end with a trailing slash.",
                "Don't touch this unless you know what you're doing!"
        })
        public static String dataBaseUrl = "https://cloud.daporkchop.net/minecraft/mods/realworldcc/data/";

        @Config.Comment({
                "The root directory in which terrain data will be cached.",
                "Relative paths are treated as being relative to either your .minecraft folder (for clients) or the directory from which the server was started (for servers)",
                "If it starts to get too big, it may be safely deleted when the game isn't running, but be aware that this may come at a performance cost!",
                "Must end with a trailing slash.",
                "Most of the time there should be no reason to change this."
        })
        public static String dataCacheDir = "./realmapcc/data/";
    }
}
