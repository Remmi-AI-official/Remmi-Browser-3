package com.remmi.browser.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.remmi.browser.R
import com.remmi.browser.security.ClipboardManager
import com.remmi.browser.security.CurrentTorRoute
import com.remmi.browser.security.GhostRoutePhase
import com.remmi.browser.security.PrivacyProfile
import com.remmi.browser.security.TorCircuit
import com.remmi.browser.security.TorManager
import com.remmi.browser.storage.BookmarkItem
import com.remmi.browser.storage.HistoryItem
import com.remmi.browser.storage.RemmiDatabase
import com.remmi.browser.storage.SearchEngine
import com.remmi.browser.storage.SpeedDialItem
import com.remmi.browser.ui.theme.CyberTheme
import com.remmi.browser.ui.theme.ThemeCyber
import com.remmi.browser.util.DefaultBrowserHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

fun getFaviconUrl(url: String): String = ""

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NewTabPage(
  profile: PrivacyProfile = PrivacyProfile.SHIELD,
  blockedTrackersCount: Int = 0,
  torState: TorManager.TorState = TorManager.TorState.OFF,
  circuit: TorCircuit? = null,
  isDesktopMode: Boolean = false,
  isReaderMode: Boolean = false,
  searchEngine: SearchEngine = SearchEngine.DUCK_DUCK_GO,
  speedDials: List<SpeedDialItem> = emptyList(),
  backgroundAnimation: String = BackgroundTypes.LIGHT_AURA_MESH,
  customWallpaperUri: String? = null,
  wallpaperDimLevel: Float = 0.0f,
  fullscreenWallpaperEnabled: Boolean = true,
  wallpaperScaleMode: String = "CROP",
  onSearch: (query: String, engine: SearchEngine) -> Unit = { _, _ -> },
  onNavigate: (String) -> Unit = {},
  onSelectSearchEngine: (SearchEngine) -> Unit = {},
  onSelectTheme: (CyberTheme) -> Unit = {},
  onAddSpeedDial: (SpeedDialItem) -> Unit = {},
  onEditSpeedDial: (SpeedDialItem) -> Unit = {},
  onDeleteSpeedDial: (String) -> Unit = {},
  onResetSpeedDials: () -> Unit = {},
  onUpdateWallpaper: (String?) -> Unit = {},
  onUpdateBackgroundAnimation: (String) -> Unit = {},
  onUpdateWallpaperDimLevel: (Float) -> Unit = {},
  onUpdateFullscreenWallpaper: (Boolean) -> Unit = {},
  onUpdateWallpaperScaleMode: (String) -> Unit = {},
  onNewTab: () -> Unit = {},
  onOpenBookmarks: () -> Unit = {},
  onOpenHistory: () -> Unit = {},
  onOpenDownloads: () -> Unit = {},
  onOpenSettings: () -> Unit = {},
  onOpenReadingList: () -> Unit = {},
  onToggleDesktop: () -> Unit = {},
  onToggleGhost: () -> Unit = {},
  onToggleReader: () -> Unit = {},
  onInspectCircuit: () -> Unit = {},
  onSecurityShieldClick: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val focusManager = LocalFocusManager.current
  val clipboard = remember { ClipboardManager(context) }
  val scrollState = rememberScrollState()

  var searchQuery by remember { mutableStateOf("") }
  var isSearchFocused by remember { mutableStateOf(false) }
  var showAddDialog by remember { mutableStateOf(false) }
  var showThemeDialog by remember { mutableStateOf(false) }
  var showWallpaperDialog by remember { mutableStateOf(false) }
  var showSearchEngineMenu by remember { mutableStateOf(false) }
  var isFavoritesEditMode by remember { mutableStateOf(false) }

  var editingItem by remember { mutableStateOf<SpeedDialItem?>(null) }
  var itemToDelete by remember { mutableStateOf<SpeedDialItem?>(null) }

  var copiedUrlPrompt by remember { mutableStateOf<String?>(null) }
  var dismissedCopiedUrl by remember { mutableStateOf<String?>(null) }
  var historySuggestions by remember { mutableStateOf<List<HistoryItem>>(emptyList()) }
  var bookmarkSuggestions by remember { mutableStateOf<List<BookmarkItem>>(emptyList()) }

  val scope = rememberCoroutineScope()
  var isDefaultBrowser by remember { mutableStateOf(true) }
  var isDefaultPromptDismissed by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    val isDef = withContext(Dispatchers.IO) {
      DefaultBrowserHelper.isDefaultBrowser(context)
    }
    isDefaultBrowser = isDef
  }

  val defaultBrowserLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) {
    scope.launch(Dispatchers.IO) {
      val isDef = DefaultBrowserHelper.isDefaultBrowser(context)
      withContext(Dispatchers.Main) {
        isDefaultBrowser = isDef
      }
    }
  }

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        scope.launch(Dispatchers.IO) {
          val isDef = DefaultBrowserHelper.isDefaultBrowser(context)
          withContext(Dispatchers.Main) {
            isDefaultBrowser = isDef
          }
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  LaunchedEffect(Unit) {
    val clip = withContext(Dispatchers.IO) { clipboard.getCopiedUrl() }
    if (!clip.isNullOrBlank() && clip != dismissedCopiedUrl) {
      copiedUrlPrompt = clip
    }
  }

  LaunchedEffect(searchQuery) {
    val query = searchQuery.trim()
    if (query.isNotEmpty()) {
      withContext(Dispatchers.IO) {
        val db = RemmiDatabase.getDatabaseAsync(context)
        val hist = db.historyDao().searchHistory(query)
        val bkmk = db.bookmarkDao().searchBookmarks(query)
        historySuggestions = hist.take(4)
        bookmarkSuggestions = bkmk.take(4)
      }
    } else {
      historySuggestions = emptyList()
      bookmarkSuggestions = emptyList()
    }
  }

  val voiceSearchLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val query = matches?.firstOrNull()
      if (!query.isNullOrBlank()) {
        searchQuery = query
        onSearch(query, searchEngine)
      }
    }
  }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      onUpdateWallpaper(uri.toString())
      onUpdateBackgroundAnimation(BackgroundTypes.CUSTOM_IMAGE)
    }
  }

  val colors = ThemeCyber.colors
  val isLight = colors.isLight
  val cardBg = colors.surface
  val cardBorder = colors.surfaceBorder
  val textPrimary = colors.textPrimary
  val textSecondary = colors.textSecondary

  val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
  val (greetingText, greetingIcon) = remember(currentHour) {
    when (currentHour) {
      in 5..11 -> "Good Morning" to "☀️"
      in 12..16 -> "Good Afternoon" to "🌤️"
      in 17..21 -> "Good Evening" to "☾"
      else -> "Good Night" to "✨"
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(if (fullscreenWallpaperEnabled) Color.Transparent else colors.background)
  ) {
    if (!fullscreenWallpaperEnabled) {
      CyberpunkBackground(
        backgroundType = backgroundAnimation,
        customWallpaperUri = customWallpaperUri,
        wallpaperDimLevel = wallpaperDimLevel,
        wallpaperScaleMode = wallpaperScaleMode,
      )
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 18.dp)
        .padding(top = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // ==========================================
      // 1. TOP BAR: SECURE PILL & CIRCULAR PALETTE / SETTINGS BUTTONS
      // ==========================================
      val isGhostMode = profile == PrivacyProfile.GHOST
      val isTorOffline = torState is TorManager.TorState.OFF ||
          torState is TorManager.TorState.STOPPING ||
          CurrentTorRoute.currentPhase == GhostRoutePhase.SHIELD ||
          !CurrentTorRoute.isGhostActive
      val isTorConnected = !isTorOffline &&
          (torState is TorManager.TorState.READY || CurrentTorRoute.isReady) &&
          (isGhostMode || CurrentTorRoute.isGhostActive)
      val isTorConnecting = isGhostMode && !isTorConnected && !isTorOffline && (torState.isConnecting || CurrentTorRoute.currentPhase in listOf(
        GhostRoutePhase.STARTING_TOR,
        GhostRoutePhase.VERIFYING_TOR,
        GhostRoutePhase.APPLYING_GECKO,
        GhostRoutePhase.VERIFYING_GECKO,
        GhostRoutePhase.ROTATING
      ))
      val isTorFailed = isGhostMode && !isTorConnected && (torState is TorManager.TorState.FAILED || CurrentTorRoute.currentPhase == GhostRoutePhase.FAILED)

      val pillAccent = when {
        isTorConnected -> colors.torPurple
        isTorConnecting -> colors.warningYellow
        isTorFailed -> colors.dangerRed
        else -> Color(0xFF1E7E34)
      }
      val pillBg = when {
        isTorConnected -> colors.torPurple.copy(alpha = 0.14f)
        isTorConnecting -> colors.warningYellow.copy(alpha = 0.14f)
        isTorFailed -> colors.dangerRed.copy(alpha = 0.14f)
        isLight -> Color(0xFFE8F5E9)
        else -> colors.successGreen.copy(alpha = 0.16f)
      }
      val pillBorder = when {
        isTorConnected -> colors.torPurple.copy(alpha = 0.38f)
        isTorConnecting -> colors.warningYellow.copy(alpha = 0.38f)
        isTorFailed -> colors.dangerRed.copy(alpha = 0.38f)
        isLight -> Color(0xFFA5D6A7)
        else -> colors.successGreen.copy(alpha = 0.38f)
      }
      val pillLabel = when {
        isTorConnected -> "TOR ACTIVE"
        isTorConnecting -> "CONNECTING..."
        isTorFailed -> "TOR OFFLINE"
        else -> "SECURE"
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(50),
          color = pillBg,
          border = BorderStroke(1.2.dp, pillBorder),
          modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable {
              if (isTorConnected || isTorConnecting || isTorFailed) {
                onInspectCircuit()
              } else {
                onSecurityShieldClick()
              }
            }
            .testTag("home_security_pill")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (isTorConnected || isTorConnecting) {
              Icon(
                painter = painterResource(R.drawable.ic_tor),
                contentDescription = "Tor Status",
                tint = pillAccent,
                modifier = Modifier.size(16.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Security Status",
                tint = pillAccent,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = pillLabel,
              color = pillAccent,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              letterSpacing = 0.8.sp
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          val settingsRepo = remember { com.remmi.browser.storage.SettingsRepository.getInstance(context) }

          Surface(
            shape = CircleShape,
            color = cardBg.copy(alpha = if (isLight) 0.92f else 0.88f),
            border = BorderStroke(1.dp, if (!isLight) colors.primary.copy(alpha = 0.55f) else cardBorder),
            shadowElevation = if (isLight) 2.dp else 0.dp,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = {
                  if (isLight) {
                    settingsRepo.updateAppearanceMode(com.remmi.browser.storage.AppearanceMode.DARK)
                    settingsRepo.updatePureBlackOled(true)
                    Toast.makeText(context, "OLED Dark Mode Enabled", Toast.LENGTH_SHORT).show()
                  } else {
                    settingsRepo.updateAppearanceMode(com.remmi.browser.storage.AppearanceMode.LIGHT)
                    settingsRepo.updatePureBlackOled(false)
                    Toast.makeText(context, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
                  }
                }
              )
              .testTag("home_dark_mode_toggle_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isLight) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = if (isLight) "Enable OLED Dark Mode" else "Switch to Light Mode",
                tint = if (isLight) Color(0xFF6366F1) else Color(0xFFFBBF24),
                modifier = Modifier.size(21.dp)
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = cardBg.copy(alpha = if (isLight) 0.92f else 0.88f),
            border = BorderStroke(1.dp, cardBorder),
            shadowElevation = if (isLight) 2.dp else 0.dp,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = { showThemeDialog = true }
              )
              .testTag("home_theme_toggle_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Theme & Appearance",
                tint = colors.primary,
                modifier = Modifier.size(21.dp)
              )
            }
          }

          Surface(
            shape = CircleShape,
            color = cardBg.copy(alpha = if (isLight) 0.92f else 0.88f),
            border = BorderStroke(1.dp, cardBorder),
            shadowElevation = if (isLight) 2.dp else 0.dp,
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = onOpenSettings
              )
              .testTag("home_settings_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = textPrimary,
                modifier = Modifier.size(21.dp)
              )
            }
          }
        }
      }

      // ==========================================
      // 2. SCROLLABLE HOME CONTENT
      // ==========================================
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(scrollState)
          .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // ==========================================
        // 3. HERO GREETING & ANIMATED PANDA MASCOT
        // ==========================================
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "$greetingText  $greetingIcon",
              color = textSecondary,
              fontSize = 15.5.sp,
              fontWeight = FontWeight.Medium
            )

            Text(
              text = "Explore the web",
              color = textPrimary,
              fontSize = 27.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = (-0.4).sp,
              lineHeight = 32.sp
            )

            Text(
              text = buildAnnotatedString {
                withStyle(
                  SpanStyle(
                    color = colors.primary,
                    fontWeight = FontWeight.ExtraBold
                  )
                ) {
                  append("your ")
                }
                withStyle(
                  SpanStyle(
                    color = textPrimary,
                    fontWeight = FontWeight.ExtraBold
                  )
                ) {
                  append("way")
                }
              },
              fontSize = 27.sp,
              letterSpacing = (-0.4).sp,
              lineHeight = 32.sp
            )
          }

          PandaMascotArt(
            size = 112.dp,
            isDarkTheme = !isLight,
            isGhost = isGhostMode,
            accentColor = colors.primary
          )
        }

        // ==========================================
        // 4. DEFAULT BROWSER BANNER (matches screenshot)
        // ==========================================
        if (!isDefaultBrowser && !isDefaultPromptDismissed) {
          val bannerBg = if (isLight) Color(0xFFEFF6FF).copy(alpha = 0.92f) else cardBg
          val bannerBorder = if (isLight) Color(0xFFBFDBFE) else colors.primary.copy(alpha = 0.35f)
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = bannerBg,
            border = BorderStroke(1.dp, bannerBorder),
            shadowElevation = if (isLight) 1.dp else 0.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
              .testTag("home_set_default_browser_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.primary.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(19.dp)
                  )
                }
                Text(
                  text = "Set Remmi as default browser",
                  color = textPrimary,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f)
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Button(
                  onClick = {
                    val activity = context as? Activity
                    if (activity != null) {
                      DefaultBrowserHelper.requestSetDefaultBrowser(activity, defaultBrowserLauncher)
                    }
                  },
                  shape = RoundedCornerShape(11.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White
                  ),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                  modifier = Modifier
                    .height(34.dp)
                    .testTag("home_set_default_browser_btn")
                ) {
                  Text(
                    text = "SET DEFAULT",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                  )
                }

                IconButton(
                  onClick = { isDefaultPromptDismissed = true },
                  modifier = Modifier.size(26.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        // ==========================================
        // 5. PILL SEARCH / ADDRESS BAR
        // ==========================================
        Surface(
          shape = RoundedCornerShape(28.dp),
          color = cardBg.copy(alpha = if (isLight) 0.96f else 0.92f),
          border = BorderStroke(
            width = if (isSearchFocused) 1.6.dp else 1.dp,
            color = if (isSearchFocused) colors.primary else cardBorder
          ),
          shadowElevation = if (isLight) 3.dp else 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .testTag("home_search_bar_surface")
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { showSearchEngineMenu = true },
              modifier = Modifier.size(34.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Engine",
                tint = colors.primary,
                modifier = Modifier.size(23.dp)
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Box(
              modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search or enter web address",
                  color = colors.textMuted,
                  fontSize = 15.5.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                  color = textPrimary,
                  fontSize = 15.5.sp,
                  fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(colors.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                  onSearch = {
                    val query = searchQuery.trim()
                    if (query.isNotEmpty()) {
                      focusManager.clearFocus()
                      val target = resolveUserInputUrlOrSearch(query, searchEngine)
                      onNavigate(target)
                    }
                  }
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("home_search_input")
              )
            }

            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { searchQuery = "" },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear search",
                  tint = textSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            } else {
              IconButton(
                onClick = {
                  val clip = clipboard.getCopiedUrl()
                  if (!clip.isNullOrBlank()) {
                    searchQuery = clip
                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                  } else {
                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste URL from Clipboard",
                  tint = textSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }

              IconButton(
                onClick = {
                  try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                      putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                      putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to search...")
                    }
                    voiceSearchLauncher.launch(intent)
                  } catch (_: Exception) {
                    Toast.makeText(context, "Voice search unavailable", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Voice Search",
                  tint = textSecondary,
                  modifier = Modifier.size(21.dp)
                )
              }
            }
          }
        }

        // Live Suggestions Card
        if (searchQuery.trim().isNotEmpty() && (historySuggestions.isNotEmpty() || bookmarkSuggestions.isNotEmpty())) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            shadowElevation = if (isLight) 3.dp else 0.dp,
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              bookmarkSuggestions.forEach { item ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      focusManager.clearFocus()
                      onNavigate(item.url)
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = colors.warningYellow,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.title,
                      color = textPrimary,
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = item.url,
                      color = textSecondary,
                      fontSize = 11.5.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }

              historySuggestions.forEach { item ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                      focusManager.clearFocus()
                      onNavigate(item.url)
                    }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.title,
                      color = textPrimary,
                      fontSize = 13.5.sp,
                      fontWeight = FontWeight.SemiBold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = item.url,
                      color = textSecondary,
                      fontSize = 11.5.sp,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }

        // Copied Link Banner
        copiedUrlPrompt?.let { url ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = null,
                  tint = colors.primary,
                  modifier = Modifier.size(20.dp)
                )
                Column {
                  Text(
                    text = "Link in clipboard",
                    color = colors.primary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = url,
                    color = textPrimary,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                  onClick = {
                    copiedUrlPrompt = null
                    dismissedCopiedUrl = url
                    onNavigate(url)
                  }
                ) {
                  Text("Open", color = colors.primary, fontWeight = FontWeight.Bold)
                }
                IconButton(
                  onClick = {
                    copiedUrlPrompt = null
                    dismissedCopiedUrl = url
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textSecondary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }
        }

        // ==========================================
        // 6. PASTEL CIRCULAR QUICK ACTIONS CARD WITH PAGER (exact match to screenshot)
        // ==========================================
        val quickPagerState = rememberPagerState(pageCount = { 2 })
        Surface(
          shape = RoundedCornerShape(26.dp),
          color = cardBg.copy(alpha = if (isLight) 0.96f else 0.92f),
          border = BorderStroke(1.dp, cardBorder),
          shadowElevation = if (isLight) 4.dp else 0.dp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp)
            .testTag("home_quick_actions_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp, bottom = 10.dp, start = 6.dp, end = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            HorizontalPager(
              state = quickPagerState,
              modifier = Modifier.fillMaxWidth()
            ) { page ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (page == 0) {
                  PastelQuickActionCircle(
                    icon = Icons.Default.Bookmark,
                    label = "Bookmarks",
                    circleBg = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1E3A8A).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFBBDEFB) else Color(0xFF3B82F6).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF1976D2) else Color(0xFF60A5FA),
                    textPrimary = textPrimary,
                    onClick = onOpenBookmarks
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.History,
                    label = "History",
                    circleBg = if (isLight) Color(0xFFF3E5F5) else Color(0xFF581C87).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFE1BEE7) else Color(0xFFA855F7).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF8E24AA) else Color(0xFFC084FC),
                    textPrimary = textPrimary,
                    onClick = onOpenHistory
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.Download,
                    label = "Downloads",
                    circleBg = if (isLight) Color(0xFFE0F2F1) else Color(0xFF064E3B).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFB2DFDB) else Color(0xFF10B981).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF00897B) else Color(0xFF34D399),
                    textPrimary = textPrimary,
                    onClick = onOpenDownloads
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.MenuBook,
                    label = "Reading List",
                    circleBg = if (isLight) Color(0xFFFFF8E1) else Color(0xFF78350F).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFFFECB3) else Color(0xFFF59E0B).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFFF57C00) else Color(0xFFFBBF24),
                    textPrimary = textPrimary,
                    onClick = onOpenReadingList
                  )
                  PastelQuickActionCircle(
                    painter = painterResource(R.drawable.ic_tor),
                    label = "Tor",
                    circleBg = if (isLight) Color(0xFFFCE4EC) else Color(0xFF701A75).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFF8BBD0) else Color(0xFFD946EF).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFFAB47BC) else Color(0xFFE879F9),
                    textPrimary = textPrimary,
                    onClick = onToggleGhost
                  )
                } else {
                  PastelQuickActionCircle(
                    icon = Icons.Default.Shield,
                    label = "Shield",
                    circleBg = if (isLight) Color(0xFFE8F5E9) else Color(0xFF064E3B).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFC8E6C9) else Color(0xFF22C55E).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF2E7D32) else Color(0xFF4ADE80),
                    textPrimary = textPrimary,
                    onClick = onSecurityShieldClick
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.DesktopWindows,
                    label = "Desktop",
                    circleBg = if (isLight) Color(0xFFE8EAF6) else Color(0xFF312E81).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFC5CAE9) else Color(0xFF6366F1).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF3949AB) else Color(0xFF818CF8),
                    textPrimary = textPrimary,
                    onClick = onToggleDesktop
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.Palette,
                    label = "Themes",
                    circleBg = if (isLight) Color(0xFFE0F7FA) else Color(0xFF164E63).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFB2EBF2) else Color(0xFF06B6D4).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF0097A7) else Color(0xFF22D3EE),
                    textPrimary = textPrimary,
                    onClick = { showThemeDialog = true }
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.AddCircleOutline,
                    label = "New Tab",
                    circleBg = if (isLight) Color(0xFFE3F2FD) else Color(0xFF1E3A8A).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFBBDEFB) else Color(0xFF3B82F6).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF1976D2) else Color(0xFF60A5FA),
                    textPrimary = textPrimary,
                    onClick = onNewTab
                  )
                  PastelQuickActionCircle(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    circleBg = if (isLight) Color(0xFFECEFF1) else Color(0xFF334155).copy(alpha = 0.38f),
                    circleBorder = if (isLight) Color(0xFFCFD8DC) else Color(0xFF64748B).copy(alpha = 0.45f),
                    iconTint = if (isLight) Color(0xFF455A64) else Color(0xFFCBD5E1),
                    textPrimary = textPrimary,
                    onClick = onOpenSettings
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pager Indicator (Elongated blue pill + grey dot)
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              for (i in 0 until 2) {
                val selected = quickPagerState.currentPage == i
                Box(
                  modifier = Modifier
                    .height(5.dp)
                    .width(if (selected) 18.dp else 5.dp)
                    .clip(CircleShape)
                    .background(
                      if (selected) colors.primary
                      else colors.textMuted.copy(alpha = 0.32f)
                    )
                )
              }
            }
          }
        }

        // ==========================================
        // 7. QUICK LINKS SECTION (directly on background, matching screenshot)
        // ==========================================
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "Quick Links",
              color = textPrimary,
              fontSize = 19.sp,
              fontWeight = FontWeight.ExtraBold
            )
            IconButton(
              onClick = { isFavoritesEditMode = !isFavoritesEditMode },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = if (isFavoritesEditMode) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = "Edit Quick Links",
                tint = if (isFavoritesEditMode) colors.primary else textSecondary.copy(alpha = 0.75f),
                modifier = Modifier.size(17.dp)
              )
            }
          }

          Text(
            text = "Show all",
            color = colors.primary,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { onOpenBookmarks() }
              .padding(horizontal = 6.dp, vertical = 4.dp)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val defaultSpeedDials = listOf(
          SpeedDialItem(id = "ddg", title = "DuckDuckGo", url = "https://duckduckgo.com", iconKey = "search"),
          SpeedDialItem(id = "tor", title = "Check Tor", url = "https://check.torproject.org", iconKey = "vpn"),
          SpeedDialItem(id = "github", title = "GitHub", url = "https://github.com", iconKey = "code"),
          SpeedDialItem(id = "eff", title = "EFF", url = "https://www.eff.org", iconKey = "policy"),
          SpeedDialItem(id = "proton", title = "Proton", url = "https://proton.me", iconKey = "shield"),
          SpeedDialItem(id = "wikipedia", title = "Wikipedia", url = "https://en.wikipedia.org", iconKey = "wiki"),
          SpeedDialItem(id = "hackernews", title = "HackerNews", url = "https://news.ycombinator.com", iconKey = "news"),
          SpeedDialItem(id = "reddit", title = "Reddit", url = "https://www.reddit.com", iconKey = "forum"),
        )
        val effectiveSpeedDials = if (speedDials.isNotEmpty()) speedDials else defaultSpeedDials

        val chunkedFavorites = effectiveSpeedDials.chunked(5)
        chunkedFavorites.forEach { rowItems ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            rowItems.forEach { item ->
              FavoriteTile(
                item = item,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                isLight = isLight,
                isEditMode = isFavoritesEditMode,
                onClick = {
                  if (isFavoritesEditMode) {
                    editingItem = item
                  } else {
                    onNavigate(item.url)
                  }
                },
                onLongClick = { editingItem = item },
                onDelete = { itemToDelete = item },
                modifier = Modifier.weight(1f)
              )
            }
            val emptySlots = 5 - rowItems.size
            if (emptySlots > 0 && rowItems == chunkedFavorites.last()) {
              FavoriteAddTile(
                isLight = isLight,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textSecondary = textPrimary,
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
              )
              for (i in 1 until emptySlots) {
                Spacer(modifier = Modifier.weight(1f))
              }
            } else {
              for (i in 0 until emptySlots) {
                Spacer(modifier = Modifier.weight(1f))
              }
            }
          }
        }

        if (effectiveSpeedDials.size % 5 == 0) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start
          ) {
            FavoriteAddTile(
              isLight = isLight,
              cardBg = cardBg,
              cardBorder = cardBorder,
              textSecondary = textPrimary,
              onClick = { showAddDialog = true },
              modifier = Modifier.width(68.dp)
            )
          }
        }
      }
    }
  }

  // ==========================================
  // DIALOGS & OVERLAYS
  // ==========================================
  if (showThemeDialog) {
    ThemeSelectorDialog(
      currentAnimation = backgroundAnimation,
      customWallpaperUri = customWallpaperUri,
      wallpaperDimLevel = wallpaperDimLevel,
      wallpaperScaleMode = wallpaperScaleMode,
      onDismiss = { showThemeDialog = false },
      onSelectTheme = { theme -> onSelectTheme(theme) },
      onSelectAnimation = { anim ->
        if (customWallpaperUri != null) {
          onUpdateWallpaper(null)
        }
        onUpdateBackgroundAnimation(anim)
      },
      onPickPhoto = { photoPickerLauncher.launch("image/*") },
      onClearWallpaper = {
        onUpdateWallpaper(null)
        onUpdateBackgroundAnimation(BackgroundTypes.LIGHT_AURA_MESH)
      },
      onUpdateDimLevel = onUpdateWallpaperDimLevel,
      onUpdateScaleMode = onUpdateWallpaperScaleMode
    )
  }

  if (showSearchEngineMenu) {
    SearchEngineSelectorDialog(
      currentEngine = searchEngine,
      onDismiss = { showSearchEngineMenu = false },
      onSelect = { engine ->
        onSelectSearchEngine(engine)
        showSearchEngineMenu = false
      }
    )
  }

  if (showAddDialog) {
    AddFavoriteDialog(
      onDismiss = { showAddDialog = false },
      onAdd = { title, url ->
        val fixedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) "https://$url" else url
        onAddSpeedDial(SpeedDialItem(title = title, url = fixedUrl))
        showAddDialog = false
      }
    )
  }

  editingItem?.let { item ->
    EditFavoriteDialog(
      item = item,
      onDismiss = { editingItem = null },
      onSave = { updated ->
        onEditSpeedDial(updated)
        editingItem = null
      },
      onDelete = {
        onDeleteSpeedDial(item.id)
        editingItem = null
      }
    )
  }

  itemToDelete?.let { item ->
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text("Delete Quick Link?", fontWeight = FontWeight.Bold, color = textPrimary) },
      text = { Text("Are you sure you want to remove \"${item.title}\" from quick links?", color = textSecondary) },
      confirmButton = {
        TextButton(
          onClick = {
            onDeleteSpeedDial(item.id)
            itemToDelete = null
          }
        ) {
          Text("Delete", color = colors.dangerRed, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text("Cancel", color = textSecondary)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = cardBg
    )
  }

  if (showWallpaperDialog) {
    WallpaperDialog(
      customWallpaperUri = customWallpaperUri,
      wallpaperDimLevel = wallpaperDimLevel,
      wallpaperScaleMode = wallpaperScaleMode,
      onDismiss = { showWallpaperDialog = false },
      onPickPhoto = { photoPickerLauncher.launch("image/*") },
      onClearWallpaper = {
        onUpdateWallpaper(null)
        onUpdateBackgroundAnimation(BackgroundTypes.LIGHT_AURA_MESH)
      },
      onUpdateDimLevel = onUpdateWallpaperDimLevel,
      onUpdateScaleMode = onUpdateWallpaperScaleMode
    )
  }
}

