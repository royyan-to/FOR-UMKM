package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.BusinessContextBuilder
import com.example.data.entity.ProductEntity
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketingGeneratorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aiGenerations by viewModel.aiGenerations.collectAsState()

    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var targetAudience by remember { mutableStateOf("Anak muda & pekerja kantor yang mencari kepraktisan") }
    var usp by remember { mutableStateOf("Bahan segar berkualitas, higienis, dan rasa otentik") }
    var selectedPlatform by remember { mutableStateOf("Instagram") }

    var latestGeneratedResult by remember { mutableStateOf<String?>(null) }
    var productDropdownExpanded by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 0.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "AI Marketing Generator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SwissPrimaryText
                )
                Text(
                    text = "Buat caption Instagram, deskripsi toko, promo WhatsApp, dan jadwal konten 7 hari secara instan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )
            }
        }

        // Form Card
        item {
            SwissCard {
                Text(
                    text = "Data Produk & Kampanye",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Select from existing product
                if (products.isNotEmpty()) {
                    Text(text = "Pilih dari produk yang sudah ada:", fontSize = 11.sp, color = SwissSecondaryText)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = productDropdownExpanded,
                        onExpandedChange = { productDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProduct?.name ?: "Pilih Produk dari Katalog...",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = productDropdownExpanded,
                            onDismissRequest = { productDropdownExpanded = false }
                        ) {
                            for (p in products) {
                                DropdownMenuItem(
                                    text = { Text("${p.name} - ${BusinessContextBuilder.formatRupiah(p.price)}") },
                                    onClick = {
                                        selectedProduct = p
                                        productName = p.name
                                        category = p.category
                                        priceText = p.price.toString()
                                        if (p.description.isNotBlank()) usp = p.description
                                        productDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nama Produk *") },
                    modifier = Modifier.fillMaxWidth().testTag("marketing_product_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Kategori") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetAudience,
                    onValueChange = { targetAudience = it },
                    label = { Text("Target Pelanggan") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = usp,
                    onValueChange = { usp = it },
                    label = { Text("Keunggulan Produk (USP)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Platform Utama:", style = MaterialTheme.typography.labelSmall, color = SwissSecondaryText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Instagram", "Marketplace", "WhatsApp").forEach { plat ->
                        val isSel = selectedPlatform == plat
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SwissPrimaryText else SwissSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPlatform = plat }
                        ) {
                            Text(
                                text = plat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SwissPrimaryText,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val price = priceText.toLongOrNull() ?: 25000L
                        viewModel.generateMarketing(
                            productName = productName.ifBlank { "Produk Unggulan" },
                            category = category.ifBlank { "Umum" },
                            price = price,
                            targetAudience = targetAudience,
                            usp = usp,
                            platform = selectedPlatform
                        ) { result ->
                            latestGeneratedResult = result
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SwissAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_marketing_button"),
                    enabled = !isAiThinking
                ) {
                    if (isAiThinking) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Sedang Menulis Konten...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Materi Konten", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Generated Results View
        latestGeneratedResult?.let { content ->
            item {
                SwissCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SwissAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hasil Generate AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SwissPrimaryText
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(content))
                                Toast.makeText(context, "Materi marketing berhasil disalin!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Salin Semua", tint = SwissPrimaryText)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
                    ) {
                        Text(
                            text = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwissPrimaryText,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        // History of Saved Generations
        if (aiGenerations.isNotEmpty()) {
            item {
                Text(
                    text = "Riwayat Konten Tersimpan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
            }

            items(aiGenerations, key = { it.id }) { gen ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = gen.prompt,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SwissPrimaryText
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(gen.resultContent))
                                    Toast.makeText(context, "Konten disalin!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Salin", tint = SwissSecondaryText, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = gen.resultContent.take(150) + "...",
                            style = MaterialTheme.typography.bodySmall,
                            color = SwissSecondaryText
                        )
                    }
                }
            }
        }
    }
}
