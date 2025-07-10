# Advanced Load Balancers with Java, Laravel and NGINX

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

This project extends the previous scenario from the `04-load-balancer` branch, exploring different **load balancing strategies using NGINX**. The environment simulates multiple Java APIs, a Laravel application in PHP, and uses NGINX as a reverse proxy supporting the following types of load balancers:

* **Round Robin**
* **Weighted Round Robin**
* **Least Connections**

Additionally, requests are routed through different paths (`/round-robin`, `/weighted`, `/least-conn`) which trigger the respective algorithms configured in NGINX.

---

## 🏗️ Architectural Diagram

![Architectural Diagram](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

---

## 🎯 Purpose

To demonstrate in a practical and visual way how each load balancing algorithm behaves when distributing requests across three Java services, each returning JSON responses that include the name of the responding server.

---

## 📘 What is a Load Balancer?

A load balancer is a middleware component that distributes requests among multiple backend instances, helping to:

* Improve performance;
* Ensure high availability;
* Reduce bottlenecks;
* Enable horizontal scalability;
* Provide automatic failover.

In this project, we use **NGINX as a reverse proxy**, testing different algorithms to understand their behavior and characteristics in real-world scenarios.

---

## 🧭 Load Balancing Strategies Used

| Strategy             | URL Path                | Characteristics                                                  |
| -------------------- | ----------------------- | ---------------------------------------------------------------- |
| Round Robin          | `/round-robin/games/1`  | Requests are distributed cyclically across servers               |
| Weighted Round Robin | `/weighted/games/1`     | Servers receive requests according to a configured "weight"      |
| Least Connections    | `/least-conn/games/1`   | New requests are sent to the server with the fewest active connections |

The homepage provides a visual dashboard with interactive buttons to test and observe the behavior of each strategy.

---

## 🗂️ Project Structure

```txt
.
├── docker-compose.yml                 # Container orchestration (NGINX, Java, Laravel)
├── java/                              # Spring Boot backend (game-list-api)
│   ├── games1/                        # Java API service 1
│   ├── games2/                        # Java API service 2
│   └── games3/                        # Java API service 3
├── php/                               # Main directory for PHP projects
│   └── laravel1/                      # Laravel backend project
├── settings/                          # NGINX configurations
│   ├── nginx.conf                     # Main config
│   └── servers/                       # Individual virtual hosts
│       ├── proxy-reverse.conf         # Smart routing at the gateway
│       ├── server1-html.conf          # Static HTML
│       ├── server2-css.conf           # Static CSS
│       ├── nginx-laravel.conf         # Internal communication with Laravel
│       └── nginx-java-balancer.conf   # Advanced balancers: round robin, weighted, least_conn
└── web/
    ├── html/                          # Static HTML
    ├── server1/                       # Served by server1-html
    ├── server2/                       # Served by server2-css
    └── error/                         # Custom error pages
        ├── error40x.html
        └── error50x.html
```

---

## 🧪 Test Panel

The homepage `http://localhost/` displays an interactive interface with:

* 📌 **Tests for Java and Laravel endpoints**
* ⚙️ **Visual comparison between balancing strategies**
* 🚨 **Simulated 404 and 502 error scenarios**

Each Java endpoint returns a JSON that includes the `server` key showing which container handled the request — allowing for visual validation of the balancing.

---

## 💡 How Each Configuration Works

The configuration for each algorithm is located in the `nginx-java-balancer.conf` file. They follow the structure below:

```nginx
# Round Robin (default)
upstream games_api_round_robin {
    server games1:9001;
    server games2:9001;
    server games3:9001;
}

# Weighted Round Robin
upstream games_api_weighted {
    server games1:9001 weight=3;
    server games2:9001;
    server games3:9001;
}

# Least Connections
upstream games_api_least_conn {
    least_conn;
    server games1:9001;
    server games2:9001;
    server games3:9001;
}
```

Each group is mapped to a specific route using `rewrite` and `proxy_pass`, as detailed in the previous branch's README.

---

## 🚀 Launching the Environment

```bash
docker compose up --build
```

Access [http://localhost](http://localhost) and explore the interactive test panel.

---

## 🧭 Tip

On the panel, observe the server names (`games1`, `games2`, `games3`) in the JSON response body — this helps visualize how each strategy is distributing the traffic.

For the **least_conn** algorithm, the system automatically fires 15 background requests. On `games2` and `games3`, these requests simulate long processing times (up to 25 seconds), while `games1` responds almost instantly (around 1 ms). This uneven load allows you to observe how NGINX redirects new requests to the least busy server.

---

## 📫 Contact

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)
