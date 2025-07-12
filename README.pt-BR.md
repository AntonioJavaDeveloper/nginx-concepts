# 📊 Logs Personalizados no NGINX com Java, Laravel e Docker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

O NGINX registra automaticamente todas as requisições que passam por ele, bem como erros encontrados durante o processamento. Esses registros são fundamentais para entender o comportamento da aplicação, detectar gargalos, investigar falhas e garantir observabilidade em ambientes de produção. Com poucas configurações, é possível customizar o que será logado e como essas informações serão exibidas.

Além dos arquivos padrão (`access.log` e `error.log`), o NGINX permite definir formatos personalizados de log, com variáveis que mostram IP do cliente, tempo de resposta do backend, URI acessada, código de status e muito mais. Também é possível redirecionar os logs para a saída padrão do container, integrando com ferramentas como ELK ou Grafana Loki — prática essencial em ambientes modernos baseados em containers e microserviços.

---

## 🏗️ Diagrama Arquitetural

![Diagrama Arquitetural](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

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

## 📁 Onde estão os logs do NGINX?

Por padrão, os logs de acesso e erro ficam armazenados em dois arquivos dentro do container NGINX:

| Tipo de log     | Caminho dentro do container    | Descrição                          |
|-----------------|--------------------------------|-------------------------------------|
| Access Log      | `/var/log/nginx/access.log`    | Todas as requisições HTTP feitas   |
| Error Log       | `/var/log/nginx/error.log`     | Erros de proxy, upstream, sintaxe  |

Esses arquivos são criados automaticamente, conforme a diretiva `access_log` e `error_log` definida no `nginx.conf` principal (ou herdada dos arquivos incluídos via `include`).

#### 🔁 **Nota sobre containers**

> 📝 **Importante:** Se você estiver utilizando a imagem oficial do NGINX (`nginx:1.26`), os arquivos `/var/log/nginx/access.log` e `/var/log/nginx/error.log` são, na verdade, **links simbólicos para `/dev/stdout` e `/dev/stderr`**.
> Ou seja, os logs são enviados diretamente para o console do container, e **não são gravados em disco** como arquivos tradicionais.
>
> Você pode verificar isso com:
>
> ```bash
> docker compose exec nginx-java-balancer ls -l /var/log/nginx
> ```

---

## 📘 Logs de Acesso no NGINX

O `access_log` é o registro **de todas as requisições HTTP processadas com sucesso ou falha** (independente do status code). Ele permite visualizar quais endpoints foram acessados, por quem, em que momento, com qual resposta e quanto tempo demoraram.

### ✅ Exemplo de configuração:

```nginx
access_log /var/log/nginx/access.log custom_logs;
```

Isso indica que o log será gravado no caminho `/var/log/nginx/access.log`, utilizando o formato `custom_logs` previamente definido.

> 🧾 Nota: em containers baseados na imagem oficial do NGINX, esse caminho aponta para o console do container, e **não para um arquivo real no disco**.

### ✍️ Como criar formatos personalizados de log (`log_format`)

Você pode definir um ou mais formatos personalizados de log com a diretiva `log_format`. Isso permite adaptar a saída para diferentes finalidades, como:

* Auditoria de segurança
* Análise de desempenho
* Integração com ferramentas externas

#### Exemplo de um formato resumido:

```nginx
log_format minimal '$remote_addr - $request';
```

#### Exemplo mais completo:

```nginx
log_format custom_logs '$remote_addr [$time_local] '
                       '"$request" $status $body_bytes_sent '
                       '"$http_referer" "$http_user_agent" '
                       'real_ip=$http_x_forwarded_for '
                       'x_real_ip=$http_x_real_ip '
                       'ua="$upstream_addr" '
                       'rt=$request_time urt=$upstream_response_time';
```

Depois de definido, você pode usar esse nome (`custom_logs`) na diretiva `access_log`.

### 📌 Onde pode ser definido o `log_format`?

| Escopo de uso | Pode usar `log_format`? | Comentário                                                          |
| ------------- | ----------------------- | ------------------------------------------------------------------- |
| **http**      | ✅ Sim                   | Ideal para definições globais. É o lugar recomendado.               |
| **server**    | ❌ Não                   | Não é permitido definir `log_format` diretamente no bloco `server`. |
| **location**  | ❌ Não                   | Também **não é permitido** criar `log_format` dentro de `location`. |

> 📌 **Importante:** Apesar de não ser possível criar `log_format` dentro de `server` ou `location`, você **pode usar formatos já definidos** dentro desses blocos, como:

```nginx
server {
    access_log /var/log/nginx/myapi.log minimal;

    location /api {
        access_log /var/log/nginx/api-detalhado.log custom_logs;
    }
}
```

### 🔁 Criando múltiplos logs com diferentes formatos

Você pode definir múltiplos `access_log` com formatos diferentes para fins específicos:

```nginx
access_log /var/log/nginx/access.log custom_logs;
access_log /var/log/nginx/auditoria.log minimal;
```

Essa abordagem é útil para:

* Criar logs reduzidos para análise rápida
* Criar logs completos apenas para determinadas rotas
* Manter logs separados por aplicação (ex: Laravel x Java)

---

## 🐞 Logs de Erro no NGINX

O NGINX permite registrar mensagens de erro em diferentes níveis de severidade, úteis para diagnosticar falhas na aplicação, no proxy ou no servidor.

### 📌 Onde ficam os logs de erro?

Os logs de erro do servidor, que estão configurados no arquivo `settings/servers/server1-html.conf`, vão para:

```nginx
error_log /var/log/nginx/error.log warn;
```

Esse caminho (`/var/log/nginx/error.log`) é **relativo ao container**, e armazena todos os erros gerados ao servir páginas estáticas ou ao lidar com falhas de proxy.


> 🧾 Nota: em containers baseados na imagem oficial do NGINX, esse (`/var/log/nginx/error.log`) caminho aponta para o console do container, e **não para um arquivo real no disco**.


### ⚠️ Níveis de log disponíveis no NGINX

| Nível    | Descrição                                                                             | Inclui níveis inferiores? |
| -------- | ------------------------------------------------------------------------------------- | ------------------------- |
| `debug`  | **Mais detalhado possível**. Inclui headers, fases do ciclo de vida, roteamento, etc. | Sim                       |
| `info`   | Informações úteis para diagnóstico e acompanhamento do fluxo de requisições           | Sim                       |
| `notice` | Avisos que não afetam diretamente a operação, mas devem ser observados                | Sim                       |
| `warn`   | Erros potenciais ou temporários, como atrasos ou diretórios ausentes                  | Sim                       |
| `error`  | Erros importantes, como falhas de proxy, 502, 403, 500 etc.                           | Sim                       |
| `crit`   | Situações críticas que impedem o funcionamento do serviço                             | Sim                       |
| `alert`  | Requer ação imediata. Pode ser falha de disco, memória, etc.                          | Sim                       |
| `emerg`  | O servidor está inoperante. Usado em eventos catastróficos.                           | Sim                       |

💡 Se você configurar:

```nginx
error_log /var/log/nginx/error.log info;
```

Você verá todas as mensagens com nível `info` **e todos os níveis abaixo dele** (`notice`, `warn`, `error`, etc.).

### 🔍 Acompanhando os logs em tempo real

Como os logs do NGINX estão sendo enviados para `stdout` e `stderr`, você pode acompanhá-los facilmente com o comando:

```bash
docker compose logs -f <serviço>
````

#### ✅ Exemplos:

* **Para acompanhar os logs do balanceador (`nginx-java-balancer`)**:

  ```bash
  docker compose logs -f nginx-java-balancer
  ```

* **Para acompanhar os logs do servidor principal (`nginx`, na porta 80)**:

  ```bash
  docker compose logs -f nginx
  ```

Você verá tanto os acessos (`access_log`) quanto os erros (`error_log`) no console em tempo real.

### 🧪 Testando os erros no painel

No navegador, após subir o projeto com:

```bash
docker compose up --build
```

Acesse [http://localhost](http://localhost) e clique no botão **"Ver mais"**, depois verifique a seção **"📌 Erros principais"**. Caso haja erros 404 e 502 gerados ali serão enviados automaticamente para `/var/log/nginx/error.log`.

⚠️ **Importante:**
Nem todos os erros são tratados pelo mesmo servidor:

* **Erros como `/api/abc` ou `/round-robin/games/abc`** são processados pelo *balanceador* (`nginx-java-balancer`).
* **Erros como `/abc` ou `/error`** ocorrem no *NGINX principal* (`nginx`, serviço da porta 80), e são redirecionados para o servidor da porta `8081`, onde há páginas HTML customizadas para códigos 40x e 50x.

---

## 📦 Como enviar os logs para stdout/stderr (modo 12factor / ELK-ready)


> ⚠️ **Observação:** No caso da imagem `nginx:1.26`, o redirecionamento para `stdout/stderr` já está habilitado por padrão.  
> As diretivas `access_log` e `error_log` continuam funcionando, mas os caminhos apontam internamente para `/dev/stdout` e `/dev/stderr`.


Por padrão, o NGINX escreve logs em arquivos. Mas **para enviar logs para um sistema de log externo** como ELK, Loki, Datadog, etc., é melhor redirecionar os logs para **stdout** e **stderr**.

### ✅ Etapas:

1. **Abra o arquivo `nginx.conf` principal**
2. Altere as diretivas `access_log` e `error_log` assim:

```nginx
# Redireciona logs de acesso para a saída padrão
access_log /dev/stdout custom_logs;

# Redireciona logs de erro para a saída de erro padrão
error_log /dev/stderr warn;
```

> ⚠️ Certifique-se de que o `log_format custom_logs` esteja definido no mesmo arquivo ou incluído por ele.

3. **Reinicie o container:**

```bash
docker compose restart nginx
```

4. Agora os logs vão aparecer diretamente com:

```bash
docker compose logs -f nginx
```

## 🧠 Por que enviar os logs para stdout/stderr importa?

Esse modelo segue o padrão **[12factor](https://12factor.net/logs)**:

> > "A aplicação deve tratar logs como streams de eventos e escrevê-los para stdout. É responsabilidade do ambiente operacional capturar, roteá-los e persistir se necessário."

Isso facilita a integração com ferramentas como:

* Elasticsearch + Logstash + Kibana (ELK)
* Grafana + Loki
* Fluentd
* CloudWatch
* Prometheus (com exporters)

---

## ✅ Conclusão

Com isso, você pode:

* Ver os logs da aplicação em tempo real
* Modificar o formato dos logs
* Enviar logs diretamente para soluções externas
* Auditar problemas de roteamento, erros de proxy e comportamento dos algoritmos de balanceamento

---

## 📫 Contato

* 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
* 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
* 📧 [antonio@javadeveloper.com.br](mailto:antonio@javadeveloper.com.br)

> Desenvolvido por [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)