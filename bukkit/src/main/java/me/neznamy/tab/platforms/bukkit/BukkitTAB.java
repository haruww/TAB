package me.neznamy.tab.platforms.bukkit;

import com.github.puregero.multilib.MultiLib;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.platforms.bukkit.nms.NMSStorage;
import me.neznamy.tab.platforms.bukkit.platform.BukkitPlatform;
import me.neznamy.tab.platforms.bukkit.platform.FoliaPlatform;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.util.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.UUID;

/**
 * Main class for Bukkit.
 */
public class BukkitTAB extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!isVersionSupported()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        TAB.create(ReflectionUtils.classExists("io.papermc.paper.threadedregions.RegionizedServer") ?
                new FoliaPlatform(this) : new BukkitPlatform(this));

        setupMultiLibChannels();
    }

    private void setupMultiLibChannels() {
        MultiLib.onString(this, "tab-player-join", (data) -> {
            String[] parts = data.split(":");
            TAB.getInstance().getCPUManager().runTaskLater(300, "Tablist name formatting", TabConstants.CpuUsageCategory.PLAYER_JOIN, () -> {
                Player p = getPlayerMultiLib(UUID.fromString(parts[0]));
                TAB.getInstance().getFeatureManager().onJoin(new BukkitTabPlayer((BukkitPlatform) TAB.getInstance().getPlatform(), p));
            });
        });
        MultiLib.onString(this, "tab-player-quit", (data) -> {
            TAB.getInstance().getCPUManager().runTask(() -> TAB.getInstance().getFeatureManager().onQuit(TAB.getInstance().getPlayer(UUID.fromString(data))));
        });
        MultiLib.onString(this, "tab-player-world-change", (data) -> {
            String[] parts = data.split(":");
            TAB.getInstance().getCPUManager().runTask(() ->
                    TAB.getInstance().getFeatureManager().onWorldChange(UUID.fromString(parts[0]), parts[1]));
        });
    }

    private Player getPlayerMultiLib(UUID uuid) {
        for (Player all : MultiLib.getAllOnlinePlayers()) {
            if (all.getUniqueId().equals(uuid)) return all;
        }
        return null;
    }


    @Override
    public void onDisable() {
        //null check due to compatibility check making instance not get set on unsupported versions
        if (TAB.getInstance() != null) TAB.getInstance().unload();
    }
    
    /**
     * Initializes all used NMS classes, constructors, fields and methods.
     * Returns {@code true} if everything went successfully,
     * {@code false} if anything went wrong.
     *
     * @return  {@code true} if server version is compatible, {@code false} if not
     */
    private boolean isVersionSupported() {
        try {
            NMSStorage.setInstance(new NMSStorage());
            return true;
        } catch (Exception ex) {
            if (ProtocolVersion.fromFriendlyName(Bukkit.getBukkitVersion().split("-")[0]) == ProtocolVersion.UNKNOWN_SERVER_VERSION) {
                Bukkit.getConsoleSender().sendMessage(String.format(
                        "%s[TAB] Your server version is not compatible. This plugin version was made for %s - %s. Disabling.",
                        EnumChatFormat.RED.getFormat(), ProtocolVersion.V1_5, ProtocolVersion.LATEST_KNOWN_VERSION
                ));
            } else {
                Bukkit.getConsoleSender().sendMessage(EnumChatFormat.RED.getFormat() + "[TAB] A compatibility issue " +
                        "with your server was found. Unless you are running some really weird server software, this is a bug.");
            }
            return false;
        }
    }
}