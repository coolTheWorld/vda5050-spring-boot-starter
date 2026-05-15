package com.navasmart.vda5050.listener;

import com.navasmart.vda5050.autoconfigure.Vda5050Properties;
import com.navasmart.vda5050.event.vehicle.VehicleRegistryEvent;
import com.navasmart.vda5050.event.vehicle.VehicleUnRegistryEvent;
import com.navasmart.vda5050.mqtt.MqttConnectionManager;
import com.navasmart.vda5050.vehicle.VehicleContext;
import com.navasmart.vda5050.vehicle.VehicleRegistry;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

/**
 * 监听外部发布的 {@link VehicleRegistryEvent}，编排 {@link VehicleRegistry} 与 {@link MqttConnectionManager}。
 */
public class VehicleRegistryListener {

    private static final Logger log = LoggerFactory.getLogger(VehicleRegistryListener.class);

    private final VehicleRegistry vehicleRegistry;
    private final MqttConnectionManager mqttConnectionManager;
    private final Vda5050Properties properties;

    public VehicleRegistryListener(VehicleRegistry vehicleRegistry,
                                   MqttConnectionManager mqttConnectionManager,
                                   Vda5050Properties properties) {
        this.vehicleRegistry = vehicleRegistry;
        this.mqttConnectionManager = mqttConnectionManager;
        this.properties = properties;
    }

    @EventListener
    public void onVehicleRegistry(VehicleRegistryEvent registryEvent) throws MqttException {

        String manufacturer = registryEvent.getManufacturer();
        String serialNumber = registryEvent.getSerialNumber();
        boolean serverMode = registryEvent.isServerMode();
        boolean proxyMode = registryEvent.isProxyMode();
        VehicleContext ctx;
        //车辆注册
        ctx = vehicleRegistry.registerVehicle(manufacturer, serialNumber, proxyMode, serverMode);
        if (properties.getServer().isEnabled() && serverMode) {
            mqttConnectionManager.subscribeServerVehicle(ctx);
        }

        if (properties.getProxy().isEnabled() && proxyMode) {
            mqttConnectionManager.connectProxyVehicle(ctx);
        }
    }

    @EventListener
    public void onVehicleUnRegistry(VehicleUnRegistryEvent unRegistryEvent) throws MqttException {
        String manufacturer = unRegistryEvent.getManufacturer();
        String serialNumber = unRegistryEvent.getSerialNumber();
        VehicleContext ctx;
        //车辆注销
        ctx = vehicleRegistry.unregisterVehicle(manufacturer, serialNumber);
        if (ctx == null) {
            log.warn("Vehicle Un-registered for null vehicle,skipping");
            return;
        }
        if (ctx.isServerMode()) {
            mqttConnectionManager.unsubscribeServerVehicle(ctx);
        }
        if (ctx.isProxyMode()) {
            mqttConnectionManager.disconnectProxyVehicle(ctx);
        }
    }
}
