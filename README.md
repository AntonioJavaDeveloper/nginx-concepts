# NGINX with Docker – Branch: `03-reverse-proxy`

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

This branch presents an architecture with multiple NGINX servers, including a reverse proxy configured to dynamically route requests based on URL patterns. Although the examples involve static files (such as HTML and CSS) or dynamic content served by a Java application, the focus is to demonstrate that the strategy is entirely technology-agnostic — it can be applied equally to applications in PHP, Node.js, Ruby on Rails, or any other stack. This structure simulates a realistic scenario of separation of concerns between frontend and backend, with a central server intelligently and efficiently managing traffic.

Additionally, the repository consumes the [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) project as a backend service. This Java system was adapted to return data in a standardized response format, thanks to the addition of:

* `@ControllerAdvice` – captures and handles exceptions globally;
* `ResponseBodyAdvice` – intercepts responses before they are sent to the client;
* `@Value` – reads configurations from `application.properties`, such as the application name, and inserts them automatically into the JSON responses.

This standardization was designed to make it explicit which backend instance or service is providing the response, which is essential when there are multiple replicas or services behind a load balancer. The `"server"` field in the response facilitates tracking and debugging.

### 🔍 Example JSON Response

```json
{
  "server": "games1",
  "data": {
    "id": 1,
    "title": "s1 - Mass Effect Trilogy",
    "year": 2012,
    "genre": "Role-playing (RPG), Shooter",
    "platforms": "XBox, Playstation, PC",
    "score": 4.8,
    "imgUrl": "https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/game-list-api/images/1.png",
    "shortDescription": "Lorem ipsum dolor sit amet consectetur adipisicing elit. Odit esse officiis corrupti unde repellat non quibusdam! Id nihil itaque ipsum!",
    "longDescription": "Lorem ipsum dolor sit amet consectetur adipisicing elit. Delectus dolorum illum placeat eligendi, quis maiores veniam. Incidunt dolorum, nisi deleniti dicta odit voluptatem nam provident temporibus reprehenderit blanditiis consectetur tenetur. Dignissimos blanditiis quod corporis iste, aliquid perspiciatis architecto quasi tempore ipsam voluptates ea ad distinctio, sapiente qui, amet quidem culpa."
  }
}
```

The `"server"` field is automatically populated based on the `application.name` property defined in `application.properties`, allowing each backend instance to identify itself transparently.

---

## 🏗️ Diagrama Arquitetural

![Diagrama Arquitetural](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/02-proxy-reverse-java.png)

---

## 📦 Project Structure

```
.
├── docker-compose.yml                  # Orchestration of NGINX + Java containers
├── java/                               # Spring Boot backend project (based on game-list-api)
├── settings/                           # NGINX configurations
│   ├── nginx.conf                      # Main configuration
│   └── servers/                        # Individual virtual hosts
│       ├── proxy-reverse.conf          # Main server with routing logic
│       ├── server1-html.conf           # Static HTML server
│       └── server2-css.conf            # Static CSS server
└── web/                                # Static content
    ├── html/                           # Basic HTML (index.html)
    ├── server1/                        # Served by server1-html
    ├── server2/                        # Served by server2-css
    └── error/                          # Custom error pages
        ├── error40x.html
        └── error50x.html
```

---

## 🚀 How to Run

1. Start the containers:

   ```bash
   docker compose up -d
   ```

2. Access in your browser:

    * [http://localhost](http://localhost) → HTML content via reverse proxy
    * [http://localhost/style.css](http://localhost/style.css) → CSS file via reverse proxy
    * [http://localhost/games](http://localhost/games) → Java backend API via reverse proxy

3. Test NGINX configuration:

   ```bash
   docker compose exec nginx nginx -t
   ```

---

## 🔧 Customization

To expand the architecture with new servers or modify the reverse proxy behavior:

* ⚠️ **Edit the main routing file**: every new route or proxy rule change must be reflected in `settings/nginx.conf`, which acts as the central distribution point for requests.

* ➕ **Add new servers** by creating `.conf` files in the `settings/servers/` directory, defining each server’s behavior.

* 🔀 **Associate a new port** in `docker-compose.yml` to the new service, if necessary. Example of an additional service:

  ```yaml
  server3:
    image: nginx:1.26
    container_name: server3-js
    expose:
      - "8083"
    volumes:
      - ./settings/nginx.conf:/etc/nginx/nginx.conf
      - ./settings/servers/server3-js.conf:/etc/nginx/servers/server3-js.conf
      - ./web/server3:/usr/share/nginx/server3
      - ./web/error:/usr/share/nginx/error
    restart: unless-stopped
  ```

* 🧩 **Volumes are mounted** to allow hot-reload of configurations without rebuilding the image.

* ❗ **After changes** in the router (`nginx.conf`) or adding new services, restart the routing process by executing:

  ```bash
  docker compose exec nginx nginx -s reload
  ```

* 🧱 **Custom HTTP error pages** (such as 404 or 502) are served directly from the `web/error` directory.

---

## 📂 Routing Examples

The reverse proxy behavior is defined in the `proxy-reverse.conf` file, which inspects the path and/or resource extension to decide where to forward the request:

| URL Path                           | Destination (Container) | Port | Content Type            |
| ---------------------------------- | ----------------------- | ---- | ----------------------- |
| `/` or `/index.html`               | `server1-html`          | 8081 | Main HTML page          |
| `/anything.html`                   | `server1-html`          | 8081 | Any static HTML         |
| `/style.css` or `/assets/main.css` | `server2-css`           | 8082 | CSS files               |
| `/games`, `/api/*`, `/others`      | `games1-app`            | 9001 | Java REST API (Spring)  |
| `/nonexistent`                     | `nginx`                 | -    | Redirected to 40x error |

Also, the custom error files located in `web/error` are used whenever errors like the following occur:

* `400`, `401`, `404` → `error40x.html`
* `500`, `502`, `503`, `504` → `error50x.html`

---

## ✅ Requirements

* Docker 20+
* Docker Compose 2+

---

## 📎 Technical References

Below are some technologies and resources used in this repository:

* **NGINX as Reverse Proxy**
  [https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)

* **Docker Compose for multi-container orchestration**
  [https://docs.docker.com/compose/](https://docs.docker.com/compose/)

* **Spring Boot with `@ControllerAdvice` and `ResponseBodyAdvice`**

    * Error interception and response customization:
      [https://www.baeldung.com/spring-controller-advice](https://www.baeldung.com/spring-controller-advice)
      [https://www.baeldung.com/spring-responsebodyadvice](https://www.baeldung.com/spring-responsebodyadvice)

* **HTTP headers with `proxy_set_header` in NGINX**
  [https://nginx.org/en/docs/http/ngx\_http\_proxy\_module.html#proxy\_set\_header](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header)

* **MIME files with `mime.types`**
  [https://nginx.org/en/docs/http/ngx\_http\_core\_module.html#include](https://nginx.org/en/docs/http/ngx_http_core_module.html#include)

* **Error handling in NGINX (`error_page`)**
  [https://nginx.org/en/docs/http/ngx\_http\_core\_module.html#error\_page](https://nginx.org/en/docs/http/ngx_http_core_module.html#error_page)

---

**This environment serves as a testing and experimentation base for reverse proxy practices, static content separation, and dynamic APIs.**

---

## 📫 Contact

If you want to get in touch for opportunities or questions:

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)
