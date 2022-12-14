package hu.parancsmester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class main extends JavaPlugin implements Listener {
	private ArrayList<Player> frozenPlayers = new ArrayList<>();
	private ArrayList<Player> reverseDamagePlayers = new ArrayList<>();
	private ArrayList<Player> randomEffectPlayers = new ArrayList<>();
	private HashMap<Player, Player> rotatingPlayers = new HashMap<>();
	private List<String> arguments = Arrays.asList("freeze", "burn", "fall", "reversedamage", "sculk", "skeleton", "snow", "levitate", "invrandom", "invdamage", "cube", "rotate", "randomeffect");
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(frozenPlayers.contains(p)) {
			e.setCancelled(true);
		} else if(rotatingPlayers.containsKey(p)) {
			Location loc = rotatingPlayers.get(p).getLocation();
			loc.setPitch(p.getLocation().getPitch());
			loc.setYaw(p.getLocation().getYaw());
			rotatingPlayers.get(p).teleport(loc);
		}
	}

	@EventHandler
	public void onPlayerDamagesOtherEntity(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player) {
			Player p = (Player) e.getDamager();
			if(reverseDamagePlayers.contains(p)) {
				e.setCancelled(true);
				p.damage(e.getDamage());
			}
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(label.equalsIgnoreCase("troll")) {
			if(sender instanceof Player) {
				Player p = (Player) sender;
				if(args.length > 1) {
					final Player target = Bukkit.getPlayer(args[1]);
					if(target == null) {
						p.sendMessage("??4Helytelen j??t??kosn??v!");
						return true;
					}
					switch(args[0]) {
						case "freeze":
							if(frozenPlayers.contains(target)) {
								frozenPlayers.remove(target);
								p.sendMessage("??eLevetted a fagy??st ??o"+target.getName()+"??r??e j??t??kosr??l!");
							} else {
								frozenPlayers.add(target);
								p.sendMessage("??eMegfagyasztottad ??o"+target.getName()+"??r??e j??t??kost!");
							}
							break;
						case "burn":
							try {
								int sec = Integer.parseInt(args[2]);
								if(sec <= 0) {
									p.sendMessage("??e"+sec+" m??sodpercre minek akarod felgy??jtani? :D");
								} else {
									p.setFireTicks(sec*20);
									p.sendMessage("??eFelgy??jtottad ??o"+target.getName()+"??r??e j??t??kost ??o"+sec+"??r??e m??sodpercre!");
								}
							} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
								p.sendMessage("??4??rv??nytelen sz??m!");
								return true;
							}
							break;
						case "fall":
							Location loc = target.getLocation();
							World world = target.getWorld();
							for(int loopY = loc.getBlockY()+1; loopY > -65; loopY--) {
								for(int loopX = loc.getBlockX()-1; loopX < loc.getBlockX()+2; loopX++) {
									for(int loopZ = loc.getBlockZ()-1; loopZ < loc.getBlockZ()+2; loopZ++) {
										world.getBlockAt(loopX, loopY, loopZ).setType(Material.AIR);
									}
								}
							}
							p.sendMessage("??eMegny??lt a f??ld ??o"+target.getName()+"??r??e j??t??kos alatt!");
							break;
						case "reversedamage":
							if(reverseDamagePlayers.contains(target)) {
								reverseDamagePlayers.remove(target);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l nem fogja ??nmag??t sebezni, ha sebez egy entit??st!");
							} else {
								reverseDamagePlayers.add(target);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l ??nmag??t fogja sebezni, ha sebez egy entit??st!");
							}
							break;
						case "sculk":
							target.playSound(target, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1000, 1);
							target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 20));
							p.sendMessage("??eMegijesztetted ??o"+target.getName()+"??r??e j??t??kost!");
							break;
						case "skeleton":
							try {
								int darab = Integer.parseInt(args[2]);
								if(darab <= 0) {
									p.sendMessage("??e"+darab+" csontv??zat minek akarsz megid??zni? :D");
								} else {
									for(int loop = darab; loop > 0; loop--) {
										LivingEntity skeleton = (LivingEntity) target.getWorld().spawnEntity(target.getLocation(), EntityType.SKELETON);
										skeleton.attack(target);
										skeleton.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 20000000, 255, false, false));
										skeleton.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20000000, 255, false, false));
										ItemStack bow = skeleton.getEquipment().getItemInMainHand();
										bow.addEnchantment(Enchantment.ARROW_FIRE, 1);
										skeleton.getEquipment().setItemInMainHand(bow);
									}
									p.sendMessage("??eMegid??zt??l ??o"+darab+"??r??e csontv??zat ??o"+target.getName()+"??r??e j??t??koshoz!");
								}
							} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
								p.sendMessage("??4??rv??nytelen darabsz??m!");
								return true;
							}
							break;
						case "snow":
							target.getWorld().getBlockAt(target.getLocation().subtract(0, 1, 0)).setType(Material.POWDER_SNOW);
							p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos al?? porh?? termett!");
							break;
						case "levitate":
							target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 600, 50));
							p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l lebeg egy ideig!");
							break;
						case "invrandom":
							List<ItemStack> inv = Arrays.asList(target.getInventory().getContents());
							Collections.shuffle(inv);
							target.getInventory().setContents(inv.toArray(new ItemStack[0]));
							p.sendMessage("??e??sszekeverted ??o"+target.getName()+"??r??e j??t??kos eszk??zt??r??t!");
							break;
						case "invdamage":
							ItemStack[] list = target.getInventory().getContents();
							for(ItemStack i : list) {
								if(i != null) {
									Damageable meta = (Damageable) i.getItemMeta();
									meta.setDamage(i.getType().getMaxDurability()-1);
									i.setItemMeta(meta);
								}
							}
							target.getInventory().setContents(list);
							p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos minden eszk??z??nek ??o1??r??e lett a tart??ss??ga!");
							break;
						case "cube":
							Location loc2 = target.getLocation();
							World world2 = target.getWorld();
							for(int loopY = loc2.getBlockY()-1; loopY < loc2.getBlockY()+3; loopY++) {
								for(int loopX = loc2.getBlockX()-1; loopX < loc2.getBlockX()+2; loopX++) {
									for(int loopZ = loc2.getBlockZ()-1; loopZ < loc2.getBlockZ()+2; loopZ++) {
										if(loopX == loc2.getBlockX() && loopZ == loc2.getBlockZ() && (loopY == loc2.getBlockY() || loopY == loc2.getBlockY()+1)) {
											world2.getBlockAt(loopX, loopY, loopZ).setType(Material.WATER);
										} else {
											world2.getBlockAt(loopX, loopY, loopZ).setType(Material.OBSIDIAN);
										}
									}
								}
							}
							p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kost bez??rtad egy v??zzel teli kock??ba!");
							break;
						case "rotate":
							if(rotatingPlayers.containsKey(p)) {
								rotatingPlayers.remove(p);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l nem fog arra n??zni, amerre Te n??zel!");
							} else {
								rotatingPlayers.put(p, target);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l arra fog n??zni, amerre Te n??zel!");
							}
							break;
						case "randomeffect":
							if(randomEffectPlayers.contains(target)) {
								randomEffectPlayers.remove(target);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l nem fog v??letlenszer?? effekteket kapni!");
							} else {
								randomEffectPlayers.add(target);
								p.sendMessage("??e??o"+target.getName()+"??r??e j??t??kos mostant??l v??letlenszer?? effekteket fog kapni!");
							}
							new BukkitRunnable() {
								@Override
								public void run() {
									Random generator = new Random();
									if(randomEffectPlayers.contains(target)) {
										target.addPotionEffect(new PotionEffect(PotionEffectType.getById(generator.nextInt(33)+1), (generator.nextInt(60)+1)*20, generator.nextInt(100)+1));
									} else {
										cancel();
									}
								}
							}.runTaskTimer(this, 0, 100);
							break;
						default:
							p.sendMessage("??4??rv??nytelen opci??!");
					}
				} else if(args.length == 1) {
					p.sendMessage("??4Nem adt??l meg j??t??kost!");
				} else {
					p.sendMessage("??b??lTroll plugin haszn??lat:\n??b=====================================================\n??e/troll freeze <j??t??kos>: ??aMegfagyasztja a c??lpontot (KAPCSOL??)\n??e/troll burn <j??t??kos> <m??sodperc>: ??aFelgy??jtja a c??lpontot\n??e/troll fall <j??t??kos>: ??aA c??lpont alatt egy 3x3-as g??d??r keletkezik, ami a voidba vezet\n??e/troll reversedamage <j??t??kos>: ??aHa a c??lpont sebezni pr??b??l egy entit??st, ??nmag??t fogja sebezni annyival (KAPCSOL??)\n??e/troll sculk <j??t??kos>: ??aA c??lpont s??t??ts??g effektet kap, ??s sculk hangokat hall\n??e/troll skeleton <j??t??kos> <darab>: ??aA c??lpontban l??thatatlan, t??zes ??jas csontv??zak teremnek\n??e/troll snow <j??t??kos>: ??aA c??lpont alatti blokk porh??v?? v??ltozik\n??e/troll levitate <j??t??kos>: ??aA c??lpont lebeg??s effektet kap\n??e/troll invrandom <j??t??kos>: ??aA c??lpont eszk??zt??r??nak tartalma ??sszekeveredik\n??e/troll invdamage <j??t??kos>: ??aA c??lpont eszk??zt??r??ban minden eszk??z tart??ss??ga 1-re v??ltozik\n??e/troll cube <j??t??kos>: ??aA c??lpont k??r?? egy v??zzel teli obszidi??nkocka keletkezik\n??e/troll rotate <j??t??kos>: ??aA c??lpont arra n??z, amerre Te n??zel (KAPCSOL??)\n??e/troll randomeffect <j??t??kos>: ??aA c??lpont v??letlenszer?? effektet kap 5 m??sodpercenk??nt 1-60 mp k??z??tti id??tartammal, 1-100 k??z??tti er??ss??ggel (KAPCSOL??)\n??b=====================================================");
				}
			} else {
				sender.sendMessage("Nem vagy j??t??kos!");
			}
		}
		return true;
	}

	@Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(sender instanceof Player && alias.equalsIgnoreCase("troll") && args.length == 1) {
			return arguments;
		}
		return null;
	}
}
