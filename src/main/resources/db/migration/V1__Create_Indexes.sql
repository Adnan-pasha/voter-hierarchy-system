-- Users
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_active ON users(active);
CREATE INDEX IF NOT EXISTS idx_created_at ON users(created_at);
CREATE INDEX IF NOT EXISTS idx_user_auth ON users(username, active);

-- User Roles
CREATE INDEX IF NOT EXISTS idx_role ON user_roles(role);

-- Family
CREATE INDEX IF NOT EXISTS idx_family_code ON family(family_code);
CREATE INDEX IF NOT EXISTS idx_contact_number ON family(contact_number);
CREATE INDEX IF NOT EXISTS idx_created_at ON family(created_at);

-- Person
CREATE INDEX IF NOT EXISTS idx_family_id ON person(family_id);
CREATE INDEX IF NOT EXISTS idx_is_family_head ON person(is_family_head);
CREATE INDEX IF NOT EXISTS idx_status ON person(status);
CREATE INDEX IF NOT EXISTS idx_relation_type ON person(relation_type);
CREATE INDEX IF NOT EXISTS idx_family_person ON person(family_id, is_family_head, status);

-- Voter Details 2002
CREATE INDEX IF NOT EXISTS idx_name ON voter_details_2002(name);
CREATE INDEX IF NOT EXISTS idx_epic_no ON voter_details_2002(epic_no);
CREATE INDEX IF NOT EXISTS idx_ac_part_serial ON voter_details_2002(ac_no, part_no, serial_no);
CREATE INDEX IF NOT EXISTS idx_voter_search_2002 ON voter_details_2002(name, epic_no);

-- Voter Details Current
CREATE INDEX IF NOT EXISTS idx_name ON voter_details_current(name);
CREATE INDEX IF NOT EXISTS idx_parent_spouse_name ON voter_details_current(parent_spouse_name);
CREATE INDEX IF NOT EXISTS idx_epic_no ON voter_details_current(epic_no);
CREATE INDEX IF NOT EXISTS idx_ac_part_serial ON voter_details_current(ac_no, part_no, serial_no);
CREATE INDEX IF NOT EXISTS idx_voter_search_current ON voter_details_current(name, epic_no);

-- BLO Details
CREATE INDEX IF NOT EXISTS idx_blo_name ON blo_details(blo_name);
CREATE INDEX IF NOT EXISTS idx_blo_mobile ON blo_details(blo_mobile);