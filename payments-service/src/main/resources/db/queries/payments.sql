-- name: InitPayment :one
INSERT INTO payments (id, user_id, ticket_id, provider_id, price, created_at, finished_at)
VALUES ($1, $2, $3, $4, $5, $6, $7)
RETURNING id, user_id, ticket_id, provider_id, price, created_at, finished_at;

-- name: FinalizePayment :one
UPDATE payments
SET finished_at = $2
WHERE provider_id = $1
RETURNING id, user_id, ticket_id, provider_id, price, created_at, finished_at;

-- name: GetPayments :many
SELECT id, user_id, ticket_id, provider_id, price, created_at, finished_at
FROM payments;
