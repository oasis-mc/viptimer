package red.oases.viptimer.Objects.Timers;

import org.bukkit.Bukkit;
import red.oases.viptimer.Objects.Privilege;
import red.oases.viptimer.Utils.Common;
import red.oases.viptimer.Utils.Data;
import red.oases.viptimer.Utils.Logs;

/**
 * <b>Delivery Timer</b><br/><br/>
 * 提供一个可重复运行的任务，此任务检测在线玩家的跨服权限发放（give/take 的执行）情况
 */
public class DeliveryTimer extends CancellableTimer {
    @Override
    protected void execute() {
        for (var p : Bukkit.getServer().getOnlinePlayers()) {
            for (var t : Common.getTypes()) {
                if (Data.hasRecord(p.getName(), t) && !Data.hasDelivery(p.getName(), t)) {
                    var record = Data.getRecord(p.getName(), t);
                    assert record != null;
                    Logs.info(p.getName() + "'s " + t + " is not delivered on this server.");
                    Common.givePrivileges(p.getName(), t);
                    Logs.send(p, "你已获得 %s，有效期至 %s".formatted(
                            Privilege.of(t).displayname(),
                            Common.formatTimestamp(record.getUntil())
                    ));
                    Logs.send(p, "多谢支持！");
                    Logs.info("Delivery completed!");
                    Data.createDelivery(p.getName(), t);
                }

                if (!Data.hasRecord(p.getName(), t) && Data.hasDelivery(p.getName(), t)) {
                    Logs.info(p.getName() + "'s " + t + " is not removed on this server.");
                    Common.takePrivileges(p.getName(), t);
                    Logs.send(p, "你不再拥有 VIP（%s）。"
                            .formatted(Privilege.of(t).displayname()));
                    Logs.info("Removal completed!");
                    Data.deleteDelivery(p.getName(), t);
                }
            }
        }
    }
}
