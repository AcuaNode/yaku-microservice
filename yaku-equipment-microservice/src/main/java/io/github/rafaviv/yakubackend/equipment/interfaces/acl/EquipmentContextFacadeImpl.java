package io.github.rafaviv.yakubackend.equipment.interfaces.acl;

import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import org.springframework.stereotype.Service;
@Service
public class EquipmentContextFacadeImpl implements EquipmentContextFacade {

    private final FarmRepository farmRepository;

    public EquipmentContextFacadeImpl(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }
}