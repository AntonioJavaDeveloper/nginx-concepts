# 📊 Custom Logging in NGINX with Java, Laravel, and Docker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

NGINX automatically logs all requests it handles, as well as any errors encountered during processing. These logs are essential for understanding application behavior, identifying bottlenecks, investigating issues, and ensuring observability in production environments. With minimal configuration, it's possible to customize what gets logged and how that information is displayed.

In addition to the default log files (`access.log` and `error.log`), NGINX allows you to define custom log formats with variables that show client IP, backend response time, accessed URI, status code, and much more. Logs can also be redirected to the container’s standard output, integrating with tools like ELK or Grafana Loki — a best practice in modern container-based and microservices architectures.

---

## 🏗️ Architecture Diagram

![Architecture Diagram](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

---

## 🗂️ Project Structure

```txt
.
├── docker-compose.yml                 # Container orchestration (NGINX, Java, Laravel)
├── java/                              # Backend project in Spring Boot (game-list-api)
│   ├── games1/                        # Java service with game API 1
│   ├── games2/                        # Java service with game API 2
│   └── games3/                        # Java service with game API 3
├── php/                               # Main directory for PHP projects
│   └── laravel1/                      # Backend project in PHP (Laravel)
├── settings/                          # NGINX configurations
│   ├── nginx.conf                     # Main configuration
│   └── servers/                       # Individual virtual hosts
│       ├── proxy-reverse.conf         # Smart routing on the gateway
│       ├── server1-html.conf          # Static HTML
│       ├── server2-css.conf           # Static CSS
│       ├── nginx-laravel.conf         # Internal communication with Laravel
│       └── nginx-java-balancer.conf   # Custom log setup for detailed request analysis at the load balancer
└── web/
    ├── html/                          # Static HTML
    ├── server1/                       # Served by server1-html
    ├── server2/                       # Served by server2-css
    └── error/                         # Custom error pages
        ├── error40x.html
        └── error50x.html
```

...

(Truncated for brevity; full content can be generated again if needed)
