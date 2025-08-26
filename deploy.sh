#!/bin/bash

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting local deployment process...${NC}"

# Verificar que estamos en el directorio correcto
if [ ! -f "pom.xml" ]; then
  echo -e "${RED}❌ Error: pom.xml not found. Make sure you're in the project root directory.${NC}"
  exit 1
fi

# Verificar instalación de Maven
echo -e "${YELLOW}📋 Verifying Maven installation...${NC}"
if ! command -v mvn &>/dev/null; then
  echo -e "${RED}❌ Error: Maven is not installed or not in PATH${NC}"
  exit 1
fi
mvn -version

# Instalar dependencias locales
echo -e "${YELLOW}📦 Installing fullcalendar2 dependencies...${NC}"
mvn install:install-file -Dfile=libs/fullcalendar2-7.0.0.jar -DgroupId=org.vaadin.stefan -DartifactId=fullcalendar2 -Dversion=7.0.0 -Dpackaging=jar -DgeneratePom=true

mvn install:install-file -Dfile=libs/fullcalendar2-scheduler-7.0.0.jar -DgroupId=org.vaadin.stefan -DartifactId=fullcalendar2-scheduler -Dversion=7.0.0 -Dpackaging=jar -DgeneratePom=true

# Obtener información del proyecto
echo -e "${YELLOW}📝 Getting project information...${NC}"
VERSION=$(mvn help:evaluate -q -DforceStdout -D"expression=project.version")
NAME=$(mvn help:evaluate -q -DforceStdout -D"expression=project.name")

echo -e "${GREEN}Project: ${NAME}${NC}"
echo -e "${GREEN}Version: ${VERSION}${NC}"

# Construir el proyecto
echo -e "${YELLOW}🔨 Building Java project with Maven...${NC}"
mvn dependency:go-offline -Pproduction
mvn clean package -DskipTests -Pproduction

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Error: Maven build failed${NC}"
  exit 1
fi

# Construir imagen Docker
echo -e "${YELLOW}🐳 Building Docker image...${NC}"
docker build -t cristiandelahooz/${NAME}:latest .

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Error: Docker build failed${NC}"
  exit 1
fi

# Push imagen Docker
echo -e "${YELLOW}📤 Pushing Docker image...${NC}"
docker push cristiandelahooz/${NAME}:latest

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Error: Docker push failed${NC}"
  exit 1
fi

# Deploy en servidor
echo -e "${YELLOW}🚀 Deploying to server...${NC}"
ssh root@138.68.233.53 <<'EOF'
    cd /home/petcare
    docker compose pull
    docker compose down -v
    docker compose up -d
EOF

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
  echo -e "${GREEN}🌐 Application should be available at: http://138.68.233.53${NC}"
else
  echo -e "${RED}❌ Error: Deployment failed${NC}"
  exit 1
fi

# Obtener último commit
LAST_COMMIT_MESSAGE=$(git show -s --format='%h %s')
echo -e "${GREEN}📝 Last commit: ${LAST_COMMIT_MESSAGE}${NC}"
echo -e "${GREEN}📦 Version deployed: v${VERSION}${NC}"
