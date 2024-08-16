package pl.grzywniak.saplingregen;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SaplingRegen extends JavaPlugin implements Listener
{
	private static SaplingRegen instance;
	private static Config cfg = new Config();
	public HashMap<Integer, Location> saplingList = new HashMap<Integer, Location>();
	
	//TODO
	//- Po scienciu drzewa odczekac ilosc cykli do ponownego zasadzenia
	//- Blokada niszczenia sadzonek [CONFIG TRUE/FALSE]
	//- Efekty na roznych wersjach mc
	
	@Override
	public void onEnable()
	{
		int pluginID = 1953;
        new Metrics(this, pluginID);
		Bukkit.getPluginManager().registerEvents(this, this);
		cfg.createDefaultFiles();
		instance = this;
		checkSapling();
		saplingList = Config.getLocations();
	}
	
	@Override
	public void onDisable()
	{
		System.out.println("Thanks for using my plugin from MineS.pl :)");
	}
	
	public static SaplingRegen getInstance()
	{
		return instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(cmd.getName().equalsIgnoreCase("sapling"))
		{
			if(sender instanceof Player)
			{
				Player p = (Player)sender;
				if(p.hasPermission("SaplingRegen.use"))
				{
					if(args.length == 1)
					{
						if(args[0].equals("set"))
						{
							Location l = p.getLocation();
	
							int getX = (int) l.getX();
							int getY = (int) l.getY();
							int getZ = (int) l.getZ();
							
							String getWorldName = l.getWorld().getName();
							
							if(getX < 0)
							{
								getX -= 1;
							}
							if(getZ < 0)
							{
								getZ -= 1;
							}
							
							l.setX(getX);
							l.setY(getY);
							l.setZ(getZ);
							l.setPitch(0);
							l.setYaw(0);

							if(!saplingList.containsValue(l)) //PLAYER ZAOKRAGLIC
							{
								p.sendMessage("§aYou have set up regenerating saplings!");
								cfg.setLocation(getX, getY, getZ, getWorldName);
							}
							else
							{
								p.sendMessage("§cThis place has already been set!");
							}
						}
						else if(args[0].equals("remove"))
						{
							Location pLoc = new Location(p.getLocation().getWorld(), (int) p.getLocation().getX(), (int) p.getLocation().getY(), (int) p.getLocation().getZ());
							
							if(cfg.remove(pLoc))
							{
								p.sendMessage("§aLocation was removed!");
							}
							else
							{
								p.sendMessage("§cDoes not exist sapling at this location!");
							}
						}
						else
						{
							p.sendMessage("§cBad command!\n§eTry use: /sapling <set, remove>");
						}
					}
					else
					{			
						p.sendMessage("§cBad command!\n§eTry use: /sapling <set, remove>");
					}
				}
				else
				{
					p.sendMessage("§cYou have not permission §e'saplingregen.use'");
				}
			}
			else
			{
				sender.sendMessage("§cYou must be a player!");
			}
			
		}
		return false;
	}
	
	public void checkSapling()
	{
		Bukkit.getScheduler().runTaskTimer(getInstance(), new Runnable()
		{
            @Override
			public void run()
            {
				for(int i = 1; i <= saplingList.size(); i++)
				{
					Location loc = saplingList.get(i);
					if(loc.getBlock().isEmpty())
					{
						loc.getBlock().setType(Material.OAK_SAPLING);
						if(!Bukkit.getOnlinePlayers().isEmpty())
						{
							for(Player p : Bukkit.getOnlinePlayers())
							{
								if(p.getLocation().getWorld().equals(loc.getWorld()) && p.getLocation().distance(loc) < 16)
								{
									p.spawnParticle(Particle.CLOUD, loc, 50);
//									PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(EnumParticle.CLOUD, false, (float)loc.getX(), (float)loc.getY() + 1.0F, (float)loc.getZ(), 0.2F, 0.5F, 0.2F, 0.1F, 200, null);
//									((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
									p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
								}
							}
						}
					}
				}
            }
		}, 0, 20 * cfg.timer);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		if(cfg.messageAfterDestroyingSaplings && e.getBlock().getType().toString().contains("SAPLING") && !e.getBlock().getType().toString().contains("POTTED"))
		{
			e.getPlayer().sendMessage(cfg.textOfThisMessage);
		}
	}
}