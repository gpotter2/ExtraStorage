/*
 *  Copyright (C) 2015 Gabriel POTTER
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */ 

package org.blazr.extrastorage;
 
 import java.io.File;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
 
 
 public class EventHandlers
   implements Listener
 {
   private ExtraStorage plugin;
   
   public EventHandlers(ExtraStorage plugin){
	   this.plugin = plugin;
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void onInventoryClick(InventoryClickEvent event)
   {
     Logger log;
     try
     {
       if (((event.getWhoClicked().hasPermission("ExtraStorage.bp.open")) || (event.getWhoClicked().hasPermission("ExtraStorage.sign.use"))) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getWhoClicked().getWorld().getName()))) {
         if ((event.getInventory().getTitle().contentEquals(plugin.getConfig().getString("storage-name"))) && (!event.isCancelled())) {
           ExtraStorage.invChanged.put(ExtraStorage.getUUIDMinecraft((OfflinePlayer) event.getWhoClicked(), true), Boolean.valueOf(true));
         }
       }
     }
     catch (Exception ex)
     {
       log = plugin.getLogger();
   	 ex.printStackTrace();
    	 log.severe("Error in onInventoryClick method caused by " + event.getWhoClicked().getName());
}
}
   
   @SuppressWarnings("deprecation")
@EventHandler(priority=EventPriority.NORMAL)
   private void onInventoryClose(InventoryCloseEvent event)
   {
     Logger log;
	 UUID player_uuid = ExtraStorage.getUUIDMinecraft((OfflinePlayer) event.getPlayer(), true);
     try
     {
       if (((event.getPlayer().hasPermission("ExtraStorage.bp.open")) || (event.getPlayer().hasPermission("ExtraStorage.sign.use"))) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName())) && (event.getInventory().getTitle().equals(plugin.getConfig().getString("storage-name"))))
       {
         ItemStack[] invItems = ((Inventory)ExtraStorage.Inventories.get(player_uuid)).getContents();
         
 
         ItemStack[] drops = new ItemStack[54];
         int n = 0;
         int itemIndex = 0;
         ItemStack emptyItemStack = new ItemStack(Material.AIR, 0);
         boolean messaged = false;
         for (ItemStack items : invItems)
         {
           if (items != null)
           {
             if (plugin.getConfig().getList("blacklisted-items.items").contains(items.getType().toString()))
             {
               drops[n] = items;
               invItems[itemIndex] = emptyItemStack;
               if (!messaged)
               {
                 plugin.getServer().getPlayer(event.getPlayer().getName()).sendMessage("You can't put that item in your backpack.");
                 messaged = true;
               }
             }
             n++;
           }
           itemIndex++;
         }
         ((Inventory)ExtraStorage.Inventories.get(player_uuid)).setContents(invItems);
         for (n = 0; n < drops.length; n++) {
           if (drops[n] != null)
           {
             Item dropItem = event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), drops[n]);
             PlayerDropItemEvent itemDrop = new PlayerDropItemEvent((Player)event.getPlayer(), dropItem);
             plugin.getServer().getPluginManager().callEvent(itemDrop);
           }
         }
       }
       if (((event.getPlayer().hasPermission("ExtraStorage.bp.open")) || (event.getPlayer().hasPermission("ExtraStorage.sign.use"))) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName()))) {
         if (ExtraStorage.invChanged.containsKey(player_uuid)) {
           if (((Boolean)ExtraStorage.invChanged.get(player_uuid)).booleanValue())
           {
             IO.saveBackPack((Inventory) ExtraStorage.Inventories.get(player_uuid), (File)ExtraStorage.saveFiles.get(player_uuid));
             ExtraStorage.invChanged.remove(player_uuid);
           }
         }
       }
     }
     catch (Exception e)
     {
       log = plugin.getLogger();
       log.severe("Error in onInventoryClose()");
       e.printStackTrace();
	 }
   }
   
   @EventHandler
   public void join(PlayerJoinEvent event){
 	  if(plugin != null){
		   if(plugin.updatenotice){
	 		  if(event.getPlayer().hasPermission("ExtraStorage.*")){
	 			  event.getPlayer().sendMessage(ChatColor.GREEN + "[ExtraStorage] A new great update of the plugin is available !");
	 			  event.getPlayer().sendMessage(ChatColor.GREEN + "[ExtraStorage] Your version: " + ChatColor.RED + "v" + plugin.getDescription().getVersion() + ChatColor.GREEN + ". New version: " + ChatColor.GOLD + plugin.updatenoticemessage + ChatColor.GREEN + " !");
	 			  event.getPlayer().sendMessage(ChatColor.GREEN + "[ExtraStorage] To download it, just type: '/bp update' !");
	 		  }
	 	  }
 	  }
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void onItemPickup(PlayerPickupItemEvent event)
   {
     Logger log;
     try
     {
       if ((plugin.getConfig().getBoolean("auto-add-pickups-to-storage")) && (event.getPlayer().hasPermission("ExtraStorage.bp.open")) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName()))) {
		 UUID player_uuid = ExtraStorage.getUUIDMinecraft((OfflinePlayer) event.getPlayer(), true);
         if (!plugin.getConfig().getList("blacklisted-items.items").contains(event.getItem().getItemStack().getType().toString()))
         {
           if (event.isCancelled()) {
             return;
           }
           int emptyPlayerSlots = 0;
           for (ItemStack item : event.getPlayer().getInventory()) {
             if (item == null) {
               emptyPlayerSlots++;
             }
           }
           int emptyBackPackSlots = 0;
           if(ExtraStorage.Inventories.containsKey(player_uuid)){
	           for (ItemStack item : (Inventory) ExtraStorage.Inventories.get(player_uuid)) {
	             if (item == null) {
	               emptyBackPackSlots++;
	             }
	           }
	           if (emptyPlayerSlots <= 1)
	           {
	             if (emptyBackPackSlots > 0)
	             {
	               if ((plugin.getServer().getPluginManager().getPlugin("VanishNoPacket") != null) && (plugin.getConfig().getBoolean("Compatibility-Settings.Vanish-No-Packet.no-item-pickup-when-vanished")))
	               {
	                 VNPCompat.vanishPlayerPickupItemEvent(event, plugin);
	               }
	               else
	               {
	                 ((Inventory) ExtraStorage.Inventories.get(player_uuid)).addItem(new ItemStack[] { event.getItem().getItemStack() });
	 
	                 ExtraStorage.invChanged.put(player_uuid, Boolean.valueOf(true));
	                 
	                 event.getItem().remove();
	                 event.setCancelled(true);
	                 event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_PICKUP, 100.0F, 100.0F);
	               }
	               return;
	             }
	             event.getPlayer().sendMessage("Backpack and inventory are full.");
	             
	             return;
	           }
           }
         }
         else
         {
           int emptyPlayerSlots = 0;
           for (ItemStack item : event.getPlayer().getInventory()) {
             if (item == null) {
               emptyPlayerSlots++;
             }
           }
           int emptyBackPackSlots = 0;
           for (ItemStack item : (Inventory)ExtraStorage.Inventories.get(player_uuid)) {
             if (item == null) {
               emptyBackPackSlots++;
             }
           }
           if ((emptyPlayerSlots == 1) && (emptyBackPackSlots > 0) && (plugin.getConfig().getList("blacklisted-items.items").contains(event.getItem().getItemStack().getType().toString()))) {
             event.setCancelled(true);
           }
         }
       }
     }
     catch (Exception ex)
     {
       log = plugin.getLogger();
    	log.severe("Error in onItemPickup method caused by " + event.getPlayer().getName());
					ex.printStackTrace();
	 }	
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void onPlayerDeath(PlayerDeathEvent event)
   {
     Logger log;
     try
     {
       if ((plugin.getConfig().getBoolean("drop-items-on-player-death")) && 
         (event.getEntity().getPlayer().hasPermission("ExtraStorage.bp.open")) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getEntity().getPlayer().getWorld().getName())) && (!event.getEntity().getPlayer().hasPermission("ExtraStorage.player.noitemdrop")))
       {
    	 
         UUID temp_uuid = ExtraStorage.getUUIDMinecraft(event.getEntity().getPlayer(), true);
         if(temp_uuid == null){
			   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + event.getEntity().getPlayer().getName());
			   return;
		 }
         String playerName = temp_uuid.toString() + "";
         ItemStack[] drops = ((Inventory)ExtraStorage.Inventories.get(playerName)).getContents();
         
         ((Inventory)ExtraStorage.Inventories.get(playerName)).clear();
         for (int n = 0; n < drops.length; n++) {
           if (drops[n] != null)
           {
             Item dropItem = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), drops[n]);
             PlayerDropItemEvent itemDrop = new PlayerDropItemEvent(event.getEntity(), dropItem);
             
             plugin.getServer().getPluginManager().callEvent(itemDrop);
           }
         }
         IO.saveBackPack((Inventory)ExtraStorage.Inventories.get(playerName), (File)ExtraStorage.saveFiles.get(playerName));
       }
     }
     catch (Exception ex) {
     log = plugin.getLogger();
     ex.printStackTrace();
     log.severe("Error in onPlayerDeath method caused by " + event.getEntity().getPlayer().getName());
				  }
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void onPlayerJoin(PlayerJoinEvent event)
   {
     Logger log;
     try
     {
       if (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName())) {
         IO.loadBackpackFromDiskOnLogin(event.getPlayer(), plugin);
       }
     }
     catch (Exception e)
     {
       log = plugin.getLogger();
       e.printStackTrace();
       log.severe("Error loading backpack inventory for " + event.getPlayer().getName());
				  }
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void onPlayerQuit(PlayerQuitEvent event)
   {
     Logger log;
     try
     {
       if (((event.getPlayer().hasPermission("ExtraStorage.bp.open")) || (event.getPlayer().hasPermission("ExtraStorage.sign.use"))) && (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName())))
       {
         UUID playerName = ExtraStorage.getUUIDMinecraft(event.getPlayer(), true);
         if(playerName == null){
			   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + event.getPlayer().getName());
			   return;
		   }
         if(ExtraStorage.saveFiles.get(playerName) != null && ExtraStorage.Inventories.get(playerName) != null){
        	 IO.saveBackPack((Inventory) ExtraStorage.Inventories.get(playerName), ExtraStorage.saveFiles.get(playerName));
         }
         ExtraStorage.Inventories.remove(playerName);
       }
     }
     catch (Exception ex)
     {
       log = plugin.getLogger();
       ex.printStackTrace();
       log.severe("Error in onPlayerQuit method caused by " + event.getPlayer().getName());
				  }
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void playerChangedWorld(PlayerChangedWorldEvent event)
   {
     Logger log;
     try
     {
       if (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName())) {
         IO.loadBackpackFromDiskOnLogin(event.getPlayer(), plugin);
       }
     }
     catch (Exception e)
     {
       log = plugin.getLogger();
       e.printStackTrace();
       log.severe("Error loading backpack inventory for " + event.getPlayer().getName() + " after world switch.");
					}
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void signChanged(SignChangeEvent event)
   {
     Logger log;
     try
     {
       if (event.getPlayer().hasPermission("ExtraStorage.sign.place"))
       {
         if (event.getLine(0).toLowerCase().contains("extrastorage")) {
           if (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName()))
           {
             event.setLine(0, ChatColor.DARK_RED + "ExtraStorage");
           }
           else
           {
             event.getPlayer().sendMessage("ExtraStorage not allowed in plugin world.");
             
             event.setCancelled(true);
           }
         }
       }
       else if (event.getLine(0).toLowerCase().contains("extrastorage"))
       {
         event.getPlayer().sendMessage("You don't have permission to place that sign.");
         
         event.setCancelled(true);
       }
     }
     catch (Exception e)
     {
       	log = plugin.getLogger();
    		 e.printStackTrace();
    	 	log.severe("Error in signChanged method caused by " + event.getPlayer().getName());
					}
   }
   
   @EventHandler(priority=EventPriority.NORMAL)
   private void signClicked(PlayerInteractEvent event)
   {
     Logger log;
     try
     {
       if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (
         (event.getClickedBlock().getType() == Material.SIGN) || (event.getClickedBlock().getType() == Material.SIGN_POST) || (event.getClickedBlock().getType() == Material.WALL_SIGN)))
       {
         Sign sign = (Sign)event.getClickedBlock().getState();
         if (sign.getLine(0).contains("ExtraStorage")) {
           if (event.getPlayer().hasPermission("ExtraStorage.sign.use"))
           {
             if (!plugin.getConfig().getList("world-blacklist.worlds").contains(event.getPlayer().getWorld().getName()))
             {
			   UUID player_uuid = ExtraStorage.getUUIDMinecraft(event.getPlayer(), true);
			   if(player_uuid == null){
				   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + event.getPlayer().getName());
				   return;
			   }
               if (ExtraStorage.Inventories.containsKey(player_uuid))
               {
                 event.getPlayer().openInventory((Inventory)ExtraStorage.Inventories.get(player_uuid));
               }
               else
               {
                 IO.loadBackpackFromDiskOnLogin(event.getPlayer(), plugin);
                 
                 event.getPlayer().openInventory((Inventory)ExtraStorage.Inventories.get(player_uuid));
               }
             }
             else {
               event.getPlayer().sendMessage("Can't use that sign here.");
             }
           }
           else {
             event.getPlayer().sendMessage("You don't have permission to use that sign.");
           }
         }
       }
     }
     catch (Exception e)
     {
       log = plugin.getLogger();
    	 e.printStackTrace();
     	log.severe("Error in signClicked method caused by " + event.getPlayer().getName());
					}
   }
 }