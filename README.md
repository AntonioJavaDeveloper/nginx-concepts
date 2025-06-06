# API Gateway with NGINX + Java + Laravel

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

This project demonstrates a realistic setup with multiple services, including Java APIs, PHP applications (Laravel), and a reverse proxy NGINX that performs smart routing based on request paths.

Additionally, the system consumes the [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) project as backend, and now also introduces a Laravel application served internally, reinforcing the gateway's flexibility with multiple technologies.

---

## 🏗️ Architectural Diagram

![Architectural Diagram](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/03-gateway.png)

---

## 📘 What is an API Gateway?

An API Gateway is a server that acts as an entry point to multiple backend services. It:

* Redirects requests to the correct services
* Can rewrite URLs
* Handles custom errors
* Serves static or dynamic content

In this project, NGINX is used as an API Gateway to route requests between Docker containers.

---

## 📂 Routing Examples

Below are the paths routed by NGINX (API Gateway) as defined in `proxy-reverse.conf`. The table includes internal destinations, ports, and important notes on URL rewriting and service communication.

| URL Path                          | Destination (Container) | Port | Content Type / Notes                                                                 |
|----------------------------------|--------------------------|------|----------------------------------------------------------------------------------------|
| `/`                              | `server1`                | 8081 | Redirects to `/index.html` (static HTML)                                              |
| `/error`                         | `server1`                | 1000 | Purposely invalid port to trigger custom error page                                   |
| `/laravel`                       | `nginx-laravel`          | 8083 | NGINX forwards the request to internal `laravel1` server, rewriting `/laravel/abc` → `/abc` |
| `/api/**`                        | `games1`                 | 9001 | Rewrites `/api/...` to `/...` (e.g., `/api/games` → `/games`)                        |
| `*.html`                         | `server1`                | 8081 | Any `.html` file is served by the static HTML container                               |
| `*.css`                          | `server2`                | 8082 | Any `.css` file is served by the static CSS container                                 |
| `/error40x.html`, `/error50x.html` | `nginx` (local)         | -    | Error files served directly from the NGINX container using `/usr/share/nginx/error` directory |

> 🔄 The `/laravel` path **does not** access the PHP backend (`laravel1:9000`) directly. Instead, it goes through an intermediate NGINX (`nginx-laravel:8083`), which handles the URLs and executes PHP scripts using **FastCGI**.

> 🧠 The `rewrite` usage in the `/api` block is essential to remove the `/api` prefix before forwarding to Spring Boot, ensuring the backend receives the correct URL.

---

## 📌 Main Endpoints (Java) + Laravel

Below are the main endpoints accessible through the gateway:

| Method | Path                                   | Description                             |
|--------|----------------------------------------|-----------------------------------------|
| GET    | `/api/games`                           | Lists all games                         |
| GET    | `/api/games/{id}`                      | Returns details of a game               |
| GET    | `/api/lists`                           | Lists all game lists                    |
| GET    | `/api/lists/{listId}/games`            | Lists games of a specific list          |
| -      | `http://localhost/laravel`             | Laravel home page                       |

> These endpoints come from the `http://games1:9001` server, implemented in Java. If a load balancer is present, the `"server"` key in the JSON response indicates which backend instance responded to the request.

> The request to the Laravel system first reaches the server at `http://nginx-laravel:8083`. This server forwards the request to `http://laravel1:9000` (PHP/Laravel server). **All servers are inaccessible outside the Docker context.**

---

## 🚫 Main Errors

The table below summarizes key error scenarios identified in the NGINX reverse proxy setup. HTTP codes are handled differently depending on the source service. In all cases, the final NGINX proxy server is responsible for delivering the corresponding HTML error page to the client, discarding any custom content from intermediate servers.

