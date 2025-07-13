# ⚙️ Comunicação com Backends Dinâmicos via FastCGI

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Antes da popularização dos servidores autocontidos (como Spring Boot, Node.js ou Gunicorn), era comum que o NGINX servisse apenas como proxy reverso, delegando a execução da lógica backend para um interpretador separado — frequentemente via **FastCGI**, um protocolo otimizado para esse tipo de comunicação.

Diferente do `proxy_pass`, que encaminha requisições HTTP, o `fastcgi_pass` fala um **protocolo específico**, usado por servidores como o **php-fpm** (no caso do PHP), mas também aplicável a outros ambientes que implementam FastCGI.

Aqui vamos construir essa ponte passo a passo, começando com um exemplo incompleto e evoluindo até uma configuração robusta e funcional.

Vamos analisar:

* Como o NGINX delega scripts dinâmicos via `fastcgi_pass`
* Por que o primeiro exemplo falha
* O papel de variáveis como `SCRIPT_FILENAME` e `PATH_INFO`
* Como o `fastcgi_split_path_info` separa a rota do script
* E como frameworks modernos — como Laravel, mas também outros que respeitam `PATH_INFO` — resolvem rotas internas

> ℹ️ Hoje, muitas linguagens modernas optam por servidores autocontidos — onde a aplicação e o servidor HTTP vivem no mesmo processo. No entanto, a abordagem com FastCGI **ainda é altamente relevante** em contextos onde o desacoplamento entre servidor e aplicação traz vantagens de segurança, escalabilidade ou legado.
>
> Ao final, você terá uma configuração clara e eficiente para executar requisições dinâmicas via FastCGI — com foco na performance e compatibilidade, independente da linguagem usada no backend.

---

## 📂 Estrutura inicial do projeto

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
│       ├── nginx-laravel.conf         # Virtual host que se comunica com PHP-FPM via FastCGI
│       └── nginx-java-balancer.conf   # Configuração de logs personalizados para análise detalhada das requisições no balanceador
└── web/
    ├── html/                          # HTML estático
    ├── server1/                       # Servido por server1-html
    ├── server2/                       # Servido por server2-css
    └── error/                         # Páginas de erro personalizadas
        ├── error40x.html
        └── error50x.html
