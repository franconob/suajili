-- User trips table: Links users to their booked trips
CREATE TABLE IF NOT EXISTS user_trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- References auth.users(id) in Supabase
    trip_id VARCHAR(255) NOT NULL, -- References trip title/id from trips JSON
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, COMPLETED, CANCELLED
    departure_date DATE,
    return_date DATE,
    travelers INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2),
    currency VARCHAR(3),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for faster user lookups
CREATE INDEX IF NOT EXISTS idx_user_trips_user_id ON user_trips(user_id);

-- Index for finding upcoming trips
CREATE INDEX IF NOT EXISTS idx_user_trips_departure ON user_trips(departure_date);

-- Trip documents table: Stores documents related to user trips
CREATE TABLE IF NOT EXISTS trip_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_trip_id UUID NOT NULL REFERENCES user_trips(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL, -- TICKET, VOUCHER, ITINERARY, INSURANCE, OTHER
    storage_path VARCHAR(512) NOT NULL, -- Path to file in storage (e.g., Supabase Storage)
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Index for faster document lookups
CREATE INDEX IF NOT EXISTS idx_trip_documents_user_trip ON trip_documents(user_trip_id);

-- Trigger to update updated_at on user_trips
CREATE OR REPLACE FUNCTION update_user_trips_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_user_trips_updated_at ON user_trips;
CREATE TRIGGER trigger_user_trips_updated_at
    BEFORE UPDATE ON user_trips
    FOR EACH ROW
    EXECUTE FUNCTION update_user_trips_updated_at();

-- Sample data for testing (optional - comment out in production)
-- INSERT INTO user_trips (user_id, trip_id, status, departure_date, return_date, travelers, total_price, currency)
-- VALUES
--     ('00000000-0000-0000-0000-000000000001', 'Bariloche Clasico', 'CONFIRMED', '2026-03-15', '2026-03-22', 2, 450000.00, 'ARS'),
--     ('00000000-0000-0000-0000-000000000001', 'Cataratas del Iguazu', 'PENDING', '2026-06-10', '2026-06-14', 1, 280000.00, 'ARS');
