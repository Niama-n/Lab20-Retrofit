# TP 20 — NumberBookApp : Carnet de contacts Android avec API REST (Retrofit)

**NumberBookApp** est une application Android native développée en Java. Elle permet de lire les contacts stockés sur le téléphone, de les afficher dans une interface moderne, de les synchroniser vers un serveur distant (PHP + MySQL via XAMPP), et de rechercher des entrées directement dans la base de données distante.

Ce projet met en pratique trois compétences fondamentales du développement mobile :
- l'accès aux données système Android (carnet de contacts),
- la communication réseau asynchrone avec une API REST,
- la construction d'une interface utilisateur réactive avec `RecyclerView`.

---
## Voila la demonstration

<img width="720" height="1600" alt="1" src="https://github.com/user-attachments/assets/9c2bd49c-22cb-4b0c-8ff3-2b4d385d5ba3" />
<img width="720" height="1600" alt="2" src="https://github.com/user-attachments/assets/a4ea178e-d2c7-4f71-a96e-ab0f9433cf85" />

## Table des matières

1. [Objectifs pédagogiques](#objectifs-pédagogiques)
2. [Fonctionnalités](#fonctionnalités)
3. [Scénario d'utilisation](#scénario-dutilisation)
4. [Stack technique](#stack-technique)
5. [Architecture du projet](#architecture-du-projet)
6. [Explication des classes Java](#explication-des-classes-java)
7. [Fonctionnement détaillé de chaque action](#fonctionnement-détaillé-de-chaque-action)
8. [Comprendre Retrofit et Gson](#comprendre-retrofit-et-gson)
9. [Interface utilisateur](#interface-utilisateur)
10. [API REST côté serveur](#api-rest-côté-serveur)
11. [Prérequis](#prérequis)
12. [Guide d'installation pas à pas](#guide-dinstallation-pas-à-pas)
13. [Vérification et tests manuels](#vérification-et-tests-manuels)
14. [Dépannage](#dépannage)
15. [Captures d'écran](#captures-décran)

---

## Objectifs pédagogiques

Ce travail pratique vise à comprendre comment une application mobile communique avec un backend web. Chaque objectif correspond à une partie concrète du code.

| Objectif | Ce qu'on apprend | Où c'est dans le code |
|----------|------------------|------------------------|
| **Lire les contacts Android** | Utiliser `ContentResolver` et `ContactsContract` pour interroger le carnet d'adresses du téléphone | `MainActivity.importDeviceContacts()` |
| **Gérer les permissions runtime** | Demander `READ_CONTACTS` à l'exécution (obligatoire depuis Android 6+) | `MainActivity.requestContactsAccess()` |
| **Afficher une liste dynamique** | RecyclerView + Adapter pour recycler les vues et gagner en performance | `PhoneListAdapter` |
| **Consommer une API REST** | Déclarer des endpoints typés et exécuter des requêtes HTTP | `RemoteEndpoints` + Retrofit |
| **Sérialiser du JSON** | Convertir des objets Java ↔ JSON avec Gson et `@SerializedName` | `PhoneEntry`, `ServerResult` |
| **Séparer les responsabilités** | UI, logique métier et réseau dans des fichiers distincts | Architecture en couches |

### Compétences visées à la fin du TP

- Savoir configurer Retrofit dans un projet Gradle Android
- Comprendre la différence entre requête synchrone et asynchrone (`enqueue`)
- Savoir adapter l'URL du serveur selon émulateur ou appareil physique
- Maîtriser le flux permission → lecture données → affichage UI

---

## Fonctionnalités

| Fonctionnalité | Description |
|----------------|-------------|
| **Import local** | Récupère tous les numéros du carnet Android et les trie par nom (A→Z) |
| **Affichage en cartes** | Chaque contact apparaît dans une carte arrondie (nom + numéro) |
| **Upload vers serveur** | Envoie chaque entrée locale au backend via `POST insertContact.php` |
| **Recherche distante** | Interroge MySQL via `GET searchContact.php?keyword=...` |
| **Messages utilisateur** | Toasts informatifs (permission refusée, erreur réseau, liste vide…) |
| **Thème Material 3** | Palette teal / corail, mode clair et mode sombre |
| **Interface en anglais** | Tous les libellés centralisés dans `strings.xml` |

---

## Scénario d'utilisation

Voici le parcours typique d'un utilisateur :

```
1. Ouvrir NumberBookApp
        ↓
2. Appuyer sur « Import from Device »
        ↓
   → L'app demande la permission READ_CONTACTS (si pas encore accordée)
        ↓
   → Les contacts du téléphone s'affichent dans la liste
        ↓
3. Appuyer sur « Upload to Server »
        ↓
   → Chaque contact est envoyé en JSON vers le serveur PHP
        ↓
   → Les données sont stockées dans la table MySQL `contacts`
        ↓
4. Saisir un nom ou numéro dans la barre de recherche
        ↓
5. Appuyer sur « Find »
        ↓
   → L'app interroge le serveur et affiche uniquement les résultats correspondants
```

---

## Stack technique

| Couche | Technologie | Rôle détaillé |
|--------|-------------|---------------|
| **UI** | XML Layouts + Material 3 | Définit l'apparence : boutons, champs, liste, couleurs |
| **Logique** | Java 11 | Orchestre les actions utilisateur dans `MainActivity` |
| **Liste** | RecyclerView 1.3.2 | Affiche des milliers de contacts sans ralentir l'app |
| **Réseau** | Retrofit 2.11 | Transforme une interface Java en client HTTP |
| **JSON** | Gson (via converter-gson) | Convertit automatiquement JSON ↔ objets Java |
| **Backend** | PHP 8 + Apache (XAMPP) | Reçoit les requêtes HTTP et exécute du SQL |
| **BDD** | MySQL | Stocke les contacts synchronisés |
| **Build** | Gradle Kotlin DSL | Gère les dépendances et la compilation |
| **Tests** | JUnit 4 + Espresso | Vérifie le modèle et le package de l'app |

### Dépendances principales (`app/build.gradle.kts`)

```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
```

| Paramètre Android | Valeur |
|-------------------|--------|
| `applicationId` | `com.numberbook.app` |
| `minSdk` | 28 (Android 9 Pie) |
| `targetSdk` | 36 |
| `compileSdk` | 36 |

---

## Architecture du projet

Le projet suit une **architecture en couches** : chaque couche a une responsabilité unique et ne dépend que de la couche inférieure.

```
┌─────────────────────────────────────────────────────────────┐
│                    COUCHE PRÉSENTATION                       │
│  activity_main.xml · item_phone_entry.xml · strings.xml      │
│  colors.xml · themes.xml · PhoneListAdapter                  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    COUCHE LOGIQUE (Métier)                   │
│  MainActivity — permissions, import contacts, orchestration  │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    COUCHE RÉSEAU                             │
│  RemoteEndpoints · HttpServiceFactory · PhoneEntry           │
│  ServerResult                                                │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP / JSON
┌──────────────────────────▼──────────────────────────────────┐
│                    COUCHE SERVEUR (externe)                  │
│  PHP (insertContact, getAllContacts, searchContact)          │
│  MySQL (table contacts)                                      │
└─────────────────────────────────────────────────────────────┘
```

### Arborescence des fichiers

```
NumberBookApp/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/numberbook/app/
│   │   │   ├── MainActivity.java          ← Activité principale (cerveau de l'app)
│   │   │   ├── PhoneEntry.java            ← Modèle : une entrée nom + numéro
│   │   │   ├── PhoneListAdapter.java      ← Lie les données à la RecyclerView
│   │   │   ├── RemoteEndpoints.java       ← Contrat HTTP (interface Retrofit)
│   │   │   ├── HttpServiceFactory.java    ← Crée l'instance Retrofit (singleton)
│   │   │   └── ServerResult.java          ← Réponse { success, message } du serveur
│   │   │
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml      ← Écran principal (boutons + liste)
│   │   │   │   └── item_phone_entry.xml   ← Template d'une carte contact
│   │   │   ├── values/
│   │   │   │   ├── colors.xml             ← Palette de couleurs
│   │   │   │   ├── strings.xml            ← Textes de l'interface
│   │   │   │   └── themes.xml             ← Thème Material 3
│   │   │   ├── values-night/
│   │   │   │   └── themes.xml             ← Variante mode sombre
│   │   │   └── drawable/                  ← Formes arrondies (recherche, cartes)
│   │   │
│   │   └── AndroidManifest.xml            ← Permissions, activité, cleartext HTTP
│   │
│   ├── src/test/                          ← Tests unitaires (JVM locale)
│   └── src/androidTest/                   ← Tests instrumentés (sur appareil)
│
├── gradle/                                ← Wrapper Gradle
├── settings.gradle.kts                    ← Nom du projet : NumberBookApp
└── README.md
```

### Flux de données global

```
┌─────────────────┐   ContentProvider    ┌──────────────┐
│ Carnet Android  │ ───────────────────► │ MainActivity │
│ (ContactsContract)                    └──────┬───────┘
└─────────────────┘                            │
                    ┌──────────────────────────┼──────────────────────────┐
                    ▼                          ▼                          ▼
           ┌────────────────┐       ┌─────────────────┐       ┌────────────────┐
           │ PhoneListAdapter│       │ RemoteEndpoints │       │   strings.xml  │
           │  (RecyclerView) │       │    (Retrofit)   │       │  (textes UI)   │
           └────────────────┘       └────────┬────────┘       └────────────────┘
                                             │ HTTP + JSON
                                    ┌────────▼────────┐
                                    │  API PHP/XAMPP  │
                                    └────────┬────────┘
                                             │ SQL
                                    ┌────────▼────────┐
                                    │      MySQL      │
                                    └─────────────────┘
```

---

## Explication des classes Java

### `MainActivity.java` — Le contrôleur principal

C'est le **point d'entrée** de l'application (déclarée dans le `AndroidManifest.xml`). Elle hérite de `AppCompatActivity` et coordonne toute l'interface.

| Méthode | Rôle |
|---------|------|
| `onCreate()` | Initialise la vue, le RecyclerView et le service réseau |
| `bindViews()` | Relie les composants XML (boutons, champ recherche, liste) aux variables Java |
| `configureRecyclerView()` | Configure la liste verticale et attache l'adaptateur |
| `requestContactsAccess()` | Vérifie si la permission contacts est accordée, sinon la demande |
| `importDeviceContacts()` | Lit le carnet via `ContentResolver` et remplit `localEntries` |
| `transmitEntriesToBackend()` | Envoie chaque `PhoneEntry` au serveur en boucle |
| `performRemoteLookup()` | Lance une recherche distante avec le mot-clé saisi |

**Pourquoi séparer en méthodes ?** Chaque méthode a une seule responsabilité, ce qui rend le code lisible, testable et facile à modifier.

---

### `PhoneEntry.java` — Le modèle de données

Représente **un contact** côté application. Contient le nom et le numéro lus depuis le téléphone, plus les champs renvoyés par le serveur (`id`, `source`, `created_at`).

```java
@SerializedName("name")      // nom du champ dans le JSON PHP
private String fullName;     // nom de la variable Java (peut être différent)
```

**Pourquoi `@SerializedName` ?** Gson fait le lien entre les noms JSON du serveur (`name`, `phone`) et les noms Java (`fullName`, `mobileNumber`). Sans cette annotation, la désérialisation échouerait car les noms ne correspondent pas.

---

### `PhoneListAdapter.java` — L'adaptateur de liste

Fait le pont entre la **liste de données** (`List<PhoneEntry>`) et la **RecyclerView** affichée à l'écran.

| Méthode | Rôle |
|---------|------|
| `onCreateViewHolder()` | Crée une nouvelle carte visuelle (`item_phone_entry.xml`) |
| `onBindViewHolder()` | Remplit la carte avec le nom et le numéro à la position donnée |
| `getItemCount()` | Indique combien d'éléments afficher |
| `refreshEntries()` | Met à jour la liste et rafraîchit l'affichage |

**Pourquoi RecyclerView et pas ListView ?** RecyclerView recycle les vues hors écran : avec 500 contacts, seules ~10 vues sont en mémoire au lieu de 500.

---

### `RemoteEndpoints.java` — Le contrat API

Interface Java dont Retrofit génère automatiquement l'implémentation HTTP. Chaque méthode annotée correspond à un endpoint PHP :

```java
@POST("insertContact.php")
Call<ServerResult> pushEntry(@Body PhoneEntry entry);
// → POST http://10.0.2.2/numberbook-api/api/insertContact.php
//   Corps : {"name":"Alice","phone":"0612345678"}
```

| Annotation | Signification |
|------------|---------------|
| `@POST` / `@GET` | Méthode HTTP utilisée |
| `@Body` | L'objet sera converti en JSON dans le corps de la requête |
| `@Query("keyword")` | Ajoute `?keyword=valeur` à l'URL |
| `Call<T>` | Représente une requête asynchrone dont la réponse est de type `T` |

---

### `HttpServiceFactory.java` — La fabrique réseau

Crée et conserve **une seule instance** de Retrofit (pattern Singleton). Évite de recréer le client HTTP à chaque requête, ce qui économise mémoire et temps.

```java
private static final String API_ROOT = "http://10.0.2.2/numberbook-api/api/";
```

| Environnement | URL à utiliser | Explication |
|---------------|----------------|-------------|
| Émulateur Android Studio | `http://10.0.2.2/...` | `10.0.2.2` est l'alias de `localhost` du PC hôte |
| Téléphone physique (Wi-Fi) | `http://192.168.x.x/...` | IP locale du PC sur le réseau Wi-Fi |

---

### `ServerResult.java` — La réponse serveur

Modélise la réponse JSON renvoyée par `insertContact.php` :

```json
{ "success": true, "message": "Contact inséré avec succès" }
```

Utilisé uniquement lors de l'upload pour savoir si l'opération a réussi (même si l'app n'affiche pas encore ce détail à l'utilisateur).

---

## Fonctionnement détaillé de chaque action

### Action 1 — Import des contacts (`Import from Device`)

```
Utilisateur clique
       ↓
requestContactsAccess()
       ↓
Permission déjà accordée ? ──Non──► Popup système « Autoriser l'accès aux contacts ? »
       │                                      ↓
      Oui                              Oui → importDeviceContacts()
       ↓                               Non → Toast « Contacts access was denied »
importDeviceContacts()
       ↓
ContentResolver.query(ContactsContract...) → Cursor
       ↓
Pour chaque ligne : extraire DISPLAY_NAME + NUMBER
       ↓
Créer PhoneEntry → ajouter à localEntries
       ↓
listAdapter.refreshEntries() → RecyclerView mise à jour
       ↓
Toast « X entries loaded from device »
```

**Point clé :** `ContactsContract.CommonDataKinds.Phone.CONTENT_URI` est l'URI standard Android pour accéder aux numéros de téléphone, pas aux contacts email ou adresse.

---

### Action 2 — Upload vers le serveur (`Upload to Server`)

```
Utilisateur clique
       ↓
localEntries est vide ? ──Oui──► Toast « No entries to upload — import first »
       │
      Non
       ↓
Pour chaque PhoneEntry dans localEntries :
       ↓
remoteService.pushEntry(entry).enqueue(callback)
       ↓
Retrofit convertit PhoneEntry → JSON {"name":"...","phone":"..."}
       ↓
POST http://10.0.2.2/numberbook-api/api/insertContact.php
       ↓
PHP insère dans MySQL → renvoie {"success":true,"message":"..."}
       ↓
onResponse() ou onFailure() (erreur réseau → Toast)
       ↓
Toast « Upload in progress… »
```

**Point clé :** `.enqueue()` exécute la requête **en arrière-plan** (thread séparé). L'interface ne se bloque pas pendant l'envoi. Sans `enqueue()`, l'app planterait avec une `NetworkOnMainThreadException`.

---

### Action 3 — Recherche distante (`Find`)

```
Utilisateur saisit un mot-clé + clique Find
       ↓
Champ vide ? ──Oui──► Toast « Please enter a search term »
       │
      Non
       ↓
remoteService.lookupEntries(queryText).enqueue(callback)
       ↓
GET http://10.0.2.2/numberbook-api/api/searchContact.php?keyword=alice
       ↓
PHP exécute SELECT ... WHERE name LIKE '%alice%' OR phone LIKE '%alice%'
       ↓
Renvoie JSON : [{"id":1,"name":"Alice","phone":"0612..."}, ...]
       ↓
Gson convertit JSON → List<PhoneEntry>
       ↓
listAdapter.refreshEntries(response.body()) → liste mise à jour
```

**Point clé :** La recherche interroge le **serveur**, pas la liste locale. Même si vous n'avez pas importé de contacts, vous pouvez chercher ce qui est déjà en base MySQL.

---

## Comprendre Retrofit et Gson

### Qu'est-ce que Retrofit ?

Retrofit est une bibliothèque Square qui **transforme une interface Java en client HTTP**. Au lieu d'écrire manuellement :

```java
// Sans Retrofit (code verbeux et fragile)
URL url = new URL("http://10.0.2.2/numberbook-api/api/searchContact.php?keyword=" + keyword);
HttpURLConnection conn = (HttpURLConnection) url.openConnection();
conn.setRequestMethod("GET");
// ... lire le flux, parser le JSON manuellement ...
```

On écrit simplement :

```java
// Avec Retrofit (déclaratif et typé)
remoteService.lookupEntries(keyword).enqueue(callback);
```

### Chaîne de conversion Gson

```
PhoneEntry (Java)  ──Gson──►  {"name":"Alice","phone":"0612"}  ──HTTP──►  Serveur PHP
PhoneEntry (Java)  ◄──Gson──  [{"id":1,"name":"Alice",...}]   ◄──HTTP──  Serveur PHP
```

Gson utilise les annotations `@SerializedName` pour faire correspondre les noms de champs JSON et Java.

### Pattern Singleton dans `HttpServiceFactory`

```java
public static Retrofit obtainInstance() {
    if (sharedInstance == null) {          // créé une seule fois
        sharedInstance = new Retrofit.Builder()
            .baseUrl(API_ROOT)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }
    return sharedInstance;
}
```

---

## Interface utilisateur

### Structure de l'écran principal (`activity_main.xml`)

```
┌──────────────────────────────────────┐
│  Phone Directory                     │  ← Titre (teal foncé, 26sp)
│  Manage and sync your contacts       │  ← Sous-titre (gris)
│                                      │
│  ┌────────────────────────────────┐  │
│  │     Import from Device         │  │  ← Bouton teal
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │     Upload to Server           │  │  ← Bouton corail
│  └────────────────────────────────┘  │
│                                      │
│  ┌──────────────────────┬────────┐  │
│  │ Type a name or...    │  Find  │  │  ← Barre de recherche
│  └──────────────────────┴────────┘  │
│                                      │
│  ┌────────────────────────────────┐  │
│  │  Alice Martin                  │  │  ← Carte contact
│  │  +33 6 12 34 56 78             │  │
│  ├────────────────────────────────┤  │
│  │  Bob Dupont                    │  │
│  │  06 98 76 54 32                │  │
│  └────────────────────────────────┘  │
│         (RecyclerView scrollable)    │
└──────────────────────────────────────┘
```

### Palette de couleurs

| Nom | Code hex | Utilisation |
|-----|----------|-------------|
| Teal Primary | `#00695C` | Bouton import, bordure recherche |
| Teal Dark | `#004D40` | Barre de statut, bouton Find |
| Coral Accent | `#FF6E40` | Bouton upload (action secondaire importante) |
| Background | `#E8EAF6` | Fond général de l'écran |
| Surface | `#FFFFFF` | Cartes et champ de recherche |
| Text Primary | `#1C2833` | Noms des contacts, titre |
| Text Secondary | `#546E7A` | Numéros, sous-titre |

### Permissions Android (`AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.INTERNET" />
```

| Permission | Pourquoi elle est nécessaire |
|------------|------------------------------|
| `READ_CONTACTS` | Lire le carnet d'adresses du téléphone |
| `INTERNET` | Envoyer et recevoir des requêtes HTTP |
| `usesCleartextTraffic="true"` | Autoriser HTTP non chiffré (XAMPP local n'a pas de HTTPS) |

---

## API REST côté serveur

### Endpoints

| Méthode | URL | Corps / Paramètres | Réponse |
|---------|-----|-------------------|---------|
| `POST` | `/insertContact.php` | JSON `{name, phone}` | `{success, message}` |
| `GET` | `/getAllContacts.php` | — | `[{id, name, phone, source, created_at}]` |
| `GET` | `/searchContact.php` | `?keyword=texte` | `[{id, name, phone, ...}]` |

### Exemple — Insertion d'un contact

**Requête :**
```http
POST /numberbook-api/api/insertContact.php
Content-Type: application/json

{
  "name": "Jean Dupont",
  "phone": "+33 6 12 34 56 78"
}
```

**Réponse :**
```json
{
  "success": true,
  "message": "Contact inséré avec succès"
}
```

### Exemple — Recherche

**Requête :**
```http
GET /numberbook-api/api/searchContact.php?keyword=dupont
```

**Réponse :**
```json
[
  {
    "id": 3,
    "name": "Jean Dupont",
    "phone": "+33 6 12 34 56 78",
    "source": "android",
    "created_at": "2026-06-05 14:30:00"
  }
]
```

### Structure de la base MySQL

```sql
CREATE DATABASE numberbook_db;
USE numberbook_db;

CREATE TABLE contacts (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    source     VARCHAR(50)  DEFAULT 'android',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
```

| Colonne | Type | Description |
|---------|------|-------------|
| `id` | INT | Identifiant unique auto-incrémenté |
| `name` | VARCHAR(100) | Nom complet du contact |
| `phone` | VARCHAR(30) | Numéro de téléphone |
| `source` | VARCHAR(50) | Origine de l'entrée (`android` par défaut) |
| `created_at` | TIMESTAMP | Date et heure d'insertion |

---

## Prérequis

| Outil | Version minimale | Vérification |
|-------|------------------|--------------|
| Android Studio | Récente (SDK 36) | `File > Project Structure > SDK` |
| JDK | 11+ | `java -version` dans le terminal |
| XAMPP | Apache + MySQL | Panneau de contrôle XAMPP |
| Émulateur ou téléphone | API 28+ | Android 9 minimum |
| Connexion réseau | Wi-Fi (appareil physique) | Même réseau que le PC |

---

## Guide d'installation pas à pas

### Étape 1 — Obtenir et ouvrir le projet

```bash
git clone <url-du-depot>
cd NumberBookApp
```

1. Ouvrir Android Studio → **File > Open** → sélectionner le dossier
2. Attendre la synchronisation Gradle (barre de progression en bas)
3. Si demandé, accepter l'installation du SDK 36

---

### Étape 2 — Installer et démarrer XAMPP

1. Télécharger XAMPP : [https://www.apachefriends.org](https://www.apachefriends.org)
2. Installer et lancer le **XAMPP Control Panel**
3. Cliquer **Start** sur **Apache** et **MySQL** (les deux doivent devenir verts)
4. Vérifier que Apache fonctionne : ouvrir `http://localhost` dans un navigateur

---

### Étape 3 — Déployer les scripts PHP

Créer le dossier et y placer les fichiers PHP de l'API :

```
C:\xampp\htdocs\numberbook-api\api\
├── config.php            ← Connexion MySQL (host, user, password, dbname)
├── insertContact.php     ← POST : insère un contact
├── getAllContacts.php    ← GET  : liste tous les contacts
└── searchContact.php     ← GET  : recherche par mot-clé
```

Vérifier l'API dans le navigateur :
```
http://localhost/numberbook-api/api/getAllContacts.php
```
→ Doit renvoyer `[]` (tableau vide) ou une liste JSON.

---

### Étape 4 — Créer la base de données MySQL

1. Ouvrir **phpMyAdmin** : `http://localhost/phpmyadmin`
2. Cliquer **Nouvelle base de données** → nom : `numberbook_db` → Interclassement : `utf8mb4_general_ci`
3. Sélectionner `numberbook_db` → onglet **SQL** → coller et exécuter :

```sql
CREATE TABLE contacts (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    phone      VARCHAR(30)  NOT NULL,
    source     VARCHAR(50)  DEFAULT 'android',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
```

4. Vérifier que la table `contacts` apparaît dans le panneau gauche

---

### Étape 5 — Configurer l'URL du serveur dans l'app

Ouvrir `HttpServiceFactory.java` et choisir la bonne URL :

```java
// ✅ Émulateur Android Studio
private static final String API_ROOT = "http://10.0.2.2/numberbook-api/api/";

// ✅ Téléphone physique (décommenter et adapter l'IP)
// private static final String API_ROOT = "http://192.168.1.42/numberbook-api/api/";
```

**Trouver l'IP locale du PC :**
```bash
# Windows
ipconfig
# Chercher "Adresse IPv4" sous la carte Wi-Fi (ex: 192.168.1.42)

# Linux / Mac
ifconfig
# ou : ip addr show
```

> Le téléphone et le PC doivent être sur le **même réseau Wi-Fi**.

---

### Étape 6 — Vérifier le AndroidManifest.xml

Confirmer la présence de ces éléments (déjà configurés dans le projet) :

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_CONTACTS" />

<application
    android:usesCleartextTraffic="true"
    android:theme="@style/Theme.NumberBookApp"
    ... >
    <activity android:name=".MainActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

---

### Étape 7 — Compiler et lancer l'application

1. Dans Android Studio, sélectionner un émulateur (ex: Pixel 6 API 34) ou brancher un téléphone
2. Activer le **débogage USB** sur le téléphone (`Paramètres > Options développeur`)
3. Cliquer sur le bouton **Run ▶** (ou `Shift + F10`)
4. Attendre la compilation et l'installation automatique
5. À l'ouverture, accorder la permission **Contacts** quand la popup apparaît

**Compilation en ligne de commande :**
```bash
# Windows
.\gradlew.bat assembleDebug

# L'APK généré se trouve dans :
# app/build/outputs/apk/debug/app-debug.apk
```

---

## Vérification et tests manuels

### Checklist de validation

| # | Test | Action | Résultat attendu | ✅ |
|---|------|--------|------------------|----|
| 1 | Lancement | Ouvrir l'app | Écran « Phone Directory » s'affiche | |
| 2 | Permission | Cliquer « Import from Device » | Popup permission → Accepter | |
| 3 | Import | Après permission accordée | Liste remplie, toast « X entries loaded » | |
| 4 | Upload | Cliquer « Upload to Server » | Toast « Upload in progress… » | |
| 5 | BDD | Vérifier phpMyAdmin | Lignes visibles dans `contacts` | |
| 6 | Recherche | Taper un nom → « Find » | Liste filtrée avec les résultats serveur | |
| 7 | Erreur réseau | Stopper Apache → Upload | Toast « Connection error » | |
| 8 | Liste vide | Upload sans import préalable | Toast « No entries to upload » | |

### Test rapide de l'API avec le navigateur

```
# Tous les contacts
http://localhost/numberbook-api/api/getAllContacts.php

# Recherche
http://localhost/numberbook-api/api/searchContact.php?keyword=jean
```

### Tests automatisés inclus

```bash
# Tests unitaires (modèle PhoneEntry)
.\gradlew.bat test

# Tests instrumentés (package de l'app)
.\gradlew.bat connectedAndroidTest
```

---

## Dépannage

| Problème | Cause probable | Solution |
|----------|----------------|----------|
| `Connection error` à l'upload | Apache arrêté ou URL incorrecte | Démarrer Apache, vérifier `API_ROOT` |
| Liste vide après import | Permission refusée | `Paramètres > Apps > NumberBookApp > Autorisations > Contacts` |
| `Cleartext traffic not permitted` | HTTP bloqué par Android | Ajouter `usesCleartextTraffic="true"` dans le manifeste |
| Appareil physique ne connecte pas | IP incorrecte ou réseau différent | Vérifier `ipconfig`, même Wi-Fi PC/téléphone |
| `10.0.2.2` ne fonctionne pas sur téléphone | Cette IP est réservée à l'émulateur | Utiliser l'IP locale `192.168.x.x` |
| Recherche sans résultat | Base vide ou mot-clé incorrect | Vérifier phpMyAdmin, tester l'URL dans le navigateur |
| Gradle sync failed | SDK manquant | `File > Settings > Android SDK` → installer API 36 |
| Erreur `NetworkOnMainThreadException` | Requête HTTP sur le thread principal | Toujours utiliser `.enqueue()`, jamais `.execute()` |

### Vérifier la connectivité depuis l'émulateur

Dans l'émulateur, ouvrir le navigateur et aller à :
```
http://10.0.2.2/numberbook-api/api/getAllContacts.php
```
Si ça fonctionne dans le navigateur de l'émulateur mais pas dans l'app, le problème est dans le code Java. Sinon, c'est XAMPP ou l'URL.

---

## Réalisé par

**NIAMA NAFTAOUI**
