# 🐾 Sistema de Gestão de Pets (CRUD)

Este é um projeto de estudo desenvolvido em **Java Desktop** para gerenciar cadastros de animais em uma clínica ou sistema de adoção. O projeto utiliza a arquitetura **DAO (Data Access Object)** para separar a lógica de negócio da persistência de dados.

## 🚀 Funcionalidades

O sistema oferece um ciclo completo de gerenciamento de dados (CRUD):

*   **Listagem de Pets**: Uma tela de consulta principal que exibe todos os animais cadastrados em uma `JTable` dinâmica.
*   **Cadastro Completo**: Inclusão de novos pets com informações de Nome, Dono, Espécie, Sexo, Idade, Peso e Raça.
*   **Edição Inteligente**: Ao selecionar um item na tabela e clicar em "Editar", o sistema abre a tela de cadastro já preenchida com os dados atuais do banco, permitindo atualizações rápidas de todos os campos.
*   **Exclusão de Registros**: Remoção de pets do banco de dados com uma caixa de confirmação para garantir a segurança da operação.
*   **Integração com Banco de Dados**: Persistência real utilizando MySQL e JDBC.

## 🛠️ Tecnologias Utilizadas

*   **Linguagem**: Java 17+
*   **Interface Gráfica**: Java Swing (IntelliJ GUI Designer)
*   **Banco de Dados**: MySQL (via XAMPP/PHPMyAdmin)
*   **Persistência**: JDBC (Java Database Connectivity)

## 📂 Estrutura do Projeto

*   `Pet.java`: Classe de modelo que representa o objeto Pet no sistema.
*   `PetDAO.java`: Classe de acesso a dados contendo os métodos SQL (Insert, Select, Update, Delete).
*   `ConsultaPetView`: Tela principal responsável pela visualização e gestão da tabela.
*   `CadastroPetView`: Tela de formulário unificada para criação e edição de registros.
*   `resources/sistemacadastro.sql`: Script para replicação exata do banco de dados.

## 📦 Como rodar o projeto

### 1. Pré-requisitos
*   Possuir o **JDK 17** ou superior instalado.
*   Ter o **XAMPP** (ou servidor MySQL local) instalado e ativo.

### 2. Configuração do Banco de Dados
Para que o sistema funcione corretamente, é necessário importar a estrutura das tabelas:

1.  Abra o **PHPMyAdmin** (geralmente em `http://localhost/phpmyadmin`).
2.  Crie um novo banco de dados chamado `sistemacadastro`.
3.  Clique no banco criado e navegue até a aba **"Importar"**.
4.  Selecione o arquivo SQL localizado em: `src/main/resources/sistemacadastro.sql`.
5.  Clique no botão **"Executar"** ao final da página.

### 3. Execução
1.  Clone este repositório em sua máquina local.
2.  Abra o projeto no **IntelliJ IDEA**.
3.  Certifique-se de adicionar o driver **MySQL Connector/J** às bibliotecas do projeto (*Project Structure > Libraries*).
4.  Execute a classe `Main.java`.

---
Desenvolvido por Flávio Morilla, Igor Amaro e Matheus Campioni como parte de estudos em Java Swing e JDBC.