-- name: CreateEvent :one
INSERT INTO events (id, name, description, location, start_time, end_time, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
RETURNING
id, name, description, location, start_time, end_time, created_at;

-- name: GetEvents :many
SELECT id, name, description, location, start_time, end_time, created_at
FROM events;