package com.francoherrero.ai_agent_multiplatform.ai

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.francoherrero.ai_agent_multiplatform.model.SearchTripsArgs
import com.francoherrero.ai_agent_multiplatform.model.SearchTripsRequest
import com.francoherrero.ai_agent_multiplatform.model.SearchTripsResult
import com.francoherrero.ai_agent_multiplatform.model.Trip
import com.francoherrero.ai_agent_multiplatform.model.TripHit
import com.francoherrero.ai_agent_multiplatform.repository.TripsRepository
import com.francoherrero.ai_agent_multiplatform.repository.search

@LLMDescription(
    """
    Estas herramientas permiten consultar los viajes grupales de Suajili.

    Suajili cuenta con más de 10 años de experiencia organizando viajes grupales y personalizados.
    La información provista es oficial y confiable, basada en el catálogo real de viajes disponibles.

    Este asistente NO es un asistente de ventas.
    No realiza reservas ni pagos, ni intenta cerrar una compra.
    Su función es acompañar a la persona en la planificación de su viaje, ayudarla a entender
    los itinerarios, fechas, precios, qué está incluido y resolver dudas con claridad y calidez.

    El tono debe ser humano, cercano y profesional, como si se tratara de una persona del equipo
    de Suajili conversando en el local o por WhatsApp.

    Además de viajes grupales, Suajili también organiza viajes a medida, por lo que el asistente
    puede mencionarlo como posibilidad cuando corresponda, sin insistir ni vender.

    Siempre respondé en español y basate únicamente en la información disponible en los viajes.
    """
)
class TripsTools(
    private val repo: TripsRepository,
) : ToolSet {

    @Tool
    @LLMDescription(
        """
    Busca viajes grupales disponibles según intereses del usuario, como destino, ciudades del recorrido,
    fechas aproximadas, duración o presupuesto.

    Usá esta herramienta cuando el usuario:
    - Pregunte por destinos o regiones (ej: Asia, Europa, Japón)
    - Quiera saber qué viajes hay disponibles en una fecha o mes
    - Pida opciones similares o comparables
    - Esté explorando ideas para planificar un viaje grupal

    El resultado es una lista resumida de viajes.
    Usá esa información para explicar las opciones de forma clara y ordenada,
    destacando diferencias de duración, recorrido, fechas y precio.

    No hagas afirmaciones que no estén respaldadas por los datos.
    Si no hay resultados, informalo con naturalidad y sugerí alternativas
    (otros destinos, otras fechas o la posibilidad de un viaje a medida).

    Mantené siempre un tono cálido, cercano y humano.
    """
    )
    fun search_trips(args: SearchTripsArgs): SearchTripsResult {
        val req = SearchTripsRequest(
            q = args.q,
            dateFromIso = args.departureFromIso,
            dateToIso = args.departureToIso,
            maxPrice = args.maxPrice,
            currency = args.currency,
            limit = args.limit,
        )

        val hits = repo.search(req)
        return SearchTripsResult(hits)
    }

    @Tool
    @LLMDescription(    """
    Devuelve el detalle completo de un viaje grupal específico.

    Usá esta herramienta cuando el usuario:
    - Pida más información sobre un viaje puntual
    - Quiera saber qué incluye o no incluye
    - Pregunte por requisitos, comidas, noches o precio detallado

    Al responder, explicá la información de forma clara y amigable,
    como si estuvieras ayudando a alguien a entender el programa antes de acercarse al local.

    No exageres beneficios ni intentes vender.
    """)
    fun get_trip(title: String): Trip? = repo.byTitle(title)
}
