# Balancemaneto de Caraga em Java e API Gateway com NGINX + Laravel

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)

Este projeto demonstra um cenário realista com múltiplos serviços, incluindo APIs em Java, aplicações PHP (Laravel), e um gateway reverso NGINX com **balanceamento de carga entre múltiplas instâncias backend Java**.

Além disso, o sistema consome o projeto [game-list-api](https://github.com/AntonioJavaDeveloper/game-list-api) como backend, e também integra uma aplicação Laravel servida internamente, reforçando a flexibilidade de uso do gateway com múltiplas tecnologias.

---

## 🏗️ Diagrama Arquitetural

![Diagrama Arquitetural](https://raw.githubusercontent.com/AntonioJavaDeveloper/assets/refs/heads/main/nginx-concepts/images/04-load-balancer.png)

---

## 📘 O que é um API Gateway?

Um API Gateway é um servidor que atua como ponto de entrada para múltiplos serviços backend. Ele:

* Redireciona requisições para os serviços corretos
* Pode reescrever URLs
* Realiza balanceamento de carga
* Trata erros personalizados
* Serve conteúdos estáticos ou dinâmicos

Neste projeto, o NGINX é usado como gateway e também como balanceador de carga entre as APIs Java (`games1`, `games2`, `games3`).

---

## 🗂️ Estrutura do Projeto

```txt
.
.
├── docker-compose.yml                 # Orquestração dos containers (NGINX, Java, Laravel)
├── java/                              # Projeto backend em Spring Boot (game-list-api)
│   ├── games1/                        # Serviço Java com API de jogos 1
│   ├── games2/                        # Serviço Java com API de jogos 2
│   └── games3/                        # Serviço Java com API de jogos 3
├── php/                               # Diretório principal dos projetos PHP
│   ├── laravel1/                      # Projeto backend em PHP (Laravel)
├── settings/                          # Configurações NGINX
│   ├── nginx.conf                     # Configuração principal
│   └── servers/                       # Virtual hosts individuais
│       ├── proxy-reverse.conf         # Roteamento inteligente no gateway
│       ├── server1-html.conf          # HTML estático
│       ├── server2-css.conf           # CSS estático
│       └── nginx-laravel.conf         # Comunicação interna com Laravel
│       └── nginx-java-balancer.conf   # Balanceamento de carga entre os serviços Java (games1, games2, games3)
└── web/
    ├── html/                          # HTML estático
    ├── server1/                       # Servido por server1-html
    ├── server2/                       # Servido por server2-css
    └── error/                         # Páginas de erro personalizadas
        ├── error40x.html
        └── error50x.html
```

---

## 📦 Novidade: Balanceamento de Carga com NGINX

Agora, o endpoint `/api/**` não aponta diretamente para o container `games1`. Em vez disso, foi criado um novo serviço chamado `nginx-java-balancer`, que realiza o **balanceamento de carga entre os três serviços Java**:

* `games1` (porta interna 9001)
* `games2` (porta interna 9001)
* `games3` (porta interna 9001)

Esses serviços são balanceados pelo bloco `upstream` no `nginx-java-balancer.conf`.

```nginx
upstream games_api {
    server games1:9001;
    server games2:9001;
    server games3:9001;
}
```

Toda requisição `/api/**` chega ao proxy principal, que a redireciona para `nginx-java-balancer`, responsável por distribuir as requisições de forma equilibrada entre os três containers Java.

---

## 🛠️ Ajustes nos serviços Java

Cada serviço (`games1`, `games2`, `games3`) foi configurado com:

* `server.port=9001`
* `server.name=games1`, `games2`, ou `games3` (para identificar a origem da resposta)
* `spring.profiles.active`, `cors.origins`, e outras propriedades comuns

Exemplo da resposta da API:

```json
{
  "server": "games2",
  "data": {
    "id": 1,
    "title": "Mass Effect Trilogy",
    ...
  }
}
```

Isso permite validar de forma prática se o balanceamento está funcionando corretamente — cada requisição pode retornar de um serviço diferente (`games1`, `games2` ou `games3`).

---

## 📂 Exemplos de Roteamento

### Configuração atualizada (`proxy-reverse.conf`):

| Caminho da URL                     | Destino (Container)   | Porta | Observações                                                                |
| ---------------------------------- | --------------------- | ----- | -------------------------------------------------------------------------- |
| `/api/**`                          | `nginx-java-balancer` | 8080  | Balanceamento entre `games1`, `games2`, `games3` com reescrita da URL      |
| `/laravel`                         | `nginx-laravel`       | 8083  | Aplicação Laravel servida por NGINX intermediário com reescrita de caminho |
| `/`, `*.html`                      | `server1`             | 8081  | Conteúdo HTML estático                                                     |
| `*.css`                            | `server2`             | 8082  | Conteúdo CSS estático                                                      |
| `/error40x.html`, `/error50x.html` | `nginx` (local)       | -     | Arquivos de erro servidos diretamente do NGINX                             |

> 🔄 O uso de `rewrite ^/api(/.*)$ $1 break;` no bloco `/api` é **essencial** para remover o prefixo `/api` antes de encaminhar ao backend Java.

---

## 🌐 Redes Docker: Organização e Segurança entre Serviços

A arquitetura deste projeto foi aprimorada com a separação dos serviços em redes distintas, conforme definido no arquivo `docker-compose.yml`. Agora, cada grupo de serviços se comunica apenas com os serviços que realmente precisa acessar, graças à definição explícita de redes Docker específicas.

Essa separação de redes contribui significativamente para:

- **🔒 Segurança**: serviços que não precisam se comunicar ficam completamente isolados uns dos outros, reduzindo a superfície de ataque;
- **🧩 Organização**: facilita o entendimento da arquitetura e o rastreamento de dependências;
- **🚀 Desempenho e escalabilidade**: redes isoladas evitam tráfego desnecessário e tornam o sistema mais eficiente;
- **🛠️ Facilidade de depuração**: ao isolar componentes, fica mais simples diagnosticar problemas de comunicação;
- **📦 Boa prática DevOps**: é uma abordagem amplamente recomendada em ambientes de produção com múltiplos containers.

### 🔗 Redes e Serviços

| Rede             | Serviços Associados                                                                                 |
| ---------------- | --------------------------------------------------------------------------------------------------- |
| `reverse-proxy`  | `nginx`, `nginx-java-balancer`, `nginx-laravel`, `server1-html`, `server2-css`                      |
| `static-content` | `server1-html`, `server2-css`                                                                       |
| `java`           | `nginx-java-balancer`, `games1-app`, `games2-app`, `games3-app`                                     |
| `laravel`        | `nginx-laravel`, `laravel1`                                                                         |
| `default`        | Não utilizado — todos os serviços estão explicitamente conectados às suas redes específicas.        |

> ℹ️ **Curiosidade:** no proxy reverso, o serviço `server1-html` é responsável por servir arquivos `*.html`, enquanto o `server2-css` atende as requisições `*.css`, organizando o conteúdo estático de forma eficiente.

---

## 📌 Endpoints principais

| Método | Caminho                                  | Descrição                                |
|--------|------------------------------------------|------------------------------------------|
| GET    | `/api/games`                             | Lista todos os jogos                     |
| GET    | `/api/games/{id}`                        | Retorna detalhes de um jogo              |
| GET    | `/api/lists`                             | Lista todas as listas de jogos           |
| GET    | `/api/lists/{listId}/games`              | Lista os jogos de uma lista específica   |
| -      | `http://localhost/laravel`               | Página inicial do Laravel                |

---

## 🧪 Teste o Balanceamento

Faça múltiplas requisições para o endpoint `/api/games`. Você deverá observar que o campo `"server"` na resposta alterna entre `games1`, `games2` e `games3`, validando que o balanceador está funcionando.

---

## 🚀 Como subir os containers

```bash
docker compose up --build
```

Certifique-se de que as portas e volumes estão corretamente configurados.

---

**Este ambiente serve como base de testes e experimentação para práticas com proxy reverso, separação de conteúdo estático e APIs dinâmicas.**

---

## 📫 Contato

Caso deseje entrar em contato para oportunidades ou dúvidas:

- 🌐 [https://javadeveloper.com.br/](https://javadeveloper.com.br/)
- 💼 [LinkedIn](https://www.linkedin.com/in/antonio-javadeveloper/)
- 📧 antonio@javadeveloper.com.br

> Developed by [AntonioJavaDeveloper](https://github.com/AntonioJavaDeveloper)