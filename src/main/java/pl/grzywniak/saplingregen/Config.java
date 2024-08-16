package pl.grzywniak.saplingregen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config 
{
	final public static File directory = new File("plugins/SaplingRegen/");
	final public static File locations = new File("plugins/SaplingRegen/Locations.yml");
	final public static File config = new File("plugins/SaplingRegen/Config.yml");
	
	public boolean messageAfterDestroyingSaplings;
	public String textOfThisMessage;
	public int timer;
	
	public void createDefaultFiles()
	{
		if(!directory.exists())
		{
			directory.mkdir();
		}
		if(!locations.exists())
		{
			try
			{
				locations.createNewFile();
				
				Writer writer = new BufferedWriter(new FileWriter(locations));
	             
	            writer.write("Locations:");
	            writer.close();
			}
			catch(IOException e)
			{
				e.getMessage();
			}
		}
		if(!config.exists())
		{
			try
			{
				config.createNewFile();
				
				Writer writer = new BufferedWriter(new FileWriter(config));
	             
				writer.write("Config:");                                                       ((BufferedWriter) writer).newLine();
				writer.write("  #What time do you check the locations of the saplings?");      ((BufferedWriter) writer).newLine();
                writer.write("  TimerInSec: 15");                  						       ((BufferedWriter) writer).newLine();
                writer.write("  MessageAfterDestroyingSaplings: true");                        ((BufferedWriter) writer).newLine();
                writer.write("  TextOfThisMessage: '§eRemember to plant the tree later ;)'");  ((BufferedWriter) writer).newLine();
	            writer.close();
			}
			catch(IOException e)
			{
				e.getMessage();
			}
		}
		getVariables();
	}
	
	public void getVariables()
	{
		FileConfiguration conf = YamlConfiguration.loadConfiguration(config);
		
		messageAfterDestroyingSaplings = conf.getBoolean("Config.MessageAfterDestroyingSaplings");
		textOfThisMessage = conf.getString("Config.TextOfThisMessage");
		timer = conf.getInt("Config.TimerInSec");
		
	}
	
	public static HashMap<Integer, Location> getLocations()
	{
		HashMap<Integer, Location> locationss = new HashMap<Integer, Location>();
		FileConfiguration conf = YamlConfiguration.loadConfiguration(locations);
		
		for(int i = 1; i <= getCountOfLocations(); i++)
		{
			if(conf.contains("Locations." + i + ".world"))
			{
				locationss.put(i, new Location(Bukkit.getServer().createWorld(new WorldCreator(conf.getString("Locations." + i + ".world"))), conf.getDouble("Locations."+i+".x"), conf.getDouble("Locations."+i+".y"), conf.getDouble("Locations."+i+".z")));
			}
		}
		
		return locationss;
	}
	
	public void setLocation(int x, int y, int z, String worldName)
	{
		
		int count = getCountOfLocations() + 1;
		try
		{
			FileConfiguration location = YamlConfiguration.loadConfiguration(locations);
			location.createSection("Locations." + count + ".world");
			location.createSection("Locations." + count + ".x");
			location.createSection("Locations." + count + ".y");
			location.createSection("Locations." + count + ".z");
			
			location.set("Locations." + count + ".world", worldName);
			location.set("Locations." + count + ".x", x);
			location.set("Locations." + count + ".y", y);
			location.set("Locations." + count + ".z", z);
			
			location.save(locations);
			SaplingRegen.getInstance().saplingList.put(count, new Location(Bukkit.getWorld(worldName), x, y, z));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void removeLocation(int number) 
	{
		int count = getCountOfLocations();
		FileConfiguration conf = YamlConfiguration.loadConfiguration(locations);
		try
		{
			conf.set("Locations." + number + ".world", conf.get("Locations." + count + ".world"));
			conf.set("Locations." + number + ".x", conf.get("Locations." + count + ".x"));
			conf.set("Locations." + number + ".y", conf.get("Locations." + count + ".y"));
			conf.set("Locations." + number + ".z", conf.get("Locations." + count + ".z"));
			conf.save(locations);
			conf.set("Locations." + count, null);
			conf.save(locations);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static int getCountOfLocations()
	{
		int count = 0;
		
		FileConfiguration location = YamlConfiguration.loadConfiguration(locations);
		ConfigurationSection defined = location.getConfigurationSection("Locations");
		
		if(defined != null)
		{
//			for(String keys : defined.getKeys(false).size())
//			{
//				count += 1;
//			}
			count = defined.getKeys(false).size();
		}
		return count;
	}

	public boolean remove(Location location)
	{
		boolean isExist = false;
		
		if(location.getX() < 0)
		{
			location.setX(location.getX() - 1);
		}
		if(location.getZ() < 0)
		{
			location.setZ(location.getZ() - 1);
		}
		
		for (Map.Entry<Integer, Location> lista : getLocations().entrySet())
		{
		    Integer key = lista.getKey();
		    Location value = lista.getValue();
		    
		    if(value.equals(location))
		    {
		    	isExist = true;
		    	removeLocation(key);
		    	
		    	break;
		    }
		}
		
		int tempKeyToRemove = -1;
		
        for (int key: SaplingRegen.getInstance().saplingList.keySet())
        {
        	if(SaplingRegen.getInstance().saplingList.get(key).equals(location))
        	{
        		tempKeyToRemove = key;
        		break;
        	}
        }
        if(tempKeyToRemove != -1) SaplingRegen.getInstance().saplingList.remove(tempKeyToRemove);
		
		return isExist;
	}
}