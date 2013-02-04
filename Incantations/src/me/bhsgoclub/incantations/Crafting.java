package me.bhsgoclub.incantations;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class Crafting {

	Incantations plugin;
	
	public void loadRecipes()
	{
		/**
		 * Fulmen recipe.
		 * 1 stick, 64 blaze rods.
		 */
		ItemStack fulmen = new ItemStack(Material.STICK, 1);
		ItemMeta fulMeta = fulmen.getItemMeta();
		ArrayList<String> fulLore = new ArrayList<String>();
		fulLore.add(ChatColor.RED + "Frag Grenade");
		fulMeta.setLore(fulLore);
		fulmen.setItemMeta(fulMeta);
		ShapelessRecipe fulRecipe = new ShapelessRecipe(fulmen).addIngredient(1, Material.STICK).addIngredient(64, Material.BLAZE_ROD);
		plugin.getServer().addRecipe(fulRecipe);
		
		/**
		 * Tarduscado recipe.
		 * 1 stick, 64 cob webs.
		 */
		ItemStack tard = new ItemStack(Material.STICK, 1);
		ItemMeta tardMeta = tard.getItemMeta();
		ArrayList<String> tardLore = new ArrayList<String>();
		tardLore.add(ChatColor.RED + "Frag Grenade");
		tardMeta.setLore(fulLore);
		tard.setItemMeta(fulMeta);
		ShapelessRecipe tardRecipe = new ShapelessRecipe(tard).addIngredient(1, Material.STICK).addIngredient(64, Material.WEB);
		plugin.getServer().addRecipe(tardRecipe);
		
		plugin.getLogger().log(Level.INFO, "[Incantations] Recipes loaded.");
		return;
	}
}
