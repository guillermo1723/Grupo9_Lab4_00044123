package com.server.app.components;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.server.app.entities.Permission;
import com.server.app.repositories.PermissionRepository;

@Component
public class SaveEndpoints {

    private final RequestMappingHandlerMapping handlerMapping;
    private final PermissionRepository permissionRepository;

    public SaveEndpoints(RequestMappingHandlerMapping handlerMapping, PermissionRepository permissionRepository) {
        this.handlerMapping = handlerMapping;
        this.permissionRepository = permissionRepository;
    }

    public void sync() {
        handlerMapping.getHandlerMethods().forEach(this::saveMapping);
    }

    private void saveMapping(RequestMappingInfo info, HandlerMethod handlerMethod) {
        if (!handlerMethod.getBeanType().getPackageName().startsWith("com.server.app.controllers")) {
            return;
        }

        Set<String> paths = new LinkedHashSet<>(info.getPatternValues());
        Set<String> methods = info.getMethodsCondition().getMethods().stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (paths.isEmpty() || methods.isEmpty()) {
            return;
        }

        for (String path : paths) {
            if (!path.startsWith("/api/")) {
                continue;
            }

            if ("/api/auth/login".equals(path) || "/api/auth/signup".equals(path)) {
                continue;
            }

            for (String method : methods) {
                Permission permission = new Permission();
                permission.setPath(path);
                permission.setMethod(method);

                permissionRepository.findByPathAndMethod(path, method)
                        .orElseGet(() -> permissionRepository.save(permission));
            }
        }
    }
}