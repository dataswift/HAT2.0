
--
-- Data for Name: data_table; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_table VALUES (16, '2015-11-02 22:35:17.30308', '2015-11-02 22:35:17.30308', 'My Static Data', 'MyStaticRecords');
INSERT INTO data_table VALUES (103, '2015-11-03 17:12:44.876', '2015-11-03 17:12:44.876', 'Body Measurements', 'MyStaticRecords');
INSERT INTO data_table VALUES (104, '2015-11-03 18:56:07.158', '2015-11-03 18:56:07.158', 'My Wardrobe', 'MyStaticRecords');
INSERT INTO data_table VALUES (105, '2015-11-03 18:56:21.088', '2015-11-03 18:56:21.088', 'Shirts', 'MyStaticRecords');
INSERT INTO data_table VALUES (106, '2015-11-03 18:56:33.348', '2015-11-03 18:56:33.348', 'Sweaters', 'MyStaticRecords');
INSERT INTO data_table VALUES (107, '2015-11-03 18:56:41.88', '2015-11-03 18:56:41.88', 'Shoes', 'MyStaticRecords');
INSERT INTO data_table VALUES (108, '2015-11-03 19:20:09.938', '2015-11-03 19:20:09.938', 'Family', 'MyStaticRecords');
INSERT INTO data_table VALUES (109, '2015-11-03 19:20:17.942', '2015-11-03 19:20:17.942', 'Spouse', 'MyStaticRecords');


--
-- Data for Name: data_field; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_field VALUES (49, '2015-11-02 22:35:17.303906', '2015-11-02 22:35:17.303906', 'My Name', 16);
INSERT INTO data_field VALUES (323, '2015-11-03 17:12:53.958', '2015-11-03 17:12:53.958', 'weight', 103);
INSERT INTO data_field VALUES (324, '2015-11-03 17:13:00.618', '2015-11-03 17:13:00.618', 'height', 103);
INSERT INTO data_field VALUES (325, '2015-11-03 18:57:45.513', '2015-11-03 18:57:45.513', 'Description', 105);
INSERT INTO data_field VALUES (326, '2015-11-03 18:58:00.247', '2015-11-03 18:58:00.247', 'Date Purchased', 105);
INSERT INTO data_field VALUES (327, '2015-11-03 18:58:07.551', '2015-11-03 18:58:07.551', 'Last Worn', 105);
INSERT INTO data_field VALUES (328, '2015-11-03 19:20:26.959', '2015-11-03 19:20:26.959', 'Name', 109);
INSERT INTO data_field VALUES (329, '2015-11-03 19:20:33.356', '2015-11-03 19:20:33.356', 'Age', 109);

--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_tabletotablecrossref VALUES (65, '2015-11-03 17:12:44.968', '2015-11-03 17:12:44.968', 'parentChild', 16, 103);
INSERT INTO data_tabletotablecrossref VALUES (66, '2015-11-03 18:56:07.267', '2015-11-03 18:56:07.267', 'parentChild', 16, 104);
INSERT INTO data_tabletotablecrossref VALUES (67, '2015-11-03 18:56:21.163', '2015-11-03 18:56:21.163', 'parentChild', 104, 105);
INSERT INTO data_tabletotablecrossref VALUES (68, '2015-11-03 18:56:33.418', '2015-11-03 18:56:33.419', 'parentChild', 104, 106);
INSERT INTO data_tabletotablecrossref VALUES (69, '2015-11-03 18:56:41.97', '2015-11-03 18:56:41.97', 'parentChild', 104, 107);
INSERT INTO data_tabletotablecrossref VALUES (70, '2015-11-03 19:20:10.067', '2015-11-03 19:20:10.067', 'parentChild', 16, 108);
INSERT INTO data_tabletotablecrossref VALUES (71, '2015-11-03 19:20:18.032', '2015-11-03 19:20:18.032', 'parentChild', 108, 109);

--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record VALUES (8, '2015-11-02 22:35:17.307522', '2015-11-02 22:35:17.307522', 'Day 1');

--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value VALUES (72, '2015-11-02 22:35:17.314089', '2015-11-02 22:35:17.314089', 'My Name', 49, 8);
INSERT INTO data_value VALUES (561, '2015-11-03 19:33:43.315', '2015-11-03 19:33:43.315', 'Wendy', 328, 8);
INSERT INTO data_value VALUES (557, '2015-11-03 18:36:52.656', '2015-11-03 18:36:52.656', '92', 323, 8);
INSERT INTO data_value VALUES (558, '2015-11-03 18:36:59.254', '2015-11-03 18:36:59.254', '193', 324, 8);
INSERT INTO data_value VALUES (559, '2015-11-03 18:58:22.207', '2015-11-03 18:58:22.207', 'the blue ones', 325, 8);
INSERT INTO data_value VALUES (560, '2015-11-03 19:05:44.395', '2015-11-03 19:05:44.395', 'Bob the Plumber', 49, 8);