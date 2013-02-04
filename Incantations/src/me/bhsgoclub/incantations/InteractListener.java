package me.bhsgoclub.incantations;

import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InteractListener implements Listener
{

	Incantations plugin;
	Incantations main = new Incantations();
	IncantationsPlayerListener cast = new IncantationsPlayerListener(main);
	public InteractListener(Incantations instance)
	{
		this.plugin = instance;
	}
	
	public HashMap<String, Long> fulmen = new HashMap<String, Long>();
	
	@EventHandler
	public void onAirFlight(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		Action action = event.getAction();
		if((action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && player.getItemInHand().getAmount() > 0)
		{
			ItemStack inHand = player.getItemInHand();
			ItemMeta meta = inHand.getItemMeta();
			List<String> lore = meta.getLore();
			if(inHand.hasItemMeta())
			{
				/**
				 * Fulmen spell
				 */
				if(inHand.getItemMeta().hasEnchants())
					return;
				if(lore.contains(ChatColor.AQUA + "Fly"))
				{
					if(player.hasPermission("avatarpvp.air.fly"))
					{
						/**
						 * Check if a player has used it before
						 * If not then let them do it and add to hashmap
						 * If yes then check cooldown and let them know
						 * how much time they have left.
						 */
						
						if(fulmen.containsKey(player.getName()))
						{
							long fulCool = 30;
							long diff = (System.currentTimeMillis() - fulmen.get(player.getName())) / 1000;
							if(fulCool < diff)
							{
								cast.CastSpell("fulmen", player);
								fulmen.put(player.getName(), System.currentTimeMillis());
								return;
							}
							else
							{
								player.sendMessage(main.title() + "You must wait " + ChatColor.RED + (fulCool - diff) + ChatColor.WHITE + " seconds before using this again.");
								return;
							}
						}
						else
						{
							cast.CastSpell("fulmen", player);
							fulmen.put(player.getName(), System.currentTimeMillis());
							return;
						}
						
					}
					else
					{
						player.sendMessage(main.noPerm());
						return;
					}
				}
				else
					return;
			}
		}
	}
}
