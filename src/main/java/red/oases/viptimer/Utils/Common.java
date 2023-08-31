package red.oases.viptimer.Utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import red.oases.viptimer.Extra.Enums.TaskAction;
import red.oases.viptimer.Extra.Enums.TimeUnit;
import red.oases.viptimer.Extra.Exceptions.UnexpectedMatchException;
import red.oases.viptimer.Extra.Interfaces.StringHandler;
import red.oases.viptimer.Objects.Delivery;
import red.oases.viptimer.Objects.Privilege;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Common {
    public static long mustPositiveLong(String target) {
        long result;
        try {
            result = Long.parseLong(target);
        } catch (NumberFormatException e) {
            return 0;
        }
        return result;
    }

    /**
     * 转换对应字符串为非负整数。如果转换失败，返回 0。
     *
     * @param target 待转换的字符串
     * @return 成功为对应整数，不成功为 0
     */
    public static int mustPositive(String target) {
        int result;
        try {
            result = Integer.parseInt(target);
        } catch (NumberFormatException e) {
            return 0;
        }
        return result;
    }

    public static long getUntil(int number, String unit) {
        return getUntil(number, TimeUnit.from(unit));
    }

    public static long getUntil(int number, TimeUnit unit) {
        var now = new Date().getTime();
        switch (unit) {
            case HOUR -> {
                return now + number * 3_600_000L;
            }

            case DAY -> {
                return now + number * 24 * 3_600_000L;
            }

            case MONTH -> {
                return now + number * 30 * 24 * 3_600_000L;
            }
        }
        throw new UnexpectedMatchException();
    }

    public static String formatTimestamp(long epoch) {
        return formatDate(new Date(epoch));
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static boolean notType(String t) {
        return !getTypes().contains(t);
    }

    public static void executeCommands(List<String> commands, StringHandler handler) {
        for (var cmd : commands) {
            Bukkit.getServer().dispatchCommand(
                    Bukkit.getConsoleSender(),
                    handler.handle(cmd)
            );
        }
    }

    public static void takePrivileges(String player, String type) {
        executeCommands(Privilege.of(type).take(), cmd -> cmd.replaceAll("\\$player", player));
    }

    public static void givePrivileges(String player, String type) {
        executeCommands(Privilege.of(type).give(), cmd -> cmd.replaceAll("\\$player", player));
    }

    public static void takePrivilegesOrLater(String player, String type) {
        var p = Bukkit.getPlayer(player);
        if (p != null) {
            takePrivileges(player, type);
        } else {
            Logs.info("A 'TAKE' Delivery is scheduled for %s.%s due to target-offline.".formatted(player, type));
            Delivery.doLater(player, type, TaskAction.TAKE);
        }
    }

    public static void givePrivilegesOrLater(String player, String type) {
        var p = Bukkit.getPlayer(player);
        if (p != null) {
            givePrivileges(player, type);
        } else {
            Logs.info("A 'GIVE' Delivery is scheduled for %s.%s due to target-offline.".formatted(player, type));
            Delivery.doLater(player, type, TaskAction.GIVE);
        }
    }

    public static List<String> getTypes() {
        var section = Files.types.getConfigurationSection("types");
        if (section == null) return List.of();
        return section.getKeys(false).stream().toList();
    }

    /**
     * 获取当前插件所在服务器对应的 instance id
     * 如果不存在则会创建，储存在 types.yml 内
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static String getInstanceId() {
        var instId = Files.types.getString("inst_id");

        if (instId != null) {
            UUID.fromString(instId);
            // throw IllegalArgumentException if not valid.
        } else {
            instId = UUID.randomUUID().toString();
            Files.types.set("inst_id", instId);
            Files.saveTypes();
        }

        return instId;
    }

    public static void transferTypes() {
        switch (Const.role) {
            case DISTRIBUTOR -> {
                if (!Data.updateDistribution(getInstanceId(), Files.types.saveToString())) {
                    Logs.severe("Cannot create distribution of instance " + getInstanceId() + ".");
                }
            }

            case RECEIVER -> {
                var distribution = Data.getDistributionUnreceived();
                if (distribution == null) return;
                // Backup current instance id.
                var instanceId = getInstanceId();
                try {
                    Files.types.loadFromString(distribution.dist_content());
                } catch (InvalidConfigurationException e) {
                    throw new RuntimeException(e.getMessage());
                }
                if (!distribution.setReceived(instanceId)) {
                    Logs.severe("Cannot mark received for Distribution by " + distribution.dist_by() + ", to be received by " + instanceId + ".");
                }
                // Override incoming instance id.
                Files.types.set("inst_id", instanceId);
                Files.saveTypes();
            }
        }
    }
}
