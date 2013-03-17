package stonebreaker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class PacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		if(packet.channel != Config.channel) {
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
					StoneBreaker.config.sendTargetToPlayer(manager);
				}
			}
			if(command.equalsIgnoreCase(EnumCommand.TARGET.toString())) {
				int size = stream.readInt();
				StoneBreaker.config.blocks.clear();

				for(int i = 0; i < size; ++i) {
					int blockId = stream.readInt();
					int metadata = stream.readInt();
					StoneBreaker.config.blocks.add(blockId);
				}
				printTarget(player);
			}
		} catch (IOException e) {
			// TODO Ž©“®¶¬‚³‚ê‚½ catch ƒuƒƒbƒN
			e.printStackTrace();
		}
	}

	private void printTarget(Player player) {
		if(player instanceof EntityClientPlayerMP) {
			EntityClientPlayerMP thePlayer = (EntityClientPlayerMP) player;
			String target = "";
			for(int blockId : StoneBreaker.config.blocks) {
				if(target.isEmpty() == false) {
					target += ", ";
				}
				target += Block.blocksList[blockId].getLocalizedName();
			}
			thePlayer.addChatMessage("StoneBreaker Target = " + target);
		}
	}

	private void registerBlock(Player player, int blockId, int metadata) {
		if(StoneBreaker.config.blocks.contains(blockId)) {
			StoneBreaker.config.blocks.remove(blockId);
		}
		else {
			StoneBreaker.config.blocks.add(blockId);
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
		boolean flag = theWorld.setBlockAndMetadataWithNotify(x, y, z, 0, 0, 3);

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
