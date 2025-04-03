package com.sqluedo.ViewModel

import androidx.lifecycle.ViewModel
import com.sqluedo.data.model.BlocSQL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel pour gérer l'édition des requêtes SQL avec des blocs.
 */
class JeuBlocViewModel : ViewModel() {

    // Liste des blocs SQL dans l'éditeur
    private val _blocsSQL = MutableStateFlow<List<BlocSQL>>(emptyList())
    val blocsSQL: StateFlow<List<BlocSQL>> = _blocsSQL

    // Mode d'édition (blocs ou texte)
    private val _modeBlocActif = MutableStateFlow(true)
    val modeBlocActif: StateFlow<Boolean> = _modeBlocActif

    // Requête SQL générée à partir des blocs
    private val _requeteSQL = MutableStateFlow("")
    val requeteSQL: StateFlow<String> = _requeteSQL

    /**
     * Ajoute un bloc SQL à la fin de la liste
     */
    fun ajouterBloc(bloc: BlocSQL) {
        val nouveauxBlocs = _blocsSQL.value.toMutableList()
        nouveauxBlocs.add(bloc)
        _blocsSQL.value = nouveauxBlocs
        mettreAJourRequeteTexte()
    }

    /**
     * Insère un bloc SQL à l'index donné
     */
    fun ajouterBlocAt(index: Int, bloc: BlocSQL) {
        val nouveauxBlocs = _blocsSQL.value.toMutableList()
        if (index >= 0 && index <= nouveauxBlocs.size) {
            nouveauxBlocs.add(index, bloc)
            _blocsSQL.value = nouveauxBlocs
            mettreAJourRequeteTexte()
        }
    }

    /**
     * Supprime un bloc SQL de la requête
     */
    fun supprimerBloc(index: Int) {
        if (index >= 0 && index < _blocsSQL.value.size) {
            val nouveauxBlocs = _blocsSQL.value.toMutableList()
            nouveauxBlocs.removeAt(index)
            _blocsSQL.value = nouveauxBlocs
            mettreAJourRequeteTexte()
        }
    }

    /**
     * Met à jour un bloc SQL
     */
    fun mettreAJourBloc(index: Int, bloc: BlocSQL) {
        if (index >= 0 && index < _blocsSQL.value.size) {
            val nouveauxBlocs = _blocsSQL.value.toMutableList()
            nouveauxBlocs[index] = bloc
            _blocsSQL.value = nouveauxBlocs
            mettreAJourRequeteTexte()
        }
    }

    /**
     * Met à jour la requête texte à partir des blocs
     */
    private fun mettreAJourRequeteTexte() {
        val requeteBuilder = StringBuilder()

        _blocsSQL.value.forEach { bloc ->
            when (bloc.type) {
                "SELECT" -> requeteBuilder.append("SELECT ${bloc.valeur} ")
                "FROM" -> requeteBuilder.append("FROM ${bloc.valeur} ")
                "WHERE" -> requeteBuilder.append("WHERE ${bloc.valeur} ")
                "AND" -> requeteBuilder.append("AND ${bloc.valeur} ")
                "OR" -> requeteBuilder.append("OR ${bloc.valeur} ")
                "JOIN" -> requeteBuilder.append("JOIN ${bloc.valeur} ")
                "ON" -> requeteBuilder.append("ON ${bloc.valeur} ")
                "GROUP BY" -> requeteBuilder.append("GROUP BY ${bloc.valeur} ")
                "ORDER BY" -> requeteBuilder.append("ORDER BY ${bloc.valeur} ")
                "HAVING" -> requeteBuilder.append("HAVING ${bloc.valeur} ")
                else -> requeteBuilder.append("${bloc.type} ${bloc.valeur} ")
            }
        }

        _requeteSQL.value = requeteBuilder.toString().trim()
    }

    /**
     * Bascule entre le mode bloc et le mode texte
     */
    fun basculerMode() {
        _modeBlocActif.value = !_modeBlocActif.value
        if (_modeBlocActif.value) {
            parserRequeteTexte()
        }
    }

    /**
     * Parse la requête texte pour créer des blocs (implémentation simple)
     */
    private fun parserRequeteTexte() {
        val motsClésSQL = listOf("SELECT", "FROM", "WHERE", "AND", "OR", "JOIN", "ON", "GROUP BY", "ORDER BY", "HAVING")
        val nouveauxBlocs = mutableListOf<BlocSQL>()

        val parties = _requeteSQL.value.split(" ")
        var i = 0
        while (i < parties.size) {
            if (parties[i].uppercase() in motsClésSQL) {
                val motClé = parties[i].uppercase()
                var valeur = ""
                i++
                while (i < parties.size && parties[i].uppercase() !in motsClésSQL) {
                    valeur += "${parties[i]} "
                    i++
                }
                nouveauxBlocs.add(BlocSQL(type = motClé, valeur = valeur.trim()))
                if (i < parties.size) { i-- }
            }
            i++
        }
        _blocsSQL.value = nouveauxBlocs
    }

    /**
     * Met à jour la requête texte directement (mode texte)
     */
    fun mettreAJourRequeteTexte(requete: String) {
        _requeteSQL.value = requete
    }

    /**
     * Retourne la requête SQL actuelle
     */
    fun getRequeteSQL(): String = _requeteSQL.value
}
