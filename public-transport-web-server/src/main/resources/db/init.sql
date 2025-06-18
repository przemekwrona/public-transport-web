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
VALUES (1, 'Chmielnik - Kije', 'L1', true, false, null, '142902', 'Chmielnik', '145543', 'Kije', null, 1);

INSERT INTO trip (trip_id, variant_name, mode, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin_stop_id, origin_stop_name, destination_stop_id, destination_stop_name, route_id)
VALUES (1, 'MAIN', 'FRONT', 'Kije', null, null, 27, 13382, 1781, true, null, 'Chmielnik', null, 'Kije', 1);

INSERT INTO trip (trip_id, variant_name, mode, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin_stop_id, origin_stop_name, destination_stop_id, destination_stop_name, route_id)
VALUES (2, 'MAIN', 'BACK', 'Chmielnik', null, null, 27, 13417, 1784, true, null, 'Kije', null, 'Chmielnik', 1);

INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 1, 0, 0, 0, 145543);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 2, 17, 17, 129, 145417);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 3, 152, 152, 1146, 145416);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 4, 268, 268, 2018, 145642);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 5, 384, 384, 2894, 145449);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 6, 530, 530, 3990, 145437);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 7, 607, 607, 4574, 145419);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 8, 737, 737, 5553, 145653);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 9, 851, 851, 6412, 145582);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 10, 1148, 1148, 8640, 142804);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 11, 1323, 1323, 9953, 141951);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 12, 1525, 1525, 11472, 200981);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (2, 13, 1784, 1784, 13417, 141875);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 1, 0, 0, 0, 142902);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 2, 278, 278, 2087, 234100);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 3, 480, 480, 3609, 142201);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 4, 661, 661, 4969, 143073);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 5, 946, 946, 7109, 145581);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 6, 1058, 1058, 7953, 145418);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 7, 1184, 1184, 8898, 145434);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 8, 1276, 1276, 9588, 145643);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 9, 1411, 1411, 10605, 145448);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 10, 1527, 1527, 11475, 145638);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 11, 1651, 1651, 12407, 145528);
INSERT INTO stop_time (trip_id, stop_sequence, arrival_second, departure_second, distance_meters, stop_id) VALUES (1, 12, 1781, 1781, 13384, 145543);

INSERT INTO calendar (service_id, agency_id, calendar_name, designation, description, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES (1, 1, 'D/20250101/20251231/1', 'D', 'kursuje od poniedziałku do piątku oprócz świąt', true, true, true, true, true, false, false, '2025-01-01', '2025-12-31');

INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-11-11', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-24', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-26', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-05-01', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-01-06', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-12-25', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-01-01', 'REMOVED');
INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (1, '2025-05-02', 'REMOVED');

INSERT INTO brigade (brigade_id, brigade_number, agency_id, service_id) VALUES (1, 'DP/KIJE-CHMIELNIK/1', 1, 1);

INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/1', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 1, 2, 19200, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/2', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 2, 1, 21300, null, null, null, null, null, 1781, null, 'Chmielnik', 'KIJE', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/3', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 3, 2, 25200, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/4', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 4, 1, 27000, null, null, null, null, null, 1781, null, 'Chmielnik', 'KIJE', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/5', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 5, 2, 30600, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/6', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 6, 1, 32400, null, null, null, null, null, 1781, null, 'Chmielnik', 'KIJE', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/7', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 7, 2, 34200, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/8', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 8, 1, 36000, null, null, null, null, null, 1781, null, 'Chmielnik', 'Kije', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/9', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 9, 2, 54300, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/10', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 10, 1, 56100, null, null, null, null, null, 1781, null, 'Chmielnik', 'Kije', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/11', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 11, 2, 57600, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/12', 'L1', 'Chmielnik - Kije', 'MAIN', 'FRONT', 12, 1, 59400, null, null, null, null, null, 1781, null, 'Chmielnik', 'Kije', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/13', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 13, 2, 61200, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
INSERT INTO brigade_trip (brigade_trip_id, line, name, variant, mode, trip_sequence, root_trip_id, departure_time_in_second, headsign, variant_designation, variant_description, communication_velocity, distance_in_meters, travel_time_in_seconds, is_main_variant, origin, destination, brigade_id) VALUES ('NEOBUS/DP/KIJE-CHMIELNIK/1/14', 'L1', 'Chmielnik - Kije', 'MAIN', 'BACK', 14, 2, 63000, null, null, null, null, null, 1784, null, 'KIJE', 'Chmielnik', 1);
