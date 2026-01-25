import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sencsu.components.forms.*
import com.example.sencsu.components.modals.AddDependantModalContent
import com.example.sencsu.data.remote.dto.AdherentDto
import com.example.sencsu.data.remote.dto.FormConstants
import com.example.sencsu.domain.viewmodel.AddAdherentUiEvent
import com.example.sencsu.domain.viewmodel.AddAdherentUiState
import com.example.sencsu.domain.viewmodel.AddAdherentViewModel
import com.example.sencsu.utils.toLocaleString
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
private val NeutralDark = Color(0xFF1E293B) // Texte principal et boutons
private val NeutralMedium = Color(0xFF64748B) // Texte secondaire
private val NeutralLight = Color(0xFFF1F5F9) // Fonds légers
private val BorderColor = Color(0xFFE2E8F0)
private val SuccessGreen = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AddAdherentScreen(
    onBack: () -> Unit,
    onNavigateToPayment: (adherentId: Long, montantTotal: Int) -> Unit,
    agentId: Long?,
    viewModel: AddAdherentViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(0) }
    val steps = listOf("Identité", "Contact", "Zone", "Photos", "Bénéficiaires")

    var showAlertDialog by remember { mutableStateOf(false) }
    var alertTitle by remember { mutableStateOf("") }
    var alertMessage by remember { mutableStateOf("") }
    var alertAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddAdherentUiEvent.NavigateToPayment -> {
                    event.adherentId?.let {
                        onNavigateToPayment(it, event.montantTotal)
                    }
                }
                is AddAdherentUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ModernHeader(
                title = "Nouvel Adhérent",
                currentStep = currentStep,
                totalSteps = steps.size,
                totalCost = state.totalCost,
                totalBeneficiaries = state.dependants.size + 1,
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Barre de progression discrète
            StepProgressBar(
                totalSteps = steps.size,
                currentStep = currentStep,
                modifier = Modifier.padding(20.dp)
            )

            // Contenu du formulaire
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "form_step"
            ) { step ->
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    when (step) {
                        0 -> IdentitySection(state, viewModel)
                        1 -> ContactSection(state, viewModel)
                        2 -> LocationSection(state, viewModel)
                        3 -> PhotosSection(state, viewModel)
                        4 -> BeneficiariesSection(
                            state = state,
                            viewModel = viewModel,
                            onShowAlert = { title, msg, action ->
                                alertTitle = title
                                alertMessage = msg
                                alertAction = action
                                showAlertDialog = true
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation
            SimpleNavigation(
                currentStep = currentStep,
                totalSteps = steps.size,
                isLoading = state.isLoading,
                onPrevious = { if (currentStep > 0) currentStep-- },
                onNext = { if (currentStep < steps.size - 1) currentStep++ },
                onSubmit = { viewModel.submitWithUpload(context, agentId) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Modals & Dialogs
        if (state.isModalVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hideModal() },
                containerColor = Color.White
            ) {
                AddDependantModalContent(
                    viewModel = viewModel,
                    onSave = { viewModel.saveDependant() },
                    onCancel = { viewModel.hideModal() }
                )
            }
        }

        if (showAlertDialog) {
            SimpleAlertDialog(
                title = alertTitle,
                message = alertMessage,
                onConfirm = {
                    alertAction?.invoke()
                    showAlertDialog = false
                },
                onDismiss = { showAlertDialog = false }
            )
        }
    }
}

@Composable
fun ModernHeader(
    title: String,
    currentStep: Int,
    totalSteps: Int,
    totalCost: Int,
    totalBeneficiaries: Int,
    onBack: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier
            .padding(vertical = 30.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()

                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = NeutralDark)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = NeutralDark)
                    Text("Étape ${currentStep + 1} sur $totalSteps", fontSize = 12.sp, color = NeutralMedium)
                }
            }

            // Infos Montant et Bénéficiaires à droite
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${totalCost.toLocaleString()} F",
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    color = NeutralDark
                )
                Surface(
                    color = NeutralLight,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$totalBeneficiaries bénéficiaire(s)",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NeutralMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressBar(totalSteps: Int, currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (index <= currentStep) NeutralDark else BorderColor)
            )
        }
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = NeutralMedium, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = NeutralDark, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

// --- Sections du formulaire ---

@Composable
fun IdentitySection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Informations personnelles", Icons.Outlined.Person) {
        FormTextField(value = state.prenoms, onValueChange = viewModel::updatePrenoms, label = "Prénoms", placeholder = "Prénoms*")
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(value = state.nom, onValueChange = viewModel::updateNom, label = "Nom", placeholder = "Nom de famille*")
        Spacer(modifier = Modifier.height(12.dp))
        DatePickerField(label = "Date de naissance", value = state.dateNaissance, onDateSelected = viewModel::updateDateNaissance)
        Spacer(modifier = Modifier.height(12.dp))
        SegmentedSelector(title = "Sexe*", options = listOf("M", "F"), selected = state.sexe, onSelect = viewModel::updateSexe)
        Spacer(modifier = Modifier.height(12.dp))
        SegmentedSelector(title = "Situation*", options = FormConstants.SITUATIONS, selected = state.situationMatrimoniale, onSelect = viewModel::updateSituationMatrimoniale)
    }
}

