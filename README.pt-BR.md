# API Gateway com NGINX + Java + Laravel

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Este projeto demonstra um cenário realista com múltiplos serviços, incluindo APIs em Java, aplicações PHP (Laravel), e um gateway reverso NGINX que realiza o roteamento inteligente com base nos caminhos de requisição.

Além disso, o sistema consome o projeto [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) como backend, e agora também introduz uma aplicação Laravel servida internamente, reforçando a flexibilidade de uso do gateway com múltiplas tecnologias.

---

## 🏗️ Diagrama Arquitetural

![Diagrama Arquitetural](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/03-gateway.png)

---

## 📘 O que é um API Gateway?

Um API Gateway é um servidor que atua como ponto de entrada para múltiplos serviços backend. Ele:

* Redireciona requisições para os serviços corretos
* Pode reescrever URLs
* Trata erros personalizados
* Serve conteúdos estáticos ou dinâmicos

No contexto deste projeto, o NGINX é usado como API Gateway para rotear requisições entre containers Docker.

---

## 📂 Exemplos de Roteamento

A seguir estão os caminhos roteados pelo NGINX (API Gateway) conforme definidos em `proxy-reverse.conf`. A tabela inclui os destinos internos, portas e observações importantes sobre reescrita de URL e comunicação entre serviços.

| Caminho da URL                     | Destino (Container) | Porta | Tipo de Conteúdo / Observações                                                                     |
| ---------------------------------- | ------------------- | ----- | -------------------------------------------------------------------------------------------------- |
| `/`                                | `server1`           | 8081  | Redireciona para `/index.html` (HTML estático)                                                     |
| `/error`                           | `server1`           | 1000  | Porta inválida proposital para acionar página de erro personalizada                                |
| `/laravel`                         | `nginx-laravel`     | 8083  | NGINX encaminha a requisição ao servidor interno `laravel1`. Reescrevendo `/laravel/abc` → `/abc`  |
| `/api/**`                          | `games1`            | 9001  | Reescreve `/api/...` para `/...` (ex: `/api/games` → `/games`)                                     |
| `*.html`                           | `server1`           | 8081  | Qualquer arquivo `.html` é servido pelo container de HTML estático                                 |
| `*.css`                            | `server2`           | 8082  | Qualquer arquivo `.css` é servido pelo container de CSS estático                                   |
| `/error40x.html`, `/error50x.html` | `nginx` (local)     | -     | Arquivos de erro servidos diretamente do container NGINX usando diretório `/usr/share/nginx/error` |

> 🔄 O caminho `/laravel` **não** acessa diretamente o backend PHP (`laravel1:9000`). Em vez disso, ele passa por um NGINX intermediário (`nginx-laravel:8083`), que trata as URLs e executa os scripts PHP por meio do **FastCGI**.

> 🧠 O uso de `rewrite` no bloco `/api` é essencial para remover o prefixo `/api` antes de encaminhar ao Spring Boot, garantindo que o backend receba a URL no formato correto.

---

## 📌 Endpoints principais das APIs Java e da aplicação Laravel

Abaixo estão listados os principais endpoints acessíveis através do gateway:


| Método | Caminho                                  | Descrição                                |
|--------|------------------------------------------|------------------------------------------|
| GET    | `/api/games`                             | Lista todos os jogos                     |
| GET    | `/api/games/{id}`                        | Retorna detalhes de um jogo              |
| GET    | `/api/lists`                             | Lista todas as listas de jogos           |
| GET    | `/api/lists/{listId}/games`              | Lista os jogos de uma lista específica   |
| -      | `http://localhost/laravel`               | Página inicial do Laravel                |


> Os endpoints acima provêm do servidor `http://games1:9001`, implementado em Java. Se houver um balanceador de carga no ambiente, a chave `"server"` presente nas respostas JSON indicará qual instância de backend atendeu a requisição.

> A requisição ao sistema em Laravel chega primeiramente ao servidor `http://nginx-laravel:8083`. Esse servidor encaminha a requisição ao servidor `http://laravel1:9000` (servidor PHP/Laravel). **Todos os servidores estão inacessíveis fora do contexto Docker.**


---

## 🚫 Erros principais

A tabela abaixo resume os principais cenários de erro identificados no ambiente com proxy reverso NGINX. Os códigos HTTP são tratados de diferentes formas, dependendo do serviço de origem. Em todos os casos, o servidor proxy NGINX final é responsável por devolver ao cliente a página HTML correspondente ao erro, descartando qualquer conteúdo personalizado dos servidores intermediários.


