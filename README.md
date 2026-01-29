# Family-wise Voter Hierarchy Collection System

A Spring Boot web application for collecting voter information in a family hierarchy format to support Special Intensive Revision (SIR) activities.

## Features

- ✅ Family Head creation with 2002 voter list data
- ✅ Multiple family member addition (Spouse, Son, Daughter, Dependent)
- ✅ Age-based and status-based conditional field control
- ✅ Automatic data normalization (trim and space collapse)
- ✅ Hierarchy validation based on CURRENT voter ID data
- ✅ Visual hierarchy preview for BLO review
- ✅ Persistent storage in relational database
- ✅ Responsive UI with Bootstrap 5
- ✅ CSRF protection and security

## Technology Stack

- **Backend:** Spring Boot 3.2.0
- **Frontend:** Thymeleaf, Bootstrap 5
- **Database:** MySQL / PostgreSQL / H2
- **Security:** Spring Security
- **Build Tool:** Maven
- **Java Version:** 17

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ OR PostgreSQL 13+ OR use H2 (in-memory)
- Git (optional)

## Database Setup

### Option 1: MySQL (Recommended for Production)

```sql
CREATE DATABASE voter_hierarchy;
CREATE USER 'voter_user'@'localhost' IDENTIFIED BY 'voter_pass';
GRANT ALL PRIVILEGES ON voter_hierarchy.* TO 'voter_user'@'localhost';
FLUSH PRIVILEGES;
```

### Option 2: PostgreSQL

```sql
CREATE DATABASE voter_hierarchy;
CREATE USER voter_user WITH PASSWORD 'voter_pass';
GRANT ALL PRIVILEGES ON DATABASE voter_hierarchy TO voter_user;
```

### Option 3: H2 (For Development/Testing)

No setup required - uses in-memory database.

## Installation & Configuration

### 1. Clone or Extract the Project

```bash
cd voter-hierarchy-system
```

### 2. Configure Database

Edit `src/main/resources/application.properties`:

**For MySQL (Default):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/voter_hierarchy?createDatabaseIfNotExist=true
spring.datasource.username=voter_user
spring.datasource.password=voter_pass
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

**For PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/voter_hierarchy
spring.datasource.username=voter_user
spring.datasource.password=voter_pass
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**For H2 (Development):**
```properties
spring.datasource.url=jdbc:h2:mem:voter_hierarchy
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

### 3. Build the Application

```bash
mvn clean package
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/voter-hierarchy-system-1.0.0.jar
```

### 5. Access the Application

Open your browser and navigate to:
```
http://localhost:8080
```

**Default Login Credentials:**
- Username: `admin`
- Password: `admin123`

## Application Workflow

### 1. Create Family Head

1. Click "Create New Family"
2. Enter mandatory 2002 voter list details:
   - Name, Parent/Spouse Name
   - EPIC No, AC No, Part No, Serial No
3. Select Status (Active/Expired)
4. If Active, enter Current Voter ID details and BLO information
5. Click "Create Family & Add Members"

### 2. Add Family Members

1. Select Relation Type (Spouse/Son/Daughter/Dependent)
2. Enter Age
3. Select Status (Active/Expired)
4. If Age ≥ 41: Enter 2002 voter details
5. If Status = Active: Enter Current voter ID details and BLO information
6. Click "Add Member"
7. Repeat to add more members

### 3. View Hierarchy & Validation

1. Click "View Hierarchy"
2. Review the family tree structure
3. Check for validation errors (if any)
4. Validation rules:
   - **SPOUSE:** Parent/Spouse(Current) must match Family Head Name(2002)
   - **SON/DAUGHTER/DEPENDENT:** Parent/Spouse(Current) must match Family Head Name(2002) OR any Spouse Name(2002)

### 4. Finish Collection

1. Click "Finish Collection"
2. Data is ready for BLO verification

## Key Business Rules

### Data Normalization

All text inputs are automatically normalized:
- Leading/trailing spaces removed
- Multiple spaces collapsed to single space
- Example: `"  Ghouse   Mohiddin "` → `"Ghouse Mohiddin"`

### Conditional Field Rules

#### Family Head
- 2002 voter details: **Always mandatory**
- Current voter details: **Mandatory if Status = Active**
- No age validation applies

#### Family Members
- 2002 voter details: **Mandatory if Age ≥ 41**
- Current voter details: **Mandatory if Status = Active**

### Hierarchy Validation

- **Validation Basis:** ONLY Current Voter ID data
- **Ignored for Validation:** Age, Status, 2002 names
- **Validation Timing:** After hierarchy is built (non-blocking)

## Project Structure

```
voter-hierarchy-system/
├── src/
│   ├── main/
│   │   ├── java/com/election/voterhierarchy/
│   │   │   ├── VoterHierarchyApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── FamilyController.java
│   │   │   │   └── HomeController.java
│   │   │   ├── dto/
│   │   │   │   ├── FamilyHeadDTO.java
│   │   │   │   ├── FamilyMemberDTO.java
│   │   │   │   ├── HierarchyNode.java
│   │   │   │   └── ValidationError.java
│   │   │   ├── entity/
│   │   │   │   ├── Family.java
│   │   │   │   ├── Person.java
│   │   │   │   ├── VoterDetails2002.java
│   │   │   │   ├── VoterDetailsCurrent.java
│   │   │   │   └── BloDetails.java
│   │   │   ├── enums/
│   │   │   │   ├── RelationType.java
│   │   │   │   └── PersonStatus.java
│   │   │   ├── repository/
│   │   │   │   ├── FamilyRepository.java
│   │   │   │   └── PersonRepository.java
│   │   │   ├── service/
│   │   │   │   ├── FamilyService.java
│   │   │   │   └── HierarchyValidationService.java
│   │   │   └── util/
│   │   │       └── StringNormalizationUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           ├── layout.html
│   │           └── family/
│   │               ├── list.html
│   │               ├── create-head.html
│   │               ├── add-member.html
│   │               └── hierarchy.html
│   └── test/
└── pom.xml
```

## Database Schema

### Tables
1. **family** - Family records
2. **person** - Person records (head and members)
3. **voter_details_2002** - 2002 voter list data
4. **voter_details_current** - Current voter ID data
5. **blo_details** - BLO information

### Relationships
- Family 1:1 Family Head (Person)
- Family 1:N Members (Person)
- Person 1:1 VoterDetails2002
- Person 1:1 VoterDetailsCurrent
- VoterDetailsCurrent 1:1 BloDetails

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/families` | List all families |
| GET | `/families/new` | Show create family form |
| POST | `/families/create` | Create new family with head |
| GET | `/families/{id}/members/new` | Show add member form |
| POST | `/families/{id}/members/add` | Add family member |
| GET | `/families/{id}/hierarchy` | View family hierarchy |
| GET | `/families/{id}/finish` | Finish family collection |

