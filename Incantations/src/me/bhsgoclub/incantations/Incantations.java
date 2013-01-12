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
import org.bukkit.configuration.file.YamlConfiguration;

public class Incantations extends JavaPlugin {
	static Watcher watcher = new Watcher(null);
	public File folder = getDataFolder();

	/*
	 * public YamlConfiguration config; public YamlConfiguration spells; public
	 * static YamlConfiguration users;
	 */
	public final Hashtable<Player, Spellbook> spellbookCollection = new Hashtable<Player, Spellbook>();
	public final Hashtable<String, String> spellLookup = new Hashtable<String, String>();
	

	/*
	 * public PermissionHandler permissions; public Boolean usePermissions =
	 * false; NOT NEEDED
	 */

	/*
	 * public enum spellNames { Bubble("sphaera"), Lightning("fulmen"),
	 * SlowFall("tarduscado"), Heal("remedium"), Blink("transulto"),
	 * Breathe("respiro"), Freeze("frigidus"), Thaw("concalesco"),
	 * Extinguish("extinguo"), Rain("pluvia"), Storm("tempestas"),
	 * Clear("sereno"), Mutate("mutatio"), Wall("clausus"), Replenish("repleo"),
	 * Protect("tueri"), Fireball("ignifera"), Write("scripto"),
	 * Transmute("transmutare"), Entomb("sepulcrum"), GlassToIce("glacia"),
	 * Light("lux"), WaterWalking("superaquas"); private spellNames(String name)
	 * { this.name = name; } public String name; }
	 */
	public Map<String, String> spellMap;

