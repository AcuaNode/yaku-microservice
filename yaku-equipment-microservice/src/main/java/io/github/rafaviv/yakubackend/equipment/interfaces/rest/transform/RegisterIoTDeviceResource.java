package io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform;

public record RegisterIoTDeviceResource(String deviceId, String deviceName, Long farmId) {}
