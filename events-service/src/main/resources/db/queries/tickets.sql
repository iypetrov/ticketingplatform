-- name: CreateTicket :one
insert into tickets (id, user_id, event_id, purchase_date, price, status, created_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
RETURNING
id, user_id, event_id, purchase_date, price, status, created_at;
