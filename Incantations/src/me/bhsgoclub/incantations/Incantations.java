package me.bhsgoclub.incantations;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.Item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.Plugin;

public class Incantations extends JavaPlugin
{	
    public final static Logger log = Logger.getLogger("Minecraft");
    public final static Watcher watcher = new Watcher(this);
    
    public Configuration config;
    public Configuration spells;
    public static Configuration users;
    
    public final Hashtable<Player, Spellbook> spellbookCollection = new Hashtable<Player, Spellbook>();
    public final Hashtable<String, String> spellLookup = new Hashtable<String, String>();
    
    private final IncantationsPlayerListener playerListener = new IncantationsPlayerListener(this);
    private final IncantationsBlockListener blockListener = new IncantationsBlockListener(this);
    private final IncantationsEntityListener entityListener = new IncantationsEntityListener(this);
    
    /*public PermissionHandler permissions;
    public Boolean usePermissions = false;
    NOT NEEDED*/
        
    /*
    public enum spellNames
    {
		Bubble("sphaera"), Lightning("fulmen"), SlowFall("tarduscado"), Heal("remedium"), Blink("transulto"),
		Breathe("respiro"), Freeze("frigidus"), Thaw("concalesco"), Extinguish("extinguo"), Rain("pluvia"),
		Storm("tempestas"), Clear("sereno"), Mutate("mutatio"), Wall("clausus"), Replenish("repleo"),
		Protect("tueri"), Fireball("ignifera"), Write("scripto"), Transmute("transmutare"), Entomb("sepulcrum"),
		GlassToIce("glacia"), Light("lux"), WaterWalking("superaquas");
		private spellNames(String name)
		{
			this.name = name;
		}
		public String name;
    }
    */
    @Override
    public void onEnable()
    {
    	// Read or create config file
    	parseConfigs();
    	
    	// Set single stack
    	//TODO: Fix
    	//Util.disableStacking(Item.byId[config.getInt("General.CastItem", 280)]);
    	//Util.disableStacking(Item.BOOK);
    	
    	// Hook events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.blockListener, this);
        pm.registerEvents(this.entityListener, this);
       
        
        // Start watcher thread
        getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20, 5);
        
