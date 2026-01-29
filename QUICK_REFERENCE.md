# Quick Reference Guide

## Installation (5 Minutes)

### Prerequisites Check
```bash
java -version    # Need 17+
mvn -version     # Need 3.6+
```

### Database Setup (Choose One)

**MySQL:**
```sql
CREATE DATABASE voter_hierarchy;
CREATE USER 'voter_user'@'localhost' IDENTIFIED BY 'voter_pass';
GRANT ALL PRIVILEGES ON voter_hierarchy.* TO 'voter_user'@'localhost';
```

**H2 (No Setup - In-Memory):**
Just update application.properties with H2 settings.

### Quick Start
```bash
# Extract project
tar -xzf voter-hierarchy-system.tar.gz
cd voter-hierarchy-system

# Update database credentials in:
# src/main/resources/application.properties

# Run
./start.sh

# Or manually:
mvn clean package
java -jar target/voter-hierarchy-system-1.0.0.jar
```

Access: http://localhost:8080
Login: admin / admin123

---

## Key Business Rules (Quick Reference)

### Family Head
- 2002 Details: **ALWAYS Required**
- Current Details: **Required if Active**
- Age Validation: **None**

### Family Members
- 2002 Details: **Required if Age ≥ 41**
- Current Details: **Required if Active**

### Validation Rules (CRITICAL)
**Based on CURRENT voter ID only:**

| Member Type | Rule |
|-------------|------|
| SPOUSE | Parent/Spouse(Current) = Family Head Name(2002) |
| SON/DAUGHTER/DEPENDENT | Parent/Spouse(Current) = Head Name(2002) OR Spouse Name(2002) |

**Ignored:** Age, Status, 2002 names

---

## Workflow Cheat Sheet

```
1. Create Family Head
   ↓
2. Add Members (Spouse, Son, Daughter, Dependent)
   ↓
3. View Hierarchy & Check Validation
   ↓
4. Fix Errors (if any)
   ↓
5. Finish Collection
```

---

## API Quick Reference

| Action | URL | Method |
|--------|-----|--------|
| List Families | /families | GET |
| Create Family | /families/new | GET |
| Save Family | /families/create | POST |
| Add Member | /families/{id}/members/new | GET |
| Save Member | /families/{id}/members/add | POST |
| View Hierarchy | /families/{id}/hierarchy | GET |
| Finish | /families/{id}/finish | GET |

---

## Database Tables

```
family
  ├── person (family_head)
  └── person[] (members)
       ├── voter_details_2002
       └── voter_details_current
            └── blo_details
```

---

## Common Commands

### Start/Stop Service
```bash
sudo systemctl start voter-hierarchy
sudo systemctl stop voter-hierarchy
sudo systemctl restart voter-hierarchy
sudo systemctl status voter-hierarchy
```

### View Logs
```bash
# Service logs
sudo journalctl -u voter-hierarchy -f

# Application logs
tail -f logs/spring.log
```

### Database Backup
```bash
mysqldump -u voter_user -p voter_hierarchy > backup.sql
```

### Database Restore
```bash
mysql -u voter_user -p voter_hierarchy < backup.sql
```

---

## Troubleshooting Quick Fixes

**Port 8080 in use:**
```properties
# application.properties
server.port=8081
```

**Database connection failed:**
```bash
# Check MySQL running
sudo systemctl status mysql

# Test connection
mysql -u voter_user -p
```

**Application won't start:**
```bash
# Check logs
sudo journalctl -u voter-hierarchy -n 50

# Rebuild
mvn clean package
```

**Validation errors showing incorrectly:**
- Check name normalization
- Verify exact name matches from 2002 data
- Compare case-insensitively

---

## Data Normalization Examples

| Input | Normalized Output |
|-------|------------------|
| `"  Ghouse   Mohiddin "` | `"Ghouse Mohiddin"` |
| `"Abdul    Rahiman"` | `"Abdul Rahiman"` |
| `"  John  "` | `"John"` |

---

## Configuration Quick Reference

### application.properties Key Settings

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/voter_hierarchy
spring.datasource.username=voter_user
spring.datasource.password=change_me

# Server
server.port=8080

# Security
spring.security.user.name=admin
spring.security.user.password=change_me

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
```

---

## Validation Error Examples

**Good:**
```
SPOUSE - Ayesha Begum
Parent/Spouse (Current): Ghouse Mohiddin
✓ Matches Family Head Name (2002): Ghouse Mohiddin
```

**Error:**
```
❌ DAUGHTER "Ullapathnajiya"
Parent/Spouse mismatch (CURRENT VOTER ID)
Expected: Ghouse Mohiddin OR Ayesha Begum
Found: Gausmohiddin
```

---

## Security Defaults

- Default User: `admin`
- Default Pass: `admin123`
- **⚠️ Change in production!**

---

## Performance Tuning Quick Tips

```bash
# Increase JVM memory
java -Xmx1024m -Xms512m -jar app.jar

# MySQL tuning
max_connections = 200
innodb_buffer_pool_size = 512M

# Connection pool
spring.datasource.hikari.maximum-pool-size=20
```

---

## File Locations

| Item | Location |
|------|----------|
| Application | `/opt/voter-hierarchy/` |
| JAR File | `target/voter-hierarchy-system-1.0.0.jar` |
| Logs | `/var/log/voter-hierarchy/` or `logs/` |
| Config | `src/main/resources/application.properties` |
| Service | `/etc/systemd/system/voter-hierarchy.service` |
| Backups | `/opt/voter-hierarchy/backups/` |

---

## Testing Data Samples

**Valid Family Head:**
- Name: `Ghouse Mohiddin`
- Parent: `Abdul Rahiman`
- EPIC: `ABC1234567`

**Valid Spouse:**
- Name: `Ayesha Begum`
- Parent: `Ghouse Mohiddin` (matches head)

**Valid Son:**
- Name: `Mohammed Fahad`
- Parent: `Ghouse Mohiddin` (matches head)
- OR Parent: `Ayesha Begum` (matches spouse)

---

## Support Contacts

- Documentation: README.md, DEPLOYMENT.md, TESTING.md
- Logs: Check application and service logs
- Database: Verify connectivity and data integrity

---

## Version Info

- Current Version: 1.0.0
- Spring Boot: 3.2.0
- Java: 17+
- Build Date: 2024-01-29

---

## Quick Commands Summary

```bash
# Build
mvn clean package

# Run
java -jar target/voter-hierarchy-system-1.0.0.jar

# Run with profile
java -jar -Dspring.profiles.active=production app.jar

# Check status
curl http://localhost:8080/actuator/health

# Backup
mysqldump -u voter_user -p voter_hierarchy > backup.sql

# View logs
tail -f logs/spring.log
```
