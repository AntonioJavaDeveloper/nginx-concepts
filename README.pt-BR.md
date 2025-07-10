# Balanceadores de Carga Avançados com Java, Laravel e NGINX

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Este projeto estende o cenário anterior da branch `04-load-balancer`, explorando diferentes **estratégias de balanceamento de carga com NGINX**. O ambiente simula múltiplas APIs em Java, uma aplicação Laravel em PHP, e utiliza o NGINX como gateway reverso com suporte aos seguintes tipos de balanceadores:

* **Round Robin**
* **Weighted Round Robin**
* **Least Connections**

Além disso, as requisições são roteadas por caminhos distintos (`/round-robin`, `/weighted`, `/least-conn`) que acionam os respectivos algoritmos de balanceamento definidos no NGINX.

---

## 🏗️ Diagrama Arquitetural

![Diagrama Arquitetural](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

---

## 🎯 Objetivo

Demonstrar de forma prática e visual como cada algoritmo de balanceamento se comporta ao distribuir requisições entre três serviços Java com respostas JSON que incluem o nome do servidor que respondeu.

---

## 📘 O que é um Balanceador de Carga?

Um balanceador de carga é um componente intermediário que distribui requisições entre múltiplas instâncias backend, ajudando a:

* Melhorar a performance;
* Garantir alta disponibilidade;
* Reduzir gargalos;
* Permitir escalabilidade horizontal;
* Realizar failover automático.

Neste projeto, utilizamos o **NGINX como balanceador reverso**, testando diferentes algoritmos para entender suas características e comportamentos em cenários reais.

---

## 🧭 Estratégias de Balanceamento Adotadas

| Estratégia           | Caminho da URL         | Características                                                 |
| -------------------- | ---------------------- | --------------------------------------------------------------- |
| Round Robin          | `/round-robin/games/1` | Requisições distribuídas ciclicamente entre os servidores       |
| Weighted Round Robin | `/weighted/games/1`    | Servidores recebem requisições conforme seu "peso" configurado  |
| Least Connections    | `/least-conn/games/1`  | Novas requisições vão para o servidor com menos conexões ativas |

A página inicial do projeto oferece um painel visual com botões interativos para testar e visualizar os endpoints funcionando com cada estratégia.

---

## 🗂️ Estrutura do Projeto

```txt
.
├── docker-compose.yml                 # Orquestração dos containers (NGINX, Java, Laravel)
├── java/                              # Projeto backend em Spring Boot (game-list-api)
│   ├── games1/                        # Serviço Java com API de jogos 1
│   ├── games2/                        # Serviço Java com API de jogos 2
│   └── games3/                        # Serviço Java com API de jogos 3
├── php/                               # Diretório principal dos projetos PHP
│   └── laravel1/                      # Projeto backend em PHP (Laravel)
├── settings/                          # Configurações NGINX
│   ├── nginx.conf                     # Configuração principal
│   └── servers/                       # Virtual hosts individuais
│       ├── proxy-reverse.conf         # Roteamento inteligente no gateway
│       ├── server1-html.conf          # HTML estático
│       ├── server2-css.conf           # CSS estático
│       ├── nginx-laravel.conf         # Comunicação interna com Laravel
│       └── nginx-java-balancer.conf   # Balanceadores avançados: round robin, weighted, least_conn
└── web/
    ├── html/                          # HTML estático
    ├── server1/                       # Servido por server1-html
    ├── server2/                       # Servido por server2-css
    └── error/                         # Páginas de erro personalizadas
        ├── error40x.html
        └── error50x.html
```

---

## 🧪 Painel de Testes

A página inicial `http://localhost/` exibe uma interface interativa com:

* 📌 **Testes dos endpoints Java e Laravel**
* ⚙️ **Comparação visual entre estratégias de balanceamento**
* 🚨 **Simulações de erros 404 e 502**

Cada endpoint Java retorna um JSON que inclui a chave `server` com o nome do container que respondeu, permitindo validar visualmente o balanceamento.

---

## 💡 Como funciona cada configuração

As configurações de cada algoritmo estão no arquivo `nginx-java-balancer.conf`. Elas seguem a estrutura:

```nginx
# Round Robin (padrão)
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

Cada grupo é mapeado em rotas distintas com `rewrite` e `proxy_pass`, conforme explicado detalhadamente no README da branch anterior.

---

## 🚀 Subindo o ambiente

```bash
docker compose up --build
```

Acesse [http://localhost](http://localhost) e explore os testes no painel interativo.

---

## 🧭 Dica

No painel, observe os nomes dos servidores (`games1`, `games2`, `games3`) no corpo das respostas JSON — isso evidencia como cada estratégia está distribuindo as requisições.

Para o algoritmo least_conn, o sistema dispara automaticamente 15 requisições em segundo plano. Nos servidores games2 e games3, essas requisições simulam alto tempo de resposta (até 25 segundos), enquanto no games1 elas são quase instantâneas (cerca de 1 ms). Isso cria uma carga desigual, permitindo observar como o NGINX redireciona novas requisições para o servidor menos sobrecarregado.

---

## 📫 Contato

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Desenvolvido por [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)
