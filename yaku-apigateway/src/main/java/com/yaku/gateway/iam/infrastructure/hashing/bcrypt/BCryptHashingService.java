package com.yaku.gateway.iam.infrastructure.hashing.bcrypt;

import com.yaku.gateway.iam.application.internal.outboundservices.hashing.HashingService;
import org.springframework.security.crypto.password.PasswordEncoder;

public interface BCryptHashingService extends HashingService, PasswordEncoder {
}
