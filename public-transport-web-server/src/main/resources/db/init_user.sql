INSERT INTO app_role (app_role_id, description) VALUES ('SUPER_USER', 'Role for system owner');
INSERT INTO app_role (app_role_id, description) VALUES ('AGENCY_OWNER', 'Role for the owner of a transit agency');

INSERT INTO app_user (username, email, password, created_at, updated_at, account_non_expired, account_non_locked, credentials_non_expired, enabled)
VALUES ( 'pwrona', 'biuro@nastepnastacja.pl', '$2a$10$yPoPJ9H4MAm5PP/.hIU/uebkqnFMiTGHb3/I.AMqpkDILHdwxi456', now(), now(), true, true, true, true);

INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES ((SELECT app_user_id FROM app_user WHERE username = 'pwrona'), 'SUPER_USER');
INSERT INTO app_user_app_role(app_user_id, app_role_id) VALUES ((SELECT app_user_id FROM app_user WHERE username = 'pwrona'), 'AGENCY_OWNER');
