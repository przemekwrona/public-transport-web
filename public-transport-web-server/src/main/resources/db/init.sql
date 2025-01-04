INSERT INTO app_user (app_user_id, username, email, password, created_at, updated_at, account_non_expired, account_non_locked, credentials_non_expired, enabled)
VALUES (1, 'pwrona', 'pwrona@igeolab.pl', '$2a$10$yPoPJ9H4MAm5PP/.hIU/uebkqnFMiTGHb3/I.AMqpkDILHdwxi456', '2025-01-04 11:36:40.937926', '2025-01-04 11:36:40.937949', true, true, true, true);

INSERT INTO agency (agency_id, agency_code, agency_name, agency_url, agency_timetable_url, agency_phone, app_user_id)
VALUES (1, 'NEOBUS', 'NEOBUS POLSKA Czurczak Spółka Komandytowa', 'neobus.pl', 'rozklad-jazdy', '510038116', 1);
