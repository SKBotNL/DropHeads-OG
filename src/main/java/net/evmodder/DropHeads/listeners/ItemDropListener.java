package net.evmodder.DropHeads.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import net.evmodder.DropHeads.DropHeads;
import net.kyori.adventure.text.TextComponent;
import plugin.extras.HeadUtils;
import plugin.util.PlayerProfile;

public class ItemDropListener implements Listener{
	private final DropHeads pl;
	private final boolean /*TEXTURE_UPDATE, */FORCE_NAME_UPDATE, FORCE_LORE_UPDATE, TYPE_UPDATE;
	private final boolean SAVE_TYPE_IN_LORE;

	public ItemDropListener(){
		pl = DropHeads.getPlugin();
		//FORCE_TEXTURE_UPDATE = pl.getConfig().getBoolean("refresh-textures", false);
		FORCE_NAME_UPDATE = pl.getConfig().getBoolean("refresh-item-names", false);
		FORCE_LORE_UPDATE = pl.getConfig().getBoolean("refresh-item-lores", false) && !pl.getConfig().getBoolean("save-custom-lore", true);
		TYPE_UPDATE = pl.getConfig().getBoolean("update-piglin-heads", true);
		SAVE_TYPE_IN_LORE = pl.getConfig().getBoolean("show-head-type-in-lore", false);
	}

	private boolean hasCustomLore(ItemMeta meta){
		if(!meta.hasLore()) return false;
		if(!SAVE_TYPE_IN_LORE) return true;
		if(meta.lore().size() == 1 && DropHeads.stripColor((TextComponent) meta.lore().get(0)).matches("\\w+:\\w+")) return false;
		return true;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBarf(ItemSpawnEvent evt){
		if(!HeadUtils.isPlayerHead(evt.getEntity().getItemStack().getType()) || !evt.getEntity().getItemStack().hasItemMeta()
				|| pl.getInternalAPI().isHeadDatabaseHead(evt.getEntity().getItemStack())) return;

		ItemStack originalItem = evt.getEntity().getItemStack();
		final SkullMeta originalMeta = (SkullMeta) originalItem.getItemMeta();
		final PlayerProfile originalProfile = HeadUtils.getGameProfile(originalMeta);
		if(originalProfile == null) return;
		final ItemStack refreshedItem = pl.getAPI().getHead(originalProfile); // Gets a refreshed texture by textureKey (profile name)
		if(refreshedItem == null) return;
		//if(FORCE_TEXTURE_UPDATE){
		final PlayerProfile refreshedProfile = HeadUtils.getGameProfile((SkullMeta)refreshedItem.getItemMeta());
		HeadUtils.setGameProfile(originalMeta, refreshedProfile); // This refreshes the texture
		//}
		if(TYPE_UPDATE && originalItem.getType() != refreshedItem.getType()) originalItem.setType(refreshedItem.getType());

		if(!originalMeta.hasDisplayName() || FORCE_NAME_UPDATE) originalMeta.displayName(refreshedItem.getItemMeta().displayName());
		if(!hasCustomLore(originalMeta) || FORCE_LORE_UPDATE) originalMeta.lore(refreshedItem.getItemMeta().lore());
		originalItem.setItemMeta(originalMeta);
		evt.getEntity().setItemStack(originalItem);
	}

}