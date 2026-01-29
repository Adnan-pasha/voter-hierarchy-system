# Testing Guide - Voter Hierarchy System

## Test Scenarios

### Test Case 1: Create Family with Active Head

**Objective:** Verify family head creation with active status

**Steps:**
1. Navigate to http://localhost:8080
2. Login with: admin / admin123
3. Click "Create New Family"
4. Fill in 2002 Voter Details:
   - Name: `Ghouse Mohiddin`
   - Parent/Spouse Name: `Abdul Rahiman`
   - EPIC No: `ABC1234567`
   - AC No: `123`
   - Part No: `45`
   - Serial No: `678`
5. Select Status: `Active`
6. Fill in Current Voter Details:
   - Name: `Ghouse Mohiddin`
   - Parent/Spouse Name: `Abdul Rahiman`
   - EPIC No: `XYZ9876543`
   - AC No: `123`
   - Part No: `45`
   - Serial No: `890`
7. Fill in BLO Details:
   - BLO Name: `Rajesh Kumar`
   - BLO Mobile: `9876543210`
8. Click "Create Family & Add Members"

**Expected Result:**
- Family created successfully
- Redirected to "Add Member" page
- Success message displayed
- Family code generated (e.g., FAM-ABCD1234)

---

### Test Case 2: Add Spouse with Validation Match

**Objective:** Verify spouse addition with correct parent/spouse name

**Pre-requisite:** Complete Test Case 1

**Steps:**
1. On "Add Member" page
2. Select Relation Type: `Spouse`
3. Enter Age: `45`
4. Select Status: `Active`
5. Fill 2002 Details:
   - Name: `Ayesha Begum`
   - Parent/Spouse Name: `Mohammed Hussain`
   - EPIC No: `DEF2345678`
   - AC No: `123`
   - Part No: `45`
   - Serial No: `679`
6. Fill Current Details:
   - Name: `Ayesha Begum`
   - Parent/Spouse Name: `Ghouse Mohiddin` (matches family head)
   - EPIC No: `PQR8765432`
   - AC No: `123`
   - Part No: `45`
   - Serial No: `891`
7. Fill BLO Details:
   - BLO Name: `Rajesh Kumar`
   - BLO Mobile: `9876543210`
8. Click "Add Member"

**Expected Result:**
- Member added successfully
- No validation errors in hierarchy view

---

### Test Case 3: Add Son with Parent Name Mismatch

**Objective:** Verify validation detects parent name mismatch

**Pre-requisite:** Complete Test Case 1 & 2

**Steps:**
1. Add Son with Age: `30`, Status: `Active`
2. Skip 2002 details (age < 41)
3. Fill Current Details:
   - Name: `Mohammed Fahad`
   - Parent/Spouse Name: `Gausmohiddin` (intentional mismatch)
   - Other details as needed
4. Click "Add Member"
5. View Hierarchy

**Expected Result:**
- Member added (no blocking)
- Validation error displayed in hierarchy:
  ```
  SON - Mohammed Fahad
  Parent/Spouse mismatch (CURRENT VOTER ID)
  Expected: Ghouse Mohiddin OR Ayesha Begum
  Found: Gausmohiddin
  ```

---

### Test Case 4: Add Daughter with Correct Parent

**Objective:** Verify son/daughter can match either parent

**Pre-requisite:** Complete Test Case 1 & 2

**Steps:**
1. Add Daughter with Age: `28`, Status: `Active`
2. Fill Current Details with Parent/Spouse Name: `Ayesha Begum`
3. Complete other required fields

**Expected Result:**
- Member added successfully
- No validation errors (matches spouse name)

---

### Test Case 5: Add Member Age >= 41

**Objective:** Verify 2002 details required for age >= 41

**Steps:**
1. Add Son with Age: `45`, Status: `Active`
2. Try to skip 2002 details
3. Fill current details

**Expected Result:**
- 2002 details section becomes mandatory
- Form validation prevents submission without 2002 data
- Error message displayed

---

### Test Case 6: Add Expired/Died Member

**Objective:** Verify expired status works correctly

**Steps:**
1. Add Dependent with Age: `70`, Status: `Expired`
2. Fill 2002 details (age >= 41)
3. Skip current details (not active)

**Expected Result:**
- Member added successfully
- Current details section hidden
- No validation errors
- Hierarchy shows member with "Expired/Died" badge

---

### Test Case 7: Data Normalization

**Objective:** Verify string normalization works

**Steps:**
1. Create family head with name: `  Ghouse   Mohiddin  ` (extra spaces)
2. Add spouse with parent name: `Ghouse  Mohiddin` (different spacing)

**Expected Result:**
- Names normalized to: `Ghouse Mohiddin`
- Validation passes despite different spacing
- Database stores normalized values

---

### Test Case 8: Family Head Expired Status

**Objective:** Verify family head can be expired

**Steps:**
1. Create new family
2. Set family head status to: `Expired`
3. Skip current voter details

**Expected Result:**
- Family created successfully
- Family head remains root of hierarchy
- Current details section hidden
- Can still add active members

