package com.yaku.gateway.iam.interfaces.rest.transform;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.interfaces.rest.resources.FarmTokenResource;

public class FarmTokenResourceFromEntityAssembler {
    public static FarmTokenResource toResourceFromEntity(FarmToken entity) {
        return new FarmTokenResource(entity.getId(), entity.getToken(), entity.getFarmId(), entity.isUsed());
    }
}
