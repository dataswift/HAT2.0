INSERT INTO people_persontopersonrelationshiptype
VALUES (1, now(), now(), 'agent', 'The direct performer or driver of the action (animate or inanimate)');
INSERT INTO people_persontopersonrelationshiptype VALUES (2, now(), now(), 'children', 'A child of the person');
INSERT INTO people_persontopersonrelationshiptype
VALUES (3, now(), now(), 'coach', 'A person that acts in a coaching role for a sports team');
INSERT INTO people_persontopersonrelationshiptype VALUES (4, now(), now(), 'colleague', 'A colleague of the person');

INSERT INTO people_persontopersonrelationshiptype VALUES (5, now(), now(), 'spouse', 'A spouse of a person');

SELECT setval('people_persontopersonrelationshiptype_id_seq', (SELECT max(id)+1 from people_persontopersonrelationshiptype), false);