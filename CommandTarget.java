package stonebreaker;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatMessageComponent;

public class CommandTarget extends CommandBase {

	@Override
	public String getCommandName() {
		return "sb";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {

		String username = icommandsender.getCommandSenderName();
		if (icommandsender instanceof EntityPlayerMP) {
			EntityPlayerMP player = getCommandSenderAsPlayer(icommandsender);
			if (username == null || username.isEmpty()) {
				return;
			}

			if (player == null) {
				return;
			}

			if (player.worldObj.isRemote) {
				return;
			}

			if (astring.length < 1) {
				player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("StoneBreaker : command format error"));
				return;
			}
			try {
				if (astring[0].compareToIgnoreCase("list") == 0) {
					String target = "";
					for (int[] pair : StoneBreaker.config.blocks) {
						int blockId = pair[0];
						int metadata = pair[1];
						Block block = Block.blocksList[blockId];
						if (block != null) {
							if (target.isEmpty() == false) {
								target += ", ";
							}
							target += new ItemStack(blockId, 1, metadata).getDisplayName();
						}
					}
					player.addChatMessage("StoneBreaker Target = " + target);
				}
			} catch (NumberFormatException e) {
				player.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("StoneBreaker : command format error"));
			}
		}

	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "/sb list";
	}

}
