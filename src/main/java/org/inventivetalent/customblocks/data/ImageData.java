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

package org.inventivetalent.customblocks.data;

import lombok.*;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.customblocks.HeadTextureChanger;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.mcwrapper.auth.properties.PropertyWrapper;
import org.mineskin.data.Texture;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(doNotUseGetters = true)
@ToString(doNotUseGetters = true)
public class ImageData {

	protected String image;
	protected String value;
	protected String signature;

	public GameProfileWrapper toProfile() {
		GameProfileWrapper profileWrapper = new GameProfileWrapper(UUID.randomUUID(), "CustomBlock");
		profileWrapper.getProperties().put("textures", new PropertyWrapper("textures", value, signature));
		return profileWrapper;
	}

	public ItemStack toItem(String displayName, String blockName) {
		return toItem(displayName, blockName, null);
	}

	public ItemStack toItem(String displayName, String blockName, Collection<String> extraLore) {
		ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
		meta.setDisplayName(displayName);
		List<String> lore = new ArrayList<>(Arrays.asList(blockName, getImage()));
		if (extraLore != null) { lore.addAll(extraLore); }
		meta.setLore(lore);
		try {
			HeadTextureChanger.applyTextureToMeta(meta, toProfile().getHandle());
		} catch (Exception e) {
			throw new RuntimeException();
		}
		itemStack.setItemMeta(meta);
		return itemStack;
	}

	public static ImageData fromProperty(String image, Texture texture) {
		return new ImageData(image, texture.value, texture.signature);
	}

}
