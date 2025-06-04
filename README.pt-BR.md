# NGINX com Docker – Branch: `01-server-basics`

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Este branch apresenta uma arquitetura básica e modularizada para configurar e executar servidores virtuais com NGINX utilizando Docker. A estrutura é pensada para facilitar o desenvolvimento local, testes de configurações e simulações de cenários comuns como:

- Servidores HTTP estáticos
- Virtual hosts (múltiplos servidores)
- Organização de arquivos de configuração
- Inclusão dinâmica de servidores via `include`
- Separação clara entre código, configuração e infraestrutura

## 📦 Estrutura do Projeto

```
.
├── docker-compose.yml             # Orquestração do container NGINX
├── settings/                      # Configurações do NGINX
│   ├── nginx.conf                 # Configuração principal
│   └── servers/                   # Virtual hosts (inclusos por nginx.conf)
│       ├── .gitkeep               # Mantém o diretório no versionamento
│       └── server1.conf           # Exemplo de servidor na porta 8080
└── web/                           # Conteúdo estático servido pelo NGINX
    ├── html/                      # Servido na raiz (porta 80)
    │   └── index.html
    └── server1/                   # Servido na porta 8080
        └── index.html
    └── error/                     # Páginas personalizadas de erro (40x, 50x)
        └── error40x.html
        └── error50x.html
```

## 🚀 Como executar

1. Suba os containers:
   ```bash
   docker compose up -d
   ```

2. Acesse no navegador:

    - [http://localhost](http://localhost) → Conteúdo do diretório `web/html`
    - [http://localhost:8080](http://localhost:8080) → Conteúdo do diretório `web/server1`

3. Teste a configuração:
   ```bash
   docker compose exec nginx nginx -t
   ```

## 🔧 Personalização

- Adicione novos servidores criando arquivos `.conf` em `settings/servers/`.
- Para cada servidor, associe uma nova porta no `docker-compose.yml`, se necessário.
- Os volumes são montados para permitir hot-reload sem rebuild da imagem.
- É possível definir páginas customizadas para erros HTTP (como 400, 401, 404, 500 etc.) utilizando diretivas error_page. Um exemplo pode ser visto com os arquivos error40x.html e error50x.html, servidos a partir de um diretório específico.

## ✅ Requisitos

- Docker 20+
- Docker Compose 2+

## 📝 Notas

- Diretórios vazios são mantidos com `.gitkeep` para garantir versionamento.
- A imagem `nginx:1.26` é usada explicitamente para garantir previsibilidade.
- Este conteúdo está vinculado ao **branch `01-server-basics`**, o qual documenta a configuração e estrutura inicial com base nos fundamentos do NGINX.

---

**Este ambiente serve como base de testes e experimentação de configurações do NGINX.**

---

## 📫 Contato

Caso deseje entrar em contato para oportunidades ou dúvidas:

- 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
- 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
- 📧 antonio@javadeveloper.com.br

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)