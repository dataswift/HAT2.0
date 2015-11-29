--
-- Data for Name: data_table; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_table
VALUES (10, now(), now(), 'HyperDataBrowser', 'HyperDataBrowser');
INSERT INTO data_table
VALUES (11, now(), now(), 'Collections', 'HyperDataBrowser');
INSERT INTO data_table VALUES (12, now(), now(), 'Entities', 'HyperDataBrowser');

--
-- Data for Name: data_field; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_field VALUES (10, now(), now(), 'name', 11);
INSERT INTO data_field VALUES (11, now(), now(), 'kinds', 11);
INSERT INTO data_field VALUES (12, now(), now(), 'collection_id', 11);
INSERT INTO data_field VALUES (13, now(), now(), 'kind', 12);
INSERT INTO data_field VALUES (14, '2015-10-29 15:41:29', '2015-10-29 15:41:29', 'name', 12);
INSERT INTO data_field VALUES (15, now(), now(), 'entity_id', 12);
INSERT INTO data_field VALUES (16, now(), now(), 'collection_id', 12);

--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record VALUES (3, now(), now(), 'Contacts');
INSERT INTO data_record
VALUES (4, now(), now(), 'Collection Entity Me (Personal Info)');
INSERT INTO data_record
VALUES (5, now(), now(), 'Collection Entity My Location (Personal Info)');
INSERT INTO data_record
VALUES (6, now(), now(), 'Collection Entity Phone (Personal Info)');
INSERT INTO data_record
VALUES (7, now(), now(), 'Collection Entity Spouse (Contacts)');
INSERT INTO data_record VALUES (129, now(), now(), 'Me');
INSERT INTO data_record
VALUES (8, now(), now(), 'Collection Home[person,location,event,thing]');

--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_tabletotablecrossref
VALUES (9, now(), now(), 'parent child', 10, 11);
INSERT INTO data_tabletotablecrossref
VALUES (10, now(), now(), 'parent child', 10, 12);

--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value VALUES (10, now(), now(), 'Contacts', 10, 3);
INSERT INTO data_value VALUES (11, now(), now(), 'person', 11, 3);
INSERT INTO data_value VALUES (12, now(), now(), '2', 12, 3);
INSERT INTO data_value VALUES (13, now(), now(), 'Personal Info', 10, 129);
INSERT INTO data_value
VALUES (522, now(), now(), 'person,location,thing,event', 11, 129);
INSERT INTO data_value VALUES (14, now(), now(), '1', 12, 129);
INSERT INTO data_value VALUES (15, now(), now(), 'person', 13, 4);
INSERT INTO data_value VALUES (16, now(), now(), 'Me', 14, 4);
INSERT INTO data_value VALUES (17, now(), now(), '1', 16, 4);
INSERT INTO data_value VALUES (18, now(), now(), '1', 15, 4);
INSERT INTO data_value VALUES (19, now(), now(), 'location', 13, 5);
INSERT INTO data_value VALUES (20, now(), now(), 'My Location', 14, 5);
INSERT INTO data_value VALUES (21, now(), now(), '1', 16, 5);
INSERT INTO data_value VALUES (22, now(), now(), '2', 15, 5);
INSERT INTO data_value VALUES (23, now(), now(), 'thing', 13, 6);
INSERT INTO data_value VALUES (24, now(), now(), 'Phone', 14, 6);
INSERT INTO data_value VALUES (25, now(), now(), '1', 16, 6);
INSERT INTO data_value VALUES (26, now(), now(), '3', 15, 6);
INSERT INTO data_value VALUES (27, now(), now(), 'person', 13, 7);
INSERT INTO data_value VALUES (28, now(), now(), 'Spouse', 14, 7);
INSERT INTO data_value VALUES (29, now(), now(), '2', 16, 7);
INSERT INTO data_value VALUES (30, now(), now(), '4', 15, 7);
INSERT INTO data_value VALUES (31, now(), now(), 'Home', 10, 8);
INSERT INTO data_value
VALUES (32, now(), now(), 'person,location,event,thing', 11, 8);
INSERT INTO data_value VALUES (33, now(), now(), '2255103', 12, 8);


--
-- Data for Name: system_propertyrecord; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO system_propertyrecord VALUES (1, now(), now(), 'person/1/property/dynamic/1:
(323,)');
INSERT INTO system_propertyrecord VALUES (2, now(), now(), 'person/1/property/dynamic/2:given name
(49,given name)');
INSERT INTO system_propertyrecord VALUES (3, now(), now(), 'person/4/property/dynamic/2:given name
(328,given name)');
INSERT INTO system_propertyrecord VALUES (4, now(), now(), 'person/1/property/dynamic/3:
(324,)');

--
-- Data for Name: system_relationshiprecord; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO system_relationshiprecord
VALUES (1, now(), now(), 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord
VALUES (2, now(), now(), 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord
VALUES (3, now(), now(), 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord
VALUES (4, now(), now(), 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord
VALUES (5, now(), now(), 'thing/3/person/1:Owns');
INSERT INTO system_relationshiprecord
VALUES (6, now(), now(), 'thing/3/person/1:owns');
INSERT INTO system_relationshiprecord VALUES
  (7, now(), now(), 'person/1/person/4:colleague - A colleague of the person');
INSERT INTO system_relationshiprecord VALUES
  (8, now(), now(), 'person/1/person/4:colleague - A colleague of the person');
INSERT INTO system_relationshiprecord
VALUES (9, now(), now(), 'location/2/thing/3:LocatedIn');


--
-- Data for Name: people_person; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_person VALUES (1, now(), now(), 'Me', 'demo.hat.org');
INSERT INTO people_person VALUES (4, now(), now(), 'Spouse', 'spouse.hat.org');

--
-- Data for Name: things_thing; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO things_thing VALUES (3, now(), now(), 'Phone');

--
-- Data for Name: locations_location; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO locations_location VALUES (2, now(), now(), 'My Location');


--
-- Data for Name: people_personlocationcrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_personlocationcrossref
VALUES (4, now(), now(), 2, 1, 'GPSLocation', TRUE, 4);

--
-- Data for Name: people_persontopersoncrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_persontopersoncrossref
VALUES (2, now(), now(), 1, 4, 4, TRUE, 8);

--
-- Data for Name: people_systempropertydynamiccrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_systempropertydynamiccrossref
VALUES (1, now(), now(), 1, 1, 2, '', TRUE, 1);
INSERT INTO people_systempropertydynamiccrossref
VALUES (2, now(), now(), 1, 2, 1, 'given name', TRUE, 2);
INSERT INTO people_systempropertydynamiccrossref
VALUES (3, now(), now(), 4, 2, 8, 'given name', TRUE, 3);
INSERT INTO people_systempropertydynamiccrossref
VALUES (4, now(), now(), 1, 3, 3, '', TRUE, 4);


INSERT INTO things_thingpersoncrossref
VALUES (2, now(), now(), 1, 3, 'owns', TRUE, 6);


--
-- Data for Name: locations_locationthingcrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO locations_locationthingcrossref
VALUES (1, now(), now(), 3, 2, 'LocatedIn', TRUE, 9);