@Composable
fun ContactSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Contact & ID", Icons.Outlined.Phone) {
        FormTextField(value = state.whatsapp, onValueChange = viewModel::updateWhatsapp, label = "WhatsApp", placeholder = "77...", keyboardType = KeyboardType.Phone)
        Spacer(modifier = Modifier.height(12.dp))
        SegmentedSelector(title = "Pièce*", options = FormConstants.TYPES_PIECE, selected = state.typePiece, onSelect = viewModel::updateTypePiece)
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(
            value = if (state.typePiece == "CNI") state.numeroCNI else state.numeroExtrait,
            onValueChange = if (state.typePiece == "CNI") viewModel::updateNumeroCNI else viewModel::updateNumeroExtrait,
            label = "Numéro de pièce",
            placeholder = "Entrez le numéro*"
        )
    }
}

@Composable
fun LocationSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Localisation", Icons.Outlined.LocationOn) {
        SegmentedSelector(title = "Département*", options = FormConstants.DEPARTEMENTS, selected = state.departement, onSelect = viewModel::updateDepartement)
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(value = state.commune, onValueChange = viewModel::updateCommune, label = "Commune", placeholder = "Ville/Commune*")
        Spacer(modifier = Modifier.height(12.dp))
        FormTextField(value = state.adresse, onValueChange = viewModel::updateAdresse, label = "Adresse", placeholder = "Quartier, rue...*")
    }
}

@Composable
fun PhotosSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel) {
    SectionCard("Documents", Icons.Outlined.CameraAlt) {
        ImagePickerComponent(label = "Photo d'identité", imageUri = state.photoUri, onImageSelected = viewModel::updatePhotoUri)
        Spacer(modifier = Modifier.height(12.dp))
        ImagePickerComponent(label = "CNI Recto", imageUri = state.rectoUri, onImageSelected = viewModel::updateRectoUri)
        Spacer(modifier = Modifier.height(12.dp))
        ImagePickerComponent(label = "CNI Verso", imageUri = state.versoUri, onImageSelected = viewModel::updateVersoUri)
    }
}

@Composable
fun BeneficiariesSection(state: AddAdherentUiState, viewModel: AddAdherentViewModel, onShowAlert: (String, String, () -> Unit) -> Unit) {
    SectionCard("Bénéficiaires", Icons.Outlined.Group) {
        state.dependants.forEachIndexed { index, dep ->
            DependantCard(dependant = dep, onEdit = { viewModel.showEditDependantModal(index, dep) }, onDelete = {
                onShowAlert("Supprimer", "Retirer ce bénéficiaire ?") { viewModel.removeDependant(index) }
            })
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedButton(
            onClick = { viewModel.showAddDependantModal() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, NeutralDark.copy(0.2f))
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("Ajouter une personne", color = NeutralDark)
        }
    }
}

@Composable
fun SimpleNavigation(
    currentStep: Int,
    totalSteps: Int,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text("Précédent", color = NeutralMedium)
            }
        }

        Button(
            onClick = if (currentStep == totalSteps - 1) onSubmit else onNext,
            modifier = Modifier.weight(1f).height(50.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeutralDark)
        ) {
            if (isLoading) {
                AppProgressIndicator(
                    // progress = state.uploadProgress, // Optionnel : si tu veux voir l'avancement
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )            } else {
                Text(if (currentStep == totalSteps - 1) "Enregistrer" else "Suivant")
            }
        }
    }
}

@Composable
fun SimpleAlertDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Confirmer", color = Color.Red) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler", color = NeutralMedium) }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}


@Composable
fun AppProgressIndicator(
    progress: Float? = null, // Si null, il tourne indéfiniment
    modifier: Modifier = Modifier.size(24.dp),
    color: Color = NeutralDark,
    trackColor: Color = NeutralDark.copy(alpha = 0.1f),
    strokeWidth: Dp = 3.dp
) {
    if (progress != null) {
        // --- Version déterminée (pour l'upload) ---
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
            label = "progress"
        )

        Canvas(modifier = modifier) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Rail (fond)
            drawCircle(color = trackColor, style = stroke)
            // Progression
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = stroke
            )
        }
    } else {
        // --- Version indéterminée (le spinner qui tourne) ---
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = keyframes { durationMillis = 1000 },
                repeatMode = RepeatMode.Restart
            ),
            label = "angle"
        )

        Canvas(modifier = modifier) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(color = trackColor, style = stroke)
            drawArc(
                color = color,
                startAngle = angle,
                sweepAngle = 90f, // Longueur du trait qui tourne
                useCenter = false,
                style = stroke
            )
        }
    }
}