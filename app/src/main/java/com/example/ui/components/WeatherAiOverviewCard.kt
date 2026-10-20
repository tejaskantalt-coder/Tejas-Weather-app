package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.WeatherAiOverview
import com.example.data.model.AppLanguage
import com.example.ui.AiChatUiState
import com.example.ui.AiOverviewUiState
import com.example.ui.theme.*

@Composable
fun WeatherAiOverviewCard(
    aiOverviewState: AiOverviewUiState,
    aiChatState: AiChatUiState,
    currentLanguage: AppLanguage,
    onRefreshOverview: () -> Unit,
    onAskQuestion: (String) -> Unit,
    onOpenLanguageSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userQuestion by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FrostedCardBg),
        border = BorderStroke(1.dp, FrostedCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_ai_overview_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Sparkle Icon + Title + Language Indicator + Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SunYellow.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI",
                            tint = SunYellow,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Gemini AI Weather Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Text(
                            text = "Atmospheric intelligence powered by Gemini 3.8",
                            fontSize = 11.sp,
                            color = TextWhiteTertiary
                        )
                    }
                }

                // Controls: Language Badge + Refresh button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Clickable language pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FrostedInnerBg,
                        border = BorderStroke(1.dp, FrostedCardBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenLanguageSheet() }
                            .testTag("ai_card_language_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = currentLanguage.flag, fontSize = 13.sp)
                            Text(
                                text = currentLanguage.code.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhitePrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefreshOverview,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(FrostedInnerBg)
                            .testTag("ai_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh AI Overview",
                            tint = TextWhitePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body depending on AI state
            when (aiOverviewState) {
                is AiOverviewUiState.Loading -> {
                    LoadingAiOverview(currentLanguage = currentLanguage)
                }

                is AiOverviewUiState.Success -> {
                    SuccessAiOverview(overview = aiOverviewState.overview)
                }

                is AiOverviewUiState.Error -> {
                    ErrorAiOverview(
                        message = aiOverviewState.message,
                        onRetry = onRefreshOverview
                    )
                }

                is AiOverviewUiState.Idle -> {
                    Text(
                        text = "Loading atmospheric insights in ${currentLanguage.displayName}...",
                        fontSize = 13.sp,
                        color = TextWhiteSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Q&A Section with Gemini
            HorizontalDivider(
                color = FrostedCardBorder,
                thickness = 0.8.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ask Gemini about today's weather",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhitePrimary
            )

            // Quick suggestion chips
            val quickQuestions = remember(currentLanguage) {
                getLocalizedQuickQuestions(currentLanguage)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickQuestions.forEach { question ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = FrostedInnerBg,
                        border = BorderStroke(1.dp, FrostedCardBorder.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                focusManager.clearFocus()
                                onAskQuestion(question)
                            }
                    ) {
                        Text(
                            text = question,
                            fontSize = 12.sp,
                            color = TextWhiteSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Question Input Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userQuestion,
                    onValueChange = { userQuestion = it },
                    placeholder = {
                        Text(
                            text = "Ask in ${currentLanguage.displayName}…",
                            fontSize = 13.sp,
                            color = TextWhiteTertiary
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhitePrimary,
                        unfocusedTextColor = TextWhitePrimary,
                        focusedBorderColor = SunYellow,
                        unfocusedBorderColor = FrostedCardBorder,
                        focusedContainerColor = FrostedInnerBg,
                        unfocusedContainerColor = FrostedInnerBg
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userQuestion.isNotBlank()) {
                                onAskQuestion(userQuestion)
                                userQuestion = ""
                                focusManager.clearFocus()
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_question_input")
                )

                IconButton(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            onAskQuestion(userQuestion)
                            userQuestion = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SunYellow)
                        .testTag("ai_send_question_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = AccuWeatherNavy
                    )
                }
            }

            // Streaming / Answer Result Display
            AnimatedVisibility(visible = aiChatState !is AiChatUiState.Idle) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(FrostedInnerBg)
                        .border(1.dp, FrostedCardBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    when (aiChatState) {
                        is AiChatUiState.Loading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = SunYellow
                                )
                                Text(
                                    text = "Gemini is typing in ${currentLanguage.displayName}…",
                                    fontSize = 12.sp,
                                    color = TextWhiteSecondary
                                )
                            }
                        }

                        is AiChatUiState.Streaming -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Q: ${aiChatState.prompt}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhiteTertiary
                                )
                                Text(
                                    text = aiChatState.answer,
                                    fontSize = 13.sp,
                                    color = TextWhitePrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        is AiChatUiState.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Q: ${aiChatState.prompt}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhiteTertiary
                                )
                                Text(
                                    text = aiChatState.answer,
                                    fontSize = 13.sp,
                                    color = TextWhitePrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        is AiChatUiState.Error -> {
                            Text(
                                text = "Error: ${aiChatState.message}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        is AiChatUiState.Idle -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingAiOverview(currentLanguage: AppLanguage) {
    val transition = rememberInfiniteTransition(label = "AiLoadingPulse")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = SunYellow
            )
            Text(
                text = "Gemini is analyzing atmosphere in ${currentLanguage.displayName} (${currentLanguage.nativeName})…",
                fontSize = 13.sp,
                color = TextWhiteSecondary.copy(alpha = alpha),
                fontWeight = FontWeight.Medium
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(FrostedInnerBg.copy(alpha = alpha))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(FrostedInnerBg.copy(alpha = alpha))
        )
    }
}

@Composable
private fun SuccessAiOverview(overview: WeatherAiOverview) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Headline & Highlight Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = overview.headline,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary,
                modifier = Modifier.weight(1f)
            )

            if (overview.alertBadge.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SunYellow.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, SunYellow.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = overview.alertBadge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SunYellow,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Narrative Summary
        Text(
            text = overview.narrative,
            fontSize = 14.sp,
            color = TextWhiteSecondary,
            lineHeight = 20.sp
        )

        // Bento Recommendation Pill Rows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clothing advice pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FrostedInnerBg,
                border = BorderStroke(1.dp, FrostedCardBorder.copy(alpha = 0.6f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = TempCurveYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Attire",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TempCurveYellow
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = overview.clothingAdvice,
                        fontSize = 12.sp,
                        color = TextWhitePrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Outdoor advice pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FrostedInnerBg,
                border = BorderStroke(1.dp, FrostedCardBorder.copy(alpha = 0.6f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = RainCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Outdoors",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RainCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = overview.outdoorAdvice,
                        fontSize = 12.sp,
                        color = TextWhitePrimary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorAiOverview(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "AI Overview: $message",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = FrostedInnerBg),
            border = BorderStroke(1.dp, FrostedCardBorder)
        ) {
            Text("Retry with Gemini", fontSize = 12.sp, color = TextWhitePrimary)
        }
    }
}

private fun getLocalizedQuickQuestions(language: AppLanguage): List<String> {
    return when (language) {
        AppLanguage.SPANISH -> listOf(
            "¿Qué ropa debo usar hoy?",
            "¿Lloverá más tarde?",
            "¿Es buen momento para correr?"
        )
        AppLanguage.FRENCH -> listOf(
            "Que dois-je porter aujourd'hui ?",
            "Va-t-il pleuvoir plus tard ?",
            "Est-ce idéal pour courir ?"
        )
        AppLanguage.GERMAN -> listOf(
            "Was sollte ich heute anziehen?",
            "Wird es später regnen?",
            "Ist es gut für Sport im Freien?"
        )
        AppLanguage.ITALIAN -> listOf(
            "Cosa dovrei indossare oggi?",
            "Pioverà più tardi?",
            "È un buon momento per correre?"
        )
        AppLanguage.HINDI -> listOf(
            "आज मुझे क्या पहनना चाहिए?",
            "क्या बाद में बारिश होगी?",
            "क्या बाहर दौड़ने का अच्छा समय है?"
        )
        AppLanguage.JAPANESE -> listOf(
            "今日の服装は何がいいですか？",
            "後で雨は降りますか？",
            "ランニングに適していますか？"
        )
        AppLanguage.KOREAN -> listOf(
            "오늘 어떤 옷을 입어야 하나요?",
            "나중에 비가 올까요?",
            "야외 운동하기 좋나요?"
        )
        AppLanguage.PORTUGUESE -> listOf(
            "O que devo vestir hoje?",
            "Vai chover mais tarde?",
            "É bom para correr ao ar livre?"
        )
        AppLanguage.POLISH -> listOf(
            "W co się dzisiaj ubrać?",
            "Czy będzie później padać?",
            "Czy warto iść pobiegać?"
        )
        AppLanguage.ENGLISH -> listOf(
            "What should I wear today?",
            "Will it rain later today?",
            "Best time for a workout?"
        )
    }
}
