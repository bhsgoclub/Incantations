package me.bhsgoclub.incantations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Util
{
    public static boolean playerSpendReagent(Player player, Boolean isDye, int material, int count)
    {
    	if (isDye)
    		return playerSpendDye(player, material, count);
    	else
    		return playerSpendItem(player, material, count);
    }
    
    public static boolean playerSpendItem(Player player, int itemId, int amount)
    {
    	PlayerInventory inventory = player.getInventory();
    	
    	Map<Integer, ? extends ItemStack> items = inventory.all(itemId);
    	if (items.isEmpty())
    		return false;
    	
    	int itemCount = 0;
        for (ItemStack item : items.values())
        {
        	itemCount += item.getAmount();
        }
    	
        if (itemCount >= amount)
        {
        	removeFromSlots(inventory, items.keySet().toArray(new Integer[items.size()]), amount);
        	return true;
        }

        return false;
    }
    
    // Gets all slots containing the requested dye in a players inventory
    public static Map<Integer, ? extends ItemStack> getDye(PlayerInventory inventory, int color)
	{
    	Map<Integer, ? extends ItemStack> dyes = inventory.all(Material.INK_SACK);
    	final Iterator<? extends ItemStack> iter = dyes.values().iterator();
    	while (iter.hasNext())
        {
        	if (iter.next().getDurability() != color)
    			iter.remove();
    	}
    	
    	return dyes;
	}
    
    public static void removeFromSlots(PlayerInventory inventory, Integer[] slots, int amount)
    {
    	for (Integer slot : slots)
    	{
    	    ItemStack items = inventory.getItem(slot);
    	    int itemCount = items.getAmount();
    	    
    	    if (itemCount > amount)
            {
            	items.setAmount(itemCount - amount);
                return;
            }
    	    else if (itemCount < amount)
            {
            	amount -= itemCount;
            	inventory.clear(slot);
            }
    	    else if (itemCount == amount)
            {
                inventory.clear(slot);
                return;
            }
    	}
    }
    
    // Since dyes are stored in data rather than a seperate material, we need an alternate function for them
    public static boolean playerSpendDye(Player player, int color, int amount)
    {
    	PlayerInventory inventory = player.getInventory();
    	
    	Map<Integer, ? extends ItemStack> items = getDye(inventory, color);
    	if (items.isEmpty())
    		return false;
    	
        int itemCount = 0;
        for (ItemStack item : items.values())
        {
        	itemCount += item.getAmount();
        }
        
        if (itemCount >= amount)
        {
        	removeFromSlots(inventory, items.keySet().toArray(new Integer[items.size()]), amount);
        	return true;
        }

        return false;
    }
    
    private static String[] dyeColors = new String[]
	{
    	"Ink Sac",
   	 	"Rose Red",
   	 	"Cactus Green",
   	 	"Cocoa Beans",
   	 	"Lapis Lazuli",
   	 	"Purple Dye",
   	 	"Cyan Dye",
   	 	"Light Gray Dye",
   	 	"Gray Dye",
   	 	"Pink Dye",
   	 	"Lime Dye",
   	 	"Dandelion Yellow",
   	 	"Light Blue Dye",
   	 	"Magenta Dye",
   	 	"Orange Dye",
   	 	"Bone Meal"
    };
    public static String getItemName(int item, Boolean isDye)
    {
    	if (!isDye)
    	{
    		String name = Material.getMaterial(item).toString();
    		name = name.substring(0, 1) + name.substring(1).toLowerCase();
    		return name;
    	}
    	else
    		return dyeColors[item];
    }
    
    public static Boolean isPlayerHoldingBook(Player player)
    {
    	return (player.getItemInHand().getType() == Material.BOOK);
    }
    public static Boolean isPlayerHolding(Player player, int item)
    {
    	return (player.getItemInHand().getTypeId() == item);
    }
    
    public static List<LivingEntity> getPlayerTarget(Player player, double range)
    {
    	return getPlayerTarget(player, range, false);
    }
    public static List<LivingEntity> getPlayerTarget(Player player, double range, Boolean playersOnly)
    {
    	// Find target
    	Block targetBlock = player.getTargetBlock(null, 500);
    	Location targetBlockLocation = targetBlock.getLocation();
    	World world = player.getWorld();
    	
    	List<LivingEntity> result = new ArrayList<LivingEntity>();
    	
    	Boolean selfCast = player.getEyeLocation().getDirection().getY() < -0.9d;
    	
    	if (playersOnly)
    	{
	        List<Player> players = world.getPlayers();
	        
	        for (Player target : players)
	        {
	        	if (selfCast || target != player)
            	{
		        	Location targetLocation = target.getLocation();
		            if (targetBlockLocation.distance(targetLocation) <= range)
		            	result.add(target);
            	}
	        }
    	}
    	else
    	{
    		List<Entity> entities = world.getEntities();
            
            for (Entity target : entities)
            {
            	if (selfCast || target != player)
            	{
	                Location targetLocation = target.getLocation();
	                if (targetBlockLocation.distance(targetLocation) <= range && (target instanceof LivingEntity))
	                	result.add((LivingEntity)target);
            	}
            }
    	}
    	
    	return result;
    }
    
    public static void disableStacking(org.bukkit.entity.Item item)
    {
    	// From Nisovin's bookworm plugin
    	try
    	{
            boolean ok = false;
            try
            {
	            // attempt to make books with different data values stack separately
	            Field field1 = net.minecraft.server.Item.class.getDeclaredField("bj");
	            if (field1.getType() == boolean.class)
	            {
                    field1.setAccessible(true);
                    field1.setBoolean(item, true);
                    ok = true;
	            }
            } catch (Exception e) {
            	e.printStackTrace();
            }
            if (!ok) {
                // otherwise limit stack size to 1
                Field field2 = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
                field2.setAccessible(true);
                field2.setInt(item, 1);
            }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }	
    }
    
}