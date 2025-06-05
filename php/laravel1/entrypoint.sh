#!/bin/sh

echo "🔧 Iniciando setup do Laravel..."

# Garante que o diretório e o arquivo do SQLite existam
mkdir -p database
touch database/database.sqlite
chown -R laravel:laravel database

# Instala dependências com o Composer, se necessário
if [ ! -d "vendor" ]; then
  echo "📦 Executando composer install..."
  composer install
fi

# Copia .env, se não existir
if [ ! -f ".env" ]; then
  echo "📄 Criando arquivo .env..."
  cp .env.example .env
fi

# Gera APP_KEY, se não estiver definida
if ! grep -q "^APP_KEY=base64:" .env; then
  echo "🔐 Gerando APP_KEY..."
  php artisan key:generate
fi

# Executa as migrations
echo "🧬 Executando migrations..."
php artisan migrate --force

echo "🚀 Iniciando PHP-FPM..."
exec php-fpm
