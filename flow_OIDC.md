# Authentification Google OAuth2 + JWT avec Spring Security

Ce projet implémente une architecture de sécurité hybride : il délègue l'authentification initiale à **Google (OIDC)**, puis maintient la session de manière stateless via un **JWT interne** stocké dans un cookie sécurisé.

---

## 🏗️ Architecture du Flux

### 1. Phase d'authentification (OIDC)
1. **Accès initial :** L’utilisateur appelle `/contacts` sans JWT. Spring Security redirige vers Google.
2. **Redirection Google :** ```http
   GET [https://accounts.google.com/o/oauth2/v2/auth](https://accounts.google.com/o/oauth2/v2/auth)
   ?client_id=...
   &redirect_uri=http://localhost:8080/login/oauth2/code/google
   &scope=openid%20email%20profile
   &response_type=code
### 3. Retour de Google avec un code
* Google redirige vers ton application :
  `/login/oauth2/code/google?code=123`

### 4. Traitement par Spring Security
* `OAuth2LoginAuthenticationFilter` intercepte la requête :
    - Échange le code contre des tokens (Access Token + ID Token).
    - Valide l’ID Token (OIDC).
    - Crée un objet `OidcUser`.

### 5. Success Handler (ton code)
* Ton `successHandler` s’exécute :
    - Synchronise l’utilisateur en base (sub, email, role).
    - Génère un **JWT interne**.
    - Envoie le JWT dans un **cookie**.
* Redirection vers `/contacts`.

---

## 🔒 Étapes avec JWT

### 6. Accès à /contacts avec cookie JWT
* L’utilisateur revient sur `/contacts`, cette fois avec le cookie JWT.

### 7. JwtFilter (ton code)
* Le filtre lit le JWT.
* Valide la signature et la date d’expiration.
* Extrait le `sub` et le `role`.
* Crée un objet `Authentication`.

### 8. SecurityContext
* Le `SecurityContext` est rempli avec l’authentification.
* Les contrôleurs/services peuvent accéder à l’utilisateur courant via le `SecurityContextHolder`.

### 9. ContactController
* Le contrôleur applique la logique métier :
    - Filtrage des contacts par `owner.sub`.

---

## 🔄 Résumé du flux
1. **`/contacts`** ➔ redirection Google (OIDC).
2. **Google** ➔ `/login/oauth2/code/google?code=...`.
3. **Spring Security** ➔ échange du code, création `OidcUser`.
4. **SuccessHandler** ➔ sync DB, génère JWT, envoie cookie.
5. **`/contacts` avec cookie JWT** ➔ `JwtFilter` valide et remplit `SecurityContext`.
6. **Controller** ➔ logique métier avec utilisateur authentifié.

---

## ✅ Points clés
* **OIDC (Google) :** utilisé uniquement pour l’authentification initiale.
* **JWT interne :** utilisé pour toutes les requêtes suivantes (Stateless).
* **Séparation des rôles :**
    - **Spring Security** gère le flux OAuth2/OIDC.
    - **Ton code** gère la persistance utilisateur et la génération du JWT.
    - **Le JwtFilter** sécurise les endpoints REST.