INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Brand', 'The brand(s) associated with a product or service, or the brand(s) maintained by an organization or business person.');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'ContactPoint', 'A contact point for a person or organization. Supersedes contactPoints');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Country', 'Nationality of the person');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Date', 'Date of birth');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Demand', 'A pointer to products or services sought by the organization or person (demand)');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Distance', 'The height of the item');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'EducationalOrganization', 'An educational organizations that the person is an alumni of');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Offer', 'A pointer to products or services offered by the organization or person');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Place', 'A somewhat fixed, physical extension');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'PostalAddress', 'Physical address of the item');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'PriceSpecification', 'The total financial value of the person as calculated by subtracting assets from liabilities');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'QuantitativeValue', 'A quantitative (numerical) value');
INSERT INTO system_type (date_created, last_updated, name, description) VALUES (now(), now(), 'Text', 'A generic textual value');



INSERT INTO system_unitofmeasurement VALUES (1, now(), now(), 'kilograms', 'measurement of weight', 'kg');
INSERT INTO system_unitofmeasurement VALUES (2, now(), now(), 'meters', 'measurement of height or length', 'm');
INSERT INTO system_unitofmeasurement VALUES (3, now(), now(), 'none', 'no unit of measurement (plain text)', NULL);
INSERT INTO system_unitofmeasurement VALUES (4, now(), now(), 'centimeters', 'measurement of height or length', 'cm');

INSERT INTO system_property VALUES (1, now(), now(), 'bodyWeight', 'Body weight of a person',
                                    (SELECT id FROM system_type WHERE name='QuantitativeValue'),
                                    (SELECT id FROM system_unitofmeasurement WHERE name='kilograms'));

INSERT INTO system_property VALUES (2, now(), now(), 'name', 'Name',
                                    (SELECT id FROM system_type WHERE name='Text'),
                                    (SELECT id FROM system_unitofmeasurement WHERE name='none'));

INSERT INTO system_property VALUES (3, now(), now(), 'height', 'Body height of a person',
                                    (SELECT id FROM system_type WHERE name='QuantitativeValue'),
                                    (SELECT id FROM system_unitofmeasurement WHERE name='centimeters'));