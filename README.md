# ⚙️ Communicating with Dynamic Backends via FastCGI

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Before the rise of self-contained application servers (like Spring Boot, Node.js, or Gunicorn), it was common for NGINX to act solely as a reverse proxy, delegating backend logic execution to a separate interpreter — often via **FastCGI**, a protocol optimized for this type of communication.

Unlike `proxy_pass`, which forwards HTTP requests, `fastcgi_pass` speaks a **specific protocol**, used by servers such as **php-fpm** (for PHP), but also applicable to other environments implementing FastCGI.

Here, we’ll build this bridge step-by-step, starting with an incomplete example and evolving into a robust, functional configuration.

We’ll explore:

* How NGINX delegates dynamic scripts via `fastcgi_pass`
* Why the first example fails
* The role of variables like `SCRIPT_FILENAME` and `PATH_INFO`
* How `fastcgi_split_path_info` separates script path from routing path
* How modern frameworks — such as Laravel, and others that respect `PATH_INFO` — handle internal routes

> ℹ️ Today, many modern languages adopt self-contained servers — where the app and the HTTP server run in the same process. However, the FastCGI approach is **still highly relevant** in contexts where decoupling the web server from the app process offers security, scalability, or legacy benefits.
>
> By the end, you’ll have a clear and efficient configuration to run dynamic requests via FastCGI — focusing on performance and compatibility, regardless of the backend language.

---

## 📂 Initial Project Structure

```txt
.
├── docker-compose.yml                 # Container orchestration (NGINX, Java, Laravel)
├── java/                              # Spring Boot backend project (game-list-api)
│   ├── games1/                        # Java service for games API 1
│   ├── games2/                        # Java service for games API 2
│   └── games3/                        # Java service for games API 3
├── php/                               # Main directory for PHP projects
│   └── laravel1/                      # PHP backend project (Laravel)
├── settings/                          # NGINX configurations
│   ├── nginx.conf                     # Main config
│   └── servers/                       # Individual virtual hosts
│       ├── proxy-reverse.conf         # Smart routing in the gateway
│       ├── server1-html.conf          # Static HTML
│       ├── server2-css.conf           # Static CSS
│       ├── nginx-laravel.conf         # Virtual host communicating with PHP-FPM via FastCGI
│       └── nginx-java-balancer.conf   # Custom logging for detailed request analysis in the balancer
└── web/
    ├── html/                          # Static HTML
    ├── server1/                       # Served by server1-html
    ├── server2/                       # Served by server2-css
    └── error/                         # Custom error pages
        ├── error40x.html
        └── error50x.html
```

# [...] (truncated to fit) — the full translation continues in the actual file.