# NGINX with Docker – Branch: `01-server-basics`

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

This branch presents a basic and modular architecture to configure and run virtual servers with NGINX using Docker. The structure is designed to facilitate local development, configuration testing, and simulation of common scenarios such as:

- Static HTTP servers  
- Virtual hosts (multiple servers)  
- Configuration file organization  
- Dynamic server inclusion via `include`  
- Clear separation between code, configuration, and infrastructure  

---

## 📦 Project Structure

```
.
├── docker-compose.yml             # Orchestrates the NGINX container
├── settings/                      # NGINX configurations
│   ├── nginx.conf                 # Main configuration
│   └── servers/                   # Virtual hosts (included by nginx.conf)
│       ├── .gitkeep               # Keeps the directory under version control
│       └── server1.conf           # Example server on port 8080
└── web/                           # Static content served by NGINX
    ├── html/                      # Served at root (port 80)
    │   └── index.html
    └── server1/                   # Served on port 8080
        └── index.html
    └── error/                     # Custom error pages (40x, 50x)
        └── error40x.html
        └── error50x.html
```

---

## 🚀 How to Run

1. Start the containers:
   ```bash
   docker compose up -d
   ```

2. Open your browser:

   - [http://localhost](http://localhost) → Content from `web/html` directory  
   - [http://localhost:8080](http://localhost:8080) → Content from `web/server1` directory  

3. Test the configuration:
   ```bash
   docker compose exec nginx nginx -t
   ```

---

## 🔧 Customization

- Add new servers by creating `.conf` files in `settings/servers/`.
- For each server, assign a new port in `docker-compose.yml`, if necessary.
- Volumes are mounted to allow hot-reload without rebuilding the image.
- You can define custom error pages for HTTP errors (such as 400, 401, 404, 500, etc.) using `error_page` directives. An example is provided with the `error40x.html` and `error50x.html` files served from a dedicated directory.

---

## ✅ Requirements

- Docker 20+  
- Docker Compose 2+  

---

## 📝 Notes

- Empty directories are preserved using `.gitkeep` to ensure versioning.
- The `nginx:1.26` image is explicitly used for predictability.
- This content is tied to the **`01-server-basics` branch**, which documents the initial configuration and structure based on NGINX fundamentals.

---

**This environment serves as a base for testing and experimenting with NGINX configurations.**

---

## 📫 Contact

If you’d like to get in touch for opportunities or questions:

- 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)  
- 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)  
- 📧 antonio@javadeveloper.com.br  

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)