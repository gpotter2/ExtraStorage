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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.blazr.extrastorage.util.Updater;
import org.blazr.extrastorage.util.Updater.UpdateResult;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.Files;

public class CommandsHandler extends Thread {

	private CommandSender sender;
	private Command cmd;
	private String[] args;
	private ExtraStorage plugin;
	
	public CommandsHandler(CommandSender sender, Command cmd, String[] args, ExtraStorage plugin){
		this.sender = sender;
		this.cmd = cmd;
		this.args = args;
		this.plugin = plugin;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void run() {

	     Logger log = plugin.getLogger();
		 UUID sender_uuid = null;
		 if(sender instanceof Player){
			  sender_uuid = ExtraStorage.getUUIDMinecraft((OfflinePlayer) sender, false);
		 }
	     try
	     {
	       String cmdName = cmd.getName().toLowerCase();
	       if (cmdName.trim().equals("bp"))
	       {
	         int numOfArgs = args.length;
	         switch (numOfArgs)
	         {
	         case 0: 
	           if (!(sender instanceof Player))
	           {
	             sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be a player to use the backpack!");
	           }
	           else
	           {
	             Player player = (Player)sender;
	             if (plugin.getConfig().getList("world-blacklist.worlds").contains(player.getWorld().getName())) {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Backpack not allowed in this world.");
	             } else if (sender.hasPermission("ExtraStorage.bp.open")) {
	               if ((!plugin.getConfig().getBoolean("allow-when-not-in-survival-mode")) && (player.getGameMode() != GameMode.SURVIVAL)) {
	                 sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be in survival mode to use the backpack!");
	               } else if (ExtraStorage.Inventories.containsKey(sender_uuid)) {
	                 player.openInventory((Inventory) ExtraStorage.Inventories.get(sender_uuid));
	               } else {
	                 IO.loadBackpackFromDiskOnLogin(player, plugin);
	                 player.openInventory((Inventory)ExtraStorage.Inventories.get(sender_uuid));
	               }
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           }
	           break;
	         case 1: 
	           if (args[0].equalsIgnoreCase("reload"))
	           {
	             if (sender.hasPermission("ExtraStorage.bp.reload"))
	             {
	            	 plugin.onDisable();
	            	 plugin.reloadConfig();
	            	 ExtraStorage.Inventories = new HashMap<UUID, Inventory>();
	               for (Player player1 : plugin.getServer().getOnlinePlayers()) {
	            	   IO.loadBackpackFromDiskOnLogin(player1, plugin);
	               }
	               sender.sendMessage(ExtraStorage.PNC + "Reloaded ExtraStorage successfully.");
	               
	               log.info("Reloaded successfully");
	             }
	             else
	             {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           }
	           else if (args[0].equalsIgnoreCase("check"))
	           {
	             if (!(sender instanceof Player)) {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be a player to check backpacks!");
	             } else if (sender.hasPermission("ExtraStorage.bp.check")) {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Include the player's name to check. \"/bp check <player_name>\"");
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           }
	           else if (args[0].equalsIgnoreCase("update"))
	           {
	             if (sender.hasPermission("ExtraStorage.*")) {
	            	plugin.getLogger().info("Downloading the new version of ExtraStorage !");
		    		Updater up = new Updater(plugin, 56836, plugin.e_file, Updater.UpdateType.DEFAULT, false);
			        if(up.getResult() == UpdateResult.SUCCESS){
			        	sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "Plugin successfuly downloaded ! Reloading the plugin...");
			        	plugin.reload(sender);
			        } else if(up.getResult() == UpdateResult.NO_UPDATE){
			        	sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "The plugin already is up to date !");
			        } else {
			        	sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Plugin couldn't be downloaded !");
			        }
	             } else {
	            	 sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           }
	           else if (args[0].equalsIgnoreCase("drop"))
	           {
	             if (!(sender instanceof Player))
	             {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be a player to use that command!");
	             }
	             else if (sender.hasPermission("ExtraStorage.bp.open"))
	             {
	               File overageStorage = new File(plugin.getDataFolder() + File.separator + "data" + File.separator + "temp_inventories");
	               
	 
	               Player player1 = (Player)sender;
	               if (!overageStorage.exists()) {
	                 overageStorage.mkdir();
	               }
	               File overageSaveFile = new File(overageStorage.getCanonicalPath() + File.separator + sender_uuid + ".yml");
	               
	 
	 
	               FileConfiguration overageConfig = YamlConfiguration.loadConfiguration(overageSaveFile);
	               if (ExtraStorage.dropItems.containsKey(sender_uuid))
	               {
	                 ItemStack[] drops = (ItemStack[])ExtraStorage.dropItems.get(sender_uuid);
	                 for (int n = 0; n < drops.length; n++) {
	                   if (drops[n] != null)
	                   {
	                     Item dropItem = player1.getWorld().dropItemNaturally(player1.getLocation(), drops[n]);
	                     PlayerDropItemEvent itemDrop = new PlayerDropItemEvent(player1, dropItem);
	                     plugin.getServer().getPluginManager().callEvent(itemDrop);
	                   }
	                 }
	                 ExtraStorage.dropItems.remove(sender_uuid);
	                 if (overageSaveFile.exists()) {
	                   overageSaveFile.delete();
	                 }
	               }
	               else if (overageSaveFile.exists())
	               {
	                 List<?> list = overageConfig.getList("inventory");
	                 
	                 ItemStack[] loadedInventory = new ItemStack[54];
	                 if (list != null)
	                 {
	                   for (int i = 0; i < list.size(); i++) {
	                     loadedInventory[i] = ((ItemStack)list.get(i));
	                   }
	                   for (ItemStack item : loadedInventory) {
	                     if (item != null) {
	                       player1.getWorld().dropItemNaturally(player1.getLocation(), item);
	                     }
	                   }
	                 }
	                 overageSaveFile.delete();
	               }
	               else
	               {
	                 sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Nothing to be dropped.");
	               }
	             }
	             else
	             {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You do not have permission for that command");
	             }
	           } else if (args[0].equalsIgnoreCase("version")) {
	             if (sender.hasPermission("ExtraStorage.player.version")) {
	               sender.sendMessage(ExtraStorage.PNC + "ExtraStorage Version: " + plugin.getDescription().getVersion() + " !");
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You do not have permission for that command");
	             }
	           } else if (args[0].equalsIgnoreCase("not_imported")) {
	               if (sender.hasPermission("ExtraStorage.*")) {
	            	   String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
	            	   File folder_not_imported = new File(folderpath_not_imported);
	            	   if(!folder_not_imported.exists()) folder_not_imported.mkdir();
	            	   File[] list_folder_not_imported = folder_not_imported.listFiles();
	            	   if(list_folder_not_imported.length != 0) sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "All not imported bagpacks:");
	            	   else sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "They are no not imported bagpacks !!!");
	            	   for(File f : list_folder_not_imported){
	            		   if(f.isFile()){
	            			   sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + f.getName().substring(0, f.getName().length() - 4));
	            		   }
	            	   }
	               } else {
	            	   sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You do not have permission for that command");
	               }
	           } else if(args[0].equalsIgnoreCase("help")){
	        	   sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + " The command list is available here:");
	        	   sender.sendMessage(ChatColor.BOLD + "" + ChatColor.BLUE + "http://dev.bukkit.org/bukkit-plugins/extra-storage/pages/main/commands-and-permissions/");
	           } else if (args[0].equalsIgnoreCase("my_uuid")){
							  if(!(sender instanceof Player)){
								  sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be a player to use that command!");
								  return;
							  }
	             if (sender.hasPermission("ExtraStorage.player.*")) {
									double now = System.currentTimeMillis();
									double after = System.currentTimeMillis();
									double time = after - now;
	               	sender.sendMessage(ExtraStorage.PNC + "ExtraStorage: Your uuid=" + sender_uuid + "; Time=" + time + "ms");
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You do not have permission for that command");
	             }
	           } else if (args[0].equalsIgnoreCase("import")) {
	             if (sender.hasPermission("ExtraStorage.*")) {
	            	 if(ExtraStorage.importStarted){
	            		 ExtraStorage.imp.stopImport();
	            		 ExtraStorage.imp = null;
	            		 ExtraStorage.importStarted = false;
	            		 sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "Import stopped !");
	            	 } else {
	            		 ExtraStorage.importStarted = true;
	            		 ExtraStorage.imp = new Import(plugin, sender);
	            		 ExtraStorage.imp.start();
	            	 }
				} else {
	               	sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You do not have permission for that command");
	             }
	           }
	           else {
	             sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Unknown command. Type /bp help");
	           }
	           break;
	         case 2: 
	           if (args[0].equalsIgnoreCase("check"))
	           {
	             if (!(sender instanceof Player)) {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You must be a player to check backpacks!");
	             } else if (sender.hasPermission("ExtraStorage.player.check")) {
				   UUID concerned_uuid = ExtraStorage.getUUIDMinecraft(plugin.getServer().getOfflinePlayer(args[1]), false);
				   if(concerned_uuid != null){
					   if (!ExtraStorage.Inventories.containsKey(concerned_uuid) && plugin.getServer().getOfflinePlayer(args[1]).isOnline()) {
						   IO.loadBackpackFromDiskOnLogin((Player) plugin.getServer().getOfflinePlayer(args[1]), plugin);
					   }
		               if (ExtraStorage.Inventories.containsKey(concerned_uuid)) {
		                 Player checkee = plugin.getServer().getPlayer(args[1]);
		                 if (!checkee.hasPermission("ExtraStorage.player.check.exempt")) {
		                   Player player1 = (Player) sender;
		                   player1.openInventory((Inventory) ExtraStorage.Inventories.get(concerned_uuid));
		                 } else {
		                   sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "This player cannot be checked !");
		                 }
		               } else {
		                 sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Could not find a backpack for " + args[1] + ".");
		               }
				   } else {
					   sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Couldn't find unique ID from the player:" + args[1]);
				   }
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           } else {
	             sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Unknown command. Type /bp help");
	           }
	           break;
	         case 3:
	         if(args[0].equalsIgnoreCase("set")) {
	             if (sender.hasPermission("ExtraStorage.*")) {
	            	 String folder_path = plugin.getDataFolder() + File.separator + "data" + File.separator;
	            	 String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
					 if(!new File(folderpath_not_imported).exists()) new File(folderpath_not_imported).mkdir();
	            	 if(new File(folder_path + "not_imported" + File.separator + args[1] + ".yml").exists()){
	            		File old = new File(folder_path + "not_imported" + File.separator + args[1] + ".yml");
	            		UUID new_name_uuid = ExtraStorage.getUUIDMinecraft(plugin.getServer().getOfflinePlayer(args[2]), false);
	            		if(new_name_uuid == null){
	            			sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Couldn't find unique ID from the player:" + args[2]);
	            		} else {
	            			if(plugin.getConfig().getBoolean("world-specific-backpacks", false)){
	            				String parts[] = old.getName().substring(0, old.getName().length() - 4).split("_");
	            				String pandw[] = plugin.getWorldAndPlayer(parts);
	            				if(pandw[0] != null && pandw[1] != null){
		            				File final_file = new File(folder_path + pandw[0] + "_" + new_name_uuid + ".yml");
		            				Files.move(old, final_file);
		            				sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "The bagpack '" + args[1] + "' was set to the player " + args[2] + " !");
	            				} else {
	            					sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "The world does not exist !");
	            				}
	            			} else {
	            				File final_file = new File(folder_path + new_name_uuid + ".yml");
	            				Files.move(old, final_file);
	            				sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "The bagpack '" + args[1] + "' was set to the player " + args[2] + " !");
	            			}
	            		}
	            	 } else {
	                     sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "No bagpacks found with this name:'" + args[1] + "' in the not_imported folder !");
	                 }
	             } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "You don't have permission for that command.");
	             }
	           } else {
	               sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Unknown command. Type /bp help");
	           }
	           break;
	         default: 
	           sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + "Too many arguments for the command.");
	         }
	         return;
	       }
	     } catch (Exception e) {
	       log = plugin.getLogger();
	   	   log.severe("Error in setPlayerStorage()");
	   	   e.printStackTrace();
	     }
	     return;
	}
}
