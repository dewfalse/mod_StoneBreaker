package stonebreaker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.packet.Packet14BlockDig;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.src.ModLoader;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;

public class ClientTickHandler implements ITickHandler {

	int prev_blockHitWait = 0;
	int targetBlockId = 0;
	int targetBlockMetadata = 0;
	Coord blockCoord = new Coord();
	int sideHit = 0;

	Queue<Coord> nextTarget = new LinkedList<Coord>();
	Set<Coord> vectors = new LinkedHashSet();

	int count = 0;

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.CLIENT))) {
			GuiScreen guiscreen = Minecraft.getMinecraft().currentScreen;
			if (guiscreen == null) {
				onTickInGame();
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

	@Override
	public String getLabel() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	public void onTickInGame() {
		if(isBreakBlock()) {
			count = 0;
			startBreak();
		}
		if(continueBreak() == false) {
		}
		if(isValidTool(targetBlockId, targetBlockMetadata) == false) {
			nextTarget.clear();
		}
		if(nextTarget.size() == 0) {
			updateBlockInfo();
		}
		// System.out.println("onTickInGame");
	}

	private void startBreak() {
		isValidStatus();
		isValidMode();

		if(isValidTarget(targetBlockId, targetBlockMetadata) == false) {
			nextTarget.clear();
			return;
		}

		vectors = getVector(sideHit, StoneBreaker.config.mode);
		nextTarget.addAll(getNextTarget(blockCoord, vectors));
		if(StoneBreaker.config.mode == EnumMode.tunnel) {
			nextTarget.add(blockCoord.addVector(Coord.up));
			nextTarget.add(blockCoord.addVector(Coord.up).addVector(Coord.up));
		}
		if(StoneBreaker.config.mode == EnumMode.tunnel || StoneBreaker.config.mode == EnumMode.upstair || StoneBreaker.config.mode == EnumMode.downstair) {
			nextTarget.add(blockCoord.addVector(Coord.up));
			nextTarget.add(blockCoord.addVector(Coord.up).addVector(Coord.up));
			nextTarget.add(blockCoord.addVector(Coord.up).addVector(Coord.up).addVector(Coord.up));
			//nextTarget.addAll(getNextTarget(blockCoord.addVector(Coord.up), vectors));
			//nextTarget.addAll(getNextTarget(blockCoord.addVector(Coord.up).addVector(Coord.up), vectors));
		}
	}

	private Set<Coord> getVector(int sideHit, EnumMode mode) {
		Set<Coord> set = new LinkedHashSet();
		switch(mode) {
		case off:
			break;
		case line:
		case tunnel:
			set.add(Coord.sideToCoord[sideHit]);
			break;
		case upstair:
		{
			Coord c = Coord.up.addVector(Coord.sideToCoord[sideHit]);
			set.add(c);
			break;
		}
		case downstair:
		{
			Coord c = Coord.down.addVector(Coord.sideToCoord[sideHit]);
			set.add(c);
			break;
		}
		case upper:
		{
			Coord c = Coord.sideToCoord[sideHit];
			if(c == Coord.down) {
				break;
			}

			if(c == Coord.west) {
				set.add(Coord.south);set.add(Coord.north);
				set.add(Coord.west);set.add(Coord.sw);set.add(Coord.nw);
				set.add(Coord.uw);set.add(Coord.usw);set.add(Coord.unw);
				set.add(Coord.us);set.add(Coord.up);set.add(Coord.un);
			}
			if(c == Coord.east) {
				set.add(Coord.south);set.add(Coord.north);
				set.add(Coord.east);set.add(Coord.se);set.add(Coord.ne);
				set.add(Coord.ue);set.add(Coord.use);set.add(Coord.une);
				set.add(Coord.us);set.add(Coord.up);set.add(Coord.un);
			}
			if(c == Coord.south) {
				set.add(Coord.east);set.add(Coord.west);
				set.add(Coord.south);set.add(Coord.sw);set.add(Coord.se);
				set.add(Coord.use);set.add(Coord.us);set.add(Coord.usw);
				set.add(Coord.ue);set.add(Coord.up);set.add(Coord.uw);
			}
			if(c == Coord.north) {
				set.add(Coord.east);set.add(Coord.west);
				set.add(Coord.north);set.add(Coord.ne);set.add(Coord.nw);
				set.add(Coord.ue);set.add(Coord.up);set.add(Coord.uw);
				set.add(Coord.une);set.add(Coord.un);set.add(Coord.unw);
			}
			if(c == Coord.up) {
				set.add(Coord.east);set.add(Coord.ne);
				set.add(Coord.north);set.add(Coord.nw);
				set.add(Coord.west);set.add(Coord.sw);
				set.add(Coord.south);set.add(Coord.se);
				set.add(Coord.use);set.add(Coord.us);set.add(Coord.usw);
				set.add(Coord.ue);set.add(Coord.up);set.add(Coord.uw);
				set.add(Coord.une);set.add(Coord.un);set.add(Coord.unw);
			}
			break;
		}
		case under:
		{
			Coord c = Coord.sideToCoord[sideHit];
			if(c == Coord.up) {
				break;
			}

			if(c == Coord.west) {
				set.add(Coord.south);set.add(Coord.north);
				set.add(Coord.west);set.add(Coord.sw);set.add(Coord.nw);
				set.add(Coord.dw);set.add(Coord.dsw);set.add(Coord.dnw);
				set.add(Coord.ds);set.add(Coord.down);set.add(Coord.dn);
			}
			if(c == Coord.east) {
				set.add(Coord.south);set.add(Coord.north);
				set.add(Coord.east);set.add(Coord.se);set.add(Coord.ne);
				set.add(Coord.de);set.add(Coord.dse);set.add(Coord.dne);
				set.add(Coord.ds);set.add(Coord.down);set.add(Coord.dn);
			}
			if(c == Coord.south) {
				set.add(Coord.east);set.add(Coord.west);
				set.add(Coord.south);set.add(Coord.sw);set.add(Coord.se);
				set.add(Coord.dse);set.add(Coord.ds);set.add(Coord.dsw);
				set.add(Coord.de);set.add(Coord.down);set.add(Coord.dw);
			}
			if(c == Coord.north) {
				set.add(Coord.east);set.add(Coord.west);
				set.add(Coord.north);set.add(Coord.ne);set.add(Coord.nw);
				set.add(Coord.de);set.add(Coord.down);set.add(Coord.dw);
				set.add(Coord.dne);set.add(Coord.dn);set.add(Coord.dnw);
			}
			if(c == Coord.down) {
				set.add(Coord.east);set.add(Coord.ne);
				set.add(Coord.north);set.add(Coord.nw);
				set.add(Coord.west);set.add(Coord.sw);
				set.add(Coord.south);set.add(Coord.se);
				set.add(Coord.dse);set.add(Coord.ds);set.add(Coord.dsw);
				set.add(Coord.de);set.add(Coord.down);set.add(Coord.dw);
				set.add(Coord.dne);set.add(Coord.dn);set.add(Coord.dnw);
			}
			break;
		}
		case horizontal:
			set.add(Coord.north);set.add(Coord.ne);
			set.add(Coord.east);set.add(Coord.se);
			set.add(Coord.west);set.add(Coord.sw);
			set.add(Coord.south);set.add(Coord.nw);
			break;
		case vertical:
		{
			Coord c = Coord.sideToCoord[sideHit];
			if(c == Coord.up || c == Coord.down) {
				set.add(c);
			}
			break;
		}
		case all:
			set.add(Coord.east);set.add(Coord.ne);
			set.add(Coord.north);set.add(Coord.nw);
			set.add(Coord.west);set.add(Coord.sw);
			set.add(Coord.south);set.add(Coord.se);
			set.add(Coord.use);set.add(Coord.us);set.add(Coord.usw);
			set.add(Coord.ue);set.add(Coord.up);set.add(Coord.uw);
			set.add(Coord.une);set.add(Coord.un);set.add(Coord.unw);
			set.add(Coord.dse);set.add(Coord.ds);set.add(Coord.dsw);
			set.add(Coord.de);set.add(Coord.down);set.add(Coord.dw);
			set.add(Coord.dne);set.add(Coord.dn);set.add(Coord.dnw);
			break;
		}
		return set;
	}

	private Set<Coord> getNextTarget(Coord blockCoord,Set<Coord> vec) {
		Set<Coord> set = new LinkedHashSet();
		for(Coord direction : vec) {
			set.add(blockCoord.addVector(direction));
		}
		return set;
	}

	private boolean isValidMode() {
		return true;
	}

	private boolean isValidStatus() {
		if(StoneBreaker.config.break_limit > 0 && count >= StoneBreaker.config.break_limit) {
			return false;
		}
		return true;
	}

	private boolean isValidTool(int blockId, int metadata) {

		Minecraft mc = Minecraft.getMinecraft();

		// air
		if(blockId == 0) return false;
		// bedrock
		if(blockId == Block.bedrock.blockID) return false;

		Block block = Block.blocksList[blockId];
		if(block == null) return false;
		// liquid or air
		if(block.blockMaterial.isLiquid() || block.blockMaterial == Material.air) return false;

		//int metadata = b.metadata == -1 ? 0 : b.metadata;

		// this is not a target block type

		if(targetBlockId != blockId) return false;

		ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
		// held no item
		if(itemStack == null) return false;

		Item item = Item.itemsList[itemStack.itemID];
		String itemName = Item.itemsList[itemStack.itemID].getClass().getName();
		// if tool or shears or specific item, check effective
		if(block.blockMaterial == Material.grass
				|| block.blockMaterial == Material.ground
				|| block.blockMaterial == Material.wood
				|| block.blockMaterial == Material.rock
				|| block.blockMaterial == Material.iron
				|| block.blockMaterial == Material.anvil
				|| block.blockMaterial == Material.clay
				|| block.blockMaterial == Material.pumpkin
				|| block.blockMaterial == Material.web
				|| block.blockMaterial == Material.sand)
		{
			if(item instanceof ItemTool || item instanceof ItemShears || StoneBreaker.config.tools.contains(itemName)) {
				if(StoneBreaker.config.effective_tool_only && item.getStrVsBlock(itemStack, block, metadata) <= 1.0F) {
					return false;
				}
			} else {
				return false;
			}
		}
		return true;
	}

	private boolean isValidTarget(int blockId, int metadata) {
		for(int[] pair : StoneBreaker.config.blocks) {
			if(pair[0] == blockId) {
				if(pair[1] == OreDictionary.WILDCARD_VALUE || pair[1] == metadata) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean continueBreak() {
		for(int i = 0; i < 10; ++i) {
			Coord target = nextTarget.poll();
			if(target == null) {
				return false;
			}
			if(isValidStatus() == false) {
				nextTarget.clear();
				return false;
			}
			if(breakBlock(target)) {
				count++;
				nextTarget.addAll(getNextTarget(target, vectors));
			}
		}
		return true;
	}

	// true if break success
	private boolean breakBlock(Coord pos) {

		int x = pos.x;
		int y = pos.y;
		int z = pos.z;

		// height limit
		if(y < 1 || 255 < y) return false;

		if(StoneBreaker.config.distance_limit > 0) {
			if(Math.abs(blockCoord.x - x) + Math.abs(blockCoord.y - y) + Math.abs(blockCoord.z - z) > StoneBreaker.config.distance_limit) {
				return false;
			}
		}
		if(StoneBreaker.config.virtical_distance_limit > 0) {
			if(Math.abs(blockCoord.y - y) > StoneBreaker.config.virtical_distance_limit) {
				return false;
			}
		}
		Minecraft mc = Minecraft.getMinecraft();

		int blockId = mc.theWorld.getBlockId(x, y, z);
		int metadata = mc.theWorld.getBlockMetadata(x, y, z);
		if(isValidTarget(blockId, metadata) == false) return false;
		// air
		if(blockId == 0) return false;
		// bedrock
		if(blockId == Block.bedrock.blockID) return false;

		Block block = Block.blocksList[blockId];
		if(block == null) return false;
		// liquid or air
		if(block.blockMaterial.isLiquid() || block.blockMaterial == Material.air) return false;


		// this is not a target block type
		if(targetBlockId == Block.grass.blockID && blockId == Block.dirt.blockID) {

		}
		else if(targetBlockId == Block.dirt.blockID && blockId == Block.grass.blockID) {

		}
		else if(targetBlockId == Block.oreRedstone.blockID && blockId == Block.oreRedstoneGlowing.blockID) {

		}
		else if(targetBlockId == Block.oreRedstoneGlowing.blockID && blockId == Block.oreRedstone.blockID) {

		}
		else if(targetBlockId != blockId) {
			return false;
		}

		if(isValidTool(blockId, metadata) == false) {
			return false;
		}

		ItemStack itemStack = mc.thePlayer.getCurrentEquippedItem();
		// held no item
		if(itemStack == null) return false;

		block.onBlockDestroyedByPlayer(mc.theWorld, x, y, z, blockId);
		mc.thePlayer.sendQueue.addToSendQueue(new Packet14BlockDig(2, x, y, z, 0));
		mc.theWorld.playAuxSFX(2001, x, y, z, block.blockID + (mc.theWorld.getBlockMetadata(x, y, z) << 12));
		boolean flag = mc.theWorld.setBlock(x, y, z, 0, 0, 3);

		if (block != null && flag) {
			block.removeBlockByPlayer(mc.theWorld, mc.thePlayer, x, y, z);
			mc.theWorld.markBlockForUpdate(x, y, z);
		}

		itemStack.onBlockDestroyed(mc.theWorld, blockId, x, y, z, mc.thePlayer);
		sendPacket(EnumCommand.BREAK, pos);
		if (itemStack.stackSize == 0) {
			// itemstack.onItemDestroyedByUse(minecraft.thePlayer);
			mc.thePlayer.destroyCurrentEquippedItem();
			//ret = false;
		}

		boolean flag1 = mc.thePlayer.canHarvestBlock(block);

		if (flag1) {
			if(StoneBreaker.config.drop_to_player) {
				block.harvestBlock(mc.theWorld, mc.thePlayer, (int)mc.thePlayer.posX, (int)mc.thePlayer.posY, (int)mc.thePlayer.posZ, metadata);
			}
			else {
				block.harvestBlock(mc.theWorld, mc.thePlayer, x, y, z, metadata);
			}
		}

		return true;
	}

	private void sendPacket(EnumCommand command, Coord pos) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF(command.toString());
			stream.writeInt(pos.x);
			stream.writeInt(pos.y);
			stream.writeInt(pos.z);

			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = Config.channel;
			packet.data = bytes.toByteArray();
			packet.length = packet.data.length;
			Minecraft mc = Minecraft.getMinecraft();
			mc.thePlayer.sendQueue.addToSendQueue(packet);
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	void updateBlockInfo() {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.objectMouseOver == null) {
			return;
		}

		if (mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			blockCoord.x = mc.objectMouseOver.blockX;
			blockCoord.y = mc.objectMouseOver.blockY;
			blockCoord.z = mc.objectMouseOver.blockZ;
			sideHit  = mc.objectMouseOver.sideHit;
			targetBlockId = mc.theWorld.getBlockId(blockCoord.x, blockCoord.y, blockCoord.z);
			targetBlockMetadata  = mc.theWorld.getBlockMetadata(blockCoord.x, blockCoord.y, blockCoord.z);
		}
	}

	boolean isBreakBlock() {

		boolean isBreak = false;
		Minecraft mc = Minecraft.getMinecraft();
		try {
			int blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, mc.playerController, 8);
			isBreak = (blockHitWait == 5 && blockHitWait != prev_blockHitWait );
			prev_blockHitWait = blockHitWait;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return isBreak;
	}
}
