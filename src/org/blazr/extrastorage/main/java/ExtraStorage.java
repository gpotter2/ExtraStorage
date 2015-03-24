/*
 *  Copyright (C) 2015 Antony Prince and Gabriel POTTER
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

package org.blazr.extrastorage.main.java;

import java.awt.Event;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.blazr.extrastorage.main.java.Updater.UpdateResult;
import org.blazr.extrastorage.main.java.json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.io.Files;
 
 public class ExtraStorage
   extends JavaPlugin
 {
   protected static Map<UUID, Inventory> Inventories = new HashMap<UUID, Inventory>();
   protected static Map<UUID, File> saveFiles = new HashMap<UUID, File>();
   protected static Map<UUID, Boolean> invChanged = new HashMap<UUID, Boolean>();
   protected static Map<UUID, ItemStack[]> dropItems = new HashMap<UUID, ItemStack[]>();
   protected static ExtraStorage plugin = null;
   protected static int errorLogLevel = 1;
   protected static String PNC = ChatColor.YELLOW + "[ExtraStorage]";
   static  boolean importStarted = false;
   static Import imp = null;
   
   public boolean updatenotice = false;
   public String updatenoticemessage = null;
   
   public boolean addItemToPlayerStorage(String playerName, ItemStack item)
   {
     try
     {
       boolean hasEmpty = false;
       for (ItemStack inventoryItem : (Inventory)Inventories.get(playerName.toLowerCase())) {
         if (inventoryItem == null) {
           hasEmpty = true;
         }
       }
       if (hasEmpty)
       {
         ((Inventory)Inventories.get(playerName.toLowerCase())).addItem(new ItemStack[] { item });
         return true;
       }
       return false;
     }
     catch (Exception e)
     {
       Logger log = getLogger();
       switch (errorLogLevel)
       {
       case 0: 
         break;
       case 1: 
         StackTraceElement[] stackTrace = e.getStackTrace();
         log.severe("Error in addItemToPlayerStorage().");
         log.severe(stackTrace[0].toString());
         log.severe(stackTrace[1].toString());
         break;
       case 2: 
         e.printStackTrace();
         log.severe("Error in addItemToPlayerStorage().");
         break;
       default: 
         e.printStackTrace();
         log.severe("Error in addItemToPlayerStorage().");
       }
     }
     return false;
   }
   
   public Inventory getPlayerStorage(String playerName)
   {
     try
     {
       if (Inventories.containsKey(playerName.toLowerCase())) {
         return (Inventory)Inventories.get(playerName.toLowerCase());
       }
       return null;
     }
     catch (Exception e)
     {
       Logger log = getLogger();
       switch (errorLogLevel)
       {
       case 0: 
         break;
       case 1: 
         StackTraceElement[] stackTrace = e.getStackTrace();
         log.severe("Error in getPlayerStorage().");
         log.severe(stackTrace[0].toString());
         log.severe(stackTrace[1].toString());
         break;
       case 2: 
         e.printStackTrace();
         log.severe("Error in getPlayerStorage().");
         break;
       default: 
         e.printStackTrace();
         log.severe("Error in getPlayerStorage().");
       }
     }
     return null;
   }
   
   public boolean removeItemFromPlayerStorage(String playerName, ItemStack item)
   {
     try
     {
       boolean hasItem = false;
       for (ItemStack inventoryItem : (Inventory)Inventories.get(playerName.toLowerCase())) {
         if (inventoryItem == item) {
           hasItem = true;
         }
       }
       if (hasItem)
       {
         ((Inventory)Inventories.get(playerName.toLowerCase())).remove(item);
         return true;
       }
       return false;
     }
     catch (Exception e)
     {
       Logger log = getLogger();
       switch (errorLogLevel)
       {
       case 0: 
         break;
       case 1: 
         StackTraceElement[] stackTrace = e.getStackTrace();
         log.severe("Error in removeItemFromPlayerStorage().");
         log.severe(stackTrace[0].toString());
         log.severe(stackTrace[1].toString());
         break;
       case 2: 
         e.printStackTrace();
         log.severe("Error in removeItemFromPlayerStorage().");
         break;
       default: 
         e.printStackTrace();
         log.severe("Error in removeItemFromPlayerStorage().");
       }
     }
     return false;
   }
   
   @SuppressWarnings("deprecation")
public void setPlayerStorage(String playerName, Inventory inventory)
   {
     Logger log;
     try
     {
					UUID concerned_uuid = ExtraStorage.getUUIDMinecraft(Bukkit.getOfflinePlayer(playerName));
       Inventories.put(concerned_uuid, inventory);
       invChanged.put(UUID.fromString(playerName), Boolean.valueOf(true));
     }
     catch (Exception e)
     {
       log = getLogger();
   	log.severe("Error in setPlayerStorage()");
					e.printStackTrace();
					}
   }
   
   @SuppressWarnings("deprecation")
public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
   {
     Logger log = getLogger();
				  UUID sender_uuid = null;
				  if(sender instanceof Player){
					  sender_uuid = ExtraStorage.getUUIDMinecraft((OfflinePlayer) sender);
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
             sender.sendMessage(PNC + ChatColor.RED + "You must be a player to use the backpack!");
           }
           else
           {
             Player player = (Player)sender;
             if (getConfig().getList("world-blacklist.worlds").contains(player.getWorld().getName())) {
               sender.sendMessage(PNC + ChatColor.RED + "Backpack not allowed in this world.");
             } else if (sender.hasPermission("ExtraStorage.bp.open"))
             {
               if ((!getConfig().getBoolean("allow-when-not-in-survival-mode")) && (player.getGameMode() != GameMode.SURVIVAL))
               {
                 sender.sendMessage(PNC + ChatColor.RED + "You must be in survival mode to use the backpack!");
               }
               else if (Inventories.containsKey(sender_uuid))
               {
                 player.openInventory((Inventory) Inventories.get(sender_uuid));
               }
               else
               {
                 IO.loadBackpackFromDiskOnLogin(player, this);
                 player.openInventory((Inventory)Inventories.get(sender_uuid));
               }
             }
             else {
               sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           }
           break;
         case 1: 
           if (args[0].equalsIgnoreCase("reload"))
           {
             if (sender.hasPermission("ExtraStorage.bp.reload"))
             {
               onDisable();
               reloadConfig();
               Inventories = new HashMap<UUID, Inventory>();
               for (Player player1 : getServer().getOnlinePlayers()) {
            	   IO.loadBackpackFromDiskOnLogin(player1, this);
               }
               sender.sendMessage(PNC + "Reloaded ExtraStorage successfully.");
               
               log.info("Reloaded successfully");
             }
             else
             {
               sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           }
           else if (args[0].equalsIgnoreCase("check"))
           {
             if (!(sender instanceof Player)) {
               sender.sendMessage(PNC + ChatColor.RED + "You must be a player to check backpacks!");
             } else if (sender.hasPermission("ExtraStorage.bp.check")) {
               sender.sendMessage(PNC + ChatColor.RED + "Include the player's name to check. \"/bp check <player_name>\"");
             } else {
               sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           }
           else if (args[0].equalsIgnoreCase("update"))
           {
             if (sender.hasPermission("ExtraStorage.*")) {
            	this.getLogger().info("Downloading the new version of ExtraStorage !");
	    		Updater up = new Updater(this, 56836, getFile(), Updater.UpdateType.DEFAULT, false);
		        if(up.getResult() == UpdateResult.SUCCESS){
		        	sender.sendMessage(PNC + ChatColor.GREEN + "Plugin successfuly downloaded ! Reloading the plugin...");
		        	this.reload(sender);
		        } else if(up.getResult() == UpdateResult.NO_UPDATE){
		        	sender.sendMessage(PNC + ChatColor.GREEN + "The plugin already is up to date !");
		        } else {
		        	sender.sendMessage(PNC + ChatColor.RED + "Plugin couldn't be downloaded !");
		        }
             } else {
            	 sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           }
           else if (args[0].equalsIgnoreCase("drop"))
           {
             if (!(sender instanceof Player))
             {
               sender.sendMessage(PNC + ChatColor.RED + "You must be a player to use that command!");
             }
             else if (sender.hasPermission("ExtraStorage.bp.open"))
             {
               File overageStorage = new File(getDataFolder() + File.separator + "data" + File.separator + "temp_inventories");
               
 
               Player player1 = (Player)sender;
               if (!overageStorage.exists()) {
                 overageStorage.mkdir();
               }
               File overageSaveFile = new File(overageStorage.getCanonicalPath() + File.separator + sender_uuid + ".yml");
               
 
 
               FileConfiguration overageConfig = YamlConfiguration.loadConfiguration(overageSaveFile);
               if (dropItems.containsKey(sender_uuid))
               {
                 ItemStack[] drops = (ItemStack[])dropItems.get(sender_uuid);
                 for (int n = 0; n < drops.length; n++) {
                   if (drops[n] != null)
                   {
                     Item dropItem = player1.getWorld().dropItemNaturally(player1.getLocation(), drops[n]);
                     PlayerDropItemEvent itemDrop = new PlayerDropItemEvent(player1, dropItem);
                     plugin.getServer().getPluginManager().callEvent(itemDrop);
                   }
                 }
                 dropItems.remove(sender_uuid);
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
                 sender.sendMessage(PNC + ChatColor.RED + "Nothing to be dropped.");
               }
             }
             else
             {
               sender.sendMessage(PNC + ChatColor.RED + "You do not have permission for that command");
             }
           } else if (args[0].equalsIgnoreCase("version")) {
             if (sender.hasPermission("ExtraStorage.player.version")) {
               sender.sendMessage(PNC + "ExtraStorage Version: " + getDescription().getVersion() + " (modified)");
             } else {
               sender.sendMessage(PNC + ChatColor.RED + "You do not have permission for that command");
             }
           } else if (args[0].equalsIgnoreCase("not_imported")) {
               if (sender.hasPermission("ExtraStorage.*")) {
            	   String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
            	   File folder_not_imported = new File(folderpath_not_imported);
            	   if(!folder_not_imported.exists()) folder_not_imported.mkdir();
            	   File[] list_folder_not_imported = folder_not_imported.listFiles();
            	   if(list_folder_not_imported.length != 0) sender.sendMessage(PNC + ChatColor.RED + "All not imported bagpacks:");
            	   else sender.sendMessage(PNC + ChatColor.GREEN + "They are no not imported bagpacks !!!");
            	   for(File f : list_folder_not_imported){
            		   if(f.isFile()){
            			   sender.sendMessage(PNC + ChatColor.RED + f.getName().substring(0, f.getName().length() - 4));
            		   }
            	   }
               } else {
            	   sender.sendMessage(PNC + ChatColor.RED + "You do not have permission for that command");
               }
           } else if (args[0].equalsIgnoreCase("my_uuid")){
						  if(!(sender instanceof Player)){
							  sender.sendMessage(PNC + ChatColor.RED + "You must be a player to use that command!");
							  return true;
						  }
             if (sender.hasPermission("ExtraStorage.player.*")) {
								double now = System.currentTimeMillis();
								UUID player_uuid = getUUIDMinecraft((Player) sender);
								double after = System.currentTimeMillis();
								double time = after - now;
               	sender.sendMessage(PNC + "ExtraStorage: Your uuid=" + player_uuid + "; Time=" + time + "ms");
             } else {
               sender.sendMessage(PNC + ChatColor.RED + "You do not have permission for that command");
             }
           } else if (args[0].equalsIgnoreCase("import")) {
             if (sender.hasPermission("ExtraStorage.*")) {
            	 if(importStarted){
            		 imp.stopImport();
            		 imp = null;
            		 importStarted = false;
            		 sender.sendMessage(PNC + ChatColor.GREEN + "Import stopped !");
            	 } else {
            		importStarted = true;
					imp = new Import(this, sender);
					imp.start();
            	 }
			} else {
               	sender.sendMessage(PNC + ChatColor.RED + "You do not have permission for that command");
             }
           }
           else {
             sender.sendMessage(PNC + ChatColor.RED + "Unknown command.");
           }
           break;
         case 2: 
           if (args[0].equalsIgnoreCase("check"))
           {
             if (!(sender instanceof Player)) {
               sender.sendMessage(PNC + ChatColor.RED + "You must be a player to check backpacks!");
             } else if (sender.hasPermission("ExtraStorage.player.check")) {
			   UUID concerned_uuid = getUUIDMinecraft(getServer().getOfflinePlayer(args[1]));
               if (Inventories.containsKey(concerned_uuid)) {
                 Player checkee = getServer().getPlayer(args[1]);
                 if (!checkee.hasPermission("ExtraStorage.player.check.exempt")) {
                   Player player1 = (Player) sender;
                   player1.openInventory((Inventory) Inventories.get(concerned_uuid));
                 } else {
                   sender.sendMessage(PNC + ChatColor.RED + "You don't have permission to check that player.");
                 }
               } else {
                 sender.sendMessage(PNC + ChatColor.RED + "Could not find a backpack for " + args[1] + ".");
               }
             } else {
               sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           } else {
             sender.sendMessage(PNC + ChatColor.RED + "Unknown command.");
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
            		UUID new_name_uuid = getUUIDMinecraft(getServer().getOfflinePlayer(args[2]));
            		if(new_name_uuid == null){
            			sender.sendMessage(PNC + ChatColor.RED + "The player:" + args[2] + " does not exist on Mojang servers !");
            		} else {
            			if(plugin.getConfig().getBoolean("world-specific-backpacks", false)){
            				String parts[] = old.getName().substring(0, old.getName().length() - 4).split("_");
            				String pandw[] = getWorldAndPlayer(parts);
            				if(pandw[0] != null && pandw[1] != null){
	            				File final_file = new File(folder_path + pandw[0] + "_" + new_name_uuid + ".yml");
	            				Files.move(old, final_file);
	            				sender.sendMessage(PNC + ChatColor.GREEN + "The bagpack '" + args[1] + "' was set to the player " + args[2] + " !");
            				} else {
            					sender.sendMessage(PNC + ChatColor.RED + "The world does not exist !");
            				}
            			} else {
            				File final_file = new File(folder_path + new_name_uuid + ".yml");
            				Files.move(old, final_file);
            				sender.sendMessage(PNC + ChatColor.GREEN + "The bagpack '" + args[1] + "' was set to the player " + args[2] + " !");
            			}
            		}
            	 } else {
                     sender.sendMessage(PNC + ChatColor.RED + "No bagpacks found with this name:'" + args[1] + "' in the not_imported folder !");
                 }
             } else {
               sender.sendMessage(PNC + ChatColor.RED + "You don't have permission for that command.");
             }
           } else {
               sender.sendMessage(PNC + ChatColor.RED + "Unknown command.");
           }
           break;
         default: 
           sender.sendMessage(PNC + ChatColor.RED + "Too many arguments for the command.");
         }
         return true;
       }
     } catch (Exception e) {
       log = getLogger();
   	   log.severe("Error in setPlayerStorage()");
   	   e.printStackTrace();
     }
     return false;
   }
   
   private String[] getWorldAndPlayer(String[] s){
		String old = null;
		String retur[] = new String[2];
		int finished_world = 0;
		for(int i = 0; i < s.length; i++){
			String ss = s[i];
			if(old == null) old = ss;
			else old = old + ss;
			World temp = plugin.getServer().getWorld(old);
			if(temp != null){
				retur[0] = temp.getName();
				finished_world = i;
				break;
			}
		}
		if(retur[0] != null){
			String player_name = null;
			for(int i = finished_world; i < s.length; i++){
				if(player_name == null) player_name = s[i];
				else player_name = player_name + "_" + s[i];
			}
			retur[1] = player_name;
		}
		return retur;
	}
   
   public void onDisable()
   {
     Logger log = getLogger();
     try
     {
       IO.save();
     }
     catch (Exception e)
     {
		log.severe("Error saving inventories during disable! Backpacks may not be saved properly!");
		e.printStackTrace();
	 }
     log.info("Disabled!");
   }
   
   @SuppressWarnings("deprecation")
   public void onEnable() {
     Logger log = getLogger();
     try
     {
       plugin = this;
       PluginManager pm = getServer().getPluginManager();
       EventHandlers eh = new EventHandlers();
       pm.registerEvents(eh, this);
       File defaultDir = getDataFolder().getCanonicalFile();
       if (!defaultDir.exists())
       {
         defaultDir.mkdir();
         File newDataLoc = new File(defaultDir.getCanonicalPath() + File.separator + "data");
         
         newDataLoc.mkdir();
         saveResource("LICENSE.txt", true);
       }
       else
       {
         File newDataLoc = new File(defaultDir.getCanonicalPath() + File.separator + "data");
         if (!newDataLoc.exists())
         {
           newDataLoc.mkdir();
           saveResource("LICENSE.txt", true);
         }
       }
       File oldFile1 = new File(defaultDir.getCanonicalPath() + File.separator + "data" + File.separator + "LastUpdateCheckTime");
       
 
       File oldFile2 = new File(defaultDir.getCanonicalPath() + File.separator + "data" + File.separator + "LatestVersion");
       if (oldFile1.exists()) {
         oldFile1.delete();
       }
       if (oldFile2.exists()) {
         oldFile2.delete();
       }
       for (Player player : getServer().getOnlinePlayers()) {
         if (!getConfig().getList("world-blacklist.worlds").contains(player.getWorld().getName())) {
           IO.loadBackpackFromDiskOnLogin(player, this);
         }
       }
       log.info("Enabled successfully.");
       FileConfiguration conf = getConfig();
       conf.options().copyDefaults(true);
       if (conf.get("Comaptibility-Settings.Vanish-No-Packet.no-item-pickup-when-vanished") != null) {
         conf.set("Comaptibility-Settings", null);
       }
       List<String> blacklist = conf.getStringList("blacklisted-items");
       boolean isOldStyle = false;
       for (String item : blacklist) {
         if (isNumeric(item)) {
           isOldStyle = true;
         }
       }
       if (isOldStyle)
       {
         List<String> newList = new ArrayList<String>();
         for (String item : blacklist) {
           if (isNumeric(item))
           {
             int itemCode = Integer.parseInt(item);
             
             ItemStack tempIS = new ItemStack(itemCode);
             newList.add(tempIS.getType().toString());
           }
           else
           {
             newList.add(item);
           }
         }
         conf.set("blacklisted-items", newList);
       }
       if(!conf.isSet("update-check")) conf.set("update-check", true);
       boolean update_check = conf.getBoolean("update-check");
       saveConfig();
       
       try {
     	  Metrics metrics = new Metrics(this);
           metrics.start();
       } catch (IOException e) {
           // Failed to submit the stats :-(
       }
       
       if (update_check) {
	        Updater up = new Updater(this, 56836, getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
	        if(up.getResult() == UpdateResult.UPDATE_AVAILABLE){
	        	getLogger().info("A new version of the plugin is available !");
	        	updatenotice = true;
	        	updatenoticemessage = up.getLatestName().toLowerCase().replace("extrastorage", "");
	        }
     }
       
     } catch (Exception e) {
		e.printStackTrace();
		log.severe("Error in onEnable! Plugin not enabled properly!");
	 }
       
   }
   
   private static boolean isNumeric(String str)
   {
     for (char c : str.toCharArray()) {
       if (Character.isDigit(c)) {
         return true;
       }
     }
     return false;
   }
   
   private static boolean isUUID(String s){
		try {
			UUID.fromString(s);
			return true;
		} catch(Exception e){
			return false;
		}
   }
   
   public static UUID getUUIDMinecraft(OfflinePlayer p){
	   String uuid = getUUIDMinecraftS(p);
	   if(isUUID(uuid)){
		   return UUID.fromString(uuid);
	   }
	   return null;
   }
   
   private static String getText(String myURL) {
		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				urlConn.setReadTimeout(5 * 1000);
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.defaultCharset());
				BufferedReader bufferedReader = new BufferedReader(in);
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}
			}
		in.close();
		} catch (IOException e) {
			if(e.getMessage().contains("429")) return "wait";
			throw new RuntimeException("Exception while calling URL:"+ myURL, e);
		} 
		return sb.toString();
	}
   
   
	public static String getUUIDMinecraftS(OfflinePlayer p){//GETTING JSON UUID FROM MINECRAFT SERVERS
		String basic = "https://api.mojang.com/users/profiles/minecraft/";
		String get = getText(basic + p.getName());
		if(get == null || get.equals("")){//IF USERNAME DIDN'T EXIST
			return null;
		} else if(get.equals("wait")){
			return "wait";
		}
		JSONObject array = new JSONObject(get);
		if(array.has("id")){//ADDING THE - TO MAKE IT A REAL UUID
			return UUID.fromString(array.getString("id").replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")).toString();
		} else {
			return null;
		}
	}
	
    @SuppressWarnings("unchecked")
	public void reload(CommandSender s) {
    	
    	final Plugin plugin = this;
    	final CommandSender sender = s;
    	
    	new BukkitRunnable() {
			@Override
			public void run() {
		        PluginManager pluginManager = Bukkit.getPluginManager();
		        SimpleCommandMap commandMap = null;
		        List<Plugin> plugins = null;
		        Map<String, Plugin> names = null;
		        Map<String, Command> commands = null;
		        Map<Event, SortedSet<RegisteredListener>> listeners = null;
		        boolean reloadlisteners = true;
		        if (pluginManager != null) {
		            try {
		                Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
		                pluginsField.setAccessible(true);
		                plugins = (List<Plugin>) pluginsField.get(pluginManager);
		                Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
		                lookupNamesField.setAccessible(true);
		                names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);
		                try {
		                    Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
		                    listenersField.setAccessible(true);
		                    listeners = (Map<Event, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
		                } catch (Exception e) {
		                    reloadlisteners = false;
		                }
		                Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
		                commandMapField.setAccessible(true);
		                commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);
		                Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
		                knownCommandsField.setAccessible(true);
		                commands = (Map<String, Command>) knownCommandsField.get(commandMap);
		            } catch (NoSuchFieldException e) {
		                e.printStackTrace();
		                return;
		            } catch (IllegalAccessException e) {
		                e.printStackTrace();
		                return;
		            }
		        }
		        pluginManager.disablePlugin(plugin);
		        if (plugins != null && plugins.contains(plugin))
		            plugins.remove(plugin);
		        if (names != null && names.containsKey(plugin))
		            names.remove(plugin);
		        if (listeners != null && reloadlisteners) {
		            for (SortedSet<RegisteredListener> set : listeners.values()) {
		                for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext(); ) {
		                    RegisteredListener value = it.next();
		                    if (value.getPlugin() == plugin) {
		                        it.remove();
		                    }
		                }
		            }
		        }
		        if (commandMap != null) {
		            for (Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator(); it.hasNext(); ) {
		                Map.Entry<String, Command> entry = it.next();
		                if (entry.getValue() instanceof PluginCommand) {
		                    PluginCommand c = (PluginCommand) entry.getValue();
		                    if (c.getPlugin() == plugin) {
		                        c.unregister(commandMap);
		                        it.remove();
		                    }
		                }
		            }
		        }
		        ClassLoader cl = plugin.getClass().getClassLoader();
		        if (cl instanceof URLClassLoader) {
		            try {
		                ((URLClassLoader) cl).close();
		            } catch (IOException ex) {
		                Logger.getLogger(ExtraStorage.class.getName()).log(Level.SEVERE, null, ex);
		            }
		        }
		        System.gc();
		        Plugin target = null;
		        try {
		            target = Bukkit.getPluginManager().loadPlugin(getFile());
		        } catch (InvalidDescriptionException e) {
		            e.printStackTrace();
		            return;
		        } catch (InvalidPluginException e) {
		            e.printStackTrace();
		            return;
		        }
		        target.onLoad();
		        Bukkit.getPluginManager().enablePlugin(target);
		        sender.sendMessage(ChatColor.YELLOW + "[ExtraStorage]" + ChatColor.GREEN + " Plugin successfuly reloaded !");
			}
		}.runTaskLater(this, 10);
}
}
