INSERT INTO permissions (name, description) VALUES
    ('user:read',   'Foydalanuvchilarni ko''rish'),
    ('user:create', 'Foydalanuvchi yaratish'),
    ('user:update', 'Foydalanuvchini  yangilash'),
    ('user:delete', 'Foydalanuvchini o''chirish'),

    ('role:read', 'Rolelarni ko''rish'),
    ('role:create', 'Rolelarni yaratish'),
    ('role:update', 'Roleni yangilash'),
    ('role:delete', 'Roleni o''chirish'),
    ('role:assign', 'Rolelarni biriktirish')
ON CONFLICT (name) WHERE deleted = FALSE DO NOTHING;

INSERT INTO roles (name, description, system_role) VALUES
    ('ADMIN', 'Tizim admini', true),
    ('USER', 'Oddiy user', true),
    ('MANAGER', 'Manager', false),
    ('OPERATOR', 'Oddiy operatorsanu', false)
ON CONFLICT (name) WHERE deleted = FALSE DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'MANAGER'
    AND p.name IN ('user:read', 'user:update', 'role:read');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'USER'
  AND p.name IN ('user:read');


INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('role:read', 'role:create', 'role:update', 'role:delete', 'role:assign',
                 'user:read', 'user:create', 'user:update', 'user:delete');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'OPERATOR'
  AND p.name IN ('user:read', 'user:update');