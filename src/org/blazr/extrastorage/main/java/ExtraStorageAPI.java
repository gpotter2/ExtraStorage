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

import java.io.IOException;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ExtraStorageAPI {
	/**
	 * 
	 * Return the ExtraStorage's Inventory of the player
	 * 
	 * @param player 
	 * @return The ExtraStorage inventory of the player or null if it doesn't exist
	 * @throws IOException If the plugin couldn't load the bagpack if not loaded
	 * @throws NullPointerException If the player is null
	 */
	public static Inventory getInventory(Player player) throws IOException {
		if(player == null){
			new NullPointerException("The player cannot be null !").printStackTrace();
			return null;
		}
		UUID uuid = ExtraStorage.getUUIDMinecraft(player, false);
		Inventory retur = null;
		if (ExtraStorage.Inventories.containsKey(uuid)) {
            retur = (Inventory) ExtraStorage.Inventories.get(uuid);
        } else {
            ExtraStorage.loadBackpackFromDiskOnLogin(player);
            retur = (Inventory)ExtraStorage.Inventories.get(uuid);
        }
		return retur;
	}
	/**
	 * Set the ExtraStorage's Inventory for a player
	 * 
	 * @param player
	 * @param inventory
	 * @throws NullPointerException If the player or the Inventory is null
	 */
	public static void setInventory(Player player, Inventory inventory) {
		if(player == null){
			new NullPointerException("The player cannot be null !").printStackTrace();
			return;
		}
		if(inventory == null){
			new NullPointerException("The inventory cannot be null !").printStackTrace();
			return;
		}
		ExtraStorage.setPlayerStorage(player, inventory);
	}
}
