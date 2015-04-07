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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.google.common.io.Files;

public class Import extends Thread {

	ExtraStorage plugin;
	CommandSender sender;
	List<String> not_converted;
	int filesupdated;
	boolean stop;
	
	Import(ExtraStorage plugin, CommandSender sender){
		this.plugin = plugin;
		this.sender = sender;
		this.not_converted = new LinkedList<String>();
		this.filesupdated = 0;
		this.stop = false;
	}
		
	@SuppressWarnings({ "deprecation", "static-access" })
	@Override
	public void run() {
			File folder = new File(plugin.getDataFolder() + File.separator + "data");
			File[] files = folder.listFiles();
			if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Starting importing names ! Have to check " + files.length + " files!");
			if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Step: 1/2 => Changing names...");
			boolean worldactivated = plugin.getConfig().getBoolean("world-specific-backpacks", false);
			for(int i = 0; i < files.length; i++){
				if(stop){
					break;
				}
				File f = files[i];
				if(!f.isDirectory()){
					double now = System.currentTimeMillis();
					if(!worldactivated){
						if(!isUUID(f.getName().substring(0, f.getName().length() - 4))){
							OfflinePlayer concerned = Bukkit.getServer().getOfflinePlayer(f.getName().substring(0, f.getName().length() - 4));
							if(concerned != null){
								String concerned_uuid = getUUID(concerned);
								if(concerned_uuid != null){
									String pathFile = plugin.getDataFolder() + File.separator + "data" + File.separator + concerned_uuid + ".yml";
									if(new File(pathFile).exists()) new File(pathFile).delete();
									f.renameTo(new File(pathFile));
									filesupdated++;
								} else {
									String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
									if(!new File(folderpath_not_imported).exists()) new File(folderpath_not_imported).mkdir();
									try {
										Files.move(f, new File(folderpath_not_imported + File.separator + f.getName()));
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								printAll10(files.length, i, System.currentTimeMillis() - now);
							}
						}
					} else {
						String parts[] = f.getName().substring(0, f.getName().length() - 4).split("_");
						if(parts.length != 0){
							if(!isUUID(parts[parts.length - 1])){
								String pandw[] = getWorldAndPlayer(parts);
								if(pandw[0] != null && pandw[1] != null){
									OfflinePlayer concerned = Bukkit.getServer().getOfflinePlayer(pandw[1]);
									if(concerned != null){
										String concerned_uuid = getUUID(concerned);
										if(concerned_uuid != null){
											String pathFile = plugin.getDataFolder() + File.separator + "data" + File.separator + pandw[0] + "_" + concerned_uuid + ".yml";
											if(new File(pathFile).exists()) new File(pathFile).delete();
											f.renameTo(new File(pathFile));
											filesupdated++;
										} else {
											if(isNotEmpty(f)){
												String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
												if(!new File(folderpath_not_imported).exists()) new File(folderpath_not_imported).mkdir();
												try {
													Files.move(f, new File(folderpath_not_imported + File.separator + f.getName()));
												} catch (IOException e) {
													e.printStackTrace();
												}
											}
										}
										printAll10(files.length, i, System.currentTimeMillis() - now);
									}
								} else {
									if(isNotEmpty(f)){
										String folderpath_not_imported = plugin.getDataFolder() + File.separator + "data" + File.separator + "not_imported";
										if(!new File(folderpath_not_imported).exists()) new File(folderpath_not_imported).mkdir();
										try {
											Files.move(f, new File(folderpath_not_imported + File.separator + f.getName()));
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
			if(!stop) if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Step: 2/2 => Names updated ! Reloading...");
			plugin.saveFiles = new HashMap<UUID, File>();
			plugin.Inventories = new HashMap<UUID, Inventory>();
			for (Player player : plugin.getServer().getOnlinePlayers()) {
				if (!plugin.getConfig().getList("world-blacklist.worlds").contains(player.getWorld().getName())) {
					try {
						IO.loadBackpackFromDiskOnLogin(player, plugin);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if(!stop){
				if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Reload complete !");
				if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.GREEN + "Import Finished ! " + filesupdated + " were updated !");
				if(not_converted.size() != 0){
					if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + not_converted.size() + " files couldn't been converted:");
					for(String s : not_converted){
						if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.RED + s);
					}
				}
			}
			ExtraStorage.imp = null;
			ExtraStorage.importStarted = false;
	}
	
	private String[] getWorldAndPlayer(String[] s){
		String old = null;
		String retur[] = new String[2];
		for(int i = 0; i < s.length; i++){
			String ss = s[i];
			if(old == null) old = ss;
			else old = old + ss;
			World temp = plugin.getServer().getWorld(old);
			if(temp != null){
				retur[0] = temp.getName();
				break;
			}
		}
		if(retur[0] != null){
			String player_name = null;
			for(int i = 0; i < s.length; i++){
				if(player_name == null) player_name = s[i];
				else player_name = player_name + "_" + s[i];
			}
			retur[1] = player_name.replaceFirst(retur[0] + "_", "");
		}
		return retur;
	}
	
	private boolean isNotEmpty(File f){
		BufferedReader br = null;   
		try {
			br = new BufferedReader(new FileReader(f));
			if (br.readLine() == null) {
				br.close();
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { br.close(); } catch (Exception ignore) {}
		}
		return true;
	}
	
	private boolean isUUID(String s){
			try {
				UUID.fromString(s);
				return true;
			} catch(Exception e){
				return false;
			}
	}
	
	private void printAll10(int size, int actual, double speed){
		if(actual % 10 == 0){
			int percent = (actual * 100) / size;
			if(sender != null) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Status: " + percent + "% (" + actual + "/" + size + ") ! Speed: " + speed + "ms!");
		}
	}

	@SuppressWarnings("deprecation")
	private String getUUID(OfflinePlayer p){
		String retur = ExtraStorage.getUUIDMinecraftS(p, false);
		if(retur != null){
			if(retur.equals("wait")){
				boolean wait = true;
				while(wait){
					if(sender != null && retur.equals("wait")) sender.sendMessage(ExtraStorage.PNC + ChatColor.YELLOW + "Minecraft API servers are overcharged... Waiting for them to be good !");
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					retur = ExtraStorage.getUUIDMinecraftS(p, false);
					if(retur == null){
						wait = false;
					} else if(!retur.equals("wait")){
						wait = false;
					}
				}
			}
		}
		if(retur == null){
			not_converted.add(p.getName());
			return null;
		}
		return retur.toString();
	}
	
	public void stopImport(){
		this.stop = true;
	}
}