| Path               | Description |
|--------------------|-------------|
| [/abc](/abc)       | **404 Error** - Unknown address. Error is captured and returned directly to the client by the proxy server `http://nginx`, which delivers the default 404 error HTML page. |
| [/api/abc](/api/abc) | **404 Error** - Unknown address. Error is captured by `http://games1:9001`, passed to the proxy `http://nginx`, which responds to the client with the default HTML error page. |
| [/laravel/abc](/laravel/abc) | **404 Error** - Unknown address. Error handled by `http://laravel1:9000` with a custom HTML page. This response is forwarded to the web server `http://nginx-laravel:8083`, which passes it to the proxy `http://nginx`. The proxy ignores the custom HTML and sends the default 404 error page to the client. |
| [/error](/error)   | **502 Error** - Bad Gateway. The request sent to `http://server1:1000` fails, since the server listens on port 8081. The proxy `http://nginx` returns the default 502 error HTML page. |

> **ℹ️ Note:** All error pages are delivered to the client by the proxy server (`http://nginx`). Even if an intermediate server provides a custom HTML body, it will be ignored and replaced by the proxy’s default error page.

---

## 🗂️ Project Structure

```txt
.
├── docker-compose.yml                 # Container orchestration (NGINX, Java, Laravel)
├── java/                              # Spring Boot backend project (game-list-api)
├── php/                               # Main directory for PHP projects
│   ├── laravel1/                      # PHP backend project (Laravel)
├── settings/                          # NGINX configurations
│   ├── nginx.conf                     # Main configuration
│   └── servers/                       # Individual virtual hosts
│       ├── proxy-reverse.conf         # Smart routing in the gateway
│       ├── server1-html.conf          # Static HTML
│       ├── server2-css.conf           # Static CSS
│       └── nginx-laravel.conf         # Internal communication with Laravel
└── web/
    ├── html/                          # Static HTML
    ├── server1/                       # Served by server1-html
    ├── server2/                       # Served by server2-css
    └── error/                         # Custom error pages
        ├── error40x.html
        └── error50x.html
```

---

## 🚀 How to Run

1. Start the containers:

   ```bash
   docker compose up -d
   ```

2. Access services via browser:

    * [http://localhost](http://localhost) → HTML content via API Gateway
    * [http://localhost/style.css](http://localhost/style.css) → CSS via API Gateway
    * [http://localhost/api/games](http://localhost/api/games) → Java API (Spring)
    * [http://localhost/laravel](http://localhost/laravel) → Laravel home interface

3. Test NGINX configuration:

   ```bash
   docker compose exec nginx nginx -t
   ```

---

## 🔧 Customization

To expand or change the architecture:

* ⚠️ **Edit `nginx.conf`** for new routes or proxy logic.

* ➕ **Add new servers** with `.conf` files in `settings/servers/`.

* 🔀 **Map new ports** via `docker-compose.yml` to ease internal testing.

* 🧩 **Mapped volumes** allow config reload without rebuild.

* ❗ **Reload configuration** after changes:

  ```bash
  docker compose exec nginx nginx -s reload
  ```

* 🧱 **Custom errors** in `web/error` are served automatically:

    * `40x` → `error40x.html`
    * `50x` → `error50x.html`

---

## 📎 Technical References

Below are some technologies and resources used in this repository:

* **NGINX as Reverse Proxy**
  [https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)

* **Docker Compose for multi-container**
  [https://docs.docker.com/compose/](https://docs.docker.com/compose/)

* **Spring Boot with `@ControllerAdvice` and `ResponseBodyAdvice`**

    * Error interception and response customization:
      [https://www.baeldung.com/spring-controller-advice](https://www.baeldung.com/spring-controller-advice)
      [https://www.baeldung.com/spring-responsebodyadvice](https://www.baeldung.com/spring-responsebodyadvice)

* **HTTP Headers with `proxy_set_header` in NGINX**
  [https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header)

* **MIME files with `mime.types`**
  [https://nginx.org/en/docs/http/ngx_http_core_module.html#include](https://nginx.org/en/docs/http/ngx_http_core_module.html#include)

* **Error handling in NGINX (`error_page`)**
  [https://nginx.org/en/docs/http/ngx_http_core_module.html#error_page](https://nginx.org/en/docs/http/ngx_http_core_module.html#error_page)

---

**This environment serves as a testing and experimentation base for reverse proxy practices, static content separation, and dynamic APIs.**

---

## 📫 Contact

If you want to get in touch for opportunities or questions:

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)