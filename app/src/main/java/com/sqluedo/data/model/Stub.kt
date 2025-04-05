package com.sqluedo.data.model

object Stub {
    val enquetes = listOf(
        Enquete(
            id = 1,
            nomCreateur = "admin_prof",
            nom = "Enquête n1",
            description = "Analyse de la satisfaction des clients pour le trimestre en cours.",
            difficulteIntermediare = 3,
            difficulteDificile = 5,
            mld = "Table utilisateur(NomUtilisateur, Mdp, Role, NomGroupe)",
            solution = "Solution de l'enquête n1",
            indice = "Indice de l'enquête n1",
            nomDatabase = "Database1"
        ),
        Enquete(
            id = 2,
            nomCreateur = "admin_prof",
            nom = "Enquête n2",
            description = "Étude des méthodes de travail et de l'efficacité au sein de l'équipe.",
            difficulteIntermediare = 2,
            difficulteDificile = 4,
            mld = "Table utilisateur(NomUtilisateur, Mdp, Role, NomGroupe)",
            solution = "Solution de l'enquête n2",
            indice = "Indice de l'enquête n2",
            nomDatabase = "Database2"
        ),
        Enquete(
            id = 3,
            nomCreateur = "admin_prof",
            nom = "Enquête n3",
            description = "Évaluation de l'utilisation des ressources matérielles et humaines.",
            difficulteIntermediare = 4,
            difficulteDificile = 6,
            mld = "Table utilisateur(NomUtilisateur, Mdp, Role, NomGroupe)",
            solution = "Solution de l'enquête n3",
            indice = "Indice de l'enquête n3",
            nomDatabase = "Database3"
        ),
        Enquete(
            id = 4,
            nomCreateur = "formateur1",
            nom = "Enquête n4",
            description = "Analyse des tendances du marché et des opportunités pour l'année 2025.",
            difficulteIntermediare = 3,
            difficulteDificile = 5,
            mld = "Table groupe(Nom, NomCreateur, Code, NbUtilisateur)",
            solution = "Solution de l'enquête n4",
            indice = "Indice de l'enquête n4",
            nomDatabase = "Database4"
        ),
        Enquete(
            id = 5,
            nomCreateur = "formateur1",
            nom = "Enquête n5",
            description = "Étude des canaux de communication et de leur efficacité au sein de l'entreprise.",
            difficulteIntermediare = 2,
            difficulteDificile = 4,
            mld = "Table utilisateur(NomUtilisateur, Mdp, Role, NomGroupe)\nTable groupe(Nom, NomCreateur, Code, NbUtilisateur)",
            solution = "Solution de l'enquête n5",
            indice = "Indice de l'enquête n5",
            nomDatabase = "Database5"
        )
    )

    var utilisateur1 = Utilisateur(
        nomUtilisateur = "Utilisateur1",
        nomGroupe = null,
        mdp = "userpass",
        role = "Membre"
    )

    var groupe1 = Group(
        nom = "Groupe A",
        code = "GA123",
        nbUtilisateur = 10,
        nomCreator = utilisateur1
    )
    init {
        utilisateur1.nomGroupe = groupe1
    }
    val statistiques = Statistiques(
        idStatistique = "S001",
        nomUtilisateur = utilisateur1,
        idEnquete = enquetes.first(),
        nbTentatives = 3,
        reussi = true,
        tempsPasse = 120
    )
}