---
-- #%L
-- i2b2 Patient Data Export Service
-- %%
-- Copyright (C) 2013 Emory University
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---

---
-- Database setup for PostgreSQL
---

alter table output_column_configurations drop constraint FK78A321884808884F;
alter table output_configurations_output_column_configurations drop constraint FK17265E0C6501D153;
alter table output_configurations_output_column_configurations drop constraint FK17265E0C5DF9429;
drop table i2b2_concepts cascade;
drop table output_column_configurations cascade;
drop table output_configurations cascade;
drop table output_configurations_output_column_configurations cascade;
drop sequence I2B2_CONCEPT_SEQ;
drop sequence OUTPUT_COL_CONFIG_SEQ;
drop sequence OUTPUT_CONFIG_SEQ;
create table i2b2_concepts (id int8 not null, columnName varchar(255), dimensionCode varchar(255) not null, displayName varchar(255), hasChildren varchar(255), i2b2Key varchar(255) not null, icd9 varchar(255), isSynonym varchar(255) not null, level int4 not null, name varchar(255), operator varchar(255), tableName varchar(255) not null, tooltip varchar(255), xmlOrig CLOB, primary key (id));
create table output_column_configurations (id int8 not null, aggregation int4, columnName varchar(255) not null, columnOrder int4 not null, displayFormat int4 not null, howMany int4, includeTimeRange bool, includeUnits bool, i2b2Concept_id int8, primary key (id));
create table output_configurations (id int8 not null, missingValue varchar(255), name varchar(255), quoteChar varchar(1), rowDimension int4 not null, separator varchar(1), username varchar(255) not null, whitespaceReplacement varchar(255), primary key (id), unique (username, name));
create table output_configurations_output_column_configurations (output_configurations_id int8 not null, columnConfigs_id int8 not null, unique (columnConfigs_id));
alter table output_column_configurations add constraint FK78A321884808884F foreign key (i2b2Concept_id) references i2b2_concepts;
alter table output_configurations_output_column_configurations add constraint FK17265E0C6501D153 foreign key (columnConfigs_id) references output_column_configurations;
alter table output_configurations_output_column_configurations add constraint FK17265E0C5DF9429 foreign key (output_configurations_id) references output_configurations;
create sequence I2B2_CONCEPT_SEQ;
create sequence OUTPUT_COL_CONFIG_SEQ;
create sequence OUTPUT_CONFIG_SEQ;