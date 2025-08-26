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

# Verificar instalación de Docker Buildx
echo -e "${YELLOW}🐳 Verifying Docker Buildx...${NC}"
if ! docker buildx version &>/dev/null; then
  echo -e "${RED}❌ Error: Docker Buildx is not available${NC}"
  exit 1
fi

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

# Crear builder multi-plataforma si no existe
echo -e "${YELLOW}🏗️ Setting up multi-platform builder...${NC}"
docker buildx create --use --name multiarch-builder 2>/dev/null || docker buildx use multiarch-builder

# Construir y push imagen Docker multi-plataforma
echo -e "${YELLOW}🐳 Building and pushing Docker image for multiple platforms...${NC}"
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t cristiandelahooz/${NAME}:latest \
  --push \
  .

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Error: Docker build/push failed${NC}"
  exit 1
fi

# Deploy en servidor
echo -e "${YELLOW}🚀 Deploying to server...${NC}"
ssh root@138.68.233.53 <<EOF
    cd /home/petcare
    echo "Pulling latest images..."
    docker compose pull
    echo "Stopping containers..."
    docker compose down -v
    echo "Starting containers..."
    docker compose up -d
    echo "Checking container status..."
    docker compose ps
EOF

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
  echo -e "${GREEN}🌐 Application should be available at: http://138.68.233.53${NC}"

  # Verificar que los contenedores estén corriendo
  echo -e "${YELLOW}🔍 Verifying deployment...${NC}"
  ssh root@138.68.233.53 'docker compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"'
else
  echo -e "${RED}❌ Error: Deployment failed${NC}"
  exit 1
fi

# Obtener último commit
LAST_COMMIT_MESSAGE=$(git show -s --format='%h %s')
echo -e "${GREEN}📝 Last commit: ${LAST_COMMIT_MESSAGE}${NC}"
echo -e "${GREEN}📦 Version deployed: v${VERSION}${NC}"

# Limpiar builder (opcional)
echo -e "${YELLOW}🧹 Cleaning up...${NC}"
docker buildx prune -f >/dev/null 2>&1

echo -e "${GREEN}🎉 Deployment process completed!${NC}"
