-- Make catalog_trip_id nullable to support custom trips without a catalog reference
ALTER TABLE user_trips ALTER COLUMN catalog_trip_id DROP NOT NULL;
