/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.customblocks;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.PacketOptions;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;

public class PacketListener extends PacketHandler {

	protected static PacketListener instance;

	public PacketListener(Plugin pl) {
		super(pl);
		if (instance != null) { throw new IllegalStateException("Cannot instantiate PacketListener twice"); }
		instance = this;
		addHandler(instance);
	}

	public static void disable() {
		if (instance != null) {
			removeHandler(instance);
			instance = null;
		}
	}

	@Override
	public void onSend(SentPacket packet) {
	}

	@Override
	@PacketOptions(forcePlayer = true)
	public void onReceive(ReceivedPacket packet) {
		if (packet.hasPlayer()) {
			if (packet.getPacketName().equals("PacketPlayInUseEntity")) {
				int id = (int) packet.getPacketValue("a");
				Object useAction = packet.getPacketValue("action");

				if (useAction == null) { return; }

				ArmorStand ent = null;
				for (ArmorStand e : packet.getPlayer().getWorld().getEntitiesByClass(ArmorStand.class)) {
					if (e.getEntityId() == id) {
						ent = e;
					}
				}
				if (ent == null) { return; }
				if (ent.getCustomName() == null || !ent.getCustomName().startsWith("CustomBlock_#")) { return; }

				Player p = packet.getPlayer();

				int action = ((Enum) useAction).ordinal();

				final Block block = ent.getLocation().add(0, 1, 0).getBlock();

				BlockBreakEvent event = new BlockBreakEvent(block, p);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					if (!p.hasPermission("customblocks.break")) {
						packet.setCancelled(true);
						return;
					}
				}

				if (action != 1) {
					packet.setCancelled(true);// Interact
					return;
				}

				final Entity ent2 = ent;

				final String baseName = ent.getCustomName();
				for (final Entity ent1 : p.getNearbyEntities(16, 16, 16)) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {

						@Override
						public void run() {
							if (ent1 == null) { return; }
							if (ent1 instanceof ArmorStand) {
								if (ent1.getCustomName() != null && ent1.getCustomName().equals(baseName)) {
									if (ent1.getLocation().getBlockX() == ent2.getLocation().getBlockX() && ent1.getLocation().getBlockZ() == ent2.getLocation().getBlockZ() && (ent1.getLocation().getBlockY() == ent2.getLocation().getBlockY() || ent1.getLocation().getBlockY() + 1 == ent2.getLocation().getBlockY())) {
										ent1.remove();
									}
								}
							}
						}
					});
				}

				Bukkit.getScheduler().scheduleSyncDelayedTask(this.getPlugin(), new Runnable() {

					@Override
					public void run() {
						block.breakNaturally(new ItemStack(Material.AIR));
						block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 159);
						block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 42);
					}
				});
			}
		}
	}

}
