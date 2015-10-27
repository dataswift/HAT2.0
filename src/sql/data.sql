--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

--
-- Data for Name: data_debit; Type: TABLE DATA; Schema: public; Owner: hat20
--



--
-- Name: data_debit_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_debit_id_seq', 1, false);


--
-- Data for Name: data_table; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_table (id, date_created, last_updated, name, source_name) VALUES (15, now(), now(), 'testTable2', 'api');
INSERT INTO data_table (id, date_created, last_updated, name, source_name) VALUES (16, now(), now(), 'My Static Data', 'HyperDataBrowser');


--
-- Data for Name: data_field; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_field (id, date_created, last_updated, name, table_id_fk) VALUES (49, now(), now(), 'My Name', 16);


--
-- Name: data_field_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_field_id_seq', 49, true);


--
-- Data for Name: data_record; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_record (id, date_created, last_updated, name) VALUES (8, now(), now(), 'Day 1');


--
-- Name: data_record_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_record_id_seq', 8, true);


--
-- Name: data_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_table_id_seq', 16, true);


--
-- Data for Name: data_tabletotablecrossref; Type: TABLE DATA; Schema: public; Owner: hat20
--



--
-- Name: data_tabletotablecrossref_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_tabletotablecrossref_id_seq', 9, true);


--
-- Data for Name: data_value; Type: TABLE DATA; Schema: public; Owner: hat20
--

INSERT INTO data_value (id, date_created, last_updated, value, field_id, record_id) VALUES (72, now(), now(), 'My Name', 49, 8);


--
-- Name: data_value_id_seq; Type: SEQUENCE SET; Schema: public; Owner: hat20
--

SELECT pg_catalog.setval('data_value_id_seq', 72, true);


--
-- PostgreSQL database dump complete
--

