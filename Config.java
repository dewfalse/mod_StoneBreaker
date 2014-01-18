package stonebreaker;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.Player;

public class Config {
	public static final String channel = "sb";
	public static Set<int[]> blocks = new LinkedHashSet();
	public static Set<String> tools = new LinkedHashSet();
	public static boolean effective_tool_only = true;
	public static EnumMode mode = EnumMode.off;
	public static List<EnumMode> allowMode = new ArrayList();
	public static int break_limit = 0;
	public static int distance_limit = 0;
	public static int virtical_distance_limit = 0;
	public static boolean drop_to_player = true;
	public static boolean allow_register = true;

	public void load(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();
			String value = cfg.get(Configuration.CATEGORY_GENERAL, "blockIDs", "14,15,16,21,56,73,74,89,129,153").getString();
			for(String token : value.split(",")) {
				try {
					String[] tmp = token.split(":");
					if(tmp.length == 1) {
						Integer blockId = Integer.parseInt(token.trim());
						if(blockId != null) {
							blocks.add(new int[] {blockId.intValue(), OreDictionary.WILDCARD_VALUE });
						}
					}
					else if(tmp.length == 2){
						Integer blockId = Integer.parseInt(tmp[0].trim());
						Integer metadata = Integer.parseInt(tmp[1].trim());
						if(blockId != null) {
							blocks.add(new int[] {blockId.intValue(),metadata.intValue()});
						}
					}
				}
				catch(NumberFormatException e) {

				}
			}

			value = cfg.get(Configuration.CATEGORY_GENERAL, "tools", "").getString();
			for(String token : value.split(",")) {
				String tool = token.trim();
				if(!tool.isEmpty()) {
					tools.add(tool);
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

	public void sendConfigToPlayer(INetworkManager manager) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF(EnumCommand.CONFIG.toString());

			// blocks
			stream.writeInt(StoneBreaker.config.blocks.size());
			for(int[] pair : StoneBreaker.config.blocks) {
				stream.writeInt(pair[0]);
				stream.writeInt(pair[1]);
			}

			// tools
			stream.writeInt(StoneBreaker.config.tools.size());
			for(String tool : StoneBreaker.config.tools) {
				stream.writeUTF(tool);
			}

			// effective_tool_only
			stream.writeBoolean(effective_tool_only);

			// allow mode
			stream.writeInt(EnumMode.values().length);
			for(EnumMode m : EnumMode.values()) {
				stream.writeBoolean(allowMode.contains(m));
			}

			stream.writeInt(break_limit);
			stream.writeInt(distance_limit);
			stream.writeInt(virtical_distance_limit);
			stream.writeBoolean(drop_to_player);
			stream.writeBoolean(allow_register);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = Config.channel;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;

			manager.addToSendQueue(packet);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public void readPacket(DataInputStream stream) throws IOException {
		blocks.clear();
		int size = stream.readInt();
		for(int i = 0; i < size; ++i) {
			int blockId = stream.readInt();
			int metadata = stream.readInt();
			blocks.add(new int[]{blockId, metadata});
		}

		tools.clear();
		size = stream.readInt();
		for(int i = 0; i < size; ++i) {
			String tool = stream.readUTF();
			tools.add(tool);
		}

		effective_tool_only = stream.readBoolean();

		allowMode.clear();
		size = stream.readInt();
		for(int i = 0; i < size; ++i) {
			if(stream.readBoolean()) {
				allowMode.add(EnumMode.values()[i]);
			}
		}
		if(allowMode.contains(EnumMode.off) == false){
			allowMode.add(EnumMode.off);
		}

		break_limit = stream.readInt();
		distance_limit = stream.readInt();
		virtical_distance_limit = stream.readInt();
		drop_to_player = stream.readBoolean();
		allow_register = stream.readBoolean();
	}

}
