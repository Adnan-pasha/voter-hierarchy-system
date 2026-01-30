# RBAC Testing Guide

## Default Users
- **ADMIN:** admin/admin123
- **OPERATOR:** operator/operator123
- **VIEWER:** viewer/viewer123

## Test Cases

### 1. ADMIN Role Tests
- ✅ Can access `/users` (User Management)
- ✅ Can create new users
- ✅ Can edit users and assign roles
- ✅ Can delete users (except last admin)
- ✅ Can create/edit/delete families
- ✅ Can view all families
- ✅ All UI buttons visible

### 2. OPERATOR Role Tests
- ❌ Cannot access `/users`
- ✅ Can create new families
- ✅ Can add/edit members
- ✅ Can delete families and members
- ✅ Can view all families
- ✅ Create/Edit/Delete buttons visible

### 3. VIEWER Role Tests
- ❌ Cannot access `/users`
- ❌ Cannot create families
- ❌ Cannot edit families
- ❌ Cannot delete families
- ✅ Can view families
- ✅ Can view hierarchy
- ✅ Can print BLO sheets
- ❌ No Create/Edit/Delete buttons visible

### 4. Session Tests
- ✅ Login updates last_login timestamp
- ✅ Logout invalidates session
- ✅ Inactive users cannot login
- ✅ Multiple concurrent users work correctly

### 5. Security Tests
- ✅ Direct URL access blocked by role
- ✅ Backend validates permissions
- ✅ Passwords encrypted (BCrypt)
- ✅ CSRF protection enabled