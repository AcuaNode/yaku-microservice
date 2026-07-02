package com.yaku.gateway.iam.interfaces.rest.transform;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.interfaces.rest.resources.FarmTokenResource;

public class FarmTokenResourceFromEntityAssembler {
    public static FarmTokenResource toResourceFromEntity(FarmToken farmToken) {
        return new FarmTokenResource(
                farmToken.getId(),
                farmToken.getToken(),
                farmToken.getFarmId(),
                farmToken.isUsed()
        );
    }
}