@Composable
private fun PastelQuickActionCircle(
  icon: ImageVector? = null,
  painter: androidx.compose.ui.graphics.painter.Painter? = null,
  label: String,
  circleBg: Color,
  circleBorder: Color,
  iconTint: Color,
  textPrimary: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 4.dp, vertical = 4.dp)
  ) {
    Surface(
      shape = CircleShape,
      color = circleBg,
      border = BorderStroke(1.5.dp, circleBorder),
      modifier = Modifier.size(52.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        if (painter != null) {
          Icon(
            painter = painter,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(23.dp)
          )
        } else if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(23.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(7.dp))

    Text(
      text = label,
      color = textPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteTile(
  item: SpeedDialItem,
  cardBg: Color,
  cardBorder: Color,
  textPrimary: Color,
  isLight: Boolean,
  isEditMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .padding(horizontal = 3.dp)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
  ) {
    Box(
      modifier = Modifier.size(58.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        shadowElevation = if (isLight) 2.dp else 0.dp,
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(18.dp))
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          BrandFaviconIcon(
            url = item.url,
            iconKey = item.iconKey,
            title = item.title,
            size = 32.dp
          )
        }
      }

      if (isEditMode) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-4).dp)
            .size(19.dp)
            .clip(CircleShape)
            .background(Color(0xFFDC2626))
            .clickable(onClick = onDelete),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.size(12.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = item.title,
      color = textPrimary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
private fun FavoriteAddTile(
  isLight: Boolean,
  cardBg: Color,
  cardBorder: Color,
  textSecondary: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accent = ThemeCyber.colors.primary
  val addBg = if (isLight) Color(0xFFEFF6FF) else cardBg
  val addBorder = if (isLight) Color(0xFFBFDBFE) else cardBorder
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .padding(horizontal = 3.dp)
      .clip(RoundedCornerShape(18.dp))
      .clickable(onClick = onClick)
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = addBg,
      border = BorderStroke(1.dp, addBorder),
      shadowElevation = if (isLight) 1.dp else 0.dp,
      modifier = Modifier
        .size(58.dp)
        .clip(RoundedCornerShape(18.dp))
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Quick Link",
          tint = accent,
          modifier = Modifier.size(26.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = "Add",
      color = textSecondary,
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center
    )
  }
}

private data class LiveMotionOption(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector
)

@Composable
private fun ThemeSelectorDialog(
  currentAnimation: String,
  customWallpaperUri: String?,
  wallpaperDimLevel: Float,
  wallpaperScaleMode: String,
  onDismiss: () -> Unit,
  onSelectTheme: (CyberTheme) -> Unit,
  onSelectAnimation: (String) -> Unit,
  onPickPhoto: () -> Unit,
  onClearWallpaper: () -> Unit,
  onUpdateDimLevel: (Float) -> Unit,
  onUpdateScaleMode: (String) -> Unit
) {
  val colors = ThemeCyber.colors
  val accentColor = colors.primary
  var selectedTab by remember { mutableIntStateOf(0) }

  val motionOptions = remember {
    listOf(
      LiveMotionOption(
        id = BackgroundTypes.LIGHT_AURA_MESH,
        title = "Soft Aura Waves",
        subtitle = "Ambient fluid mesh gradient",
        icon = Icons.Default.AutoAwesome
      ),
      LiveMotionOption(
        id = BackgroundTypes.LIGHT_FLOATING_ORBS,
        title = "Floating Pastel Orbs",
        subtitle = "Soft floating glowing spheres",
        icon = Icons.Default.BubbleChart
      ),
      LiveMotionOption(
        id = BackgroundTypes.LIGHT_GEOMETRIC_DOTS,
        title = "Pulsing Dot Grid",
        subtitle = "Clean geometric pulsing grid",
        icon = Icons.Default.Apps
      ),
      LiveMotionOption(
        id = BackgroundTypes.LIGHT_CONSTELLATION,
        title = "Starry Constellation",
        subtitle = "Interactive connected nodes",
        icon = Icons.Default.Hub
      ),
      LiveMotionOption(
        id = BackgroundTypes.DIGITAL_AURORA,
        title = "Digital Aurora",
        subtitle = "Flowing neon aurora ribbons",
        icon = Icons.Default.Waves
      ),
      LiveMotionOption(
        id = BackgroundTypes.NEON_PARTICLES,
        title = "Neon Particles",
        subtitle = "Drifting glowing cyber particles",
        icon = Icons.Default.Grain
      ),
      LiveMotionOption(
        id = BackgroundTypes.CYBERPUNK_GRID,
        title = "Cyberpunk Grid",
        subtitle = "Perspective synthwave horizon",
        icon = Icons.Default.GridOn
      ),
      LiveMotionOption(
        id = BackgroundTypes.MATRIX_RAIN,
        title = "Matrix Rain",
        subtitle = "Digital falling code stream",
        icon = Icons.Default.Code
      ),
      LiveMotionOption(
        id = BackgroundTypes.MINIMAL_GRADIENT,
        title = "Static Minimal",
        subtitle = "Clean static minimal surface",
        icon = Icons.Default.FilterDrama
      )
    )
  }

  val themes = listOf(
    CyberTheme.NORMAL_DEFAULT,
    CyberTheme.MINIMAL_DARK,
    CyberTheme.JARVIS,
    CyberTheme.CYBER_MATRIX,
    CyberTheme.STARK_IND,
    CyberTheme.VERONICA
  )

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      shadowElevation = 10.dp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 2.dp, vertical = 12.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Header matching Screenshot 1
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Palette,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(25.dp)
            )
            Text(
              text = "Theme & Appearance",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 19.sp,
              color = colors.textPrimary
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = colors.textSecondary
            )
          }
        }

        // 3-Tab Segmented Bar ("✨ Live Motion", "🎨 Colors", "🖼️ Wallpaper")
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = colors.surfaceLight,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            val tabs = listOf("✨ Live Motion", "🎨 Colors", "🖼️ Wallpaper")
            tabs.forEachIndexed { index, title ->
              val isSelected = selectedTab == index
              Surface(
                shape = RoundedCornerShape(11.dp),
                color = if (isSelected) colors.surface else Color.Transparent,
                shadowElevation = if (isSelected && colors.isLight) 1.dp else 0.dp,
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(11.dp))
                  .clickable { selectedTab = index }
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 9.dp, horizontal = 2.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accentColor else colors.textSecondary,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }

        when (selectedTab) {
          0 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Choose Live Background Animation:",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
              )

              LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 350.dp)
              ) {
                items(motionOptions) { option ->
                  val isSelected = currentAnimation == option.id && customWallpaperUri.isNullOrBlank()
                  val itemBg = if (isSelected) {
                    if (colors.isLight) Color(0xFFEFF6FF) else accentColor.copy(alpha = 0.16f)
                  } else {
                    colors.surface
                  }
                  val itemBorder = if (isSelected) accentColor else colors.surfaceBorder
                  Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = itemBg,
                    border = BorderStroke(if (isSelected) 1.8.dp else 1.dp, itemBorder),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(16.dp))
                      .clickable { onSelectAnimation(option.id) }
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                              if (isSelected) accentColor.copy(alpha = 0.18f)
                              else colors.surfaceLight
                            ),
                          contentAlignment = Alignment.Center
                        ) {
                          Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (isSelected) accentColor else colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                          )
                        }
                        Column {
                          Text(
                            text = option.title,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                          )
                          Text(
                            text = option.subtitle,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                          )
                        }
                      }

                      if (isSelected) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = "Selected",
                          tint = accentColor,
                          modifier = Modifier.size(24.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          1 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "Select Color Palette:",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
              )

              LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 310.dp)
              ) {
                items(themes) { theme ->
                  Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.background,
                    border = BorderStroke(1.4.dp, theme.primaryAccent),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(64.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .clickable { onSelectTheme(theme) }
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(22.dp)
                          .clip(CircleShape)
                          .background(theme.primaryAccent)
                      )
                      Column {
                        Text(
                          text = theme.displayName,
                          fontSize = 12.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = colors.textPrimary,
                          maxLines = 1
                        )
                        Text(
                          text = if (theme.isLight) "Light" else "Dark",
                          fontSize = 11.sp,
                          color = colors.textSecondary
                        )
                      }
                    }
                  }
                }
              }
            }
          }

          else -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              Text(
                text = "Custom Gallery Wallpaper:",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
              )

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Button(
                  onClick = onPickPhoto,
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Choose Photo")
                }

                if (customWallpaperUri != null) {
                  OutlinedButton(
                    onClick = onClearWallpaper,
                    shape = RoundedCornerShape(12.dp)
                  ) {
                    Text("Reset")
                  }
                }
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val visibilityPercent = ((1f - wallpaperDimLevel) * 100).toInt()
                Text(
                  text = "Wallpaper Clarity: $visibilityPercent%",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = colors.textPrimary
                )
                Slider(
                  value = 1f - wallpaperDimLevel,
                  onValueChange = { onUpdateDimLevel(1f - it) },
                  colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor
                  )
                )
              }

              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "Scaling Mode",
                  fontSize = 12.5.sp,
                  fontWeight = FontWeight.Medium,
                  color = colors.textPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  listOf("CROP" to "Fill Crop", "FIT" to "Fit", "FILL" to "Stretch").forEach { (mode, label) ->
                    val isSelected = wallpaperScaleMode.equals(mode, ignoreCase = true)
                    FilterChip(
                      selected = isSelected,
                      onClick = { onUpdateScaleMode(mode) },
                      label = { Text(label, fontSize = 11.5.sp) }
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SearchEngineSelectorDialog(
  currentEngine: SearchEngine,
  onDismiss: () -> Unit,
  onSelect: (SearchEngine) -> Unit
) {
  val colors = ThemeCyber.colors
  val engines = SearchEngine.entries

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Select Search Engine",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = colors.textPrimary
        )

        engines.forEach { engine ->
          val isSelected = engine == currentEngine
          val engineColor = colors.primary
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) engineColor.copy(alpha = 0.12f) else colors.background,
            border = BorderStroke(
              1.dp,
              if (isSelected) engineColor else colors.surfaceBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelect(engine) }
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(engineColor.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = engineColor,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Column {
                  Text(
                    text = engine.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                  )
                  Text(
                    text = engine.subtitle,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                  )
                }
              }
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Selected",
                  tint = engineColor,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AddFavoriteDialog(
  onDismiss: () -> Unit,
  onAdd: (title: String, url: String) -> Unit
) {
  val colors = ThemeCyber.colors
  var title by remember { mutableStateOf("") }
  var url by remember { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = "Add Quick Link",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = colors.textPrimary
        )

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          placeholder = { Text("e.g. GitHub") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("URL / Web Address") },
          placeholder = { Text("e.g. github.com") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = colors.textSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (url.isNotBlank()) {
                val resolvedTitle = if (title.isBlank()) url.replace("https://", "").replace("http://", "").substringBefore("/") else title
                onAdd(resolvedTitle, url)
              }
            },
            enabled = url.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Add Link")
          }
        }
      }
    }
  }
}

