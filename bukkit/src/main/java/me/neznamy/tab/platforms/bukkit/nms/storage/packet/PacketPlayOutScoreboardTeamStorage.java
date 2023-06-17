package me.neznamy.tab.platforms.bukkit.nms.storage.packet;

import lombok.SneakyThrows;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import me.neznamy.tab.platforms.bukkit.nms.storage.nms.NMSStorage;
import me.neznamy.tab.shared.platform.Scoreboard;
import me.neznamy.tab.shared.platform.Scoreboard.CollisionRule;
import me.neznamy.tab.shared.platform.Scoreboard.NameVisibility;
import me.neznamy.tab.shared.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

@SuppressWarnings({"unchecked", "rawtypes"})
public class PacketPlayOutScoreboardTeamStorage {

    public static Class<?> CLASS;
    public static Constructor<?> CONSTRUCTOR;
    public static Method Constructor_of;
    public static Method Constructor_ofBoolean;
    public static Field NAME;
    public static Field ACTION;
    public static Field PLAYERS;

    public static Class<Enum> EnumNameTagVisibility;
    public static Class<Enum> EnumTeamPush;

    public static Class<?> ScoreboardTeam;
    public static Constructor<?> newScoreboardTeam;
    public static Method ScoreboardTeam_getPlayerNameSet;
    public static Method ScoreboardTeam_setNameTagVisibility;
    public static Method ScoreboardTeam_setCollisionRule;
    public static Method ScoreboardTeam_setPrefix;
    public static Method ScoreboardTeam_setSuffix;
    public static Method ScoreboardTeam_setColor;
    public static Method ScoreboardTeam_setAllowFriendlyFire;
    public static Method ScoreboardTeam_setCanSeeFriendlyInvisibles;

    public static void load(NMSStorage nms) throws NoSuchMethodException {
        newScoreboardTeam = ScoreboardTeam.getConstructor(nms.Scoreboard, String.class);
        NAME = ReflectionUtils.getFields(CLASS, String.class).get(0);
        ACTION = ReflectionUtils.getInstanceFields(CLASS, int.class).get(0);
        PLAYERS = ReflectionUtils.getFields(CLASS, Collection.class).get(0);
        ScoreboardTeam_getPlayerNameSet = ReflectionUtils.getMethods(ScoreboardTeam, Collection.class).get(0);
        if (nms.getMinorVersion() >= 9) {
            ScoreboardTeam_setCollisionRule = ReflectionUtils.getMethods(ScoreboardTeam, void.class, EnumTeamPush).get(0);
        }
        if (nms.getMinorVersion() >= 13) {
            ScoreboardTeam_setColor = ReflectionUtils.getMethods(ScoreboardTeam, void.class, nms.EnumChatFormat).get(0);
        }
        if (nms.getMinorVersion() >= 17) {
            Constructor_of = ReflectionUtils.getMethods(CLASS, CLASS, ScoreboardTeam).get(0);
            Constructor_ofBoolean = ReflectionUtils.getMethods(CLASS, CLASS, ScoreboardTeam, boolean.class).get(0);
        } else {
            CONSTRUCTOR = CLASS.getConstructor(ScoreboardTeam, int.class);
        }
    }

    @SneakyThrows
    private static void createTeamModern(Object team, ProtocolVersion clientVersion, String prefix, String suffix, NameVisibility visibility, CollisionRule collision) {
        NMSStorage nms = NMSStorage.getInstance();
        ScoreboardTeam_setPrefix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(prefix), clientVersion));
        ScoreboardTeam_setSuffix.invoke(team, nms.toNMSComponent(IChatBaseComponent.optimizedComponent(suffix), clientVersion));
        ScoreboardTeam_setColor.invoke(team, Enum.valueOf(nms.EnumChatFormat, EnumChatFormat.lastColorsOf(prefix).toString()));
        ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, visibility.name()));
        ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, collision.name()));
    }

    @SneakyThrows
    private static void createTeamLegacy(Object team, String prefix, String suffix, Scoreboard.NameVisibility visibility, CollisionRule collision) {
        NMSStorage nms = NMSStorage.getInstance();
        ScoreboardTeam_setPrefix.invoke(team, prefix);
        ScoreboardTeam_setSuffix.invoke(team, suffix);
        if (nms.getMinorVersion() >= 8) ScoreboardTeam_setNameTagVisibility.invoke(team, Enum.valueOf(EnumNameTagVisibility, visibility.name()));
        if (nms.getMinorVersion() >= 9) ScoreboardTeam_setCollisionRule.invoke(team, Enum.valueOf(EnumTeamPush, collision.name()));
    }

    @SneakyThrows
    public static Object register(String name, String prefix, String suffix, NameVisibility visibility, CollisionRule collision,
                                  Collection<String> players, int options, ProtocolVersion clientVersion) {
        NMSStorage nms = NMSStorage.getInstance();
        Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
        ((Collection<String>)ScoreboardTeam_getPlayerNameSet.invoke(team)).addAll(players);
        ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
        if (nms.getMinorVersion() >= 13) {
            createTeamModern(team, clientVersion, prefix, suffix, visibility, collision);
        } else {
            createTeamLegacy(team, prefix, suffix, visibility, collision);
        }
        if (nms.getMinorVersion() >= 17) {
            return Constructor_ofBoolean.invoke(null, team, true);
        }
        return CONSTRUCTOR.newInstance(team, 0);
    }

    @SneakyThrows
    public static Object unregister(String name) {
        NMSStorage nms = NMSStorage.getInstance();
        Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
        if (nms.getMinorVersion() >= 17) {
            return Constructor_of.invoke(null, team);
        } else {
            return CONSTRUCTOR.newInstance(team, 1);
        }
    }

    @SneakyThrows
    public static Object update(String name, String prefix, String suffix, NameVisibility visibility,
                                CollisionRule collision, int options, ProtocolVersion clientVersion) {
        NMSStorage nms = NMSStorage.getInstance();
        Object team = newScoreboardTeam.newInstance(nms.emptyScoreboard, name);
        ScoreboardTeam_setAllowFriendlyFire.invoke(team, (options & 0x1) > 0);
        ScoreboardTeam_setCanSeeFriendlyInvisibles.invoke(team, (options & 0x2) > 0);
        if (nms.getMinorVersion() >= 13) {
            createTeamModern(team, clientVersion, prefix, suffix, visibility, collision);
        } else {
            createTeamLegacy(team, prefix, suffix, visibility, collision);
        }
        if (nms.getMinorVersion() >= 17) {
            return Constructor_ofBoolean.invoke(null, team, false);
        }
        return CONSTRUCTOR.newInstance(team, 2);
    }
}
