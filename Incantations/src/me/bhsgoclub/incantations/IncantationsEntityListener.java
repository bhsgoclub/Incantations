package me.bhsgoclub.incantations;

import me.bhsgoclub.incantations.Incantations;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

public class IncantationsEntityListener implements Listener
{
	private Incantations plugin;

    public IncantationsEntityListener(Incantations instance)
    {
        plugin = instance;
    }

    @SuppressWarnings("static-access")
	@EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player)event.getEntity();
            
            if (plugin.watcher.getTicks(player, "Protect") > 0)
                event.setCancelled(true);
            /*
            else if ((LastSpell.trim().split(" ")[0].equalsIgnoreCase("celeritas")) && (event.getCause() == EntityDamageEvent.DamageCause.FALL)) {
              event.setCancelled(true);
            }
            */
        }
    }
}