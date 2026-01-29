-- Sample Test Data for Voter Hierarchy System
-- This file contains sample data for testing the application

-- Note: Run this AFTER the application has started and created the tables
-- Or use the application UI to create test data

-- Instructions:
-- 1. Start the application (tables will be auto-created)
-- 2. Login with admin/admin123
-- 3. Use the web interface to create families
-- 4. Or connect to database and run this script

-- Example: Creating a sample family
-- This is just for reference - use the application UI for actual data entry

/*
INSERT INTO family (family_code, created_at, updated_at, created_by) 
VALUES ('FAM-TEST001', NOW(), NOW(), 'OPERATOR');

-- Get the family ID
SET @family_id = LAST_INSERT_ID();

-- Create family head
INSERT INTO person (family_id, is_family_head, relation_type, status) 
VALUES (@family_id, 1, 'FAMILY_HEAD', 'ACTIVE');

-- Get person ID
SET @person_id = LAST_INSERT_ID();

-- Add 2002 voter details
INSERT INTO voter_details_2002 (person_id, name, parent_spouse_name, epic_no, ac_no, part_no, serial_no)
VALUES (@person_id, 'Ghouse Mohiddin', 'Abdul Rahiman', 'ABC1234567', '123', '45', '678');

-- Add current voter details
INSERT INTO voter_details_current (person_id, name, parent_spouse_name, epic_no, ac_no, part_no, serial_no)
VALUES (@person_id, 'Ghouse Mohiddin', 'Abdul Rahiman', 'XYZ9876543', '123', '45', '890');

-- Get current details ID
SET @current_id = LAST_INSERT_ID();

-- Add BLO details
INSERT INTO blo_details (voter_details_current_id, blo_name, blo_mobile)
VALUES (@current_id, 'BLO Rajesh Kumar', '9876543210');

-- Add a spouse
INSERT INTO person (family_id, is_family_head, relation_type, age, status) 
VALUES (@family_id, 0, 'SPOUSE', 45, 'ACTIVE');

SET @spouse_id = LAST_INSERT_ID();

INSERT INTO voter_details_2002 (person_id, name, parent_spouse_name, epic_no, ac_no, part_no, serial_no)
VALUES (@spouse_id, 'Ayesha Begum', 'Mohammed Hussain', 'DEF2345678', '123', '45', '679');

INSERT INTO voter_details_current (person_id, name, parent_spouse_name, epic_no, ac_no, part_no, serial_no)
VALUES (@spouse_id, 'Ayesha Begum', 'Ghouse Mohiddin', 'PQR8765432', '123', '45', '891');

SET @spouse_current_id = LAST_INSERT_ID();

INSERT INTO blo_details (voter_details_current_id, blo_name, blo_mobile)
VALUES (@spouse_current_id, 'BLO Rajesh Kumar', '9876543210');
*/

-- For actual usage, please use the web application interface
-- This ensures proper validation and data normalization
