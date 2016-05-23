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

import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.imgur.UploadCallback;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.command.Command;
import org.inventivetalent.pluginannotations.command.Completion;
import org.inventivetalent.pluginannotations.command.JoinedArg;
import org.inventivetalent.pluginannotations.command.Permission;
import org.inventivetalent.pluginannotations.config.ConfigValue;
import org.inventivetalent.skullclient.SkullCallback;
import org.inventivetalent.skullclient.SkullData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class CustomBlocks extends JavaPlugin implements Listener {

	File savesFolder = new File(getDataFolder(), "saves");

	@ConfigValue(path = "imgurClientId") public          String  imgurClientId;
	@ConfigValue(path = "forceVisibility") public static boolean forceVisibility;

	@ConfigValue(path = "debug") public boolean debug = false;

	BlockManager blockManager;

	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("PacketListenerApi")) {
			getLogger().warning("****************************************");
			getLogger().warning(" ");
			getLogger().warning("    Please install PacketListenerApi    ");
			getLogger().warning("       https://r.spiget.org/2930        ");
			getLogger().warning(" ");
			getLogger().warning("****************************************");
			throw new RuntimeException("Missing PacketListenerApi");
		}

		saveDefaultConfig();
		PluginAnnotations.loadAll(this, this);
		Bukkit.getPluginManager().registerEvents(this, this);

		if (!savesFolder.exists()) {
			savesFolder.mkdirs();
		}

		blockManager = new BlockManager(this);
		new PacketListener(this);
	}

	@Command(name = "createcustomblock",
			 aliases = {
					 "createblock",
					 "customblockcreate",
					 "cbc",
					 "ccb"
			 },
			 usage = "<Name> <URL>",
			 description = "Create a custom block",
			 min = 2,
			 max = 2,
			 fallbackPrefix = "customblocks")
	@Permission("customblocks.create")
	public void createBlock(final CommandSender sender, final String name, final String urlString) {
		if (blockManager.doesBlockExist(name)) {
			sender.sendMessage("§cBlock already exists");
			return;
		}
		try {
			final URL url = new URL(urlString);

			sender.sendMessage("§7Creating block...");
			blockManager.createBlock(name, urlString, new ImageDownloadCallback() {
				@Override
				public void exception(Throwable throwable) {
					sender.sendMessage("§cCould not download image: " + throwable.getMessage());
					getLogger().log(Level.WARNING, "Could not download image", throwable);
				}

				@Override
				public void done() {
					sender.sendMessage("§eOriginal image downloaded");
					sender.sendMessage(" ");
					sender.sendMessage("§7Generating images...");
				}
			}, new UploadCallback() {
				@Override
				public void exception(Throwable throwable) {
					sender.sendMessage("§cCould not upload image: " + throwable.getMessage());
					getLogger().log(Level.WARNING, "Could not upload image", throwable);
				}

				@Override
				public void uploaded(Map<String, String> map, JsonElement jsonElement) {
					sender.sendMessage("§eImages generated & uploaded");
					sender.sendMessage(" ");
					sender.sendMessage("§7Generating skull data (This may take up to 5 minutes)...");
				}
			}, new SkullCallback() {
				int generateCounter = 1;

				@Override
				public void waiting(long l) {
					sender.sendMessage("§8(Waiting " + (l / 1000D) + "s for next skull)");
				}

				@Override
				public void uploading() {
					sender.sendMessage("§7Generating skull (" + (generateCounter++) + "/9)...");
				}

				@Override
				public void error(String s) {
					sender.sendMessage("§cCould not generate skull data: " + s);
					getLogger().log(Level.WARNING, "Could not generate skull data: " + s);
				}

				@Override
				public void done(SkullData skullData) {
					sender.sendMessage("§eSkull data generated. §7Finishing block...");
					sender.sendMessage(" ");
				}
			}, new BlockCallback() {
				@Override
				public void done(final CustomBlock block) {
					sender.sendMessage("§aBlock successfully generated. §eUse §7/cbg " + name + " §eto get it!");
					Bukkit.getScheduler().runTask(CustomBlocks.this, new Runnable() {
						@Override
						public void run() {
							blockManager.saveBlock(block);
							sender.sendMessage(" ");
							sender.sendMessage("§7Block saved to file.");
						}
					});
				}
			});
		} catch (MalformedURLException e) {
			sender.sendMessage("§cInvalid URL");
		}
	}

	@Command(name = "givecustomblock",
			 aliases = {
					 "getcustomblock",
					 "customblockgive",
					 "cbg",
					 "gcb"
			 },
			 usage = "<Name> [Flags (solid, smallBlock, bigBlock, fullBlock)]",
			 description = "Get a custom block",
			 min = 1,
			 max = -1,
			 fallbackPrefix = "customblocks")
	@Permission("customblocks.give")
	public void giveBlock(final Player sender, final String name, @JoinedArg String flagsString) {
		if (!blockManager.doesBlockExist(name)) {
			sender.sendMessage("§cBlock doesn't exist");
			return;
		}
		if (flagsString == null) { flagsString = ""; }
		flagsString = flagsString.toLowerCase();
		Set<String> flags = new HashSet<>(Arrays.asList(flagsString.split(" ")));

		CustomBlock customBlock = blockManager.loadBlock(name);
		try {
			ItemStack itemStack = customBlock.baseAsItem(flags);
			sender.getInventory().addItem(itemStack);
		} catch (Throwable e) {
			sender.sendMessage("§cFailed to generate skull item. See console for details.");
			getLogger().log(Level.SEVERE, "Failed to generate skull item", e);
		}
	}

	@Completion(name = "givecustomblock")
	public void giveBlock(List<String> completions, Player sender, final String name, @JoinedArg String flagsString) {
		if (sender.hasPermission("customblocks.give")) {
			if (name == null) {
				for (String s : savesFolder.list()) {
					if (s.endsWith(".cb")) {
						completions.add(s.toLowerCase().substring(0, s.length() - 3));
					}
				}
			} else {
				completions.addAll(Arrays.asList("solid", "smallBlock", "bigBlock", "fullBlock"));
			}
		}
	}

	@EventHandler
	public void on(PlayerInteractEvent e) {
		if (e.getClickedBlock() != null) {
			if (e.getAction() != Action.RIGHT_CLICK_BLOCK) { return; }
			if (e.getClickedBlock().getType() != Material.AIR) {
				ItemStack hand = e.getPlayer().getItemInHand();
				if (hand != null && (hand.getType() == Material.SKULL_ITEM || hand.getType() == Material.SKULL)) {
					if (hand.hasItemMeta()) {
						if (e.getPlayer().hasPermission("customblocks.place")) {
							String displayName = hand.getItemMeta().getDisplayName();
							if ("§6CustomBlock".equals(displayName)) {
								if (hand.getItemMeta().getLore().size() < 2) { return; }
								e.setCancelled(true);
								String name = hand.getItemMeta().getLore().get(0);
								if (!blockManager.doesBlockExist(name)) {
									e.getPlayer().sendMessage("§cBlock file doesn't exist");
									return;
								}
								CustomBlock customBlock = blockManager.loadBlock(name);
								if (customBlock == null) { return; }

								List<String> flags = new ArrayList<>(hand.getItemMeta().getLore());
								flags.remove(0);// This feels so cheaty...
								flags.remove(0);

								int size = flags.contains("fullblock") ? 2 : flags.contains("bigblock") ? 1 : 0;
								customBlock.spawnOnClicked(e.getClickedBlock(), e.getBlockFace(), flags.contains("solid"), size);
							}
						}
					}
				}
			}
		}
	}

}