	public void onEnable() {
		spellMap = new HashMap<String, String>();

		loadFiles();
		loadListeners();
		//setupConfig();
		//setupSpells();
		//setupUsers();
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

	public void setupConfig() {
		this.getConfig().set("General.SpellAnounceLevel", 1);
		this.getConfig().set("General.SpellRange", 50);
		this.getConfig().set("General.SuperCommand", "magna");
		this.getConfig().set("General.CastItem", 280);

		this.getConfig().set("Reagents.RegularIsDye", false);
		this.getConfig().set("Reagents.Regular", 331);
		this.getConfig().set("Reagents.MasterIsDye", true);
		this.getConfig().set("Reagents.Master", 4);
		this.getConfig().set("Reagents.TransmuteCost", 16);
		this.getConfig().set("Reagents.Level1", 1);
		this.getConfig().set("Reagents.Level2", 3);
		this.getConfig().set("Reagents.Level3", 5);

		this.getConfig().set("Spellbook.Enabled", true);
		this.getConfig().set("Spellbook.InscriptionCommand", "scripto");
		this.getConfig().set("Spellbook.WriteReagentIsDye", true);
		this.getConfig().set("Spellbook.WriteReagent", 4);
		this.getConfig().set("Spellbook.Cooldown", 5);

	}

	public void setupSpells() {
    	File spellsFile = new File(getDataFolder(), "Spells.yml");
    	
    	if (!spellsFile.exists()) {
    		try {
    			spellsFile.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	
    
            YamlConfiguration spells = YamlConfiguration.loadConfiguration(spellsFile);
            spells.addDefault("Spells.Lightning.Name", "fulmen");
            spells.addDefault("Spells.Lightning.CostMultiplier", 1);
	        spells.addDefault("Spells.Lightning.Master", false);
	        spells.addDefault("Spells.Lightning.Cooldown", 5);
	        spells.addDefault("Spells.Lightning.CastItemOnly", false);
	
        	spells.addDefault("Spells.Bubble.Name", "sphaera");
	        spells.addDefault("Spells.Bubble.CostMultiplier", 1);
	        spells.addDefault("Spells.Bubble.Master", false);
	        spells.addDefault("Spells.Bubble.Cooldown", 30);
	        spells.addDefault("Spells.Bubble.CastItemOnly", false);

	        spells.addDefault("Spells.SlowFall.Name", "tarduscado");
	        spells.addDefault("Spells.SlowFall.CostMultiplier", 1);
	        spells.addDefault("Spells.SlowFall.Master", false);
	        spells.addDefault("Spells.SlowFall.Cooldown", 0);
	        spells.addDefault("Spells.SlowFall.CastItemOnly", false);

	        spells.addDefault("Spells.Heal.Name", "remedium");
	        spells.addDefault("Spells.Heal.CostMultiplier", 1);
	        spells.addDefault("Spells.Heal.Master", false);
	        spells.addDefault("Spells.Heal.Cooldown", 0);
	        spells.addDefault("Spells.Heal.CastItemOnly", false);

	        spells.addDefault("Spells.Blink.Name", "transulto");
	        spells.addDefault("Spells.Blink.CostMultiplier", 1);
	        spells.addDefault("Spells.Blink.Master", false);
	        spells.addDefault("Spells.Blink.Cooldown", 30);
	        spells.addDefault("Spells.Blink.CastItemOnly", false);

	        spells.addDefault("Spells.Breathe.Name", "respiro");
	        spells.addDefault("Spells.Breathe.CostMultiplier", 1);
	        spells.addDefault("Spells.Breathe.Master", false);
	        spells.addDefault("Spells.Breathe.Cooldown", 0);
	        spells.addDefault("Spells.Breathe.CastItemOnly", false);

	        spells.addDefault("Spells.Freeze.Name", "frigidus");
	        spells.addDefault("Spells.Freeze.CostMultiplier", 1);
	        spells.addDefault("Spells.Freeze.Master", false);
	        spells.addDefault("Spells.Freeze.Cooldown", 0);
	        spells.addDefault("Spells.Freeze.CastItemOnly", false);

	        spells.addDefault("Spells.Thaw.Name", "concalesco");
	        spells.addDefault("Spells.Thaw.CostMultiplier", 1);
	        spells.addDefault("Spells.Thaw.Master", false);
	        spells.addDefault("Spells.Thaw.Cooldown", 0);
	        spells.addDefault("Spells.Thaw.CastItemOnly", false);

	        spells.addDefault("Spells.Extinguish.Name", "extinguo");
	        spells.addDefault("Spells.Extinguish.CostMultiplier", 1);
	        spells.addDefault("Spells.Extinguish.Master", false);
	        spells.addDefault("Spells.Extinguish.Cooldown", 0);
	        spells.addDefault("Spells.Extinguish.CastItemOnly", false);

	        spells.addDefault("Spells.Rain.Name", "pluvia");
	        spells.addDefault("Spells.Rain.CostMultiplier", 1);
	        spells.addDefault("Spells.Rain.Master", true);
	        spells.addDefault("Spells.Rain.Cooldown", 0);
	        spells.addDefault("Spells.Rain.CastItemOnly", false);

	        spells.addDefault("Spells.Storm.Name", "tempestas");
	        spells.addDefault("Spells.Storm.CostMultiplier", 1);
	        spells.addDefault("Spells.Storm.Master", true);
	        spells.addDefault("Spells.Storm.Cooldown", 0);
	        spells.addDefault("Spells.Storm.CastItemOnly", false);

	        spells.addDefault("Spells.Clear.Name", "sereno");
	        spells.addDefault("Spells.Clear.CostMultiplier", 1);
	        spells.addDefault("Spells.Clear.Master", true);
	        spells.addDefault("Spells.Clear.Cooldown", 0);
	        spells.addDefault("Spells.Clear.CastItemOnly", false);

	        spells.addDefault("Spells.Mutate.Name", "mutatio");
	        spells.addDefault("Spells.Mutate.CostMultiplier", 1);
	        spells.addDefault("Spells.Mutate.Master", false);
	        spells.addDefault("Spells.Mutate.Cooldown", 0);
	        spells.addDefault("Spells.Mutate.CastItemOnly", false);

	        spells.addDefault("Spells.Wall.Name", "clausus");
	        spells.addDefault("Spells.Wall.CostMultiplier", 1);
	        spells.addDefault("Spells.Wall.Master", false);
	        spells.addDefault("Spells.Wall.Cooldown", 30);
	        spells.addDefault("Spells.Wall.CastItemOnly", false);

	        spells.addDefault("Spells.Replenish.Name", "repleo");
	        spells.addDefault("Spells.Replenish.CostMultiplier", 1);
	        spells.addDefault("Spells.Replenish.Master", false);
	        spells.addDefault("Spells.Replenish.Cooldown", 0);
	        spells.addDefault("Spells.Replenish.CastItemOnly", false);

	        spells.addDefault("Spells.Protect.Name", "tueri");
	        spells.addDefault("Spells.Protect.CostMultiplier", 1);
	        spells.addDefault("Spells.Protect.Master", false);
	        spells.addDefault("Spells.Protect.Cooldown", 30);
	        spells.addDefault("Spells.Protect.CastItemOnly", false);

	        spells.addDefault("Spells.Fireball.Name", "ignifera");
	        spells.addDefault("Spells.Fireball.CostMultiplier", 1);
	        spells.addDefault("Spells.Fireball.Master", false);
	        spells.addDefault("Spells.Fireball.Cooldown", 0);
	        spells.addDefault("Spells.Fireball.CastItemOnly", false);

	        spells.addDefault("Spells.Transmute.Name", "transmutare");
	        spells.addDefault("Spells.Transmute.CostMultiplier", 0);
	        spells.addDefault("Spells.Transmute.Master", false);
	        spells.addDefault("Spells.Transmute.Cooldown", 0);
	        spells.addDefault("Spells.Transmute.CastItemOnly", false);

	        spells.addDefault("Spells.Entomb.Name", "sepulcrum");
	        spells.addDefault("Spells.Entomb.CostMultiplier", 1);
	        spells.addDefault("Spells.Entomb.Master", false);
	        spells.addDefault("Spells.Entomb.Cooldown", 0);
	        spells.addDefault("Spells.Entomb.CastItemOnly", false);

	        spells.addDefault("Spells.GlassToIce.Name", "glacia");
	        spells.addDefault("Spells.GlassToIce.CostMultiplier", 1);
	        spells.addDefault("Spells.GlassToIce.Master", false);
	        spells.addDefault("Spells.GlassToIce.Cooldown", 0);
	        spells.addDefault("Spells.GlassToIce.CastItemOnly", false);

	        spells.addDefault("Spells.Light.Name", "lux");
	        spells.addDefault("Spells.Light.CostMultiplier", 1);
	        spells.addDefault("Spells.Light.Master", false);
	        spells.addDefault("Spells.Light.Cooldown", 0);
	        spells.addDefault("Spells.Light.CastItemOnly", false);

	        spells.addDefault("Spells.WaterWalking.Name", "superaquas");
	        spells.addDefault("Spells.WaterWalking.CostMultiplier", 1);
	        spells.addDefault("Spells.WaterWalking.Master", false);
	        spells.addDefault("Spells.WaterWalking.Cooldown", 0);
	        spells.addDefault("Spells.WaterWalking.CastItemOnly", false);

	        spells.addDefault("Spells.Launch.Name", "jaceo");
	        spells.addDefault("Spells.Launch.CostMultiplier", 1);
	        spells.addDefault("Spells.Launch.Master", false);
	        spells.addDefault("Spells.Launch.Cooldown", 5);
	        spells.addDefault("Spells.Launch.CastItemOnly", false);

	        spells.addDefault("Spells.Break.Name", "ruptura");
	        spells.addDefault("Spells.Break.CostMultiplier", 1);
	        spells.addDefault("Spells.Break.Master", false);
	        spells.addDefault("Spells.Break.Cooldown", 10);
	        spells.addDefault("Spells.Break.CastItemOnly", false);

	        spells.addDefault("Spells.Activate.Name", "tincidunt");
	        spells.addDefault("Spells.Activate.CostMultiplier", 1);
	        spells.addDefault("Spells.Activate.Master", false);
	        spells.addDefault("Spells.Activate.Cooldown", 0);
	        spells.addDefault("Spells.Activate.CastItemOnly", false);

	        spells.addDefault("Spells.Silence.Name", "silentium");
	        spells.addDefault("Spells.Silence.CostMultiplier", 1);
	        spells.addDefault("Spells.Silence.Master", false);
	        spells.addDefault("Spells.Silence.Cooldown", 30);
	        spells.addDefault("Spells.Silence.CastItemOnly", false);

	        spells.options().copyDefaults(true);

	        try {
	        	spells.save(spellsFile);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
	        
    	}
	}	
    public void setupUsers() {
    	File usersFile = new File(getDataFolder(), "Users.yml");
    	
    	if (!usersFile.exists()) {
    		try {
    			usersFile.createNewFile();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		YamlConfiguration users = YamlConfiguration.loadConfiguration(usersFile);
    		users.options().copyDefaults(true);
    		try {
    			users.save(usersFile);
    		} catch (IOException e) {
    		  e.printStackTrace();
    		}
    	}
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
 