---

### Test Case 9: View Hierarchy

**Objective:** Verify hierarchy display

**Steps:**
1. Create family with multiple members
2. Click "View Hierarchy"

**Expected Result:**
- Family tree displayed correctly
- Family head shown with 2002 details
- Members shown with current details (if available)
- Validation errors clearly listed
- Tree structure properly indented

---

### Test Case 10: Multiple Families

**Objective:** Verify multiple families can be managed

**Steps:**
1. Create Family 1 with 3 members
2. Click "Finish & Review"
3. Create Family 2 with 2 members
4. View family list

**Expected Result:**
- Both families listed
- Each has unique family code
- Member counts correct
- Can navigate between families

---

## Validation Testing Matrix

| Relation | Parent/Spouse Name | Expected Validation |
|----------|-------------------|---------------------|
| SPOUSE | Matches Head Name(2002) | ✓ PASS |
| SPOUSE | Different Name | ✗ FAIL |
| SON | Matches Head Name(2002) | ✓ PASS |
| SON | Matches Spouse Name(2002) | ✓ PASS |
| SON | Matches Neither | ✗ FAIL |
| DAUGHTER | Matches Head Name(2002) | ✓ PASS |
| DAUGHTER | Matches Spouse Name(2002) | ✓ PASS |
| DAUGHTER | Matches Neither | ✗ FAIL |
| DEPENDENT | Matches Head Name(2002) | ✓ PASS |
| DEPENDENT | Matches Spouse Name(2002) | ✓ PASS |
| DEPENDENT | Matches Neither | ✗ FAIL |

---

## Edge Cases to Test

### Edge Case 1: Very Long Names
- Test with names > 100 characters
- Verify proper display and storage

### Edge Case 2: Special Characters
- Test names with special characters: `O'Brien`, `D'Souza`
- Verify normalization handles them correctly

### Edge Case 3: Age Boundaries
- Test age = 40 (2002 details not required)
- Test age = 41 (2002 details required)
- Test age = 0 or negative (should fail validation)

### Edge Case 4: Empty Family
- Create family head only
- View hierarchy with no members
- Verify no errors

### Edge Case 5: Large Family
- Add 20+ members
- Verify performance remains acceptable
- Check hierarchy display

---

## Browser Compatibility Testing

Test on:
- ✓ Chrome (latest)
- ✓ Firefox (latest)
- ✓ Safari (latest)
- ✓ Edge (latest)
- ✓ Mobile browsers

---

## Performance Testing

### Load Test Scenario
1. Create 100 families
2. Each family with 5-10 members
3. Monitor response times:
   - Family creation: < 2 seconds
   - Add member: < 1 second
   - View hierarchy: < 1 second
   - List families: < 2 seconds

---

## Security Testing

### Test Authentication
1. Try accessing without login → Should redirect to login
2. Try wrong credentials → Should show error
3. Login with correct credentials → Should grant access
4. Logout → Should clear session

### Test CSRF Protection
1. Submit forms from external sites
2. Verify CSRF token validation

---

## Database Testing

### Data Integrity
```sql
-- Verify foreign key constraints
SELECT * FROM person WHERE family_id NOT IN (SELECT id FROM family);
-- Should return empty

-- Verify no orphaned voter details
SELECT * FROM voter_details_2002 WHERE person_id NOT IN (SELECT id FROM person);
SELECT * FROM voter_details_current WHERE person_id NOT IN (SELECT id FROM person);
-- Should return empty

-- Check data normalization
SELECT * FROM voter_details_2002 WHERE name LIKE '%  %';
-- Should return empty (no double spaces)
```

---

## Regression Testing Checklist

After any code changes, verify:
- [ ] Family head creation works
- [ ] Member addition works
- [ ] Validation logic correct
- [ ] Data normalization works
- [ ] Hierarchy display correct
- [ ] All forms validate properly
- [ ] No database errors
- [ ] Security still works
- [ ] UI responsive on mobile

---

## Bug Reporting Template

When reporting bugs, include:

**Bug Title:** Clear, descriptive title

**Severity:** Critical / High / Medium / Low

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happened

**Screenshots:**
Attach relevant screenshots

**Environment:**
- Browser: Chrome 120
- OS: Windows 11
- Database: MySQL 8.0
- Application Version: 1.0.0

**Additional Notes:**
Any other relevant information

---

## Test Data Cleanup

To clean test data:

```sql
-- Delete all families (cascades to all related tables)
DELETE FROM family;

-- Reset auto-increment
ALTER TABLE family AUTO_INCREMENT = 1;
ALTER TABLE person AUTO_INCREMENT = 1;
ALTER TABLE voter_details_2002 AUTO_INCREMENT = 1;
ALTER TABLE voter_details_current AUTO_INCREMENT = 1;
ALTER TABLE blo_details AUTO_INCREMENT = 1;
```

---

## Automated Testing (Future)

Consider implementing:
- JUnit tests for service layer
- Integration tests for repositories
- Selenium tests for UI workflows
- API testing with REST Assured
