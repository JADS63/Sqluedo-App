package com.sqluedo.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

/**
 * Classe pour gérer les deux formats possibles du champ nomGroupe:
 * - Une chaîne simple (API renvoie "Groupe_D2")
 * - Un objet Group complet (notre structure de données)
 */
class GroupOrStringSerializer : KSerializer<Group?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GroupOrString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Group?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            // Lors de la sérialisation, on utilise le nom du groupe
            encoder.encodeString(value.nom)
        }
    }

    override fun deserialize(decoder: Decoder): Group? {
        // Vérifier si la valeur est null
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()

            // Si c'est une chaîne simple
            if (element is JsonPrimitive && element.isString) {
                val nomGroupe = element.jsonPrimitive.content
                // Créer un objet Group temporaire avec les informations minimales
                return Group(
                    nom = nomGroupe,
                    code = "",  // Code inconnu
                    nbUtilisateur = 0,  // Nombre d'utilisateurs inconnu
                    nomCreator = null  // Créateur inconnu
                )
            }
            // Si c'est null
            return null
        }

        // Fallback en cas d'erreur
        return null
    }
}

@Serializable
data class Utilisateur(
    val nomUtilisateur: String,
    @Serializable(with = GroupOrStringSerializer::class)
    var nomGroupe: Group?,
    val mdp: String,
    val role: String,
)