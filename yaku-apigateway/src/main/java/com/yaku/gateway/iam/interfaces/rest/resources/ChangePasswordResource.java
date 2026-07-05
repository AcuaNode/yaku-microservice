package com.yaku.gateway.iam.interfaces.rest.resources;

public record ChangePasswordResource(String currentPassword, String newPassword) {
}