| Caminho            | Descrição |
|--------------------|-----------|
| [/abc](/abc)       | **Erro 404** - Endereço desconhecido. Erro capturado e entregue diretamente ao cliente pelo servidor proxy `http://nginx`, que devolve a página HTML padrão de erro 404. |
| [/api/abc](/api/abc) | **Erro 404** - Endereço desconhecido. Erro capturado pelo serviço `http://games1:9001`, repassado ao proxy `http://nginx`, que responde ao cliente com a página de erro HTML padrão. |
| [/laravel/abc](/laravel/abc) | **Erro 404** - Endereço desconhecido. Erro tratado por `http://laravel1:9000` com uma página HTML personalizada. Essa resposta é repassada ao servidor web `http://nginx-laravel:8083`, que a encaminha ao proxy `http://nginx`. O proxy ignora o HTML personalizado e envia ao cliente sua página padrão de erro 404. |
| [/error](/error)   | **Erro 502** - Bad Gateway. A requisição enviada ao `http://server1:1000` falha, pois o servidor escuta na porta 8081. O proxy `http://nginx` devolve ao cliente a página HTML padrão de erro 502. |


> **ℹ️ Nota:** Todas as páginas de erro são entregues ao cliente pelo servidor proxy (`http://nginx`). Mesmo que um servidor intermediário forneça uma página personalizada no corpo da resposta, ela será ignorada e substituída pela versão padrão de erro do proxy.

---

## 🗂️ Estrutura do Projeto

```txt
.
.
├── docker-compose.yml                 # Orquestração dos containers (NGINX, Java, Laravel)
├── java/                              # Projeto backend em Spring Boot (game-list-api)
├── php/                               # Diretório principal dos projetos PHP
│   ├── laravel1/                      # Projeto backend em PHP (Laravel)
├── settings/                          # Configurações NGINX
│   ├── nginx.conf                     # Configuração principal
│   └── servers/                       # Virtual hosts individuais
│       ├── proxy-reverse.conf         # Roteamento inteligente no gateway
│       ├── server1-html.conf          # HTML estático
│       ├── server2-css.conf           # CSS estático
│       └── nginx-laravel.conf         # Comunicação interna com Laravel
└── web/
    ├── html/                          # HTML estático
    ├── server1/                       # Servido por server1-html
    ├── server2/                       # Servido por server2-css
    └── error/                         # Páginas de erro personalizadas
        ├── error40x.html
        └── error50x.html
```

---

## 🚀 Como executar

1. Suba os containers:

   ```bash
   docker compose up -d
   ```

2. Acesse os serviços via navegador:

    * [http://localhost](http://localhost) → Conteúdo HTML via API Gateway
    * [http://localhost/style.css](http://localhost/style.css) → CSS via API Gateway
    * [http://localhost/api/games](http://localhost/api/games) → API Java (Spring)
    * [http://localhost/laravel](http://localhost/laravel) → Interface inicial do Laravel

3. Teste a configuração do NGINX:

   ```bash
   docker compose exec nginx nginx -t
   ```

---

## 🔧 Personalização

Para expandir ou alterar a arquitetura:

* ⚠️ **Edite o `nginx.conf`** para novas rotas ou lógicas no proxy.

* ➕ **Adicione novos servidores** com arquivos `.conf` em `settings/servers/`.

* 🔀 **Mapeie novas portas** via `docker-compose.yml` para facilitar testes internos.

* 🧩 **Volumes mapeados** permitem recarregamento das configurações sem rebuild.

* ❗ **Recarregue a configuração** após alterações:

  ```bash
  docker compose exec nginx nginx -s reload
  ```

* 🧱 **Erros personalizados** em `web/error` são automaticamente servidos:

    * `40x` → `error40x.html`
    * `50x` → `error50x.html`

---

## 📎 Referências Técnicas

Abaixo, algumas tecnologias e recursos utilizados neste repositório:

* **NGINX como Proxy Reverso**
  [https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/](https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/)

* **Docker Compose para múltiplos containers**
  [https://docs.docker.com/compose/](https://docs.docker.com/compose/)

* **Spring Boot com `@ControllerAdvice` e `ResponseBodyAdvice`**

    * Interceptação de erros e personalização de respostas:
      [https://www.baeldung.com/spring-controller-advice](https://www.baeldung.com/spring-controller-advice)
      [https://www.baeldung.com/spring-responsebodyadvice](https://www.baeldung.com/spring-responsebodyadvice)

* **Headers HTTP com `proxy_set_header` no NGINX**
  [https://nginx.org/en/docs/http/ngx\_http\_proxy\_module.html#proxy\_set\_header](https://nginx.org/en/docs/http/ngx_http_proxy_module.html#proxy_set_header)

* **Arquivos MIME com `mime.types`**
  [https://nginx.org/en/docs/http/ngx\_http\_core\_module.html#include](https://nginx.org/en/docs/http/ngx_http_core_module.html#include)

* **Tratamento de erros no NGINX (`error_page`)**
  [https://nginx.org/en/docs/http/ngx\_http\_core\_module.html#error\_page](https://nginx.org/en/docs/http/ngx_http_core_module.html#error_page)

---

**Este ambiente serve como base de testes e experimentação para práticas com proxy reverso, separação de conteúdo estático e APIs dinâmicas.**

---

## 📫 Contato

Caso deseje entrar em contato para oportunidades ou dúvidas:

- 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
- 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
- 📧 antonio@javadeveloper.com.br

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)