# Community Center API

Uma API REST para gerenciamento de centros comunitários, desenvolvida com Spring Boot e MongoDB.

## 📋 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.3**
- **Spring Data MongoDB** - Para persistência de dados
- **Spring Web MVC** - Para desenvolvimento da API REST
- **MongoDB** - Banco de dados NoSQL
- **Lombok** - Para redução de código boilerplate
- **SpringDoc OpenAPI** - Para documentação automática da API
- **Maven** - Gerenciamento de dependências

## 🚀 Como Executar o Projeto

### Pré-requisitos

- Java 17 ou superior
- Maven 3.6 ou superior
- MongoDB instalado e executando
- Git

### Passos para execução

1. **Clone o repositório:**
   ```bash
   git clone <url-do-repositorio>
   cd CommunityCenterAPI
   ```

2. **Configure o MongoDB:**
   - Certifique-se de que o MongoDB está rodando na porta padrão (27017)
   - Configure a string de conexão no arquivo `application.properties` se necessário

3. **Execute o projeto:**
   ```bash
   # Usando Maven Wrapper (recomendado)
   ./mvnw spring-boot:run
   
   # Ou usando Maven instalado
   mvn spring-boot:run
   ```

4. **Acesse a aplicação:**
   - API: `http://localhost:8080`
   - Documentação Swagger: `http://localhost:8080/swagger-ui.html`

## 📁 Estrutura do Projeto
- CommunityCenterAPI/
- ├── src/
- │ ├── main/
- │ │ ├── java/
- │ │ │ └── br/com/antoniolps/CommunityCenterAPI/
- │ │ └── resources/
- │ └── test/
- ├── pom.xml
- └── README.md

## 📖 Documentação da API

A documentação interativa da API está disponível através do Swagger UI após executar a aplicação:

🔗 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

## 🛠️ Desenvolvimento

### Executar em modo de desenvolvimento

O projeto inclui Spring Boot DevTools para hot reload automático:
```bash 
./mvnw spring-boot:run
```

### Executar testes

```bash
./mvnw test
```