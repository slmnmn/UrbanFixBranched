package com.example.urbanfix.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                // SECCIÓN DEL MAPA CON OVERLAYS Y TARJETA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp)
                ) {
                    // Mapa de fondo
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

                        // Ícono bombillo a la izquierda
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gen_ia),
                                contentDescription = "Bombillo",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }

                        // Ícono marcador a la derecha
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB76998)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.gen_ia),
                                contentDescription = "Ubicación",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // TARJETA AZUL OSCURA CON INFORMACIÓN (encima del mapa)
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
                                // Título centrado
                                Text(
                                    text = "Alumbrado Público",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Descripción
                                Text(
                                    text = "Se reporta que en la Calle 31A #13 – a 91 Sur el alumbrado está intermitente, específicamente al lado del CAI del Gustavo Restrepo",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Fila inferior con líneas individuales encima de cada ícono
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // 🔹 Código
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
                                                contentDescription = "Copiar",
                                                modifier = Modifier.size(18.dp),
                                                tint = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // 🔹 Like
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
                                                    contentDescription = "Like",
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

                                    // 🔹 Dislike
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
                                                    contentDescription = "Dislike",
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

                                    // 🔹 Estado
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
                                            text = "En Proceso",
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

                            // Botón editar en la parte superior derecha (FUERA del Column)
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
                                    text = "EDITAR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Círculo con imagen en el centro (debe estar encima de todo)
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                            .offset(y = (-50).dp)
                            .clip(CircleShape)
                            .background(Color.White, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.alumbrado_foto),
                            contentDescription = "Foto del reporte",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                // Información de fecha y creador
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-20).dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reportado el 06 - 09 - 2025 - 19:45",
                        fontSize = 10.sp,
                        color = Color(0xFF333333),
                        lineHeight = 13.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creado por Dylan Gutierrez",
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

                // SECCIÓN DE COMENTARIOS
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

                    // Mostrar comentarios
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

            // Campo de comentario fijo en la parte inferior
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    // Barra de comentario
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
                                .height(44.dp),
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

                    // Barra de navegación inferior
                    BottomNavBar(navController = navController)
                }
            }
        }
    }

    // Diálogo para editar comentario
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

    // Diálogo de opciones de comentario
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
                comments = comments.filter { it.id != showOptionsDialog }
                showOptionsDialog = null
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
    Box(modifier = Modifier.fillMaxWidth()) {
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
                        color = BlackFull
                    )

                    if (comment.isVerified) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "Verified",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF00BCD4)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // "Ahora" o tiempo
                    Text(
                        text = comment.time,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )

                    // 🔹 Icono de 3 puntos al lado del tiempo
                    if (comment.isCurrentUser) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_comentario),
                                contentDescription = "Opciones",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF666666)
                            )
                        }
                    }
                }
                Text(
                    text = comment.text,
                    fontSize = 12.sp,
                    color = Color.Black
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
                .fillMaxWidth(1f)
                .padding(3.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)), // fondo verdoso claro
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

            ) {
                // Header con X
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F5E1)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "¿Qué deseas hacer con tu comentario?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier
                            .weight(1f)
                             .padding(top= 10.dp, bottom = 10.dp, end= 10.dp, start = 10.dp),
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icono_x_salir),
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF666666)
                        )
                    }
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
                        contentDescription = "Editar",
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = "Editar",
                        fontSize = 15.sp,
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
                        contentDescription = "Eliminar",
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = "Eliminar",
                        fontSize = 15.sp,
                        color = Color(0xFF000000)
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
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar comentario",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFCCCCCC),
                        focusedBorderColor = Color(0xFF4F7D94)
                    ),
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = Color(0xFF666666))
                    }

                    Button(
                        onClick = { onSave(editedText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7D94)
                        ),
                        enabled = editedText.isNotBlank()
                    ) {
                        Text("Guardar", color = Color.White)
                    }
                }
            }
        }
    }
}