package net.daporkchop.realmapcc;

import net.daporkchop.realmapcc.capability.HeightsCapability;
import net.daporkchop.realmapcc.command.CommandTPR;
import net.daporkchop.realmapcc.generator.RealWorldType;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

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
    public void serverStarting(FMLServerStartingEvent event)    {
        event.registerServerCommand(new CommandTPR());
    }

    @Config(modid = MOD_ID)
    public static class Conf {
        @Config.Comment("The scale multiplier on the horizontal axes (X/Z)")
        public static double scaleHoriz = 1.0d;

        @Config.Comment("The scale multiplier for the vertical (Y) axis")
        public static double scaleVert = 1.0d;

        @Config.Comment("The base URL of the server which will serve terrain data. Must end with a trailing slash. Don't touch this unless you know what you're doing!")
        public static String dataBaseUrl = "https://cloud.daporkchop.net/minecraft/mods/realworldcc/data/";

        @Config.Comment("The root directory in which terrain data will be cached. Don't touch this unless you know what you're doing!")
        public static String dataCacheDir = "./realmapcc/data/";
    }
}
