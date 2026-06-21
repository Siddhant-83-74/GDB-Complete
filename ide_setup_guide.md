# [Project Name] - IDE Setup Guide

This document provides instructions for setting up your development environment. A properly configured environment ensures that your code compiles cleanly and adheres to project styling guidelines.

---

## 1. System Package Installation

Before setting up your IDEs, ensure the following core tools are installed on your machine.

| Dependency | Purpose | Minimum Version | Download Link |
|:---|:---|:---|:---|
| **JDK** | Compiles and runs the Spring Boot Backend | Java 17+ (21 recommended) | [Adoptium / Eclipse Temurin](https://adoptium.net/) |
| **Node.js** | Runs the React frontend and package manager | v18+ | [Node.js Official](https://nodejs.org/) |
| **Maven** | Java package manager and build tool | v3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |

*Note: If the Spring Boot project contains an `mvnw` (Maven Wrapper) executable, a local Maven installation is optional.*

---

## 2. VS Code Setup (Frontend)

Visual Studio Code is the recommended IDE for React/JavaScript frontend development. 

### Recommended Extensions
Search for and install the following extensions in the VS Code Extensions marketplace (`Ctrl+Shift+X`):

*   **ESLint** (`dbaeumer.vscode-eslint`): Integrates ESLint into VS Code to highlight syntax and stylistic errors dynamically.
*   **Prettier - Code formatter** (`esbenp.prettier-vscode`): Enforces consistent code style on save.
*   **Simple React Snippets** (`burkeholland.simple-react-snippets`): Provides quick boilerplate generation for React components.
*   *(Optional)* **Extension Pack for Java** (`vscjava.vscode-java-pack`): Only required if you intend to use VS Code for the backend as well.

### VS Code Settings Configuration
To automatically format code upon saving, add this to your VS Code `settings.json`:
```json
{
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.formatOnSave": true,
  "eslint.format.enable": true
}
```

---

## 3. IntelliJ IDEA Setup (Backend)

IntelliJ IDEA (Community or Ultimate) is the industry standard IDE for Spring Boot applications.

### 3.1. Configure Project SDK
1. Open IntelliJ IDEA and open your backend project.
2. Go to **File ➔ Project Structure** (`Ctrl+Alt+Shift+S`).
3. Under **Project Settings ➔ Project**, set the **SDK** to your installed Java 17/21 version.
4. Ensure the **Language level** matches the SDK version.

### 3.2. Enable Annotation Processing (Crucial for Lombok)
Our backend extensively utilizes Lombok to reduce boilerplate code (Getters/Setters/Constructors). Without annotation processing, IntelliJ will highlight Lombok annotations as errors.
1. Go to **File ➔ Settings** (`Ctrl+Alt+S`).
2. Navigate to **Build, Execution, Deployment ➔ Compiler ➔ Annotation Processors**.
3. Check the box labeled **Enable annotation processing**.
4. Click **Apply** and **OK**.

### 3.3. Maven Syncing
Whenever you make changes to `pom.xml` dependencies:
1. Open the **Maven** tool window on the right-hand sidebar.
2. Click the **Reload All Maven Projects** icon (the circular arrows).
3. Wait for the indexing process to finish at the bottom right of the screen.
