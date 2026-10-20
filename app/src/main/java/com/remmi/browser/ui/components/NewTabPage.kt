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
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable(
                role = androidx.compose.ui.semantics.Role.Button,
                onClick = { showThemeDialog = true }
              )
              .testTag("home_theme_toggle_button")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Switch Theme",
                tint = textPrimary,
                modifier = Modifier.size(19.dp)
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(12.dp))
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
                modifier = Modifier.size(19.dp)
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
          .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Default Browser Prompt Card
        if (!isDefaultBrowser && !isDefaultPromptDismissed) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 12.dp)
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.14f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp)
                  )
                }
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
                    text = "Fast, private browsing by default",
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
                  shape = RoundedCornerShape(8.dp),
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
        // 3. HERO WELCOME CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
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
                text = "Private. Fast. Yours.",
                color = textSecondary,
                fontSize = 13.sp
              )
            }

            PandaMascotArt(
              size = 80.dp,
              isDarkTheme = !isLight,
              isGhost = isGhostMode,
              accentColor = colors.primary,
              modifier = Modifier.padding(start = 8.dp)
            )
          }
        }

        // ==========================================
        // 4. SEARCH / ADDRESS BAR
        // ==========================================
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = cardBg,
          border = BorderStroke(
            width = if (isSearchFocused) 1.5.dp else 1.dp,
            color = if (isSearchFocused) colors.primary else cardBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .testTag("home_search_bar_surface")
        ) {
          Row(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IconButton(
              onClick = { showSearchEngineMenu = true },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Engine",
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
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
                  text = "Search ${searchEngine.displayName} or type URL...",
                  color = colors.textMuted,
                  fontSize = 14.sp,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(
                  color = textPrimary,
                  fontSize = 14.sp,
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
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear search",
                  tint = textSecondary,
                  modifier = Modifier.size(16.dp)
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
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.ContentPaste,
                  contentDescription = "Paste URL from Clipboard",
                  tint = textSecondary,
                  modifier = Modifier.size(17.dp)
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
                modifier = Modifier.size(30.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Voice Search",
                  tint = textSecondary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        // Live Suggestions Card
        if (searchQuery.trim().isNotEmpty() && (historySuggestions.isNotEmpty() || bookmarkSuggestions.isNotEmpty())) {
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 14.dp)
          ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
              bookmarkSuggestions.forEach { item ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
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
                    .clip(RoundedCornerShape(8.dp))
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
            shape = RoundedCornerShape(14.dp),
            color = cardBg,
            border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.45f)),
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
        // 5. QUICK TOOLS CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(18.dp),
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
              .padding(vertical = 12.dp, horizontal = 8.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 2.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Quick Tools",
                color = textPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (isGhostMode) "Ghost Profile" else "Shield Profile",
                color = pillAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceEvenly,
              verticalAlignment = Alignment.CenterVertically
            ) {
              QuickActionItem(
                icon = Icons.Default.Bookmark,
                label = "Bookmarks",
                textPrimary = textPrimary,
                accentColor = colors.primary,
                onClick = onOpenBookmarks
              )
              QuickActionItem(
                icon = Icons.Default.History,
                label = "History",
                textPrimary = textPrimary,
                accentColor = colors.primary,
                onClick = onOpenHistory
              )
              QuickActionItem(
                icon = Icons.Default.Download,
                label = "Downloads",
                textPrimary = textPrimary,
                accentColor = colors.primary,
                onClick = onOpenDownloads
              )
              QuickActionItem(
                icon = Icons.Default.MenuBook,
                label = "Reading List",
                textPrimary = textPrimary,
                accentColor = colors.primary,
                onClick = onOpenReadingList
              )
              QuickActionItem(
                painter = painterResource(R.drawable.ic_tor),
                label = "Tor Mode",
                textPrimary = textPrimary,
                accentColor = colors.torPurple,
                onClick = onToggleGhost
              )
            }
          }
        }

        // ==========================================
        // 6. PINNED QUICK LINKS CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = cardBg,
          border = BorderStroke(1.dp, cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 12.dp)
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
                Text(
                  text = "Favorites",
                  color = textPrimary,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold
                )
                IconButton(
                  onClick = { isFavoritesEditMode = !isFavoritesEditMode },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = if (isFavoritesEditMode) Icons.Default.Check else Icons.Default.Edit,
                    contentDescription = "Edit Favorites",
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
                    cardBg = colors.surface,
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
                    cardBg = colors.surface,
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
                  cardBg = colors.surface,
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
        // 7. PRIVACY SUMMARY CARD
        // ==========================================
        Surface(
          shape = RoundedCornerShape(16.dp),
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
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(colors.secondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.VerifiedUser,
                  contentDescription = null,
                  tint = colors.secondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Column {
                Text(
                  text = "Privacy Shield",
                  color = textPrimary,
                  fontSize = 13.5.sp,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "$blockedTrackersCount trackers blocked • Zero telemetry",
                  color = textSecondary,
                  fontSize = 11.5.sp
                )
              }
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = colors.secondary.copy(alpha = 0.14f)
            ) {
              Text(
                text = "ACTIVE",
                color = colors.secondary,
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
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .padding(horizontal = 4.dp)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick
      )
  ) {
    Box(
      modifier = Modifier.size(50.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        modifier = Modifier
          .fillMaxSize()
          .clip(RoundedCornerShape(14.dp))
      ) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          BrandFaviconIcon(
            url = item.url,
            iconKey = item.iconKey,
            title = item.title,
            size = 28.dp
          )
        }
      }

      if (isEditMode) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 4.dp, y = (-4).dp)
            .size(18.dp)
            .clip(CircleShape)
            .background(Color(0xFFDC2626))
            .clickable(onClick = onDelete),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.size(11.dp)
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
      .padding(horizontal = 4.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
  ) {
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = cardBg,
      border = BorderStroke(1.dp, cardBorder),
      modifier = Modifier
        .size(50.dp)
        .clip(RoundedCornerShape(14.dp))
    ) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = "Add Favorite",
          tint = accent,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
      text = "Add",
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
      shape = RoundedCornerShape(20.dp),
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
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
              )
            }
            Text(
              text = "Theme & Wallpaper",
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
            val tabs = listOf("Color Palettes", "Wallpaper")
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
                text = "Select Theme Palette:",
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
                    shape = RoundedCornerShape(12.dp),
                    color = colors.background,
                    border = BorderStroke(1.2.dp, theme.primaryAccent),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(60.dp)
                      .clip(RoundedCornerShape(12.dp))
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
                          .size(20.dp)
                          .clip(RoundedCornerShape(6.dp))
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
                          text = if (theme.isLight) "Light" else "Dark",
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
      shape = RoundedCornerShape(18.dp),
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
      shape = RoundedCornerShape(18.dp),
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
          text = "Add to Favorites",
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
