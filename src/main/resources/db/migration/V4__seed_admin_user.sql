INSERT INTO users (email, password, active) VALUES
      ('admin@app.co', '{bcrypt}$2y$10$l4zpMzgWXbgIFLBL7QnPm.JHPpY0GM2QaAVoHAix4/XP.dGMHb0Wy%', true),
      ('user@app.co', '{bcrypt}$2y$10$l4zpMzgWXbgIFLBL7QnPm.JHPpY0GM2QaAVoHAix4/XP.dGMHb0Wy%', true),
      ('manager@app.co', '{bcrypt}$2y$10$l4zpMzgWXbgIFLBL7QnPm.JHPpY0GM2QaAVoHAix4/XP.dGMHb0Wy%', true),
      ('operator@app.co', '{bcrypt}$2y$10$l4zpMzgWXbgIFLBL7QnPm.JHPpY0GM2QaAVoHAix4/XP.dGMHb0Wy%', true);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@app.co'
  AND r.name = 'ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'user@app.co'
  AND r.name = 'USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'manager@app.co'
  AND r.name = 'MANAGER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'operator@app.co'
  AND r.name = 'OPERATOR';