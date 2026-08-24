package com.pyth0nkod3r.fabricfocus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pyth0nkod3r.fabricfocus.data.*
import com.pyth0nkod3r.fabricfocus.ui.theme.FabricFocusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FabricFocusTheme { FabricFocusApp(StudyRepository(this)) } }
    }
}

private enum class Tab(val label: String) { Home("Home"), Practice("Practice"), Review("Review"), Progress("Progress") }

@Composable
private fun FabricFocusApp(repo: StudyRepository) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    var practiceTopic by rememberSaveable { mutableStateOf<String?>(null) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                listOf(Tab.Home to Icons.Outlined.Home, Tab.Practice to Icons.Outlined.PlayCircle, Tab.Review to Icons.Outlined.BookmarkBorder, Tab.Progress to Icons.Outlined.Insights).forEach { (item, icon) ->
                    NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Icon(icon, item.label) }, label = { Text(item.label) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (tab) {
                Tab.Home -> HomeScreen(repo, startPractice = { practiceTopic = null; tab = Tab.Practice }, exploreTopic = { practiceTopic = it; tab = Tab.Practice })
                Tab.Practice -> PracticeScreen(repo, topicFilter = practiceTopic, clearFilter = { practiceTopic = null })
                Tab.Review -> ReviewScreen(repo)
                Tab.Progress -> ProgressScreen(repo)
            }
        }
    }
}

@Composable
private fun HomeScreen(repo: StudyRepository, startPractice: () -> Unit, exploreTopic: (String) -> Unit) {
    val dashboard = remember { repo.dashboard() }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Fabric Focus", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("DP-600 • offline study space", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ready for a focused session?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${dashboard.total} questions and ${dashboard.imageCount} linked diagrams are available offline.")
                    Button(onClick = startPractice, modifier = Modifier.padding(top = 6.dp)) { Icon(Icons.Outlined.ArrowForward, null); Spacer(Modifier.width(8.dp)); Text("Start practice") }
                }
            }
        }
        item { Text("Study coverage", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        items(dashboard.topics) { topic ->
            ElevatedCard(shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.fillMaxWidth().clickable { exploreTopic(topic.name) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AccountTree, null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { Text(topic.name, fontWeight = FontWeight.Medium); Text("${topic.questions} questions", style = MaterialTheme.typography.bodySmall) }
                    Text("Explore", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun PracticeScreen(repo: StudyRepository, topicFilter: String? = null, clearFilter: () -> Unit = {}) {
    var offset by rememberSaveable { mutableIntStateOf(0) }
    var selection by rememberSaveable { mutableStateOf<String?>(null) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(topicFilter) { offset = 0; selection = null; submitted = false }
    val question = remember(offset, topicFilter) { repo.question(offset, topicFilter) }
    val choices = remember(question?.id) { question?.let { repo.choices(it.id) }.orEmpty() }
    val images = remember(question?.id) { question?.let { repo.images(it.id, "question") }.orEmpty() }
    if (question == null) { EmptyState("You reached the end", "Start a new study session to review questions again."); return }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) { AssistChip(onClick = {}, label = { Text(question.topic) }); if (topicFilter != null) { Spacer(Modifier.width(8.dp)); AssistChip(onClick = clearFilter, label = { Text("✕ $topicFilter") }) }; Spacer(Modifier.width(8.dp)); Text("Question ${offset + 1}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.height(12.dp)); Text(question.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        if (images.isNotEmpty()) item {
            images.forEach { image -> AsyncImage(model = "file:///android_asset/" + image.path.removePrefix("assets/"), contentDescription = "Question diagram", modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) }
        }
        items(choices) { choice ->
            val isSelected = selection == choice.key
            val isCorrect = submitted && choice.isCorrect
            val isWrong = submitted && isSelected && !choice.isCorrect
            val colors = CardDefaults.cardColors(containerColor = when { isCorrect -> MaterialTheme.colorScheme.tertiaryContainer; isWrong -> MaterialTheme.colorScheme.errorContainer; isSelected -> MaterialTheme.colorScheme.secondaryContainer; else -> MaterialTheme.colorScheme.surfaceVariant })
            Card(colors = colors, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().clickable(enabled = !submitted) { selection = choice.key }) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Text(choice.key, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(14.dp)); Text(choice.text) }
            }
        }
        item {
            Button(onClick = { if (submitted) { offset++; selection = null; submitted = false } else submitted = true }, enabled = selection != null || submitted, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(if (submitted) "Next question" else "Check answer") }
            AnimatedVisibility(submitted) { ExplanationCard(question.explanation, repo.images(question.id, "explanation")) }
        }
    }
}

@Composable private fun ExplanationCard(explanation: String, images: List<QuestionImage>) {
    ElevatedCard(Modifier.padding(top = 14.dp), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Lightbulb, null); Spacer(Modifier.width(8.dp)); Text("Explanation", fontWeight = FontWeight.Bold) }
            Text(if (explanation.isBlank()) "No explanation was provided for this item." else explanation)
            images.forEach { image -> AsyncImage(model = "file:///android_asset/" + image.path.removePrefix("assets/"), contentDescription = "Explanation diagram", modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) }
        }
    }
}

@Composable private fun ReviewScreen(repo: StudyRepository) = EmptyState("Review queue", "Bookmarks and incorrect-answer review will appear here as you study.")
@Composable private fun ProgressScreen(repo: StudyRepository) { val d = remember { repo.dashboard() }; EmptyState("Progress insights", "Your question bank has ${d.total} items. Complete practice sessions to unlock topic accuracy and streaks.") }
@Composable private fun EmptyState(title: String, detail: String) { Box(Modifier.fillMaxSize().padding(30.dp), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.AutoGraph, null, modifier = Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