## Security

- Basic authentication enabled
- CSRF protection active
- Role-based access control ready for extension
- Password encryption for user accounts

## Troubleshooting

### Database Connection Issues

**Error:** `Access denied for user`
- Check database username/password in `application.properties`
- Verify database user has correct permissions

**Error:** `Unknown database`
- Create database manually or add `?createDatabaseIfNotExist=true` to MySQL URL

### Application Won't Start

**Error:** `Port 8080 already in use`
- Change port in `application.properties`: `server.port=8081`

**Error:** `ClassNotFoundException`
- Run `mvn clean install` to rebuild

### Validation Errors

If seeing unexpected validation errors:
1. Check that 2002 names are exactly matching
2. Verify string normalization is working
3. Check case-insensitive comparison

## Production Deployment

### 1. Update Configuration

```properties
# Use production database
spring.datasource.url=jdbc:mysql://prod-server:3306/voter_hierarchy
spring.datasource.username=prod_user
spring.datasource.password=strong_password

# Disable dev tools
spring.devtools.restart.enabled=false

# Set production profile
spring.profiles.active=production

# Disable SQL logging
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=WARN
```

### 2. Build Production JAR

```bash
mvn clean package -DskipTests
```

### 3. Run as Service

Create systemd service file `/etc/systemd/system/voter-hierarchy.service`:

```ini
[Unit]
Description=Voter Hierarchy System
After=syslog.target network.target

[Service]
User=voter
ExecStart=/usr/bin/java -jar /opt/voter-hierarchy/voter-hierarchy-system-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable voter-hierarchy
sudo systemctl start voter-hierarchy
sudo systemctl status voter-hierarchy
```

## Future Enhancements

- [ ] Fuzzy name matching for validation
- [ ] Duplicate family detection
- [ ] Printable BLO verification sheets
- [ ] Audit trail for all changes
- [ ] Bulk data import (CSV/Excel)
- [ ] Advanced search and filtering
- [ ] Report generation
- [ ] Mobile app support

## Support

For issues or questions:
1. Check application logs: `logs/spring.log`
2. Enable debug logging: `logging.level.com.election=DEBUG`
3. Review validation rules in documentation

## License

Internal use only - Election Commission data collection system.

## Version History

- **1.0.0** (2024-01-29)
  - Initial release
  - Family hierarchy management
  - Validation based on current voter data
  - BLO verification support
