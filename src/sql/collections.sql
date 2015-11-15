--
-- Data for Name: data_table; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_table VALUES (100, '2015-10-29 15:41:28.85', '2015-10-29 15:41:28.85', 'HyperDataBrowser', 'HyperDataBrowser');
INSERT INTO data_table VALUES (101, '2015-10-29 15:41:28.953', '2015-10-29 15:41:28.953', 'Collections', 'HyperDataBrowser');
INSERT INTO data_table VALUES (102, '2015-10-29 15:41:28.99', '2015-10-29 15:41:28.99', 'Entities', 'HyperDataBrowser');

--
-- Data for Name: data_field; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_field VALUES (315, '2015-10-29 15:41:28.96', '2015-10-29 15:41:28.96', 'name', 101);
INSERT INTO data_field VALUES (316, '2015-10-29 15:41:28.974', '2015-10-29 15:41:28.974', 'kinds', 101);
INSERT INTO data_field VALUES (317, '2015-10-29 15:41:28.979', '2015-10-29 15:41:28.979', 'collection_id', 101);
INSERT INTO data_field VALUES (318, '2015-10-29 15:41:28.994', '2015-10-29 15:41:28.994', 'kind', 102);
INSERT INTO data_field VALUES (319, '2015-10-29 15:41:29', '2015-10-29 15:41:29', 'name', 102);
INSERT INTO data_field VALUES (320, '2015-10-29 15:41:29.006', '2015-10-29 15:41:29.006', 'entity_id', 102);
INSERT INTO data_field VALUES (321, '2015-10-29 15:41:29.013', '2015-10-29 15:41:29.013', 'collection_id', 102);

--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record VALUES (130, '2015-10-29 15:48:34.525', '2015-10-29 15:48:34.525', 'Contacts');
INSERT INTO data_record VALUES (132, '2015-11-02 22:37:03.025', '2015-11-02 22:37:03.025', 'Collection Entity Me (Personal Info)');
INSERT INTO data_record VALUES (133, '2015-11-02 22:37:22.259', '2015-11-02 22:37:22.259', 'Collection Entity My Location (Personal Info)');
INSERT INTO data_record VALUES (134, '2015-11-02 22:43:41.746', '2015-11-02 22:43:41.746', 'Collection Entity Phone (Personal Info)');
INSERT INTO data_record VALUES (135, '2015-11-02 23:19:10.24', '2015-11-02 23:19:10.24', 'Collection Entity Spouse (Contacts)');
INSERT INTO data_record VALUES (129, '2015-10-29 15:43:53.026', '2015-10-29 15:43:53.026', 'Me');
INSERT INTO data_record VALUES (136, '2015-11-03 19:54:38.865', '2015-11-03 19:54:38.865', 'Collection Home[person,location,event,thing]');

--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_tabletotablecrossref VALUES (63, '2015-10-29 15:41:29.019', '2015-10-29 15:41:29.019', 'parent child', 100, 101);
INSERT INTO data_tabletotablecrossref VALUES (64, '2015-10-29 15:41:29.03', '2015-10-29 15:41:29.03', 'parent child', 100, 102);

--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value VALUES (530, '2015-10-29 15:49:05.307', '2015-10-29 15:49:05.307', 'Contacts', 315, 130);
INSERT INTO data_value VALUES (531, '2015-10-29 15:49:05.31', '2015-10-29 15:49:05.31', 'person', 316, 130);
INSERT INTO data_value VALUES (532, '2015-10-29 15:49:05.315', '2015-10-29 15:49:05.315', '2', 317, 130);
INSERT INTO data_value VALUES (521, '2015-10-29 15:45:40.412', '2015-10-29 15:45:40.412', 'Personal Info', 315, 129);
INSERT INTO data_value VALUES (522, '2015-10-29 15:45:40.423', '2015-10-29 15:45:40.423', 'person,location,thing,event', 316, 129);
INSERT INTO data_value VALUES (523, '2015-10-29 15:45:40.428', '2015-10-29 15:45:40.428', '1', 317, 129);
INSERT INTO data_value VALUES (541, '2015-11-02 22:37:03.194', '2015-11-02 22:37:03.194', 'person', 318, 132);
INSERT INTO data_value VALUES (542, '2015-11-02 22:37:03.207', '2015-11-02 22:37:03.207', 'Me', 319, 132);
INSERT INTO data_value VALUES (543, '2015-11-02 22:37:03.211', '2015-11-02 22:37:03.211', '1', 321, 132);
INSERT INTO data_value VALUES (544, '2015-11-02 22:37:03.214', '2015-11-02 22:37:03.214', '1', 320, 132);
INSERT INTO data_value VALUES (545, '2015-11-02 22:37:22.401', '2015-11-02 22:37:22.401', 'location', 318, 133);
INSERT INTO data_value VALUES (546, '2015-11-02 22:37:22.404', '2015-11-02 22:37:22.404', 'My Location', 319, 133);
INSERT INTO data_value VALUES (547, '2015-11-02 22:37:22.408', '2015-11-02 22:37:22.408', '1', 321, 133);
INSERT INTO data_value VALUES (548, '2015-11-02 22:37:22.413', '2015-11-02 22:37:22.413', '2', 320, 133);
INSERT INTO data_value VALUES (549, '2015-11-02 22:43:41.876', '2015-11-02 22:43:41.876', 'thing', 318, 134);
INSERT INTO data_value VALUES (550, '2015-11-02 22:43:41.884', '2015-11-02 22:43:41.884', 'Phone', 319, 134);
INSERT INTO data_value VALUES (551, '2015-11-02 22:43:41.888', '2015-11-02 22:43:41.888', '1', 321, 134);
INSERT INTO data_value VALUES (552, '2015-11-02 22:43:41.891', '2015-11-02 22:43:41.891', '3', 320, 134);
INSERT INTO data_value VALUES (553, '2015-11-02 23:19:10.332', '2015-11-02 23:19:10.332', 'person', 318, 135);
INSERT INTO data_value VALUES (554, '2015-11-02 23:19:10.341', '2015-11-02 23:19:10.341', 'Spouse', 319, 135);
INSERT INTO data_value VALUES (555, '2015-11-02 23:19:10.344', '2015-11-02 23:19:10.344', '2', 321, 135);
INSERT INTO data_value VALUES (556, '2015-11-02 23:19:10.346', '2015-11-02 23:19:10.346', '4', 320, 135);
INSERT INTO data_value VALUES (562, '2015-11-03 19:54:38.939', '2015-11-03 19:54:38.939', 'Home', 315, 136);
INSERT INTO data_value VALUES (563, '2015-11-03 19:54:38.941', '2015-11-03 19:54:38.942', 'person,location,event,thing', 316, 136);
INSERT INTO data_value VALUES (564, '2015-11-03 19:54:38.944', '2015-11-03 19:54:38.944', '2255103', 317, 136);



