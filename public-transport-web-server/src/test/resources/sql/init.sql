INSERT INTO app_user (app_user_id, username, email, password, created_at, updated_at, account_non_expired, account_non_locked, credentials_non_expired, enabled)
VALUES (100, 'user', 'user@igeolab.pl', '$2a$10$yPoPJ9H4MAm5PP/.hIU/uebkqnFMiTGHb3/I.AMqpkDILHdwxi456', '2025-01-04 11:36:40.937926', '2025-01-04 11:36:40.937949', true, true, true, true);

INSERT INTO agency (agency_id, agency_code, agency_name, agency_url, agency_timetable_url, agency_phone, app_user_id)
VALUES (100, 'BESKIDBUS', 'BESKIDBUS POLSKA Spółka Komandytowa', 'beskidbus.pl', 'rozklad-jazdy', '781000000', 100);

insert into routeEntity (route_id, name, line, origin, destination, via, agency_id)
values (100, 'CHMIELNIK - PIERZCHNICA', '202', 'CHMIELNIK', 'PIERZCHNICA', '', 100);

INSERT INTO stop(bdot10k_id, name, lon, lat) VALUES ('10032', 'Morawica', 21.0507955, 52.1546729);
INSERT INTO stop(bdot10k_id, name, lon, lat) VALUES ('10033', 'Chmielnik', 23.6511807, 51.2666132);
