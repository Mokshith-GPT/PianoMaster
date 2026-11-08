package com.pianomaster.app

import android.Manifest
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import com.pianomaster.app.audio.MicrophonePitchListener
import com.pianomaster.app.domain.LessonCatalog
import com.pianomaster.app.domain.PianoNote
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PianoMasterApp() }
    }
}

private enum class Destination(val label: String) { Home("Home"), Learn("Learn"), Piano("Piano"), Practice("Practice"), Songs("Songs"), Tutor("Tutor"), Progress("Progress") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PianoMasterApp() {
    var selected by remember { mutableStateOf(Destination.Home) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("piano_master", Context.MODE_PRIVATE) }
    var completedLessons by remember { mutableIntStateOf(prefs.getInt("completed_lessons", 0)) }
    var practiceSeconds by remember { mutableIntStateOf(prefs.getInt("practice_seconds", 0)) }
    var notesCorrect by remember { mutableIntStateOf(prefs.getInt("notes_correct", 0)) }
    var notesTotal by remember { mutableIntStateOf(prefs.getInt("notes_total", 0)) }
    var showAudioInfo by remember { mutableStateOf(false) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFF9DBEFF), secondary = Color(0xFF80CBC4), background = Color(0xFF10141C), surface = Color(0xFF1A2230))) {
        Scaffold(
            topBar = { TopAppBar(title = { Column { Text("Piano Master", fontWeight = FontWeight.Bold); Text("Your personal piano academy", style = MaterialTheme.typography.labelSmall) } }, actions = { IconButton(onClick = { showAudioInfo = true }) { Icon(Icons.Default.Info, "Audio information") } }) },
            bottomBar = { NavigationBar { Destination.values().forEach { d ->
                val icon = when (d) { Destination.Home -> Icons.Default.Home; Destination.Learn -> Icons.Default.School; Destination.Piano -> Icons.Default.MusicNote; Destination.Practice -> Icons.Default.Mic; Destination.Songs -> Icons.Default.LibraryBooks; Destination.Tutor -> Icons.Default.Lightbulb; Destination.Progress -> Icons.Default.BarChart }
                NavigationBarItem(selected == d, onClick = { selected = d }, icon = { Icon(icon, d.label) }, label = { Text(d.label) })
            } } }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (selected) {
                    Destination.Home -> HomeScreen(completedLessons, practiceSeconds, onLearn = { selected = Destination.Learn }, onPiano = { selected = Destination.Piano }, onPractice = { selected = Destination.Practice })
                    Destination.Learn -> LearnScreen(onComplete = { completedLessons++; prefs.edit().putInt("completed_lessons", completedLessons).apply() })
                    Destination.Piano -> PianoScreen()
                    Destination.Practice -> PracticeScreen(onResult = { correct -> notesTotal++; if (correct) notesCorrect++; prefs.edit().putInt("notes_total", notesTotal).putInt("notes_correct", notesCorrect).apply() }, onPracticeMinute = { practiceSeconds += 60; prefs.edit().putInt("practice_seconds", practiceSeconds).apply() })
                    Destination.Songs -> SongsScreen()
                    Destination.Tutor -> TutorScreen()
                    Destination.Progress -> ProgressScreen(completedLessons, practiceSeconds, notesCorrect, notesTotal)
                }
            }
        }
        if (showAudioInfo) AlertDialog(onDismissRequest = { showAudioInfo = false }, title = { Text("Audio listening mode") }, text = { Text("Your keyboard currently connects by audio output only. Piano Master listens through your phone microphone and estimates one dominant note at a time. It cannot reliably identify exact key presses or complex chords from audio. Real-time exact note and timing feedback requires compatible USB or Bluetooth MIDI input.") }, confirmButton = { TextButton(onClick = { showAudioInfo = false }) { Text("Understood") } })
    }
}

