--liquibase formatted sql

--changeset hubofallthings:applications context:structuresonly

CREATE SEQUENCE hat.application_seq;

CREATE TABLE hat.applications (
  application_id  INTEGER   NOT NULL DEFAULT nextval('hat.application_seq') PRIMARY KEY,
  date_created    TIMESTAMP NOT NULL DEFAULT (NOW()),
  date_setup      TIMESTAMP,
  title           VARCHAR   NOT NULL,
  description     VARCHAR   NOT NULL,
  logo_url        VARCHAR   NOT NULL,
  url             VARCHAR   NOT NULL,
  auth_url        VARCHAR   NOT NULL,
  browser         BOOLEAN   NOT NULL,
  category        VARCHAR   NOT NULL,
  setup           BOOLEAN   NOT NULL,
  login_available BOOLEAN   NOT NULL
);

--rollback DROP TABLE hat.applications;
--rollback DROP SEQUENCE hat.application_seq;

--changeset hubofallthings:presetApplications context:data,testdata

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('MarketSquare', 'Community and Public space for HATs', '/assets/images/MarketSquare-logo.svg',
        'https://marketsquare.hubofallthings.com', '/authenticate/hat', FALSE, 'app', TRUE, TRUE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Rumpel', 'Private hyperdata browser for your HAT data', '/assets/images/Rumpel-logo.svg',
        'https://rumpel.hubofallthings.com', '/users/authenticate', TRUE, 'app', TRUE, TRUE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Hatters', 'HATs, Apps and HAT2HAT exchanges', '/assets/images/Hatters-logo.svg',
        'https://hatters.hubofallthings.com', '/authenticate/hat', FALSE, 'app', TRUE, TRUE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Rumpel', 'Private hyperdata browser for your HAT data', '/assets/images/Rumpel-logo.svg',
        'http://rumpel-stage.hubofallthings.com.s3-website-eu-west-1.amazonaws.com', '/users/authenticate', TRUE,
        'testapp', TRUE, TRUE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Facebook', 'Pull in all of your Facebook Data', 'https://rumpel.hubofallthings.com/icons/facebook-plug.png',
        'https://social-plug.hubofallthings.com', '/hat/authenticate', FALSE, 'dataplug', TRUE, TRUE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Calendar', 'Paste an iCal link to any of your calendars for it to be added to your HAT',
        'https://rumpel.hubofallthings.com/icons/calendar-plug.svg', 'https://calendar-plug.hubofallthings.com', '',
        FALSE, 'dataplug', TRUE, FALSE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('Photos', 'Import your best moments from Dropbox into your HAT',
        'https://rumpel.hubofallthings.com/icons/photos-plug.svg', 'https://photos-plug.hubofallthings.com', '', FALSE,
        'dataplug', TRUE, FALSE);

INSERT INTO hat.applications (title, description, logo_url, url, auth_url, browser, category, setup, login_available)
VALUES ('RumpelLite', 'Your location coming in directly from your iOS device into your HAT!',
        'https://rumpel.hubofallthings.com/icons/location-plug.svg', 'https://itunes.apple.com',
        '/gb/app/rumpel-lite/id1147137249', FALSE, 'dataplug', TRUE, FALSE);

--rollback DELETE FROM hat.applications WHERE title IN ('MarketSquare', 'Rumpel', 'Hatters', 'Facebook', 'Calendar', 'Photos', 'RumpelLite');
