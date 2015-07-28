
CREATE TABLE public.system_relationshiprecord (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                CONSTRAINT system_relationshiprecord_pk PRIMARY KEY (id)
);


CREATE TABLE public.system_relationshiprecordtorecordcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                relationshiprecord_id1 INTEGER NOT NULL,
                relationshiprecord_id2 INTEGER NOT NULL,
                CONSTRAINT system_relationshiprecordtorecordcrossref_pk PRIMARY KEY (id)
);


CREATE SEQUENCE public.system_propertyrecord_id_seq;

CREATE TABLE public.system_propertyrecord (
                id INTEGER NOT NULL DEFAULT nextval('public.system_propertyrecord_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                CONSTRAINT system_propertyrecord_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_propertyrecord_id_seq OWNED BY public.system_propertyrecord.id;

CREATE TABLE public.data_bundle (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT data_bundle_pk PRIMARY KEY (id)
);


CREATE TABLE public.system_relationshiprecordtobundlecrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                bundle_id INTEGER NOT NULL,
                CONSTRAINT system_relationshiprecordtobundlecrossref_pk PRIMARY KEY (id)
);


CREATE SEQUENCE public.system_propertyrecordtobundlecrrossref_id_seq;

CREATE TABLE public.system_propertyrecordtobundlecrrossref (
                id VARCHAR NOT NULL DEFAULT nextval('public.system_propertyrecordtobundlecrrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                bundle_id INTEGER NOT NULL,
                CONSTRAINT system_propertyrecordtobundlecrrossref_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_propertyrecordtobundlecrrossref_id_seq OWNED BY public.system_propertyrecordtobundlecrrossref.id;

CREATE TABLE public.data_debit (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                start_date TIMESTAMP NOT NULL,
                end_date TIMESTAMP NOT NULL,
                rolling BOOLEAN NOT NULL,
                sell_rent BOOLEAN NOT NULL,
                price REAL NOT NULL,
                data_debit_key VARCHAR NOT NULL,
                bundle_id INTEGER NOT NULL,
                sender_id VARCHAR(36) NOT NULL,
                recipient_id VARCHAR(36) NOT NULL,
                CONSTRAINT data_debit_pk PRIMARY KEY (id)
);


CREATE SEQUENCE public.events_event_id_seq;

CREATE TABLE public.events_event (
                id INTEGER NOT NULL DEFAULT nextval('public.events_event_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR(100) NOT NULL,
                CONSTRAINT events_event_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.events_event_id_seq OWNED BY public.events_event.id;

CREATE SEQUENCE public.events_eventtoeventcrossref_id_seq;

CREATE TABLE public.events_eventtoeventcrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.events_eventtoeventcrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                event_one_id INTEGER NOT NULL,
                event_two_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                CONSTRAINT events_eventtoeventcrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.events_eventtoeventcrossref_id_seq OWNED BY public.events_eventtoeventcrossref.id;

CREATE INDEX events_eventtoeventcrossref_event_one_id
 ON public.events_eventtoeventcrossref USING BTREE
 ( event_one_id );

CREATE INDEX events_eventtoeventcrossref_event_two_id
 ON public.events_eventtoeventcrossref USING BTREE
 ( event_two_id );

CREATE SEQUENCE public.data_record_id_seq;

CREATE TABLE public.data_record (
                id INTEGER NOT NULL DEFAULT nextval('public.data_record_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                CONSTRAINT data_record_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.data_record_id_seq OWNED BY public.data_record.id;

CREATE SEQUENCE public.system_type_id_seq;

CREATE TABLE public.system_type (
                id INTEGER NOT NULL DEFAULT nextval('public.system_type_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                description TEXT NOT NULL,
                CONSTRAINT system_type_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_type_id_seq OWNED BY public.system_type.id;

CREATE SEQUENCE public.events_systemtypecrossref_id_seq;

CREATE TABLE public.events_systemtypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.events_systemtypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                event_id INTEGER NOT NULL,
                system_type_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                CONSTRAINT events_systemtypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.events_systemtypecrossref_id_seq OWNED BY public.events_systemtypecrossref.id;

CREATE INDEX events_systemtypecrossref_thing_id
 ON public.events_systemtypecrossref USING BTREE
 ( event_id );

CREATE INDEX events_systemtypecrossref_system_type_id
 ON public.events_systemtypecrossref USING BTREE
 ( system_type_id );

CREATE SEQUENCE public.system_typetotypecrossref_id_seq;

CREATE TABLE public.system_typetotypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.system_typetotypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                type_one_id INTEGER NOT NULL,
                type_two_id INTEGER NOT NULL,
                relationship_description VARCHAR(100),
                CONSTRAINT system_typetotypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_typetotypecrossref_id_seq OWNED BY public.system_typetotypecrossref.id;

CREATE INDEX system_typetotypecrossref_type_one_id
 ON public.system_typetotypecrossref USING BTREE
 ( type_one_id );

CREATE INDEX system_typetotypecrossref_type_two_id
 ON public.system_typetotypecrossref USING BTREE
 ( type_two_id );

CREATE SEQUENCE public.data_table_id_seq;

CREATE TABLE public.data_table (
                id INTEGER NOT NULL DEFAULT nextval('public.data_table_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                source_name VARCHAR NOT NULL,
                CONSTRAINT data_table_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.data_table_id_seq OWNED BY public.data_table.id;

CREATE SEQUENCE public.data_tabletotablecrossref_id_seq;

CREATE TABLE public.data_tabletotablecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.data_tabletotablecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                relationship_type VARCHAR NOT NULL,
                table1 INTEGER NOT NULL,
                table2 INTEGER NOT NULL,
                CONSTRAINT data_tabletotablecrossref_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.data_tabletotablecrossref_id_seq OWNED BY public.data_tabletotablecrossref.id;

CREATE SEQUENCE public.data_field_id_seq;

CREATE TABLE public.data_field (
                id INTEGER NOT NULL DEFAULT nextval('public.data_field_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                table_id_fk INTEGER NOT NULL,
                CONSTRAINT data_field_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.data_field_id_seq OWNED BY public.data_field.id;

CREATE SEQUENCE public.data_value_id_seq;

CREATE TABLE public.data_value (
                id INTEGER NOT NULL DEFAULT nextval('public.data_value_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                value TEXT NOT NULL,
                field_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                CONSTRAINT data_value_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.data_value_id_seq OWNED BY public.data_value.id;

CREATE SEQUENCE public.people_persontopersonrelationshiptype_id_seq;

CREATE TABLE public.people_persontopersonrelationshiptype (
                id INTEGER NOT NULL DEFAULT nextval('public.people_persontopersonrelationshiptype_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR(100) NOT NULL,
                description TEXT,
                CONSTRAINT people_persontopersonrelationshiptype_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.people_persontopersonrelationshiptype_id_seq OWNED BY public.people_persontopersonrelationshiptype.id;

CREATE SEQUENCE public.people_person_id_seq;

CREATE TABLE public.people_person (
                id INTEGER NOT NULL DEFAULT nextval('public.people_person_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                person_id VARCHAR(36) NOT NULL,
                CONSTRAINT people_person_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.people_person_id_seq OWNED BY public.people_person.id;

CREATE TABLE public.events_eventpersoncrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                person_id INTEGER NOT NULL,
                event_od INTEGER NOT NULL,
                relationship_description TEXT,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT events_eventpersoncrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_eventpersoncrossref_person_id
 ON public.events_eventpersoncrossref USING BTREE
 ( person_id );

CREATE INDEX events_eventpersoncrossref_event_id
 ON public.events_eventpersoncrossref USING BTREE
 ( event_od );

CREATE SEQUENCE public.people_systemtypecrossref_id_seq;

CREATE TABLE public.people_systemtypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.people_systemtypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                users_id INTEGER NOT NULL,
                system_type_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                CONSTRAINT people_systemtypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.people_systemtypecrossref_id_seq OWNED BY public.people_systemtypecrossref.id;

CREATE INDEX people_systemtypecrossref_person_id
 ON public.people_systemtypecrossref USING BTREE
 ( users_id );

CREATE SEQUENCE public.people_persontopersoncrossref_id_seq;

CREATE TABLE public.people_persontopersoncrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.people_persontopersoncrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                person_one_id INTEGER NOT NULL,
                person_two_id INTEGER NOT NULL,
                relationship_type_id INTEGER NOT NULL,
                CONSTRAINT people_persontopersoncrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.people_persontopersoncrossref_id_seq OWNED BY public.people_persontopersoncrossref.id;

CREATE INDEX users_persontopersoncrossref_person_one_id
 ON public.people_persontopersoncrossref USING BTREE
 ( person_one_id );

CREATE INDEX users_persontopersoncrossref_person_two_id
 ON public.people_persontopersoncrossref USING BTREE
 ( person_two_id );

CREATE INDEX users_persontopersoncrossref_relationship_type_id
 ON public.people_persontopersoncrossref USING BTREE
 ( relationship_type_id );

CREATE SEQUENCE public.things_thing_id_seq;

CREATE TABLE public.things_thing (
                id INTEGER NOT NULL DEFAULT nextval('public.things_thing_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR(100) NOT NULL,
                CONSTRAINT things_thing_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.things_thing_id_seq OWNED BY public.things_thing.id;

CREATE TABLE public.events_eventthingcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_id INTEGER NOT NULL,
                event_id INTEGER NOT NULL,
                relationship_description TEXT NOT NULL,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT events_eventthingcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_eventthing_thingid_crossref_owner_id
 ON public.events_eventthingcrossref USING BTREE
 ( thing_id );

CREATE INDEX events_eventthing_eventid_crossref_owner_id
 ON public.events_eventthingcrossref USING BTREE
 ( event_id );

CREATE SEQUENCE public.things_systemtypecrossref_id_seq;

CREATE TABLE public.things_systemtypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.things_systemtypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_id INTEGER NOT NULL,
                system_type_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                CONSTRAINT things_systemtypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.things_systemtypecrossref_id_seq OWNED BY public.things_systemtypecrossref.id;

CREATE INDEX things_thingpropertycrossref_thing_id
 ON public.things_systemtypecrossref USING BTREE
 ( thing_id );

CREATE INDEX things_thingpropertycrossref_thing_property_id
 ON public.things_systemtypecrossref USING BTREE
 ( system_type_id );

CREATE SEQUENCE public.things_thingtothingcrossref_id_seq;

CREATE TABLE public.things_thingtothingcrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.things_thingtothingcrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_one_id INTEGER NOT NULL,
                thing_two_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                CONSTRAINT things_thingtothingcrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.things_thingtothingcrossref_id_seq OWNED BY public.things_thingtothingcrossref.id;

CREATE INDEX things_thingtothingcrossref_thing_one_id
 ON public.things_thingtothingcrossref USING BTREE
 ( thing_one_id );

CREATE INDEX things_thingtothingcrossref_thing_two_id
 ON public.things_thingtothingcrossref USING BTREE
 ( thing_two_id );

CREATE TABLE public.things_thingpersoncrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                description TEXT,
                person_id INTEGER NOT NULL,
                thing_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT things_thingpersoncrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX things_thingpersoncrossref_owner_id
 ON public.things_thingpersoncrossref USING BTREE
 ( person_id );

CREATE INDEX things_thingpersoncrossref_thing_id
 ON public.things_thingpersoncrossref USING BTREE
 ( thing_id );

CREATE SEQUENCE public.system_unitofmeasurement_id_seq;

CREATE TABLE public.system_unitofmeasurement (
                id INTEGER NOT NULL DEFAULT nextval('public.system_unitofmeasurement_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR(100) NOT NULL,
                description TEXT,
                symbol VARCHAR(16),
                CONSTRAINT system_unitofmeasurement_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_unitofmeasurement_id_seq OWNED BY public.system_unitofmeasurement.id;

CREATE TABLE public.system_property (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR NOT NULL,
                description TEXT NOT NULL,
                type_id INTEGER NOT NULL,
                unitofmeasurement_id INTEGER NOT NULL,
                CONSTRAINT system_property_pkey PRIMARY KEY (id)
);


CREATE TABLE public.people_systempropertydynamiccrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                person_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT people_systempropertydynamiccrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX people_systempropertydynamiccrossref_people_id
 ON public.people_systempropertydynamiccrossref USING BTREE
 ( person_id );

CREATE INDEX people_systempropertydynamiccrossref_property_id
 ON public.people_systempropertydynamiccrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.people_systempropertystaticcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                person_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT people_systempropertystaticcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX people_systempropertystaticcrossref_person_id
 ON public.people_systempropertystaticcrossref USING BTREE
 ( person_id );

CREATE INDEX people_systempropertystaticcrossref_property_id
 ON public.people_systempropertystaticcrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.events_systempropertystaticcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                event_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT events_systempropertystaticcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_systempropertystaticcrossref_event_id
 ON public.events_systempropertystaticcrossref USING BTREE
 ( event_id );

CREATE TABLE public.events_systempropertydynamiccrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                event_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT events_systempropertydynamiccrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_systempropertydynamiccrossref_event_id
 ON public.events_systempropertydynamiccrossref USING BTREE
 ( event_id );

CREATE INDEX events_systempropertydynamiccrossref_property_id
 ON public.events_systempropertydynamiccrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.things_systempropertydynamiccrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT things_systempropertydynamiccrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX things_thingdynamicpropertycrossref_thing_id
 ON public.things_systempropertydynamiccrossref USING BTREE
 ( thing_id );

CREATE INDEX things_thingdyanmicpropertycrossref_thing_property_id
 ON public.things_systempropertydynamiccrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.things_systempropertystaticcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT things_systempropertystaticcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX things_thingstaticpropertycrossref_thing_id
 ON public.things_systempropertystaticcrossref USING BTREE
 ( thing_id );

CREATE INDEX things_thingstaticpropertycrossref_thing_property_id
 ON public.things_systempropertystaticcrossref USING BTREE
 ( system_property_id );

CREATE SEQUENCE public.system_eventlog_id_seq;

CREATE TABLE public.system_eventlog (
                id INTEGER NOT NULL DEFAULT nextval('public.system_eventlog_id_seq'),
                event_type VARCHAR(45) NOT NULL,
                date DATE NOT NULL,
                time TIME NOT NULL,
                creator VARCHAR(100) NOT NULL,
                command VARCHAR(100) NOT NULL,
                result VARCHAR(45) NOT NULL,
                CONSTRAINT system_eventlog_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.system_eventlog_id_seq OWNED BY public.system_eventlog.id;

CREATE SEQUENCE public.organisations_organisation_id_seq;

CREATE TABLE public.organisations_organisation (
                id INTEGER NOT NULL DEFAULT nextval('public.organisations_organisation_id_seq'),
                date_created TIMESTAMP NOT NULL,
                lasty_updated TIMESTAMP NOT NULL,
                name VARCHAR(100) NOT NULL,
                CONSTRAINT organisations_organisation_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.organisations_organisation_id_seq OWNED BY public.organisations_organisation.id;

CREATE SEQUENCE public.organisations_organisationthingcrossref_id_seq;

CREATE TABLE public.organisations_organisationthingcrossref (
                id VARCHAR NOT NULL DEFAULT nextval('public.organisations_organisationthingcrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                thing_id INTEGER NOT NULL,
                organisation_id INTEGER NOT NULL,
                relationship_type VARCHAR(100) NOT NULL,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT organisations_organisationthingcrossref_pk PRIMARY KEY (id)
);


ALTER SEQUENCE public.organisations_organisationthingcrossref_id_seq OWNED BY public.organisations_organisationthingcrossref.id;

CREATE SEQUENCE public.organisations_organisationtoorganisationcrossref_id_seq;

CREATE TABLE public.organisations_organisationtoorganisationcrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.organisations_organisationtoorganisationcrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                organisation_one_id INTEGER NOT NULL,
                organisation_two_id INTEGER NOT NULL,
                relationship_type VARCHAR(100) NOT NULL,
                is_current BOOLEAN NOT NULL,
                CONSTRAINT organisations_organisationtoorganisationcrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.organisations_organisationtoorganisationcrossref_id_seq OWNED BY public.organisations_organisationtoorganisationcrossref.id;

CREATE INDEX organisation_organisationtoorganisationcrossref_person_one_id
 ON public.organisations_organisationtoorganisationcrossref USING BTREE
 ( organisation_one_id );

CREATE INDEX organisation_organisationtoorganisationcrossref_person_two_id
 ON public.organisations_organisationtoorganisationcrossref USING BTREE
 ( organisation_two_id );

CREATE TABLE public.organisations_systempropertystaticcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                organisation_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT organisations_systempropertystaticcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX organisationssystempropertystaticcrossref_organisation_id
 ON public.organisations_systempropertystaticcrossref USING BTREE
 ( organisation_id );

CREATE INDEX organisationssystempropertystaticcrossref_property_id
 ON public.organisations_systempropertystaticcrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.organisations_systempropertydynamiccrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                organisation_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT organisations_systempropertydynamiccrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX organisationssystempropertydynamiccrossref_organisation_id
 ON public.organisations_systempropertydynamiccrossref USING BTREE
 ( organisation_id );

CREATE INDEX organisationssystempropertydynamiccrossref_property_id
 ON public.organisations_systempropertydynamiccrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.events_eventorganisationcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                organisation_id INTEGER NOT NULL,
                event_od INTEGER NOT NULL,
                relationship_description TEXT,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT events_eventorganisationcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_eventorganisationcrossref_organisation_id
 ON public.events_eventorganisationcrossref USING BTREE
 ( organisation_id );

CREATE INDEX events_eventorganisationcrossref_event_id
 ON public.events_eventorganisationcrossref USING BTREE
 ( event_od );

CREATE SEQUENCE public.organisation_systemtypecrossref_id_seq;

CREATE TABLE public.organisation_systemtypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.organisation_systemtypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                organisation_id INTEGER NOT NULL,
                system_type_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                CONSTRAINT organisation_systemtypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.organisation_systemtypecrossref_id_seq OWNED BY public.organisation_systemtypecrossref.id;

CREATE INDEX organisation_systemtypecrossref_organisation_id
 ON public.organisation_systemtypecrossref USING BTREE
 ( organisation_id );

CREATE INDEX organisation_systemtypecrossref_system_type_id
 ON public.organisation_systemtypecrossref USING BTREE
 ( system_type_id );

CREATE TABLE public.people_personorganisationcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                person_id INTEGER NOT NULL,
                organisation_id INTEGER NOT NULL,
                relationship_type VARCHAR NOT NULL,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT people_personorganisationcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX people_personorganisationcrossref_organisation_id
 ON public.people_personorganisationcrossref USING BTREE
 ( organisation_id );

CREATE INDEX people_personorganisationcrossref_person_id
 ON public.people_personorganisationcrossref USING BTREE
 ( person_id );

CREATE SEQUENCE public.locations_location_id_seq;

CREATE TABLE public.locations_location (
                id INTEGER NOT NULL DEFAULT nextval('public.locations_location_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                name VARCHAR(512) NOT NULL,
                CONSTRAINT locations_location_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.locations_location_id_seq OWNED BY public.locations_location.id;

CREATE TABLE public.locations_systempropertystaticcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                record_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT locations_systempropertystaticcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX locations_systempropertystaticcrossref_location_id
 ON public.locations_systempropertystaticcrossref USING BTREE
 ( location_id );

CREATE INDEX locations_systempropertystaticcrossref_property_id
 ON public.locations_systempropertystaticcrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.locations_systempropertydynamiccrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                system_property_id INTEGER NOT NULL,
                field_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                propertyrecord_id INTEGER NOT NULL,
                CONSTRAINT locations_systempropertydynamiccrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX locations_systempropertydynamiccrossref_location_id
 ON public.locations_systempropertydynamiccrossref USING BTREE
 ( location_id );

CREATE INDEX locations_systempropertydynamiccrossref_property_id
 ON public.locations_systempropertydynamiccrossref USING BTREE
 ( system_property_id );

CREATE TABLE public.events_eventlocationcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                event_id INTEGER NOT NULL,
                relationship_description TEXT NOT NULL,
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT events_eventlocationcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX events_eventlocationcrossref_location_id
 ON public.events_eventlocationcrossref USING BTREE
 ( location_id );

CREATE INDEX events_eventlocationcrossref_event_id
 ON public.events_eventlocationcrossref USING BTREE
 ( event_id );

CREATE SEQUENCE public.locations_systemtypecrossref_id_seq;

CREATE TABLE public.locations_systemtypecrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.locations_systemtypecrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                system_type_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                CONSTRAINT locations_systemtypecrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.locations_systemtypecrossref_id_seq OWNED BY public.locations_systemtypecrossref.id;

CREATE INDEX location_systemtypecrossref_location_id
 ON public.locations_systemtypecrossref USING BTREE
 ( location_id );

CREATE INDEX location_systemtypecrossref_system_type_id
 ON public.locations_systemtypecrossref USING BTREE
 ( system_type_id );

CREATE TABLE public.organisations_organisationlocationcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                organisation_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT organisations_organisationlocationcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX organisations_organisationlocationcrossref_organisation_id
 ON public.organisations_organisationlocationcrossref USING BTREE
 ( organisation_id );

CREATE INDEX organisations_organisationlocationcrossref_location_id
 ON public.organisations_organisationlocationcrossref USING BTREE
 ( location_id );

CREATE SEQUENCE public.locations_locationtolocationcrossref_id_seq;

CREATE TABLE public.locations_locationtolocationcrossref (
                id INTEGER NOT NULL DEFAULT nextval('public.locations_locationtolocationcrossref_id_seq'),
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                loc_one_id INTEGER NOT NULL,
                loc_two_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                CONSTRAINT locations_locationtolocationcrossref_pkey PRIMARY KEY (id)
);


ALTER SEQUENCE public.locations_locationtolocationcrossref_id_seq OWNED BY public.locations_locationtolocationcrossref.id;

CREATE INDEX locations_locationtolocationcrossref_loc_one_id
 ON public.locations_locationtolocationcrossref USING BTREE
 ( loc_one_id );

CREATE INDEX locations_locationtolocationcrossref_loc_two_id
 ON public.locations_locationtolocationcrossref USING BTREE
 ( loc_two_id );

CREATE TABLE public.locations_locationthingcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                thing_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT locations_locationthingcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX locations_locationthingcrossref_location_id
 ON public.locations_locationthingcrossref USING BTREE
 ( location_id );

CREATE INDEX locations_locationthingcrossref_thing_id
 ON public.locations_locationthingcrossref USING BTREE
 ( thing_id );

CREATE TABLE public.people_personlocationcrossref (
                id INTEGER NOT NULL,
                date_created TIMESTAMP NOT NULL,
                last_updated TIMESTAMP NOT NULL,
                location_id INTEGER NOT NULL,
                person_id INTEGER NOT NULL,
                relationship_type VARCHAR(100),
                is_current BOOLEAN NOT NULL,
                relationshiprecord_id INTEGER NOT NULL,
                CONSTRAINT people_personlocationcrossref_pkey PRIMARY KEY (id)
);


CREATE INDEX locations_locationpersoncrossref_location_id
 ON public.people_personlocationcrossref USING BTREE
 ( location_id );

CREATE INDEX locations_locationpersoncrossref_person_id
 ON public.people_personlocationcrossref USING BTREE
 ( person_id );

ALTER TABLE public.organisations_organisationlocationcrossref ADD CONSTRAINT system_relationshiprecord_organisations_organisationlocation278
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_personlocationcrossref ADD CONSTRAINT system_relationshiprecord_people_personlocationcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_locationthingcrossref ADD CONSTRAINT system_relationshiprecord_locations_locationthingcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_thingpersoncrossref ADD CONSTRAINT system_relationshiprecord_things_thingpersoncrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventorganisationcrossref ADD CONSTRAINT system_relationshiprecord_events_eventorganisationcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventpersoncrossref ADD CONSTRAINT system_relationshiprecord_events_eventpersoncrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventlocationcrossref ADD CONSTRAINT system_relationshiprecord_events_eventlocationcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventthingcrossref ADD CONSTRAINT system_relationshiprecord_events_eventthingcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_personorganisationcrossref ADD CONSTRAINT system_relationshiprecord_people_personorganisationcrossref_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationthingcrossref ADD CONSTRAINT system_relationshiprecord_organisations_organisationthingcro825
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_bundle ADD CONSTRAINT system_relationshiprecord_bundle_fk
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_relationshiprecordtorecordcrossref ADD CONSTRAINT system_relationshiprecord_system_relationshiprecordtorecordc18
FOREIGN KEY (relationshiprecord_id2)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_relationshiprecordtorecordcrossref ADD CONSTRAINT system_relationshiprecord_system_relationshiprecordtorecordc567
FOREIGN KEY (relationshiprecord_id1)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_relationshiprecordtobundlecrossref ADD CONSTRAINT system_relationshiprecord_system_relationshiprecordtobundlec161
FOREIGN KEY (relationshiprecord_id)
REFERENCES public.system_relationshiprecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertystaticcrossref ADD CONSTRAINT property_record_events_systempropertystaticcrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertydynamiccrossref ADD CONSTRAINT property_record_events_systempropertydynamiccrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertystaticcrossref ADD CONSTRAINT property_record_things_systempropertystaticcrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertydynamiccrossref ADD CONSTRAINT property_record_things_systempropertydynamiccrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertydynamiccrossref ADD CONSTRAINT property_record_locations_systempropertydynamiccrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertystaticcrossref ADD CONSTRAINT property_record_locations_systempropertystaticcrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertystaticcrossref ADD CONSTRAINT property_record_organisations_systempropertystaticcrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertydynamiccrossref ADD CONSTRAINT property_record_organisations_systempropertydynamiccrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertystaticcrossref ADD CONSTRAINT property_record_people_systempropertystaticcrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertydynamiccrossref ADD CONSTRAINT property_record_people_systempropertydynamiccrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_bundle ADD CONSTRAINT system_propertyrecord_bundle_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_bundle ADD CONSTRAINT system_propertyrecord_bundle_fk1
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_propertyrecordtobundlecrrossref ADD CONSTRAINT system_propertyrecord_system_propertytobundlecrossref_fk
FOREIGN KEY (propertyrecord_id)
REFERENCES public.system_propertyrecord (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_debit ADD CONSTRAINT data_bundle_data_debit_fk1
FOREIGN KEY (bundle_id)
REFERENCES public.data_bundle (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_propertyrecordtobundlecrrossref ADD CONSTRAINT data_bundle_system_propertytobundlecrossref_fk
FOREIGN KEY (bundle_id)
REFERENCES public.data_bundle (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_relationshiprecordtobundlecrossref ADD CONSTRAINT data_bundle_system_relationshiprecordtobundlecrossref_fk
FOREIGN KEY (bundle_id)
REFERENCES public.data_bundle (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventpersoncrossref ADD CONSTRAINT events_eventpersoncrossref_thing_id_fkey
FOREIGN KEY (event_od)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.events_eventtoeventcrossref ADD CONSTRAINT event_two_id_refs_id_fk
FOREIGN KEY (event_two_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.events_eventtoeventcrossref ADD CONSTRAINT event_one_id_refs_id_fk
FOREIGN KEY (event_one_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.events_systemtypecrossref ADD CONSTRAINT events_systemtypecrossref_fk
FOREIGN KEY (event_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventlocationcrossref ADD CONSTRAINT events_eventlocationcrossref_fk
FOREIGN KEY (event_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventthingcrossref ADD CONSTRAINT events_eventthingcrossref_fk
FOREIGN KEY (event_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventorganisationcrossref ADD CONSTRAINT events_eventorganisationcrossref_fk
FOREIGN KEY (event_od)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertydynamiccrossref ADD CONSTRAINT events_systempropertydynamiccrossref_fk
FOREIGN KEY (event_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertystaticcrossref ADD CONSTRAINT events_systempropertycrossref_fk
FOREIGN KEY (event_id)
REFERENCES public.events_event (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_value ADD CONSTRAINT data_record_data_value_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertystaticcrossref ADD CONSTRAINT data_record_things_systempropertycrossref_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertystaticcrossref ADD CONSTRAINT data_record_events_systempropertystaticcrossref_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertystaticcrossref ADD CONSTRAINT data_record_locations_systempropertystaticcrossref_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertystaticcrossref ADD CONSTRAINT data_record_people_systempropertystaticcrossref_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertystaticcrossref ADD CONSTRAINT data_record_organisations_systempropertystaticcrossref_fk
FOREIGN KEY (record_id)
REFERENCES public.data_record (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systemtypecrossref ADD CONSTRAINT system_type_things_systemtypecrossref_fk
FOREIGN KEY (system_type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisation_systemtypecrossref ADD CONSTRAINT system_type_organisation_systemtypecrossref_fk
FOREIGN KEY (system_type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systemtypecrossref ADD CONSTRAINT system_type_location_systemtypecrossref_fk
FOREIGN KEY (system_type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_typetotypecrossref ADD CONSTRAINT system_type_system_typetotypecrossref_fk
FOREIGN KEY (type_one_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_typetotypecrossref ADD CONSTRAINT system_type_system_typetotypecrossref_fk1
FOREIGN KEY (type_two_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systemtypecrossref ADD CONSTRAINT system_type_events_systemtypecrossref_fk
FOREIGN KEY (system_type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systemtypecrossref ADD CONSTRAINT system_type_people_systemtypecrossref_fk
FOREIGN KEY (system_type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_property ADD CONSTRAINT system_type_system_property_fk
FOREIGN KEY (type_id)
REFERENCES public.system_type (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_field ADD CONSTRAINT data_table_fk
FOREIGN KEY (table_id_fk)
REFERENCES public.data_table (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_tabletotablecrossref ADD CONSTRAINT data_table_data_tabletotablecrossref_fk
FOREIGN KEY (table2)
REFERENCES public.data_table (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_tabletotablecrossref ADD CONSTRAINT data_table_data_tabletotablecrossref_fk1
FOREIGN KEY (table1)
REFERENCES public.data_table (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertydynamiccrossref ADD CONSTRAINT data_field_things_systempropertydynamiccrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertydynamiccrossref ADD CONSTRAINT data_field_events_systempropertydynamiccrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertydynamiccrossref ADD CONSTRAINT data_field_locations_systempropertydynamiccrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertydynamiccrossref ADD CONSTRAINT data_field_people_systempropertydynamiccrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertydynamiccrossref ADD CONSTRAINT data_field_organisations_systempropertydynamiccrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.data_value ADD CONSTRAINT data_field_data_value_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertystaticcrossref ADD CONSTRAINT data_field_people_systempropertystaticcrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertystaticcrossref ADD CONSTRAINT data_field_organisations_systempropertystaticcrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertystaticcrossref ADD CONSTRAINT data_field_locations_systempropertystaticcrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertystaticcrossref ADD CONSTRAINT data_field_things_systempropertystaticcrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertystaticcrossref ADD CONSTRAINT data_field_events_systempropertystaticcrossref_fk
FOREIGN KEY (field_id)
REFERENCES public.data_field (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_persontopersoncrossref ADD CONSTRAINT relationship_type_id_refs_id_fk
FOREIGN KEY (relationship_type_id)
REFERENCES public.people_persontopersonrelationshiptype (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_personlocationcrossref ADD CONSTRAINT person_id_refs_id
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_thingpersoncrossref ADD CONSTRAINT owner_id_refs_id
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_persontopersoncrossref ADD CONSTRAINT people_persontopersoncrossref_person_one_id_fkey
FOREIGN KEY (person_one_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_persontopersoncrossref ADD CONSTRAINT people_persontopersoncrossref_person_two_id_fkey
FOREIGN KEY (person_two_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_personorganisationcrossref ADD CONSTRAINT person_id_refs_id_fk
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_systemtypecrossref ADD CONSTRAINT people_person_people_systemtypecrossref_fk
FOREIGN KEY (users_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventpersoncrossref ADD CONSTRAINT people_person_people_eventpersoncrossref_fk
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertystaticcrossref ADD CONSTRAINT people_person_people_systempropertystaticcrossref_fk
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertydynamiccrossref ADD CONSTRAINT people_person_people_systempropertydynamiccrossref_fk
FOREIGN KEY (person_id)
REFERENCES public.people_person (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_locationthingcrossref ADD CONSTRAINT thing_id_refs_id_fk
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_thingpersoncrossref ADD CONSTRAINT things_thingpersoncrossref_thing_id_fkey
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_systempropertystaticcrossref ADD CONSTRAINT things_thingstaticpropertycrossref_thing_id_fkey
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_thingtothingcrossref ADD CONSTRAINT thing_two_id_refs_id_fk
FOREIGN KEY (thing_two_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_thingtothingcrossref ADD CONSTRAINT thing_one_id_refs_id_fk
FOREIGN KEY (thing_one_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_systemtypecrossref ADD CONSTRAINT things_systemtypecrossref_fk
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventthingcrossref ADD CONSTRAINT events_thingeventcrossref_fk
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertydynamiccrossref ADD CONSTRAINT things_systempropertydynamiccrossref_fk
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationthingcrossref ADD CONSTRAINT things_thing_organisations_organisationthingcrossref_fk
FOREIGN KEY (thing_id)
REFERENCES public.things_thing (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.system_property ADD CONSTRAINT system_unitofmeasurement_system_property_fk
FOREIGN KEY (unitofmeasurement_id)
REFERENCES public.system_unitofmeasurement (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.things_systempropertystaticcrossref ADD CONSTRAINT thing_property_id_refs_id_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.things_systempropertydynamiccrossref ADD CONSTRAINT system_property_things_systempropertydynamiccrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertydynamiccrossref ADD CONSTRAINT system_property_events_systempropertydynamiccrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_systempropertystaticcrossref ADD CONSTRAINT system_property_events_systempropertystaticcrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertystaticcrossref ADD CONSTRAINT system_property_locations_systempropertystaticcrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertydynamiccrossref ADD CONSTRAINT system_property_locations_systempropertydynamiccrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertystaticcrossref ADD CONSTRAINT system_property_organisations_systempropertystaticcrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertydynamiccrossref ADD CONSTRAINT system_property_organisations_systempropertydynamiccrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertystaticcrossref ADD CONSTRAINT system_property_people_systempropertystaticcrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_systempropertydynamiccrossref ADD CONSTRAINT system_property_people_systempropertydynamiccrossref_fk
FOREIGN KEY (system_property_id)
REFERENCES public.system_property (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationlocationcrossref ADD CONSTRAINT organisations_organisationlocationcrossref_organisation_id_fkey
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.people_personorganisationcrossref ADD CONSTRAINT organisation_id_refs_id_fk
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.organisation_systemtypecrossref ADD CONSTRAINT organisations_organisation_organisation_systemtypecrossref_fk
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventorganisationcrossref ADD CONSTRAINT organisations_organisation_events_eventorganisationcrossref_fk
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertydynamiccrossref ADD CONSTRAINT organisations_organisation_organisations_systempropertydynam75
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_systempropertystaticcrossref ADD CONSTRAINT organisations_organisation_organisations_systempropertystati434
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationtoorganisationcrossref ADD CONSTRAINT organisations_organisation_organisation_organisationtoorgani876
FOREIGN KEY (organisation_one_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationtoorganisationcrossref ADD CONSTRAINT organisations_organisation_organisation_organisationtoorgani645
FOREIGN KEY (organisation_two_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.organisations_organisationthingcrossref ADD CONSTRAINT organisations_organisation_organisations_organisationthingcr474
FOREIGN KEY (organisation_id)
REFERENCES public.organisations_organisation (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.people_personlocationcrossref ADD CONSTRAINT locations_locationpersoncrossref_location_id_fkey
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.locations_locationthingcrossref ADD CONSTRAINT locations_locationthingcrossref_location_id_fkey
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.locations_locationtolocationcrossref ADD CONSTRAINT locations_locationtolocationcrossref_loc_two_id_fkey
FOREIGN KEY (loc_two_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.locations_locationtolocationcrossref ADD CONSTRAINT locations_locationtolocationcrossref_loc_one_id_fkey
FOREIGN KEY (loc_one_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE public.organisations_organisationlocationcrossref ADD CONSTRAINT locations_location_organisations_organisationlocationcrossre499
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systemtypecrossref ADD CONSTRAINT locations_location_location_systemtypecrossref_fk
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.events_eventlocationcrossref ADD CONSTRAINT locations_location_events_eventlocationcrossref_fk
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertydynamiccrossref ADD CONSTRAINT locations_location_locations_systempropertydynamiccrossref_fk
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;

ALTER TABLE public.locations_systempropertystaticcrossref ADD CONSTRAINT locations_location_locations_systempropertystaticcrossref_fk
FOREIGN KEY (location_id)
REFERENCES public.locations_location (id)
ON DELETE NO ACTION
ON UPDATE NO ACTION
NOT DEFERRABLE;
