package com.aubynsamuel.clipsync.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.clipsync.R

@Composable
fun AppInfoCard() {
    val uriHandler = LocalUriHandler.current

    val buyMeACoffee = stringResource(R.string.buy_me_a_coffee)
    val sourceCodeUrl = stringResource(R.string.sourceCode)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp)
            .background(
                colorScheme.primaryContainer,
                shape = RoundedCornerShape(30.dp)
            )
            .padding(15.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy((-5).dp)) {
                Text(
                    "ClipSync",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = colorScheme.onPrimaryContainer
                )
                Text(
                    "1.1.0",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = colorScheme.onPrimaryContainer
                )
            }
            Row {
                IconButton(
                    { uriHandler.openUri(sourceCodeUrl) },
                    colors = IconButtonDefaults.iconButtonColors()
                        .copy(containerColor = colorScheme.onPrimaryContainer)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = "Source Code Url",
                        tint = colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { uriHandler.openUri(buyMeACoffee) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(
                    containerColor = colorScheme.onPrimaryContainer,
                    contentColor = colorScheme.onPrimary
                )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_buymeacoffee),
                    contentDescription = "Buy me a coffee"
                )
                Spacer(Modifier.width(5.dp))
                Text("Sponsor", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}