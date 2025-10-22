package com.example.urbanfix.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.window.Dialog

data class Comment(
    val id: String,
    val author: String,
    val initials: String,
    val text: String,
    val time: String,
    val isVerified: Boolean = false,
    val backgroundColor: Color,
    val isCurrentUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarReporteScreen(
    navController: NavHostController,
    reportId: String = ""
) {
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var likeActive by remember { mutableStateOf(true) }
    var dislikeActive by remember { mutableStateOf(false) }
    var likeCount by remember { mutableStateOf(1) }
    var dislikeCount by remember { mutableStateOf(0) }
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    var showDialog by remember { mutableStateOf(false) }
    val code = "#2025-0001"
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<String?>(null) }
    var showImageGallery by remember { mutableStateOf(false) }
    val reportImages = listOf(
        R.drawable.alumbrado_foto,
        R.drawable.prueba_circulo
    )

    var comments by remember {
        mutableStateOf(
            listOf(
                Comment(
                    id = "1",
                    author = "Fernanda Ladino",
                    initials = "FL",
                    text = "Estamos procesando el reporte.",
                    time = "hace 20 horas",
                    isVerified = true,
                    backgroundColor = Color(0xFFB76998),
                    isCurrentUser = false
                ),
                Comment(
                    id = "2",
                    author = "Dylan Gutierrez",
                    initials = "DG",
                    text = "Ya estoy cansado de esa situación, espero lo arreglen pronto.",
                    time = "hace 1d",
                    isVerified = false,
                    backgroundColor = Color(0xFFDEB6D1),
                    isCurrentUser = false
                )
            )
        )
    }

    var showMenuForComment by remember { mutableStateOf<String?>(null) }
    var commentToEdit by remember { mutableStateOf<Comment?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp, end = 42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.report_details_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(top = 20.dp)) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_content_description),
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlueMain
                ),
                modifier = Modifier.height(72.dp)
            )
        },
        containerColor = Color(0xFFF1FAEE)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(Color(0xFFF1FAEE))
                    .paddingFromBaseline(bottom = 180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFFE8EEF5))
                    ) {
                        MapboxMapComponent(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(connection = object : NestedScrollConnection {}),
                            hasPermission = true,
                            selectedLocation = null,
                            onMapReady = {},
                            onLocationSelected = {}
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp)
                            .offset(y = (-30).dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.report_category_lighting),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = stringResource(R.string.report_description_example),
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.offset(y = -2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(100.dp)
                                                .height(2.dp)
                                                .background(Color.White.copy(alpha = 0.6f))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "#2025-0001",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Icon(
                                                painter = painterResource(id = R.drawable.copiar_code),
                                                contentDescription = stringResource(R.string.copy_code),
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(code))
                                                        showDialog = true
                                                    },
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    if (showDialog) {
                                        CodeCopiedDialog(onDismiss = { showDialog = false })
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(2.dp)
                                                .background(Color.White.copy(alpha = 0.6f))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        IconButton(
                                            onClick = {
                                                if (likeActive) {
                                                    likeActive = false
                                                    likeCount--
                                                } else {
                                                    likeActive = true
                                                    likeCount++
                                                    if (dislikeActive) {
                                                        dislikeActive = false
                                                        dislikeCount--
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        id = if (likeActive) R.drawable.like_relleno else R.drawable.sin_like
                                                    ),
                                                    contentDescription = stringResource(R.string.like_button),
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (likeActive) Color.White else Color.White.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = likeCount.toString(),
                                                    fontSize = 11.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(40.dp)
                                                .height(2.dp)
                                                .background(Color.White.copy(alpha = 0.6f))
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        IconButton(
                                            onClick = {
                                                if (dislikeActive) {
                                                    dislikeActive = false
                                                    dislikeCount--
                                                } else {
                                                    dislikeActive = true
                                                    dislikeCount++
                                                    if (likeActive) {
                                                        likeActive = false
                                                        likeCount--
                                                    }
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Icon(
                                                    painter = painterResource(
                                                        id = if (dislikeActive) R.drawable.dislike_relleno else R.drawable.no_like
                                                    ),
                                                    contentDescription = stringResource(R.string.dislike_button),
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (dislikeActive) Color.White else Color.White.copy(alpha = 0.3f)
                                                )
                                                Text(
                                                    text = dislikeCount.toString(),
                                                    fontSize = 11.sp,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.offset(y = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(90.dp)
                                                .height(2.dp)
                                                .background(Color.White.copy(alpha = 0.6f))
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = stringResource(R.string.status_in_progress),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier
                                                .background(Color(0xFFFFB800), RoundedCornerShape(6.dp))
                                                .padding(start = 10.dp, top = 4.dp, end = 10.dp, bottom = 6.dp)
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    navController.navigate("reportar/alumbrado")
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFCCCCCC)
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.edit_button),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                            .offset(y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                            .clickable { showImageGallery = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alumbrado_foto),
                            contentDescription = stringResource(R.string.report_photo),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (showImageGallery) {
                        ImageGalleryDialog(
                            images = reportImages,
                            initialIndex = 0,
                            onDismiss = { showImageGallery = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reported_date_example),
                        fontSize = 10.sp,
                        color = Color(0xFF333333),
                        lineHeight = 13.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.created_by_example),
                            fontSize = 10.sp,
                            color = Color(0xFF333333),
                            textDecoration = TextDecoration.Underline
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB76998)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "DG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.comments_label),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlackFull
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    comments.forEach { comment ->
                        CommentItem(
                            comment = comment,
                            showMenu = showOptionsDialog == comment.id,
                            onMenuClick = {
                                showOptionsDialog = if (showOptionsDialog == comment.id) null else comment.id
                            },
                            onEditClick = {
                                commentToEdit = comment
                                showEditDialog = true
                                showOptionsDialog = null
                            },
                            onDeleteClick = {
                                comments = comments.filter { it.id != comment.id }
                                showOptionsDialog = null
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFB76998))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.add_comment_placeholder),
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    val newComment = Comment(
                                        id = System.currentTimeMillis().toString(),
                                        author = "Dylan Gutierrez",
                                        initials = "DG",
                                        text = commentText,
                                        time = "ahora",
                                        isVerified = false,
                                        backgroundColor = Color(0xFFA8DADC),
                                        isCurrentUser = true
                                    )
                                    comments = listOf(newComment) + comments
                                    commentText = ""
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (commentText.isNotBlank()) Color(0xFF8B4A6F) else Color(0xFFB76998),
                                    CircleShape
                                ),
                            enabled = commentText.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icono_enviar_comentario),
                                contentDescription = stringResource(R.string.send_comment),
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                    }

                    BottomNavBar(navController = navController)
                }
            }
        }
    }

    if (showEditDialog && commentToEdit != null) {
        EditCommentDialog(
            comment = commentToEdit!!,
            onDismiss = {
                showEditDialog = false
                commentToEdit = null
            },
            onSave = { editedText ->
                comments = comments.map {
                    if (it.id == commentToEdit?.id) it.copy(text = editedText) else it
                }
                showEditDialog = false
                commentToEdit = null
            }
        )
    }

    if (showOptionsDialog != null && commentToEdit == null) {
        CommentOptionsDialog(
            onDismiss = { showOptionsDialog = null },
            onEdit = {
                val comment = comments.find { it.id == showOptionsDialog }
                if (comment != null) {
                    commentToEdit = comment
                    showEditDialog = true
                    showOptionsDialog = null
                }
            },
            onDelete = {
                commentToDelete = showOptionsDialog
                showDeleteDialog = true
                showOptionsDialog = null
            }
        )
    }

    if (showDeleteDialog && commentToDelete != null) {
        DeleteCommentDialog(
            onDismiss = {
                showDeleteDialog = false
                commentToDelete = null
            },
            onConfirm = {
                comments = comments.filter { it.id != commentToDelete }
                showDeleteDialog = false
                commentToDelete = null
            }
        )
    }
}
@Composable
fun CommentItem(
    comment: Comment,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (comment.isCurrentUser) Color(0xFF2C3E50) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = if (comment.isCurrentUser) 12.dp else 0.dp,
                vertical = if (comment.isCurrentUser) 10.dp else 0.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(comment.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.initials,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (comment.isCurrentUser) Color.Black else Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = comment.author,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comment.isCurrentUser) Color.White else BlackFull
                    )

                    if (comment.isVerified) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.verified_description),
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF00BCD4)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = comment.time,
                        fontSize = 11.sp,
                        color = if (comment.isCurrentUser) Color(0xFFCCCCCC) else Color(0xFF999999)
                    )

                    if (comment.isCurrentUser) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_comentario),
                                contentDescription = stringResource(R.string.options_description),
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = comment.text,
                    fontSize = 12.sp,
                    color = if (comment.isCurrentUser) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
