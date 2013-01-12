package me.bhsgoclub.incantations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Incantations extends JavaPlugin {
	static Watcher watcher = new Watcher(null);
	public File folder = getDataFolder();
	
	public final Hashtable<Player, Spellbook> spellbookCollection = new Hashtable<Player, Spellbook>();
	public final Hashtable<String, String> spellLookup = new Hashtable<String, String>();
	public Map<String, String> spellMap;

	public void onEnable() {
		spellMap = new HashMap<String, String>();

		loadFiles();
		loadListeners();
		startMetrics();
	}
	
	public void loadListeners()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new IncantationsPlayerListener(this), this);
		pm.registerEvents(new IncantationsBlockListener(this), this);
		pm.registerEvents(new IncantationsEntityListener(this), this);
	}
	
	public void startMetrics()
	  {
	    try
	    {
	      Metrics metrics = new Metrics(this);
	      metrics.start();
	    }
	    catch (IOException localIOException)
	    {
	    	localIOException.printStackTrace();
	    }
	  }
	
	public void loadFiles()
	{
		
		loadConfig();
		loadSpells();
		loadUsers();
	}
    
    public void loadConfig()
	{
    	Logger log = getServer().getLogger();
		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(getDataFolder() + "/config.yml");
		if(!config.exists())
		{
			try{
				getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't create config");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(getDataFolder() + File.separator + "config.yml"));
				InputStream is = getResource("config.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
				
				log.log(Level.INFO, "[Incantations] Wrote new config");
				
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't write config: " + e);
			}	
		}
		else
		{
			log.log(Level.INFO, "[Incantations] Config found.");
		}
	}
	
	/**
	 * Set up the Spells.yml
	 */
	
	public void loadSpells()
	{
		Logger log = getServer().getLogger();
		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(getDataFolder() + "/Spells.yml");
		if(!config.exists())
		{
			try{
				getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't create spells file.");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(getDataFolder() + File.separator + "Spells.yml"));
				InputStream is = getResource("Spells.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
				
				log.log(Level.INFO, "[Incantations] Wrote new spells file.");
				
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't write new spells file: " + e);
			}	
		}
		else
		{
			log.log(Level.INFO, "[Incantations] spells file found.");
		}
	}
	
	/**
	 * Create a new users file if it isn't there.
	 */
	
	public void loadUsers()
	{
		Logger log = getServer().getLogger();
		/**
		 * Check to see if there's a config.
		 * If not then create a new one.
		 */
		File config = new File(getDataFolder() + "/Users.yml");
		if(!config.exists())
		{
			try{
				getDataFolder().mkdir();
				config.createNewFile();
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't create users file.");
			}
			/**
			 * Write the config file here.
			 * New, genius way to write it :)
			 */
			try {
				FileOutputStream fos = new FileOutputStream(new File(getDataFolder() + File.separator + "Users.yml"));
				InputStream is = getResource("Users.yml");
				byte[] linebuffer = new byte[4096];
				int lineLength = 0;
				while((lineLength = is.read(linebuffer)) > 0)
				{
					fos.write(linebuffer, 0, lineLength);
				}
				fos.close();
				
				log.log(Level.INFO, "[Incantations] Wrote new users file.");
				
			} catch (IOException e) {
				log.log(Level.SEVERE, "[Incantations] Couldn't write users file: " + e);
			}	
		}
		else
		{
			log.log(Level.INFO, "[Incantations] users file found.");
		}
	}
}  
 