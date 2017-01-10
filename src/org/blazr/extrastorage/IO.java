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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
 
 
 public class IO
 {
   private static Inventory changeTitle(Inventory inv, ExtraStorage plugin, int invSize) {
	   Inventory newInventory = null;
	   if(plugin.config_loader.getStorageName() != null){
		     newInventory = plugin.getServer().createInventory(null, invSize, plugin.config_loader.getStorageName());
		     int n = 0;
		     while (n < invSize)
		     {
		       newInventory.setItem(n, inv.getItem(n));
		       n++;
		     }
	 }
     return newInventory;
   }
   
   private static void loadBackPack(File loadFile, Inventory inventory, ExtraStorage plugin, int invSize, Player player) throws IOException{
     FileConfiguration backpackConfig = YamlConfiguration.loadConfiguration(loadFile);
     
     List<?> list = backpackConfig.getList("inventory");
     ItemStack[] overageItems = new ItemStack[54];
     ItemStack[] loadedInventory = new ItemStack[54];
     ItemStack[] actualInventory = new ItemStack[invSize];
     if (list != null)
     {
       for (int i = 0; i < list.size(); i++) {
         loadedInventory[i] = ((ItemStack)list.get(i));
       }
       int n = 0;
       int o = 0;
       boolean overageOccurred = false;
       while (n < invSize)
       {
         if (loadedInventory[n] != null) {
           actualInventory[n] = loadedInventory[n];
         }
         n++;
       }
       if (loadedInventory.length > invSize) {
         while (n < loadedInventory.length)
         {
           if (loadedInventory[n] != null)
           {
             overageItems[o] = loadedInventory[n];
             loadedInventory[n] = new ItemStack(Material.AIR);
             if (!overageOccurred) {
               overageOccurred = true;
             }
             o++;
           }
           n++;
         }
       }
	   UUID player_uuid = ExtraStorage.getUUIDMinecraft(player, true);
	   if(player_uuid == null){
		   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + player.getName());
		   return;
	   }
       if (overageOccurred)
       {
         ExtraStorage.dropItems.put(player_uuid, overageItems);
         
         overageItems = null;
         File overageStorage = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "temp_inventories");
         if (!overageStorage.exists()) {
           overageStorage.mkdir();
         }
         File overageSaveFile = new File(overageStorage.getCanonicalPath() + File.separator + player_uuid + ".yml");
         
 
         FileConfiguration overageConfig = YamlConfiguration.loadConfiguration(overageSaveFile);
         
         overageConfig.set("inventory", ExtraStorage.dropItems.get(player_uuid));
         
         overageConfig.save(overageSaveFile);
         String[] notifications = new String[3];
         notifications[0] = (ExtraStorage.PNC + ChatColor.RED + "Your backpack size has been reduced.");
         
         notifications[1] = (ExtraStorage.PNC + ChatColor.RED + "To retrieve the extra items, find a safe place");
         
         notifications[2] = (ExtraStorage.PNC + ChatColor.RED + "and type \"/bp drop\" to drop the extra items.");
         
         player.sendMessage(notifications);
       }
       inventory.setContents(actualInventory);
       inventory = changeTitle(inventory, plugin, invSize);
       ExtraStorage.Inventories.put(player_uuid, inventory);
       if (overageOccurred) {
         saveBackPack(inventory, loadFile);
         overageOccurred = false;
       }
     }
   }
   
   protected static void loadBackpackFromDiskOnLogin(Player player, ExtraStorage plugin) throws IOException {
     if ((player.hasPermission("ExtraStorage.bp.open")) || (player.hasPermission("ExtraStorage.sign.use")))
     {
       Inventory inventory = null;
       String playerName = null;
       UUID player_uuid = ExtraStorage.getUUIDMinecraft(player, true);
       if(player_uuid == null){
		   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + player.getName());
		   return;
	   }
       if (plugin.config_loader.WorldSpecificBagPack()) {
        	playerName = player.getWorld().getName() + "_" + player_uuid;
       } else {
         	playerName = player_uuid + "";
       }
       File defaultDir = new File(plugin.getDataFolder() + File.separator + "data");
       if (!defaultDir.exists()) {
         defaultDir.mkdir();
       }
       File overageStorage = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "temp_inventories");
       if (!overageStorage.exists()) {
         overageStorage.mkdir();
       }
       File overageInventoriesFile = new File(overageStorage.getCanonicalPath() + File.separator + player_uuid + ".yml");
       
 
       String invSaveLocation = plugin.getDataFolder().getCanonicalPath() + File.separator + "data" + File.separator + playerName + ".yml";
       
 
       File invSaveFile = new File(invSaveLocation);
       ExtraStorage.saveFiles.put(player_uuid, invSaveFile);
       
       int storageSize = 0;
       if (player.hasPermission("ExtraStorage.bp.size.54")) {
         storageSize = 54;
       } else if (player.hasPermission("ExtraStorage.bp.size.45")) {
         storageSize = 45;
       } else if (player.hasPermission("ExtraStorage.bp.size.36")) {
         storageSize = 36;
       } else if (player.hasPermission("ExtraStorage.bp.size.27")) {
         storageSize = 27;
       } else if (player.hasPermission("ExtraStorage.bp.size.18")) {
         storageSize = 18;
       } else if (player.hasPermission("ExtraStorage.bp.size.9")) {
         storageSize = 9;
       } else {
         storageSize = 54;
       }
       if (invSaveFile.exists())
       {
         ItemStack[] items = new ItemStack[54];
         int n = 0;
         while (n < 54)
         {
           items[n] = new ItemStack(Material.AIR);
           n++;
         }
         inventory = plugin.getServer().createInventory(null, 54, "Backpack");
         
         inventory.setContents(items);
         ExtraStorage.Inventories.put(player_uuid, inventory);
         
         loadBackPack(ExtraStorage.saveFiles.get(player_uuid), ExtraStorage.Inventories.get(player_uuid), plugin, storageSize, player);
       }
       else if (!invSaveFile.exists())
       {
         ItemStack[] items = new ItemStack[storageSize];
         int n = 0;
         while (n < storageSize)
         {
           items[n] = new ItemStack(Material.AIR);
           n++;
         }
         inventory = plugin.getServer().createInventory(null, storageSize, "Backpack");
         
         inventory.setContents(items);
         ExtraStorage.Inventories.put(player_uuid, inventory);
         
         invSaveFile.createNewFile();
       }
       if (overageInventoriesFile.exists()) {
    	   if(!plugin.config_loader.DisableDropMessage()){
	    	   String[] notifications = new String[3];
	           notifications[0] = (ExtraStorage.PNC + ChatColor.RED + "You have extra items because your backpack can't contain them");
	           notifications[1] = (ExtraStorage.PNC + ChatColor.RED + "To retrieve the extra items, find a safe place");
	           notifications[2] = (ExtraStorage.PNC + ChatColor.RED + "and type \"/bp drop\" to drop the extra items.");
	           player.sendMessage(notifications);
    	   }
       }
     }
   }
   
   protected static void save(ExtraStorage plugin) throws Exception {
	     Iterator<Map.Entry<UUID, Boolean>> it = ExtraStorage.invChanged.entrySet().iterator();
	     while (it.hasNext()) {
	       Map.Entry<UUID, Boolean> pair = it.next();
	       String playerName = pair.getKey() + "";
	       if ((ExtraStorage.saveFiles.containsKey(playerName)) && 
	         (pair.getValue().booleanValue())) {
	    	   saveBackPack(ExtraStorage.Inventories.get(playerName), ExtraStorage.saveFiles.get(playerName));
	       }
	     }
	     if(ExtraStorage.mojangUUID){
		     File uuid_save_file = new File(plugin.getDataFolder().getCanonicalPath() + File.separator + "uuid_database.yml");
		     if(!uuid_save_file.exists()){
				   uuid_save_file.createNewFile();
			 }
		     YamlConfiguration uuid_save = YamlConfiguration.loadConfiguration(uuid_save_file);
		     Iterator<Map.Entry<UUID, UUID>> it_uuid = ExtraStorage.known_uuid.entrySet().iterator();
		     while (it_uuid.hasNext()) {
		    	 Map.Entry<UUID, UUID> pair_uuid = it_uuid.next();
		    	 uuid_save.set(pair_uuid.getKey().toString(), pair_uuid.getValue().toString());
		     }
		     uuid_save.save(uuid_save_file);
	     }
   }
   
   protected static void saveBackPack(Inventory inventory, File saveFile) throws IOException {
	   if(saveFile != null){
	       FileConfiguration backpackConfig = YamlConfiguration.loadConfiguration(saveFile);
	       if(backpackConfig != null){
		 	      backpackConfig.set("inventory", inventory.getContents());
	  		      backpackConfig.save(saveFile);
	       }
	   }
   }
 }
