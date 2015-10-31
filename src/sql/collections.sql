
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
-- Name: data_field_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_field_id_seq', 321, true);


--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record VALUES (129, '2015-10-29 15:43:53.026', '2015-10-29 15:43:53.026', 'Me');
INSERT INTO data_record VALUES (130, '2015-10-29 15:48:34.525', '2015-10-29 15:48:34.525', 'Contacts');
INSERT INTO data_record VALUES (131, '2015-10-29 17:08:54.722', '2015-10-29 17:08:54.723', 'Collection Entity Myself (Personal Info)');


--
-- Name: data_record_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_record_id_seq', 131, true);


--
-- Name: data_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_table_id_seq', 102, true);


--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_tabletotablecrossref VALUES (63, '2015-10-29 15:41:29.019', '2015-10-29 15:41:29.019', 'parent child', 100, 101);
INSERT INTO data_tabletotablecrossref VALUES (64, '2015-10-29 15:41:29.03', '2015-10-29 15:41:29.03', 'parent child', 100, 102);


--
-- Name: data_tabletotablecrossref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_tabletotablecrossref_id_seq', 64, true);


--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value VALUES (521, '2015-10-29 15:45:40.412', '2015-10-29 15:45:40.412', 'Personal Info', 315, 129);
INSERT INTO data_value VALUES (522, '2015-10-29 15:45:40.423', '2015-10-29 15:45:40.423', 'person,location,thing,event', 316, 129);
INSERT INTO data_value VALUES (523, '2015-10-29 15:45:40.428', '2015-10-29 15:45:40.428', '1', 317, 129);
INSERT INTO data_value VALUES (530, '2015-10-29 15:49:05.307', '2015-10-29 15:49:05.307', 'Contacts', 315, 130);
INSERT INTO data_value VALUES (531, '2015-10-29 15:49:05.31', '2015-10-29 15:49:05.31', 'person', 316, 130);
INSERT INTO data_value VALUES (532, '2015-10-29 15:49:05.315', '2015-10-29 15:49:05.315', '2', 317, 130);
INSERT INTO data_value VALUES (533, '2015-10-29 17:08:54.962', '2015-10-29 17:08:54.962', 'person', 318, 131);
INSERT INTO data_value VALUES (534, '2015-10-29 17:08:54.975', '2015-10-29 17:08:54.975', 'Myself', 319, 131);
INSERT INTO data_value VALUES (535, '2015-10-29 17:08:54.978', '2015-10-29 17:08:54.978', '1', 321, 131);
INSERT INTO data_value VALUES (536, '2015-10-29 17:08:54.981', '2015-10-29 17:08:54.981', '18', 320, 131);
INSERT INTO data_value VALUES (537, '2015-10-29 17:09:13.272', '2015-10-29 17:09:13.272', 'person', 318, 132);
INSERT INTO data_value VALUES (538, '2015-10-29 17:09:13.276', '2015-10-29 17:09:13.276', 'Partner', 319, 132);
INSERT INTO data_value VALUES (539, '2015-10-29 17:09:13.28', '2015-10-29 17:09:13.28', '2', 321, 132);
INSERT INTO data_value VALUES (540, '2015-10-29 17:09:13.283', '2015-10-29 17:09:13.283', '19', 320, 132);


--
-- Name: data_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_value_id_seq', 540, true);


--
-- PostgreSQL database dump complete
--

