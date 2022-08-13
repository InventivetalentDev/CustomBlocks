package org.inventivetalent.customblocks;

import com.google.common.io.BaseEncoding;
import org.bukkit.Location;
import org.bukkit.block.Skull;
import org.bukkit.inventory.meta.SkullMeta;
import org.inventivetalent.mcwrapper.auth.GameProfileWrapper;
import org.inventivetalent.mcwrapper.auth.properties.PropertyWrapper;
import org.inventivetalent.reflection.minecraft.Minecraft;
import org.inventivetalent.reflection.resolver.*;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;
import org.inventivetalent.reflection.resolver.minecraft.OBCClassResolver;

import java.lang.reflect.Field;
import java.util.UUID;

public class HeadTextureChanger {

    static final ClassResolver classResolver = new ClassResolver();
    static final NMSClassResolver nmsClassResolver = new NMSClassResolver();
    static final OBCClassResolver obcClassResolver = new OBCClassResolver();

    static Class<?> World = nmsClassResolver.resolveSilent("World");
    static Class<?> WorldServer = nmsClassResolver.resolveSilent("WorldServer");
    static Class<?> TileEntitySkull = nmsClassResolver.resolveSilent("TileEntitySkull");
    static Class<?> NBTTagCompound = nmsClassResolver.resolveSilent("NBTTagCompound");
    static Class<?> NBTBase = nmsClassResolver.resolveSilent("NBTBase");
    static Class<?> GameProfileSerializer = nmsClassResolver.resolveSilent("GameProfileSerializer");
    static Class<?> CraftMetaSkull = obcClassResolver.resolveSilent("inventory.CraftMetaSkull");

    static Class<?> GameProfile = classResolver.resolveSilent("net.minecraft.util.com.mojang.authlib.GameProfile", "com.mojang.authlib.GameProfile");

    static final MethodResolver WorldMethodResolver = new MethodResolver(World);
    static final MethodResolver WorldServerMethodResolver = new MethodResolver(WorldServer);
    static final MethodResolver TileEntitySkullMethodResolver = new MethodResolver(TileEntitySkull);
    static final MethodResolver NBTTagCompoundMethodResolver = new MethodResolver(NBTTagCompound);
    static final MethodResolver GameProfileSerializerMethodResolver = new MethodResolver(GameProfileSerializer);

    static final FieldResolver TileEntitySkullFieldResolver = new FieldResolver(TileEntitySkull);
    static final FieldResolver CraftMetaSkullFieldResolver = new FieldResolver(CraftMetaSkull);

    static final ConstructorResolver NBTTagCompoundConstructorResolver = new ConstructorResolver(NBTTagCompound);
    static final ConstructorResolver CraftMetaSkullConstructorResolver = new ConstructorResolver(CraftMetaSkull);

    public static String encodeBase64(byte[] bytes) {
        return BaseEncoding.base64().encode(bytes);
    }

    public static String buildResourceLocation(String url) {
        String format = "{textures:{SKIN:{url:\"%s\"}}}";
        return String.format(format, url);
    }

    public static Object createProfile(String data) {
        try {
            GameProfileWrapper profileWrapper = new GameProfileWrapper(UUID.randomUUID(), "CustomBlock");
            PropertyWrapper propertyWrapper = new PropertyWrapper("textures", data);
            profileWrapper.getProperties().put("textures", propertyWrapper);

            return profileWrapper.getHandle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object createProfile(String value, String signature) {
        if (signature == null) {return createProfile(value);}
        try {
            GameProfileWrapper profileWrapper = new GameProfileWrapper(UUID.randomUUID(), "CustomBlock");
            PropertyWrapper propertyWrapper = new PropertyWrapper("textures", value, signature);
            profileWrapper.getProperties().put("textures", propertyWrapper);

            return profileWrapper.getHandle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void applyTextureToSkull(Skull skull, Object profile) throws Exception {
        Location location = skull.getLocation();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        Object world = Minecraft.getHandle(location.getWorld());
        Object tileEntity = WorldServerMethodResolver.resolve("getTileEntity").invoke(world, x, y, z);
        TileEntitySkullFieldResolver.resolveByFirstType(GameProfile).set(tileEntity, profile);
        WorldMethodResolver.resolve(new ResolverQuery("notify", int.class, int.class, int.class)).invoke(world, x, y, z);
    }

    public static SkullMeta applyTextureToMeta(SkullMeta meta, Object profile) throws Exception {
        if (meta == null) {throw new IllegalArgumentException("meta cannot be null");}
        if (profile == null) {throw new IllegalArgumentException("profile cannot be null");}
        Object baseNBTTag = NBTTagCompound.newInstance();
        Object ownerNBTTag = NBTTagCompound.newInstance();

        GameProfileSerializerMethodResolver.resolve(new ResolverQuery("serialize", NBTTagCompound, GameProfile)).invoke(null, ownerNBTTag, profile);
        NBTTagCompoundMethodResolver.resolve(new ResolverQuery("set", String.class, NBTBase)).invoke(baseNBTTag, "SkullOwner", ownerNBTTag);

        SkullMeta newMeta = (SkullMeta) CraftMetaSkullConstructorResolver.resolve(new Class[]{NBTTagCompound}).newInstance(baseNBTTag);

        Field profileField = CraftMetaSkullFieldResolver.resolve("profile");
        profileField.set(meta, profile);
        profileField.set(newMeta, profile);

        return newMeta;
    }

}
