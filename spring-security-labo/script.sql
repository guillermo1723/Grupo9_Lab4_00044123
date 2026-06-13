INSERT INTO roles (name) VALUES
('ADMIN'),
('USER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'USER' AND p.method = 'GET'
ON CONFLICT DO NOTHING;

insert into role_permissions(permission_id, role_id) values (13, 2); --ejemplo para cambiar el rol a un usuario

select * from role_permissions;
select * from permissions;