@Composable
private fun HomeScreen(completed: Int, seconds: Int, onLearn: () -> Unit, onPiano: () -> Unit, onPractice: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Welcome to your piano studio", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("A practical learning path for your 37-key keyboard.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Column(Modifier.padding(18.dp)) { Text("YOUR NEXT LESSON", style = MaterialTheme.typography.labelMedium); Text("Lesson 1 · Find the C notes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("Locate C and build your first five-note pattern."); Button(onClick = onLearn, modifier = Modifier.padding(top = 10.dp)) { Text("Continue learning") } } } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { MetricCard("Lessons completed", completed.toString(), Modifier.weight(1f)); MetricCard("Practice time", "${seconds / 60} min", Modifier.weight(1f)) } }
        item { ActionCard("37-key note map", "Explore all 37 notes and octaves", Icons.Default.MusicNote, onPiano) }
        item { ActionCard("Microphone practice", "Listen to notes played on your keyboard", Icons.Default.Mic, onPractice) }
        item { Text("Learning path", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { LearningRow("01", "Find the C notes", "Identify groups of two black keys", true) }
        item { LearningRow("02", "Five-finger position", "Play C-D-E-F-G with fingers 1–5", false) }
        item { LearningRow("03", "Your first melody", "Learn a simple melody", false) }
        item { LearningRow("04", "Basic chords", "Build C, F and G major", false) }
    }
}

@Composable private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) { Card(modifier) { Column(Modifier.padding(14.dp)) { Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) } } }
@Composable private fun LearningRow(number: String, title: String, subtitle: String, active: Boolean) { Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(number, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.SemiBold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (active) Icon(Icons.Default.PlayCircle, null, tint = MaterialTheme.colorScheme.primary) else Icon(Icons.Default.LockOpen, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) } }
@Composable private fun ActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, action: () -> Unit) { Card(onClick = action, modifier = Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, Modifier.size(34.dp)); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; Icon(Icons.Default.ChevronRight, null) } } }

