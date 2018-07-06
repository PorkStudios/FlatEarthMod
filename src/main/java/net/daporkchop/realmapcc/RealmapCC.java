package net.daporkchop.realmapcc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
        modid = RealmapCC.MOD_ID,
        name = RealmapCC.MOD_NAME,
        version = RealmapCC.VERSION
)
public class RealmapCC {

    public static final String MOD_ID = "realmapcc";
    public static final String MOD_NAME = "Realmap - Cubic Chunks";
    public static final String VERSION = "1.0-SNAPSHOT";

    @Mod.Instance(MOD_ID)
    public static RealmapCC INSTANCE;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
