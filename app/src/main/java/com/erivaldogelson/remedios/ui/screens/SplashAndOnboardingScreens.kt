package com.erivaldogelson.remedios.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.erivaldogelson.remedios.ui.components.AnimatedPrimaryActionButton
import com.erivaldogelson.remedios.ui.components.PremiumScaffoldBackground
import com.erivaldogelson.remedios.ui.theme.Mist
import com.erivaldogelson.remedios.ui.theme.RemediosTheme
import com.erivaldogelson.remedios.ui.theme.SoftLilac
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val description: String,
    val emoji: String,
)

@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(1200)
        onFinished()
    }

    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 8 }),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("💊", style = MaterialTheme.typography.displayLarge)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text("Remédios", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Cuidado calmo, bonito e preciso para cada dose.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pages = listOf(
        OnboardingPage(
            title = "Registre seus remédios com clareza",
            description = "Adicione manualmente, organize horários e use cores para identificar cada tratamento com calma.",
            emoji = "💊",
        ),
        OnboardingPage(
            title = "Escaneie pela câmera",
            description = "Use OCR para sugerir nome, dose e laboratório a partir da embalagem sem digitação cansativa.",
            emoji = "📷",
        ),
        OnboardingPage(
            title = "Lembretes vivos e histórico elegante",
            description = "Acompanhe próximas doses, ações rápidas e uma base pronta para Live Updates e Now Bar no Android 16.",
            emoji = "✨",
        ),
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val currentPage = pagerState.currentPage
    val scope = rememberCoroutineScope()

    PremiumScaffoldBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Bem-vindo ao Remédios",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { page ->
                val item = pages[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SoftLilac.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(item.emoji, style = MaterialTheme.typography.displayLarge)
                    }
                    Spacer(Modifier.height(28.dp))
                    Text(item.title, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.76f),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "${currentPage + 1} / ${pages.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.58f),
                        fontWeight = FontWeight.Medium,
                    )
                }
                AnimatedPrimaryActionButton(
                    text = if (currentPage == pages.lastIndex) "Começar agora" else "Continuar",
                    onClick = {
                        if (currentPage == pages.lastIndex) {
                            onContinue()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun SplashPreview() {
    RemediosTheme {
        SplashScreen(onFinished = {})
    }
}

@Preview
@Composable
private fun OnboardingPreview() {
    RemediosTheme {
        OnboardingScreen(onContinue = {})
    }
}
