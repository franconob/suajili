-- ============================================
-- CATALOG: Group Trip Templates
-- ============================================

CREATE TABLE catalog_trips (
    id VARCHAR(255) PRIMARY KEY,  -- e.g., "910-dragones-de-asia"
    title VARCHAR(255) NOT NULL,
    image_url TEXT,
    duration_days INTEGER NOT NULL,
    route TEXT[] NOT NULL,  -- Array of destination names

    -- Pricing
    price_currency VARCHAR(3) DEFAULT 'USD',
    price_base DECIMAL(10, 2),
    price_single_supplement DECIMAL(10, 2),
    price_taxes DECIMAL(10, 2),

    -- Details (arrays)
    includes TEXT[],
    not_included TEXT[],
    requirements TEXT[],

    -- Metadata
    url TEXT,
    embedding_text TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Scheduled departures for group trips
CREATE TABLE catalog_trip_departures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    catalog_trip_id VARCHAR(255) NOT NULL REFERENCES catalog_trips(id) ON DELETE CASCADE,
    departure_date DATE NOT NULL,
    return_date DATE,
    max_participants INTEGER DEFAULT 20,
    current_participants INTEGER DEFAULT 0,
    status VARCHAR(32) DEFAULT 'open' CHECK (status IN ('open', 'closed', 'cancelled', 'completed')),
    created_at TIMESTAMPTZ DEFAULT NOW(),

    UNIQUE(catalog_trip_id, departure_date)
);

CREATE INDEX idx_departures_catalog_trip ON catalog_trip_departures(catalog_trip_id);
CREATE INDEX idx_departures_date ON catalog_trip_departures(departure_date);

-- ============================================
-- USER BOOKINGS
-- ============================================

-- Drop existing user_trips if you want to recreate (BE CAREFUL in production!)
-- DROP TABLE IF EXISTS trip_documents;
-- DROP TABLE IF EXISTS user_trips;

-- Updated user_trips table
CREATE TABLE user_trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,  -- References Supabase auth.users

    -- Trip type discriminator
    type VARCHAR(32) NOT NULL DEFAULT 'group' CHECK (type IN ('group', 'custom')),

    -- For group trips: reference to catalog
    catalog_trip_id VARCHAR(255) REFERENCES catalog_trips(id) ON DELETE SET NULL,
    departure_id UUID REFERENCES catalog_trip_departures(id) ON DELETE SET NULL,

    -- For custom trips (or override for group)
    title VARCHAR(255),

    -- Booking details
    status VARCHAR(32) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'confirmed', 'paid', 'completed', 'cancelled')),
    departure_date DATE,
    return_date DATE,

    -- Pricing
    total_price DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'USD',

    -- Notes
    notes TEXT,

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_user_trips_user ON user_trips(user_id);
CREATE INDEX idx_user_trips_status ON user_trips(status);
CREATE INDEX idx_user_trips_type ON user_trips(type);

-- ============================================
-- CUSTOM TRIP DESTINATIONS
-- ============================================

CREATE TABLE user_trip_destinations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_trip_id UUID NOT NULL REFERENCES user_trips(id) ON DELETE CASCADE,
    destination_name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    sequence INTEGER NOT NULL,  -- Order in itinerary (1, 2, 3...)
    start_date DATE,
    end_date DATE,
    nights INTEGER,
    accommodation TEXT,  -- Hotel name or description
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_destinations_trip ON user_trip_destinations(user_trip_id);

-- ============================================
-- TRAVELERS
-- ============================================

CREATE TABLE user_trip_travelers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_trip_id UUID NOT NULL REFERENCES user_trips(id) ON DELETE CASCADE,

    -- Personal info
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    birth_date DATE,
    nationality VARCHAR(100),

    -- Document
    document_type VARCHAR(32) CHECK (document_type IN ('passport', 'dni', 'other')),
    document_number VARCHAR(100),
    document_expiry DATE,

    -- Role
    is_primary BOOLEAN DEFAULT FALSE,  -- Main contact/booker

    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_travelers_trip ON user_trip_travelers(user_trip_id);

-- ============================================
-- DOCUMENTS (updated from existing)
-- ============================================

CREATE TABLE trip_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_trip_id UUID NOT NULL REFERENCES user_trips(id) ON DELETE CASCADE,

    name VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL CHECK (type IN ('ticket', 'voucher', 'itinerary', 'insurance', 'passport', 'visa', 'other')),

    -- Storage (Supabase Storage)
    storage_path TEXT NOT NULL,
    file_size INTEGER,
    mime_type VARCHAR(100),

    -- Optional: link to specific traveler
    traveler_id UUID REFERENCES user_trip_travelers(id) ON DELETE SET NULL,

    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_documents_trip ON trip_documents(user_trip_id);
CREATE INDEX idx_documents_traveler ON trip_documents(traveler_id);

-- ============================================
-- ROW LEVEL SECURITY (Supabase)
-- ============================================

ALTER TABLE user_trips ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_trip_destinations ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_trip_travelers ENABLE ROW LEVEL SECURITY;
ALTER TABLE trip_documents ENABLE ROW LEVEL SECURITY;

-- Users can only see their own trips
CREATE POLICY "Users can view own trips" ON user_trips
    FOR SELECT USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own trips" ON user_trips
    FOR INSERT WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own trips" ON user_trips
    FOR UPDATE USING (auth.uid() = user_id);

-- Destinations: access through trip ownership
CREATE POLICY "Users can view own trip destinations" ON user_trip_destinations
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

CREATE POLICY "Users can manage own trip destinations" ON user_trip_destinations
    FOR ALL USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

-- Travelers: access through trip ownership
CREATE POLICY "Users can view own trip travelers" ON user_trip_travelers
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

CREATE POLICY "Users can manage own trip travelers" ON user_trip_travelers
    FOR ALL USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

-- Documents: access through trip ownership
CREATE POLICY "Users can view own documents" ON trip_documents
    FOR SELECT USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

CREATE POLICY "Users can manage own documents" ON trip_documents
    FOR ALL USING (
        EXISTS (SELECT 1 FROM user_trips WHERE id = user_trip_id AND user_id = auth.uid())
    );

-- Catalog is public read
ALTER TABLE catalog_trips ENABLE ROW LEVEL SECURITY;
ALTER TABLE catalog_trip_departures ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Catalog trips are public" ON catalog_trips
    FOR SELECT USING (true);

CREATE POLICY "Catalog departures are public" ON catalog_trip_departures
    FOR SELECT USING (true);

-- ============================================
-- HELPER FUNCTIONS
-- ============================================

-- Auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER user_trips_updated_at
    BEFORE UPDATE ON user_trips
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER travelers_updated_at
    BEFORE UPDATE ON user_trip_travelers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER catalog_trips_updated_at
    BEFORE UPDATE ON catalog_trips
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
