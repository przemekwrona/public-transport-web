CREATE SCHEMA IF NOT EXISTS public_transport;

DO $$
BEGIN
    EXECUTE format(
        'ALTER DATABASE %I SET search_path TO public_transport',
        current_database()
    );
END
$$;
