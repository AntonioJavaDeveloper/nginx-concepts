# NGINX Playground

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Repository dedicated to building environments and solutions using NGINX as a core component for web infrastructure. Each branch explores a different use case of NGINX, from basic HTTP server functionality to more advanced roles such as reverse proxy, API gateway, and load balancer.

---

## 📌 Purpose

This lab aims to explore, validate, and version NGINX configurations and usage strategies in various web architecture scenarios.

---

## 🔀 Branch Structure

Features are organized into independent branches, each addressing a specific context:

- `01-server-basics` – Static HTTP server and minimal structure
- `02-proxy-reverse` – Reverse proxy with multiple services
- `03-api-gateway` – Route forwarding and microservices segmentation
- `04-load-balancer` – Load balancing with upstreams and fallback
- `05-logs-custom` – Advanced log configuration
- *(Other branches will be added as the project evolves)*

---

## ⚙️ Technologies and Practices Covered

- NGINX 1.26+
- Docker / Docker Compose
- HTTP routing
- Static file serving
- Reverse Proxy
- Load Balancing (Round Robin)
- API Gateway with `proxy_pass`
- Custom error pages
- Logging and debugging

---

## 📁 Local Execution

```bash
docker-compose up -d
```

---

**This environment serves as a base for testing and experimenting with NGINX configurations.**

---

## 📫 Contact

If you’d like to get in touch for opportunities or questions:

- 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
- 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
- 📧 antonio@javadeveloper.com.br

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)