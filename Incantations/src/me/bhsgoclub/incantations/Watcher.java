package me.bhsgoclub.incantations;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Watcher
  implements Runnable
{	
    private Incantations plugin;
    
    // Timing values
    private long lastTime;// = new Date().getTime();
    private long currentTick = 0L;
    private final long step = 250L;
    private float intermediateStep = 0.0f;
    
    // Holds all magically created blocks that need to be cleaned up after a while
    private class BlockInfo
	{
	    public long time;// = new Date().getTime();
	    public Material original;// = Material.AIR;
	    public byte data;
	}
    private Map<Block, BlockInfo> magicBlocks = new HashMap<Block, BlockInfo>();
    
    private Map<Player, HashMap<String, Long>> cooldownCollection = new HashMap<Player, HashMap<String, Long>>();
    
    private class TickerInfo
    {
    	public long duration;
    	public Location origin;
    	public int frequency;
    }
    private Map<Player, HashMap<String, TickerInfo>> tickerCollection = new HashMap<Player, HashMap<String, TickerInfo>>();
    
    public Watcher(Incantations plugin)
    {
    	this.plugin = plugin;
    	lastTime = new Date().getTime();
    }
    
    // Adds a block to the list of magically created blocks. Call this method BEFORE changing the original block.
    public void addBlock(Block block, Long duration)
    {
    	Material type = block.getType();
    	byte data = block.getData();
    	if (magicBlocks.containsKey(block))
    	{
    		type = magicBlocks.get(block).original;
    		data = magicBlocks.get(block).data;
    		magicBlocks.remove(block);
    	}
		
		BlockInfo blockInfo = new BlockInfo();
		blockInfo.time = new Date().getTime() + duration;
		blockInfo.original = type;
		blockInfo.data = data;
		magicBlocks.put(block, blockInfo);
    }
    
    public Boolean removeBlock(Block block)
    {
    	return removeBlock(block, true);
    }
    private Boolean removeBlock(Block block, Boolean delete)
    {
    	if (magicBlocks.containsKey(block))
        {
    		Material original = magicBlocks.get(block).original;
    		Byte data = magicBlocks.get(block).data;
			// Dirt block is set first to stop flowing water
    		/*
			if (original != Material.STATIONARY_WATER && original != Material.WATER)
				block.setType(Material.DIRT);
				*/
			block.setType(original);
			block.setData(data);
			if (delete) magicBlocks.remove(block);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
			return true;
        }
    	else
    		return false;
    }
    
    public void addPlayer(Player player)
    {
    	if (!cooldownCollection.containsKey(player))
    		cooldownCollection.put(player, new HashMap<String, Long>());
    	if (!tickerCollection.containsKey(player))
    		tickerCollection.put(player, new HashMap<String, TickerInfo>());
    }
    
    public void removePlayer(Player player)
    {
    	if (cooldownCollection.get(player).isEmpty() && tickerCollection.get(player).isEmpty())
    	{
    		cooldownCollection.remove(player);
    		tickerCollection.remove(player);
    	}
    }
    
    public void addCooldown(Player player, String spell, Long duration)
    {
    	Map<String, Long> playerCooldown = cooldownCollection.get(player);
    	playerCooldown.put(spell, duration);
    	
    }
    
    public void addTicker(Player player, String spell, Long duration, int frequency)
    {
    	Map<String, TickerInfo> playerTicks = tickerCollection.get(player);
    	TickerInfo info = new TickerInfo();
    	info.duration = duration;
    	info.origin = player.getLocation();
    	info.frequency = frequency;
    	playerTicks.put(spell, info);
    	
    }
    
    public Long getCooldown(Player player, String spell)
    {
    	Map<String, Long> playerCooldown = cooldownCollection.get(player);
    	Long duration = playerCooldown.get(spell);
    	if (duration != null && duration > 0)
    		return duration;
    	else
    		return 0L;
    }
    
    public Long getTicks(Player player, String spell)
    {
    	Map<String, TickerInfo> playerTicks = tickerCollection.get(player);
    	TickerInfo info = playerTicks.get(spell);
    	if (info == null)
    		return 0L;
    	Long duration = info.duration;
    	if (duration > 0)
    		return duration;
    	else
    		return 0L;
    }
    
    public void cleanUp()
    {
    	Iterator<Block> iter = magicBlocks.keySet().iterator();
    	while (iter.hasNext())
    	{
    		removeBlock(iter.next(), false);
    		iter.remove();
    	}
    }
    
    public void run()
    {
    	// Lots of timing calcultations
        long currentTime = new Date().getTime();
        long currentTimeStep = currentTime - lastTime;
        lastTime = currentTime;
        intermediateStep += (float)currentTimeStep / (float)step;
        long flooredStep = (long)Math.floor(intermediateStep);
        intermediateStep -= flooredStep;
        currentTick += flooredStep;
        
        // Save user config every 5 minutes
        if (currentTick % 10000 == 0)
        	((World) Incantations.users).save();
        
        // Cleanup
        if (currentTick % 2 == 0 && magicBlocks.size() != 0)
        {
            //Enumeration<Block> keys = magicBlocks.keys();
        	final Iterator<Entry<Block, BlockInfo>> iter = magicBlocks.entrySet().iterator();
            while(iter.hasNext())
            {
            	Entry<Block, BlockInfo> entry = iter.next();
            	
            	// Check expiration date
            	if (currentTime >= entry.getValue().time)
            	{
            		removeBlock(entry.getKey(), false);
            		iter.remove();
            	}
            }
        }
        
        // The next code isn't very good, because it uses the cooldownCollection to iterate through all players for both cooldowns and ticks.
        // And naming of entries/iterators is crap. Too much else to do to clean it up though, and it works perfectly.
        final Iterator<Entry<Player, HashMap<String, Long>>> cdIter = cooldownCollection.entrySet().iterator();
        while (cdIter.hasNext())
        {
        	Boolean remove = false;
        	
        	// Cooldowns
        	Entry<Player, HashMap<String, Long>> cdEntry = cdIter.next();
        	Player player = cdEntry.getKey();
        	Map<String, Long> cd = cdEntry.getValue();
        	
        	//for (String spell : cd.keySet())
        	final Iterator<Entry<String, Long>> iter = cd.entrySet().iterator();
        	while (iter.hasNext())
            {
            	Entry<String, Long> entry = iter.next();
            	String spell = entry.getKey();
        		Long duration = entry.getValue();
        		
        		duration -= currentTimeStep;
        		cd.put(spell, duration);
        		if (duration <= 0)
        		{
        			String spellName = plugin.spells.getString("Spells." + spell + ".Name");
        			if (spellName == null)
        				spellName = spell;
        			
        			if (player != null && player.isOnline())
        				player.sendMessage(spellName + " cooldown finished.");
        			else if (cd.isEmpty() && tickerCollection.get(player).isEmpty())
        				remove = true;
        			
        			iter.remove();
        		}
        		
            }
        	
        	// Ticks
        	Map<String, TickerInfo> ticks = tickerCollection.get(player);
        	//for (String spell : ticks.keySet())
        	final Iterator<Entry<String, TickerInfo>> tickIter = ticks.entrySet().iterator();
        	while (tickIter.hasNext())
            {
            	Entry<String, TickerInfo> entry = tickIter.next();
            	String spell = entry.getKey();
            	TickerInfo info = entry.getValue();
        		
        		if (currentTick % info.frequency == 0)
        		{
            		info.duration -= step * info.frequency;
            		
            		Boolean finished = false;
            		if (spell.equals("Heal"))
            			finished = heal(player, info.origin);
            		else if (spell.equals("WaterWalking"))
            			waterWalking(player);
            		else if (spell.equals("Breathe"))
            			breathe(player);
            		
            		if (info.duration <= 0 || finished)
            		{
            			if (player != null && player.isOnline())
            			{
            				String spellName = plugin.spells.getString("Spells." + spell + ".Name");
            				if (spellName != null)
            					player.sendMessage(spellName + " wore off.");
            			}
            			else if (cd.isEmpty() && ticks.isEmpty())
            				remove = true;
            			
            			tickIter.remove();
            		}
            		
        		}
            }
        	
        	if (remove)
        	{
				cdIter.remove();
				tickerCollection.remove(player);
			}
        	
        }
            
        //}
        
    }
    
    private Boolean heal(Player player, Location origin)
    {
    	Location location = player.getLocation();
        if ((location.getBlockX() != origin.getBlockX())
          || (location.getBlockY() != origin.getBlockY())
          || (location.getBlockZ() != origin.getBlockZ())
          || (player.getHealth() >= 20))
            return true;
        //this.health += 1;
        player.setHealth(player.getHealth() + 1);
        player.getWorld().playEffect(location, Effect.SMOKE, 1);
        
        return false;
    }
    
    //private final int[] valuesX = {  0, -1,  0,  1, -2, -1,  0,  1,  2, -1,  0,  1,  0 };
	//private final int[] valuesZ = { -2, -1, -1, -1,  0,  0,  0,  0,  0,  1,  1,  1,  2 };
    private void waterWalking(Player player)
    {
    	Location location = player.getLocation();
    	World world = player.getWorld();
        int playerX = location.getBlockX();
        int playerY = location.getBlockY();
        int playerZ = location.getBlockZ();
        
        // Calculate directional vector for player
        
        
        for (int y = 0; y >= -1; y--)
        {
	        for (int x = -2; x <= 2; x++)
	        {
	        	for (int z = -2; z <= 2; z++)
	        	{
		    		Block block = world.getBlockAt(playerX + x, playerY + y, playerZ + z);
		    		Material type = block.getType();
		    		Byte data = block.getData();
		    		if ((type == Material.STATIONARY_WATER || type == Material.WATER)
						&& (data & 0x7) == 0x0 && (data & 0x8) != 0x8)
		    		{
		    			addBlock(block, 5000L);
		                block.setType(Material.ICE);
		    		}
	        	}
	        }
        }
    
    }
    
    private void breathe(Player player)
    {
    	//TODO: Fix player GUI display of air
        player.setRemainingAir(player.getMaximumAir());
    }
    
}