```

---

## 🚀 Evolução até chegar ao FastCGI

Antes da popularização dos servidores de aplicação autocontidos e das arquiteturas modernas, a execução de lógica no backend era feita através de um mecanismo chamado **CGI (Common Gateway Interface)** — uma forma primitiva, mas funcional, de estender a capacidade dos servidores web para além de arquivos estáticos.

Na era inicial da web, o papel do servidor era simples: **servir documentos HTML**. Com o tempo, surgiu a necessidade de executar lógica — como contar acessos ou gerar páginas dinâmicas. O CGI atendia essa demanda criando **um novo processo a cada requisição**. Esse processo executava o código (geralmente scripts ou binários em C), processava a entrada, gerava a saída HTML e encerrava. Essa abordagem funcionava, mas impunha sérios custos de performance e escalabilidade.

Para resolver esse gargalo, surgiu o **FastCGI**, um protocolo que mantém processos vivos e reutilizáveis. Em vez de iniciar e encerrar um processo por requisição, o servidor web encaminha as requisições para um **gerenciador FastCGI** (como o PHP-FPM), que se comunica com os processos em execução. Isso **reduz o overhead**, melhora a performance e permite o uso eficiente dos recursos.

Essa abordagem segue bastante presente em ambientes que utilizam interpretadores externos — sendo o PHP com php-fpm o caso mais emblemático. Ainda assim, a arquitetura FastCGI também pode ser adotada em linguagens como Python ou Ruby, sempre que o servidor web precisar manter o processo de aplicação separado da camada HTTP. Outras linguagens também podem se beneficiar desse modelo, embora seja mais comum que utilizem **servidores autocontidos**, como é o caso do Java com Spring Boot ou do Python com Gunicorn.

---

## 🔄 Resumo comparativo:

| Aspecto                      | **CGI**                                                       | **FastCGI**                                                              |
| ---------------------------- | ------------------------------------------------------------- | ------------------------------------------------------------------------ |
| 🧠 Ideia básica              | Servidor cria **um processo novo a cada requisição**          | Servidor **reutiliza processos** existentes (pool de processos)          |
| ⚙️ Funcionamento             | Processo é criado → executa lógica → responde → é encerrado   | Processo é criado uma vez → recebe múltiplas requisições                 |
| 🐌 Desempenho                | Ruim, devido à criação e destruição constante de processos    | Muito mais eficiente e escalável                                         |
| 🛠️ Implementação típica     | Lógica escrita em C ou script, executada como binário externo | Processo gerenciado por um **FastCGI Process Manager** (ex: PHP-FPM)     |
| ♻️ Reuso de recursos         | Não reutiliza nada                                            | Reaproveita conexões, processos, sockets, etc.                           |
| 🧼 Gerenciamento de recursos | Feito manualmente pelo desenvolvedor                          | Feito automaticamente pelo gerenciador (libera conexões, arquivos, etc.) |
| 🌐 Exemplo moderno           | Raro hoje em dia                                              | Usado em quase toda aplicação PHP com NGINX (via PHP-FPM)                |

> Mesmo outras linguagens como **Python e Java** *podem* usar FastCGI, mas **é mais comum que tenham seus próprios servidores** (ex: uWSGI, Gunicorn, Tomcat).

---

## 🧱 Primeiros passos com FastCGI

Vamos configurar o NGINX para delegar a execução de arquivos `.php` ao processo gerenciado por `php-fpm`. Como o `php-fpm` escuta a porta `9000` por padrão, utilizaremos essa porta no destino do `fastcgi_pass`.

No lugar de `proxy_pass` (usado com HTTP), usaremos `fastcgi_pass`, que fala o protocolo FastCGI com o backend.

> **Importante:** FastCGI não fala HTTP. A requisição precisa ser "traduzida" para esse protocolo. Por isso o NGINX exige mais informações para fazer essa ponte corretamente.

---

## 🚫 Primeiro exemplo (incompleto)

```nginx
location ~ \.php$ {
    fastcgi_pass laravel1:9000;
}
```

### ❌ Por que isso **não funciona**:

Apesar de parecer correto, essa configuração falha silenciosamente ou retorna erro 502. O motivo é que **faltam diversas informações que o NGINX precisa passar para o `php-fpm` via protocolo FastCGI**, como:

* Qual é o caminho do script que deve ser executado (`SCRIPT_FILENAME`)
* Qual é a query string da requisição (`QUERY_STRING`)
* Qual é o tipo de conteúdo (`CONTENT_TYPE`)
* Qual é o método HTTP (`REQUEST_METHOD`)
* E várias outras variáveis de ambiente que o FastCGI precisa

---

### 💡 Definindo o caminho correto

```nginx
location ~ \.php$ {
    fastcgi_pass laravel1:9000;
    include fastcgi_params;

    fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
}
```

> Agora sim, o `php-fpm` saberá exatamente qual arquivo executar, porque a diretiva `SCRIPT_FILENAME` irá **montar dinamicamente o caminho completo do arquivo PHP** requisitado, combinando duas variáveis:

* **`$document_root`** → contém o valor definido pela diretiva `root` do `server`, que neste caso é:

  ```nginx
  server {
    listen 8083;
    index index.php;
    root /var/www/public;
    ...
  ```

  Ou seja, `$document_root` equivale a:

  ```
  /var/www/public
  ```

* **`$fastcgi_script_name`** → representa a **parte final da URL da requisição**, a partir da raiz virtual, ou seja, o caminho do script `.php` solicitado. Por exemplo, em uma requisição para:

  ```
  http://localhost/laravel/index.php
  ```

  Como o proxy principal (`nginx:80`) está usando:

  ```nginx
  proxy_pass http://nginx-laravel:8083/;
  ```

  ...o prefixo `/laravel` **é removido automaticamente**, e a URL recebida pelo `nginx-laravel:8083` passa a ser simplesmente:

  ```
  /index.php
  ```

  Portanto, `$fastcgi_script_name` será:

  ```
  /index.php
  ```
---

### 🔍 Entendendo o valor de `$fastcgi_script_name` em diferentes cenários

#### 📌 Exemplo 1 — Acesso direto ao script:

**URL:**

```
http://localhost/laravel/index.php
```

1. O NGINX principal (`localhost:80`) está configurado com:

   ```nginx
   proxy_pass http://nginx-laravel:8083/;
   ```

   Com essa barra final (`/`), o prefixo `/laravel` é removido automaticamente.

2. Então a URL que chega no `nginx-laravel:8083` é:

   ```
   /index.php
   ```

3. Nesse caso:

   ```
   $fastcgi_script_name = /index.php
   ```

   E o `SCRIPT_FILENAME` será montado como:

   ```
   /var/www/public/index.php
   ```

---

#### 📌 Exemplo 2 — Acesso com rota após o index.php:

**URL:**

```
http://localhost/laravel/index.php/teste
```

1. O NGINX principal remove o prefixo `/laravel`, restando:

   ```
   /index.php/teste
   ```

2. Para lidar com esse cenário, usamos dentro de `location ~ \.php$`:

   ```nginx
   fastcgi_split_path_info ^(.+\.php)(/.+)$;
   ```

   Isso separa a parte do script (`.php`) e a parte extra do caminho.

3. O que acontece aqui:

  * `$fastcgi_script_name = /index.php`
  * `$fastcgi_path_info = /teste`

4. Então:

   ```nginx
   fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
   # SCRIPT_FILENAME → /var/www/public/index.php
   ```

   E o PHP-FPM (Laravel) recebe o `PATH_INFO` como `/teste`, permitindo ao Laravel **resolver essa rota internamente.**

---

#### 📌 Exemplo 3 — Acesso com rota amigável (sem index.php na URL):

**URL:**

```
http://localhost/laravel/teste
```

1. O NGINX principal remove o prefixo `/laravel`, restando:

   ```
   /teste
   ```

Nesse caso, esse endereço irá ser redirecionado para o `location /`. O `try_files`, dentro de `location /`, do `nginx-laravel` faz a mágica:

```nginx
location / {
    try_files $uri $uri/ /index.php?$query_string;
}
```

2. A URL original é `/teste`, que **não existe fisicamente** como arquivo.

3. Então o `try_files` **encaminha para**:

   ```
   /index.php
   ```

   ...e mantém a query string, se houver.

4. Com isso, internamente é como se fosse:

   ```
   /index.php/teste
   ```

> A partir daqui temos o caso de `📌 Exemplo 2 — Acesso com rota após o index.php:` logo acima:

E assim:

* `$fastcgi_script_name = /index.php`
* `$fastcgi_path_info = /teste`

> ✅ Laravel trata isso internamente e encontra a rota definida em `routes/web.php`.

---

### ✅ Resumo rápido

| URL requisitada                  | `$fastcgi_script_name` | `$fastcgi_path_info` |
| -------------------------------- | ---------------------- | -------------------- |
| `/laravel/index.php`             | `/index.php`           | *(vazio)*            |
| `/laravel/index.php/teste`       | `/index.php`           | `/teste`             |
| `/laravel/teste` (rota amigável) | `/index.php`           | `/teste`             |

Esse comportamento só funciona corretamente porque temos **essas diretivas combinadas** no `location ~ \.php$`:

```nginx
fastcgi_split_path_info ^(.+\.php)(/.+)$;
include fastcgi_params;
fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;
fastcgi_param PATH_INFO $fastcgi_path_info;
```

---

🔧 **Resultado final da interpolação:**

O `SCRIPT_FILENAME` final enviado ao `php-fpm` será:

```
/var/www/public/index.php
```

Isso garante que o `php-fpm` dentro do container `laravel1` (que tem o volume montado com `./php/laravel1:/var/www`) encontre corretamente o script que deve ser executado.

---

## ✅ Versão final recomendada

```nginx
server {
    listen 8083;
    index index.php;
    root /var/www/public;

    client_max_body_size 51g;           # Aceita uploads grandes
    client_body_buffer_size 512k;
    client_body_in_file_only clean;

    location ~ \.php$ {
        try_files $uri =404;  # Evita execução se o arquivo não existir
        fastcgi_split_path_info ^(.+\.php)(/.+)$;  # Divide script + path extra
        fastcgi_pass laravel1:9000;  # Encaminha requisição FastCGI para o php-fpm
        fastcgi_index index.php;  # Página padrão se não especificada
        include fastcgi_params;  # Variáveis padrão do FastCGI
        fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name;  # Caminho real do arquivo
        fastcgi_param PATH_INFO $fastcgi_path_info;  # Parte extra da URL
    }

    location / {
        try_files $uri $uri/ /index.php?$query_string;
        gzip_static on;  # Usa versão compactada se existir
    }

    error_log  /var/log/nginx/error.log;
    access_log /var/log/nginx/access.log;
}
```

---

## 🎯 Conclusão

A configuração com `fastcgi_pass` é mais detalhada do que com `proxy_pass` porque o protocolo **FastCGI exige variáveis específicas** que não estão presentes numa simples requisição HTTP. Para resolver isso:

* **Incluímos o arquivo `fastcgi_params`**
* **Passamos explicitamente o caminho do script**
* **Tratamos `PATH_INFO` para URLs amigáveis**

A partir daqui, você pode montar um projeto funcional em PHP com o `php-fpm` e o NGINX rodando como gateway.

---

## 📫 Contato

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Desenvolvido por [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)