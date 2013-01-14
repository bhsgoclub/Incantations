package me.bhsgoclub.incantations;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.bhsgoclub.incantations.Incantations;
import me.bhsgoclub.incantations.Spellbook;
import me.bhsgoclub.incantations.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Giant;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Dye;
import org.bukkit.util.Vector;

public class IncantationsPlayerListener implements Listener
{
    public Incantations plugin;
    
    public IncantationsPlayerListener(Incantations instance)
    {
        this.plugin = instance;
    }
    

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
    	Player player = event.getPlayer();
    	
        if(Incantations.watcher.getTicks(player, "SlowFall") > 0)
        {
        	//(BUG: CraftBukkit doesn't report the X and Y values of the velocity, so player can only go straigth down when spell is active.) 
        	//FIXED/WORKED AROUND
        	Vector velocity = event.getTo().toVector().subtract(event.getFrom().toVector());
        	
            //Vector velocity = player.getVelocity();
            //double vx = velocity.getX();
            double vy = velocity.getY();
            //double vz = velocity.getZ();
            if (vy < -0.1D)
            {
                velocity.setY(-0.1D);
                player.setVelocity(velocity);
                player.setFallDistance(0);
            }
            
        }
        
        /*
        if(playerInfo.Haste)
        {
        	Vector velocity = event.getTo().toVector().subtract(event.getFrom().toVector());
        	
            double vx = velocity.getX() * 2.0d;
            double vz = velocity.getZ() * 2.0d;
            if (vx > 2.0d)
            	vx = 2.0d;
            else if (vx < -2.0d)
            	vx = -2.0d;
            if (vz > 2.0d)
            	vz = 2.0d;
            else if (vz < -2.0d)
            	vz = -2.0d;
            velocity.setX(vx);
            velocity.setZ(vz);
            player.setVelocity(velocity);
        }
        */
        
    }
    
    @SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event)
    {
    	Player player = event.getPlayer();
    	ItemStack item = player.getItemInHand();
    	Action action = event.getAction();
    	
    	if (plugin.getConfig().getBoolean("Spellbook.Enabled", true) && item.getType() == Material.BOOK)
    	{
    		Spellbook spellbook = plugin.spellbookCollection.get(player);
    		if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR)
    		{
    			if (spellbook.StoredSpell.size() == 0)
    				return;
    			
    			// Check cooldown
    			Long cd = Incantations.watcher.getCooldown(player, "Spellbook");
    			if (cd > 0)
    			{
    				player.sendMessage("Your spellbook needs to cool down for " + Long.toString(cd / 1000L) + " more seconds");
    				player.sendMessage("before it can be used again.");
    			}
    			else
    			{
	    			String spell = spellbook.StoredSpell.get(spellbook.CurrentSpell);
	    			if (spell != "")
	    			{
	    				CastSpell(spell, player);
	    				Incantations.watcher.addCooldown(player, "Spellbook", plugin.getConfig().getInt("Spellbook.Cooldown", 5) * 1000L);
	    				player.updateInventory();
	    			}
    			}
    		}
    		else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
    		{
    			if (spellbook.StoredSpell.size() == 0)
    				return;
    			
    			if (++spellbook.CurrentSpell >= spellbook.StoredSpell.size())
    				spellbook.CurrentSpell = 0;
    			
    			player.sendMessage(")You turn a page and ready the '" + spellbook.StoredSpell.get(spellbook.CurrentSpell) + "' spell.");
    		}
    		
    	}
    	
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
    	File usersFile = new File(plugin.getDataFolder() + File.separator + "Users.yml");
        FileConfiguration users = YamlConfiguration.loadConfiguration(usersFile);
        
    	Player player = event.getPlayer();
    	Incantations.watcher.addPlayer(player);
    	Spellbook spellbook = new Spellbook();
    	spellbook.StoredSpell = users.getStringList(player.getName() + ".Spellbook");
    	plugin.spellbookCollection.put(player, spellbook);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	Incantations.watcher.removePlayer(event.getPlayer());
    	plugin.spellbookCollection.remove(event.getPlayer());
    }
  
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
    	Player player = event.getPlayer();
    	if (Incantations.watcher.getTicks(player, "Silence") > 0)
    	{
    		player.getServer().broadcastMessage(player.getName() + " drools.");
    		event.setCancelled(true);
    	}
    	//else
    	//{
	    	String message = event.getMessage().trim();
	    	if (CastSpell(message, player))
	    		event.setCancelled(true);
    	//}
    	
    	// player.getItemInHand().setDurability((short)1);
	}
    
    private String CleanUp(String message)
    {
        String output = "";
        
        for (int i = message.length() - 1; i >= 0; i--)
        {
            if (message.charAt(i) != '!')
            {
                output = message.substring(0, i + 1);
                break;
            }
        }
        
        return output.toLowerCase();
    }
    
    private Boolean CastSpell(String message, Player player)
    {
        System.out.println("cast spell");
        File spellsFile = new File(plugin.getDataFolder() + File.separator + "Spells.yml");
        FileConfiguration spells = YamlConfiguration.loadConfiguration(spellsFile);
        
        // Check the strength & filter input
        int strength = 1;
        String superCmd = plugin.getConfig().getString("General.SuperCommand", "magna");
        if (message.length() > superCmd.length() && message.substring(0, superCmd.length() + 1).toLowerCase().equals(superCmd + " "))
        {
        	strength = 3;
        	message = message.substring(6);
        }
        else if (message.toUpperCase() == message || message.charAt(message.length()-1) == '!')
        {
            strength = 2;
        }
        
        message = CleanUp(message);
        
        String writeCmd = plugin.getConfig().getString("Spellbook.InscriptionCommand", "scripto");
        
        // Process spell
        String nodeName = spells.getString("spells.lightning.name");
        
        Bukkit.getServer().broadcastMessage("node: " + nodeName);
        
        if (nodeName != null)
        {
        	System.out.println("yay not null!");

			if(player.hasPermission("incantations.spells"))
			{
	        	// Announce the spell
	        	String announce;
	        	String announceLocal;
	        	if (strength == 1)
	        	{
	        		announce = "§e" + player.getName() + "§3 raises his hands and mutters '§4" + message + "§3'.";
	        		announceLocal = "§3You raise your hands and mutter '§4" + message + "§3'.";
	        	}
	            else if (strength == 2)
	            {
	            	announce = "§e" + player.getName() + "§3 raises his hands and yells '§4" + message + "§3'.";
	        		announceLocal = "§3You raise your hands and yell '§4" + message + "§3'.";
	            }
	            else// if (strength == 3)
	            {
	            	announce = "§e" + player.getName() + "§3 raises his hands and forcefully exclaims '§4" + message + "§3'.";
	        		announceLocal = "§3You raise your hands and forcefully exclaim '§4" + message + "§3'.";
	            }
	        	
	        	int announceLevel = plugin.getConfig().getInt("General.SpellAnnounceLevel", 1);
	        	if (announceLevel < 1)
	        		player.sendMessage(announceLocal);
	        	else if (announceLevel == 1)
	    		{
	        		double size = 10.0d + 15.0f * (strength - 1);
	        		List<Entity> entities = player.getNearbyEntities(size, size, size);
	        		for (Entity entity : entities)
	        		{
	        			if (entity instanceof Player)
	        				((Player)entity).sendMessage(announce);
	        		}
	        		player.sendMessage(announceLocal);
	    		
	    		}
	        	else
	        		player.getServer().broadcastMessage(announce);
	        	
	        	String nodePath = "spells." + nodeName + ".";
		        int cooldown = spells.getInt(nodePath + "Cooldown", 0);
		        long currentCooldown = Incantations.watcher.getCooldown(player, nodeName);
		        if (cooldown > 0 && currentCooldown > 0)
		        {
		        	player.sendMessage(message + " is on cooldown for " + Long.toString(currentCooldown / 1000L) + " more seconds."); 
		        }
		        else
		        {
		        	if (spells.getBoolean(nodePath + "CastItemOnly", false) && !Util.isPlayerHolding(player, spells.getInt("General.CastItem", 280)))
		        	{
		        		player.sendMessage("You need to hold a book to cast that spell.");
		        	}
		        	else
		        	{
		        		Boolean canCast = false;
		        		int costMultiplier = spells.getInt(nodePath + "CostMultiplier", 1);
		        		if (costMultiplier > 0)
		        		{
		        			String reagentType;
		        			int cost;
		        			if (spells.getBoolean(nodePath + "Master", false))
		        			{
		        				reagentType = "Master";
		        				cost = costMultiplier;
		        			}
		        			else
		        			{
		        				reagentType = "Regular";
		        				cost = plugin.getConfig().getInt("Reagents.Level" + Integer.toString(strength), 1 + 2 * (strength - 1)) * costMultiplier;
		        			}
		        			
	        				Boolean isDye = plugin.getConfig().getBoolean("Reagents." + reagentType + "IsDye", false);
	        				int material = plugin.getConfig().getInt("Reagents." + reagentType, (isDye ? 4 : 331));
	        				if (!Util.playerSpendReagent(player, isDye, material, cost))
	    					{
	        					int lowCost = plugin.getConfig().getInt("Reagents.Level1", 1 * costMultiplier);
	        					if (strength != 1 && Util.playerSpendReagent(player, isDye, material, lowCost))
	        					{
	        						player.sendMessage("You need " + Integer.toString(cost) + " " + Util.getItemName(material, isDye) + " to cast the spell at full strength.");
	        						strength = 1;
	        						canCast = true;
	        					}
	        					else
	        						player.sendMessage("You need " + Integer.toString(cost) + " " + Util.getItemName(material, isDye) + " to cast that spell.");
	    					}
	        				else
	        					canCast = true;
		        		}
		        		else
		        			canCast = true;
		        		
		        		if (canCast)
		        		{
					        if (nodeName.equals("Wall"))
					            wall(player, strength);
					        else if (nodeName.equals("GlassToIce"))
					        	glassToIce(player, strength);
					        else if (nodeName.equals("Entomb"))
					        	entomb(player, strength);
					        /*
					        else if (nodeName.equals("Escape"))
					            escape(player);
					            */
					        else if (nodeName.equals("replenish"))
					            replenish(player, strength);
					        else if (nodeName.equals("bubble"))
					            bubble(player, strength);
					        else if (nodeName.equals("lightning"))
					            lightning(player, strength);
					        else if (nodeName.equals("light"))
					            light(player, strength);
					        else if (nodeName.equals("heal"))
					        	heal(player, strength);
					        else if (nodeName.equals("blink"))
					        	blink(player);
					        else if (nodeName.equals("breathe"))
					        	breathe(player, strength);
					        else if (nodeName.equals("freeze"))
					            freeze(player, strength);
					        else if (nodeName.equals("thaw"))
					            thaw(player, strength);
					        else if (nodeName.equals("extinguish"))
					            extinguish(player, strength);
					        else if (nodeName.equals("rain"))
					            rain(player);
					        else if (nodeName.equals("storm"))
					            storm(player);
					        else if (nodeName.equals("clear"))
					            clear(player);
					        else if (nodeName.equals("mutate"))
					            mutate(player, strength);
					        else if (nodeName.equals("activate"))
					            activate(player, strength);
					        else if (nodeName.equals("fireball"))
					        	fireball(player, strength);
					        /*
					        else if (message.equals("pulsus":
					            Pulsus(strength, ev);
					        */
					        else if (nodeName.equals("slowFall"))
					        	slowFall(player, strength);
					        else if (nodeName.equals("protect"))
					            protect(player, strength);
					        else if (nodeName.equals("waterwalking"))
					        	waterWalking(player,strength);
					        else if (nodeName.equals("transmute"))
					        	transmute(player);
					        else if (nodeName.equals("launch"))
					        	launch(player, strength);
					        else if (nodeName.equals("break"))
					        	_break(player, strength);
					        else if (nodeName.equals("silence"))
					        	silence(player, strength);
					        
					        if (cooldown > 0)
					        	Incantations.watcher.addCooldown(player, nodeName, cooldown * 1000L);
		        		}
		        	}
		        }
		        return true;
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You don't have permission to cast that.");
				return false;
			}
        }
        else if (plugin.getConfig().getBoolean("Spellbook.Enabled", true)
        		&& message.length() > writeCmd.length() && message.substring(0, writeCmd.length() + 1).toLowerCase().equals(writeCmd + " "))
        {
        	if (!player.hasPermission("incantations.spellbook"))
			{
				player.sendMessage("You can't use a spellbook.");
			}
			else
			{
				writeSpell(player, message.substring(8));
        		return true;
			}
        }
        return false;
        
    }
    
    private void writeSpell(Player player, String spell)
    {
    	
    	File usersFile = new File(plugin.getDataFolder() + File.separator + "Users.yml");
        FileConfiguration users = YamlConfiguration.loadConfiguration(usersFile);
        
		Boolean isDye = plugin.getConfig().getBoolean("Spellbook.WriteReagentIsDye", true);
		int material = plugin.getConfig().getInt("Spellbook.WriteReagent", 4);
		String nodeName = plugin.spellLookup.get(spell);
		if (nodeName == null)
		{
			player.sendMessage("You have no idea how to use the " + spell + " spell.");
		}
		else if (!Util.isPlayerHoldingBook(player))
    	{
    		player.sendMessage("You need to hold a book to inscribe a spell.");
    	}
		else if (plugin.getConfig().getBoolean("Spells." + nodeName + ".Master", false))
		{
			player.sendMessage("Cannot inscribe master spells.");
		}
		else if (!Util.playerSpendReagent(player, isDye, material, 1))
		{
			player.sendMessage("You need 1 " + Util.getItemName(material, isDye) + " to inscribe a spell.");
		}		
    	else
    	{
    		if (spell.length() >= 6 && spell.substring(0, 6).equals("magna "))
	    		spell = spell.substring(6);
	    	
    		Spellbook spellbook = plugin.spellbookCollection.get(player);
    		if (spellbook.StoredSpell.contains(spell))
    		{
    			player.sendMessage("You tear a page out of your spellbook, removing the '" + spell + "' spell.");
    			player.getWorld().dropItem(player.getLocation(), new ItemStack(Material.PAPER, 1));
    			
    			spellbook.StoredSpell.remove(spell);
    			users.set(player.getName() + ".Spellbook", spellbook.StoredSpell);
    			
    			if (spellbook.CurrentSpell >= spellbook.StoredSpell.size())
    				spellbook.CurrentSpell = 0;
    		}
    		else
    		{
    			spellbook.StoredSpell.add(spell);
    			users.set(player.getName() + ".Spellbook", spellbook.StoredSpell);
    			//item.setDurability((short)6);
    			player.sendMessage("You inscribe the spell '" + spell + "' in your spellbook.");
    		}
    	
		}
		
    }
    
    private void silence(Player player, int strength)
    {
    	// lol
    	long time = strength * 5000L;
    	
    	List<LivingEntity> targets = Util.getPlayerTarget(player, plugin.getConfig().getInt("General.SpellRange", 50), true);
    	
    	if (targets.size() != 0)
    	{
    		Player target = (Player)targets.get(0);
    		
    		Long duration = Incantations.watcher.getTicks(target, "SilenceImmunity");
        	if (duration > 0)
        	{
        		player.sendMessage(target.getName() + " is immune for " + Long.toString(duration / 1000L) + " more seconds.");
        	}
    		else
    		{
	    		Incantations.watcher.addTicker(target, "Silence", time, 4);
	    		Incantations.watcher.addTicker(target, "SilenceImmunity", 60000L, 4);
	    		target.sendMessage("You have been silenced for " + Long.toString(time / 1000L) + " seconds.");
	    		player.sendMessage("You silence " + target.getName() + " for " + Long.toString(time / 1000L) + " seconds.");
	    		player.getWorld().playEffect(target.getLocation(), Effect.SMOKE, 1);
    		}
    	}
    	
    }
    
    private void _break(Player player, int strength)
    {
    	int radius = 2;
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();
        World world = player.getWorld();
        
        for (int i = 0; i < strength; i++)
        {
        	int xorg = location.getBlockX();
            int yorg = location.getBlockY();
            int zorg = location.getBlockZ();
        	
	        for (int x = xorg - radius - 1; x <= xorg + radius + 1; x++)
	        {
	            for (int y = yorg - radius - 1; y <= yorg + radius + 1; y++)
	            {
	                for (int z = zorg - radius - 1; z <= zorg + radius + 1; z++)
	                {
	                	if (y < 1 || y > 128) continue;
	                	
	                    int dx = x - xorg;
	                    int dy = y - yorg;
	                    int dz = z - zorg;
	
	                    if ((int)Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius)
	                    {
	                        Block block = world.getBlockAt(x, y, z);
	                        Material type = block.getType();
	                        if (type != Material.WATER && type != Material.STATIONARY_WATER
	                    		&& type != Material.LAVA && type != Material.STATIONARY_LAVA
	                    		&& type != Material.BEDROCK && type != Material.CHEST && type != Material.OBSIDIAN
	                    		&& type != Material.PORTAL && type != Material.ICE && type != Material.FURNACE)
	                		{
	                        	Incantations.watcher.addBlock(block, 5000L + 1000L * i);
	                        	block.setType(Material.AIR);
	                		}
	                        
	                    }
	                    
	                }
	            }
	        }
	        
	        if (strength > 1)
	        {
	        	Vector target = location.toVector();
	        	//Vector direction = location.toVector().subtract(target).normalize();
	        	Vector direction = player.getEyeLocation().getDirection().normalize();
	        	Vector newTarget = target.add(direction.multiply(5));
	        	location = newTarget.toLocation(world);
	        }
	        
        }
        
    }
    
    private void launch(Player player, int strength)
    {
    	List<LivingEntity> targets = Util.getPlayerTarget(player, 10.0D);
    	float force = 1.0f + (float)strength / 5.0f;
    	for (LivingEntity target : targets)
    	{
    		target.setVelocity(new Vector(0.0f, force, 0.0f));
    	}
    }
    
    private void glassToIce(Player player, int strength)
    {
    	World world = player.getWorld();
    	
        int radius = 5 * strength;
    	
    	ArrayList<Block> activeBlocks = new ArrayList<Block>();
    	
    	Block targetBlock = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50));
    	int xorg = targetBlock.getX();
        int yorg = targetBlock.getY();
        int zorg = targetBlock.getZ();
        
        if (targetBlock.getType() != Material.GLASS)
        	return;
        else
        	targetBlock.setType(Material.ICE);
        
        activeBlocks.add(targetBlock);
        
        while (activeBlocks.size() > 0)
        {
	    	for (int i = activeBlocks.size() - 1; i >= 0; i--)
	    	{
	    		//Block block = iter.next();
	    		Block block = activeBlocks.get(i);
	    		
	    		//TODO: Permanent magic ice?
	    		//block.setType(Material.ICE);
	    		activeBlocks.remove(block);
	    		
	    		int blockX = block.getX();
	    		int blockY = block.getY();
	    		int blockZ = block.getZ();
	    		
	    		for (int x = blockX - 1; x <= blockX + 1; x++)
	    		{
	    			for (int y = blockY - 1; y <= blockY + 1; y++)
	    			{
	    				for (int z = blockZ - 1; z <= blockZ + 1; z++)
	    				{
	    					Block relative = world.getBlockAt(x, y, z);
	    					if (relative.getType() == Material.GLASS)
	    					{
	    						int dx = blockX - xorg;
	    						int dy = blockY - yorg;
	    						int dz = blockZ - zorg;
	    						
	    						if ((int)Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius)
	    						{
	    							block.setType(Material.ICE);
	    							activeBlocks.add(relative);
	    						}
	    						
	    					}
	    				}
	    			}
	    		}
	    		
	    	}
	    	
        }
        
    }
    
    private void waterWalking(Player player, int strength)
    {
    	long time;
    	if (strength == 1)
        	time = 10000L;
        else if (strength == 2)
        	time = 20000L;
        else// if (strength == 3)
        	time = 30000L;
    	Incantations.watcher.addTicker(player, "WaterWalking", time, 1);
    }
    
    private void slowFall(Player player, int strength)
    {
    	long time = strength * 10000L;
    	Incantations.watcher.addTicker(player, "SlowFall", time, 4);
    	player.sendMessage("Your falling speed is decreased for " + Long.toString(time / 1000L) + " seconds.");
    }
    
    private void heal(Player player, int strength)
    {
    	int frequency = 4;
    	if (strength == 2)
    		frequency = 2;
    	else if (strength == 3)
    		frequency = 1;
    	Incantations.watcher.addTicker(player, "Heal", 20000L, frequency);
    }
    
    private void breathe(Player player, int strength)
    {
    	long time;
    	if (strength == 3)
        	time = 120000L;// + 30 * (strength - 1);
        else if (strength == 2)
        	time = 60000L;
        else
        	time = 30000L;
    	Incantations.watcher.addTicker(player, "Breathe", time, 4);
    	player.sendMessage("You have infinite air for " + Long.toString(time / 1000L) + " seconds.");
    }
    
    private void blink(Player player)
    {
    	 World world = player.getWorld();
    	 Location origin = player.getLocation();
         
         Location target = player.getLastTwoTargetBlocks(null, plugin.getConfig().getInt("General.SpellRange", 50)).get(0).getLocation();
         world.playEffect(origin, Effect.SMOKE, 1);
         // Disable damage for a second
         player.setNoDamageTicks(20);
         target.setPitch(origin.getPitch());
         target.setYaw(origin.getYaw());
         player.teleport(target);
         world.playEffect(target, Effect.SMOKE, 1);
    }
    
    private void protect(Player player, int strength)
    {
         Long duration = 1000L + 2000L * strength;
         Incantations.watcher.addTicker(player, "Protect", duration, 4);
         player.sendMessage("You are protected against all damage for " + Long.toString(duration / 1000L) + " seconds.");
    }
    
    enum PlayerHoldingType
	{
		Invalid,
		Regular,
		Master
	}
    private void transmute(Player player)
    {
    	ItemStack item = player.getItemInHand();
    	int amount = item.getAmount();
    	PlayerInventory inventory = player.getInventory();
    	
    	// Gather info about reagents
    	Boolean regularIsDye = plugin.getConfig().getBoolean("Reagents.RegularIsDye", false);
    	int regular = plugin.getConfig().getInt("Reagents.Regular", 331);
    	Boolean masterIsDye = plugin.getConfig().getBoolean("Reagents.MasterIsDye", true);
    	int master = plugin.getConfig().getInt("Reagents.Master", 4);
    	
    	// Check if what the player is holding is a reagent
    	PlayerHoldingType holding = PlayerHoldingType.Invalid;
    	if (item.getType() == Material.INK_SACK)
    	{
    		if (regularIsDye && item.getDurability() == regular)
    			holding = PlayerHoldingType.Regular;
    		else if (masterIsDye && item.getDurability() == master)
    			holding = PlayerHoldingType.Master;
    	}
		else if (item.getTypeId() == regular)
			holding = PlayerHoldingType.Regular;
		else if (item.getTypeId() == master)
			holding = PlayerHoldingType.Master;
    	
    	int cost = plugin.getConfig().getInt("Reagents.TransmuteCost", 15); 
    	
    	if (holding == PlayerHoldingType.Regular)
    	{
    		// Spend regular reagent
    		if (amount < cost)
            {
            	player.sendMessage("Not enough " + Util.getItemName(regular, regularIsDye) + ", you need " + Integer.toString(cost) + ".");
                return;
            }
    		else if (amount == cost)
            {
                inventory.clear(inventory.getHeldItemSlot());
            }
    		else if (amount > cost)
            {
            	item.setAmount(amount - cost);
            }
    		
    		// Add master reagent
    		if (masterIsDye)
    		{
    			Dye reagent = new Dye();
    			reagent.setData((byte)master);
    			inventory.addItem(reagent.toItemStack(1));
    		}
    		else
    		{
    			ItemStack reagent = new ItemStack(master, 1);
    			inventory.addItem(reagent);
    		}
    	}
    	else if (holding == PlayerHoldingType.Master)
    	{
    		// Spend master reagent
    		if (amount == 1)
            {
                inventory.clear(inventory.getHeldItemSlot());
            }
    		else if (amount > 1)
            {
            	item.setAmount(amount - 1);
            }
    		
    		// Add regular reagents
    		if (regularIsDye)
    		{
    			Dye reagent = new Dye();
    			reagent.setData((byte)regular);
    			inventory.addItem(reagent.toItemStack(cost));
    		}
    		else
    		{
    			ItemStack reagent = new ItemStack(regular, cost);
    			inventory.addItem(reagent);
    		}
    	}
    	else
    	{
    		player.sendMessage("Cannot transform that material.");
    	}
    	
    }
    
    private void entomb(Player player, int strength)
    {
    	long time = strength * 10000L;
    	
    	// Find target
    	Block targetBlock = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50));
    	Location targetBlockLocation = targetBlock.getLocation();
    	World world = player.getWorld();

        List<Player> players = world.getPlayers();
        
        for (Player target : players)
        {
            Location targetLocation = target.getLocation();
            if (target != player
              && targetBlockLocation.distance(targetLocation) <= 5.0D)
            {
            	Long duration = Incantations.watcher.getTicks(target, "EntombImmunity");
            	if (duration > 0)
            	{
            		player.sendMessage(target.getName() + " is immune for " + Long.toString(duration / 1000L) + " more seconds.");
            	}
            	else
            	{
	            	player.setNoDamageTicks(20);
	            	
	            	// Give target entomb immmunity for 1min
	            	Incantations.watcher.addTicker(target, "EntombImmunity", 60000L, 4);
	    		    
	    		    int tx = targetLocation.getBlockX();
	    		    int ty = targetLocation.getBlockY();
	    		    int tz = targetLocation.getBlockZ();
	    		    
	    		    // Obsidian tomb
	    		    Block[] tomb =
			    	{
			    		// Bottom layer
			    		world.getBlockAt(tx - 1, ty - 1, tz - 1), world.getBlockAt(tx, ty - 1, tz - 1), world.getBlockAt(tx + 1, ty - 1, tz - 1),
			    		world.getBlockAt(tx - 1, ty - 1, tz), world.getBlockAt(tx, ty - 1, tz), world.getBlockAt(tx + 1, ty - 1, tz),
			    		world.getBlockAt(tx - 1, ty - 1, tz + 1), world.getBlockAt(tx, ty - 1, tz + 1), world.getBlockAt(tx + 1, ty - 1, tz + 1),
			    		
			    		// Feet layer
			    		world.getBlockAt(tx - 1, ty, tz - 1), world.getBlockAt(tx, ty, tz - 1), world.getBlockAt(tx + 1, ty, tz - 1),
			    		world.getBlockAt(tx - 1, ty, tz), world.getBlockAt(tx + 1, ty, tz),
			    		world.getBlockAt(tx - 1, ty, tz + 1), world.getBlockAt(tx, ty, tz + 1), world.getBlockAt(tx + 1, ty, tz + 1),
			    		
			    		// Eye-level layer
			    		world.getBlockAt(tx - 1, ty + 1, tz - 1), world.getBlockAt(tx, ty + 1, tz - 1), world.getBlockAt(tx + 1, ty + 1, tz - 1),
			    		world.getBlockAt(tx - 1, ty + 1, tz), world.getBlockAt(tx + 1, ty + 1, tz),
			    		world.getBlockAt(tx - 1, ty + 1, tz + 1), world.getBlockAt(tx, ty + 1, tz + 1), world.getBlockAt(tx + 1, ty + 1, tz + 1),
			    		
			    		// Top layer
			    		world.getBlockAt(tx - 1, ty + 2, tz - 1), world.getBlockAt(tx, ty + 2, tz - 1), world.getBlockAt(tx + 1, ty + 2, tz - 1),
			    		world.getBlockAt(tx - 1, ty + 2, tz), world.getBlockAt(tx, ty + 2, tz), world.getBlockAt(tx + 1, ty + 2, tz),
			    		world.getBlockAt(tx - 1, ty + 2, tz + 1), world.getBlockAt(tx, ty + 2, tz + 1), world.getBlockAt(tx + 1, ty + 2, tz + 1)
			    	};
	    		    
	    		    for (int i = 0; i < tomb.length; i++)
	    		    {
	    		    	Material type = tomb[i].getType();
	    		    	if (type != Material.BEDROCK)
			        	{
			        		Incantations.watcher.addBlock(tomb[i], time - (long)(Math.random() * 2000.0d));
			        		
			        		tomb[i].setType(Material.DIRT);
			        		tomb[i].setType(Material.OBSIDIAN);
			        	}
	    		    }
	    		    
	    		    // Signs
	    		    Block[] sign =
			    	{
						world.getBlockAt(tx, ty + 1, tz - 2),
						world.getBlockAt(tx, ty + 1, tz + 2),
			    		world.getBlockAt(tx - 2, ty + 1, tz),
			    		world.getBlockAt(tx + 2, ty + 1, tz)
			    	};
	    		    
	    		    for (int i = 0; i < sign.length; i++)
	    		    {
		    		    if (sign[i].getType() == Material.AIR)
		    		    {
		    		    	Incantations.watcher.addBlock(sign[i], time - 2000L);
			        		
			        		sign[i].setType(Material.WALL_SIGN);
			        		sign[i].setData((byte)(2 + i));
			        		Sign signBlock = (Sign)sign[i].getState();
			        		signBlock.setLine(0, "Here lies");
			        		signBlock.setLine(1, target.getName());
			        		signBlock.setLine(2, "R.I.P");
			        		SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy");
			        		signBlock.setLine(3, date.format(new Date()));
		    		    }
	    		    }
	    		    
	    		    target.sendMessage("You have been buried alive.");
	    		    
	    		    //TODO: Center targets position to avoid damage?
	    		    //NOTE: This doesn't work:
	    		    //target.teleport(targetLocation);
	    		    
	    		    break;
            	}
            }
        }
        
    }
    /*
    private void escape(Player player)
    {
    	
    }
    */
    private void fireball(Player player, int strength)
    {
    	Location location = player.getLocation();
        Location target = location.add(location.getDirection().normalize().multiply(2).toLocation(player.getWorld(), location.getYaw(), location.getPitch())).add(0.0D, 1.0D, 0.0D);
        
    	switch (strength)
    	{
    		case 1:
    			player.launchProjectile(Egg.class); //What it should be but not sure how to make that work      FIXED-bhs
    			player.sendMessage("You fumble and throw an egg.");
    			break;
    		case 2:
    			player.launchProjectile(Snowball.class); //What it should be but not sure how to make that work      Fixed-bhs
    			player.sendMessage("You strain yourself, but you only manage to throw a snowball.");
    			break;
    		case 3:
    			player.getWorld().spawn(target, Fireball.class);
    			break;
    	}
    	
    }
    
    private void mutate(Player player, int strength)
    {
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();
        World world = player.getWorld();
        
        List<Entity> entities = world.getEntities();
        
        // Used to increase chances of bad stuff happening when lots of mobs are nearby
        double badLuck = 0.0d;
        
        for (Entity entity : entities)
        {
            Location oloc = entity.getLocation();
            if ((location.distance(oloc) <= 10.0D)
              && ((entity instanceof LivingEntity) && !(entity instanceof HumanEntity) && !(entity instanceof Giant)))
            {
                Location eloc = entity.getLocation();
                
                // Don't morph owned wolves
                if (entity instanceof Wolf && ((Wolf)entity).isTamed())
                	return;
                
                EntityType[] creatures = { EntityType.CHICKEN, EntityType.COW, EntityType.SHEEP, EntityType.SQUID,EntityType.WOLF, EntityType.SLIME };
                EntityType[] monsters = { EntityType.CREEPER, EntityType.SKELETON, EntityType.SLIME, EntityType.SPIDER, EntityType.PIG_ZOMBIE, EntityType.ZOMBIE, EntityType.WOLF }; 
                
                EntityType creature;
                double chance = Math.random();
                Boolean passive = false;
                if (chance < 0.01d) // 1%
                {
                	// You fucked up now..
                	// lol -drt
	                creature = EntityType.GIANT;
	                player.sendMessage("You completely botch the spell!");
                }
                else if (chance < 0.02d) // 1%
                {
                	// ...but this might be even worse.
                	creature = EntityType.GHAST;
                	player.sendMessage("You completely botch the spell and summon a creature from the nether!");
                }
                else if (chance < (0.22d + badLuck) / strength) // 20%
                {
                	// Backfire
                	int i = (int)(Math.random() * monsters.length);
	                creature = monsters[i];
	                player.sendMessage("The spell backfires!");
                }
                else // 78%
                {
	                int i = (int)(Math.random() * creatures.length);
	                creature = creatures[i];
	                passive = true;
                }
                //Not sure if this will work, but removed deprecation
                // Replace entity
                int oldHealth = ((LivingEntity)entity).getHealth();
                entity.remove();
                world.playEffect(eloc, Effect.SMOKE, 1);
                Entity spawned = world.spawnEntity(eloc, creature);
                ((LivingEntity) spawned).setHealth(oldHealth);
                
                // Bad wolves
                if (spawned instanceof Wolf && !passive)
                	((Wolf)spawned).setAngry(true);
                // And good slimes
                else if (spawned instanceof Slime)
                {
                	if (passive)
                		((Slime)spawned).setSize(1);
                	else
                		((Slime)spawned).setSize(5);
                }
                // And make sure all other monsters are always angry
                else if (spawned instanceof PigZombie)
                {
                	((PigZombie)spawned).setTarget(player);
                }
                else if (spawned instanceof Spider)
                {
                	((Spider)spawned).setTarget(player);
                }
                
                // Increase badLuck in case another entity was nearby enough
                badLuck += 0.20d;
                
            }
        }
    }
  /*
	private void Domus(String[] components, PlayerChatEvent ev)
	{
		if (PlayerSpendReagent(ev.getPlayer(), Material.BOOK, 1))
			ev.getPlayer().teleport(ev.getPlayer().getWorld().getSpawnLocation());
	}
   */
    
    private void lightning(Player player, int strength)
    {
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();
        int xorg = location.getBlockX(); int yorg = location.getBlockY(); int zorg = location.getBlockZ();
        World world = player.getWorld();
        world.strikeLightning(location);
        
        Block block;
        
        if (strength == 1)
        {
        	block = world.getBlockAt(location);
	    	if (block.getType() == Material.SAND)
	    		block.setType(Material.GLASS);
        }
        if (strength == 2)
        {
        	block = world.getBlockAt(xorg, yorg + 1, zorg);
	    	//if (block.getType() == Material.AIR)
    		block.setType(Material.FIRE);
        }
        if (strength > 1)
        {
	        for (int x = xorg - 1; x <= xorg + 1; x++)
	        {
	            for (int z = zorg - 1; z <= zorg + 1; z++)
	            {
	            	// Sand -> glass
	            	block = world.getBlockAt(x, yorg, z);
			    	if (block.getType() == Material.SAND && (Math.random() <= 0.40D))
			    		block.setType(Material.GLASS);
			    	else if (strength > 2)
			    	{
			    		// If the spell is strong enough, turn the block above into fire.
			    		// This won't work for glass blocks, so we use the other ones.
			    		block = world.getBlockAt(x, yorg + 1, z);
				    	if (block.getType() == Material.AIR)
				    		block.setType(Material.FIRE);
			    	}
	            }   
	        }
        }
        
    }

    private void clear(Player player)
    {
        player.getWorld().setStorm(false);
        player.getWorld().setThundering(false);
    }

    private void storm(Player player)
    {
        player.getWorld().setStorm(true);
        player.getWorld().setThundering(true);
    }
    
    private void activate(Player player, int strength)
    {
    	World world = player.getWorld();
    	Block target = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50));
    	int tx = target.getX();
    	int ty = target.getY();
    	int tz = target.getZ();
    	
    	int radius = 5 * strength;
    	
    	// Simple cubic lookup
    	for (int x = tx - radius; x <= tx + radius; x++)
    	{
    		for (int y = ty - radius; y <= ty + radius; y++)
    		{
    			for (int z = tz - radius; z <= tz + radius; z++)
    			{
    				Block current = world.getBlockAt(x, y, z);
    				if (current.getType() == Material.REDSTONE_TORCH_ON)
    				{
    					Incantations.watcher.addBlock(current, 5000L);
    					current.setType(Material.AIR);
    				}
    				
    			}
    		}
    	}
    		
    	
    }
    
    private void rain(Player player)
    {
        player.getWorld().setStorm(true);
        player.getWorld().setThundering(false);
    }
    
    private void extinguish(Player player, int strength)
    {
        player.setFireTicks(0);
        player.getWorld().playEffect(player.getLocation(), Effect.EXTINGUISH, 1);
        
        if (strength > 1)
        {
        	World world = player.getWorld();
        	
        	int radius = 7 * strength;
            Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();

            int xorg = location.getBlockX();
            int yorg = location.getBlockY();
            int zorg = location.getBlockZ();

            for (int x = xorg - radius - 1; x <= xorg + radius + 1; x++)
            {
                for (int y = yorg - radius - 1; y <= yorg + radius + 1; y++)
                {
                	for (int z = zorg - radius - 1; z < zorg + radius + 1; z++)
                	{
	                    if (y < 1)
	                    	y = 1;
	                    else if (y > 120)
	                    {
	                    	y = 120;
	                    }
	                	
	                    int dx = x - xorg;
	                    int dy = y - yorg;
	                    int dz = z - zorg;
	                	
	                    if ((int)Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius)
	                    {
	                    	Block block = world.getBlockAt(x, y, z);
	                        Material type = block.getType();
	                        if (type == Material.FIRE)
	                        {
	                        	block.setType(Material.DIRT);
	                        	block.setType(Material.AIR);
	                        }
	                    }
                	}
                }
            }    
        }
        
    }

    private void replenish(Player player, int strength)
    {
    	if (!player.isOp())
    		return;
    	
        int radius = 7 * strength;
        
        //HashSet transparent = null;
        Block block = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50));

        Location origin = block.getLocation();

        int xorg = (int)origin.getX();
        int yorg = (int)origin.getY();
        int zorg = (int)origin.getZ();

        if (Math.abs(yorg - 63) < radius)
        {
            int y = 63;
            
            for (int x = xorg - radius - 1; x <= xorg + radius + 1; x++)
            {
                for (int z = zorg - radius - 1; z <= zorg + radius + 1; z++)
                {
                    int dx = x - xorg;
                    int dz = z - zorg;

                    if ((int)Math.sqrt(dx * dx + dz * dz) <= radius)
                    {
                        Block targetblock = player.getWorld().getBlockAt(x, y, z);
                        Material type = targetblock.getType();
                        if ((type == Material.AIR) || (type == Material.WATER) || (type == Material.STATIONARY_WATER))
                        {
                            targetblock.setType(Material.DIRT);
                            targetblock.setType(Material.STATIONARY_WATER);
                        }
                    }
                }
            }
        }
    }
    
    private void thaw(Player player, int strength)
    {
        int radius = 5 * strength;
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();

        int xorg = location.getBlockX();
        int yorg = location.getBlockY();
        int zorg = location.getBlockZ();

        for (int x = xorg - radius - 1; x <= xorg + radius + 1; x++)
        {
            for (int y = yorg - radius - 1; y <= yorg + radius + 1; y++)
            {
                for (int z = zorg - radius - 1; z <= zorg + radius + 1; z++)
                {
                    if (y < 1)
                        y = 1;
                    else if (y > 120)
                        y = 120;
                    
                    int dx = x - xorg;
                    int dy = y - yorg;
                    int dz = z - zorg;

                    if ((int)Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius)
                    {
                        Block block = player.getWorld().getBlockAt(x, y, z);
                        Material type = block.getType();
                        
                        if (type == Material.ICE)
                        {
                        	// Fix for permanent water
                        	if (!Incantations.watcher.removeBlock(block))
                        		block.setType(Material.STATIONARY_WATER);
                        }
                        else if (type == Material.SNOW)
                            block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    private void freeze(Player player, int strength)
    {
        int radius = 5 * strength;
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();

        int xorg = location.getBlockX();
        int yorg = location.getBlockY();
        int zorg = location.getBlockZ();

        for (int x = xorg - radius - 1; x <= xorg + radius + 1; x++)
        {
            for (int y = yorg - radius - 1; y <= yorg + radius + 1; y++)
            {
                for (int z = zorg - radius - 1; z <= zorg + radius + 1; z++)
                {
                	if (y < 1 || y > 128) continue;
                	/*
                    if (y < 1)
                        y = 1;
                    else if (y > 128)
                        y = 128;
            	 	*/
                    int dx = x - xorg;
                    int dy = y - yorg;
                    int dz = z - zorg;

                    if ((int)Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius)
                    {
                        Block block = player.getWorld().getBlockAt(x, y, z);
                        Block bottomblock = player.getWorld().getBlockAt(x, y - 1, z);
                        Material type = block.getType();
                        byte data = block.getData();
                        Material bottomtype = bottomblock.getType();
                        if ((type == Material.WATER || type == Material.STATIONARY_WATER)
                        	&& (data & 0x7) == 0x0 && (data & 0x8) != 0x8)
                          block.setType(Material.ICE);
                        else if ((type == Material.AIR) && (bottomtype != Material.AIR) && (bottomtype != Material.SNOW)) //TODO: Check for plants?
                          block.setType(Material.SNOW);
                        else if (type == Material.STATIONARY_LAVA || type == Material.LAVA)
                		{
                        	// If it's not falling, stationary
                        	if ((data & 0x7) == 0x0 && (data & 0x8) != 0x8)
                        		block.setType(Material.OBSIDIAN);
                    		else
                    			block.setType(Material.COBBLESTONE);
                		}
                        
                    }
                    
                }
            }
        }
    }
    
    private void light(Player player, int strength)
    {
    	long time;
    	if (strength == 1)
    		time = 60000L;
    	else if (strength == 2)
    		time = 120000L;
    	else //if (strength == 3)
    		time = 300000L;
    	
        Block block = player.getLastTwoTargetBlocks(null, plugin.getConfig().getInt("General.SpellRange", 50)).get(0);
        //TODO: Expand based on strength, or replace with portable light
        // Should always be air?
        Material type = block.getType();
        if (type != Material.BEDROCK)
        {
        	Incantations.watcher.addBlock(block, time);
            block.setType(Material.GLOWSTONE);
        }
    }
    
    private void wall(Player player, int strength)
    {
        int length = 5 + 5 * strength;

        Vector vector = player.getEyeLocation().getDirection().normalize();
        Location location = player.getTargetBlock(null, plugin.getConfig().getInt("General.SpellRange", 50)).getLocation();

        int xorg = location.getBlockX();
        int yorg = location.getBlockY();
        int zorg = location.getBlockZ();

        double vx = vector.getX();
        double vz = vector.getZ();

        double ox = -vz;
        double oz = vx;
        
        for (int i = -length / 2; i <= length / 2 + 1; i++)
        {
            location.setX(xorg + i * ox);
            location.setZ(zorg + i * oz);
            for (int y = yorg; y <= yorg + 5; y++)
            {
                if (y < 1 || y > 128) continue;
                location.setY(y);
                Block block = location.getBlock();
                Material type = block.getType();
                if (type == Material.AIR || type == Material.SNOW || type == Material.STATIONARY_WATER || type == Material.WATER)
                {
                	Incantations.watcher.addBlock(block, 15000L - (y - yorg + 1) * 500L);
                    block.setType(Material.WEB);
            	}
            }
        }
    }
    
    private void bubble(Player player, int strength)
    {
    	World world = player.getWorld();
    	
    	Location location = player.getLocation();
    	int playerX = location.getBlockX();
    	int playerY = location.getBlockY();
    	int playerZ = location.getBlockZ();
    	
    	int radius = 4; 
    	int radiusSq = radius * radius;
    	
        float size = (float)(2.0f * Math.ceil((double)radius)) + 1.0f;
        int halfSize = (int)size / 2;
        double offset = Math.floor(size / 2.0f);
        
        //for (int z = 1; z < size - 1; z++) ??? forgot why I did this, might be XNA specific?
        for (int z = 0; z < size; z++)
        {
        	int actualZ = playerZ + z - halfSize;
        	
        	for (int y = 0; y < size; y++)
            {
        		int actualY = playerY + y - halfSize;
        		if (actualY < 1 || actualY > 128) continue;
        		
        		for (int x = 0; x < size; x++)
                {
        			int actualX = playerX + x - halfSize;
    				/*
        			// Save air blocks, this *should* fix the concalesco bug
        			if (type == Material.AIR)
					{
						IncantatioBlockInfo blockInfo = new IncantatioBlockInfo();
                        blockInfo.time = new Date().getTime() + 10000L * strength;
                        blockInfo.original = type;
                        Incantatio.magicBlocks.put(block, blockInfo);
                        this.cleanupThread.main();
					}
        			*/
        			if ((Math.pow(x - offset, 2.0D) + Math.pow(y - offset, 2.0D) + Math.pow(z - offset, 2.0D) < radiusSq))	
                    {
        				Block block = world.getBlockAt(actualX, actualY, actualZ);
        				Material type = block.getType();
        				
        				if (IsFull(x - 1, y, z, offset, radiusSq) && IsFull(x + 1, y, z, offset, radiusSq) && IsFull(x, y - 1, z, offset, radiusSq)
						  && IsFull(x, y + 1, z, offset, radiusSq) && IsFull(x, y, z - 1, offset, radiusSq) && IsFull(x, y, z + 1, offset, radiusSq))
        				{
        					// When underwater, make a temporary bubble
        					if (type == Material.STATIONARY_WATER || type == Material.WATER)
        					{
        						Incantations.watcher.addBlock(block, 10000L * strength - (y + 1) * 500L);
	                            block.setType(Material.AIR);
        					}
        				}
        				else
        				{
	                        if (type == Material.AIR || type == Material.SNOW || type == Material.STATIONARY_WATER || type == Material.WATER)
	                        {
	                        	Incantations.watcher.addBlock(block, 10000L * strength - (y + 1) * 500L);
	                            block.setType(Material.ICE);
	                    	}
        				}
                    }	
                }
            }
        }
        
    }
    
    private Boolean IsFull(int x, int y, int z, double offset, double radiusSq)
    {
		x -= offset;
		y -= offset;
		z -= offset;
		x *= x;
		y *= y;
		z *= z;
		return x + y + z < radiusSq;
	}
    
}