@Composable private fun LearnScreen(onComplete: () -> Unit) {
    var lessonIndex by remember { mutableIntStateOf(0) }; var stepIndex by remember { mutableIntStateOf(0) }; var marked by remember { mutableStateOf(false) }
    val lesson = LessonCatalog.lessons[lessonIndex.coerceIn(LessonCatalog.lessons.indices)]; val step = lesson.steps[stepIndex.coerceIn(lesson.steps.indices)]
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("LEARNING PATH", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Text(lesson.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { (stepIndex + 1).toFloat() / lesson.steps.size }, modifier = Modifier.fillMaxWidth()); Text("Step ${stepIndex + 1} of ${lesson.steps.size} · ${lesson.level}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Card { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(step.instruction, style = MaterialTheme.typography.titleLarge); if (step.expectedMidi != null) { Text("TARGET NOTE", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium); Text(PianoNote.fromMidi(step.expectedMidi).let { "${it.name}${it.octave}" }, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }; Text(step.hint, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        OutlinedButton(onClick = { marked = !marked }, modifier = Modifier.fillMaxWidth()) { Icon(if (marked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null); Spacer(Modifier.width(8.dp)); Text(if (marked) "Practice marked" else "Mark this step practiced") }
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { OutlinedButton(onClick = { if (stepIndex > 0) stepIndex-- else if (lessonIndex > 0) { lessonIndex--; stepIndex = LessonCatalog.lessons[lessonIndex].steps.lastIndex }; marked = false }, modifier = Modifier.weight(1f), enabled = lessonIndex > 0 || stepIndex > 0) { Text("Back") }; Button(onClick = { if (stepIndex < lesson.steps.lastIndex) { stepIndex++; marked = false } else { onComplete(); if (lessonIndex < LessonCatalog.lessons.lastIndex) { lessonIndex++; stepIndex = 0; marked = false } } }, modifier = Modifier.weight(1f)) { Text(if (stepIndex == lesson.steps.lastIndex) "Complete lesson" else "Next step") } }
    }
}

@Composable private fun PianoScreen() {
    var lowestMidi by remember { mutableIntStateOf(48) }; var selectedMidi by remember { mutableIntStateOf(60) }
    val notes = (lowestMidi until lowestMidi + 37).map { PianoNote.fromMidi(it) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("37-KEY NOTE MAP", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Text("Explore your keyboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Choose the first note of your physical keyboard. The default C3–C6 range is only an estimate.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Lowest note: ${PianoNote.fromMidi(lowestMidi).name}${PianoNote.fromMidi(lowestMidi).octave}", fontWeight = FontWeight.SemiBold)
        Slider(value = lowestMidi.toFloat(), onValueChange = { lowestMidi = it.roundToInt(); selectedMidi = lowestMidi.coerceAtMost(lowestMidi + 36) }, valueRange = 36f..60f, steps = 23)
        Card { Column(Modifier.padding(14.dp)) { Text("Selected note", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(PianoNote.fromMidi(selectedMidi).let { "${it.name}${it.octave}" }, style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold); Text("MIDI $selectedMidi · ${"%.1f".format(PianoNote.fromMidi(selectedMidi).frequencyHz)} Hz") } }
        Text("White keys", fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).height(142.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            notes.filter { it.name.length == 1 }.forEach { note -> Box(Modifier.width(43.dp).fillMaxHeight().border(1.dp, Color.LightGray, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)).background(if (selectedMidi == note.midi) Color(0xFFB8D4FF) else Color.White, RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp)).clickable { selectedMidi = note.midi }, contentAlignment = Alignment.BottomCenter) { Text("${note.name}${note.octave}", color = Color.Black, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(bottom = 8.dp)) }
        }
        Text("Black keys", fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { notes.filter { it.name.length > 1 }.forEach { note -> Box(Modifier.width(54.dp).height(48.dp).background(if (selectedMidi == note.midi) Color(0xFF456FA8) else Color(0xFF20242C), RoundedCornerShape(6.dp)).clickable { selectedMidi = note.midi }, contentAlignment = Alignment.Center) { Text("${note.name}${note.octave}", color = Color.White, style = MaterialTheme.typography.labelSmall) } } }
        Text("This is a visual note map, not a sound-producing piano. Use your physical keyboard for sound.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun PracticeScreen(onResult: (Boolean) -> Unit, onPracticeMinute: () -> Unit) {
    val context = LocalContext.current
    var listening by remember { mutableStateOf(false) }; var frequency by remember { mutableStateOf<Double?>(null) }; var detected by remember { mutableStateOf<PianoNote?>(null) }; var expected by remember { mutableIntStateOf(60) }; var message by remember { mutableStateOf("Tap Start listening, then play one clear note.") }; var bpm by remember { mutableIntStateOf(80) }; var metronome by remember { mutableStateOf(false) }; var beats by remember { mutableIntStateOf(0) }
    val listener = remember { MicrophonePitchListener { hz -> frequency = hz; detected = hz?.let { PianoNote.fromFrequency(it) } } }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) { listener.start(); listening = true; message = "Listening. Play a single note clearly." } else message = "Microphone permission is required for audio note detection." }
    DisposableEffect(Unit) { onDispose { listener.stop() } }
    LaunchedEffect(metronome, bpm) { while (metronome) { val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 65); tg.startTone(if (beats % 4 == 0) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_BEEP, 65); delay((60_000L / bpm).coerceAtLeast(150)); tg.release(); beats++ } }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("PRACTICE STUDIO", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary); Text("Listen and improve", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Microphone note detector", fontWeight = FontWeight.Bold); Text("Best with one note at a time and the phone close to the keyboard speaker. Room noise and piano harmonics can affect detection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { if (listening) { listener.stop(); listening = false; message = "Listening stopped." } else { if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) { listener.start(); listening = true; message = "Listening. Play a single note clearly." } else permission.launch(Manifest.permission.RECORD_AUDIO) } }, modifier = Modifier.fillMaxWidth()) { Icon(if (listening) Icons.Default.Stop else Icons.Default.Mic, null); Spacer(Modifier.width(8.dp)); Text(if (listening) "Stop listening" else "Start listening") }; Text(if (listening) "● Listening" else "○ Not listening", color = if (listening) Color(0xFF80CBC4) else MaterialTheme.colorScheme.onSurfaceVariant); Text(message, style = MaterialTheme.typography.bodySmall) } }
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Detected note", color = MaterialTheme.colorScheme.onSurfaceVariant); Text(detected?.let { "${it.name}${it.octave}" } ?: "—", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(frequency?.let { "${"%.1f".format(it)} Hz" } ?: "Waiting for a clear pitch"); Text("Practice target", fontWeight = FontWeight.SemiBold); Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { expected = (expected - 1).coerceAtLeast(36) }) { Text("−") }; Text(PianoNote.fromMidi(expected).let { "${it.name}${it.octave}" }, style = MaterialTheme.typography.titleLarge); TextButton(onClick = { expected = (expected + 1).coerceAtMost(96) }) { Text("+") } }; val correct = detected?.midi == expected; Text(if (detected == null) "Play the target note" else if (correct) "Correct note detected!" else "Try again: target ${PianoNote.fromMidi(expected).name}${PianoNote.fromMidi(expected).octave}", color = if (correct) Color(0xFF80CBC4) else MaterialTheme.colorScheme.onSurfaceVariant); Button(onClick = { onResult(correct); message = if (correct) "Nice work. Try another note." else "Attempt recorded. Listen and try again." }, enabled = detected != null, modifier = Modifier.fillMaxWidth()) { Text("Record this attempt") } } }
        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Metronome", fontWeight = FontWeight.Bold); Text("$bpm BPM", style = MaterialTheme.typography.headlineSmall); Slider(value = bpm.toFloat(), onValueChange = { bpm = it.roundToInt() }, valueRange = 40f..200f, steps = 31); Row(verticalAlignment = Alignment.CenterVertically) { Text("Metronome"); Spacer(Modifier.weight(1f)); Switch(checked = metronome, onCheckedChange = { metronome = it }) } } }
        OutlinedButton(onClick = onPracticeMinute, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Timer, null); Spacer(Modifier.width(8.dp)); Text("Add 1 minute of practice") }
    }
}

