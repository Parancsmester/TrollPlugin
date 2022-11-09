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
						p.sendMessage("§4Helytelen játékosnév!");
						return true;
					}
					switch(args[0]) {
						case "freeze":
							if(frozenPlayers.contains(target)) {
								frozenPlayers.remove(target);
								p.sendMessage("§eLevetted a fagyást §o"+target.getName()+"§r§e játékosról!");
							} else {
								frozenPlayers.add(target);
								p.sendMessage("§eMegfagyasztottad §o"+target.getName()+"§r§e játékost!");
							}
							break;
						case "burn":
							try {
								int sec = Integer.parseInt(args[2]);
								if(sec <= 0) {
									p.sendMessage("§e"+sec+" másodpercre minek akarod felgyújtani? :D");
								} else {
									p.setFireTicks(sec*20);
									p.sendMessage("§eFelgyújtottad §o"+target.getName()+"§r§e játékost §o"+sec+"§r§e másodpercre!");
								}
							} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
								p.sendMessage("§4Érvénytelen szám!");
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
							p.sendMessage("§eMegnyílt a föld §o"+target.getName()+"§r§e játékos alatt!");
							break;
						case "reversedamage":
							if(reverseDamagePlayers.contains(target)) {
								reverseDamagePlayers.remove(target);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól nem fogja önmagát sebezni, ha sebez egy entitást!");
							} else {
								reverseDamagePlayers.add(target);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól önmagát fogja sebezni, ha sebez egy entitást!");
							}
							break;
						case "sculk":
							target.playSound(target, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1000, 1);
							target.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 20));
							p.sendMessage("§eMegijesztetted §o"+target.getName()+"§r§e játékost!");
							break;
						case "skeleton":
							try {
								int darab = Integer.parseInt(args[2]);
								if(darab <= 0) {
									p.sendMessage("§e"+darab+" csontvázat minek akarsz megidézni? :D");
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
									p.sendMessage("§eMegidéztél §o"+darab+"§r§e csontvázat §o"+target.getName()+"§r§e játékoshoz!");
								}
							} catch (ArrayIndexOutOfBoundsException | NumberFormatException ex) {
								p.sendMessage("§4Érvénytelen darabszám!");
								return true;
							}
							break;
						case "snow":
							target.getWorld().getBlockAt(target.getLocation().subtract(0, 1, 0)).setType(Material.POWDER_SNOW);
							p.sendMessage("§e§o"+target.getName()+"§r§e játékos alá porhó termett!");
							break;
						case "levitate":
							target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 600, 50));
							p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól lebeg egy ideig!");
							break;
						case "invrandom":
							List<ItemStack> inv = Arrays.asList(target.getInventory().getContents());
							Collections.shuffle(inv);
							target.getInventory().setContents(inv.toArray(new ItemStack[0]));
							p.sendMessage("§eÖsszekeverted §o"+target.getName()+"§r§e játékos eszköztárát!");
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
							p.sendMessage("§e§o"+target.getName()+"§r§e játékos minden eszközének §o1§r§e lett a tartóssága!");
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
							p.sendMessage("§e§o"+target.getName()+"§r§e játékost bezártad egy vízzel teli kockába!");
							break;
						case "rotate":
							if(rotatingPlayers.containsKey(p)) {
								rotatingPlayers.remove(p);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól nem fog arra nézni, amerre Te nézel!");
							} else {
								rotatingPlayers.put(p, target);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól arra fog nézni, amerre Te nézel!");
							}
							break;
						case "randomeffect":
							if(randomEffectPlayers.contains(target)) {
								randomEffectPlayers.remove(target);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól nem fog véletlenszerű effekteket kapni!");
							} else {
								randomEffectPlayers.add(target);
								p.sendMessage("§e§o"+target.getName()+"§r§e játékos mostantól véletlenszerű effekteket fog kapni!");
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
							p.sendMessage("§4Érvénytelen opció!");
					}
				} else if(args.length == 1) {
					p.sendMessage("§4Nem adtál meg játékost!");
				} else {
					p.sendMessage("§b§lTroll plugin használat:\n§b=====================================================\n§e/troll freeze <játékos>: §aMegfagyasztja a célpontot (KAPCSOLÓ)\n§e/troll burn <játékos> <másodperc>: §aFelgyújtja a célpontot\n§e/troll fall <játékos>: §aA célpont alatt egy 3x3-as gödör keletkezik, ami a voidba vezet\n§e/troll reversedamage <játékos>: §aHa a célpont sebezni próbál egy entitást, önmagát fogja sebezni annyival (KAPCSOLÓ)\n§e/troll sculk <játékos>: §aA célpont sötétség effektet kap, és sculk hangokat hall\n§e/troll skeleton <játékos> <darab>: §aA célpontban láthatatlan, tüzes íjas csontvázak teremnek\n§e/troll snow <játékos>: §aA célpont alatti blokk porhóvá változik\n§e/troll levitate <játékos>: §aA célpont lebegés effektet kap\n§e/troll invrandom <játékos>: §aA célpont eszköztárának tartalma összekeveredik\n§e/troll invdamage <játékos>: §aA célpont eszköztárában minden eszköz tartóssága 1-re változik\n§e/troll cube <játékos>: §aA célpont köré egy vízzel teli obszidiánkocka keletkezik\n§e/troll rotate <játékos>: §aA célpont arra néz, amerre Te nézel (KAPCSOLÓ)\n§e/troll randomeffect <játékos>: §aA célpont véletlenszerű effektet kap 5 másodpercenként 1-60 mp közötti időtartammal, 1-100 közötti erősséggel (KAPCSOLÓ)\n§b=====================================================");
				}
			} else {
				sender.sendMessage("Nem vagy játékos!");
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
