package com.solarsensear.ui.screens.vendors

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.solarsensear.data.mock.MockData
import com.solarsensear.data.models.Vendor
import com.solarsensear.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorsScreen(onBack: () -> Unit) {
    val vendors = MockData.sampleVendors

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Solar Installers", fontWeight = FontWeight.Bold)
                        Text(
                            "${vendors.size} verified installers near you",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Info.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.VerifiedUser, null, tint = Info)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("All vendors are verified and MNRE-certified")
                    }
                }
            }

            items(vendors) { vendor ->
                VendorDetailCard(vendor)
            }
        }
    }
}

//////////////////////////////////////////////////////
// ✅ CARD
//////////////////////////////////////////////////////

@Composable
private fun VendorDetailCard(vendor: Vendor) {
    val context = LocalContext.current

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

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
                            vendor.name.first().toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(vendor.name, fontWeight = FontWeight.Bold)

                            if (vendor.verified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Filled.Verified, null, tint = Info, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(vendor.city, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Text(
                    String.format(Locale.US, "%.1f ⭐", vendor.rating),
                    color = Amber500,
                    fontWeight = FontWeight.Bold
                )
            }

            // Stats
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                VendorStat("Price/kW", "₹${vendor.pricePerKwInr / 1000}K")
                VendorStat("Reviews", "${vendor.reviews}+")
                VendorStat("Exp", "${vendor.yearsInBusiness} yrs")
            }

            Divider()

            // ⭐ FIXED HERE (PASS reviews)
            RatingBar(rating = vendor.rating, reviews = vendor.reviews)

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, "tel:${vendor.phone}".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Amber500)
            ) {
                Icon(Icons.Filled.Call, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Call ${vendor.phone}")
            }
        }
    }
}

//////////////////////////////////////////////////////
// ✅ HELPERS
//////////////////////////////////////////////////////

@Composable
private fun VendorStat(label: String, value: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

//////////////////////////////////////////////////////
// ✅ FIXED FUNCTION (NO vendor ERROR)
//////////////////////////////////////////////////////

@Composable
private fun RatingBar(rating: Float, reviews: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating.toInt()) Icons.Filled.Star else Icons.Filled.StarOutline,
                contentDescription = null,
                tint = if (index < rating) Amber500 else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            "($reviews reviews)",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}