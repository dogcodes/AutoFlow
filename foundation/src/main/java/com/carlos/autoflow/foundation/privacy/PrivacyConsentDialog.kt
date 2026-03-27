package com.carlos.autoflow.foundation.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.carlos.autoflow.foundation.R
import com.carlos.autoflow.foundation.ui.WebViewActivity

@Composable
fun PrivacyConsentDialog(
    onAgree: () -> Unit,
    onDecline: () -> Unit
) {
    val intro = stringResource(id = R.string.privacy_intro)
    val tail = stringResource(id = R.string.privacy_tail)
    val linksPrefix = stringResource(id = R.string.privacy_links_prefix)
    val privacyLabel = stringResource(id = R.string.privacy_policy)
    val userAgreementLabel = stringResource(id = R.string.user_agreement)
    val context = LocalContext.current
    val annotatedSummary = buildAnnotatedString {
        append(intro)
        append("，")
        append("$linksPrefix ")
            pushStringAnnotation(tag = "privacy", annotation = PrivacyPolicy.PRIVACY_URL)
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("《")
                append(privacyLabel)
                append("》")
            }
        pop()
        append(" ")
            pushStringAnnotation(tag = "user", annotation = PrivacyPolicy.USER_AGREEMENT_URL)
            withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                append("《")
                append(userAgreementLabel)
                append("》")
            }
        pop()
        append("，")
        append(tail)
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                text = stringResource(id = R.string.privacy_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ClickableText(
                    text = annotatedSummary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { offset ->
                        annotatedSummary.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                            .firstOrNull()?.let {
                                context.startActivity(
                                WebViewActivity.createIntent(
                                    context,
                                    it.item,
                                    privacyLabel
                                )
                                )
                            }
                        annotatedSummary.getStringAnnotations(tag = "user", start = offset, end = offset)
                            .firstOrNull()?.let {
                                context.startActivity(
                                WebViewActivity.createIntent(
                                    context,
                                    it.item,
                                    userAgreementLabel
                                )
                                )
                            }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAgree,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = stringResource(id = R.string.privacy_agree))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDecline) {
                Text(text = stringResource(id = R.string.privacy_decline))
            }
        }
    )
}
