-- name: CreateEvent :one
insert into events (id, name, description, location, start_time, end_time, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
RETURNING
id, name, description, location, start_time, end_time, created_at;
