# NGINX Playground

Repositório dedicado à construção de ambientes e soluções utilizando o NGINX como componente central para infraestrutura web. Cada branch explora uma aplicação distinta do NGINX, desde seu uso como servidor HTTP até funções mais avançadas como proxy reverso, gateway de APIs e balanceador de carga.

---

## 📌 Proposta

Este laboratório tem como objetivo explorar, validar e versionar configurações e estratégias de uso do NGINX em diferentes cenários de arquitetura web.

---

## 🔀 Estrutura por branches

As features estão organizadas em branches independentes, cada uma tratando de um contexto específico:

- `server-basics` – Servidor HTTP estático e estrutura mínima
- `proxy-reverse` – Reverse proxy com múltiplos serviços
- `api-gateway` – Direcionamento de rotas e segmentação de microsserviços
- `load-balancer` – Balanceamento de carga com upstreams e fallback
- `logs-custom` – Configuração avançada de logs
- *(Outras branches serão adicionadas conforme a evolução do projeto)*

---

## ⚙️ Tecnologias e práticas abordadas

- NGINX 1.26+
- Docker / Docker Compose
- HTTP routing
- Static file serving
- Reverse Proxy
- Load Balancing (Round Robin)
- API Gateway com `proxy_pass`
- Custom error pages
- Logging e debug

---

## 📁 Execução local

```bash
docker-compose up -d
