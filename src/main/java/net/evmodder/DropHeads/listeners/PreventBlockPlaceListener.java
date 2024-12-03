package net.evmodder.DropHeads.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import net.evmodder.DropHeads.DropHeads;
import net.kyori.adventure.text.TextComponent;
import plugin.extras.HeadUtils;
import plugin.extras.TextUtils;
import plugin.util.PlayerProfile;

public class PreventBlockPlaceListener implements Listener{
	private final TextComponent PREVENT_PLACE_MSG;

	public PreventBlockPlaceListener(){
		final DropHeads pl = DropHeads.getPlugin();
		String msg = pl.getInternalAPI().loadTranslationStr("prevent-head-placement-message", "");
		if(msg.isEmpty()) msg = pl.getConfig().getString("prevent-head-placement-message", "&7[&6DropHeads&7]&c No permission to place head blocks");
		PREVENT_PLACE_MSG = (TextComponent) TextUtils.translateAlternateColorCodes('&', msg);
	}

	// This listener is only registered when 'prevent-head-placement' = true
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlaceEvent(BlockPlaceEvent evt){
		if(!HeadUtils.isPlayerHead(evt.getBlockPlaced().getType())) return;
		ItemStack headItem = evt.getHand() == EquipmentSlot.HAND
				? evt.getPlayer().getInventory().getItemInMainHand()
						: evt.getPlayer().getInventory().getItemInOffHand();
		if(!headItem.hasItemMeta()) return;
		final SkullMeta meta = (SkullMeta) headItem.getItemMeta();
		final PlayerProfile profile = HeadUtils.getGameProfile(meta);
		if(profile == null) return;

		if(evt.getPlayer().hasPermission("dropheads.canplacehead")) return;
		evt.getPlayer().sendMessage(PREVENT_PLACE_MSG);
		evt.setCancelled(true);
	}
}