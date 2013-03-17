package stonebreaker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import net.minecraft.client.Minecraft;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.Player;

public class Config {
	public static final String channel = "sb";
	public static Set<Integer> blocks = new LinkedHashSet();
	public static Set<String> tools = new LinkedHashSet();
	public static boolean effective_tool_only = true;
	public static EnumMode mode = EnumMode.off;
	public static List<EnumMode> allowMode = new ArrayList();
	public int break_limit = 0;
	public int distance_limit = 0;
	public int virtical_distance_limit = 0;
	public boolean drop_to_player = true;
	public boolean allow_register = true;

	public void load(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();
			String value = cfg.get(Configuration.CATEGORY_GENERAL, "blockIDs", "14,15,16,21,56,73,74,89").getString();
			for(String token : value.split(",")) {
				try {
					Integer blockId = Integer.parseInt(token.trim());
					if(blockId != null) {
						blocks.add(blockId.intValue());
					}
				}
				catch(NumberFormatException e) {

				}
			}

			break_limit = cfg.get(Configuration.CATEGORY_GENERAL, "break_limit", 0).getInt();
			distance_limit = cfg.get(Configuration.CATEGORY_GENERAL, "distance_limit", 0).getInt();
			virtical_distance_limit = cfg.get(Configuration.CATEGORY_GENERAL, "virtical_distance_limit", 0).getInt();
			effective_tool_only = cfg.get(Configuration.CATEGORY_GENERAL, "effective_tool_only", true).getBoolean(true);
			drop_to_player = cfg.get(Configuration.CATEGORY_GENERAL, "drop_to_player", true).getBoolean(true);
			allow_register = cfg.get(Configuration.CATEGORY_GENERAL, "allow_register", true).getBoolean(true);

			allowMode.add(EnumMode.off);
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_line", true).getBoolean(true)) {
				allowMode.add(EnumMode.line);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_tunnel", true).getBoolean(true)) {
				allowMode.add(EnumMode.tunnel);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_upstair", true).getBoolean(true)) {
				allowMode.add(EnumMode.upstair);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_downstair", true).getBoolean(true)) {
				allowMode.add(EnumMode.downstair);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_upper", true).getBoolean(true)) {
				allowMode.add(EnumMode.upper);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_under", true).getBoolean(true)) {
				allowMode.add(EnumMode.under);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_horizontal", true).getBoolean(true)) {
				allowMode.add(EnumMode.horizontal);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_vertical", true).getBoolean(true)) {
				allowMode.add(EnumMode.vertical);
			}
			if(cfg.get(Configuration.CATEGORY_GENERAL, "enable_mode_all", true).getBoolean(true)) {
				allowMode.add(EnumMode.all);
			}
			cfg.save();
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, "StoneBreaker load config exception");
		} finally {
			cfg.save();
		}
	}

	public void toggleMode() {
		for(int i = 0; i < allowMode.size(); ++i) {
			if(allowMode.get(i).equals(mode)) {
				i = (i + 1) % allowMode.size();
				mode = allowMode.get(i);
				break;
			}
		}
	}

	public EnumMode getMode() {
		return mode;
	}

	public void sendTargetToPlayer(INetworkManager manager) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF(EnumCommand.TARGET.toString());
			stream.writeInt(StoneBreaker.config.blocks.size());
			for(int blockId : StoneBreaker.config.blocks) {
				stream.writeInt(blockId);
				stream.writeInt(0);
			}

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = Config.channel;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			manager.addToSendQueue(packet);
		} catch (IOException e) {
			// TODO Ž©“®¶¬‚³‚ê‚½ catch ƒuƒƒbƒN
			e.printStackTrace();
		}
	}

}
