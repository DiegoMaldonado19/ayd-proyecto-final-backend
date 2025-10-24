package com.ayd.parkcontrol.domain.exception;

public class RoleNotFoundException extends BusinessRuleException {

    public RoleNotFoundException(Integer roleId) {
        super("El rol con ID '" + roleId + "' no existe en el sistema");
    }

    public RoleNotFoundException(String roleName) {
        super("El rol '" + roleName + "' no existe en el sistema");
    }
}
