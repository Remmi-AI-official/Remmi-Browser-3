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
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.remmi.browser.ui.theme.CyberMonoFamily
import com.remmi.browser.ui.theme.CyberTheme
import com.remmi.browser.ui.theme.PictorialIconBadge
import com.remmi.browser.ui.theme.PictorialPalette
import com.remmi.browser.ui.theme.ThemeCyber
import com.remmi.browser.ui.theme.pictorialColorFor
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
      in 17..21 -> "Good Evening" to "🌙"
      else -> "Good Night" to "✨"
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
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
        .padding(horizontal = 16.dp)
        .padding(top = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // ==========================================
      // 1. TOP BAR: NOTE STATUS PILL & PICTORIAL ACTIONS
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
        else -> colors.secondary
      }
      val pillLabel = when {
        isTorConnected -> "Tor Onion Active"
        isTorConnecting -> "Connecting Tor..."
        isTorFailed -> "Tor Offline"
        else -> "Shield Protected"
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = pillAccent.copy(alpha = 0.14f),
          border = BorderStroke(1.dp, pillAccent.copy(alpha = 0.35f)),
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
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
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
          ) {
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(pillAccent.copy(alpha = 0.22f)),
              contentAlignment = Alignment.Center
            ) {
              if (isTorConnected || isTorConnecting) {
                Icon(
                  painter = painterResource(R.drawable.ic_tor),
                  contentDescription = "Tor Status",
                  tint = pillAccent,
                  modifier = Modifier.size(13.dp)
                )
              } else {
                Icon(
                  imageVector = Icons.Default.Shield,
                  contentDescription = "Security Status",
                  tint = pillAccent,
                  modifier = Modifier.size(13.dp)
                )
              }
            }
            Text(
              text = pillLabel,
              color = pillAccent,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Colorful Pictorial Theme Button
          Surface(
            shape = RoundedCornerShape(13.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(13.dp))
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = { showThemeDialog = true }
              )
              .testTag("home_theme_toggle_button")
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD96B27).copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Switch Theme",
                tint = Color(0xFFD96B27),
                modifier = Modifier.size(20.dp)
              )
            }
          }

          // Colorful Pictorial Settings Button
          Surface(
            shape = RoundedCornerShape(13.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(13.dp))
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = onOpenSettings
              )
              .testTag("home_settings_button")
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2563EB).copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }
      }

      // ==========================================
      // 2. SCROLLABLE NOTES-STYLE HOME CONTENT
      // ==========================================
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .verticalScroll(scrollState)
          .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Default Browser Prompt Note Card
        if (!isDefaultBrowser && !isDefaultPromptDismissed) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 10.dp)
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
                PictorialIconBadge(
                  icon = Icons.Default.Language,
                  color = Color(0xFF2563EB),
                  size = 34.dp,
                  iconSize = 18.dp,
                  cornerRadius = 10.dp
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Set Remmi as default browser",
                    color = textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "Open all web links with private note protection",
                    color = textSecondary,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
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
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = Color.White
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                  modifier = Modifier
                    .height(30.dp)
                    .testTag("home_set_default_browser_btn")
                ) {
                  Text(
                    text = "SET DEFAULT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                IconButton(
                  onClick = { isDefaultPromptDismissed = true },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = textSecondary,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }

        // ==========================================
        // 3. HERO WELCOME NOTE CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Warm top Note accent bar
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(
                      colors.primary,
                      colors.secondary,
                      colors.torPurple
                    )
                  )
                )
            )
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = colors.primary.copy(alpha = 0.12f)
                ) {
                  Text(
                    text = "$greetingText $greetingIcon",
                    color = colors.primary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }

                Text(
                  text = "Remmi Browser",
                  color = textPrimary,
                  fontSize = 22.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = (-0.3).sp
                )

                Text(
                  text = buildAnnotatedString {
                    append("Explore the web ")
                    withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                      append("privately & smoothly")
                    }
                  },
                  color = textSecondary,
                  fontSize = 13.sp
                )
              }

              PandaMascotArt(
                size = 82.dp,
                isDarkTheme = !isLight,
                isGhost = isGhostMode,
                accentColor = colors.primary,
                modifier = Modifier.padding(start = 8.dp)
              )
            }
          }
        }

        // ==========================================
        // 4. NOTE-STYLE SEARCH / ADDRESS BAR
        // ==========================================
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = cardBg,
          border = BorderStroke(
            width = if (isSearchFocused) 1.5.dp else 1.dp,
            color = if (isSearchFocused) colors.primary else cardBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag("home_search_bar_surface")
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Colorful Pictorial Search Engine Button
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary.copy(alpha = 0.14f))
                .border(0.8.dp, colors.primary.copy(alpha = 0.30f), RoundedCornerShape(12.dp))
                .clickable { showSearchEngineMenu = true },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Engine",
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
              modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
              contentAlignment = Alignment.CenterStart
            ) {
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search ${searchEngine.displayName} or type URL...",
                  color = colors.textMuted,
                  fontSize = 14.5.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                  color = textPrimary,
                  fontSize = 14.5.sp,
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
              // Colorful Paste Badge
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF2E7D5B).copy(alpha = 0.13f))
                  .clickable {
                    val clip = clipboard.getCopiedUrl()
                    if (!clip.isNullOrBlank()) {
                      searchQuery = clip
                      Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste URL from Clipboard",
                  tint = Color(0xFF2E7D5B),
                  modifier = Modifier.size(17.dp)
                )
              }

              Spacer(modifier = Modifier.width(6.dp))

              // Colorful Voice Search Badge
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(0xFF6E48AA).copy(alpha = 0.13f))
                  .clickable {
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
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Voice Search",
                  tint = Color(0xFF6E48AA),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        // Live Suggestions Note Card
        if (searchQuery.trim().isNotEmpty() && (historySuggestions.isNotEmpty() || bookmarkSuggestions.isNotEmpty())) {
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                  PictorialIconBadge(
                    icon = Icons.Default.Star,
                    color = Color(0xFFD97706),
                    size = 32.dp,
                    iconSize = 16.dp,
                    cornerRadius = 9.dp
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
                  PictorialIconBadge(
                    icon = Icons.Default.History,
                    color = Color(0xFF6E48AA),
                    size = 32.dp,
                    iconSize = 16.dp,
                    cornerRadius = 9.dp
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

        // Copied Link Note Banner
        copiedUrlPrompt?.let { url ->
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, colors.secondary.copy(alpha = 0.45f)),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
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
                PictorialIconBadge(
                  icon = Icons.Default.ContentPaste,
                  color = colors.secondary,
                  size = 34.dp,
                  iconSize = 17.dp,
                  cornerRadius = 10.dp
                )
                Column {
                  Text(
                    text = "Link in clipboard",
                    color = colors.secondary,
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
                  Text("Open", color = colors.secondary, fontWeight = FontWeight.Bold)
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
        // 5. QUICK TOOLS NOTE CARD (Pictorial Icons)
        // ==========================================
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .testTag("home_quick_actions_card")
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 14.dp, horizontal = 10.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.primary)
                )
                Text(
                  text = "Workspace Notes & Tools",
                  color = textPrimary,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Text(
                text = if (isGhostMode) "Ghost Profile" else "Shield Profile",
                color = pillAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              QuickActionItem(
                icon = Icons.Default.Bookmark,
                label = "Bookmarks",
                textPrimary = textPrimary,
                accentColor = Color(0xFF2563EB),
                onClick = onOpenBookmarks
              )
              QuickActionItem(
                icon = Icons.Default.History,
                label = "History",
                textPrimary = textPrimary,
                accentColor = Color(0xFF6E48AA),
                onClick = onOpenHistory
              )
              QuickActionItem(
                icon = Icons.Default.Download,
                label = "Downloads",
                textPrimary = textPrimary,
                accentColor = Color(0xFF2E7D5B),
                onClick = onOpenDownloads
              )
              QuickActionItem(
                icon = Icons.Default.MenuBook,
                label = "Reading List",
                textPrimary = textPrimary,
                accentColor = Color(0xFFD96B27),
                onClick = onOpenReadingList
              )
              QuickActionItem(
                painter = painterResource(R.drawable.ic_tor),
                label = "Tor Mode",
                textPrimary = textPrimary,
                accentColor = Color(0xFF9333EA),
                onClick = onToggleGhost
              )
            }
          }
        }

        // ==========================================
        // 6. PINNED QUICK LINKS NOTE CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 14.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.secondary)
                )
                Text(
                  text = "Pinned Quick Links",
                  color = textPrimary,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold
                )
                IconButton(
                  onClick = { isFavoritesEditMode = !isFavoritesEditMode },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = if (isFavoritesEditMode) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit Quick Links",
                    tint = if (isFavoritesEditMode) colors.primary else textSecondary,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }

              Text(
                text = "Show all",
                color = colors.primary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .clickable { onOpenBookmarks() }
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val defaultSpeedDials = listOf(
              SpeedDialItem(id = "ddg", title = "DuckDuckGo", url = "https://duckduckgo.com"),
              SpeedDialItem(id = "github", title = "GitHub", url = "https://github.com"),
              SpeedDialItem(id = "wikipedia", title = "Wikipedia", url = "https://wikipedia.org"),
              SpeedDialItem(id = "reddit", title = "Reddit", url = "https://reddit.com"),
              SpeedDialItem(id = "hackernews", title = "Hacker News", url = "https://news.ycombinator.com"),
              SpeedDialItem(id = "youtube", title = "YouTube", url = "https://youtube.com"),
              SpeedDialItem(id = "twitter", title = "X / Twitter", url = "https://x.com"),
              SpeedDialItem(id = "tor", title = "Check Tor", url = "https://check.torproject.org"),
              SpeedDialItem(id = "proton", title = "Proton", url = "https://proton.me"),
            )
            val effectiveSpeedDials = if (speedDials.isNotEmpty()) speedDials else defaultSpeedDials

            val chunkedFavorites = effectiveSpeedDials.chunked(5)
            chunkedFavorites.forEach { rowItems ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                rowItems.forEach { item ->
                  FavoriteTile(
                    item = item,
                    cardBg = colors.background,
                    cardBorder = cardBorder,
                    textPrimary = textPrimary,
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
                    cardBg = colors.background,
                    cardBorder = cardBorder,
                    textSecondary = textSecondary,
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
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Start
              ) {
                FavoriteAddTile(
                  cardBg = colors.background,
                  cardBorder = cardBorder,
                  textSecondary = textSecondary,
                  onClick = { showAddDialog = true },
                  modifier = Modifier.width(68.dp)
                )
              }
            }
          }
        }

        // ==========================================
        // 7. PRIVACY SUMMARY STICKY NOTE CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onSecurityShieldClick() }
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
              PictorialIconBadge(
                icon = Icons.Default.VerifiedUser,
                color = Color(0xFF2E7D5B),
                size = 40.dp,
                iconSize = 20.dp,
                cornerRadius = 12.dp
              )
              Column {
                Text(
                  text = "Privacy Shield Note",
                  color = textPrimary,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "$blockedTrackersCount trackers neutralized • Zero telemetry",
                  color = textSecondary,
                  fontSize = 11.5.sp
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF2E7D5B).copy(alpha = 0.14f)
            ) {
              Text(
                text = "ACTIVE",
                color = Color(0xFF2E7D5B),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
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
      title = { Text("Delete Favorite?", fontWeight = FontWeight.Bold, color = textPrimary) },
      text = { Text("Are you sure you want to remove \"${item.title}\" from favorites?", color = textSecondary) },
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
private fun QuickActionItem(
  icon: ImageVector? = null,
  painter: androidx.compose.ui.graphics.painter.Painter? = null,
  label: String,
  textPrimary: Color,
  accentColor: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 6.dp, vertical = 4.dp)
  ) {
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = accentColor.copy(alpha = 0.15f),
      border = BorderStroke(0.9.dp, accentColor.copy(alpha = 0.32f)),
      modifier = Modifier.size(46.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        if (painter != null) {
          Icon(
            painter = painter,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(21.dp)
          )
        } else if (icon != null) {
          Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accentColor,
            modifier = Modifier.size(21.dp)
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = label,
      color = textPrimary,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold,
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
  isEditMode: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val tileAccent = remember(item.title) { pictorialColorFor(item.title) }

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
      modifier = Modifier.size(54.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = tileAccent.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, tileAccent.copy(alpha = 0.28f)),
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(16.dp))
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          BrandFaviconIcon(
            url = item.url,
            iconKey = item.iconKey,
            title = item.title,
            size = 30.dp
          )
        }
      }

      if (isEditMode) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-4).dp)
            .size(20.dp)
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

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = item.title,
      color = textPrimary,
      fontSize = 11.sp,
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
  cardBg: Color,
  cardBorder: Color,
  textSecondary: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val accent = ThemeCyber.colors.primary
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .padding(horizontal = 3.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
  ) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = accent.copy(alpha = 0.10f),
      border = BorderStroke(1.dp, accent.copy(alpha = 0.30f)),
      modifier = Modifier
        .size(54.dp)
        .clip(RoundedCornerShape(16.dp))
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Favorite",
          tint = accent,
          modifier = Modifier.size(24.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Add Note",
      color = textSecondary,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      textAlign = TextAlign.Center
    )
  }
}

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
      shape = RoundedCornerShape(22.dp),
      color = colors.surface,
      border = BorderStroke(1.dp, colors.surfaceBorder),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp, vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PictorialIconBadge(
              icon = Icons.Default.Palette,
              color = accentColor,
              size = 34.dp,
              iconSize = 18.dp,
              cornerRadius = 10.dp
            )
            Text(
              text = "Notes Theme & Paper",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              color = colors.textPrimary
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = colors.textSecondary
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = colors.surfaceLight,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            val tabs = listOf("🎨 Note Palettes", "🖼️ Wallpaper")
            tabs.forEachIndexed { index, title ->
              val isSelected = selectedTab == index
              Surface(
                shape = RoundedCornerShape(9.dp),
                color = if (isSelected) colors.surface else Color.Transparent,
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(9.dp))
                  .clickable { selectedTab = index }
              ) {
                Box(
                  modifier = Modifier.padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) accentColor else colors.textSecondary
                  )
                }
              }
            }
          }
        }

        when (selectedTab) {
          0 -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "Select Note Color Palette:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
              )

              LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.heightIn(max = 280.dp)
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
                        .padding(horizontal = 10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .size(22.dp)
                          .clip(RoundedCornerShape(7.dp))
                          .background(theme.primaryAccent)
                      )
                      Column {
                        Text(
                          text = theme.displayName,
                          fontSize = 11.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = colors.textPrimary,
                          maxLines = 1
                        )
                        Text(
                          text = if (theme.isLight) "Warm Paper" else "Dark Slate",
                          fontSize = 10.sp,
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
                text = "Custom Gallery Photo:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
              )

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
                  Text("Choose Photo")
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
                  text = "Wallpaper Clarity: $visibilityPercent%",
                  fontSize = 12.sp,
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
                  fontSize = 12.sp,
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
          val engineColor = pictorialColorFor(engine.displayName)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) engineColor.copy(alpha = 0.14f) else colors.background,
            border = BorderStroke(
              1.dp,
              if (isSelected) engineColor else colors.surfaceBorder
            ),
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
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
                PictorialIconBadge(
                  icon = Icons.Default.Search,
                  color = engineColor,
                  size = 34.dp,
                  iconSize = 17.dp,
                  cornerRadius = 10.dp
                )
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
          text = "Add Quick Note Link",
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
