#!/bin/bash

echo "=========================================="
echo "Voter Hierarchy System - Quick Start"
echo "=========================================="
echo ""

# Check Java version
echo "Checking Java installation..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "✓ Java version: $JAVA_VERSION"
else
    echo "✗ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
echo "Checking Maven installation..."
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✓ $MVN_VERSION"
else
    echo "✗ Maven not found. Please install Maven."
    exit 1
fi

echo ""
echo "Building application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✓ Build successful!"
    echo ""
    echo "Starting application..."
    echo "Access the application at: http://localhost:8080"
    echo "Default credentials: admin / admin123"
    echo ""
    echo "Press Ctrl+C to stop the application"
    echo ""
    java -jar target/voter-hierarchy-system-1.0.0.jar
else
    echo "✗ Build failed. Please check the errors above."
    exit 1
fi
