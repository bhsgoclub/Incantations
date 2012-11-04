package me.bhsgoclub.incantations;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class IncantationsBlockListener implements Listener
{
	private Incantations plugin;
	
	public IncantationsBlockListener(Incantations instance)
	{
		plugin = instance;
	}
	
	public void onBlockBreak(BlockBreakEvent event)
  	{
		Block block = event.getBlock();
		if (plugin.watcher.removeBlock(block))
			event.setCancelled(true);

  	}
	
}