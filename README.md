# PoC Chat — Your Car Your Way
<div align="center">

<img src="frontend/src/assets/banner.png" alt="Your Car Your Way - PoC Chat" width="800"/>

Preuve de concept de la fonctionnalité de chat en temps réel entre clients et agents du service client, dans le cadre du projet **Your Car Your Way**.

---

![Angular](https://img.shields.io/badge/Angular-18-DD0031?style=for-the-badge&logo=angular&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-010101?style=for-the-badge&logo=socketdotio&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

</div>

---

## Stack technique

| Couche | Technologie |
|---|---|
| Frontend | Angular 18 (Standalone Components) |
| Backend | Spring Boot 3.2.5 — Java 17 |
| Base de données | MySQL 8+ |
| Temps réel | WebSocket — STOMP via SockJS |
| Authentification | JWT (Bearer Token) |

---

## Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé :

- [Java 17](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [Node.js 18+](https://nodejs.org/) et npm
- [Angular CLI](https://angular.io/cli) : `npm install -g @angular/cli`
- [MySQL 8+](https://dev.mysql.com/downloads/)

---

## 🗄️ 1. Base de données

### Initialisation

Connectez-vous à MySQL et exécutez le script d'initialisation :

```bash
mysql -u root -p < database/init.sql
```

Ou copiez-collez le contenu du fichier `database/init.sql` directement dans votre client MySQL (MySQL Workbench, DBeaver, etc.).

Ce script crée :
- La base de données `poc_chat`
- Les tables `users`, `conversations`, `chats`
- 5 utilisateurs de test

### Comptes de test

| Email | Mot de passe | Rôle |
|---|---|---|
| `client@mail.com` | `password` | CLIENT |
| `client2@mail.com` | `password` | CLIENT |
| `client3@mail.com` | `password` | CLIENT |
| `agent@mail.com` | `password` | AGENT_SUPPORT |

---

## ⚙️ 2. Backend — Spring Boot

### Configuration

Le fichier de configuration se trouve dans :

```
backend/chat/src/main/resources/application.yml
```

Par défaut, le backend se connecte à MySQL avec ces paramètres :

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/poc_chat
    username: root
    password:
```

Si votre configuration MySQL est différente, modifiez ces valeurs ou définissez les variables d'environnement suivantes :

```bash
DB_URL=jdbc:mysql://localhost:3306/poc_chat
DB_USER=votre_utilisateur
DB_PASSWORD=votre_mot_de_passe
JWT_SECRET=votre_secret_jwt_minimum_32_caracteres
```

### Lancement

```bash
cd backend/chat
mvn spring-boot:run
```

Le backend démarre sur [**http://localhost:8080**](http://localhost:8080)

### Vérification

```bash
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"client@test.com","password":"password"}'
```

Une réponse avec un token JWT confirme que le backend fonctionne.

---

## 🖥️ 3. Frontend — Angular

### Installation des dépendances

```bash
cd frontend/chat-front
npm install
```

### Lancement

```bash
ng serve
```

Le frontend démarre sur [**http://localhost:4200**](http://localhost:4200)

---

## Ordre de lancement

> ⚠️ Respectez cet ordre pour éviter les erreurs de connexion.

1. **MySQL** — s'assurer que le serveur est démarré
2. **Backend** — `mvn spring-boot:run`
3. **Frontend** — `ng serve`
4. Ouvrir [**http://localhost:4200**](http://localhost:4200)

---

## Scénario de test

### Tester le chat en temps réel

1. Ouvrir **deux fenêtres** de navigation (une normale + une privée)
2. **Fenêtre 1** — Se connecter avec `agent@mail.com`
3. **Fenêtre 2** — Se connecter avec `client@mail.com`
4. Côté client : la conversation démarre automatiquement
5. Côté agent : la conversation apparaît dans "En attente" (rafraîchissement toutes les 5 secondes)
6. Cliquer sur **Rejoindre** côté agent
7. Les deux parties peuvent désormais échanger en temps réel

### Tester plusieurs conversations simultanées

1. Ouvrir **une, deux ou trois fenêtres** de navigation privée
2. Se connecter avec `client@mail.com`, `client2@mail.com`, `client3@mail.com`. 
3. Se connecter avec `agent@mail.com` dans une fenêtre normale 
4. Observer les conversations s'empiler dans la liste "En attente" côté agent

---
