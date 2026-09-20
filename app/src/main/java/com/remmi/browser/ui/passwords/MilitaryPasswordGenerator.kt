package com.remmi.browser.ui.passwords

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.remmi.browser.security.ClipboardManager
import com.remmi.browser.ui.theme.ThemeCyber
import java.security.SecureRandom
import kotlin.math.roundToInt

data class GeneratorSettings(
  val length: Int = 20,
  val includeUppercase: Boolean = true,
  val includeLowercase: Boolean = true,
  val includeDigits: Boolean = true,
  val includeSymbols: Boolean = true,
  val excludeAmbiguous: Boolean = true,
  val isPassphraseMode: Boolean = false,
  val wordCount: Int = 4,
)

object MilitaryPasswordEngine {
  private val RNG = SecureRandom()

  private val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
  private val UPPER_AMBIGUOUS = "IO"
  private val LOWER = "abcdefghijkmnopqrstuvwxyz"
  private val LOWER_AMBIGUOUS = "l"
  private val DIGITS = "23456789"
  private val DIGITS_AMBIGUOUS = "01"
  private val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

  /**
   * Passphrase word list (BIP-39 English subset).
   *
   * Contains exactly 1,759 unique entries, so a 4-word passphrase yields
   * log2(1759^4) ~= 43.1 bits of selection entropy and a 5-word passphrase
   * ~= 53.9 bits. Do not describe this as "2048-word BIP-39" — the list is a
   * subset, not the full standard set.
   */
  private val WORDS = listOf(
    "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract",
    "absurd", "abuse", "access", "accident", "account", "accuse", "achieve", "acid",
    "acoustic", "acquire", "across", "action", "actor", "actual", "adapt", "address",
    "adjust", "admit", "adult", "advance", "advice", "aerobic", "affair", "afford",
    "afraid", "again", "agent", "agree", "ahead", "airport", "aisle", "alarm",
    "album", "alcohol", "alert", "alien", "alley", "allow", "almost", "alone",
    "alpha", "already", "also", "alter", "always", "amateur", "amazing", "among",
    "amount", "amused", "anchor", "ancient", "angel", "anger", "angle", "angry",
    "animal", "ankle", "annual", "answer", "antenna", "antique", "anxiety", "apart",
    "apology", "appear", "apple", "approve", "april", "arctic", "arena", "argue",
    "armor", "army", "arrange", "arrest", "arrive", "arrow", "artist", "artwork",
    "aspect", "assault", "asset", "assist", "assume", "asthma", "athlete", "atom",
    "attack", "attend", "auction", "audit", "august", "aunt", "author", "autumn",
    "average", "avocado", "avoid", "awake", "aware", "awesome", "awful", "awkward",
    "axis", "baby", "bachelor", "bacon", "badge", "balance", "balcony", "bamboo",
    "banana", "banner", "bargain", "barrel", "basic", "basket", "battle", "beach",
    "beauty", "because", "become", "bedroom", "before", "begin", "behave", "behind",
    "believe", "below", "bench", "benefit", "between", "beyond", "bicycle", "bitter",
    "blade", "blame", "blanket", "blast", "bleak", "bless", "blind", "blood",
    "blossom", "blue", "board", "boat", "body", "bomb", "bone", "bonus",
    "border", "boring", "borrow", "bottom", "bounce", "bracket", "brain", "brand",
    "brave", "bread", "breeze", "brick", "bridge", "brief", "bright", "bring",
    "brisk", "broken", "bronze", "brother", "brown", "brush", "bubble", "budget",
    "buffalo", "build", "bullet", "bundle", "burden", "burger", "burst", "butter",
    "buyer", "cabin", "cable", "cactus", "cage", "calcium", "camera", "camp",
    "canal", "cancel", "candy", "cannon", "canvas", "canyon", "capable", "capital",
    "captain", "carbon", "cargo", "carpet", "carry", "castle", "casual", "catalog",
    "catch", "cattle", "caught", "cause", "caution", "cave", "ceiling", "celery",
    "cement", "census", "central", "cereal", "certain", "chair", "chalk", "champion",
    "change", "chapter", "charge", "charity", "chase", "cheap", "check", "cheese",
    "cherry", "chicken", "chief", "choice", "choose", "chunk", "cinnamon", "circle",
    "citizen", "city", "civil", "claim", "clarify", "claw", "clean", "clerk",
    "clever", "click", "client", "climb", "clinic", "clock", "close", "cloud",
    "clown", "cluster", "clutch", "coach", "coconut", "coffee", "collect", "color",
    "column", "combine", "comfort", "comic", "common", "company", "concert", "conduct",
    "confirm", "congress", "connect", "consider", "control", "convince", "coral", "corn",
    "correct", "couch", "country", "couple", "course", "cousin", "cover", "coyote",
    "cradle", "craft", "crane", "crash", "crater", "crawl", "crazy", "cream",
    "credit", "creek", "crew", "cricket", "crime", "crisp", "critic", "crop",
    "cross", "crouch", "crowd", "crucial", "cruel", "cruise", "crumble", "crush",
    "crystal", "cube", "culture", "curtain", "curve", "cushion", "custom", "cycle",
    "damage", "damp", "danger", "daring", "dash", "daughter", "dawn", "debate",
    "debris", "decade", "december", "decide", "decline", "decorate", "decrease", "deer",
    "defense", "define", "degree", "delay", "deliver", "demand", "denial", "dentist",
    "depart", "depend", "deposit", "depth", "derive", "describe", "desert", "design",
    "desk", "despair", "destroy", "detail", "detect", "develop", "device", "devote",
    "diagram", "diamond", "diary", "diesel", "differ", "digital", "dignity", "dilemma",
    "dinner", "dinosaur", "direct", "discover", "disease", "dish", "dismiss", "display",
    "distance", "divert", "dizzy", "doctor", "document", "dolphin", "domain", "donate",
    "donkey", "donor", "door", "double", "dragon", "drama", "drastic", "dream",
    "dress", "drift", "drill", "drink", "drip", "drive", "drop", "drum",
    "dry", "duck", "dumb", "dune", "during", "dust", "duty", "dwarf",
    "dynamic", "eager", "eagle", "early", "earn", "earth", "easily", "east",
    "easy", "echo", "ecology", "economy", "edge", "edit", "educate", "effort",
    "eight", "either", "elbow", "elder", "electric", "elegant", "element", "elephant",
    "elevator", "elite", "else", "embark", "embody", "embrace", "emerge", "emotion",
    "employ", "empower", "empty", "enable", "enact", "endless", "endorse", "enemy",
    "energy", "enforce", "engage", "engine", "enhance", "enjoy", "enlist", "enough",
    "enrich", "enroll", "ensure", "entire", "entry", "envelope", "episode", "equal",
    "equip", "erode", "erosion", "error", "escape", "essay", "essence", "estate",
    "eternal", "evidence", "evil", "evolve", "exact", "example", "excess", "exchange",
    "excite", "exclude", "excuse", "execute", "exercise", "exhaust", "exhibit", "exile",
    "exist", "expand", "expect", "expire", "explain", "expose", "express", "extend",
    "extra", "eyebrow", "fabric", "face", "faculty", "faint", "faith", "false",
    "family", "famous", "fancy", "fantasy", "fashion", "fatal", "father", "fatigue",
    "fault", "favorite", "feature", "february", "federal", "feel", "female", "fence",
    "festival", "fetch", "fever", "field", "figure", "file", "final", "find",
    "finger", "finish", "fire", "first", "fiscal", "fitness", "flag", "flame",
    "flash", "flavor", "flight", "flip", "float", "flock", "floor", "flower",
    "fluid", "flush", "foam", "focus", "follow", "food", "force", "forest",
    "forget", "fork", "fortune", "forum", "forward", "fossil", "foster", "found",
    "fox", "fragile", "frame", "frequent", "fresh", "friend", "fringe", "frog",
    "front", "frost", "frozen", "fruit", "fuel", "fun", "funny", "furnace",
    "fury", "future", "gadget", "galaxy", "gallery", "game", "garage", "garbage",
    "garden", "garlic", "gather", "gauge", "gaze", "general", "genius", "genre",
    "gentle", "genuine", "gesture", "ghost", "giant", "gift", "giggle", "ginger",
    "giraffe", "glad", "glance", "glare", "glass", "glide", "glimpse", "globe",
    "gloom", "glory", "glove", "glow", "glue", "goat", "goddess", "gold",
    "good", "goose", "gorilla", "gospel", "govern", "gown", "grace", "grain",
    "grant", "grape", "grass", "gravity", "great", "green", "grid", "grief",
    "grit", "grocery", "group", "grow", "guard", "guess", "guide", "guilt",
    "guitar", "gun", "gym", "habit", "hair", "hammer", "hamster", "hand",
    "happy", "harbor", "harsh", "harvest", "hawk", "hazard", "health", "heart",
    "heavy", "hedgehog", "height", "hello", "helmet", "help", "hero", "hidden",
    "highway", "hill", "hint", "history", "hobby", "hockey", "hold", "holiday",
    "hollow", "home", "honey", "hood", "hope", "horn", "horror", "horse",
    "hospital", "host", "hotel", "hour", "hover", "hub", "huge", "human",
    "humble", "humor", "hundred", "hungry", "hunt", "hurdle", "hurry", "husband",
    "hybrid", "ice", "icon", "identify", "idle", "ignore", "illness", "image",
    "imitate", "immense", "immune", "impact", "impose", "improve", "impulse", "include",
    "income", "increase", "index", "indicate", "indoor", "industry", "infant", "inflict",
    "inform", "initial", "inject", "inner", "innocent", "input", "inquiry", "insane",
    "insect", "inside", "inspire", "install", "intact", "interest", "invest", "invite",
    "involve", "iron", "island", "isolate", "issue", "item", "ivory", "jacket",
    "jaguar", "january", "jazz", "jealous", "jelly", "jewel", "join", "joke",
    "journey", "judge", "juice", "jump", "jungle", "junior", "junk", "jury",
    "justice", "kangaroo", "keen", "keep", "kernel", "key", "kick", "kidney",
    "kind", "kingdom", "kiss", "kitchen", "kite", "kitten", "kiwi", "knee",
    "knife", "knock", "know", "labor", "ladder", "lady", "lake", "lamp",
    "language", "laptop", "large", "later", "latin", "laugh", "laundry", "lava",
    "lawn", "lawsuit", "layer", "lazy", "leader", "leaf", "learn", "leave",
    "lecture", "left", "legend", "leisure", "lemon", "length", "lens", "leopard",
    "lesson", "letter", "level", "liberty", "library", "license", "life", "lift",
    "light", "like", "limb", "limit", "link", "lion", "liquid", "list",
    "little", "live", "lizard", "load", "loan", "lobster", "local", "lock",
    "logic", "lonely", "long", "loop", "lottery", "loud", "lounge", "loyal",
    "lucky", "luggage", "lumber", "lunar", "lunch", "luxury", "lyrics", "machine",
    "mad", "magic", "magnet", "major", "make", "mammal", "manage", "mandate",
    "mango", "mansion", "manual", "maple", "marble", "march", "margin", "marine",
    "market", "marriage", "mask", "mass", "master", "match", "material", "math",
    "matrix", "matter", "maximum", "maze", "meadow", "mean", "measure", "media",
    "melody", "melt", "member", "memory", "mention", "menu", "mercy", "merge",
    "merit", "message", "metal", "method", "middle", "midnight", "million", "mimic",
    "mind", "minimum", "minor", "minute", "miracle", "mirror", "misery", "miss",
    "mistake", "mix", "mixed", "mixture", "mobile", "model", "modify", "moment",
    "monitor", "monkey", "monster", "month", "moral", "morning", "mosquito", "mother",
    "motion", "mountain", "mouse", "move", "movie", "much", "muffin", "multiply",
    "muscle", "museum", "mushroom", "music", "must", "mutual", "myself", "mystery",
    "myth", "naive", "name", "napkin", "narrow", "nasty", "nation", "nature",
    "near", "neck", "need", "negative", "neglect", "neither", "nephew", "nerve",
    "nest", "network", "neutral", "never", "night", "noble", "noise", "nominee",
    "normal", "north", "notable", "nothing", "notice", "novel", "nuclear", "number",
    "nurse", "nut", "oak", "obey", "object", "oblige", "obscure", "observe",
    "obtain", "obvious", "occur", "ocean", "october", "odor", "off", "offer",
    "office", "often", "oil", "olive", "olympic", "omit", "once", "onion",
    "online", "open", "opera", "opinion", "oppose", "option", "orange", "orbit",
    "orchard", "order", "ordinary", "organ", "orient", "orphan", "ostrich", "other",
    "outdoor", "outer", "output", "outside", "oval", "oven", "over", "owner",
    "oxygen", "oyster", "ozone", "pact", "paddle", "page", "pair", "palace",
    "palm", "panda", "panel", "panic", "panther", "paper", "parade", "parent",
    "park", "parrot", "party", "patch", "path", "patient", "patrol", "pattern",
    "pause", "pave", "payment", "peace", "peanut", "pear", "peasant", "pelican",
    "penalty", "pencil", "people", "pepper", "perfect", "permit", "person", "pet",
    "phone", "photo", "phrase", "physical", "piano", "picnic", "picture", "piece",
    "pilot", "pink", "pioneer", "pipe", "pistol", "pitch", "pizza", "place",
    "planet", "plastic", "plate", "play", "please", "pledge", "pluck", "plug",
    "plunge", "poem", "poet", "point", "polar", "pole", "police", "pond",
    "pony", "pool", "popular", "portion", "position", "possible", "potato", "pottery",
    "poverty", "powder", "power", "practice", "praise", "predict", "prefer", "prepare",
    "present", "pretty", "prevent", "price", "pride", "primary", "print", "priority",
    "prison", "private", "prize", "problem", "process", "produce", "profit", "program",
    "project", "promote", "proof", "property", "prosper", "protect", "proud", "provide",
    "public", "pudding", "pull", "pulp", "pulse", "pumpkin", "punch", "pupil",
    "puppy", "purchase", "purity", "purpose", "purse", "push", "puzzle", "pyramid",
    "quality", "quantum", "quarter", "question", "quick", "quit", "quiz", "quote",
    "rabbit", "raccoon", "race", "rack", "radar", "radio", "rail", "rain",
    "raise", "rally", "ramp", "ranch", "random", "range", "rapid", "rare",
    "raven", "razor", "ready", "real", "reason", "rebel", "rebuild", "recall",
    "receive", "recipe", "record", "recycle", "reduce", "reflect", "reform", "region",
    "regret", "regular", "reject", "relax", "release", "relief", "rely", "remain",
    "remember", "remind", "remove", "render", "renew", "rent", "reopen", "repair",
    "repeat", "replace", "report", "require", "rescue", "resemble", "resist", "resource",
    "response", "result", "retire", "retreat", "return", "reunion", "reveal", "review",
    "reward", "rhythm", "rich", "ride", "rifle", "right", "rigid", "ring",
    "riot", "ripple", "risk", "ritual", "rival", "river", "road", "roast",
    "robot", "robust", "rocket", "romance", "roof", "rookie", "room", "rose",
    "rotate", "rough", "round", "route", "royal", "rubber", "rude", "rugby",
    "ruins", "rule", "runway", "rural", "saddle", "sadness", "safe", "sail",
    "salad", "salmon", "salon", "salt", "salute", "same", "sample", "sand",
    "satisfy", "satoshi", "sauce", "sausage", "save", "scale", "scan", "scatter",
    "scene", "scheme", "school", "science", "scissors", "scorpion", "scout", "screen",
    "script", "sea", "search", "season", "seat", "second", "secret", "section",
    "security", "seed", "segment", "select", "sell", "senior", "sense", "sentence",
    "series", "service", "session", "settle", "setup", "seven", "shadow", "shaft",
    "shallow", "share", "shed", "shell", "sheriff", "shield", "shift", "shine",
    "ship", "shiver", "shock", "shoe", "shoot", "shop", "short", "shoulder",
    "shove", "shrimp", "shrug", "shuffle", "shy", "sibling", "sick", "side",
    "siege", "sight", "sign", "silent", "silk", "silly", "silver", "similar",
    "simple", "since", "siren", "sister", "situate", "size", "skate", "sketch",
    "ski", "skill", "skin", "skirt", "skull", "slab", "slam", "sleep",
    "slender", "slice", "slide", "slight", "slim", "slogan", "slow", "slush",
    "small", "smart", "smile", "smoke", "smooth", "snack", "snake", "snap",
    "sniff", "snow", "soap", "soccer", "social", "sock", "soda", "soft",
    "solar", "soldier", "solid", "solution", "solve", "someone", "song", "soon",
    "sorry", "sort", "soul", "sound", "soup", "source", "south", "space",
    "spare", "spatial", "spawn", "speak", "special", "speed", "spell", "spend",
    "sphere", "spice", "spider", "spike", "spin", "spirit", "split", "sponsor",
    "spoon", "sport", "spray", "spread", "spring", "spy", "square", "squeeze",
    "squirrel", "stable", "stadium", "staff", "stage", "stairs", "stamp", "stand",
    "start", "state", "stay", "steak", "steel", "stem", "step", "stereo",
    "stick", "still", "sting", "stock", "stomach", "stone", "stool", "story",
    "stove", "strategy", "street", "strike", "strong", "struggle", "student", "stuff",
    "stumble", "style", "subject", "submit", "subway", "success", "such", "sudden",
    "suffer", "sugar", "suggest", "suit", "summer", "sun", "sunny", "sunset",
    "super", "supply", "supreme", "sure", "surface", "surge", "surprise", "surround",
    "survey", "suspect", "sustain", "swallow", "swamp", "swap", "swarm", "swear",
    "sweet", "swim", "swing", "switch", "sword", "symbol", "symptom", "syrup",
    "system", "table", "tackle", "talent", "talk", "tank", "tape", "target",
    "task", "taste", "tattoo", "taxi", "teach", "team", "tenant", "tennis",
    "tent", "term", "test", "text", "thank", "that", "theme", "theory",
    "there", "they", "thing", "this", "thought", "three", "thrive", "throw",
    "thumb", "thunder", "ticket", "tide", "tiger", "tilt", "timber", "time",
    "tiny", "tired", "tissue", "title", "toast", "tobacco", "today", "toddler",
    "together", "toilet", "token", "tomato", "tomorrow", "tone", "tongue", "tonight",
    "tool", "tooth", "topic", "topple", "torch", "tornado", "tortoise", "total",
    "tourist", "toward", "tower", "town", "toy", "track", "trade", "traffic",
    "tragic", "train", "transfer", "trap", "trash", "travel", "tray", "treat",
    "tree", "trend", "trial", "tribe", "trick", "trigger", "trim", "trip",
    "trophy", "trouble", "truck", "truly", "trumpet", "trust", "truth", "try",
    "tube", "tuna", "tunnel", "turkey", "turn", "turtle", "twelve", "twenty",
    "twice", "twin", "twist", "type", "typical", "ugly", "umbrella", "unable",
    "unaware", "uncle", "uncover", "under", "undo", "unfair", "unfold", "unhappy",
    "uniform", "unique", "unit", "universe", "unknown", "unlock", "until", "unusual",
    "unveil", "update", "upgrade", "uphold", "upon", "upper", "upset", "urban",
    "usage", "useful", "useless", "usual", "utility", "vacant", "vacuum", "vague",
    "valid", "valley", "valve", "vanish", "vapor", "various", "vast", "vault",
    "vehicle", "velvet", "vendor", "venture", "venue", "verb", "verify", "version",
    "very", "vessel", "veteran", "viable", "vibrant", "vicious", "victory", "video",
    "view", "village", "vintage", "violin", "virtual", "virus", "visa", "visit",
    "visual", "vital", "vivid", "vocal", "voice", "void", "volcano", "volume",
    "vote", "voyage", "wage", "wagon", "wait", "walk", "wall", "walnut",
    "wander", "warfare", "warm", "warrior", "wash", "wasp", "waste", "water",
    "wave", "way", "wealth", "weapon", "wear", "weasel", "weather", "web",
    "wedding", "weekend", "weird", "welcome", "west", "wet", "whale", "wheat",
    "wheel", "when", "where", "whip", "whisper", "wide", "width", "wife",
    "wild", "will", "win", "window", "wine", "wing", "wink", "winner",
    "winter", "wire", "wisdom", "wise", "wish", "witness", "wolf", "woman",
    "wonder", "wood", "wool", "word", "work", "world", "worry", "worth",
    "wrap", "wreck", "wrestle", "wrist", "write", "wrong", "yard", "year",
    "yellow", "young", "youth", "zebra", "zero", "zone", "zoo"
  )