        log.info("[Incantations] v" + getDescription().getVersion() + " active.");
    }
    
    public static YamlConfiguration loadConfiguration()
    {
    	//watcher.stop();
    	watcher.cleanUp();
    	users.save();
    	
        log.info("[Incantations]" + " *Tarantallegra* made you dance :P"); // Because we'd rather have funny messages than showing the version.
    }
    
    /*private void setupPermissions()
    {
        if (permissions != null)
            return;
        
        Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
        
        if (permissionsPlugin == null)
        {
            log.info("[Incantations] Permission system not detected, defaulting to OP");
            return;
        }
        
        permissions = ((Permissions) permissionsPlugin).getHandler();
        usePermissions = true;
        log.info("[Incantations] Using " + ((Permissions)permissionsPlugin).getDescription().getFullName());
    } NO NEED FOR THIS ANYMORE */
    
    private void parseConfigs()
    {
    	Boolean isNew = false;
    	
    	// Read or create config file
    	getDataFolder().mkdir();
    	File cfgFile = new File(getDataFolder().getPath() + File.separator + "config.yml");
    	try
    	{
			isNew = cfgFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			log.info("[Incantations] Error occured while creating config file: " + e.getMessage());
		}
    	config = new YamlConfiguration.loadConfiguration(cfgFile);
    	if (isNew)
    	{
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
    		
    		config.save();
    	}
    	else
    		config.load();
    	
    	// Read or create spell file
    	File spellFile = new File(getDataFolder().getPath() + File.separator + "spells.yml");
    	try
    	{
			isNew = spellFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			log.info("[Incantations] Error occured while creating spell file: " + e.getMessage());
		}
    	spells = new YamlConfiguration.loadConfiguration(spellFile);
    	if (isNew)
    	{
    		spells.set("Spells.Lightning.Name", "fulmen");
    		spells.set("Spells.Lightning.CostMultiplier", 1);
    		spells.set("Spells.Lightning.Master", false);
    		spells.set("Spells.Lightning.Cooldown", 5);
    		spells.set("Spells.Lightning.CastItemOnly", false);
    		
    		spells.set("Spells.Bubble.Name", "sphaera");
    		spells.set("Spells.Bubble.CostMultiplier", 1);
    		spells.set("Spells.Bubble.Master", false);
    		spells.set("Spells.Bubble.Cooldown", 30);
    		spells.set("Spells.Bubble.CastItemOnly", false);
    		
    		spells.set("Spells.SlowFall.Name", "tarduscado");
    		spells.set("Spells.SlowFall.CostMultiplier", 1);
    		spells.set("Spells.SlowFall.Master", false);
    		spells.set("Spells.SlowFall.Cooldown", 0);
    		spells.set("Spells.SlowFall.CastItemOnly", false);
    		
    		spells.set("Spells.Heal.Name", "remedium");
    		spells.set("Spells.Heal.CostMultiplier", 1);
    		spells.set("Spells.Heal.Master", false);
    		spells.set("Spells.Heal.Cooldown", 0);
    		spells.set("Spells.Heal.CastItemOnly", false);
    		
    		spells.set("Spells.Blink.Name", "transulto");
    		spells.set("Spells.Blink.CostMultiplier", 1);
    		spells.set("Spells.Blink.Master", false);
    		spells.set("Spells.Blink.Cooldown", 30);
    		spells.set("Spells.Blink.CastItemOnly", false);
    		
    		spells.set("Spells.Breathe.Name", "respiro");
    		spells.set("Spells.Breathe.CostMultiplier", 1);
    		spells.set("Spells.Breathe.Master", false);
    		spells.set("Spells.Breathe.Cooldown", 0);
    		spells.set("Spells.Breathe.CastItemOnly", false);
    		
    		spells.set("Spells.Freeze.Name", "frigidus");
    		spells.set("Spells.Freeze.CostMultiplier", 1);
    		spells.set("Spells.Freeze.Master", false);
    		spells.set("Spells.Freeze.Cooldown", 0);
    		spells.set("Spells.Freeze.CastItemOnly", false);
    		
    		spells.set("Spells.Thaw.Name", "concalesco");
    		spells.set("Spells.Thaw.CostMultiplier", 1);
    		spells.set("Spells.Thaw.Master", false);
    		spells.set("Spells.Thaw.Cooldown", 0);
    		spells.set("Spells.Thaw.CastItemOnly", false);
    		
    		spells.set("Spells.Extinguish.Name", "extinguo");
    		spells.set("Spells.Extinguish.CostMultiplier", 1);
    		spells.set("Spells.Extinguish.Master", false);
    		spells.set("Spells.Extinguish.Cooldown", 0);
    		spells.set("Spells.Extinguish.CastItemOnly", false);
    		
    		spells.set("Spells.Rain.Name", "pluvia");
    		spells.set("Spells.Rain.CostMultiplier", 1);
    		spells.set("Spells.Rain.Master", true);
    		spells.set("Spells.Rain.Cooldown", 0);
    		spells.set("Spells.Rain.CastItemOnly", false);
    		
    		spells.set("Spells.Storm.Name", "tempestas");
    		spells.set("Spells.Storm.CostMultiplier", 1);
    		spells.set("Spells.Storm.Master", true);
    		spells.set("Spells.Storm.Cooldown", 0);
    		spells.set("Spells.Storm.CastItemOnly", false);
    		
    		spells.set("Spells.Clear.Name", "sereno");
    		spells.set("Spells.Clear.CostMultiplier", 1);
    		spells.set("Spells.Clear.Master", true);
    		spells.set("Spells.Clear.Cooldown", 0);
    		spells.set("Spells.Clear.CastItemOnly", false);
    		
    		spells.set("Spells.Mutate.Name", "mutatio");
    		spells.set("Spells.Mutate.CostMultiplier", 1);
    		spells.set("Spells.Mutate.Master", false);
    		spells.set("Spells.Mutate.Cooldown", 0);
    		spells.set("Spells.Mutate.CastItemOnly", false);
    		
    		spells.set("Spells.Wall.Name", "clausus");
    		spells.set("Spells.Wall.CostMultiplier", 1);
    		spells.set("Spells.Wall.Master", false);
    		spells.set("Spells.Wall.Cooldown", 30);
    		spells.set("Spells.Wall.CastItemOnly", false);
    		
    		spells.set("Spells.Replenish.Name", "repleo");
    		spells.set("Spells.Replenish.CostMultiplier", 1);
    		spells.set("Spells.Replenish.Master", false);
    		spells.set("Spells.Replenish.Cooldown", 0);
    		spells.set("Spells.Replenish.CastItemOnly", false);
    		
    		spells.set("Spells.Protect.Name", "tueri");
    		spells.set("Spells.Protect.CostMultiplier", 1);
    		spells.set("Spells.Protect.Master", false);
    		spells.set("Spells.Protect.Cooldown", 30);
    		spells.set("Spells.Protect.CastItemOnly", false);
    		
    		spells.set("Spells.Fireball.Name", "ignifera");
    		spells.set("Spells.Fireball.CostMultiplier", 1);
    		spells.set("Spells.Fireball.Master", false);
    		spells.set("Spells.Fireball.Cooldown", 0);
    		spells.set("Spells.Fireball.CastItemOnly", false);
    		
    		spells.set("Spells.Transmute.Name", "transmutare");
    		spells.set("Spells.Transmute.CostMultiplier", 0);
    		spells.set("Spells.Transmute.Master", false);
    		spells.set("Spells.Transmute.Cooldown", 0);
    		spells.set("Spells.Transmute.CastItemOnly", false);
    		
    		spells.set("Spells.Entomb.Name", "sepulcrum");
    		spells.set("Spells.Entomb.CostMultiplier", 1);
    		spells.set("Spells.Entomb.Master", false);
    		spells.set("Spells.Entomb.Cooldown", 0);
    		spells.set("Spells.Entomb.CastItemOnly", false);
    		
    		spells.set("Spells.GlassToIce.Name", "glacia");
    		spells.set("Spells.GlassToIce.CostMultiplier", 1);
    		spells.set("Spells.GlassToIce.Master", false);
    		spells.set("Spells.GlassToIce.Cooldown", 0);
    		spells.set("Spells.GlassToIce.CastItemOnly", false);
    		
    		spells.set("Spells.Light.Name", "lux");
    		spells.set("Spells.Light.CostMultiplier", 1);
    		spells.set("Spells.Light.Master", false);
    		spells.set("Spells.Light.Cooldown", 0);
    		spells.set("Spells.Light.CastItemOnly", false);
    		
    		spells.set("Spells.WaterWalking.Name", "superaquas");
    		spells.set("Spells.WaterWalking.CostMultiplier", 1);
    		spells.set("Spells.WaterWalking.Master", false);
    		spells.set("Spells.WaterWalking.Cooldown", 0);
    		spells.set("Spells.WaterWalking.CastItemOnly", false);
    		
    		spells.set("Spells.Launch.Name", "jaceo");
    		spells.set("Spells.Launch.CostMultiplier", 1);
    		spells.set("Spells.Launch.Master", false);
    		spells.set("Spells.Launch.Cooldown", 5);
    		spells.set("Spells.Launch.CastItemOnly", false);
    		
    		spells.set("Spells.Break.Name", "ruptura");
    		spells.set("Spells.Break.CostMultiplier", 1);
    		spells.set("Spells.Break.Master", false);
    		spells.set("Spells.Break.Cooldown", 10);
    		spells.set("Spells.Break.CastItemOnly", false);
    		
    		spells.set("Spells.Activate.Name", "tincidunt");
    		spells.set("Spells.Activate.CostMultiplier", 1);
    		spells.set("Spells.Activate.Master", false);
    		spells.set("Spells.Activate.Cooldown", 0);
    		spells.set("Spells.Activate.CastItemOnly", false);
    		
    		spells.set("Spells.Silence.Name", "silentium");
    		spells.set("Spells.Silence.CostMultiplier", 1);
    		spells.set("Spells.Silence.Master", false);
    		spells.set("Spells.Silence.Cooldown", 30);
    		spells.set("Spells.Silence.CastItemOnly", false);
    		
    		spells.save();
    	}
    	else
    		spells.load();
    	
    	// Parse spell nodes for easy lookup <<<<AS OF 11/12/02 Temp Disabled>>>>
    	/*Map<String, ConfigurationNode> spellNodes = spells.getNodes("Spells");
    	for (Map.Entry<String, ConfigurationNode> entry : spellNodes.entrySet())
    	{
    		spellLookup.put(entry.getValue().getString("Name"), entry.getKey());
    	}
    	*/
    	// Read or create users file
    	File usersFile = new File(getDataFolder().getPath() + File.separator + "users.yml");
    	try
    	{
			isNew = spellFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			log.info("[Incantations] Error occured while creating users file: " + e.getMessage());
		}
    	users = new YamlConfiguration.loadConfiguration(usersFile);
    	if (isNew)
    	{
    		users.save();
    	}
    	else
    		users.load();
    	
    }
    
}