DELETE FROM role_permissions
WHERE role_id = (SELECT id FROM roles WHERE name = 'USER')
  AND permission_id = (SELECT id FROM permissions WHERE name = 'user:read');