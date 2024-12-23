-- name: CreateUser :one
insert into users (id, name, email, address, created_at)
VALUES ($1, $2, $3, $4, $5)
RETURNING
id, name, email, address, created_at;

-- name: GetAllUsers :many
SELECT id, name, email, address, created_at
FROM users;