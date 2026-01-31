# 🏦 Sistema de Pagamentos Simplificado (RestAPI)

Uma API RESTful robusta desenvolvida com Java (versão 25) e Spring Boot. Este projeto simula o backend de um banco digital, gerenciando transações financeiras entre usuários comuns e lojistas, com foco em integridade de dados e arquitetura de microsserviços containerizados.

## 🚀 Tecnologias Utilizadas

* **Java 25** (Recursos modernos da linguagem)
* **Spring Boot 4.0.2**
* **Docker & Docker Compose** (Containerização completa)
* **MariaDB** (Banco de dados relacional)
* **Spring Data JPA** (ORM)
* **OpenFeign** (Integração com APIs externas)

## 🏗️ Arquitetura

O sistema é totalmente dockerizado e orquestra dois serviços principais:

1.  **API Service:** Aplicação Spring Boot rodando na porta `8080`.
2.  **Database Service:** Banco de dados MariaDB rodando na porta `3306`.

## 🧪 Testes Automatizados e Qualidade

O projeto segue uma estratégia rigorosa de testes unitários utilizando **JUnit 5** e **Mockito** (estilo BDD), focando na blindagem da camada de serviço (`TransactionService`).

Principais cenários cobertos:
* **Caminho Feliz (Happy Path):** Garante que transações válidas debitam, creditam e salvam os estados corretamente.
* **Isolamento de Dependências:** Uso de Mocks para simular APIs externas (Autorizador e Notificador) e Repositórios.
* **Cenários de Exceção:** Testes específicos para falhas de autorização, serviços indisponíveis (fallback) e validações de regras de negócio.
* **Estratégia Fail-Fast:** Verificação de performance que assegura que o sistema **não** consome recursos externos (API de Autorização) se o usuário não tiver saldo suficiente (verificado via `shouldHaveNoInteractions`).

## 📋 Regras de Negócio

O sistema segue regras estritas para garantir a consistência das transações:

* **Cadastro:**
    * Clientes (Comuns e Lojistas) necessitam de Nome Completo, CPF/CNPJ, E-mail e Senha.
    * CPF/CNPJ e E-mails devem ser únicos no banco de dados.
* **Transferências:**
    * **Usuários Comuns:** Podem enviar dinheiro para Lojistas e outros Usuários.
    * **Lojistas:** Apenas recebem transferências, não enviam.
* **Validações:**
    * Verificação de saldo disponível antes da transação.
    * Consulta a um serviço autorizador externo (Mock) antes de efetivar a transferência.
* **Segurança (ACID):**
    * A operação é atômica; qualquer falha reverte toda a transação, garantindo que o dinheiro não se perca.
* **Notificação:**
    * Envio assíncrono de notificação (simulação de E-mail/SMS) ao recebedor após o sucesso.

## 📦 Como Rodar o Projeto

Você **não** precisa ter Java ou MariaDB instalados. A única dependência é o [Docker](https://www.docker.com/products/docker-desktop/).

### Passo a passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/annajulia-dev/rest-payment-system.git](https://github.com/annajulia-dev/rest-payment-system.git)
    cd rest-payment-system
    ```

2.  **Execute a orquestração:**
    ```bash
    docker compose up --build
    ```

3.  **Acesse:**
    A API estará pronta para receber requisições em: `http://localhost:8080`

---

## 🔌 Endpoints da API

Aqui estão os principais endpoints para teste (Recomendo usar o Postman ou Insomnia).

### 👤 Usuários (`/users`)

* **POST** `/`: Cria um novo usuário (Comum ou Lojista).
    <br>
    <img width="409" height="292" alt="Exemplo de Criação de Usuário" src="https://github.com/user-attachments/assets/25e83e35-23bc-4cef-9903-0a5e995e5258" />

* **GET** `/`: Lista todos os usuários cadastrados.
    <br>
    <img width="378" height="403" alt="Exemplo de Listagem de Usuários" src="https://github.com/user-attachments/assets/ca6e3884-77f6-4ce7-b064-0f6de83c5a33" />

* **PUT** `/{id}`: Atualiza dados cadastrais do usuário.
* **DELETE** `/{id}`: Remove um usuário do sistema.

### 💸 Transações (`/transactions`)

* **POST** `/`: Realiza transferência entre contas.
    * *Payload:* ID do pagador, ID do recebedor e valor.
    * *Nota:* O sistema validará automaticamente o saldo e a autorização externa.

---

<div align="center">
Desenvolvido por <a href="https://github.com/annajulia-dev">Anna Julia</a>
</div>
