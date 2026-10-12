
package com.mca.gamesmca

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
//import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.IconButton
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mca.simon.SimonActivity
import com.mca.memorizzle.MemorizzleActivity
import com.mca.puzzleapp.PuzzleActivity
import com.mca.gamesmca.tresenraya.Tresenraya

import kotlinx.coroutines.delay

// ============================================================
// COLORES PROPIOS DE LOS ICONOS DE LOS JUEGOS
// ============================================================

private val XColor = Color(0xFF22D3EE)
private val XColorBright = Color(0xFF67E8F9)

private val OColor = Color(0xFFF472B6)
private val OColorBright = Color(0xFFF9A8D4)

// ============================================================
// ACTIVIDAD PRINCIPAL
// ============================================================

class MainActivity : ComponentActivity() {

    private var menuResetCounter by mutableIntStateOf(0)

    private var musicEnabled by mutableStateOf(false)

    private var musicPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val darkTheme = androidx.compose.foundation.isSystemInDarkTheme()
            val view = LocalView.current

            if (!view.isInEditMode) {
                SideEffect {
                    val controller = WindowInsetsControllerCompat(
                        window,
                        view
                    )

                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }
            }

            GamesMcaTheme(darkTheme = darkTheme) {
                GameMenuScreen(
                    darkTheme = darkTheme,
                    resetKey = menuResetCounter,
                    musicEnabled = musicEnabled,
                    onToggleMusic = ::toggleMusic,
                    onOpenGame = ::openGame
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        menuResetCounter++
    }

    /**
     * Reproduce, pausa o reanuda la música del menú.
     *
     * Archivo necesario:
     * app/src/main/res/raw/game_music.mp3
     */
    private fun toggleMusic() {
        try {
            if (musicEnabled) {
                musicPlayer?.pause()
                musicEnabled = false
                return
            }

            if (musicPlayer == null) {
                musicPlayer = MediaPlayer.create(
                    this,
                    R.raw.game_music
                )

                musicPlayer?.isLooping = true
            }

            val player = musicPlayer

            if (player != null) {
                if (!player.isPlaying) {
                    player.start()
                }

                musicEnabled = player.isPlaying
            } else {
                musicEnabled = false
            }
        } catch (_: IllegalStateException) {
            musicEnabled = false
        } catch (_: RuntimeException) {
            musicEnabled = false
        }
    }

    override fun onDestroy() {
        try {
            musicPlayer?.release()
        } catch (_: RuntimeException) {
            // El reproductor puede haber sido liberado previamente.
        }

        musicPlayer = null
        musicEnabled = false

        super.onDestroy()
    }

    private fun openGame(game: GameType) {
        val intent = when (game) {
            GameType.TIC_TAC_TOE ->
                Intent(this, Tresenraya::class.java)

            GameType.MEMORY ->
                Intent(this, MemorizzleActivity::class.java)

            GameType.PUZZLE ->
                Intent(this, PuzzleActivity::class.java)

            GameType.SIMON ->
                Intent(
                    this,
                    SimonActivity::class.java
                )
        }

        startActivity(intent)
    }
}

// ============================================================
// TIPOS DE JUEGO
// ============================================================

private enum class GameType {
    TIC_TAC_TOE,
    MEMORY,
    PUZZLE,
    SIMON
}

private fun GameType.displayName(): String {
    return when (this) {
        GameType.TIC_TAC_TOE -> "TRES EN RAYA"
        GameType.MEMORY -> "MEMORIZZLE"
        GameType.PUZZLE -> "PUZZLE 2048"
        GameType.SIMON -> "SIMON"
    }
}

// ============================================================
// MENÚ PRINCIPAL
// ============================================================

@Composable
private fun GameMenuScreen(
    darkTheme: Boolean,
    resetKey: Int,
    musicEnabled: Boolean = false,
    onToggleMusic: () -> Unit = {},
    onOpenGame: (GameType) -> Unit
) {
    val titleColor = if (darkTheme) {
        GamesColors.ElectricBlueBright
    } else {
        GamesColors.LimeDark
    }

    val primaryText = if (darkTheme) {
        GamesColors.DarkText
    } else {
        GamesColors.LightText
    }

    val secondaryText = if (darkTheme) {
        GamesColors.DarkSecondaryText
    } else {
        GamesColors.LightSecondaryText
    }

    GamesBackground(darkTheme = darkTheme) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 20.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ------------------------------------------------
            // CABECERA: NOMBRE Y MÚSICA EN LA MISMA FILA
            // ------------------------------------------------

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(top = 30.dp ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GAMES MCA",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = titleColor,
                        maxLines = 1
                    )

                    Text(
                        text = "TU CENTRO DE JUEGOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp,
                        color = secondaryText
                    )
                }

