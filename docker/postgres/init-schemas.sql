CREATE SCHEMA IF NOT EXISTS public_transport;
CREATE SCHEMA IF NOT EXISTS keycloak;
CREATE SCHEMA IF NOT EXISTS traccar;

-- Existing installs keep application tables in public. Move them once.
DO $$
DECLARE
  obj record;
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'databasechangelog'
  ) AND NOT EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public_transport' AND table_name = 'databasechangelog'
  ) THEN
    FOR obj IN
      SELECT c.relkind, c.relname
      FROM pg_class c
      JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'public'
        AND c.relkind IN ('r', 'p', 'v', 'm', 'S')
        AND NOT EXISTS (
          SELECT 1 FROM pg_depend d
          WHERE d.objid = c.oid AND d.deptype = 'e'
        )
      ORDER BY CASE c.relkind WHEN 'v' THEN 1 WHEN 'm' THEN 1 ELSE 0 END, c.relname
    LOOP
      IF obj.relkind = 'S' THEN
        EXECUTE format('ALTER SEQUENCE public.%I SET SCHEMA public_transport', obj.relname);
      ELSIF obj.relkind = 'v' THEN
        EXECUTE format('ALTER VIEW public.%I SET SCHEMA public_transport', obj.relname);
      ELSIF obj.relkind = 'm' THEN
        EXECUTE format('ALTER MATERIALIZED VIEW public.%I SET SCHEMA public_transport', obj.relname);
      ELSE
        EXECUTE format('ALTER TABLE public.%I SET SCHEMA public_transport', obj.relname);
      END IF;
    END LOOP;
  END IF;
END $$;
