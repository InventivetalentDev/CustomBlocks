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

import lombok.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.inventivetalent.customblocks.data.CoordinateImageData;
import org.inventivetalent.customblocks.data.ImageData;
import org.inventivetalent.reflection.minecraft.Minecraft;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class CustomBlock {

    private String name;

    private ImageData base;

    private Set<CoordinateImageData> images = new HashSet<>();

    public CoordinateImageData getCoordinateData(int x, int y, int z) {
        for (CoordinateImageData data : images) {
            if (data.getX() == x && data.getY() == y && data.getZ() == z) {return data;}
        }
        return null;
    }

    public ItemStack baseAsItem(Collection<String> extraLore) {
        return base.toItem("§6CustomBlock", name, extraLore);
    }

    public void spawnOnClicked(Block clicked, BlockFace face, boolean solid, int size) {
        Location cLoc = clicked.getLocation();
        Location loc = cLoc.add(face.getModX(), face.getModY(), face.getModZ());
        if (solid) {
            if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1)) {
                loc.getBlock().setType(Material.BARRIER);
            } else {
                loc.getBlock().setType(Material.GLASS);
            }
        }
        if (CustomBlocks.forceVisibility) {
            Block b = loc.clone().add(0, -1, 0).getBlock();
            if (!b.getType().isTransparent()) {
                if (Minecraft.VERSION.newerThan(Minecraft.Version.v1_8_R1)) {
                    b.setType(Material.BARRIER);
                } else {
                    b.setType(Material.GLASS);
                }
            }
        }
        this.spawnBlock(loc, size);
    }

    private void spawnBlock(Location loc, int size) {
        double multiplier;
        double yMultiplier;
        double addX;
        double addY;
        double addZ;

        if (size == 2) {// fullBlock <-- Psst, try this with md_5's skin, it looks like a pufferfish :P
            double add = 0.2;
            multiplier = .25 + add;

            yMultiplier = multiplier;

            addX = -0.078125 * 2;
            addY = -0.875;
            addZ = -0.078125 * 2;
        } else if (size == 1) {// bigBlock
            multiplier = .5935;
            yMultiplier = .5935;

            addX = -0.813 / 2;
            addY = -1.03;
            addZ = -0.813 / 2;
        } else {// smallBlock
            multiplier = .415;
            yMultiplier = .415;

            addX = -0.12;
            addY = -0.15;
            addZ = -0.12;
        }

        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                for (int z = 0; z < 2; z++) {
                    CoordinateImageData data = getCoordinateData(x, y, 2 - z - 1);

                    ArmorStand stand = loc.getWorld().spawn(loc.clone().add(0, -1, 0).add(multiplier * (x + 1) + addX, yMultiplier * (y + 1) + addY, multiplier * (z + 1) + addZ), ArmorStand.class);
                    stand.setCustomName("CustomBlock_#" + this.name);
                    stand.setCustomNameVisible(false);
                    stand.setGravity(false);
                    stand.setVisible(false);
                    stand.setSmall(size == 0);
                    stand.setHelmet(data.toItem("CustomBlock", this.name));
                }
            }
        }
    }

}
