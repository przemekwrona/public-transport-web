INSERT INTO app_role (app_role_id, description) VALUES ('SUPER_USER', 'Role for system owner');
INSERT INTO app_role (app_role_id, description) VALUES ('AGENCY_OWNER', 'Role for the owner of a transit agency');

INSERT INTO app_user (app_user_id, username, email, password, created_at, updated_at, account_non_expired, account_non_locked, credentials_non_expired, enabled)
VALUES (1, 'pwrona', 'pwrona@igeolab.pl', '$2a$10$yPoPJ9H4MAm5PP/.hIU/uebkqnFMiTGHb3/I.AMqpkDILHdwxi456', '2025-01-04 11:36:40.937926', '2025-01-04 11:36:40.937949', true, true, true, true);

INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES (1, 'SUPER_USER');
INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES (1, 'AGENCY_OWNER');

INSERT INTO agency (agency_id, agency_code, agency_name, agency_url, agency_timetable_url, agency_phone, agency_owner_id)
VALUES (1, 'NEOBUS', 'NEOBUS POLSKA Czurczak Spółka Komandytowa', 'neobus.pl', 'rozklad-jazdy', '510038116', 1);

INSERT INTO app_user_agency(app_user_id, agency_id)
VALUES (1, 1);

INSERT INTO route (route_id, name, line, active, google, description, origin_stop_id, origin_stop_name, destination_stop_id, destination_stop_name, via, agency_id)
VALUES (1, 'Chmielnik - Kije', 'L1', true, false, null, '142902', 'Chmielnik', '145543', 'KIJE  / 0157 (kościół)  / 02', null, 1);

INSERT INTO trip (trip_id, variant_name, mode, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin_stop_id, origin_stop_name, destination_stop_id, destination_stop_name, route_id)
VALUES (1, 'MAIN', 'FRONT', 'KIJE', null, null, 45, 13382, 1065, true, null, 'Chmielnik', null, 'KIJE', 1);

INSERT INTO trip (trip_id, variant_name, mode, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin_stop_id, origin_stop_name, destination_stop_id, destination_stop_name, route_id)
VALUES (2, 'MAIN', 'BACK', 'Chmielnik', null, null, 45, 13417, 1068, true, null, 'KIJE', null, 'Chmielnik', 1);

INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 1, 0, 0, 0, 142902);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 2, 166, 166, 2087, 278666);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 3, 287, 287, 3609, 142201);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 4, 395, 395, 4969, 143073);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 5, 566, 566, 7109, 145581);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 6, 633, 633, 7953, 145418);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 7, 708, 708, 8897, 278661);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 8, 763, 763, 9587, 278663);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 9, 844, 844, 10603, 278640);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 10, 913, 913, 11474, 278642);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 11, 987, 987, 12405, 145528);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 12, 1065, 1065, 13382, 145543);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 1, 0, 0, 0, 145543);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 2, 10, 10, 129, 145417);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 3, 91, 91, 1146, 145416);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 4, 160, 160, 2018, 145642);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 5, 230, 230, 2894, 145449);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 6, 317, 317, 3990, 145437);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 7, 363, 363, 4573, 278660);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 8, 441, 441, 5553, 145653);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 9, 509, 509, 6412, 145582);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 10, 687, 687, 8640, 142804);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 11, 792, 792, 9953, 141951);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 12, 913, 913, 11472, 278665);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 13, 1068, 1068, 13417, 141875);

INSERT INTO calendar (service_id, agency_id, calendar_name, designation, description, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES (1, 1, 'D/20250101/20251231/1', 'D', 'kursuje od poniedziałku do piątku oprócz świąt', true, true, true, true, true, false, false, '2025-01-01', '2025-12-31');

INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-11-11', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-24', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-26', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-05-01', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-01-06', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-25', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-01-01', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-05-02', 'REMOVED');
