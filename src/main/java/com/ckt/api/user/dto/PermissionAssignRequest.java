package com.ckt.api.user.dto;

import java.util.List;

public class PermissionAssignRequest {
    private List<Long> permissionIds;

    public PermissionAssignRequest() {}

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
