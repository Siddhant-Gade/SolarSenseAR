package com.solarsensear.ui.screens.vendors

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solarsensear.data.mock.MockData
import com.solarsensear.data.models.Vendor
import com.solarsensear.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(onBack: () -> Unit) {
    val vendors = MockData.sampleVendors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Solar Installers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${vendors.size} verified installers near you", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Info banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Info.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, null, tint = Info, modifier = Modifier.size(18.dp))
                        Text(
                            "All vendors are verified and MNRE-certified",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            items(vendors) { vendor ->
                VendorDetailCard(vendor = vendor)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun VendorDetailCard(vendor: Vendor) {
    val context = LocalContext.current

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Brush.linearGradient(listOf(Navy700, Navy500)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vendor.name.first().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                vendor.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (vendor.verified) {
                                Icon(
                                    Icons.Filled.Verified,
                                    contentDescription = "Verified",
                                    tint = Info,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            vendor.city,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Rating badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Amber500.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Star, null, tint = Amber500, modifier = Modifier.size(14.dp))
                        Text(
                            "${String.format("%.1f", vendor.rating)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Amber500
                        )
                    }
                }
            }

            // Stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VendorStat(label = "Price/kW", value = "₹${(vendor.pricePerKwInr / 1000)}K")
                VendorStat(label = "Reviews", value = "${vendor.reviews}+")
                VendorStat(label = "Experience", value = "${vendor.yearsInBusiness} yrs")
            }

            HorizontalDivider()

            // Star rating bar
            RatingBar(rating = vendor.rating, reviews = vendor.reviews)

            // Contact button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${vendor.phone}"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Amber500)
            ) {
                Icon(Icons.Filled.Call, null, modifier = Modifier.size(18.dp), tint = Navy900)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call ${vendor.phone}", fontWeight = FontWeight.SemiBold, color = Navy900)
            }
        }
    }
}

@Composable
private fun VendorStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun RatingBar(rating: Float, reviews: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            val filled = index < rating.toInt()
            val half = !filled && index < rating
            Icon(
                imageVector = if (filled || half) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = if (filled || half) Amber500 else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
        Text(
            "(${vendor.reviews} reviews)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}
