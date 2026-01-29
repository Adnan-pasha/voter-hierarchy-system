# Deployment Guide - Voter Hierarchy System

## Quick Start Guide

### Step 1: Prepare Your Server

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java 17
sudo apt install openjdk-17-jdk -y
java -version

# Install Maven
sudo apt install maven -y
mvn -version
```

### Step 2: Setup Database

#### For MySQL:

```bash
# Install MySQL
sudo apt install mysql-server -y

# Secure MySQL
sudo mysql_secure_installation

# Create database and user
sudo mysql -u root -p
```

```sql
CREATE DATABASE voter_hierarchy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'voter_user'@'localhost' IDENTIFIED BY 'YourSecurePassword123!';
GRANT ALL PRIVILEGES ON voter_hierarchy.* TO 'voter_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### For PostgreSQL:

```bash
# Install PostgreSQL
sudo apt install postgresql postgresql-contrib -y

# Access PostgreSQL
sudo -u postgres psql
```

```sql
CREATE DATABASE voter_hierarchy;
CREATE USER voter_user WITH PASSWORD 'YourSecurePassword123!';
GRANT ALL PRIVILEGES ON DATABASE voter_hierarchy TO voter_user;
\q
```

### Step 3: Deploy Application

```bash
# Create application directory
sudo mkdir -p /opt/voter-hierarchy
cd /opt/voter-hierarchy

# Copy your project files here
# (Upload via SCP, Git clone, or copy manually)

# Update application.properties with your database credentials
nano src/main/resources/application.properties
```

Update these lines:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/voter_hierarchy
spring.datasource.username=voter_user
spring.datasource.password=YourSecurePassword123!

# Change default admin password
spring.security.user.password=YourAdminPassword123!
```

```bash
# Build the application
mvn clean package

# Test run
java -jar target/voter-hierarchy-system-1.0.0.jar
```

Access `http://YOUR_SERVER_IP:8080` to verify it works.

### Step 4: Configure as System Service

Create systemd service file:

```bash
sudo nano /etc/systemd/system/voter-hierarchy.service
```

Add this content:

```ini
[Unit]
Description=Voter Hierarchy Collection System
After=syslog.target network.target mysql.service
Wants=network-online.target

[Service]
User=www-data
WorkingDirectory=/opt/voter-hierarchy
ExecStart=/usr/bin/java -Xmx512m -Xms256m -jar /opt/voter-hierarchy/target/voter-hierarchy-system-1.0.0.jar
StandardOutput=journal
StandardError=journal
SyslogIdentifier=voter-hierarchy
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Enable and start the service:

```bash
# Reload systemd
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable voter-hierarchy

# Start the service
sudo systemctl start voter-hierarchy

# Check status
sudo systemctl status voter-hierarchy

# View logs
sudo journalctl -u voter-hierarchy -f
```

### Step 5: Setup Nginx Reverse Proxy (Optional but Recommended)

```bash
# Install Nginx
sudo apt install nginx -y

# Create Nginx configuration
sudo nano /etc/nginx/sites-available/voter-hierarchy
```

Add this content:

```nginx
server {
    listen 80;
    server_name your-domain.com;  # Replace with your domain or IP

    client_max_body_size 20M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
    }
}
```

Enable the site:

```bash
# Create symbolic link
sudo ln -s /etc/nginx/sites-available/voter-hierarchy /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

### Step 6: Setup SSL with Let's Encrypt (Optional)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Get SSL certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal is configured automatically
sudo certbot renew --dry-run
```

## Production Configuration

Create `application-production.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/voter_hierarchy?useSSL=true
spring.datasource.username=voter_user
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Logging
logging.level.root=WARN
logging.level.com.election=INFO
logging.file.name=/var/log/voter-hierarchy/application.log
logging.file.max-size=10MB
logging.file.max-history=30

# Security
spring.security.user.name=${ADMIN_USERNAME}
spring.security.user.password=${ADMIN_PASSWORD}

# Performance
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
```

Run with production profile:

```bash
java -jar -Dspring.profiles.active=production \
     -DDB_PASSWORD=SecureDbPass \
     -DADMIN_USERNAME=admin \
     -DADMIN_PASSWORD=SecureAdminPass \
     target/voter-hierarchy-system-1.0.0.jar
