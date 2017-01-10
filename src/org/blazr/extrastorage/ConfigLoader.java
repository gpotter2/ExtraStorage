package org.blazr.extrastorage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ConfigLoader {
	
	private ExtraStorage plugin;
	private FileConfiguration config;
	
	private List<String> blacklisted_worlds = new LinkedList<String>();
	private List<String> blacklisted_items = new LinkedList<String>();
	private String storage_name = "Bagpack";
	private boolean auto_addpickups_to_storage = false;
	private boolean drop_items_on_death = false;
	private boolean disable_no_item_drop = false;
	private boolean vanish_no_pickup = true;
	private boolean world_specific_bagpack = false;
	private boolean disable_drop_message = false;
	
	public ConfigLoader(FileConfiguration config, ExtraStorage plugin){
		this.config = config;
		this.plugin = plugin;
		this.load();
	}
	
	private class loadItem<T> {
		@SuppressWarnings("rawtypes")
		private Class type;
		private Object default_value = null;
		@SuppressWarnings("rawtypes")
		public loadItem(Class type, Object default_value) {
	        this.type = type;
	        this.default_value = default_value;
	    }
		private boolean isSet(String key){
			if (config.isSet(key)){
				return (config.get(key).getClass() == type);
			} else {
				if(default_value != null){
					config.set(key, default_value);
				}
				save();
			}
			return false;
		}
		@SuppressWarnings("unchecked")
		public T loadSingleItem(String key){
			if (!isSet(key)) return null;
			return (T) config.get(key);
		}
		@SuppressWarnings("unchecked")
		public T loadList(String key){
			if (!isSet(key)) return null;
			return (T) config.getList(key);
		}
	}
	
	public boolean isWorldBlackListed(World world){
		if(blacklisted_worlds == null) return false;
		return blacklisted_worlds.contains(world.getName());
	}
	public boolean isItemBlackListed(ItemStack is){
		if(blacklisted_items == null) return false;
		return blacklisted_items.contains(is.getType().toString());
	}
	public boolean correctStorage(Inventory inv){
		if (storage_name == null) return false;
		return (inv.getTitle().equals(storage_name));
	}
	public String getStorageName(){
		return storage_name;
	}
	public boolean AutoAddPickupsToStorage(){
		return auto_addpickups_to_storage;
	}
	public boolean DropItemsOnDeath(){
		return drop_items_on_death;
	}
	public boolean DisableNoItemDrop(){
		return disable_no_item_drop;
	}
	public boolean VanishNoPacketNoItemPickup(){
		return vanish_no_pickup;
	}
	public boolean WorldSpecificBagPack(){
		return world_specific_bagpack;
	}
	public boolean DisableDropMessage(){
		return disable_drop_message;
	}
	private void load(){
		blacklisted_worlds = new loadItem<List<String>>(ArrayList.class, blacklisted_worlds).loadList("world-blacklist.worlds");
		blacklisted_items = new loadItem<List<String>>(ArrayList.class, blacklisted_items).loadList("blacklisted-items.items");
		storage_name = new loadItem<String>(String.class, storage_name).loadSingleItem("storage-name");
		auto_addpickups_to_storage = new loadItem<Boolean>(Boolean.class, auto_addpickups_to_storage).loadSingleItem("auto-add-pickups-to-storage");
		drop_items_on_death = new loadItem<Boolean>(Boolean.class, drop_items_on_death).loadSingleItem("drop-items-on-player-death");
		disable_no_item_drop = new loadItem<Boolean>(Boolean.class, disable_no_item_drop).loadSingleItem("disable-noitemdrop-perm");
		world_specific_bagpack = new loadItem<Boolean>(Boolean.class, world_specific_bagpack).loadSingleItem("world-specific-backpacks");
		disable_drop_message = new loadItem<Boolean>(Boolean.class, disable_drop_message).loadSingleItem("disable-drop-message");
		vanish_no_pickup = new loadItem<Boolean>(Boolean.class, vanish_no_pickup).loadSingleItem("Compatibility-Settings.Vanish-No-Packet.no-item-pickup-when-vanished");
	}
	private void save(){
		plugin.saveConfig();
	}
}
