package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.BusinessContextBuilder
import com.example.data.entity.ChatMessageEntity
import com.example.ui.components.SwissCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatAssistantScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeBusiness by viewModel.activeBusiness.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val aggregates by viewModel.aggregates.collectAsState()
    val products by viewModel.products.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showClearDialog by remember { mutableStateOf(false) }

    // Scroll to bottom on new message
    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SwissBg)
    ) {
        // Business Context Header Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SwissSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SwissSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Data Bisnis Terhubung Real-Time",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwissPrimaryText
                        )
                        Text(
                            text = "${products.size} Produk • ${aggregates.totalTransactions} Transaksi • ${BusinessContextBuilder.formatRupiah(aggregates.totalRevenue)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SwissSecondaryText
                        )
                    }
                }

                IconButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Hapus Riwayat Chat",
                        tint = SwissSecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Quick Suggestion Chips
        val suggestionChips = listOf(
            "Produk mana yang paling laku?",
            "Bagaimana perbandingan penjualan?",
            "Buatkan promosi untuk produk dengan penjualan rendah",
            "Apa yang perlu saya evaluasi dari stok saat ini?",
            "Ringkas performa bisnis saya"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (chip in suggestionChips) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SwissSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder),
                    modifier = Modifier.clickable {
                        viewModel.sendChatMessage(chip)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SwissAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = chip,
                            style = MaterialTheme.typography.labelSmall,
                            color = SwissPrimaryText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    EmptyChatCard(
                        businessName = activeBusiness?.name ?: "UMKM Anda",
                        hasTransactions = aggregates.totalTransactions > 0,
                        onPromptClick = { prompt ->
                            viewModel.sendChatMessage(prompt)
                        }
                    )
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                if (message.sender == "user") {
                    UserMessageBubble(message = message)
                } else {
                    AssistantMessageBubble(
                        message = message,
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(message.message))
                            Toast.makeText(context, "Teks berhasil disalin ke clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (isAiThinking) {
                item {
                    AiThinkingCard()
                }
            }
        }

        // Input Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            color = SwissSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = {
                        Text(
                            text = "Tanyakan analisis penjualan, stok, promosi...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SwissSecondaryText,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SwissAccent,
                        unfocusedBorderColor = SwissBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4,
                    enabled = !isAiThinking
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputPrompt.isNotBlank() && !isAiThinking) SwissAccent else SwissBorder)
                        .clickable(enabled = inputPrompt.isNotBlank() && !isAiThinking) {
                            val promptToSend = inputPrompt
                            inputPrompt = ""
                            viewModel.sendChatMessage(promptToSend)
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Kirim Pertanyaan",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Hapus Riwayat Chat?", fontWeight = FontWeight.Bold) },
            text = { Text("Seluruh riwayat percakapan dengan AI akan dibersihkan. Data penjualan dan produk Anda tetap aman.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChat()
                        showClearDialog = false
                    }
                ) {
                    Text("Hapus", color = SwissAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = SwissSecondaryText)
                }
            }
        )
    }
}

@Composable
fun UserMessageBubble(message: ChatMessageEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                .background(SwissPrimaryText)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.message,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AssistantMessageBubble(
    message: ChatMessageEntity,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SwissBorder, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SwissSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header with AI Tag and Copy Button
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
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "UsahaPilot AI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SwissPrimaryText
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Data Terverifikasi",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = SwissSuccess,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SwissSuccess.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Salin Jawaban",
                            tint = SwissSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Parse structured sections if present
                val formattedText = message.message
                val sections = splitIntoSections(formattedText)

                if (sections.isNotEmpty()) {
                    for ((sectionTitle, sectionContent) in sections) {
                        EditorialSection(title = sectionTitle, content = sectionContent)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text = formattedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SwissPrimaryText,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}

private fun splitIntoSections(text: String): List<Pair<String, String>> {
    val keys = listOf("[FAKTA DATA]", "[ESTIMASI & ANALISIS]", "[ESTIMASI/ANALISIS]", "[REKOMENDASI AKSI]", "[REKOMENDASI AKSI & DRAF PROMOSI]")
    val hasKey = keys.any { text.contains(it) }
    if (!hasKey) return emptyList()

    val result = mutableListOf<Pair<String, String>>()
    val lines = text.lines()
    var currentHeader = ""
    val currentBody = StringBuilder()

    for (line in lines) {
        val trimmed = line.trim()
        val matchingKey = keys.firstOrNull { trimmed.startsWith(it) }
        if (matchingKey != null) {
            if (currentHeader.isNotEmpty() || currentBody.isNotEmpty()) {
                result.add(Pair(currentHeader, currentBody.toString().trim()))
                currentBody.clear()
            }
            currentHeader = matchingKey.removeSurrounding("[", "]")
        } else {
            currentBody.append(line).append("\n")
        }
    }

    if (currentHeader.isNotEmpty() || currentBody.isNotEmpty()) {
        result.add(Pair(currentHeader, currentBody.toString().trim()))
    }

    return result
}

@Composable
fun EditorialSection(title: String, content: String) {
    val (badgeBg, badgeColor) = when {
        title.contains("FAKTA", ignoreCase = true) -> Pair(SwissPrimaryText, Color.White)
        title.contains("ESTIMASI", ignoreCase = true) || title.contains("ANALISIS", ignoreCase = true) -> Pair(SwissSecondaryText, Color.White)
        else -> Pair(SwissAccent, Color.White) // Rekomendasi
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (title.isNotBlank()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, SwissBorder.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = SwissPrimaryText,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun AiThinkingCard() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SwissSurfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = SwissAccent
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "UsahaPilot AI sedang menganalisis data bisnis Anda...",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EmptyChatCard(
    businessName: String,
    hasTransactions: Boolean,
    onPromptClick: (String) -> Unit
) {
    SwissCard(backgroundColor = SwissSurface) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(SwissAccentMuted),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = SwissAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Asisten Bisnis $businessName",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = SwissPrimaryText
                )
                Text(
                    text = if (hasTransactions) "Tanyakan data penjualan, stok, atau minta draf promosi" else "Belum ada transaksi. Tanyakan panduan memulai atau gunakan demo data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SwissSecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Contoh pertanyaan siap pakai:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SwissSecondaryText
        )
        Spacer(modifier = Modifier.height(8.dp))

        val starterQuestions = if (hasTransactions) {
            listOf(
                "Produk mana yang paling laku bulan ini?",
                "Bagaimana perbandingan penjualan minggu ini dibanding minggu lalu?",
                "Buatkan draf promosi untuk produk dengan penjualan rendah",
                "Apa yang perlu saya evaluasi dari stok saat ini?"
            )
        } else {
            listOf(
                "Bagaimana cara mulai mencatat transaksi di aplikasi ini?",
                "Bagaimana cara memuat data demo untuk melihat contoh analitik?",
                "Apa tips dasar menyusun harga jual produk UMKM?"
            )
        }

        for (q in starterQuestions) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onPromptClick(q) },
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, SwissBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = q,
                        style = MaterialTheme.typography.bodySmall,
                        color = SwissPrimaryText,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = SwissAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