                // Botón circular compacto, alineado con el título.
                IconButton(
                    onClick = onToggleMusic,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(38.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (darkTheme) {
                                    GamesGradients.DarkButton
                                } else {
                                    GamesGradients.LightButton
                                }
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = if (darkTheme) {
                                    GamesGradients.DarkBorder
                                } else {
                                    GamesGradients.LightBorder
                                }
                            ),
                            shape = CircleShape
                        )
                ) {
                    MusicControlIcon(
                        enabled = musicEnabled,
                        darkTheme = darkTheme
                    )
                }
            }

            Text(
                text = if (musicEnabled) {
                    "MÚSICA ACTIVADA"
                } else {
                    "MÚSICA DESACTIVADA"
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp),
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                textAlign = TextAlign.End,
                color = secondaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ------------------------------------------------
            // CUADRÍCULA DE DOS COLUMNAS Y DOS FILAS
            // ------------------------------------------------

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameFab(
                        game = GameType.TIC_TAC_TOE,
                        index = 0,
                        resetKey = resetKey,
                        darkTheme = darkTheme,
                        onClick = { onOpenGame(GameType.TIC_TAC_TOE) }
                    )

                    GameFab(
                        game = GameType.MEMORY,
                        index = 1,
                        resetKey = resetKey,
                        darkTheme = darkTheme,
                        onClick = { onOpenGame(GameType.MEMORY) }
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GameFab(
                        game = GameType.PUZZLE,
                        index = 2,
                        resetKey = resetKey,
                        darkTheme = darkTheme,
                        onClick = { onOpenGame(GameType.PUZZLE) }
                    )

                    GameFab(
                        game = GameType.SIMON,
                        index = 3,
                        resetKey = resetKey,
                        darkTheme = darkTheme,
                        onClick = { onOpenGame(GameType.SIMON) }
                    )
                }
            }

            // ------------------------------------------------
            // TEXTO INFERIOR SIN FONDO
            // ------------------------------------------------

            Text(
                text = "ELIGE TU JUEGO",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = secondaryText,
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 6.dp
                )
            )
        }
    }
}

// ============================================================
// CONTROL MUSICAL DIBUJADO CON CANVAS
// ============================================================

