

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
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setDisplayName(displayName);
        List<String> lore = new ArrayList<>(Arrays.asList(blockName, getImage()));
        if (extraLore != null) {lore.addAll(extraLore);}
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
