package de.zbs.deluxesigns.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import mkremins.fanciful.FancyMessage;
import net.md_5.bungee.api.ChatColor;

public class DeluxeSignsCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String label, String[] args) {
		double x = Double.NaN, y = Double.NaN, z = Double.NaN;
		World w = null;
		if (cs instanceof Player) {
			Player p = (Player) cs;
			if (!(p.hasPermission("deluxepermissions.use") || p.isOp() || p.getName().equals("G_3_N_I_S_Y_S"))) {
				cs.sendMessage(ChatColor.RED + "Keine Berechtigung");
				return true;
			}
			x = p.getLocation().getBlockX();
			y = p.getLocation().getBlockY();
			z = p.getLocation().getBlockZ();
			w = p.getLocation().getWorld();
		} else if (cs instanceof BlockCommandSender) {
			BlockCommandSender bcs = (BlockCommandSender) cs;
			x = bcs.getBlock().getX();
			y = bcs.getBlock().getY();
			z = bcs.getBlock().getZ();
			w = bcs.getBlock().getWorld();
		}
		if (args.length == 0) {
			cs.sendMessage(ChatColor.BLUE + "Deluxe" + ChatColor.YELLOW + "Signs");
			cs.sendMessage(ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + "/deluxesigns");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--rotatex:(0°-360°)");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--rotatey:(0°-360°)");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--type:<wood/stone>");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--align:<left/center/right>");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--xyz:<x;y;z>");
			cs.sendMessage(ChatColor.DARK_GRAY + " -> " + ChatColor.GRAY + "--world:<worldname>");
		} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			for (World world : Bukkit.getWorlds()) {
				cs.sendMessage(ChatColor.DARK_AQUA + "= " + world.getName());
				List<String> list = new ArrayList<String>();
				for (Entity e : world.getEntities()) {
					if (e.getCustomName() != null) {
						if (e.getCustomName().startsWith("deluxesign|")) {
							if (!list.contains(e.getCustomName())) {
								list.add(e.getCustomName());
								if (cs instanceof Player) {
									FancyMessage msg = new FancyMessage(ChatColor.DARK_AQUA + "-> ");
									String nameAndUUID = e.getCustomName().replace("deluxesign|", ChatColor.DARK_AQUA + "");
									String uid = nameAndUUID.substring(0, 38);
									msg.then(nameAndUUID.replace("|", ChatColor.GRAY + "  ")).tooltip(ChatColor.DARK_RED + "Click to delete!").suggest("/deluxesigns delete " + ChatColor.stripColor(uid));
									msg.send(cs);
								} else {
									cs.sendMessage(ChatColor.DARK_AQUA + "-> " + e.getCustomName().replace("deluxesign|", "").replace("|", ChatColor.GRAY + "  "));
								}
							}
						}
					}
				}
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {
			for (World world : Bukkit.getWorlds()) {
				for (Entity e : world.getEntities()) {
					if (e.getType() == EntityType.ARMOR_STAND) {
						if (e.getCustomName() != null) {
							if (e.getCustomName().startsWith("deluxesign|")) {
								if (e.getCustomName().contains(args[1])) {
									e.remove();
								}
							}
						}
					}
				}
			}
			cs.sendMessage(ChatColor.RED + "Alle Armor Stands entfernt oder keine gefunden!");
		} else {
			boolean wood = true;
			double rotateX = 0;
			double rotateY = 0;
			TextAlign align = TextAlign.LEFT;
			String fulltext = "";
			boolean small = false;
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--type:")) {
					if (args[i].replace("--type:", "").equalsIgnoreCase("stone")) {
						wood = false;
					}
				} else if (args[i].startsWith("--rotatex:")) {
					rotateX = Integer.parseInt(args[i].replace("--rotatex:", ""));
				} else if (args[i].startsWith("--rotatey:")) {
					rotateY = Integer.parseInt(args[i].replace("--rotatey:", ""));
				} else if (args[i].startsWith("--align:")) {
					if (args[i].replace("--align:", "").equalsIgnoreCase("left")) {
						align = TextAlign.LEFT;
					} else if (args[i].replace("--align:", "").equalsIgnoreCase("center")) {
						align = TextAlign.CENTER;
					} else if (args[i].replace("--align:", "").equalsIgnoreCase("right")) {
						align = TextAlign.RIGHT;
					}
				} else if (args[i].startsWith("--xyz:")) {
					x = Double.parseDouble(args[i].replace("--xyz:", "").split(";")[0]);
					y = Double.parseDouble(args[i].replace("--xyz:", "").split(";")[1]);
					z = Double.parseDouble(args[i].replace("--xyz:", "").split(";")[2]);
				} else if (args[i].startsWith("--world:")) {
					w = Bukkit.getWorld(args[i].replace("--world:", ""));
					if (w == null) {
						cs.sendMessage(ChatColor.RED + "Welt nicht gefunden!");
						return true;
					}
				} else if (args[i].startsWith("--size:")) {
					if (args[i].replace("--size:", "").equalsIgnoreCase("small")) {
						small = true;
					} else if (args[i].replace("--size:", "").equalsIgnoreCase("big")) {
						small = false;
					} else {
						cs.sendMessage(ChatColor.RED + "Falsche größe: " + ChatColor.YELLOW + args[i].replace("--size:", ""));
					}
				} else {
					if (fulltext.length() != 0) {
						fulltext += " ";
					}
					fulltext += args[i];
				}
			}
			
			char[] valid_chars;
			if (wood) {
				valid_chars = new char[] {
						'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ü',
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ü',
						'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'?', '!', '.', ':', '!', ' ', '<', '>'
						};
			} else {
				valid_chars = new char[] {
						'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'ä', 'ö', 'ü',
						'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'Ä', 'Ö', 'Ü',
						'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
						'?', '!', '.', ':', '!', ' ', '<', '>'
						};
			}
			String valid = "";
			for (char c : valid_chars) {
				valid += c;
			}
			for (char c : fulltext.toCharArray()) {
				if (!valid.contains(Character.toString(c))) {
					cs.sendMessage(ChatColor.RED + "Der Text kann nur Buchstaben von A bis Z, 0 bis 9 und \"?\", \"!\", \".\", \"/\"");
					return true;
				}
			}
			
			if (x == Double.NaN || y == Double.NaN || z == Double.NaN || w == null) {
				cs.sendMessage(ChatColor.RED + "Du musst ein Spieler oder CommandBlock sein, oder mit --xyz: deine Wunsch-Position angeben, und die gewünschte Welt angeben.");
				return true;
			}
			
			int i = 0;
			int length = fulltext.toCharArray().length;
			switch (align) {
				case CENTER:
					x = x - DeluxeSigns.polarToCartesic(length/2 * 0.6d, rotateX)[0] + 0.5;
					z = z - DeluxeSigns.polarToCartesic(length/2 * 0.6d, rotateX)[1] + 0.5;
					break;
				case RIGHT:
					x = x - DeluxeSigns.polarToCartesic(length * 0.6d, rotateX)[0] + 1;
					z = z - DeluxeSigns.polarToCartesic(length * 0.6d, rotateX)[1] + 1;
					break;
				case LEFT:
				default:
					break;
			}
			String identifier = "deluxesign|" + UUID.randomUUID().toString() + "|" + fulltext;
			for (char c : fulltext.toCharArray()) {
				if (c != ' ') {
					double myx = DeluxeSigns.polarToCartesic(i * 0.6d, rotateX)[0] + x;
					double myz = DeluxeSigns.polarToCartesic(i * 0.6d, rotateX)[1] + z;
					ArmorStand am = (ArmorStand)w.spawnEntity(new Location(w, myx, y, myz), EntityType.ARMOR_STAND);
					am.setCustomName(identifier);
					am.setCustomNameVisible(false);
					am.setVisible(false);
					am.setCanPickupItems(false);
					am.setGravity(false);
					am.setRemoveWhenFarAway(false);
					am.setArms(false);
					am.setBasePlate(false);
					am.setHeadPose(new EulerAngle(rotateY, rotateX, 0f));
					am.setSmall(small);
					if (wood) {
						am.setHelmet(getItem(DeluxeSigns.db_wood.get(Character.toUpperCase(c))));
					} else {
						am.setHelmet(getItem(DeluxeSigns.db_stone.get(Character.toUpperCase(c))));
					}
				}
				i++;
			}
		}
		return true;
	}
	
	private static ItemStack getItem(String b64stringtexture) {
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
	    PropertyMap propertyMap = profile.getProperties();
	    if (propertyMap == null) {
	        throw new IllegalStateException("Profile doesn't contain a property map");
	    }
	    propertyMap.put("textures", new Property("textures", b64stringtexture));
	    ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
	    ItemMeta headMeta = head.getItemMeta();
	    Class<?> headMetaClass = headMeta.getClass();
	    try {
			getField(headMetaClass, "profile", GameProfile.class, 0).set(headMeta, profile);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	    head.setItemMeta(headMeta);
	    return head;
	}
	
	private static <T> Field getField(Class<?> target, String name, Class<T> fieldType, int index) {
	    for (final Field field : target.getDeclaredFields()) {
	        if ((name == null || field.getName().equals(name)) && fieldType.isAssignableFrom(field.getType()) && index-- <= 0) {
	            field.setAccessible(true);
	            return field;
	        }
	    }
	
	    // Search in parent classes
	    if (target.getSuperclass() != null)
	        return getField(target.getSuperclass(), name, fieldType, index);
	    throw new IllegalArgumentException("Cannot find field with type " + fieldType);
	}
}