# SQLuedo

SQLuedo est une application Android née d'un constat simple en tant qu'étudiants : apprendre le SQL avec des exercices abstraits (clients, commandes, factures) peut vite devenir répétitif.

Nous avons voulu rendre l'apprentissage plus concret en gamifiant le processus. L'idée est de transformer la base de données en une scène de crime géante.

## Le concept

Le principe est celui d'un Cluedo numérique. L'utilisateur incarne un enquêteur qui doit résoudre des crimes. Mais ici, pas de dés ni de plateau : votre seul outil est le langage SQL.

Concrètement, l'application propose :
* Des scénarios d'enquêtes scriptés (vols, meurtres).
* Une interface pour taper de vraies requêtes SQL directement sur mobile.
* Un système de vérification qui analyse vos résultats pour voir si vous avez trouvé l'indice (l'arme, le lieu ou le suspect).

Si vous savez faire un `SELECT * FROM temoins WHERE lieu = 'Garage'`, vous pouvez commencer à jouer.

## Fonctionnalités

Au-delà du concept de jeu, l'application intègre plusieurs outils pédagogiques et techniques :

**Côté Joueur :**
* Éditeur SQL avec coloration syntaxique et retour d'erreur.
* Progression par niveaux de difficulté.
* Mode "Classe" pour rejoindre un groupe de TD et comparer son avancement avec les autres étudiants.
* Statistiques détaillées (nombre de tentatives, temps de résolution).

**Côté Technique :**
Le projet est découpé en deux parties distinctes :
1. Une application mobile native (Android/Kotlin) qui gère l'interface et la logique de jeu.
2. Une API REST en PHP qui fait l'intermédiaire sécurisé avec la base de données PostgreSQL.

## Stack Technique

Ce projet a été réalisé dans le cadre d'une SAE (Situation d'Apprentissage et d'Évaluation) à l'IUT.

* **Android** : Kotlin, Jetpack Compose pour l'UI, Coroutines pour l'asynchrone.
* **Backend** : PHP natif (sans framework lourd pour la performance), architecture MVC.
* **Base de données** : PostgreSQL.

## Installation et test

Le code source est disponible ici pour ceux qui souhaitent voir comment nous avons géré l'exécution de requêtes SQL arbitraires ou la gestion de l'état sous Jetpack Compose.

1. Clonez ce dépôt.
2. Ouvrez le projet sous Android Studio.
3. Synchronisez le Gradle et lancez l'application sur un émulateur ou un device physique.

Note : L'application nécessite une connexion internet pour interroger l'API de jeu.

## Auteurs

Projet réalisé par [Votre Nom] (Développement Android) et [Nom du collègue] (Backend & Base de données).
