package me.bhsgoclub.incantations;

import me.bhsgoclub.incantations.Incantations;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class IncantationsBlockListener implements Listener
{
	private Incantations plugin;
	
	public IncantationsBlockListener(Incantations instance)
	{
		plugin = instance;
	}
	
	@SuppressWarnings("static-access")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
  	{
		Block block = event.getBlock();
		if (plugin.watcher.removeBlock(block))
			event.setCancelled(true);

  	}
	
}