```

## Monitoring & Maintenance

### View Logs

```bash
# Application logs
sudo journalctl -u voter-hierarchy -f

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### Database Backup

```bash
# Create backup script
sudo nano /opt/voter-hierarchy/backup.sh
```

```bash
#!/bin/bash
BACKUP_DIR="/opt/voter-hierarchy/backups"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

# MySQL backup
mysqldump -u voter_user -p'YourPassword' voter_hierarchy > $BACKUP_DIR/voter_hierarchy_$DATE.sql

# Keep only last 7 days
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete

echo "Backup completed: voter_hierarchy_$DATE.sql"
```

Make executable and schedule:

```bash
sudo chmod +x /opt/voter-hierarchy/backup.sh

# Add to crontab (daily at 2 AM)
sudo crontab -e
0 2 * * * /opt/voter-hierarchy/backup.sh
```

### Performance Tuning

#### JVM Options

```bash
# Update service file with JVM options
sudo nano /etc/systemd/system/voter-hierarchy.service
```

Update ExecStart line:
```ini
ExecStart=/usr/bin/java \
    -Xmx1024m \
    -Xms512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -jar /opt/voter-hierarchy/target/voter-hierarchy-system-1.0.0.jar
```

#### MySQL Tuning

```bash
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

Add:
```ini
[mysqld]
max_connections = 200
innodb_buffer_pool_size = 512M
innodb_log_file_size = 128M
query_cache_size = 32M
```

Restart MySQL:
```bash
sudo systemctl restart mysql
```

## Troubleshooting

### Application Won't Start

```bash
# Check logs
sudo journalctl -u voter-hierarchy -n 100 --no-pager

# Check if port is in use
sudo netstat -tulpn | grep 8080

# Verify Java version
java -version

# Check file permissions
ls -la /opt/voter-hierarchy/
```

### Database Connection Issues

```bash
# Test MySQL connection
mysql -u voter_user -p voter_hierarchy

# Check MySQL status
sudo systemctl status mysql

# View MySQL logs
sudo tail -f /var/log/mysql/error.log
```

### High Memory Usage

```bash
# Monitor Java process
top -p $(pgrep java)

# Adjust JVM memory in service file
# Reduce -Xmx value if needed
```

### Slow Performance

```bash
# Enable slow query log (MySQL)
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf
```

Add:
```ini
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow-query.log
long_query_time = 2
```

## Security Best Practices

1. **Change Default Passwords**
   - Update admin password in application.properties
   - Use strong database passwords

2. **Firewall Configuration**
```bash
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

3. **Regular Updates**
```bash
sudo apt update && sudo apt upgrade -y
```

4. **Database Security**
   - Use strong passwords
   - Limit database access to localhost
   - Regular backups

5. **Application Security**
   - Keep Spring Boot updated
   - Monitor security advisories
   - Use HTTPS in production

## Scaling Considerations

For high-traffic deployments:

1. **Use Connection Pooling**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

2. **Add Load Balancer**
   - Deploy multiple application instances
   - Use Nginx or HAProxy for load balancing

3. **Database Replication**
   - Setup master-slave replication
   - Use read replicas for reporting

## Support Checklist

Before requesting support, check:
- [ ] Application logs
- [ ] Database connectivity
- [ ] Disk space availability
- [ ] Memory usage
- [ ] Java version compatibility
- [ ] Browser console errors

## Rollback Procedure

If you need to rollback to previous version:

```bash
# Stop service
sudo systemctl stop voter-hierarchy

# Restore database backup
mysql -u voter_user -p voter_hierarchy < /opt/voter-hierarchy/backups/voter_hierarchy_YYYYMMDD.sql

# Restore old JAR file
cp /opt/voter-hierarchy/backups/voter-hierarchy-system-OLD.jar \
   /opt/voter-hierarchy/target/voter-hierarchy-system-1.0.0.jar

# Start service
sudo systemctl start voter-hierarchy
```

## Contacts

- **System Administrator:** [Your contact]
- **Database Administrator:** [Your contact]
- **Application Support:** [Your contact]
