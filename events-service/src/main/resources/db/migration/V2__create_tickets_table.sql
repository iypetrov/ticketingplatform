CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    user_id UUID,
    event_id UUID NOT NULL,
    purchase_date TIMESTAMP ,
    price DOUBLE PRECISION NOT NULL,
    status TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
