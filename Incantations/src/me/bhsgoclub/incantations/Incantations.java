package me.bhsgoclub.incantations;
//DRTSHOCK IS AWESOME
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import net.minecraft.server.Item;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

public class Incantations extends JavaPlugin
{	
    public final Logger log = Logger.getLogger("Minecraft");
    public final Watcher watcher = new Watcher(this);
    
    public Configuration config;
    public Configuration spells;
    public Configuration users;
    
    public final Hashtable<Player, Spellbook> spellbookCollection = new Hashtable<Player, Spellbook>();
    public final Hashtable<String, String> spellLookup = new Hashtable<String, String>();
    
    private final IncantationsPlayerListener playerListener = new IncantationsPlayerListener(this);
    private final IncantationsBlockListener blockListener = new IncantationsBlockListener(this);
    private final IncantationsEntityListener entityListener = new IncantationsEntityListener(this);
    
    public PermissionHandler permissions;
    public Boolean usePermissions = false;
        
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
        pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
        
        // Start watcher thread
        getServer().getScheduler().scheduleSyncRepeatingTask(this, watcher, 20, 5);
        
        setupPermissions();
        
        log.info("[Incantations] v" + getDescription().getVersion() + " active.");
    }
    
    @Override
    public void onDisable()
    {
    	//watcher.stop();
    	watcher.cleanUp();
    	users.save();
    	
        log.info("[Incantations] v" + getDescription().getVersion() + " disabled.");
    }
    
    private void setupPermissions()
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
    }
    
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
    	config = new Configuration(cfgFile);
    	if (isNew)
    	{
    		config.setProperty("General.SpellAnounceLevel", 1);
    		config.setProperty("General.SpellRange", 50);
    		config.setProperty("General.SuperCommand", "magna");
    		config.setProperty("General.CastItem", 280);
    		
    		config.setProperty("Reagents.RegularIsDye", false);
    		config.setProperty("Reagents.Regular", 331);
    		config.setProperty("Reagents.MasterIsDye", true);
    		config.setProperty("Reagents.Master", 4);
    		config.setProperty("Reagents.TransmuteCost", 16);
    		config.setProperty("Reagents.Level1", 1);
    		config.setProperty("Reagents.Level2", 3);
    		config.setProperty("Reagents.Level3", 5);
    		
    		config.setProperty("Spellbook.Enabled", true);
    		config.setProperty("Spellbook.InscriptionCommand", "scripto");
    		config.setProperty("Spellbook.WriteReagentIsDye", true);
    		config.setProperty("Spellbook.WriteReagent", 4);
    		config.setProperty("Spellbook.Cooldown", 5);
    		
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
    	spells = new Configuration(spellFile);
    	if (isNew)
    	{
    		spells.setProperty("Spells.Lightning.Name", "fulmen");
    		spells.setProperty("Spells.Lightning.CostMultiplier", 1);
    		spells.setProperty("Spells.Lightning.Master", false);
    		spells.setProperty("Spells.Lightning.Cooldown", 5);
    		spells.setProperty("Spells.Lightning.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Bubble.Name", "sphaera");
    		spells.setProperty("Spells.Bubble.CostMultiplier", 1);
    		spells.setProperty("Spells.Bubble.Master", false);
    		spells.setProperty("Spells.Bubble.Cooldown", 30);
    		spells.setProperty("Spells.Bubble.CastItemOnly", false);
    		
    		spells.setProperty("Spells.SlowFall.Name", "tarduscado");
    		spells.setProperty("Spells.SlowFall.CostMultiplier", 1);
    		spells.setProperty("Spells.SlowFall.Master", false);
    		spells.setProperty("Spells.SlowFall.Cooldown", 0);
    		spells.setProperty("Spells.SlowFall.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Heal.Name", "remedium");
    		spells.setProperty("Spells.Heal.CostMultiplier", 1);
    		spells.setProperty("Spells.Heal.Master", false);
    		spells.setProperty("Spells.Heal.Cooldown", 0);
    		spells.setProperty("Spells.Heal.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Blink.Name", "transulto");
    		spells.setProperty("Spells.Blink.CostMultiplier", 1);
    		spells.setProperty("Spells.Blink.Master", false);
    		spells.setProperty("Spells.Blink.Cooldown", 30);
    		spells.setProperty("Spells.Blink.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Breathe.Name", "respiro");
    		spells.setProperty("Spells.Breathe.CostMultiplier", 1);
    		spells.setProperty("Spells.Breathe.Master", false);
    		spells.setProperty("Spells.Breathe.Cooldown", 0);
    		spells.setProperty("Spells.Breathe.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Freeze.Name", "frigidus");
    		spells.setProperty("Spells.Freeze.CostMultiplier", 1);
    		spells.setProperty("Spells.Freeze.Master", false);
    		spells.setProperty("Spells.Freeze.Cooldown", 0);
    		spells.setProperty("Spells.Freeze.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Thaw.Name", "concalesco");
    		spells.setProperty("Spells.Thaw.CostMultiplier", 1);
    		spells.setProperty("Spells.Thaw.Master", false);
    		spells.setProperty("Spells.Thaw.Cooldown", 0);
    		spells.setProperty("Spells.Thaw.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Extinguish.Name", "extinguo");
    		spells.setProperty("Spells.Extinguish.CostMultiplier", 1);
    		spells.setProperty("Spells.Extinguish.Master", false);
    		spells.setProperty("Spells.Extinguish.Cooldown", 0);
    		spells.setProperty("Spells.Extinguish.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Rain.Name", "pluvia");
    		spells.setProperty("Spells.Rain.CostMultiplier", 1);
    		spells.setProperty("Spells.Rain.Master", true);
    		spells.setProperty("Spells.Rain.Cooldown", 0);
    		spells.setProperty("Spells.Rain.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Storm.Name", "tempestas");
    		spells.setProperty("Spells.Storm.CostMultiplier", 1);
    		spells.setProperty("Spells.Storm.Master", true);
    		spells.setProperty("Spells.Storm.Cooldown", 0);
    		spells.setProperty("Spells.Storm.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Clear.Name", "sereno");
    		spells.setProperty("Spells.Clear.CostMultiplier", 1);
    		spells.setProperty("Spells.Clear.Master", true);
    		spells.setProperty("Spells.Clear.Cooldown", 0);
    		spells.setProperty("Spells.Clear.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Mutate.Name", "mutatio");
    		spells.setProperty("Spells.Mutate.CostMultiplier", 1);
    		spells.setProperty("Spells.Mutate.Master", false);
    		spells.setProperty("Spells.Mutate.Cooldown", 0);
    		spells.setProperty("Spells.Mutate.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Wall.Name", "clausus");
    		spells.setProperty("Spells.Wall.CostMultiplier", 1);
    		spells.setProperty("Spells.Wall.Master", false);
    		spells.setProperty("Spells.Wall.Cooldown", 30);
    		spells.setProperty("Spells.Wall.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Replenish.Name", "repleo");
    		spells.setProperty("Spells.Replenish.CostMultiplier", 1);
    		spells.setProperty("Spells.Replenish.Master", false);
    		spells.setProperty("Spells.Replenish.Cooldown", 0);
    		spells.setProperty("Spells.Replenish.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Protect.Name", "tueri");
    		spells.setProperty("Spells.Protect.CostMultiplier", 1);
    		spells.setProperty("Spells.Protect.Master", false);
    		spells.setProperty("Spells.Protect.Cooldown", 30);
    		spells.setProperty("Spells.Protect.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Fireball.Name", "ignifera");
    		spells.setProperty("Spells.Fireball.CostMultiplier", 1);
    		spells.setProperty("Spells.Fireball.Master", false);
    		spells.setProperty("Spells.Fireball.Cooldown", 0);
    		spells.setProperty("Spells.Fireball.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Transmute.Name", "transmutare");
    		spells.setProperty("Spells.Transmute.CostMultiplier", 0);
    		spells.setProperty("Spells.Transmute.Master", false);
    		spells.setProperty("Spells.Transmute.Cooldown", 0);
    		spells.setProperty("Spells.Transmute.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Entomb.Name", "sepulcrum");
    		spells.setProperty("Spells.Entomb.CostMultiplier", 1);
    		spells.setProperty("Spells.Entomb.Master", false);
    		spells.setProperty("Spells.Entomb.Cooldown", 0);
    		spells.setProperty("Spells.Entomb.CastItemOnly", false);
    		
    		spells.setProperty("Spells.GlassToIce.Name", "glacia");
    		spells.setProperty("Spells.GlassToIce.CostMultiplier", 1);
    		spells.setProperty("Spells.GlassToIce.Master", false);
    		spells.setProperty("Spells.GlassToIce.Cooldown", 0);
    		spells.setProperty("Spells.GlassToIce.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Light.Name", "lux");
    		spells.setProperty("Spells.Light.CostMultiplier", 1);
    		spells.setProperty("Spells.Light.Master", false);
    		spells.setProperty("Spells.Light.Cooldown", 0);
    		spells.setProperty("Spells.Light.CastItemOnly", false);
    		
    		spells.setProperty("Spells.WaterWalking.Name", "superaquas");
    		spells.setProperty("Spells.WaterWalking.CostMultiplier", 1);
    		spells.setProperty("Spells.WaterWalking.Master", false);
    		spells.setProperty("Spells.WaterWalking.Cooldown", 0);
    		spells.setProperty("Spells.WaterWalking.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Launch.Name", "jaceo");
    		spells.setProperty("Spells.Launch.CostMultiplier", 1);
    		spells.setProperty("Spells.Launch.Master", false);
    		spells.setProperty("Spells.Launch.Cooldown", 5);
    		spells.setProperty("Spells.Launch.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Break.Name", "ruptura");
    		spells.setProperty("Spells.Break.CostMultiplier", 1);
    		spells.setProperty("Spells.Break.Master", false);
    		spells.setProperty("Spells.Break.Cooldown", 10);
    		spells.setProperty("Spells.Break.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Activate.Name", "tincidunt");
    		spells.setProperty("Spells.Activate.CostMultiplier", 1);
    		spells.setProperty("Spells.Activate.Master", false);
    		spells.setProperty("Spells.Activate.Cooldown", 0);
    		spells.setProperty("Spells.Activate.CastItemOnly", false);
    		
    		spells.setProperty("Spells.Silence.Name", "silentium");
    		spells.setProperty("Spells.Silence.CostMultiplier", 1);
    		spells.setProperty("Spells.Silence.Master", false);
    		spells.setProperty("Spells.Silence.Cooldown", 30);
    		spells.setProperty("Spells.Silence.CastItemOnly", false);
    		
    		spells.save();
    	}
    	else
    		spells.load();
    	
    	// Parse spell nodes for easy lookup
    	Map<String, ConfigurationNode> spellNodes = spells.getNodes("Spells");
    	for (Map.Entry<String, ConfigurationNode> entry : spellNodes.entrySet())
    	{
    		spellLookup.put(entry.getValue().getString("Name"), entry.getKey());
    	}
    	
    	// Read or create users file
    	File usersFile = new File(getDataFolder().getPath() + File.separator + "users.yml");
    	try
    	{
			isNew = spellFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			log.info("[Incantations] Error occured while creating users file: " + e.getMessage());
		}
    	users = new Configuration(usersFile);
    	if (isNew)
    	{
    		users.save();
    	}
    	else
    		users.load();
    	
    }
    
}