i 📚 Guía de Despliegue de PetCare en DigitalOcean

## 📋 Requisitos Previos

### 1. Cuenta de Docker Hub

✅ **Ya tienes el repositorio creado**: `cristiandelahooz/petcare`

Ahora necesitas generar un Access Token:

1. Ve a [Docker Hub](https://hub.docker.com/) → Account Settings
2. Ve a Security → New Access Token
3. Dale un nombre descriptivo (ej: "petcare-github-actions")
4. Selecciona permisos: Read, Write, Delete
5. **IMPORTANTE**: Guarda el token inmediatamente (solo se muestra una vez)

### 2. Bot de Telegram (para notificaciones)

1. Busca `@BotFather` en Telegram
2. Envía `/newbot`
3. Dale un nombre a tu bot (ej: "PetCare Notifier")
4. Dale un username único (ej: `petcare_notify_bot`)
5. Guarda el token que te da BotFather
6. Para obtener tu Chat ID:
    - Inicia una conversación con tu bot
    - Envía cualquier mensaje
    - Ve a: `https://api.telegram.org/bot<TU_TOKEN>/getUpdates`
    - Busca `"chat":{"id":XXXXXXX}` - ese es tu Chat ID

## 🖥️ Configuración del Droplet de DigitalOcean

### Paso 1: Crear el Droplet

1. En DigitalOcean, crea un nuevo Droplet:
    - **Imagen**: Ubuntu 22.04 LTS
    - **Plan**: Basic
    - **CPU**: Regular (mínimo 2GB RAM para Java + PostgreSQL)
    - **Datacenter**: El más cercano a tus usuarios
    - **Authentication**: SSH Keys (IMPORTANTE - ver siguiente sección)
    - **Hostname**: `petcare-server` o el que prefieras

### Paso 2: Configurar SSH Keys

#### En tu computadora local:

```bash
# Generar un par de llaves SSH si no tienes una
ssh-keygen -t ed25519 -C "tu-email@ejemplo.com" -f ~/.ssh/petcare_deploy

# Ver tu llave pública
cat ~/.ssh/petcare_deploy.pub
```

#### En DigitalOcean:

1. Copia el contenido de `petcare_deploy.pub`
2. Al crear el Droplet, pega esta llave en "Add SSH Key"
3. Dale un nombre como "GitHub Actions Deploy Key"

### Paso 3: Configurar el Droplet

Una vez creado, conéctate por SSH:

```bash
ssh root@TU_IP_DROPLET
```

Ejecuta estos comandos para preparar el servidor:

```bash
# Actualizar el sistema
apt update && apt upgrade -y

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Instalar Docker Compose
apt install docker-compose-plugin -y

# Crear un usuario no-root para la aplicación (opcional pero recomendado)
adduser --disabled-password --gecos "" appuser
usermod -aG docker appuser

# Configurar firewall
ufw allow 22/tcp  # SSH
ufw allow 80/tcp  # HTTP
ufw allow 443/tcp # HTTPS (para futuro)
ufw --force enable

# Verificar que Docker funciona
docker run hello-world
```

## 🔐 Configuración de Secrets en GitHub

Ve a tu repositorio en GitHub → Settings → Secrets and variables → Actions

Agrega estos secrets:

| Secret Name          | Valor                         | Descripción                                      |
|----------------------|-------------------------------|--------------------------------------------------|
| `DOCKER_USERNAME`    | `cristiandelahooz`            | Tu nombre de usuario de Docker Hub               |
| `DOCKER_PASSWORD`    | `token-de-dockerhub`          | El Access Token de Docker Hub (NO tu contraseña) |
| `HOST`               | `IP.DE.TU.DROPLET`            | La IP de tu Droplet (ej: 167.99.123.45)          |
| `USERNAME`           | `root` o `appuser`            | Usuario SSH del servidor                         |
| `SSH_KEY`            | `contenido de petcare_deploy` | Llave privada SSH (ver abajo)                    |
| `DB_PASSWORD`        | `una-contraseña-segura`       | Contraseña para PostgreSQL                       |
| `TELEGRAM_BOT_TOKEN` | `123456:ABC-DEF...`           | Token de tu bot de Telegram                      |
| `TELEGRAM_CHAT_ID`   | `123456789`                   | Tu Chat ID de Telegram                           |

### Cómo agregar la SSH_KEY:

```bash
# En tu computadora local
cat ~/.ssh/petcare_deploy
```

Copia TODO el contenido, incluyendo:

```
-----BEGIN OPENSSH PRIVATE KEY-----
[contenido de la llave]
-----END OPENSSH PRIVATE KEY-----
```

## 🚀 Proceso de Despliegue

### Primera vez:

1. Haz push a la rama `main`
2. GitHub Actions automáticamente:
    - Construye la aplicación
    - Crea la imagen Docker
    - La sube a Docker Hub
    - Se conecta a tu Droplet
    - Descarga la imagen
    - Inicia la aplicación con PostgreSQL

### Actualizaciones:

Simplemente haz push a `main` y todo se actualiza automáticamente.

## 📍 Rutas y Ubicaciones en el Servidor

- **Aplicación**: `~/petcare/` (en el home del usuario)
- **Docker Compose**: `~/petcare/docker-compose.yml`
- **Datos de PostgreSQL**: Docker volume `postgres_data`
- **Logs**: `docker logs petcare-app-1`

## 🔍 Comandos Útiles en el Servidor

```bash
# Ver estado de los contenedores
cd ~/petcare
docker compose ps

# Ver logs de la aplicación
docker compose logs app -f

# Ver logs de la base de datos
docker compose logs db -f

# Reiniciar servicios
docker compose restart

# Detener todo
docker compose down

# Detener y eliminar volúmenes (CUIDADO: borra la BD)
docker compose down -v

# Entrar al contenedor de la app
docker compose exec app sh

# Backup de la base de datos
docker compose exec db pg_dump -U admin petcare_db > backup.sql
```

## 🌐 Acceder a la Aplicación

- **URL**: `http://TU_IP_DROPLET`
- La aplicación corre en el puerto 80 (redirigido del 8080 interno)

## 🔧 Solución de Problemas

### La aplicación no inicia:

```bash
# Verificar logs
docker compose logs app

# Verificar que la BD está corriendo
docker compose ps db

# Reiniciar todo
docker compose down
docker compose up -d
```

### Problemas de conexión SSH:

```bash
# Verificar permisos de la llave
chmod 600 ~/.ssh/petcare_deploy

# Probar conexión manual
ssh -i ~/.ssh/petcare_deploy root@TU_IP_DROPLET
```

### Telegram no envía notificaciones:

- Verifica que iniciaste una conversación con tu bot
- Confirma que el Chat ID es correcto
- Revisa que el token no tenga espacios extras

## 📝 Notas Importantes

1. **Seguridad**:
    - Cambia la contraseña de PostgreSQL regularmente
    - Considera usar un usuario no-root para SSH
    - Configura HTTPS con Let's Encrypt (Certbot)

2. **Backups**:
    - DigitalOcean ofrece backups automáticos ($2/mes)
    - Configura backups manuales de la BD regularmente

3. **Monitoreo**:
    - Considera usar DigitalOcean Monitoring (gratis)
    - Configura alertas para uso de CPU/RAM

4. **Dominio** (opcional):
    - Compra un dominio
    - Apúntalo a la IP de tu Droplet
    - Configura HTTPS con Certbot

## 🆘 Soporte

Si tienes problemas:

1. Revisa los logs de GitHub Actions
2. Revisa los logs en el servidor
3. Verifica que todos los secrets estén configurados correctamente
4. Asegúrate de que Docker Hub no tenga límites de rate (100 pulls/6h gratis)