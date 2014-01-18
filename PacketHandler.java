package stonebreaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if(packet.channel.equalsIgnoreCase(Config.channel) == false) {
			return;
		}

		DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			String command = stream.readUTF();

			if(command.equalsIgnoreCase(EnumCommand.BREAK.toString())) {
				int x = stream.readInt();
				int y = stream.readInt();
				int z = stream.readInt();

				breakBlock(player, x, y, z);
			}
			if(command.equalsIgnoreCase(EnumCommand.REGISTER.toString())) {
				if(StoneBreaker.config.allow_register) {
					int blockId = stream.readInt();
					int metadata = stream.readInt();

					registerBlock(player, blockId, metadata);
					StoneBreaker.config.sendConfigToPlayer(manager);
				}
			}
			if(command.equalsIgnoreCase(EnumCommand.CONFIG.toString())) {
				StoneBreaker.config.readPacket(stream);
				printConfig(player);
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	private void printConfig(Player player) {
		if(player instanceof EntityClientPlayerMP) {
			EntityClientPlayerMP thePlayer = (EntityClientPlayerMP) player;
			String target = "";
			for(int[] pair : StoneBreaker.config.blocks) {
				int blockId = pair[0];
				int metadata = pair[1];
				Block block = Block.blocksList[blockId];
				if(block != null) {
					if(target.isEmpty() == false) {
						target += ", ";
					}
					target += new ItemStack(blockId, 1, metadata).getDisplayName();
				}
			}
			//thePlayer.addChatMessage("StoneBreaker Target = " + target);
			System.out.println("StoneBreaker Target = " + target);
			Config c = StoneBreaker.config;
			System.out.println("StoneBreaker config : blockIDs = " + target);
			target = "";
			for(String s : c.tools) {
				if(target.isEmpty() == false) {
					target += ", ";
				}
				target += s;
			}
			System.out.println("StoneBreaker config : tools = " + target);

			System.out.println( "StoneBreaker config : effective_tool_only = " + Boolean.toString(c.effective_tool_only));

			target = "";
			for(EnumMode m : c.allowMode) {
				if(target.isEmpty() == false) {
					target += ", ";
				}
				target += m.toString();
			}
			System.out.println( "StoneBreaker config : allowMode = " + target);

			System.out.println( "StoneBreaker config : break_limit = " + Integer.toString(c.break_limit));
			System.out.println( "StoneBreaker config : distance_limit = " + Integer.toString(c.distance_limit));
			System.out.println( "StoneBreaker config : virtical_distance_limit = " + Integer.toString(c.virtical_distance_limit));
			System.out.println("StoneBreaker config : drop_to_player = " + Boolean.toString(c.drop_to_player));
			System.out.println( "StoneBreaker config : allow_register = " + Boolean.toString(c.allow_register));
		}
	}

	private void registerBlock(Player player, int blockId, int metadata) {
		if(player instanceof EntityPlayerMP) {
			EntityPlayerMP thePlayer = (EntityPlayerMP) player;
			String target = "";
			Block block = Block.blocksList[blockId];
			if(block != null) {
				target += new ItemStack(blockId, 1, metadata).getDisplayName();
				//target += Block.blocksList[blockId].getLocalizedName();
				boolean found = false;
				for(int[] pair : StoneBreaker.config.blocks) {
					if(pair[0] == blockId && (pair[1] == OreDictionary.WILDCARD_VALUE || pair[1] == metadata)) {
						StoneBreaker.config.blocks.remove(pair);
						thePlayer.addChatMessage("StoneBreaker Target Remove: " + target);
						found = true;
						break;
					}
				}
				if(found == false) {
					StoneBreaker.config.blocks.add(new int[]{blockId, metadata});
					thePlayer.addChatMessage("StoneBreaker Target Add: " + target);
				}
			}
		}

	}

	private void breakBlock(Player player, int x, int y, int z) {
		EntityPlayerMP thePlayer = (EntityPlayerMP) player;
		World theWorld = thePlayer.worldObj;

		int blockId = theWorld.getBlockId(x, y, z);
		if(blockId == 0) return;

		Block block = Block.blocksList[blockId];
		if(block == null) return;

		int metadata = theWorld.getBlockMetadata(x, y, z);

		ItemStack itemStack = thePlayer.getCurrentEquippedItem();
		if(itemStack == null) return;

		int currentItem = thePlayer.inventory.currentItem;

		block.onBlockDestroyedByPlayer(theWorld, x, y, z, blockId);
		theWorld.playAuxSFX(2001, x, y, z, block.blockID + (theWorld.getBlockMetadata(x, y, z) << 12));
		boolean flag = theWorld.setBlock(x, y, z, 0, 0, 3);

		if (block != null && flag) {
			block.removeBlockByPlayer(theWorld, thePlayer, x, y, z);
		}

		itemStack.onBlockDestroyed(theWorld, blockId, x, y, z, thePlayer);
		if (itemStack.stackSize == 0) {
			// itemstack.onItemDestroyedByUse(minecraft.thePlayer);
			thePlayer.destroyCurrentEquippedItem();
			//ret = false;
		}

		boolean flag1 = thePlayer.canHarvestBlock(block);

		if (flag1) {
			if(StoneBreaker.config.drop_to_player) {
				block.harvestBlock(theWorld, thePlayer, (int)thePlayer.posX, (int)thePlayer.posY, (int)thePlayer.posZ, metadata);
			}
			else {
				block.harvestBlock(theWorld, thePlayer, x, y, z, metadata);
			}
		}
	}

}