fun CommentOptionsDialog(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F5E1))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.comment_options_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Opción Editar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icono_edit_coment),
                        contentDescription = stringResource(R.string.edit_description),
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = stringResource(R.string.edit_option),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Opción Eliminar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icono_elim_coment),
                        contentDescription = stringResource(R.string.delete_description),
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = stringResource(R.string.delete_option),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF000000)
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Botón Volver
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EditCommentDialog(
    comment: Comment,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(comment.text) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF3))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Título del diálogo
                Text(
                    text = stringResource(R.string.edit_comment_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F4F4F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Campo de texto con fondo blanco y sombras suaves
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF4F7D94),
                        cursorColor = Color(0xFF4F7D94)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.comment_placeholder),
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    },
                    minLines = 3
                )

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFB0BEC5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Gray,
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }

                    Button(
                        onClick = { onSave(editedText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7D94),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        enabled = editedText.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save_button))
                    }
                }
            }
        }
    }
}

@Composable
fun CodeCopiedDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF90BE6D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.code_copied_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.code_copied_message),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.accept_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header rojo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFFE63946)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.delete_comment_title),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // Texto de advertencia
                Text(
                    text = stringResource(R.string.delete_warning),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp)
                )

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón Volver
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D3557)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.back_button),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Borrar
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE63946)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_button),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageGalleryDialog(
    images: List<Int>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Contenedor de la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = images[currentIndex]),
                        contentDescription = stringResource(R.string.image_description),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Botón Volver
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Flechas de navegación
            if (images.size > 1) {
                // Flecha izquierda
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex > 0) currentIndex - 1 else images.size - 1
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(56.dp)
                        .offset(y = (-40).dp)
                        .background(
                            color = if (isLeftPressed) {
                                Color.Black.copy(alpha = 0.7f)
                            } else {
                                Color.Black.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isLeftPressed = true
                                    tryAwaitRelease()
                                    isLeftPressed = false
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.previous_description),
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Flecha derecha
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex < images.size - 1) currentIndex + 1 else 0
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .offset(y = (-38).dp)
                        .size(56.dp)
                        .background(
                            color = if (isRightPressed) {
                                Color.Black.copy(alpha = 0.7f)
                            } else {
                                Color.Black.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isRightPressed = true
                                    tryAwaitRelease()
                                    isRightPressed = false
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.next_description),
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}