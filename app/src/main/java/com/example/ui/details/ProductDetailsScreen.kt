package com.example.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate50
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.UtilityBg
import com.example.ui.theme.UtilityBlue100
import com.example.ui.theme.UtilityBlue700
import com.example.ui.theme.UtilityGreen
import com.example.ui.theme.UtilityNavy
import com.example.util.PriceFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductDetailsScreen(
    product: Product,
    onScanNext: () -> Unit,
    onRefreshMaster: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val lastUpdateStr = timeFormat.format(Date())

    // Parse SAR values dynamically from product.sarValue without any hardcoded defaults or fallbacks
    val sarRaw = product.sarValue.trim()
    val (sarBody, sarHead) = remember(sarRaw) {
        if (sarRaw.isEmpty()) {
            "Not Available" to "Not Available"
        } else {
            val bodyRegex = Regex("""(?:Body\s*[:\-–]\s*|Body\s+)([^,;]+)""", RegexOption.IGNORE_CASE)
            val headRegex = Regex("""(?:Head\s*[:\-–]\s*|Head\s+)([^,;]+)""", RegexOption.IGNORE_CASE)

            val bodyMatch = bodyRegex.find(sarRaw)?.groupValues?.get(1)?.trim()
            val headMatch = headRegex.find(sarRaw)?.groupValues?.get(1)?.trim()

            if (bodyMatch != null || headMatch != null) {
                val body = bodyMatch ?: "Not Available"
                val head = headMatch ?: "Not Available"
                body to head
            } else {
                // If it is a generic SAR string without body/head distinction
                sarRaw to sarRaw
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UtilityBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Status Bar Banner: Product Found
            Surface(
                color = UtilityGreen,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PRODUCT FOUND",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }

            // Main Scrollable Content Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .testTag("product_details_card")
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Model Header Section
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                Text(
                                    text = "MODEL",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Slate400
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = product.model.ifEmpty { "N/A" },
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                                    fontWeight = FontWeight.Black,
                                    color = UtilityNavy,
                                    lineHeight = 32.sp,
                                    modifier = Modifier.testTag("product_model_text")
                                )
                            }

                            HorizontalDivider(color = Slate100, thickness = 1.dp)

                            // Main Details Body
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Row 1: Color & Memory
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Color Field
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "COLOR",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Slate400
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = product.color.ifEmpty { "N/A" },
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800,
                                            modifier = Modifier.testTag("product_color_text")
                                        )
                                    }

                                    // Memory Field
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "MEMORY",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Slate400
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = product.memory.ifEmpty { "N/A" },
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800,
                                            modifier = Modifier.testTag("product_memory_text")
                                        )
                                    }
                                }

                                // Row 2: EAN Barcode + Valid badge
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "EAN BARCODE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Slate400
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = product.ean.ifEmpty { "N/A" },
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = (-0.5).sp,
                                            color = Slate800,
                                            modifier = Modifier.testTag("product_ean_text")
                                        )
                                        Surface(
                                            color = UtilityBlue100,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "VALID",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = UtilityBlue700,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Row 3: SKU Code
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = "SKU CODE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Slate400
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = product.sku.ifEmpty { "N/A" },
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate800,
                                        modifier = Modifier.testTag("product_sku_text")
                                    )
                                }

                                // Row 4: Price (MRP)
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "PRICE (MRP)",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Slate400
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = PriceFormatter.format(product.price),
                                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp),
                                        fontWeight = FontWeight.Black,
                                        color = UtilityNavy,
                                        modifier = Modifier.testTag("product_price_text")
                                    )
                                }

                                // SAR Values Info Box
                                Surface(
                                    color = Slate50,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "SAR VALUE INFORMATION",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Slate400
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("product_sar_value_container"),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "BODY",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Slate500
                                                )
                                                Text(
                                                    text = sarBody,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate700,
                                                    modifier = Modifier.testTag("sar_body_text")
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "HEAD",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Slate500
                                                )
                                                Text(
                                                    text = sarHead,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate700,
                                                    modifier = Modifier.testTag("sar_head_text")
                                                )
                                            }
                                        }
                                        // Also provide the full raw SAR value bound directly to product.sarValue with testTag
                                        Text(
                                            text = product.sarValue.ifBlank { "Not Available" },
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = Slate400,
                                            modifier = Modifier
                                                .padding(top = 6.dp)
                                                .testTag("product_sar_value_text")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Footer Bottom Bar
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onScanNext,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = UtilityNavy,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .height(56.dp)
                            .testTag("scan_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SCAN NEXT PRODUCT",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 600.dp)
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last update: $lastUpdateStr",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Medium,
                            color = Slate400
                        )

                        Text(
                            text = "REFRESH MASTER",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = UtilityNavy,
                            modifier = Modifier
                                .clickable { onRefreshMaster() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
