package com.yaku.gateway.iam.interfaces.rest;

import com.yaku.gateway.iam.domain.model.commands.CreateFarmTokenCommand;
import com.yaku.gateway.iam.domain.services.FarmTokenCommandService;
import com.yaku.gateway.iam.interfaces.rest.resources.FarmTokenResource;
import com.yaku.gateway.iam.interfaces.rest.transform.FarmTokenResourceFromEntityAssembler;
import com.yaku.gateway.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/iam/farms/{farmId}/tokens")
public class FarmTokenController {

    private final FarmTokenCommandService farmTokenCommandService;

    public FarmTokenController(FarmTokenCommandService farmTokenCommandService) {
        this.farmTokenCommandService = farmTokenCommandService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FarmTokenResource> createFarmToken(@PathVariable Long farmId) {
        CreateFarmTokenCommand command = new CreateFarmTokenCommand(farmId);
        var farmToken = farmTokenCommandService.handle(command);
        if (farmToken.isEmpty()) return ResponseEntity.badRequest().build();
        var resource = FarmTokenResourceFromEntityAssembler.toResourceFromEntity(farmToken.get());
        return new ResponseEntity<>(resource, HttpStatus.CREATED);
    }
}
