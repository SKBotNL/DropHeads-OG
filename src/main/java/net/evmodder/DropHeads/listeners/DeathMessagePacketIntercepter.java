package net.evmodder.DropHeads.listeners;

import java.util.HashSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.evmodder.DropHeads.DropHeads;

public class DeathMessagePacketIntercepter {
    private final Plugin pl;
    private final boolean REPLACE_PLAYER_DEATH_MSG, REPLACE_PET_DEATH_MSG;
    private final HashSet<UUID> unblockedDeathBroadcasts;
    private final HashSet<String> unblockedSpecificDeathMsgs;
    private final HashSet<String> blockedSpecificMsgs;

    private final Pattern uuidPattern1 = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    private final Pattern uuidPattern2 = Pattern.compile("\\[I?;?\\s*(-?[0-9]+),\\s*(-?[0-9]+),\\s*(-?[0-9]+),\\s*(-?[0-9]+)\\s*\\]");

    private ProtocolManager protocolManager;
    private PacketListener packetListener;

    public DeathMessagePacketIntercepter(boolean replacePlayerDeathMsg, boolean replacePetDeathMsg){
        pl = DropHeads.getPlugin();
        REPLACE_PLAYER_DEATH_MSG = replacePlayerDeathMsg;
        REPLACE_PET_DEATH_MSG = replacePetDeathMsg;
        unblockedDeathBroadcasts = new HashSet<>();
        unblockedSpecificDeathMsgs = new HashSet<>();
        blockedSpecificMsgs = new HashSet<>();

        protocolManager = ProtocolLibrary.getProtocolManager();
        registerPacketListener();
    }

    public boolean hasDeathMessage(Entity e){
        return e instanceof Player || (
                e instanceof Tameable &&
                ((Tameable)e).getOwner() != null &&
                pl.getServer().getEntity(((Tameable)e).getOwner().getUniqueId()) != null
        );
    }

    private UUID parseUUIDFromFourIntStrings(String s1, String s2, String s3, String s4){
        final Integer i1 = Integer.parseInt(s1), i2 = Integer.parseInt(s2), i3 = Integer.parseInt(s3), i4 = Integer.parseInt(s4); 
        return new UUID((long)i1 << 32 | i2 & 0xFFFFFFFFL, (long)i3 << 32 | i4 & 0xFFFFFFFFL);
    }

    private void registerPacketListener() {
        packetListener = new PacketAdapter(pl, ListenerPriority.NORMAL, PacketType.Play.Server.SYSTEM_CHAT, PacketType.Play.Server.CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.isCancelled()) return;

                if (event.getPacketType() != PacketType.Play.Server.CHAT && event.getPacketType() != PacketType.Play.Server.SYSTEM_CHAT) return;

                WrappedChatComponent chatComponent = null;
                if (event.getPacketType() == PacketType.Play.Server.CHAT) {
                    // In versions before 1.19
                    chatComponent = event.getPacket().getChatComponents().read(0);
                } else {
                    // For 1.19 and above
                    chatComponent = event.getPacket().getChatComponents().read(0);
                }

                if (chatComponent == null) return;

                String jsonMsg = chatComponent.getJson();

                if (blockedSpecificMsgs.contains(jsonMsg)) {
                    event.setCancelled(true);
                    return;
                }

                if (!jsonMsg.startsWith("{\"translate\":\"death.") || unblockedSpecificDeathMsgs.remove(jsonMsg)) {
                    return;
                }

                final UUID uuid;
                Matcher matcher2 = uuidPattern2.matcher(jsonMsg);
                if (matcher2.find()) {
                    uuid = parseUUIDFromFourIntStrings(matcher2.group(1), matcher2.group(2), matcher2.group(3), matcher2.group(4));
                } else {
                    Matcher matcher1 = uuidPattern1.matcher(jsonMsg);
                    if (matcher1.find()) {
                        uuid = UUID.fromString(matcher1.group());
                    } else {
                        pl.getLogger().warning("Unable to find UUID from death message: " + jsonMsg);
                        return;
                    }
                }

                if (unblockedDeathBroadcasts.contains(uuid)) {
                    return;
                }

                Bukkit.getScheduler().runTask(pl, () -> {
                    if (unblockedDeathBroadcasts.contains(uuid)) {
                        return;
                    }
                    final Entity victim = pl.getServer().getEntity(uuid);
                    if (victim == null) {
                        pl.getLogger().warning("Unable to find death-message entity by UUID: " + uuid);
                        return;
                    }
                    if (!hasDeathMessage(victim)) {
                        pl.getLogger().warning("Detected abnormal death message for non-player entity: " + jsonMsg);
                    }

                    final boolean shouldReplaceDeathMsg = victim instanceof Player ? REPLACE_PLAYER_DEATH_MSG : REPLACE_PET_DEATH_MSG;
                    if (!shouldReplaceDeathMsg) {
                        unblockedSpecificDeathMsgs.add(jsonMsg);
                        return;
                    }

                    // Cancel the packet to prevent the death message from being sent
                    event.setCancelled(true);
                });
            }
        };
        protocolManager.addPacketListener(packetListener);
    }

    public void unregisterPacketListener() {
        if (packetListener != null) {
            protocolManager.removePacketListener(packetListener);
        }
    }

    public void unblockDeathMessage(Entity entity){
        if(hasDeathMessage(entity)){
            unblockedDeathBroadcasts.add(entity.getUniqueId());
            new BukkitRunnable(){@Override public void run(){unblockedDeathBroadcasts.remove(entity.getUniqueId());}}.runTaskLater(pl, 5);
        }
    }

    public void blockSpeficicMessage(String message, long ticksBlockedFor){
        blockedSpecificMsgs.add(message);
        new BukkitRunnable(){@Override public void run(){blockedSpecificMsgs.remove(message);}}.runTaskLater(pl, ticksBlockedFor);
    }

    public void unregisterAll(){
        unregisterPacketListener();
    }
}