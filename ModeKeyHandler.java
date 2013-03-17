package stonebreaker;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;


import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;

public class ModeKeyHandler extends KeyHandler {

	static KeyBinding modeKeyBinding = new KeyBinding("StoneBreaker.Mode", Keyboard.KEY_M);

	public ModeKeyHandler() {
		super(new KeyBinding[] { modeKeyBinding }, new boolean[] { false });
	}
	@Override
	public String getLabel() {
		return "StoneBreaker.Mode";
	}

	@Override
	public void keyDown(EnumSet<TickType> types, KeyBinding kb,
			boolean tickEnd, boolean isRepeat) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {
		Minecraft mc = Minecraft.getMinecraft();
		if(kb != this.modeKeyBinding) return;
		if(tickEnd == false) return;
		if(mc.currentScreen != null) return;
		if(mc.ingameGUI.getChatGUI().getChatOpen()) return;

		StoneBreaker.config.toggleMode();
		mc.ingameGUI.getChatGUI().printChatMessage("StoneBreaker Mode = " + StoneBreaker.config.getMode().toString());
	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.CLIENT);
	}

}
