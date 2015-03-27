/*
 *  Copyright (C) 2015 Antony Prince and Antony Prince and Gabriel POTTER
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
 
 import java.util.UUID;

 import org.bukkit.Sound;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.kitteh.vanish.VanishPlugin; 
 
 
 class VNPCompat
 {
   protected static void vanishPlayerPickupItemEvent(PlayerPickupItemEvent event, Plugin plugin)
     throws Exception
   {
     VanishPlugin vnp = (VanishPlugin) plugin.getServer().getPluginManager().getPlugin("VanishNoPacket");
     if (vnp.getManager().isVanished(event.getPlayer()))
     {
       event.setCancelled(true);
     }
     else
     {
				  UUID player_uuid = ExtraStorage.getUUIDMinecraft(event.getPlayer(), true);
       ((Inventory)ExtraStorage.Inventories.get(player_uuid)).addItem(new ItemStack[] { event.getItem().getItemStack() });
       
 
       ExtraStorage.invChanged.put(player_uuid, Boolean.valueOf(true));
       event.getItem().remove();
       event.setCancelled(true);
       event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_PICKUP, 100.0F, 100.0F);
     }
   }
 }
