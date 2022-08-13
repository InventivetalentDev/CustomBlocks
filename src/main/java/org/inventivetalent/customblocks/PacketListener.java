

package org.inventivetalent.customblocks;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
        if (instance != null) {
            throw new IllegalStateException("Cannot instantiate PacketListener twice");
        }
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
                int id = (int) packet.getPacketValue(0);
                final Object[] useAction = {packet.getPacketValue(1)};

                if (useAction[0] == null) {
                    return;
                }

                Bukkit.getScheduler().runTask(getPlugin(), () -> {
                    ArmorStand ent = null;
                    for (ArmorStand e : packet.getPlayer().getWorld().getEntitiesByClass(ArmorStand.class)) {
                        if (e.getEntityId() == id) {
                            ent = e;
                        }
                    }
                    if (ent == null) {
                        return;
                    }
                    if (ent.getCustomName() == null || !ent.getCustomName().startsWith("CustomBlock_#")) {
                        return;
                    }

                    Player p = packet.getPlayer();

                    if (!(useAction[0] instanceof Enum)) {
                        try {
                            useAction[0] = HeadTextureChanger.EnumEntityUseActionMethodResolver.resolveIndex(0).invoke(useAction[0]);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                    }
                    int action = ((Enum) useAction[0]).ordinal();

                    final Block block = ent.getLocation().add(0, 1, 0).getBlock();

                    if (!p.hasPermission("customblocks.break")) {
                        packet.setCancelled(true);
                        return;
                    }

                    if (action != 1) {
                        packet.setCancelled(true); // Interact
                        return;
                    }

                    final Entity ent2 = ent;

                    final String baseName = ent.getCustomName();
                    for (final Entity ent1 : p.getNearbyEntities(16, 16, 16)) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                            if (ent1 == null) {
                                return;
                            }
                            if (ent1 instanceof ArmorStand) {
                                if (ent1.getCustomName() != null && ent1.getCustomName().equals(baseName)) {
                                    if (ent1.getLocation().getBlockX() == ent2.getLocation().getBlockX() && ent1.getLocation().getBlockZ() == ent2.getLocation().getBlockZ() && (ent1.getLocation().getBlockY() == ent2.getLocation().getBlockY() || ent1.getLocation().getBlockY() + 1 == ent2.getLocation().getBlockY())) {
                                        ent1.remove();
                                    }
                                }
                            }
                        });
                    }

                    Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
                        block.breakNaturally(new ItemStack(Material.AIR));
                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 159);
                        block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, 42);
                    });
                });
            }
        }
    }

}
