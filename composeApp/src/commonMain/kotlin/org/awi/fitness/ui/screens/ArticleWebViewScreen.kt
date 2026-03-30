package org.awi.fitness.ui.screens

// Commented out: compose-webview-multiplatform requires CMP 1.8.0 / Kotlin 2.1.20
// which causes IrLinkageError on iOS with current CMP 1.7.3 / Kotlin 2.1.10.
// Articles now open in the external browser via LocalUriHandler.

// import androidx.compose.animation.AnimatedVisibility
// import androidx.compose.animation.fadeIn
// import androidx.compose.animation.fadeOut
// import androidx.compose.foundation.layout.Box
// import androidx.compose.foundation.layout.Column
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.ExperimentalMaterial3Api
// import androidx.compose.material3.Icon
// import androidx.compose.material3.IconButton
// import androidx.compose.material3.LinearProgressIndicator
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Scaffold
// import androidx.compose.material3.Text
// import androidx.compose.material3.TopAppBar
// import androidx.compose.material3.TopAppBarDefaults
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.runtime.snapshotFlow
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.platform.LocalUriHandler
// import androidx.compose.ui.text.style.TextOverflow
// import cafe.adriel.voyager.core.screen.Screen
// import cafe.adriel.voyager.navigator.LocalNavigator
// import cafe.adriel.voyager.navigator.currentOrThrow
// import com.multiplatform.webview.web.LoadingState
// import com.multiplatform.webview.web.WebView
// import com.multiplatform.webview.web.rememberWebViewState
// import compose.icons.TablerIcons
// import compose.icons.tablericons.ArrowLeft
// import compose.icons.tablericons.ExternalLink
//
// class ArticleWebViewScreen(
//     private val url: String,
//     private val title: String
// ) : Screen {
//     @OptIn(ExperimentalMaterial3Api::class)
//     @Composable
//     override fun Content() {
//         val navigator = LocalNavigator.currentOrThrow
//         val uriHandler = LocalUriHandler.current
//         val webViewState = rememberWebViewState(url)
//         val isLoading = webViewState.loadingState is LoadingState.Loading
//
//         Scaffold(
//             topBar = {
//                 TopAppBar(
//                     title = {
//                         Text(
//                             text = title,
//                             style = MaterialTheme.typography.titleSmall,
//                             maxLines = 1,
//                             overflow = TextOverflow.Ellipsis
//                         )
//                     },
//                     navigationIcon = {
//                         IconButton(onClick = { navigator.pop() }) {
//                             Icon(
//                                 imageVector = TablerIcons.ArrowLeft,
//                                 contentDescription = null
//                             )
//                         }
//                     },
//                     actions = {
//                         IconButton(onClick = {
//                             try { uriHandler.openUri(url) } catch (_: Exception) {}
//                         }) {
//                             Icon(
//                                 imageVector = TablerIcons.ExternalLink,
//                                 contentDescription = null,
//                                 tint = MaterialTheme.colorScheme.primary
//                             )
//                         }
//                     },
//                     colors = TopAppBarDefaults.topAppBarColors(
//                         containerColor = MaterialTheme.colorScheme.surface
//                     )
//                 )
//             }
//         ) { paddingValues ->
//             Column(
//                 modifier = Modifier
//                     .fillMaxSize()
//                     .padding(paddingValues)
//             ) {
//                 AnimatedVisibility(
//                     visible = isLoading,
//                     enter = fadeIn(),
//                     exit = fadeOut()
//                 ) {
//                     LinearProgressIndicator(
//                         modifier = Modifier.fillMaxWidth(),
//                         color = MaterialTheme.colorScheme.primary,
//                         trackColor = MaterialTheme.colorScheme.surface
//                     )
//                 }
//
//                 WebView(
//                     state = webViewState,
//                     modifier = Modifier.fillMaxSize()
//                 )
//             }
//         }
//     }
// }
