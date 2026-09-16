INSERT INTO permissions (name, description) VALUES
    ('user:read',   'Foydalanuvchilarni ko''rish'),
    ('user:create', 'Foydalanuvchi yaratish')
ON CONFLICT (name) DO NOTHING;