package com.navasmart.vda5050.event.vehicle;

import org.springframework.context.ApplicationEvent;

/**
 * 外部发布的车辆取消注册事件。
 */
public class VehicleUnRegistryEvent extends ApplicationEvent {

    private final String manufacturer;
    private final String serialNumber;


    public VehicleUnRegistryEvent(Object source, String manufacturer, String serialNumber) {
        super(source);

        this.manufacturer = manufacturer;
        this.serialNumber = serialNumber;

    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

}
