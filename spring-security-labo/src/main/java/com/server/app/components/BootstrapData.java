package com.server.app.components;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.server.app.entities.Permission;
import com.server.app.entities.Role;
import com.server.app.repositories.PermissionRepository;
import com.server.app.repositories.RoleRepository;

@Component
@Order(2)
public class BootstrapData implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final SaveEndpoints saveEndpoints;

    public BootstrapData(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            SaveEndpoints saveEndpoints
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.saveEndpoints = saveEndpoints;
    }

    @Override
    public void run(ApplicationArguments args) {
        saveEndpoints.sync();

        Role admin = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").permissions(new HashSet<>()).build()));
        Role user = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").permissions(new HashSet<>()).build()));

        Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());

        admin.setPermissions(allPermissions);
        roleRepository.save(admin);

        Set<Permission> userPermissions = allPermissions.stream()
                .filter(permission -> "GET".equalsIgnoreCase(permission.getMethod()))
                .collect(Collectors.toCollection(HashSet::new));

        allPermissions.stream()
                .filter(permission -> permission.getPath() != null
                        && permission.getPath().startsWith("/api/auth/")
                        && "PUT".equalsIgnoreCase(permission.getMethod()))
                .forEach(userPermissions::add);

        allPermissions.stream()
                .filter(permission -> permission.getPath() != null
                        && permission.getPath().startsWith("/api/finanzas/")
                        && (
                        "GET".equalsIgnoreCase(permission.getMethod())
                                || "POST".equalsIgnoreCase(permission.getMethod())
                ))
                .forEach(userPermissions::add);

        user.setPermissions(userPermissions);
        roleRepository.save(user);
    }
}