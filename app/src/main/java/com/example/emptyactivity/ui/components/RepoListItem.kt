package com.example.emptyactivity.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.emptyactivity.R
import com.example.emptyactivity.domain.model.GithubRepo

private val AvatarBlue = Color(0xFF9FA8DA)

@Composable
fun RepoListItem(
    repo: GithubRepo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AvatarBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = repo.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleMedium
                )

                repo.owner?.takeIf { it.isNotBlank() }?.let { owner ->
                    Text(
                        text = stringResource(R.string.repo_owner, owner),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Text(
                    text = stringResource(
                        R.string.repo_language_stars,
                        repo.language ?: stringResource(R.string.repo_language_unknown),
                        repo.stars
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold)
        Text(text = value)
    }
}