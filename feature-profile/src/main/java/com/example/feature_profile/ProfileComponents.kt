package com.example.feature_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

private val AvatarBlue = Color(0xFF9FA8DA)

@Composable
fun AvatarView(
    avatarUri: String,
    size: Int,
    onClick: (() -> Unit)? = null
) {
    val avatarModifier = Modifier
        .size(size.dp)
        .clip(CircleShape)
        .background(AvatarBlue)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    if (avatarUri.isBlank()) {
        Box(modifier = avatarModifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.avatar_placeholder),
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        AsyncImage(
            model = avatarUri,
            contentDescription = stringResource(R.string.avatar_content_description),
            modifier = avatarModifier
        )
    }
}

@Composable
fun ProfileInfoCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Text(value, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}