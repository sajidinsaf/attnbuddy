# AttnBuddy — Development Guide

## Prerequisites

### Backend
- Java 17 (JDK)
- Maven 3.9+
- MySQL 8.0+
- Redis 7+

### Mobile
- Node.js 18+ (LTS)
- npm or pnpm
- Expo CLI (`npx expo`)
- Expo Go app on your iPhone (download from App Store)
- An Expo account (free, create at expo.dev)

## Project Structure

```
attnbuddy/
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/com/visibleai/attnbuddy/
│   │   ├── config/           # Security, JWT, CORS configuration
│   │   ├── auth/             # Authentication (register, login, refresh)
│   │   ├── task/             # Task CRUD + PrioritizationEngine
│   │   ├── model/            # JPA entities
│   │   └── repository/       # Spring Data JPA repositories
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/     # Flyway migrations
│   └── pom.xml
│
├── mobile/                   # Expo React Native app
│   ├── app/                  # Screens (file-based routing)
│   ├── components/           # Reusable components
│   ├── services/             # API client, auth storage
│   ├── app.json              # Expo configuration
│   └── package.json
│
├── docs/                     # Project documentation
└── .claude/                  # Claude Code configuration
```

## Quick Start

### Backend

```bash
# 1. Create MySQL database
mysql -u root -p -e "CREATE DATABASE attnbuddy;"

# 2. Configure connection (copy and edit)
cp backend/src/main/resources/application-local.properties.example \
   backend/src/main/resources/application-local.properties
# Edit with your MySQL credentials

# 3. Build and run
cd backend
mvn clean compile
mvn spring-boot:run -Dspring-boot.run.profiles=local

# API available at http://localhost:8080
```

### Mobile

```bash
# 1. Install dependencies
cd mobile
npm install

# 2. Start Expo dev server
npx expo start

# 3. Scan QR code with your iPhone camera
#    This opens the app in Expo Go
```

### Testing on Physical iPhone

1. Install **Expo Go** from the App Store on your iPhone
2. Make sure your iPhone and computer are on the **same Wi-Fi network**
3. Run `npx expo start` in the mobile directory
4. Scan the QR code shown in the terminal with your iPhone camera
5. The app opens in Expo Go — edits hot-reload instantly

## Backend Deployment (MochaHost)

```bash
# Build WAR for production
cd backend
mvn clean package -Pprod -DskipTests

# Deploy to MochaHost
# Upload target/ROOT.war to your Tomcat webapps directory
```

API endpoint: `https://api.visibleai.com`

## Environment Variables

### Backend (application-local.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/attnbuddy
spring.datasource.username=your_username
spring.datasource.password=your_password
jwt.secret=your-256-bit-secret
jwt.access-token-expiry=3600000
jwt.refresh-token-expiry=2592000000
```

### Mobile
```
API_URL=http://localhost:8080  (dev)
API_URL=https://api.visibleai.com  (prod)
```

## SDLC Workflow

All changes follow this process:
1. `/new-change` — creates GitHub issue + feature branch
2. Implement with tests
3. `/pr` — creates pull request with SDLC checks
4. `/docs` — updates documentation in same commit
