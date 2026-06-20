package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.data.Shloka
import com.example.ui.GitaViewModel
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: GitaViewModel
) {
    val dailyShloka = viewModel.dailyShloka
    val currentLang = viewModel.preferredLanguage

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "नमस्कार , O Seeker",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Gita AI Guide",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }

                    // Mode Indicator Badge
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { navController.navigate("settings") }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (viewModel.personaMode == "Krishna Mode") Icons.Filled.SelfImprovement else Icons.Filled.Psychology,
                                contentDescription = "Persona Indicator",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = viewModel.personaMode,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Inspirational Quote Banner
            item {
                InspiringBanner()
            }

            // Daily Shloka System Card
            item {
                DailyShlokaCard(
                    shloka = dailyShloka,
                    language = currentLang,
                    viewModel = viewModel
                )
            }

            // Quick Menu Header
            item {
                Text(
                    text = "Spiritual Sanctuary Tools",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Quick Actions Navigation Grid Layout
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            title = "Gita AI Chat",
                            description = "Ask life questions & receive Gita answers",
                            icon = Icons.Filled.QuestionAnswer,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_nav_card"),
                            onClick = { navController.navigate("chat") }
                        )

                        FeatureCard(
                            title = "Smart Vision",
                            description = "Scan documents, notes or objects",
                            icon = Icons.Filled.PhotoCamera,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("camera_nav_card"),
                            onClick = { navController.navigate("camera") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            title = "Mood Solver",
                            description = "Emotional remedies & action steps",
                            icon = Icons.Filled.SentimentSatisfied,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mood_nav_card"),
                            onClick = { navController.navigate("mood_solver") }
                        )

                        FeatureCard(
                            title = "Shloka Library",
                            description = "Browse all 18 Chapters offline",
                            icon = Icons.Filled.Book,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("shloka_library_nav_card"),
                            onClick = { navController.navigate("shloka_library") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureCard(
                            title = "Reflection Journal",
                            description = "Write thoughts & get AI Mindset reports",
                            icon = Icons.Filled.EditNote,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("journal_nav_card"),
                            onClick = { navController.navigate("reflection_journal") }
                        )

                        FeatureCard(
                            title = "Gita Settings",
                            description = "Adjust voice, modes, and dialects",
                            icon = Icons.Filled.Settings,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_nav_card"),
                            onClick = { navController.navigate("settings") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InspiringBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                    )
                )
            )
            .shadow(4.dp)
            .padding(16.dp)
    ) {
        Column {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“Focus on your actions, Never on the outcomes.”",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 17.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bhagavad Gita Chapter 2, Verse 47",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun DailyShlokaCard(
    shloka: Shloka,
    language: String,
    viewModel: GitaViewModel
) {
    val isFavorite by viewModel.isShlokaFavorite(shloka.chapter, shloka.verse).collectAsState(initial = false)

    val textTranslation = when (language) {
        "Hindi" -> shloka.hindiTranslation
        "Marathi" -> shloka.marathiTranslation
        else -> shloka.englishTranslation
    }

    val textMeaning = when (language) {
        "Hindi" -> shloka.hindiMeaning
        "Marathi" -> shloka.marathiMeaning
        else -> shloka.englishMeaning
    }

    val textApplication = when (language) {
        "Hindi" -> shloka.hindiApplication
        "Marathi" -> shloka.marathiApplication
        else -> shloka.englishApplication
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .shadow(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY CONTEMPLATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chapter ${shloka.chapter}, Verse ${shloka.verse}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Speak aloud icon
                    IconButton(
                        onClick = {
                            val audioText = "Shloka of today. Sanskrit: ${shloka.text}. Translation: $textTranslation"
                            viewModel.speakText(audioText)
                        },
                        modifier = Modifier.testTag("play_shloka_tts")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Speak Shloka",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Bookmark icon
                    IconButton(
                        onClick = { viewModel.toggleShlokaFavorite(shloka.chapter, shloka.verse) },
                        modifier = Modifier.testTag("favorite_toggle_shloka")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Bookmark Shloka",
                            tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sanskrit Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = shloka.text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 26.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = shloka.transliteration,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Translation Title
            Text(
                text = "Translation",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textTranslation,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Meaning Title
            Text(
                text = "Spiritual Meaning",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = textMeaning,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Life Application Title
            Text(
                text = "Real World Application",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFFAED))
                    .padding(8.dp)
            ) {
                Text(
                    text = textApplication,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .shadow(2.dp)
            .clickable(onClick = onClick)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 15.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    lineHeight = 16.sp
                ),
                maxLines = 2
            )
        }
    }
}