private data class Song(val title: String, val level: String, val notes: String)
@Composable private fun SongsScreen() {
    val songs = listOf(Song("Twinkle Twinkle Little Star", "Beginner · C major", "C C G G A A G · F F E E D D C"), Song("Ode to Joy", "Beginner · C major", "E E F G · G F E D · C C D E · E D D"), Song("Mary Had a Little Lamb", "Beginner · C major", "E D C D E E E · D D D · E G G"))
    var selected by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Song library", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Starter melodies with note names. Play them on your physical keyboard.", color = MaterialTheme.colorScheme.onSurfaceVariant); songs.forEachIndexed { i, song -> Card(onClick = { selected = i }) { Column(Modifier.padding(14.dp)) { Text(song.title, fontWeight = FontWeight.SemiBold); Text(song.level, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); if (selected == i) { Divider(Modifier.padding(vertical = 8.dp)); Text(song.notes, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary); Text("Play slowly and repeat each phrase.", style = MaterialTheme.typography.bodySmall) } } } } }
}

@Composable private fun TutorScreen() {
    var question by remember { mutableStateOf("") }; var answer by remember { mutableStateOf("I'm your offline piano coach. Ask about note names, finger numbers, scales, chords or practice technique.") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Personal piano tutor", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); Text("Offline tutor basics", color = MaterialTheme.colorScheme.primary); Card { Text(answer, Modifier.padding(16.dp)) }; OutlinedTextField(value = question, onValueChange = { question = it }, label = { Text("Ask a piano question") }, modifier = Modifier.fillMaxWidth()); Button(onClick = { answer = localTutorAnswer(question); question = "" }, modifier = Modifier.fillMaxWidth(), enabled = question.isNotBlank()) { Text("Ask tutor") }; Text("This first release uses built-in offline explanations. A connected AI tutor requires a secure backend; never place a private AI API key inside an APK.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}
private fun localTutorAnswer(q: String): String { val s = q.lowercase(); return when { "finger" in s -> "Right-hand finger numbers: thumb 1, index 2, middle 3, ring 4, little finger 5. For C-D-E-F-G, use 1-2-3-4-5."; "middle c" in s || "find c" in s -> "Find a pair of black keys. The white key immediately to their left is C. Middle C is usually called C4 (MIDI 60)."; "scale" in s -> "The C major scale is C-D-E-F-G-A-B-C. It uses only white keys. Practise slowly and evenly."; "chord" in s -> "A major triad uses scale degrees 1, 3 and 5. C major is C-E-G; F major is F-A-C; G major is G-B-D."; "practice" in s -> "Practise 10–15 minutes: five minutes note finding, five minutes finger patterns, and five minutes on one short song phrase."; else -> "Start with one clear note at a time. Learn C-D-E-F-G, keep your wrist relaxed, and practise slowly. Ask me about fingers, middle C, scales, chords or practice plans." } }

@Composable private fun ProgressScreen(completed: Int, seconds: Int, correct: Int, total: Int) { Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("Your progress", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold); MetricCard("Practice time", "${seconds / 60} minutes"); MetricCard("Lessons completed", completed.toString()); MetricCard("Recorded note accuracy", if (total == 0) "Not enough data" else "${(correct * 100f / total).roundToInt()}% ($correct/$total)"); Text("Practice statistics are stored locally on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
