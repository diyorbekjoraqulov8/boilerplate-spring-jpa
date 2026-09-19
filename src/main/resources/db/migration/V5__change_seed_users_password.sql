UPDATE users
SET password = '{bcrypt}$2y$10$4XbuRhS2062QsL0obXhMZ.IoX9Bytt/7Z5titUkczDgkFIud6s5h2'
WHERE email IN ('admin@app.co', 'user@app.co', 'manager@app.co', 'operator@app.co');