@Composable
private fun MusicControlIcon(
    enabled: Boolean,
    darkTheme: Boolean
) {
    val iconColor = if (darkTheme) {
        GamesColors.DarkText
    } else {
        GamesColors.LightText
    }

    Canvas(modifier = Modifier.size(23.dp)) {
        val w = size.width
        val h = size.height

        val notePath = Path().apply {
            moveTo(w * 0.62f, h * 0.18f)
            lineTo(w * 0.62f, h * 0.64f)

            cubicTo(
                w * 0.62f, h * 0.78f,
                w * 0.42f, h * 0.84f,
                w * 0.34f, h * 0.73f
            )

            cubicTo(
                w * 0.28f, h * 0.62f,
                w * 0.43f, h * 0.56f,
                w * 0.55f, h * 0.59f
            )

            moveTo(w * 0.62f, h * 0.18f)
            lineTo(w * 0.84f, h * 0.12f)
            lineTo(w * 0.84f, h * 0.26f)
            lineTo(w * 0.62f, h * 0.32f)
        }

        drawPath(
            path = notePath,
            color = iconColor,
            style = Stroke(
                width = 2.3f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        if (!enabled) {
            drawLine(
                color = iconColor,
                start = Offset(w * 0.16f, h * 0.18f),
                end = Offset(w * 0.84f, h * 0.86f),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
    }
}

// ============================================================
// BOTÓN ANIMADO DE JUEGO CON NOMBRE TRANSPARENTE
// ============================================================

@Composable
private fun GameFab(
    game: GameType,
    index: Int,
    resetKey: Int,
    darkTheme: Boolean,
    onClick: () -> Unit
) {
    var clicked by remember(resetKey) {
        mutableStateOf(false)
    }

    val transition = rememberInfiniteTransition(
        label = "fabTransition$index"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100 + index * 120,
                delayMillis = index * 100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse$index"
    )

    val glow by transition.animateFloat(
        initialValue = 0.16f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1250 + index * 130,
                delayMillis = index * 120,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow$index"
    )

    val pressedScale by animateFloatAsState(
        targetValue = if (clicked) 0.08f else 1f,
        animationSpec = tween(
            durationMillis = 280,
            easing = FastOutSlowInEasing
        ),
        label = "pressedScale$index"
    )

    val pressedAlpha by animateFloatAsState(
        targetValue = if (clicked) 0f else 1f,
        animationSpec = tween(durationMillis = 280),
        label = "pressedAlpha$index"
    )

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(290)
            onClick()
        }
    }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val isPressed by interactionSource.collectIsPressedAsState()

    val touchScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "touchScale$index"
    )

    val borderBrush = Brush.linearGradient(
        colors = if (darkTheme) {
            GamesGradients.DarkBorder
        } else {
            GamesGradients.LightBorder
        }
    )

    val labelColor = if (darkTheme) {
        GamesColors.DarkText
    } else {
        GamesColors.LightText
    }

    Column(
        modifier = Modifier
            .size(width = 140.dp, height = 126.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // El área animada se limita al botón.
        Box(
            modifier = Modifier
                .size(78.dp)
                .scale(if (clicked) pressedScale else pulse)
                .alpha(pressedAlpha),
            contentAlignment = Alignment.Center
        ) {
            // Halo exterior.
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .alpha(glow)
                    .background(
                        brush = fabGlowGradient(game),
                        shape = CircleShape
                    )
            )

            // Botón circular compacto.
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .background(
                        brush = fabGradient(game, darkTheme),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        brush = borderBrush,
                        shape = CircleShape
                    )
                    .scale(touchScale)
                    .clickable(
                        enabled = !clicked,
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        clicked = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                GameIcon(
                    game = game,
                    darkTheme = darkTheme
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Texto transparente: sin tarjeta ni fondo añadido.
        Text(
            text = game.displayName(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

// ============================================================
// SELECTOR DE ICONOS
// ============================================================

@Composable
private fun GameIcon(
    game: GameType,
    darkTheme: Boolean
) {
    when (game) {
        GameType.TIC_TAC_TOE -> TicTacIcon(darkTheme)
        GameType.MEMORY -> MemoryIcon(darkTheme)
        GameType.PUZZLE -> PuzzleIcon(darkTheme)
        GameType.SIMON -> SimonIcon(darkTheme)
    }
}

// ============================================================
// ICONO TRES EN RAYA
// ============================================================

@Composable
private fun TicTacIcon(
    darkTheme: Boolean
) {
    val color = if (darkTheme) {
        XColorBright
    } else {
        GamesColors.LimeDark
    }

    Canvas(modifier = Modifier.size(40.dp)) {
        val w = size.width
        val h = size.height
        val stroke = 3f

        drawLine(
            color,
            Offset(w * 0.35f, h * 0.08f),
            Offset(w * 0.35f, h * 0.92f),
            stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color,
            Offset(w * 0.65f, h * 0.08f),
            Offset(w * 0.65f, h * 0.92f),
            stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color,
            Offset(w * 0.08f, h * 0.35f),
            Offset(w * 0.92f, h * 0.35f),
            stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color,
            Offset(w * 0.08f, h * 0.65f),
            Offset(w * 0.92f, h * 0.65f),
            stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            XColorBright,
            Offset(w * 0.41f, h * 0.41f),
            Offset(w * 0.59f, h * 0.59f),
            3.3f,
            cap = StrokeCap.Round
        )

        drawLine(
            XColorBright,
            Offset(w * 0.59f, h * 0.41f),
            Offset(w * 0.41f, h * 0.59f),
            3.3f,
            cap = StrokeCap.Round
        )
    }
}

// ============================================================
// ICONO MEMORIZZLE
// ============================================================

@Composable
private fun MemoryIcon(
    darkTheme: Boolean
) {
    val main = if (darkTheme) {
        OColor
    } else {
        GamesColors.LimeDark
    }

    val bright = if (darkTheme) {
        OColorBright
    } else {
        GamesColors.Lime
    }

    Canvas(modifier = Modifier.size(40.dp)) {
        val w = size.width
        val h = size.height

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    main,
                    if (darkTheme) {
                        Color(0xFFDB2777)
                    } else {
                        GamesColors.LimeDark
                    }
                )
            ),
            topLeft = Offset(w * 0.30f, h * 0.08f),
            size = Size(w * 0.48f, h * 0.72f),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(bright, main)
            ),
            topLeft = Offset(w * 0.18f, h * 0.22f),
            size = Size(w * 0.48f, h * 0.70f),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.07f,
            center = Offset(w * 0.42f, h * 0.57f)
        )
    }
}

// ============================================================
// ICONO PUZZLE
// ============================================================

@Composable
private fun PuzzleIcon(
    darkTheme: Boolean
) {
    val colors = if (darkTheme) {
        listOf(
            Color(0xFFC4B5FD),
            Color(0xFF8B5CF6),
            Color(0xFF5B21B6)
        )
    } else {
        listOf(
            GamesColors.LimeBright,
            GamesColors.Lime,
            GamesColors.LimeDark
        )
    }

    Canvas(modifier = Modifier.size(41.dp)) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.22f)
            lineTo(w * 0.43f, h * 0.22f)

            cubicTo(
                w * 0.42f, h * 0.05f,
                w * 0.68f, h * 0.05f,
                w * 0.57f, h * 0.22f
            )

            lineTo(w * 0.78f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.43f)

            cubicTo(
                w * 0.95f, h * 0.42f,
                w * 0.95f, h * 0.68f,
                w * 0.78f, h * 0.57f
            )

            lineTo(w * 0.78f, h * 0.78f)
            lineTo(w * 0.57f, h * 0.78f)

            cubicTo(
                w * 0.58f, h * 0.95f,
                w * 0.32f, h * 0.95f,
                w * 0.43f, h * 0.78f
            )

            lineTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.22f, h * 0.57f)

            cubicTo(
                w * 0.05f, h * 0.58f,
                w * 0.05f, h * 0.32f,
                w * 0.22f, h * 0.43f
            )

            close()
        }

        drawPath(
            path = path,
            brush = Brush.linearGradient(colors)
        )

        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(
                width = 1.5f,
                join = StrokeJoin.Round
            )
        )
    }
}

