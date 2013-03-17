package stonebreaker;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.EnumMovingObjectType;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class RegisterKeyHandler extends KeyHandler {

	static KeyBinding registerKeyBinding = new KeyBinding("StoneBreaker.Register", Keyboard.KEY_R);

	public RegisterKeyHandler() {
		super(new KeyBinding[] { registerKeyBinding }, new boolean[] { false });
	}

	@Override
	public String getLabel() {
		return "StoneBreaker.Register";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		Minecraft mc = Minecraft.getMinecraft();
		if(kb != this.registerKeyBinding) return;
		if(tickEnd == false) return;
		if(mc.currentScreen != null) return;
		if(mc.ingameGUI.getChatGUI().getChatOpen()) return;
		if (mc.objectMouseOver == null) {
			return;
		}

		if (mc.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			int x = mc.objectMouseOver.blockX;
			int y = mc.objectMouseOver.blockY;
			int z = mc.objectMouseOver.blockZ;
			int sideHit  = mc.objectMouseOver.sideHit;
			int blockId = mc.theWorld.getBlockId(x, y, z);
			int metadata = mc.theWorld.getBlockMetadata(x, y, z);

			sendPacket(EnumCommand.REGISTER, blockId, metadata);
		}
	}

	private void sendPacket(EnumCommand command, int blockId, int metadata) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream stream = new DataOutputStream(bytes);
		try {
			stream.writeUTF(command.toString());
			stream.writeInt(blockId);
			stream.writeInt(metadata);

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

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
