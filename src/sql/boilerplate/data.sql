
--
-- Data for Name: data_table; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_table VALUES (1, now(), now(), 'My Static Data', 'MyStaticRecords');
INSERT INTO data_table VALUES (2, now(), now(), 'Body Measurements', 'MyStaticRecords');
INSERT INTO data_table VALUES (3, now(), now(), 'My Wardrobe', 'MyStaticRecords');
INSERT INTO data_table VALUES (4, now(), now(), 'Shirts', 'MyStaticRecords');
INSERT INTO data_table VALUES (5, now(), now(), 'Sweaters', 'MyStaticRecords');
INSERT INTO data_table VALUES (6, now(), now(), 'Shoes', 'MyStaticRecords');
INSERT INTO data_table VALUES (7, now(), now(), 'Family', 'MyStaticRecords');
INSERT INTO data_table VALUES (8, now(), now(), 'Spouse', 'MyStaticRecords');


--
-- Data for Name: data_field; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_field VALUES (1, now(), now(), 'My Name', 1);
INSERT INTO data_field VALUES (2, now(), now(), 'weight', 2);
INSERT INTO data_field VALUES (3, now(), now(), 'height', 2);
INSERT INTO data_field VALUES (5, now(), now(), 'Description', 4);
INSERT INTO data_field VALUES (6, now(), now(), 'Date Purchased', 4);
INSERT INTO data_field VALUES (7, now(), now(), 'Last Worn', 4);
INSERT INTO data_field VALUES (8, now(), now(), 'Name', 8);
INSERT INTO data_field VALUES (9, now(), now(), 'Age', 8);

--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_tabletotablecrossref VALUES (1, now(), now(), 'parentChild', 1, 2);
INSERT INTO data_tabletotablecrossref VALUES (2, now(), now(), 'parentChild', 1, 3);
INSERT INTO data_tabletotablecrossref VALUES (3, now(), now(), 'parentChild', 3, 4);
INSERT INTO data_tabletotablecrossref VALUES (4, now(), now(), 'parentChild', 3, 5);
INSERT INTO data_tabletotablecrossref VALUES (5, now(), now(), 'parentChild', 3, 6);
INSERT INTO data_tabletotablecrossref VALUES (6, now(), now(), 'parentChild', 1, 7);
INSERT INTO data_tabletotablecrossref VALUES (7, now(), now(), 'parentChild', 7, 8);

--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record VALUES (1, now(), now(), 'Day 1');

--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value VALUES (1, now(), now(), 'My Name', 1, 1);
INSERT INTO data_value VALUES (2, now(), now(), 'Wendy', 8, 1);
INSERT INTO data_value VALUES (3, now(), now(), '92', 2, 1);
INSERT INTO data_value VALUES (4, now(), now(), '193', 3, 1);
INSERT INTO data_value VALUES (5, now(), now(), 'the blue ones', 5, 1);
INSERT INTO data_value VALUES (6, now(), now(), 'Bob the Plumber', 1, 1);