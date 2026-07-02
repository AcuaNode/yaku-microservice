package io.github.rafaviv.yakubackend.equipment.infrastructure.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.rafaviv.yakubackend.equipment.domain.model.events.FishFarmerAssignedEvent;

@Component
public class FishFarmerAssignedEventListener {

    @EventListener
    public void onFishFarmerAssigned(FishFarmerAssignedEvent event) {
        // Logic to handle when a fish farmer is assigned to a pond
        System.out.println("Fish farmer " + event.fishFarmerId() + " assigned to pond " + event.pondId());
    }
}