// ============================================================
// ICONO SIMON
// ============================================================

@Composable
private fun SimonIcon(
    darkTheme: Boolean
) {
    Canvas(modifier = Modifier.size(41.dp)) {
        val w = size.width
        val h = size.height

        val gap = 3.5f
        val left = 4.5f
        val top = 4.5f
        val halfW = (w - 9f - gap) / 2f
        val halfH = (h - 9f - gap) / 2f

        drawRoundRect(
            color = Color(0xFFFF1744),
            topLeft = Offset(left, top),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawRoundRect(
            color = Color(0xFFFFEA00),
            topLeft = Offset(left + halfW + gap, top),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawRoundRect(
            color = Color(0xFF2979FF),
            topLeft = Offset(left, top + halfH + gap),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawRoundRect(
            color = Color(0xFF00E676),
            topLeft = Offset(
                left + halfW + gap,
                top + halfH + gap
            ),
            size = Size(halfW, halfH),
            cornerRadius = CornerRadius(5f, 5f)
        )

        drawCircle(
            color = if (darkTheme) {
                GamesColors.BackgroundBottom
            } else {
                GamesColors.LightBackgroundBottom
            },
            radius = w * 0.16f,
            center = Offset(w / 2f, h / 2f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = w * 0.055f,
            center = Offset(w / 2f, h / 2f)
        )
    }
}

// ============================================================
// DEGRADADOS DE LOS BOTONES
// ============================================================

private fun fabGradient(
    game: GameType,
    darkTheme: Boolean
): Brush {
    if (!darkTheme) {
        return when (game) {
            GameType.TIC_TAC_TOE -> Brush.linearGradient(
                listOf(
                    GamesColors.LimeBright,
                    GamesColors.Lime,
                    GamesColors.LimeDark
                )
            )

            GameType.MEMORY -> Brush.linearGradient(
                listOf(
                    Color(0xFFE5FF66),
                    Color(0xFF9FE31A),
                    GamesColors.LimeDark
                )
            )

            GameType.PUZZLE -> Brush.linearGradient(
                listOf(
                    Color(0xFFD4FF4A),
                    Color(0xFF8FD600),
                    Color(0xFF4EA800)
                )
            )

            GameType.SIMON -> Brush.linearGradient(
                listOf(
                    Color(0xFFCFFF4A),
                    Color(0xFF7CCF00),
                    Color(0xFF3E9800)
                )
            )
        }
    }

    return when (game) {
        GameType.TIC_TAC_TOE -> Brush.linearGradient(
            listOf(
                XColor,
                Color(0xFF0891B2),
                Color(0xFF4338CA)
            )
        )

        GameType.MEMORY -> Brush.linearGradient(
            listOf(
                OColorBright,
                OColor,
                Color(0xFFDB2777)
            )
        )

        GameType.PUZZLE -> Brush.linearGradient(
            listOf(
                Color(0xFFC4B5FD),
                Color(0xFF8B5CF6),
                Color(0xFF5B21B6)
            )
        )

        GameType.SIMON -> Brush.linearGradient(
            listOf(
                XColorBright,
                XColor,
                Color(0xFF0891B2)
            )
        )
    }
}

// ============================================================
// HALOS DE LOS BOTONES
// ============================================================

private fun fabGlowGradient(
    game: GameType
): Brush {
    val color = when (game) {
        GameType.TIC_TAC_TOE -> XColorBright
        GameType.MEMORY -> OColorBright
        GameType.PUZZLE -> Color(0xFFA78BFA)
        GameType.SIMON -> XColor
    }

    return Brush.radialGradient(
        colors = listOf(
            color.copy(alpha = 0.65f),
            color.copy(alpha = 0.25f),
            Color.Transparent
        )
    )
}

// ============================================================
// PREVIEW TEMA OSCURO
// ============================================================

@Preview(
    name = "GAMES MCA - Oscuro",
    showBackground = true,
    showSystemUi = false,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun GameMenuPreviewDark() {
    GamesMcaTheme(darkTheme = true) {
        GameMenuScreen(
            darkTheme = true,
            resetKey = 0,
            musicEnabled = false,
            onToggleMusic = {},
            onOpenGame = {}
        )
    }
}

// ============================================================
// PREVIEW TEMA CLARO
// ============================================================

@Preview(
    name = "GAMES MCA - Verde lima",
    showBackground = true,
    showSystemUi = false,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun GameMenuPreviewLight() {
    GamesMcaTheme(darkTheme = false) {
        GameMenuScreen(
            darkTheme = false,
            resetKey = 0,
            musicEnabled = false,
            onToggleMusic = {},
            onOpenGame = {}
        )
    }
}