  fun generate(settings: GeneratorSettings): String {
    if (settings.isPassphraseMode) {
      val selectedWords = (0 until settings.wordCount).map {
        WORDS[RNG.nextInt(WORDS.size)]
      }
      val separator = if (settings.includeSymbols) {
        val syms = listOf("-", "_", ".", "#", "$")
        syms[RNG.nextInt(syms.size)]
      } else "-"
      val joined = selectedWords.joinToString(separator)
      val numSuffix = if (settings.includeDigits) RNG.nextInt(900) + 100 else ""
      return if (settings.includeUppercase) {
        joined.split(separator).joinToString(separator) { it.replaceFirstChar { c -> c.uppercase() } } + numSuffix
      } else {
        joined + numSuffix
      }
    }

    var pool = StringBuilder()
    if (settings.includeUppercase) pool.append(UPPER + if (!settings.excludeAmbiguous) UPPER_AMBIGUOUS else "")
    if (settings.includeLowercase) pool.append(LOWER + if (!settings.excludeAmbiguous) LOWER_AMBIGUOUS else "")
    if (settings.includeDigits) pool.append(DIGITS + if (!settings.excludeAmbiguous) DIGITS_AMBIGUOUS else "")
    if (settings.includeSymbols) pool.append(SYMBOLS)

    val charPool = pool.toString()
    if (charPool.isEmpty()) return "RemmiVault#2026!"

    val chars = CharArray(settings.length)
    for (i in 0 until settings.length) {
      chars[i] = charPool[RNG.nextInt(charPool.length)]
    }

    // Guarantee at least one character from each enabled category
    val mandatoryChars = mutableListOf<Char>()
    val upperPool = UPPER + if (!settings.excludeAmbiguous) UPPER_AMBIGUOUS else ""
    val lowerPool = LOWER + if (!settings.excludeAmbiguous) LOWER_AMBIGUOUS else ""
    val digitPool = DIGITS + if (!settings.excludeAmbiguous) DIGITS_AMBIGUOUS else ""
    if (settings.includeUppercase && upperPool.isNotEmpty()) mandatoryChars.add(upperPool[RNG.nextInt(upperPool.length)])
    if (settings.includeLowercase && lowerPool.isNotEmpty()) mandatoryChars.add(lowerPool[RNG.nextInt(lowerPool.length)])
    if (settings.includeDigits && digitPool.isNotEmpty()) mandatoryChars.add(digitPool[RNG.nextInt(digitPool.length)])
    if (settings.includeSymbols && SYMBOLS.isNotEmpty()) mandatoryChars.add(SYMBOLS[RNG.nextInt(SYMBOLS.length)])

    // Place mandatory chars at random positions
    val positions = (0 until settings.length).toMutableList()
    for (mandatoryChar in mandatoryChars) {
      if (positions.isEmpty()) break
      val posIndex = RNG.nextInt(positions.size)
      chars[positions.removeAt(posIndex)] = mandatoryChar
    }

    // Fisher-Yates shuffle to prevent positional bias
    for (i in chars.size - 1 downTo 1) {
      val j = RNG.nextInt(i + 1)
      val tmp = chars[i]
      chars[i] = chars[j]
      chars[j] = tmp
    }

    return String(chars)
  }
}

