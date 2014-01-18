package stonebreaker;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraftforge.common.Configuration;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = StoneBreaker.modid, name = StoneBreaker.modid, version = "1.0")
@NetworkMod(clientSideRequired = false, serverSideRequired = true, channels = { Config.channel }, packetHandler = PacketHandler.class, connectionHandler = ConnectionHandler.class, versionBounds = "[1.0]")
public class StoneBreaker {
	public static final String modid = "StoneBreaker";
	@SidedProxy(clientSide = "stonebreaker.ClientProxy", serverSide = "stonebreaker.CommonProxy")
	public static CommonProxy proxy;

	@Instance(modid)
	public static StoneBreaker instance;

	public static Logger logger = Logger.getLogger("Minecraft");

	public static Config config = new Config();

	@Mod.Init
	public void load(FMLInitializationEvent event) {
		proxy.init();
	}

	@PreInit
	public void preInit(FMLPreInitializationEvent event) {
		config.load(event.getSuggestedConfigurationFile());
	}

	@Mod.ServerStarting
	public void serverStarting(FMLServerStartingEvent event){
		event.registerServerCommand(new CommandTarget());
	}
}
