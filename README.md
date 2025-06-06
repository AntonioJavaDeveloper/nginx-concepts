# Java Load Balancing and API Gateway Architecture with NGINX and Laravel

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

This project demonstrates a realistic setup with multiple services, including Java APIs, PHP applications (Laravel), and a reverse NGINX gateway with **load balancing across multiple Java backend instances**.

Additionally, the system consumes the [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) project as a backend, and also integrates a Laravel application served internally—showcasing the gateway's flexibility with multiple technologies.

---

## 🏗️ Architectural Diagram

![Architectural Diagram](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

---

## 📘 What is an API Gateway?

An API Gateway is a server that acts as an entry point for multiple backend services. It:

* Redirects requests to the correct services
* Can rewrite URLs
* Performs load balancing
* Handles custom errors
* Serves static or dynamic content

In this project, NGINX is used both as a gateway and as a load balancer for the Java APIs (`games1`, `games2`, `games3`).

---

## 🗂️ Project Structure

```txt
.
.
├── docker-compose.yml                 # Container orchestration (NGINX, Java, Laravel)
├── java/                              # Spring Boot backend (game-list-api)
│   ├── games1/                        # Java service for game API 1
│   ├── games2/                        # Java service for game API 2
│   └── games3/                        # Java service for game API 3
├── php/                               # PHP project directory
│   ├── laravel1/                      # PHP backend project (Laravel)
├── settings/                          # NGINX configuration
│   ├── nginx.conf                     # Main config
│   └── servers/                       # Individual virtual hosts
│       ├── proxy-reverse.conf         # Smart routing via gateway
│       ├── server1-html.conf          # Static HTML
│       ├── server2-css.conf           # Static CSS
│       └── nginx-laravel.conf         # Internal Laravel communication
│       └── nginx-java-balancer.conf   # Load balancing for Java services (games1, games2, games3)
└── web/
    ├── html/                          # Static HTML content
    ├── server1/                       # Served by server1-html
    ├── server2/                       # Served by server2-css
    └── error/                         # Custom error pages
        ├── error40x.html
        └── error50x.html
```

---

## 📦 New: Load Balancing with NGINX

Now, the `/api/**` endpoint doesn't point directly to the `games1` container. Instead, a new service called `nginx-java-balancer` was created to perform **load balancing among the three Java services**:

* `games1` (internal port 9001)
* `games2` (internal port 9001)
* `games3` (internal port 9001)

These services are balanced using the `upstream` block in `nginx-java-balancer.conf`.

```nginx
upstream games_api {
    server games1:9001;
    server games2:9001;
    server games3:9001;
}
```

All `/api/**` requests reach the main proxy and are then forwarded to `nginx-java-balancer`, which distributes them evenly across the three Java containers.

---

## 🛠️ Java Service Configuration

Each service (`games1`, `games2`, `games3`) is configured with:

* `server.port=9001`
* `server.name=games1`, `games2`, or `games3` (to identify the response source)
* `spring.profiles.active`, `cors.origins`, and other common properties

Example API response:

```json
{
  "server": "games2",
  "data": {
    "id": 1,
    "title": "Mass Effect Trilogy"
  }
}
```

This allows you to easily verify whether the load balancer is working—each request might return a different service (`games1`, `games2`, or `games3`).

---

## 📂 Routing Examples

### Updated Configuration (`proxy-reverse.conf`):

| URL Path                           | Destination (Container) | Port | Notes                                                                 |
| ---------------------------------- | ----------------------- | ---- | --------------------------------------------------------------------- |
| `/api/**`                          | `nginx-java-balancer`   | 8080 | Load balancing between `games1`, `games2`, `games3`, with URL rewrite |
| `/laravel`                         | `nginx-laravel`         | 8083 | Laravel app served by intermediary NGINX, with path rewrite           |
| `/`, `*.html`                      | `server1`               | 8081 | Static HTML content                                                   |
| `*.css`                            | `server2`               | 8082 | Static CSS content                                                    |
| `/error40x.html`, `/error50x.html` | `nginx` (local)         | -    | Error files served directly from NGINX                                |

> 🔄 The use of `rewrite ^/api(/.*)$ $1 break;` in the `/api` block is **essential** to remove the `/api` prefix before forwarding the request to the Java backend.

---

## 🌐 Docker Networks: Organized and Secure Communication

The architecture has been improved by separating services into distinct networks as defined in the `docker-compose.yml`. Each group of services communicates only with the services it truly needs, thanks to explicit Docker network definitions.

This network isolation greatly improves:

* **🔒 Security**: services that don't need to talk are completely isolated, reducing the attack surface
* **🧹 Organization**: clearer architecture and dependency tracing
* **🚀 Performance and scalability**: avoids unnecessary traffic and improves system efficiency
* **🛠️ Debugging**: easier to identify communication issues
* **📦 DevOps Best Practice**: widely recommended approach in production environments with multiple containers

### 🔗 Networks and Services

| Network          | Associated Services                                                             |
| ---------------- | ------------------------------------------------------------------------------- |
| `reverse-proxy`  | `nginx`, `nginx-java-balancer`, `nginx-laravel`, `server1-html`, `server2-css`  |
| `static-content` | `server1-html`, `server2-css`                                                   |
| `java`           | `nginx-java-balancer`, `games1-app`, `games2-app`, `games3-app`                 |
| `laravel`        | `nginx-laravel`, `laravel1`                                                     |
| `default`        | Not used — all services are explicitly connected to their own specific networks |

> ℹ️ **Fun fact:** in the reverse proxy, `server1-html` is responsible for serving `*.html` files, while `server2-css` handles `*.css`, neatly organizing static content.

---

## 📌 Key Endpoints

| Method | Path                        | Description                    |
| ------ | --------------------------- | ------------------------------ |
| GET    | `/api/games`                | Lists all games                |
| GET    | `/api/games/{id}`           | Returns game details           |
| GET    | `/api/lists`                | Lists all game lists           |
| GET    | `/api/lists/{listId}/games` | Lists games in a specific list |
| -      | `http://localhost/laravel`  | Laravel homepage               |

---

## 🧪 Testing Load Balancing

Make multiple requests to the `/api/games` endpoint. You should observe that the `"server"` field in the response alternates between `games1`, `games2`, and `games3`, confirming that the load balancer is working.

---

## 🚀 Running the Containers

```bash
docker compose up --build
```

Make sure all ports and volumes are properly configured.

---

**This environment is designed as a testing and experimentation platform for reverse proxy practices, static content separation, and dynamic API management.**

---

## 📩 Contact

For opportunities or questions:

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)
