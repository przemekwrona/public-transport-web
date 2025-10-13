INSERT INTO app_role (app_role_id, description) VALUES ('SUPER_USER', 'Role for system owner');
INSERT INTO app_role (app_role_id, description) VALUES ('AGENCY_OWNER', 'Role for the owner of a transit agency');

INSERT INTO app_user (username, email, password, created_at, updated_at, account_non_expired, account_non_locked, credentials_non_expired, enabled)
VALUES ( 'pwrona', 'biuro@nastepnastacja.pl', '$2a$10$yPoPJ9H4MAm5PP/.hIU/uebkqnFMiTGHb3/I.AMqpkDILHdwxi456', now(), now(), true, true, true, true);

INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES ((SELECT app_user_id FROM app_user WHERE username = 'pwrona'), 'SUPER_USER');
INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES ((SELECT app_user_id FROM app_user WHERE username = 'pwrona'), 'AGENCY_OWNER');

INSERT INTO agency(agency_code, agency_name, agency_url, agency_timetable_url, street, house_number, flat_number, postal_code, postal_city, latitude, longitude, created_at, updated_at, agency_owner_id)
VALUES ('NEXTSTOP', 'Następna Stacja', 'nastepnastacja.pl','','Nakielska', '5', '', '01106', 'WARSZAWA', 52.232512, 20.935926,now(), now(), (SELECT app_user_id FROM app_user WHERE username = 'pwrona'));

INSERT INTO app_user_agency(app_user_id, agency_id) VALUES ((SELECT app_user_id FROM app_user WHERE username = 'pwrona'), (SELECT agency_id FROM agency WHERE agency_code = 'NEXTSTOP'));
