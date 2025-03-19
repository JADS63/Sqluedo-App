SQLuedo - Tâches Restantes Avant Rendu Final
📋 Vue d'ensemble
Ce document énumère les fonctionnalités et corrections à implémenter avant le rendu final du projet SQLuedo, une application d'apprentissage SQL sous forme d'enquêtes.
🛠️ Corrections à apporter au code existant
Navigation

Corriger l'implémentation de la navigation dans Navigation.kt :

Résoudre les problèmes de paramètres manquants dans InformationsScreen, JeuScreen et ResultatScreen
Mettre à jour la navigation pour utiliser la même structure que dans Navigation.kt avec notation exacte
Implémenter la gestion des arguments pour les enquêtes sélectionnées



Interface utilisateur

Finaliser les écrans partiellement implémentés :

Compléter ResultatScreen.kt qui est actuellement vide
Implémenter l'interface utilisateur complète pour InformationsScreen.kt



🚀 Développement des fonctionnalités
Repositories et ViewModels

Implémenter les repositories :

Compléter GroupeRepository.java
Compléter StatistiquesRepository.java
Compléter UtilisateurRepository.java


Développer les ViewModels :

Implémenter EnqueteListViewModel.kt
Implémenter EnqueteDetailViewModel.kt
Implémenter UserConnexionViewModel.kt
Implémenter UserInscriptionViewModel.kt
Implémenter GroupeListViewModel.kt
Implémenter GroupeDetailViewModel.kt
Implémenter CreateGroupeViewModel.kt



Fonctionnalités de l'application

Authentification :

Implémenter la logique de connexion utilisateur
Implémenter la logique d'inscription utilisateur
Ajouter la validation des formulaires


Gestion des groupes :

Fonctionnalité pour créer un groupe
Fonctionnalité pour rejoindre un groupe
Fonctionnalité pour quitter un groupe


Fonctionnalités de jeu :

Implémenter l'exécution des requêtes SQL
Créer le système de vérification des réponses
Ajouter des indicateurs de progression


Statistiques :

Implémenter le suivi des tentatives
Implémenter le calcul du temps passé
Créer la visualisation des statistiques



API et services

Connexion à l'API :

Remplacer LienVersLApi dans ApiService.kt par l'URL réelle
Compléter les endpoints API manquants
Implémenter l'intercepteur pour l'authentification



🧪 Tests et validation

Tests unitaires :

Ajouter des tests pour les ViewModels
Ajouter des tests pour les Repositories


Tests d'interface :

Vérifier la compatibilité des interfaces sur différentes tailles d'écran
Tester les cas d'erreur (pas de connexion, échec d'authentification)



📱 Optimisations

Performance :

Gérer correctement le cycle de vie des composables
Optimiser les requêtes réseau avec la mise en cache


Interface utilisateur :

Uniformiser le style dans toute l'application
Ajouter des animations de transition entre les écrans
Améliorer l'accessibilité (taille des textes, contraste)



🚧 Préparation au déploiement

Nettoyage du code :

Supprimer le code commenté inutile
Améliorer la documentation
Standardiser les conventions de nommage


Ressources :

Optimiser les ressources graphiques
Compléter les traductions dans strings.xml



📆 Priorités suggérées

Haute priorité : Corriger la navigation et compléter les écrans vides
Moyenne priorité : Implémenter les repositories et ViewModels
Moyenne priorité : Connecter l'application à l'API
Basse priorité : Optimisations et préparation au déploiement


📊 Progression estimée
CatégorieProgressionNavigation60%UI/UX70%Repositories10%ViewModels10%Services API40%Fonctionnalités de jeu50%Tests5%