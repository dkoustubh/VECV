#!/bin/bash

# VECV Pull Chord Report - Startup Script
# This script starts SQL Server and the Spring Boot application

echo "🚀 Starting VECV Pull Chord Report Project..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

# Check if SQL Server container exists
if docker ps -a | grep -q sqlserver; then
    echo "📦 SQL Server container found. Starting..."
    docker start sqlserver
    echo "✅ SQL Server started successfully"
else
    echo "📦 SQL Server container not found. Creating new container..."
    docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=Ats1234@" \
       -p 1433:1433 --name sqlserver \
       -d mcr.microsoft.com/mssql/server:2022-latest
    echo "✅ SQL Server container created and started"
    echo "⏳ Waiting 15 seconds for SQL Server to initialize..."
    sleep 15
fi

echo ""
echo "🔍 Checking if port 8070 is available..."
if lsof -Pi :8070 -sTCP:LISTEN -t >/dev/null ; then
    echo "⚠️  Port 8070 is in use. Stopping existing process..."
    lsof -ti:8070 | xargs kill -9
    sleep 2
fi

echo ""
echo "🌱 Starting Spring Boot application..."
echo "📍 Application will be available at:"
echo "   - Dashboard: http://localhost:8070/dashboard"
echo "   - Report: http://localhost:8070/report"
echo ""
echo "Press Ctrl+C to stop the application"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

./mvnw spring-boot:run
