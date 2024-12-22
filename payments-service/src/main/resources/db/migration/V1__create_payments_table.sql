CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    ticket_id UUID NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL,
    finished_at TIMESTAMP NOT NULL
);