--
-- Data for Name: system_propertyrecord; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO system_propertyrecord VALUES (1, '2015-11-03 18:35:57.572', '2015-11-03 18:35:57.572', 'person/1/property/dynamic/1:
(323,)');
INSERT INTO system_propertyrecord VALUES (2, '2015-11-03 19:23:40.633', '2015-11-03 19:23:40.633', 'person/1/property/dynamic/2:given name
(49,given name)');
INSERT INTO system_propertyrecord VALUES (3, '2015-11-03 19:33:16.218', '2015-11-03 19:33:16.218', 'person/4/property/dynamic/2:given name
(328,given name)');
INSERT INTO system_propertyrecord VALUES (4, '2015-11-03 19:38:19.391', '2015-11-03 19:38:19.391', 'person/1/property/dynamic/3:
(324,)');

--
-- Data for Name: system_relationshiprecord; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO system_relationshiprecord VALUES (1, '2015-11-02 22:37:39.894', '2015-11-02 22:37:39.894', 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord VALUES (2, '2015-11-02 22:39:16.572', '2015-11-02 22:39:16.572', 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord VALUES (3, '2015-11-02 22:40:13.612', '2015-11-02 22:40:13.612', 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord VALUES (4, '2015-11-02 22:43:23.682', '2015-11-02 22:43:23.682', 'person/1/location/2:GPSLocation');
INSERT INTO system_relationshiprecord VALUES (5, '2015-11-02 22:43:56.268', '2015-11-02 22:43:56.268', 'thing/3/person/1:Owns');
INSERT INTO system_relationshiprecord VALUES (6, '2015-11-02 22:49:07.354', '2015-11-02 22:49:07.354', 'thing/3/person/1:owns');
INSERT INTO system_relationshiprecord VALUES (7, '2015-11-02 23:55:52.225', '2015-11-02 23:55:52.225', 'person/1/person/4:colleague - A colleague of the person');
INSERT INTO system_relationshiprecord VALUES (8, '2015-11-02 23:58:25.374', '2015-11-02 23:58:25.374', 'person/1/person/4:colleague - A colleague of the person');
INSERT INTO system_relationshiprecord VALUES (9, '2015-11-03 19:51:02.125', '2015-11-03 19:51:02.125', 'location/2/thing/3:LocatedIn');


--
-- Data for Name: people_person; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_person VALUES (1, '2015-11-02 22:37:03.1', '2015-11-02 22:37:03.1', 'Me', 'demo.hat.org');
INSERT INTO people_person VALUES (4, '2015-11-02 23:19:10.263', '2015-11-02 23:19:10.263', 'Spouse', 'spouse.hat.org');

--
-- Data for Name: things_thing; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO things_thing VALUES (3, '2015-11-02 22:43:41.793', '2015-11-02 22:43:41.793', 'Phone');

--
-- Data for Name: locations_location; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO locations_location VALUES (2, '2015-11-02 22:37:22.324', '2015-11-02 22:37:22.324', 'My Location');


--
-- Data for Name: people_personlocationcrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_personlocationcrossref VALUES (4, '2015-11-02 22:43:23.759', '2015-11-02 22:43:23.759', 2, 1, 'GPSLocation', true, 4);

--
-- Data for Name: people_persontopersoncrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_persontopersoncrossref VALUES (2, '2015-11-02 23:58:25.408', '2015-11-02 23:58:25.408', 1, 4, 4, true, 8);

--
-- Data for Name: people_systempropertydynamiccrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO people_systempropertydynamiccrossref VALUES (1, '2015-11-03 18:35:57.632', '2015-11-03 18:35:57.632', 1, 1, 323, '', true, 1);
INSERT INTO people_systempropertydynamiccrossref VALUES (2, '2015-11-03 19:23:40.636', '2015-11-03 19:23:40.636', 1, 2, 49, 'given name', true, 2);
INSERT INTO people_systempropertydynamiccrossref VALUES (3, '2015-11-03 19:33:16.221', '2015-11-03 19:33:16.221', 4, 2, 328, 'given name', true, 3);
INSERT INTO people_systempropertydynamiccrossref VALUES (4, '2015-11-03 19:38:19.393', '2015-11-03 19:38:19.393', 1, 3, 324, '', true, 4);



INSERT INTO things_thingpersoncrossref VALUES (2, '2015-11-02 22:49:07.383', '2015-11-02 22:49:07.383', 1, 3, 'owns', true, 6);


--
-- Data for Name: locations_locationthingcrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO locations_locationthingcrossref VALUES (1, '2015-11-03 19:51:02.141', '2015-11-03 19:51:02.141', 3, 2, 'LocatedIn', true, 9);
