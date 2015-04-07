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
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
 
 public class ExtraStorage
   extends JavaPlugin
 {
   protected static Map<UUID, Inventory> Inventories = new HashMap<UUID, Inventory>();
   protected static Map<UUID, File> saveFiles = new HashMap<UUID, File>();
   protected static Map<UUID, Boolean> invChanged = new HashMap<UUID, Boolean>();
   protected static Map<UUID, ItemStack[]> dropItems = new HashMap<UUID, ItemStack[]>();
   protected static Map<UUID, UUID> known_uuid = new HashMap<UUID, UUID>();
   protected static ExtraStorage plugin = null;
   protected static int errorLogLevel = 1;
   protected static String PNC;
   static boolean importStarted = false;
   static Import imp = null;
   
   static boolean mojangUUID;
   
   public File e_file;
   
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
   
   public static void setPlayerStorage(Player player, Inventory inventory){
	   try {
	    	UUID concerned_uuid = ExtraStorage.getUUIDMinecraft(player, true);
	    	if(concerned_uuid == null){
	 		   plugin.getLogger().info(ChatColor.RED + "Couldn't find unique ID from the player:" + player.getName());
	 		   return;
	 	   	}
	       	Inventories.put(concerned_uuid, inventory);
	       	invChanged.put(concerned_uuid, Boolean.valueOf(true));
	     } catch (Exception e) {
	   	  	 plugin.getLogger().severe("Error in setPlayerStorage()");
	   	  	 e.printStackTrace();
		 }
   }
   
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(cmd.getName().toLowerCase().trim().equals("bp")){
		   new CommandsHandler(sender, cmd, args, this).start();
		   return true;
		}
		return false;
	}
	
	public static void loadBackpackFromDiskOnLogin(Player player) throws IOException {
		IO.loadBackpackFromDiskOnLogin(player, plugin);
	}
   
   String[] getWorldAndPlayer(String[] s){
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
     try {
       IO.save(this);
     } catch (Exception e) {
		log.severe("Error saving inventories during disable! Backpacks may not be saved properly!");
		e.printStackTrace();
	 }
     log.info("Disabled!");
   }
   
   public void loadUUID(ExtraStorage plugin) throws IOException {
	   if(mojangUUID){
		   File uuid_save_file = new File(plugin.getDataFolder().getCanonicalPath() + File.separator + "uuid_database.yml");
		   if(!uuid_save_file.exists()){
			   uuid_save_file.createNewFile();
			   return;
		   }
		   YamlConfiguration uuid_save = YamlConfiguration.loadConfiguration(uuid_save_file);
		   for(String key : uuid_save.getKeys(false)){
			   if(isUUID(key)){
				   UUID bukkit_uuid = UUID.fromString(key);
				   String value = uuid_save.getString(key);
				   if(isUUID(value)){
					   UUID mojang_uuid = UUID.fromString(value);
					   known_uuid.put(bukkit_uuid, mojang_uuid);
				   }
			   }
		   }
	   }
   }
   
   @SuppressWarnings("deprecation")
   public void onEnable() {
     Logger log = getLogger();
     try
     {
       plugin = this;
       e_file = getFile();
       PluginManager pm = getServer().getPluginManager();
       EventHandlers eh = new EventHandlers();
       pm.registerEvents(eh, this);
       File defaultDir = getDataFolder().getCanonicalFile();
       if (!defaultDir.exists()) {
         defaultDir.mkdir();
         File newDataLoc = new File(defaultDir.getCanonicalPath() + File.separator + "data");
         newDataLoc.mkdir();
         saveResource("LICENSE.txt", true);
       }
       else
       {
         File newDataLoc = new File(defaultDir.getCanonicalPath() + File.separator + "data");
         if (!newDataLoc.exists()) {
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
       if (!conf.isSet("display-prefix")) {
    	   conf.set("display-prefix", true);
       }
       if(conf.getBoolean("display-prefix")){
    	   PNC = ChatColor.YELLOW + "[ExtraStorage]";
       } else {
    	   PNC = "";
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
           if (isNumeric(item)) {
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
       if(!conf.isSet("use-Minecraft-UUID")) conf.set("use-Minecraft-UUID", true);
       boolean update_check = conf.getBoolean("update-check");
       mojangUUID = conf.getBoolean("use-Minecraft-UUID");
       loadUUID(this);
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
   
   private static boolean isNumeric(String str){
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
   
   public static UUID getUUIDMinecraft(OfflinePlayer p, boolean main_thread){
	   if(mojangUUID){
		   if(known_uuid.containsKey(p.getUniqueId())){
			   return known_uuid.get(p.getUniqueId());
		   } else {
			   String uuid = getUUIDMinecraftS(p, main_thread);
			   if(isUUID(uuid)){
				   UUID c_uuid = UUID.fromString(uuid);
				   known_uuid.put(p.getUniqueId(), c_uuid);
				   return c_uuid;
			   }
		   }
	   } else {
		   return p.getUniqueId();
	   }
	   return null;
   }
   
   private static String getText(String myURL, boolean main_thread) {
	   StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;
		try {
			URL url = new URL(myURL);
			urlConn = url.openConnection();
			if (urlConn != null)
				if(main_thread) urlConn.setReadTimeout(5 * 1000);
				else urlConn.setReadTimeout(30 * 1000);
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
			return null;
		} catch(Exception e){
		   return null;
		}
		return sb.toString();
	}
   /**
    @deprecated
	*/
	public static String getUUIDMinecraftS(OfflinePlayer p, boolean main_thread){
		if(mojangUUID){
			String basic = "https://api.mojang.com/users/profiles/minecraft/";
			String get = getText(basic + p.getName(), main_thread);
			if(get == null){
				return null;
			} else if(get.equals("wait")){
				return "wait";
			}
			JSONObject array = new JSONObject(get);
			if(array.has("id")){
				return UUID.fromString(array.getString("id").replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")).toString();
			} else {
				return null;
			}
		} else {
			return p.getUniqueId().toString();
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