@Composable
fun MilitaryPasswordGeneratorDialog(
  onDismiss: () -> Unit,
  onUsePassword: ((String) -> Unit)? = null,
) {
  val context = LocalContext.current
  val clipboard = remember { ClipboardManager(context) }

  var settings by remember { mutableStateOf(GeneratorSettings()) }
  var currentPassword by remember { mutableStateOf(MilitaryPasswordEngine.generate(settings)) }

  val entropy = remember(currentPassword) { PasswordManagerUtils.calculateEntropyBits(currentPassword) }
  val strength = remember(currentPassword) { PasswordManagerUtils.evaluateStrength(currentPassword) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ThemeCyber.colors.surface),
      border = BorderStroke(1.5.dp, ThemeCyber.colors.primary.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp)
        .testTag("dialog_military_generator")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(20.dp)
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(ThemeCyber.colors.primary.copy(alpha = 0.15f))
          ) {
            Icon(
              imageVector = Icons.Default.Security,
              contentDescription = null,
              tint = ThemeCyber.colors.primary,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Military Password Engine",
              color = ThemeCyber.colors.textPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp,
              fontFamily = ThemeCyber.fontFamily
            )
            Text(
              text = "CSPRNG Quantum-Resistant Entropy",
              color = ThemeCyber.colors.primary,
              fontSize = 11.sp,
              fontFamily = ThemeCyber.fontFamily
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = ThemeCyber.colors.textSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generated Password Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = ThemeCyber.colors.surfaceLight),
          border = BorderStroke(1.dp, strength.color.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = currentPassword,
                color = ThemeCyber.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = if (currentPassword.length > 28) 13.sp else 16.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
              )
              IconButton(
                onClick = {
                  currentPassword = MilitaryPasswordEngine.generate(settings)
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Regenerate",
                  tint = ThemeCyber.colors.primary
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Strength bar
            LinearProgressIndicator(
              progress = { strength.scoreFraction },
              color = strength.color,
              trackColor = ThemeCyber.colors.surfaceBorder,
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = strength.label,
                color = strength.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = ThemeCyber.fontFamily
              )
              Text(
                text = "$entropy bits of entropy",
                color = ThemeCyber.colors.textSecondary,
                fontSize = 12.sp,
                fontFamily = ThemeCyber.fontFamily
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Controls
        if (!settings.isPassphraseMode) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Length: ${settings.length} characters",
              color = ThemeCyber.colors.textPrimary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              fontFamily = ThemeCyber.fontFamily
            )
            Text(
              text = if (settings.length >= 24) "Military (24+)" else if (settings.length >= 16) "Strong" else "Standard",
              color = if (settings.length >= 24) ThemeCyber.colors.primary else ThemeCyber.colors.textSecondary,
              fontSize = 12.sp,
              fontFamily = ThemeCyber.fontFamily
            )
          }

          Slider(
            value = settings.length.toFloat(),
            onValueChange = {
              settings = settings.copy(length = it.roundToInt())
              currentPassword = MilitaryPasswordEngine.generate(settings)
            },
            valueRange = 8f..64f,
            steps = 55,
            colors = SliderDefaults.colors(
              thumbColor = ThemeCyber.colors.primary,
              activeTrackColor = ThemeCyber.colors.primary,
              inactiveTrackColor = ThemeCyber.colors.surfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )
        } else {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Words: ${settings.wordCount}",
              color = ThemeCyber.colors.textPrimary,
              fontSize = 13.sp,
              fontFamily = ThemeCyber.fontFamily
            )
          }
          Slider(
            value = settings.wordCount.toFloat(),
            onValueChange = {
              settings = settings.copy(wordCount = it.roundToInt())
              currentPassword = MilitaryPasswordEngine.generate(settings)
            },
            valueRange = 3f..8f,
            steps = 4,
            colors = SliderDefaults.colors(
              thumbColor = ThemeCyber.colors.primary,
              activeTrackColor = ThemeCyber.colors.primary,
              inactiveTrackColor = ThemeCyber.colors.surfaceBorder
            ),
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toggles
        GeneratorToggleRow("Passphrase Mode (Diceware)", settings.isPassphraseMode) {
          settings = settings.copy(isPassphraseMode = it)
          currentPassword = MilitaryPasswordEngine.generate(settings)
        }
        if (!settings.isPassphraseMode) {
          GeneratorToggleRow("Uppercase (A-Z)", settings.includeUppercase) {
            settings = settings.copy(includeUppercase = it)
            currentPassword = MilitaryPasswordEngine.generate(settings)
          }
          GeneratorToggleRow("Lowercase (a-z)", settings.includeLowercase) {
            settings = settings.copy(includeLowercase = it)
            currentPassword = MilitaryPasswordEngine.generate(settings)
          }
          GeneratorToggleRow("Numbers (0-9)", settings.includeDigits) {
            settings = settings.copy(includeDigits = it)
            currentPassword = MilitaryPasswordEngine.generate(settings)
          }
          GeneratorToggleRow("Special Characters (!@#$)", settings.includeSymbols) {
            settings = settings.copy(includeSymbols = it)
            currentPassword = MilitaryPasswordEngine.generate(settings)
          }
          GeneratorToggleRow("Exclude Ambiguous (1, l, 0, O)", settings.excludeAmbiguous) {
            settings = settings.copy(excludeAmbiguous = it)
            currentPassword = MilitaryPasswordEngine.generate(settings)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = {
              clipboard.copyWithAutoClear(currentPassword, label = "Generated Password", clearAfterMs = 30000)
              Toast.makeText(context, "Copied! Auto-clears in 30s.", Toast.LENGTH_SHORT).show()
            },
            border = BorderStroke(1.dp, ThemeCyber.colors.primary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
            modifier = Modifier.weight(if (onUsePassword != null) 0.8f else 1f)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = null,
              tint = ThemeCyber.colors.primary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Copy",
              color = ThemeCyber.colors.primary,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              fontFamily = ThemeCyber.fontFamily,
              maxLines = 1,
              softWrap = false
            )
          }

          if (onUsePassword != null) {
            Button(
              onClick = {
                onUsePassword(currentPassword)
                onDismiss()
              },
              colors = ButtonDefaults.buttonColors(containerColor = ThemeCyber.colors.primary),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
              modifier = Modifier.weight(1.2f)
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Use Password",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = ThemeCyber.fontFamily,
                maxLines = 1,
                softWrap = false
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun GeneratorToggleRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
  ) {
    Text(
      text = label,
      color = ThemeCyber.colors.textPrimary,
      fontSize = 12.5.sp,
      fontFamily = ThemeCyber.fontFamily
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = ThemeCyber.colors.primary,
        checkedTrackColor = ThemeCyber.colors.primary.copy(alpha = 0.3f),
        uncheckedThumbColor = ThemeCyber.colors.textSecondary,
        uncheckedTrackColor = ThemeCyber.colors.surfaceLight,
      ),
      modifier = Modifier.size(36.dp, 24.dp)
    )
  }
}
