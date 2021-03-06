package info.xiancloud.plugin.message;

import info.xiancloud.plugin.Bean;
import info.xiancloud.plugin.LocalUnitsManager;
import info.xiancloud.plugin.Unit;
import info.xiancloud.plugin.distribution.GroupJudge;
import info.xiancloud.plugin.message.sender.IAsyncSender;
import info.xiancloud.plugin.message.sender.SenderFactory;
import info.xiancloud.plugin.message.sender.virtureunit.DefaultVirtualUnitConverter;
import info.xiancloud.plugin.message.sender.virtureunit.IVirtualUnitConverter;
import info.xiancloud.plugin.message.sender.virtureunit.VirtualDaoUnitConverter;
import info.xiancloud.plugin.support.mq.mqtt.handle.NotifyHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for calling unit locally or remotely.
 *
 * @author happyyangyuan
 */
public class Xian extends SyncXian {

    /**
     * @param destinationUnitBean the destination unit description bean. group name and unit name required.
     * @param map                 parameters
     * @param handler             call back handler
     */
    public static void call(Unit destinationUnitBean, Map<String, Object> map, NotifyHandler handler) {
        call(destinationUnitBean.getGroup().getName(), destinationUnitBean.getName(), map, handler);
    }

    /**
     * @param destinationUnitBean the destination unit description bean. group name and unit name required.
     * @param bean                parameter bean
     * @param handler             call back handler
     */
    public static void call(Unit destinationUnitBean, Bean bean, NotifyHandler handler) {
        call(destinationUnitBean.getGroup().getName(), destinationUnitBean.getName(), bean, handler);
    }

    public static void call(String group, String unit, Map<String, Object> map, NotifyHandler handler) {
        UnitRequest request = UnitRequest.create(group, unit).setArgMap(map);
        call(request, handler);
    }

    public static void call(String group, String unit, Bean bean, NotifyHandler handler) {
        call(group, unit, bean.toMap(), handler);
    }

    public static void call(Class<? extends Unit> unitClass, Map<String, Object> map, NotifyHandler handler) {
        call(LocalUnitsManager.getGroupByUnitClass(unitClass).getName(),
                LocalUnitsManager.getUnitByUnitClass(unitClass).getName(),
                map,
                handler);
    }

    public static void call(Class<? extends Unit> unitClass, Bean bean, NotifyHandler handler) {
        call(unitClass, bean.toMap(), handler);
    }

    /**
     * Tell the DAO layer to query data from the read-only database.
     */
    public static void readonly(String daoGroup, String daoUnit, Map<String, Object> map, NotifyHandler handler) {
        UnitRequest request = UnitRequest.create(daoGroup, daoUnit).setArgMap(map);
        request.getContext().setReadyOnly(true);
        call(request, handler);
    }

    public static void readonly(String daoGroup, String daoUnit, Bean bean, NotifyHandler handler) {
        readonly(daoGroup, daoUnit, bean.toMap(), handler);
    }

    /**
     * call the specified unit without parameters
     */
    public static void call(String group, String unit, NotifyHandler handler) {
        Xian.call(group, unit, new HashMap<>(), handler);
    }

    /**
     * call the specified unit without parameters
     */
    public static void call(Class<? extends Unit> unitClass, NotifyHandler handler) {
        call(unitClass, new HashMap<>(), handler);
    }

    public static void call(UnitRequest request, NotifyHandler handler) {
        String group = request.getContext().getGroup(),
                unit = request.getContext().getUnit();
        String concretedUnitName = getConverter(group).getConcreteUnit(group, unit, request.getArgMap());
        request.getContext().setUnit(concretedUnitName).setVirtualUnit(unit);
        IAsyncSender sender = SenderFactory.getSender(request, handler);
        sender.send();
    }

    static IVirtualUnitConverter getConverter(String group) {
        if (GroupJudge.defined(group) && GroupJudge.isDao(group)) {
            return VirtualDaoUnitConverter.singleton;
        }
        return DefaultVirtualUnitConverter.singleton;
    }

}