@Composable
private fun EditFavoriteDialog(
  item: SpeedDialItem,
  onDismiss: () -> Unit,
  onSave: (SpeedDialItem) -> Unit,
  onDelete: () -> Unit
) {
  val colors = ThemeCyber.colors
  var title by remember { mutableStateOf(item.title) }
  var url by remember { mutableStateOf(item.url) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Edit Quick Link",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary
          )
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = colors.dangerRed
            )
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("URL / Web Address") },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel", color = colors.textSecondary)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              if (url.isNotBlank()) {
                onSave(item.copy(title = title, url = url))
              }
            },
            enabled = url.isNotBlank(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text("Save")
          }
        }
      }
    }
  }
}

@Composable
private fun WallpaperDialog(
  customWallpaperUri: String?,
  wallpaperDimLevel: Float,
  wallpaperScaleMode: String,
  onDismiss: () -> Unit,
  onPickPhoto: () -> Unit,
  onClearWallpaper: () -> Unit,
  onUpdateDimLevel: (Float) -> Unit,
  onUpdateScaleMode: (String) -> Unit
) {
  val colors = ThemeCyber.colors

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Column(
        modifier = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Wallpaper Settings",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary
          )
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = colors.textSecondary
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = onPickPhoto,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Choose Image")
          }

          if (customWallpaperUri != null) {
            OutlinedButton(
              onClick = onClearWallpaper,
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Reset")
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          val visibilityPercent = ((1f - wallpaperDimLevel) * 100).toInt()
          Text(
            text = "Clarity: $visibilityPercent%",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
          )
          Slider(
            value = 1f - wallpaperDimLevel,
            onValueChange = { onUpdateDimLevel(1f - it) },
            colors = SliderDefaults.colors(
              thumbColor = colors.primary,
              activeTrackColor = colors.primary
            )
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Scaling Mode",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("CROP" to "Fill Crop", "FIT" to "Fit", "FILL" to "Stretch").forEach { (mode, label) ->
              val isSelected = wallpaperScaleMode.equals(mode, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = { onUpdateScaleMode(mode) },
                label = { Text(label, fontSize = 11.5.sp) }
              )
            }
          }
        }
      }
    }
  }
}
