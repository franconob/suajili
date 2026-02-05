package com.francoherrero.ai_agent_multiplatform.ui.trips.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.francoherrero.ai_agent_multiplatform.model.TripStatus
import com.francoherrero.ai_agent_multiplatform.model.UserTripDto

@Composable
fun TripCard(
    trip: UserTripDto,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Image with gradient overlay
            if (trip.tripImageUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isHighlighted) 180.dp else 120.dp)
                ) {
                    AsyncImage(
                        model = trip.tripImageUrl,
                        contentDescription = trip.tripTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isHighlighted) 180.dp else 120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isHighlighted) 180.dp else 120.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // Title on image
                    Text(
                        text = trip.tripTitle ?: trip.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Title (if no image)
                if (trip.tripImageUrl == null) {
                    Text(
                        text = trip.tripTitle ?: trip.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Route
                val route = trip.tripRoute
                if (!route.isNullOrEmpty()) {
                    Text(
                        text = route.joinToString(" > "),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isHighlighted)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isHighlighted)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDateRange(trip.departureDate, trip.returnDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isHighlighted)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Travelers
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isHighlighted)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        trip.travelers?.size?.let {
                            Text(
                                text = "${trip.travelers} viajero${if (it > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isHighlighted)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Documents indicator
                val documentsCount = trip.documents?.size ?: 0
                if (documentsCount > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$documentsCount documento${if (documentsCount > 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Status badge
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = trip.status)

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ver detalles",
                        tint = if (isHighlighted)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: TripStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        TripStatus.CONFIRMED -> Triple(
            Color(0xFF4CAF50).copy(alpha = 0.2f),
            Color(0xFF2E7D32),
            "Confirmado"
        )
        TripStatus.PENDING -> Triple(
            Color(0xFFFF9800).copy(alpha = 0.2f),
            Color(0xFFE65100),
            "Pendiente"
        )
        TripStatus.COMPLETED -> Triple(
            Color(0xFF9E9E9E).copy(alpha = 0.2f),
            Color(0xFF616161),
            "Completado"
        )
        TripStatus.CANCELLED -> Triple(
            Color(0xFFF44336).copy(alpha = 0.2f),
            Color(0xFFC62828),
            "Cancelado"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "N/A"
        )
    }

    Box(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private fun formatDateRange(departureDate: String?, returnDate: String?): String {
    if (departureDate == null) return "Fecha por confirmar"

    // Simple date formatting - in a real app you'd use proper date parsing
    val departure = departureDate.take(10)

    return if (returnDate != null) {
        val returnFormatted = returnDate.take(10)
        "$departure - $returnFormatted"
    } else {
        departure
    }
}
