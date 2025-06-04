# NGINX com Docker – Branch: `03-reverse-proxy`

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Este branch apresenta uma arquitetura com múltiplos servidores NGINX, incluindo um proxy reverso configurado para rotear dinamicamente requisições com base em padrões de URL. Embora os exemplos envolvam arquivos estáticos (como HTML e CSS) ou conteúdos dinâmicos servidos por uma aplicação Java, o foco é demonstrar que a estratégia é totalmente agnóstica à tecnologia — podendo ser aplicada igualmente a aplicações em PHP, Node.js, Ruby on Rails ou qualquer outro stack. Essa estrutura simula um cenário realista de separação de responsabilidades entre frontend e backend, com um servidor central gerenciando o tráfego de forma inteligente e eficiente.

Além disso, o repositório consome o projeto [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) como serviço backend. Esse sistema Java foi adaptado para retornar dados em um formato padronizado de resposta, graças à adição de:

* `@ControllerAdvice` – captura e trata exceções de forma global;
* `ResponseBodyAdvice` – intercepta as respostas antes de serem enviadas ao cliente;
* `@Value` – permite ler configurações do `application.properties`, como o nome da aplicação, e inseri-las automaticamente nas respostas JSON.

Essa padronização foi pensada para tornar explícito qual instância ou serviço backend está provendo a resposta, o que se torna essencial quando há múltiplas réplicas ou serviços por trás de um balanceador de carga. O campo `"server"` na resposta facilita o rastreamento e depuração.

### 🔍 Exemplo de resposta JSON

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

O campo `"server"` é preenchido automaticamente com base na propriedade `application.name`, definida no `application.properties`, permitindo que cada instância backend se identifique de forma transparente.

---

## 📦 Estrutura do Projeto

```
.
├── docker-compose.yml                  # Orquestração dos containers NGINX + Java
├── java/                               # Projeto backend em Spring Boot (baseado no repositório: game-list-api)
├── settings/                           # Configurações do NGINX
│   ├── nginx.conf                      # Configuração principal
│   └── servers/                        # Virtual hosts individuais
│       ├── proxy-reverse.conf          # Servidor principal com lógica de roteamento
│       ├── server1-html.conf           # Servidor de HTML estático
│       └── server2-css.conf            # Servidor de CSS estático
└── web/                                # Conteúdo estático
    ├── html/                           # HTML básico (index.html)
    ├── server1/                        # Servido por server1-html
    ├── server2/                        # Servido por server2-css
    └── error/                          # Páginas personalizadas de erro
        ├── error40x.html
        └── error50x.html
```

---

## 🚀 Como executar

1. Suba os containers:

   ```bash
   docker compose up -d
   ```

2. Acesse no navegador:

   * [http://localhost](http://localhost) → Conteúdo HTML via proxy reverso
   * [http://localhost/style.css](http://localhost/style.css) → Arquivo CSS via proxy reverso
   * [http://localhost/games](http://localhost/games) → API backend Java via proxy reverso

3. Teste a configuração do NGINX:

   ```bash
   docker compose exec nginx nginx -t
   ```

---

## 🔧 Personalização

Para expandir a arquitetura com novos servidores ou modificar o comportamento do proxy reverso:

* ⚠️ **Edite o arquivo principal de roteamento**: toda nova rota ou alteração de regras de proxy precisa ser refletida em `settings/nginx.conf`, que atua como ponto central de distribuição das requisições.
* ➕ **Adicione novos servidores** criando arquivos `.conf` no diretório `settings/servers/`, definindo o comportamento de cada servidor individual.
* 🔀 **Associe uma nova porta** no `docker-compose.yml` ao novo serviço, se necessário. Exemplo de serviço adicional:

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

* 🧩 **Os volumes são montados** para permitir hot-reload das configurações sem necessidade de rebuild da imagem.
* ❗ **Após alterações** no roteador (`nginx.conf`) ou adição de novos serviços, reinicie o processo de roteamento executando:

    ```bash
    docker compose exec nginx nginx -s reload
    ```
  
* 🧱 **Páginas de erro HTTP personalizadas** (como 404 ou 502) são servidas diretamente do diretório `web/error`. 

---

## 📂 Exemplos de Roteamento

O comportamento do proxy reverso é definido no arquivo `proxy-reverse.conf`, que inspeciona o caminho (path) e/ou a extensão do recurso requisitado para decidir para onde encaminhar a requisição:

| Caminho da URL                      | Destino (Container) | Porta | Tipo de Conteúdo            |
| ----------------------------------- | ------------------- | ----- | --------------------------- |
| `/` ou `/index.html`                | `server1-html`      | 8081  | Página HTML principal       |
| `/qualquer-coisa.html`              | `server1-html`      | 8081  | Qualquer HTML estático      |
| `/estilo.css` ou `/assets/main.css` | `server2-css`       | 8082  | Arquivos CSS                |
| `/games`, `/api/*`, `/outros`       | `games1-app`        | 9001  | API REST em Java (Spring)   |
| `/inexistente`                      | `nginx`             | -     | Redirecionado para erro 40x |

Além disso, os arquivos de erro personalizados localizados em `web/error` são utilizados sempre que ocorrem erros como:

* `400`, `401`, `404` → `error40x.html`
* `500`, `502`, `503`, `504` → `error50x.html`

---

## ✅ Requisitos

* Docker 20+
* Docker Compose 2+

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