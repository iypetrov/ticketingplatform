-- name: CreateTicket :one
insert into tickets (id, user_id, event_id, purchase_date, price, status, created_at, updated_at)
VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
RETURNING
id, user_id, event_id, purchase_date, price, status, created_at, updated_at;

-- name: GetTickets :many
SELECT id, user_id, event_id, purchase_date, price, status, created_at, updated_at
FROM tickets
WHERE event_id = $1;

-- name: UpdateTicket :one
UPDATE tickets
SET user_id = $2,purchase_date = $3,status = $4, updated_at=$5
WHERE id = $1
RETURNING
id, user_id, event_id, purchase_date, price, status, created_at, updated_at;

-- name: GetTicketById :one
SELECT id, user_id, event_id, purchase_date, price, status, created_at, updated_at
FROM tickets
WHERE id = $1;
