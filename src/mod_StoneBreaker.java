package net.minecraft.src;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public class mod_StoneBreaker extends BaseMod {
	class Position {
		int x;
		int y;
		int z;

		public Position(long l, long m, long n) {
			this.x = (int) l;
			this.y = (int) m;
			this.z = (int) n;
		}

		public Position subtract(Position position) {
			return new Position(position.x - x, position.y - y, position.z - z);
		}

		public Position addVector(int x2, int y2, int z2) {
			return new Position(x2 + x, y2 + y, z2 + z);
		}

	    public String toString()
	    {
	        return (new StringBuilder()).append("(").append(x).append(", ").append(y).append(", ").append(z).append(")").toString();
	    }

	    @Override
	    public int hashCode() {
	    	return 13 * 13 * x + 13 * y + z;
	    }

	    @Override
	    public boolean equals(Object obj) {
	    	if(obj == null) return false;
	    	if(obj instanceof Position) {
	    		Position pos = (Position)obj;
	    		if(x == pos.x && y == pos.y && z == pos.z) {
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}
	public static KeyBinding key_stonebreak = null;
	public static int prev_i;
	public static int prev_j;
	public static int prev_k;
	public static int sideHit;
	public static int blockId;
	public static int metadata;
	public static int prev_blockHitWait;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();

	public static final int mode_off = 0;
	public static final int mode_register = 1;
	public static final int mode_unregister = 2;
	public static final int mode_line = 3;
	public static final int mode_tunnel = 4;
	public static final int mode_upstair = 5;
	public static final int mode_downstair = 6;
	public static final int mode_upper = 7;
	public static final int mode_under = 8;
	public static final int mode_horizontal = 9;
	public static final int mode_vertical = 10;
	public static final int mode_all = 11;
	public static final int mode_max = 12;

	@MLProp(min = 0, max = mode_max)
	public static int mode = mode_off;


	@MLProp(info = "separate by ','")
	public static String blockIDs = "1";

	public static Set<Integer> targetIDs = new LinkedHashSet();

	public static boolean debugmode = false;

	@MLProp(info = "toggle mode key(default:50 = 'M')")
	public static int mode_key = 50;

	public static int key_push_times = 0;

	public static boolean bInit = false;

	public static int breakcount = 0;

	@MLProp(info = "maximum number of block break (0 = unlimited)")
	public static int breaklimit = 0;
	@MLProp(info = "maximum distance of break from player (0 = unlimited)")
	public static int distancelimit = 0;
	@MLProp(info = "virtical distance limit")
	public static boolean virtical_distancelimit = true;

	@MLProp(info = "drop blocks near by player")
	public static boolean droptoplayer = true;

	public static boolean bObfuscated = true;

	//@MLProp
	//public static int break_blocks_per_tick = 16;

	@Override
	public String getVersion() {
		return "[1.2.4] StoneBreaker 0.0.5";
	}

	@Override
	public void load() {
		String str = blockIDs;
		String[] tokens = str.split(",");
		for(String token : tokens) {
			targetIDs.add(Integer.parseInt(token.trim()));
		}
		System.out.print("StoneBreaker target = ");
		System.out.println(targetIDs);
		ModLoader.setInGameHook(this, true, false);

	}

	public void printTargetIDs(Minecraft minecraft) {
		String s = "StoneBreaker target = ";
		s += targetIDs;
		minecraft.ingameGUI.addChatMessage(s);
	}
	public void printMode(Minecraft minecraft) {
		switch(mode) {
		case mode_off:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : OFF");
			break;
		case mode_line:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : LINE");
			break;
		case mode_tunnel:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : TUNNEL");
			break;
		case mode_upstair:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UPSTAIR");
			break;
		case mode_downstair:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : DOWNSTAIR");
			break;
		case mode_upper:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UPPER");
			break;
		case mode_under:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UNDER");
			break;
		case mode_horizontal:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : HORIZONTAL");
			break;
		case mode_vertical:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : VERTICAL");
			break;
		case mode_register:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : REGISTER");
			break;
		case mode_unregister:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UNREGISTER");
			break;
		case mode_all:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : ALL");
			break;
		}

	}

	@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		if(minecraft.theWorld.isRemote) {
			return false;
		}
		if(Keyboard.isKeyDown(mode_key)) {
			key_push_times++;
		} else if(key_push_times > 0) {
			mode = (mode + 1) % mode_max;
			key_push_times = 0;
			printMode(minecraft);
		}

		if(bInit == false) {
			printMode(minecraft);
			printTargetIDs(minecraft);
			bInit = true;
		}


		boolean breakflag = false;

		if(bObfuscated) {
			try {
				if(minecraft.playerController.isNotCreative()) {
					// blockHitWait obfuscate i
					int blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "i");
					if(blockHitWait == 5 && blockHitWait != prev_blockHitWait) {
						breakflag = true;
					}
					prev_blockHitWait = blockHitWait;
				}
				else {
					// blockHitWait obfuscate i
					int blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerCreative.class, minecraft.playerController, "c");
					if(blockHitWait != prev_blockHitWait) {
						breakflag = true;
					}
					prev_blockHitWait = blockHitWait;
				}
			} catch (IllegalArgumentException e) {
				bObfuscated = false;
				e.printStackTrace();
			} catch (SecurityException e) {
				bObfuscated = false;
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				bObfuscated = false;
				e.printStackTrace();
			}
		} else {
			try {
				if(minecraft.playerController.isNotCreative()) {
					// blockHitWait obfuscate i
					int blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerSP.class, minecraft.playerController, "blockHitWait");
					if(blockHitWait == 5 && blockHitWait != prev_blockHitWait) {
						breakflag = true;
					}
					prev_blockHitWait = blockHitWait;
				}
				else {
					// blockHitWait obfuscate i
					int blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerCreative.class, minecraft.playerController, "field_35647_c");
					if(blockHitWait != prev_blockHitWait) {
						breakflag = true;
					}
					prev_blockHitWait = blockHitWait;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}


		if(breakflag) {
			switch(mode) {
			case mode_register:
				if(targetIDs.contains(blockId) == false) {
					targetIDs.add(blockId);
					String s = "StoneBreaker add target : ";
					s += blockId;
					minecraft.ingameGUI.addChatMessage(s);
					printTargetIDs(minecraft);
				}
				break;
			case mode_unregister:
				if(targetIDs.contains(blockId)) {
					targetIDs.remove(blockId);
					String s = "StoneBreaker remove target : ";
					s += blockId;
					minecraft.ingameGUI.addChatMessage(s);
					printTargetIDs(minecraft);
				}
				break;
			default:
				if(targetIDs.contains(blockId)) {
					startBreak(minecraft);
				} else {
					if(debugmode) {
						System.out.print("BlockId ");
						System.out.print(blockId);
						System.out.print(" not in targetIDs ");
						System.out.println(targetIDs);
					}
				}
			}
		}

		if(positions.size() > 0) {
			continueBreak(minecraft);
			return true;
		}

		//if(Mouse.isButtonDown(0) == false) {
		//	return true;
		//}
		if (minecraft.objectMouseOver == null) {
			return true;
		}

		if (minecraft.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			prev_i = minecraft.objectMouseOver.blockX;
			prev_j = minecraft.objectMouseOver.blockY;
			prev_k = minecraft.objectMouseOver.blockZ;
			sideHit = minecraft.objectMouseOver.sideHit;
			blockId = minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k);
			Block block = Block.blocksList[blockId];
			metadata = minecraft.theWorld.getBlockMetadata(prev_i, prev_j, prev_k);
			Material material = minecraft.theWorld.getBlockMaterial(prev_i, prev_j, prev_k);
			if(breakflag) {
				if(debugmode) System.out.println(getDirection(minecraft));
			}
			return true;
		}

		return true;
	}

	private void continueBreak(Minecraft minecraft) {
		if(debugmode) {
			System.out.print("continueBreak start ");
			System.out.println(positions);
		}
		int i = 0;
		Set<Position> oldPositions = new LinkedHashSet();
		Set<Position> newPositions = new LinkedHashSet();
		oldPositions.addAll(positions);


		int breakOnce = 1;

		// ��x�ɉ󂷂̂͂P�U�܂łƂ���
		if(oldPositions.size() < breakOnce) {
			int j = 0;
			// �P�U�ڈȍ~�͎���
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					newPositions.add(position);
				}
				j++;
			}

			j = 0;
			// ���Ӄu���b�N������ɗ\��
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}

			j = 0;
			// �\��ς݃u���b�N��j��
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				// �c�[���j���͏I��
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
				// �ꊇ�j�����͏I��
				if(breaklimit > 0 && breakcount >= breaklimit) {
					positions.clear();
					breakcount = 0;
					return;
				}
			}
		}
		// ���ׂĉ�
		else {
			// ���Ӄu���b�N������ɗ\��
			for(Position position : oldPositions) {
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}
			// �\��ς݃u���b�N��j��
			for(Position position : oldPositions) {
				// �c�[���j���͏I��
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
				// �ꊇ�j�����͏I��
				if(breaklimit > 0 && breakcount >= breaklimit) {
					positions.clear();
					breakcount = 0;
					return;
				}
			}
		}

		positions = newPositions;
		if(positions.size() > 1) {
			if(debugmode) {
				System.out.print("continueBreak end");
				System.out.println(positions);
			}

		}
		if(debugmode) {
			System.out.print("continueBreak end");
			System.out.println(positions);
		}
	}

	// true : continue break
	// false: stop break
	private boolean breakBlock(Minecraft minecraft, Position position) {
		if(debugmode) {
			System.out.print("breakBlock ");
			System.out.print(position.toString());
			System.out.println(" start");
		}

		if(position.y < 1 || position.y > 255) {
			return true;
		}


        ItemStack itemstack = minecraft.thePlayer.getCurrentEquippedItem();
        if (itemstack == null)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack == null)");
        	return false;
        }
        if (itemstack.stackSize == 0)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack.stackSize == 0)");
        	return false;
        }
		int id = minecraft.theWorld.getBlockId((int)position.x, (int)position.y, (int)position.z);
		boolean bSame = false;
		if(id == blockId) {
			bSame = true;
		}
		if(id == Block.dirt.blockID && blockId == Block.grass.blockID) {
			bSame = true;
		}
		if(blockId == Block.dirt.blockID && id == Block.grass.blockID) {
			bSame = true;
		}
		if(bSame == false) {
			if(debugmode) System.out.println("breakBlock skip(BlockId)");
			return true;
		}

		// ignore liquid and air
		Block block = Block.blocksList[id];
		if(block.blockMaterial.isLiquid() || block.blockMaterial == Material.air) {
			if(debugmode) System.out.println("breakBlock skip(blockMaterial)");
			return true;
		}

		// ignore bedrock
		if(blockId == Block.bedrock.blockID) {
			return true;
		}

		// item require pickaxe or axe or shovel or shears
		if(Item.itemsList[itemstack.itemID] instanceof ItemTool) {

		} else if(itemstack.itemID == Item.shears.shiftedIndex) {

		} else {
			if(debugmode) System.out.println("breakBlock skip(Item not ItemTool)");
			return false;
		}
		//if(itemstack.getStrVsBlock(block) <= 1.0F) {
		//	System.out.println("breakBlock skip(Str)");
		//	return false;
		//}
        int i = minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z);
        boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, 0);

        if (block != null && flag) {
        	block.onBlockDestroyedByPlayer(minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
			breakcount++;
        }
        itemstack = minecraft.thePlayer.getCurrentEquippedItem();
        boolean flag1 = minecraft.thePlayer.canHarvestBlock(block);

        boolean ret = true;
        if (itemstack != null)
        {
            itemstack.onDestroyBlock(id, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);

            if (itemstack.stackSize == 0)
            {
                itemstack.onItemDestroyedByUse(minecraft.thePlayer);
                minecraft.thePlayer.destroyCurrentEquippedItem();
                ret = false;
            }
        }

        if (flag && flag1)
        {
    		if(droptoplayer) {
        		block.harvestBlock(minecraft.theWorld, minecraft.thePlayer, (int)minecraft.thePlayer.posX, (int)minecraft.thePlayer.posY, (int)minecraft.thePlayer.posZ, i);
    		} else {
        		block.harvestBlock(minecraft.theWorld, minecraft.thePlayer, (int)position.x, (int)position.y, (int)position.z, i);
    		}
        }

		if(debugmode) System.out.println("breakBlock end");
		return ret;

	}

	public Position getDirection(Minecraft minecraft) {
		/*
		 * 2 = (0, 0, 1)
		 * 3 = (0, 0, -1)
		 * 4 = (1, 0, 0)
		 * 5 = (-1, 0, 0)
		 */
		switch(sideHit) {
		case 2:
			return new Position(0, 0, 1);
		case 3:
			return new Position(0, 0, -1);
		case 4:
			return new Position(1, 0, 0);
		case 5:
			return new Position(-1, 0, 0);
		}
		return new Position(0, 0, 0);
	}

	public Set<Position> getBackDirections(Minecraft minecraft) {
		Position v = getDirection(minecraft);

		Set<Position> set = new LinkedHashSet();
		if(v.x == 1) {
			set.add(new Position(-1, 0, 1));set.add(new Position(-1, 0, 0));set.add(new Position(-1, 0, -1));
			set.add(new Position(-1, 1, 1));set.add(new Position(-1, 1, 0));set.add(new Position(-1, 1, -1));
			set.add(new Position(-1, -1, 1));set.add(new Position(-1, -1, 0));set.add(new Position(-1, -1, -1));
		} else if(v.x == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(1, 0, 0));set.add(new Position(1, 0, -1));
			set.add(new Position(1, 1, 1));set.add(new Position(1, 1, 0));set.add(new Position(1, 1, -1));
			set.add(new Position(1, -1, 1));set.add(new Position(1, -1, 0));set.add(new Position(1, -1, -1));
		} else if(v.z == 1) {
			set.add(new Position(1, 0, -1));set.add(new Position(0, 0, -1));set.add(new Position(-1, 0, -1));
			set.add(new Position(1, 1, -1));set.add(new Position(0, 1, -1));set.add(new Position(-1, 1, -1));
			set.add(new Position(1, -1, -1));set.add(new Position(0, -1, -1));set.add(new Position(-1, -1, -1));
		} else if(v.z == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(0, 0, 1));set.add(new Position(-1, 0, 1));
			set.add(new Position(1, 1, 1));set.add(new Position(0, 1, 1));set.add(new Position(-1, 1, 1));
			set.add(new Position(1, -1, 1));set.add(new Position(0, -1, 1));set.add(new Position(-1, -1, 1));
		}
		return set;
	}

	public void startBreak(Minecraft minecraft) {
		if(debugmode) System.out.println("startBreak start");

		Set<Position> backDirections = getBackDirections(minecraft);
		positions.clear();
		vectors.clear();
		Position v;

		switch(mode) {
		case mode_off:
			break;
		case mode_line:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case mode_tunnel:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case mode_downstair:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(minecraft);
				v.y = -1;
				vectors.add(v);
				break;
			}
			break;
		case mode_upstair:
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			positions.add(new Position(prev_i, prev_j + 2, prev_k));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				v = getDirection(minecraft);
				v.y = 1;
				vectors.add(v);
				break;
			}
			break;
		case mode_upper:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				vectors.removeAll(backDirections);
				break;
			}

			break;
		case mode_under:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			switch(sideHit) {
			case 0:
			case 1:
				vectors.clear();
				break;
			default:
				vectors.removeAll(backDirections);
				break;
			}
			break;
		case mode_horizontal:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 0));vectors.add(new Position(-1, 0, 0));
			vectors.add(new Position(0, 0, 1));vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, -1));
			break;
		case mode_vertical:
			positions.add(new Position(prev_i, prev_j, prev_k));

			/*
			 * 2 = (0, 0, 1)
			 * 3 = (0, 0, -1)
			 * 4 = (1, 0, 0)
			 * 5 = (-1, 0, 0)
			 */
			switch(sideHit) {
			case 0:
				vectors.add(new Position(0, 1, 0));
			case 1:
				vectors.add(new Position(0, -1, 0));
				break;
			}
			break;
		case mode_all:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			break;
		}
		if(debugmode) {
			System.out.print("positions ");
			System.out.println(positions);
			System.out.print("vectors ");
			System.out.println(vectors);
			System.out.print(minecraft.theWorld.getBlockId(prev_i+1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i-1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k+1));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k-1));
			System.out.println();
		}

		if(debugmode) System.out.println("startBreak end");
	}

	public Set<Position> addNextBreakBlocks(Minecraft minecraft, Position position) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : vectors) {
			if(vector.x == 0 && vector.y == 0 && vector.z == 0) {
				continue;
			}

			Position pos = position.addVector(vector.x, vector.y, vector.z);

			if(distancelimit > 0) {
				if(Math.abs((int)Math.round(minecraft.thePlayer.posX) - pos.x) > distancelimit) {
					continue;
				}
				if(virtical_distancelimit && Math.abs((int)Math.round(minecraft.thePlayer.posY) - pos.y) > distancelimit) {
					continue;
				}
				if(Math.abs((int)Math.round(minecraft.thePlayer.posZ) - pos.z) > distancelimit) {
					continue;
				}

			}

			switch(mode) {
			case mode_under:
				if((int)Math.round(minecraft.thePlayer.posX) == position.x && (int)Math.round(minecraft.thePlayer.posZ) == position.z) {
					return newPositions;
				}
				break;
			case mode_vertical:
				if((int)Math.round(minecraft.thePlayer.posX) == position.x && (int)Math.round(minecraft.thePlayer.posZ) == position.z && (int)Math.round(minecraft.thePlayer.posY) > position.y) {
					return newPositions;
				}
				break;
			case mode_all:
				if((int)Math.round(minecraft.thePlayer.posX) == position.x && (int)Math.round(minecraft.thePlayer.posZ) == position.z && (int)Math.round(minecraft.thePlayer.posY) > position.y) {
					return newPositions;
				}
				break;
			}
			int id = minecraft.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			boolean bSame = false;
			if(id == blockId) {
				bSame = true;
			}
			if(id == Block.dirt.blockID && blockId == Block.grass.blockID) {
				bSame = true;
			}
			if(blockId == Block.dirt.blockID && id == Block.grass.blockID) {
				bSame = true;
			}

			if(bSame) {
				if(positions.contains(pos) == false && newPositions.contains(pos) == false) {
					if(debugmode) {
						System.out.print("addNextBreakBlocks ");
						System.out.println(pos.toString());
					}
					newPositions.add(pos);
				}
			}
		}

		return newPositions;
	}

}
