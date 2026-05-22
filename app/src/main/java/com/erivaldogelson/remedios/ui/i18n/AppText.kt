package com.erivaldogelson.remedios.ui.i18n

import androidx.compose.runtime.staticCompositionLocalOf
import com.erivaldogelson.remedios.domain.model.DoseStatus
import com.erivaldogelson.remedios.domain.model.HistoryFilter
import com.erivaldogelson.remedios.domain.model.MedicationForm
import java.util.Locale

data class AppText(
    val common: CommonText,
    val nav: NavText,
    val dashboard: DashboardText,
    val history: HistoryText,
    val medication: MedicationText,
    val settings: SettingsText,
    val onboarding: OnboardingText,
    val permissions: PermissionsText,
    val activeReminder: ActiveReminderText,
    val components: ComponentsText,
) {
    fun medicationFormLabel(form: MedicationForm): String = when (form) {
        MedicationForm.TABLET -> medication.formTablet
        MedicationForm.CAPSULE -> medication.formCapsule
        MedicationForm.SYRUP -> medication.formSyrup
        MedicationForm.INJECTION -> medication.formInjection
        MedicationForm.DROPS -> medication.formDrops
        MedicationForm.CREAM -> medication.formCream
        MedicationForm.OTHER -> medication.formOther
    }

    fun doseStatusLabel(status: DoseStatus): String = when (status) {
        DoseStatus.TAKEN -> components.statusTaken
        DoseStatus.SNOOZED -> components.statusSnoozed
        DoseStatus.SKIPPED -> components.statusSkipped
        DoseStatus.MISSED -> components.statusMissed
        DoseStatus.UPCOMING -> components.statusUpcoming
    }

    fun historyFilterLabel(filter: HistoryFilter): String = when (filter) {
        HistoryFilter.DAY -> history.day
        HistoryFilter.WEEK -> history.week
        HistoryFilter.MONTH -> history.month
    }
}

data class CommonText(
    val appName: String,
    val back: String,
    val system: String,
    val useSystemLanguage: String,
    val choose: String,
    val confirm: String,
    val cancel: String,
    val noEnd: String,
    val takeDose: String,
    val snooze: String,
    val skip: String,
    val editSavedMedication: String,
    val deleteSavedMedication: String,
)

data class NavText(
    val today: String,
    val medications: String,
    val newItem: String,
    val history: String,
    val settings: String,
)

data class DashboardText(
    val nextDoseTitle: String,
    val activeDoseTitle: String,
    val intro: String,
    val next: String,
    val emptyTitle: String,
    val emptyMessage: String,
    val dosesToday: String,
    val pending: String,
    val activeReminder: String,
    val viewMedications: String,
)

data class HistoryText(
    val title: String,
    val emptyTitle: String,
    val emptyMessage: String,
    val day: String,
    val week: String,
    val month: String,
)

data class MedicationText(
    val title: String,
    val listSubtitle: String,
    val emptyTitle: String,
    val emptyMessage: String,
    val addTitle: String,
    val editTitle: String,
    val formIntro: String,
    val nameLabel: String,
    val doseLabel: String,
    val quantityLabel: String,
    val frequencyLabel: String,
    val timesLabel: String,
    val startLabel: String,
    val endLabel: String,
    val manufacturerLabel: String,
    val instructionsLabel: String,
    val notesLabel: String,
    val saving: String,
    val update: String,
    val save: String,
    val imageTitle: String,
    val imageDescription: String,
    val noPhotoYet: String,
    val analyzingImage: String,
    val camera: String,
    val gallery: String,
    val scan: String,
    val details: String,
    val schedulesPrefix: String,
    val instructionsPrefix: String,
    val notesPrefix: String,
    val quantityRemainingPrefix: String,
    val unavailableTitle: String,
    val unavailableMessage: String,
    val scanTitle: String,
    val liveReading: String,
    val pointCamera: String,
    val detectedDosePrefix: String,
    val useReading: String,
    val formTablet: String,
    val formCapsule: String,
    val formSyrup: String,
    val formInjection: String,
    val formDrops: String,
    val formCream: String,
    val formOther: String,
)

data class SettingsText(
    val settings: String,
    val settingsIntro: String,
    val remediesPlusSubtitle: String,
    val reminders: String,
    val remindersSubtitle: String,
    val remindersSubpageSubtitle: String,
    val appearance: String,
    val appearanceSubtitle: String,
    val about: String,
    val aboutSubtitle: String,
    val language: String,
    val chooseLanguage: String,
    val managePermissions: String,
    val theme: String,
    val themeAuto: String,
    val themeLight: String,
    val themeDark: String,
    val dynamicColors: String,
    val dynamicColorsSubtitle: String,
    val pillTransparency: String,
    val nowBarLiveUpdates: String,
    val nowBarLiveUpdatesSubtitle: String,
    val nowBarColor: String,
    val nowBarTone: String,
    val hapticFeedback: String,
    val hapticFeedbackSubtitle: String,
    val permissions: String,
    val permissionsSubtitle: String,
    val developer: String,
)

data class OnboardingText(
    val splashSubtitle: String,
    val welcome: String,
    val page1Title: String,
    val page1Description: String,
    val page2Title: String,
    val page2Description: String,
    val page3Title: String,
    val page3Description: String,
    val continueText: String,
    val startNow: String,
)

data class PermissionsText(
    val title: String,
    val cameraTitle: String,
    val cameraMessage: String,
    val allowCamera: String,
    val notificationsTitle: String,
    val notificationsMessage: String,
    val allowNotifications: String,
)

data class ActiveReminderText(
    val title: String,
    val emptyTitle: String,
    val emptyMessage: String,
    val activeDose: String,
    val registrationWindow: String,
    val registrationWindowMessage: String,
)

data class ComponentsText(
    val nextPrefix: String,
    val noUpcomingDoses: String,
    val ocrSuggestions: String,
    val namePrefix: String,
    val dosePrefix: String,
    val labPrefix: String,
    val applySuggestions: String,
    val statusTaken: String,
    val statusSnoozed: String,
    val statusSkipped: String,
    val statusMissed: String,
    val statusUpcoming: String,
)

data class LanguageOption(
    val tag: String,
    val title: String,
    val subtitle: String,
)

val LocalAppText = staticCompositionLocalOf { appTextFor("pt-BR") }

val supportedLanguageOptions = listOf(
    LanguageOption("system", "Sistema", "Usar idioma do sistema"),
    LanguageOption("en", "Inglês", "English"),
    LanguageOption("pt-PT", "Português de Portugal", "Portugal"),
    LanguageOption("pt-BR", "Português do Brasil", "Brasil"),
    LanguageOption("pt-AO", "Português de Angola", "Angola"),
    LanguageOption("es", "Espanhol", "Español"),
    LanguageOption("fr", "Francês", "Français"),
    LanguageOption("zh-CN", "Chinês", "中文"),
    LanguageOption("ja-JP", "Japonês", "日本語"),
)

fun appTextFor(languageTag: String): AppText {
    val locale = resolvedLocale(languageTag)
    return when {
        locale.language == "en" -> englishText
        locale.language == "pt" && locale.country == "PT" -> portugalText
        locale.language == "pt" && locale.country == "AO" -> angolaText
        locale.language == "es" -> spanishText
        locale.language == "fr" -> frenchText
        locale.language == "zh" -> chineseText
        locale.language == "ja" -> japaneseText
        else -> brazilText
    }
}

fun languageSubtitle(languageTag: String, text: AppText = appTextFor(languageTag)): String =
    if (languageTag == "system") {
        text.common.useSystemLanguage
    } else {
        supportedLanguageOptions.firstOrNull { it.tag == languageTag }?.title
            ?: Locale.forLanguageTag(languageTag).getDisplayName(Locale.forLanguageTag(languageTag))
    }

private fun resolvedLocale(languageTag: String): Locale =
    if (languageTag == "system") Locale.getDefault() else Locale.forLanguageTag(languageTag)

private val brazilText = AppText(
    common = CommonText(
        appName = "Remédios",
        back = "Voltar",
        system = "Sistema",
        useSystemLanguage = "Usar idioma do sistema",
        choose = "Escolher",
        confirm = "Confirmar",
        cancel = "Cancelar",
        noEnd = "Sem fim",
        takeDose = "Tomar dose",
        snooze = "Adiar",
        skip = "Ignorar",
        editSavedMedication = "Editar remédio salvo",
        deleteSavedMedication = "Apagar remédio salvo",
    ),
    nav = NavText("Hoje", "Remédios", "Novo", "Histórico", "Config."),
    dashboard = DashboardText(
        nextDoseTitle = "Próxima dose",
        activeDoseTitle = "Dose em andamento",
        intro = "Tudo o que importa para a próxima medicação, num lugar só.",
        next = "Próximo",
        emptyTitle = "Nenhuma dose próxima",
        emptyMessage = "Adicione seu primeiro medicamento para começar a receber lembretes, histórico e atualizações ao vivo.",
        dosesToday = "Doses hoje",
        pending = "Pendentes",
        activeReminder = "Lembrete ativo",
        viewMedications = "Ver medicamentos",
    ),
    history = HistoryText(
        title = "Histórico",
        emptyTitle = "Ainda sem registros",
        emptyMessage = "Quando você marcar doses como tomadas, adiadas ou perdidas, tudo aparece aqui.",
        day = "Dia",
        week = "Semana",
        month = "Mês",
    ),
    medication = MedicationText(
        title = "Medicamentos",
        listSubtitle = "Seu arsenal diário com imagem, dose, horários e histórico.",
        emptyTitle = "Nenhum remédio cadastrado",
        emptyMessage = "Adicione o primeiro medicamento para começar a organizar doses, fotos e lembretes.",
        addTitle = "Adicionar medicamento",
        editTitle = "Editar medicamento",
        formIntro = "Cadastre com visual, contexto e horários claros.",
        nameLabel = "Nome do remédio",
        doseLabel = "Dose",
        quantityLabel = "Qtde restante",
        frequencyLabel = "Frequência",
        timesLabel = "Horários (ex: 08:00, 20:00)",
        startLabel = "Início",
        endLabel = "Fim",
        manufacturerLabel = "Laboratório / fabricante",
        instructionsLabel = "Instruções",
        notesLabel = "Observações",
        saving = "Salvando...",
        update = "Atualizar medicamento",
        save = "Salvar medicamento",
        imageTitle = "Foto do remédio",
        imageDescription = "Imagem do remédio",
        noPhotoYet = "Sem foto ainda",
        analyzingImage = "Analisando imagem e OCR...",
        camera = "Câmera",
        gallery = "Galeria",
        scan = "Escanear",
        details = "Detalhes",
        schedulesPrefix = "Horários",
        instructionsPrefix = "Instruções",
        notesPrefix = "Observações",
        quantityRemainingPrefix = "Quantidade restante",
        unavailableTitle = "Detalhes indisponíveis",
        unavailableMessage = "Não foi possível carregar este medicamento.",
        scanTitle = "Escanear remédio",
        liveReading = "Leitura em tempo real",
        pointCamera = "Aponte a câmera para a embalagem",
        detectedDosePrefix = "Dose detectada",
        useReading = "Usar esta leitura",
        formTablet = "Comprimido",
        formCapsule = "Cápsula",
        formSyrup = "Xarope",
        formInjection = "Injeção",
        formDrops = "Gotas",
        formCream = "Creme",
        formOther = "Outro",
    ),
    settings = SettingsText(
        settings = "Configurações",
        settingsIntro = "Menu organizado para aparência, lembretes, Now Bar e informações do app.",
        remediesPlusSubtitle = "Ativação por remédio e Now Bar",
        reminders = "Lembretes",
        remindersSubtitle = "Live Updates, feedback tátil e permissões",
        remindersSubpageSubtitle = "Now Bar, feedback e permissões",
        appearance = "Aparência",
        appearanceSubtitle = "Tema claro, escuro e cores dinâmicas",
        about = "Sobre",
        aboutSubtitle = "Desenvolvedor e redes sociais",
        language = "Idioma",
        chooseLanguage = "Escolha o idioma",
        managePermissions = "Gerenciar permissões",
        theme = "Tema",
        themeAuto = "Auto",
        themeLight = "Claro",
        themeDark = "Escuro",
        dynamicColors = "Cores dinâmicas",
        dynamicColorsSubtitle = "Adapta detalhes do app às cores do dispositivo quando disponível.",
        pillTransparency = "Transparência da pílula",
        nowBarLiveUpdates = "Now Bar / Live Updates",
        nowBarLiveUpdatesSubtitle = "Só ativa quando existir remédio salvo com próxima dose.",
        nowBarColor = "Cor da Now Bar",
        nowBarTone = "Tonalidade",
        hapticFeedback = "Feedback tátil",
        hapticFeedbackSubtitle = "Aplica vibração suave em botões, navegação e cartões.",
        permissions = "Permissões",
        permissionsSubtitle = "Câmera, notificações e alarmes exatos.",
        developer = "Desenvolvedor",
    ),
    onboarding = OnboardingText(
        splashSubtitle = "Cuidado calmo, bonito e preciso para cada dose.",
        welcome = "Bem-vindo ao Remédios",
        page1Title = "Registre seus remédios com clareza",
        page1Description = "Adicione manualmente, organize horários e use cores para identificar cada tratamento com calma.",
        page2Title = "Escaneie pela câmera",
        page2Description = "Use OCR para sugerir nome, dose e laboratório a partir da embalagem sem digitação cansativa.",
        page3Title = "Lembretes vivos e histórico elegante",
        page3Description = "Acompanhe próximas doses, ações rápidas e uma base pronta para Live Updates e Now Bar no Android 16.",
        continueText = "Continuar",
        startNow = "Começar agora",
    ),
    permissions = PermissionsText(
        title = "Permissões",
        cameraTitle = "Câmera",
        cameraMessage = "Necessária para tirar foto do remédio e escanear a embalagem com OCR.",
        allowCamera = "Permitir câmera",
        notificationsTitle = "Notificações",
        notificationsMessage = "Essenciais para lembretes locais, ações rápidas e Live Updates compatíveis.",
        allowNotifications = "Permitir notificações",
    ),
    activeReminder = ActiveReminderText(
        title = "Lembrete ativo",
        emptyTitle = "Nenhum lembrete ativo",
        emptyMessage = "Quando um horário entrar em andamento, ele aparece aqui com ações rápidas.",
        activeDose = "Dose em andamento",
        registrationWindow = "Janela de registro",
        registrationWindowMessage = "Restam %1\$d min para registrar. Ativa até %2\$s com ações rápidas e Now Bar quando compatível.",
    ),
    components = ComponentsText(
        nextPrefix = "Próximo",
        noUpcomingDoses = "Sem doses próximas",
        ocrSuggestions = "Sugestões do OCR",
        namePrefix = "Nome",
        dosePrefix = "Dose",
        labPrefix = "Lab",
        applySuggestions = "Aplicar sugestões",
        statusTaken = "Tomado",
        statusSnoozed = "Adiado",
        statusSkipped = "Ignorado",
        statusMissed = "Perdido",
        statusUpcoming = "Próximo",
    ),
)

private val portugalText = brazilText.copy(
    common = brazilText.common.copy(appName = "Remédios"),
    nav = NavText("Hoje", "Remédios", "Novo", "Histórico", "Defin."),
    dashboard = brazilText.dashboard.copy(
        intro = "Tudo o que importa para a próxima medicação, num só lugar.",
        emptyMessage = "Adicione o primeiro medicamento para começar a receber lembretes, histórico e atualizações ao vivo.",
    ),
    history = brazilText.history.copy(emptyMessage = "Quando marcar doses como tomadas, adiadas ou perdidas, tudo aparece aqui."),
    medication = brazilText.medication.copy(
        listSubtitle = "O seu conjunto diário com imagem, dose, horários e histórico.",
        formIntro = "Registe com imagem, contexto e horários claros.",
        quantityLabel = "Qtd. restante",
        save = "Guardar medicamento",
        saving = "A guardar...",
        noPhotoYet = "Ainda sem fotografia",
        scan = "Digitalizar",
        scanTitle = "Digitalizar remédio",
        pointCamera = "Aponte a câmara para a embalagem",
    ),
    settings = brazilText.settings.copy(
        settings = "Definições",
        settingsIntro = "Menu organizado para aspeto, lembretes, Now Bar e informações da aplicação.",
        appearance = "Aspeto",
        appearanceSubtitle = "Tema claro, escuro e cores dinâmicas",
        chooseLanguage = "Escolher idioma",
        managePermissions = "Gerir permissões",
        themeLight = "Claro",
        themeDark = "Escuro",
        dynamicColorsSubtitle = "Adapta detalhes da aplicação às cores do dispositivo quando disponível.",
    ),
    onboarding = brazilText.onboarding.copy(
        splashSubtitle = "Cuidado calmo, bonito e preciso para cada dose.",
        welcome = "Bem-vindo ao Remédios",
        page1Title = "Registe os seus remédios com clareza",
        page2Title = "Digitalize com a câmara",
        startNow = "Começar agora",
    ),
)

private val angolaText = portugalText.copy(
    nav = NavText("Hoje", "Remédios", "Novo", "Histórico", "Defin."),
    settings = portugalText.settings.copy(
        settingsIntro = "Menu organizado para aparência, lembretes, Now Bar e informações da aplicação.",
        appearance = "Aparência",
    ),
)

private val englishText = brazilText.copy(
    common = CommonText("Remedies", "Back", "System", "Use system language", "Choose", "Confirm", "Cancel", "No end", "Take dose", "Snooze", "Skip", "Edit saved medicine", "Delete saved medicine"),
    nav = NavText("Today", "Meds", "New", "History", "Settings"),
    dashboard = DashboardText("Next dose", "Dose in progress", "Everything that matters for your next medication, all in one place.", "Next", "No upcoming dose", "Add your first medicine to start receiving reminders, history, and live updates.", "Doses today", "Pending", "Active reminder", "View medicines"),
    history = HistoryText("History", "No records yet", "When you mark doses as taken, snoozed, or skipped, everything appears here.", "Day", "Week", "Month"),
    medication = MedicationText("Medicines", "Your daily set with image, dose, times, and history.", "No medicines saved", "Add the first medicine to organize doses, photos, and reminders.", "Add medicine", "Edit medicine", "Save with clear image, context, and times.", "Medicine name", "Dose", "Qty remaining", "Frequency", "Times (e.g. 08:00, 20:00)", "Start", "End", "Lab / manufacturer", "Instructions", "Notes", "Saving...", "Update medicine", "Save medicine", "Medicine photo", "Medicine image", "No photo yet", "Analyzing image and OCR...", "Camera", "Gallery", "Scan", "Details", "Times", "Instructions", "Notes", "Quantity remaining", "Details unavailable", "Could not load this medicine.", "Scan medicine", "Live reading", "Point the camera at the package", "Detected dose", "Use this reading", "Tablet", "Capsule", "Syrup", "Injection", "Drops", "Cream", "Other"),
    settings = SettingsText("Settings", "Organized menu for appearance, reminders, Now Bar, and app information.", "Per-medicine activation and Now Bar", "Reminders", "Live Updates, haptic feedback, and permissions", "Now Bar, feedback, and permissions", "Appearance", "Light theme, dark theme, and dynamic colors", "About", "Developer and social links", "Language", "Choose language", "Manage permissions", "Theme", "Auto", "Light", "Dark", "Dynamic colors", "Adapts app details to device colors when available.", "Pill transparency", "Now Bar / Live Updates", "Only activates when a saved medicine has a next dose.", "Now Bar color", "Tone", "Haptic feedback", "Applies gentle vibration to buttons, navigation, and cards.", "Permissions", "Camera, notifications, and exact alarms.", "Developer"),
    onboarding = OnboardingText("Calm, beautiful, and precise care for every dose.", "Welcome to Remedies", "Register your medicines clearly", "Add manually, organize times, and use colors to identify each treatment calmly.", "Scan with the camera", "Use OCR to suggest name, dose, and lab from the package without tiring typing.", "Live reminders and elegant history", "Track next doses, quick actions, and a base ready for Live Updates and Now Bar on Android 16.", "Continue", "Start now"),
    permissions = PermissionsText("Permissions", "Camera", "Needed to photograph medicine and scan the package with OCR.", "Allow camera", "Notifications", "Essential for local reminders, quick actions, and compatible Live Updates.", "Allow notifications"),
    activeReminder = ActiveReminderText("Active reminder", "No active reminder", "When a scheduled time is in progress, it appears here with quick actions.", "Dose in progress", "Registration window", "%1\$d min left to register. Active until %2\$s with quick actions and Now Bar when compatible."),
    components = ComponentsText("Next", "No upcoming doses", "OCR suggestions", "Name", "Dose", "Lab", "Apply suggestions", "Taken", "Snoozed", "Skipped", "Missed", "Upcoming"),
)

private val spanishText = brazilText.copy(
    common = CommonText("Medicinas", "Volver", "Sistema", "Usar idioma del sistema", "Elegir", "Confirmar", "Cancelar", "Sin fin", "Tomar dosis", "Posponer", "Ignorar", "Editar medicina guardada", "Borrar medicina guardada"),
    nav = NavText("Hoy", "Medic.", "Nuevo", "Historial", "Ajustes"),
    dashboard = DashboardText("Próxima dosis", "Dosis en curso", "Todo lo importante para la próxima medicación, en un solo lugar.", "Próximo", "No hay dosis próximas", "Añade tu primera medicina para recibir recordatorios, historial y actualizaciones en vivo.", "Dosis hoy", "Pendientes", "Recordatorio activo", "Ver medicinas"),
    history = HistoryText("Historial", "Aún no hay registros", "Cuando marques dosis como tomadas, pospuestas o ignoradas, aparecerán aquí.", "Día", "Semana", "Mes"),
    medication = MedicationText("Medicinas", "Tu conjunto diario con imagen, dosis, horarios e historial.", "No hay medicinas guardadas", "Añade la primera medicina para organizar dosis, fotos y recordatorios.", "Añadir medicina", "Editar medicina", "Registra con imagen, contexto y horarios claros.", "Nombre de la medicina", "Dosis", "Cant. restante", "Frecuencia", "Horarios (ej.: 08:00, 20:00)", "Inicio", "Fin", "Laboratorio / fabricante", "Instrucciones", "Notas", "Guardando...", "Actualizar medicina", "Guardar medicina", "Foto de la medicina", "Imagen de la medicina", "Aún sin foto", "Analizando imagen y OCR...", "Cámara", "Galería", "Escanear", "Detalles", "Horarios", "Instrucciones", "Notas", "Cantidad restante", "Detalles no disponibles", "No fue posible cargar esta medicina.", "Escanear medicina", "Lectura en tiempo real", "Apunta la cámara al envase", "Dosis detectada", "Usar esta lectura", "Tableta", "Cápsula", "Jarabe", "Inyección", "Gotas", "Crema", "Otro"),
    settings = SettingsText("Ajustes", "Menú organizado para apariencia, recordatorios, Now Bar e información de la app.", "Activación por medicina y Now Bar", "Recordatorios", "Live Updates, respuesta háptica y permisos", "Now Bar, respuesta y permisos", "Apariencia", "Tema claro, oscuro y colores dinámicos", "Acerca de", "Desarrollador y redes sociales", "Idioma", "Elegir idioma", "Gestionar permisos", "Tema", "Auto", "Claro", "Oscuro", "Colores dinámicos", "Adapta detalles de la app a los colores del dispositivo cuando esté disponible.", "Transparencia de la píldora", "Now Bar / Live Updates", "Solo se activa cuando hay una medicina guardada con próxima dosis.", "Color de Now Bar", "Tonalidad", "Respuesta háptica", "Aplica una vibración suave en botones, navegación y tarjetas.", "Permisos", "Cámara, notificaciones y alarmas exactas.", "Desarrollador"),
    onboarding = OnboardingText("Cuidado tranquilo, bonito y preciso para cada dosis.", "Bienvenido a Medicinas", "Registra tus medicinas con claridad", "Añade manualmente, organiza horarios y usa colores para identificar cada tratamiento con calma.", "Escanea con la cámara", "Usa OCR para sugerir nombre, dosis y laboratorio desde el envase sin escribir de más.", "Recordatorios vivos e historial elegante", "Sigue próximas dosis, acciones rápidas y una base lista para Live Updates y Now Bar en Android 16.", "Continuar", "Empezar ahora"),
    permissions = PermissionsText("Permisos", "Cámara", "Necesaria para fotografiar la medicina y escanear el envase con OCR.", "Permitir cámara", "Notificaciones", "Esenciales para recordatorios locales, acciones rápidas y Live Updates compatibles.", "Permitir notificaciones"),
    activeReminder = ActiveReminderText("Recordatorio activo", "No hay recordatorio activo", "Cuando un horario esté en curso, aparecerá aquí con acciones rápidas.", "Dosis en curso", "Ventana de registro", "Quedan %1\$d min para registrar. Activa hasta %2\$s con acciones rápidas y Now Bar cuando sea compatible."),
    components = ComponentsText("Próximo", "Sin dosis próximas", "Sugerencias de OCR", "Nombre", "Dosis", "Lab", "Aplicar sugerencias", "Tomada", "Pospuesta", "Ignorada", "Perdida", "Próxima"),
)

private val frenchText = brazilText.copy(
    common = CommonText("Médicaments", "Retour", "Système", "Utiliser la langue du système", "Choisir", "Confirmer", "Annuler", "Sans fin", "Prendre la dose", "Reporter", "Ignorer", "Modifier le médicament enregistré", "Supprimer le médicament enregistré"),
    nav = NavText("Auj.", "Médic.", "Nouv.", "Hist.", "Régl."),
    dashboard = DashboardText("Prochaine dose", "Dose en cours", "Tout ce qui compte pour la prochaine prise, au même endroit.", "Prochain", "Aucune dose à venir", "Ajoutez votre premier médicament pour recevoir des rappels, un historique et des mises à jour en direct.", "Doses aujourd’hui", "En attente", "Rappel actif", "Voir les médicaments"),
    history = HistoryText("Historique", "Aucun enregistrement", "Lorsque vous marquez des doses comme prises, reportées ou ignorées, elles apparaissent ici.", "Jour", "Semaine", "Mois"),
    medication = MedicationText("Médicaments", "Votre liste quotidienne avec image, dose, horaires et historique.", "Aucun médicament enregistré", "Ajoutez le premier médicament pour organiser doses, photos et rappels.", "Ajouter un médicament", "Modifier le médicament", "Enregistrez avec image, contexte et horaires clairs.", "Nom du médicament", "Dose", "Qté restante", "Fréquence", "Horaires (ex. 08:00, 20:00)", "Début", "Fin", "Laboratoire / fabricant", "Instructions", "Notes", "Enregistrement...", "Mettre à jour", "Enregistrer", "Photo du médicament", "Image du médicament", "Pas encore de photo", "Analyse de l’image et OCR...", "Caméra", "Galerie", "Scanner", "Détails", "Horaires", "Instructions", "Notes", "Quantité restante", "Détails indisponibles", "Impossible de charger ce médicament.", "Scanner le médicament", "Lecture en temps réel", "Pointez la caméra vers l’emballage", "Dose détectée", "Utiliser cette lecture", "Comprimé", "Capsule", "Sirop", "Injection", "Gouttes", "Crème", "Autre"),
    settings = SettingsText("Réglages", "Menu organisé pour l’apparence, les rappels, la Now Bar et les informations de l’app.", "Activation par médicament et Now Bar", "Rappels", "Live Updates, retour haptique et permissions", "Now Bar, retour et permissions", "Apparence", "Thème clair, sombre et couleurs dynamiques", "À propos", "Développeur et réseaux sociaux", "Langue", "Choisir la langue", "Gérer les permissions", "Thème", "Auto", "Clair", "Sombre", "Couleurs dynamiques", "Adapte les détails de l’app aux couleurs de l’appareil si disponible.", "Transparence de la pilule", "Now Bar / Live Updates", "S’active uniquement lorsqu’un médicament enregistré a une prochaine dose.", "Couleur de la Now Bar", "Tonalité", "Retour haptique", "Applique une vibration douce aux boutons, à la navigation et aux cartes.", "Permissions", "Caméra, notifications et alarmes exactes.", "Développeur"),
    onboarding = OnboardingText("Un soin calme, beau et précis pour chaque dose.", "Bienvenue dans Médicaments", "Enregistrez vos médicaments clairement", "Ajoutez manuellement, organisez les horaires et utilisez les couleurs pour identifier chaque traitement.", "Scannez avec la caméra", "Utilisez l’OCR pour suggérer le nom, la dose et le laboratoire depuis l’emballage.", "Rappels vivants et historique élégant", "Suivez les prochaines doses, les actions rapides et une base prête pour Live Updates et Now Bar sur Android 16.", "Continuer", "Commencer"),
    permissions = PermissionsText("Permissions", "Caméra", "Nécessaire pour photographier le médicament et scanner l’emballage avec OCR.", "Autoriser la caméra", "Notifications", "Essentielles pour les rappels locaux, les actions rapides et les Live Updates compatibles.", "Autoriser les notifications"),
    activeReminder = ActiveReminderText("Rappel actif", "Aucun rappel actif", "Lorsqu’un horaire est en cours, il apparaît ici avec des actions rapides.", "Dose en cours", "Fenêtre d’enregistrement", "Il reste %1\$d min pour enregistrer. Actif jusqu’à %2\$s avec actions rapides et Now Bar si compatible."),
    components = ComponentsText("Prochain", "Aucune dose à venir", "Suggestions OCR", "Nom", "Dose", "Lab", "Appliquer les suggestions", "Pris", "Reporté", "Ignoré", "Manqué", "À venir"),
)

private val chineseText = brazilText.copy(
    common = CommonText("药品", "返回", "系统", "使用系统语言", "选择", "确认", "取消", "无结束日期", "服药", "稍后提醒", "忽略", "编辑已保存药品", "删除已保存药品"),
    nav = NavText("今天", "药品", "新增", "记录", "设置"),
    dashboard = DashboardText("下一剂", "剂量进行中", "下一次用药的重要信息都在这里。", "下一个", "没有即将到来的剂量", "添加第一个药品，开始接收提醒、历史记录和实时更新。", "今日剂量", "待处理", "活动提醒", "查看药品"),
    history = HistoryText("历史记录", "暂无记录", "标记已服用、稍后提醒或忽略的剂量会显示在这里。", "日", "周", "月"),
    medication = MedicationText("药品", "包含图片、剂量、时间和历史的每日清单。", "没有已保存药品", "添加第一个药品来整理剂量、照片和提醒。", "添加药品", "编辑药品", "用清晰的图片、说明和时间保存。", "药品名称", "剂量", "剩余数量", "频率", "时间（例：08:00, 20:00）", "开始", "结束", "实验室 / 制造商", "说明", "备注", "正在保存...", "更新药品", "保存药品", "药品照片", "药品图片", "暂无照片", "正在分析图片和 OCR...", "相机", "图库", "扫描", "详情", "时间", "说明", "备注", "剩余数量", "详情不可用", "无法加载此药品。", "扫描药品", "实时读取", "将相机对准包装", "检测到的剂量", "使用此读取结果", "片剂", "胶囊", "糖浆", "注射", "滴剂", "乳膏", "其他"),
    settings = SettingsText("设置", "用于外观、提醒、Now Bar 和应用信息的整理菜单。", "按药品启用和 Now Bar", "提醒", "Live Updates、触觉反馈和权限", "Now Bar、反馈和权限", "外观", "浅色主题、深色主题和动态颜色", "关于", "开发者和社交链接", "语言", "选择语言", "管理权限", "主题", "自动", "浅色", "深色", "动态颜色", "可用时根据设备颜色调整应用细节。", "胶囊栏透明度", "Now Bar / Live Updates", "仅在已保存药品有下一剂时启用。", "Now Bar 颜色", "色调", "触觉反馈", "为按钮、导航和卡片应用轻微振动。", "权限", "相机、通知和精确闹钟。", "开发者"),
    onboarding = OnboardingText("为每一剂提供安静、美观且精准的照护。", "欢迎使用药品", "清晰记录你的药品", "手动添加、整理时间，并用颜色轻松识别每个治疗。", "用相机扫描", "使用 OCR 从包装中建议名称、剂量和实验室。", "实时提醒和优雅历史", "跟踪下一剂、快速操作，并为 Android 16 的 Live Updates 和 Now Bar 做好准备。", "继续", "立即开始"),
    permissions = PermissionsText("权限", "相机", "用于拍摄药品并通过 OCR 扫描包装。", "允许相机", "通知", "本地提醒、快速操作和兼容 Live Updates 所必需。", "允许通知"),
    activeReminder = ActiveReminderText("活动提醒", "没有活动提醒", "当某个时间正在进行时，会在这里显示快速操作。", "剂量进行中", "记录窗口", "还剩 %1\$d 分钟可记录。有效至 %2\$s，兼容时支持快速操作和 Now Bar。"),
    components = ComponentsText("下一个", "没有即将到来的剂量", "OCR 建议", "名称", "剂量", "实验室", "应用建议", "已服用", "已推迟", "已忽略", "错过", "即将到来"),
)

private val japaneseText = brazilText.copy(
    common = CommonText("薬", "戻る", "システム", "システムの言語を使用", "選択", "確認", "キャンセル", "終了日なし", "服用する", "延期", "無視", "保存済みの薬を編集", "保存済みの薬を削除"),
    nav = NavText("今日", "薬", "追加", "履歴", "設定"),
    dashboard = DashboardText("次の服用", "服用中", "次の服用に必要な情報をひとつの場所にまとめました。", "次", "次の服用はありません", "最初の薬を追加して、リマインダー、履歴、ライブ更新を受け取りましょう。", "今日の服用", "未完了", "有効なリマインダー", "薬を見る"),
    history = HistoryText("履歴", "まだ記録がありません", "服用、延期、無視した記録がここに表示されます。", "日", "週", "月"),
    medication = MedicationText("薬", "画像、用量、時間、履歴を含む毎日のリスト。", "保存された薬はありません", "最初の薬を追加して、用量、写真、リマインダーを整理しましょう。", "薬を追加", "薬を編集", "画像、情報、時間を分かりやすく登録します。", "薬の名前", "用量", "残り数量", "頻度", "時間（例：08:00, 20:00）", "開始", "終了", "研究所 / メーカー", "指示", "メモ", "保存中...", "薬を更新", "薬を保存", "薬の写真", "薬の画像", "まだ写真がありません", "画像と OCR を解析中...", "カメラ", "ギャラリー", "スキャン", "詳細", "時間", "指示", "メモ", "残り数量", "詳細を表示できません", "この薬を読み込めませんでした。", "薬をスキャン", "リアルタイム読み取り", "カメラをパッケージに向けてください", "検出された用量", "この読み取りを使用", "錠剤", "カプセル", "シロップ", "注射", "点滴", "クリーム", "その他"),
    settings = SettingsText("設定", "外観、リマインダー、Now Bar、アプリ情報を整理したメニューです。", "薬ごとの有効化と Now Bar", "リマインダー", "Live Updates、触覚フィードバック、権限", "Now Bar、フィードバック、権限", "外観", "ライトテーマ、ダークテーマ、動的カラー", "このアプリについて", "開発者とSNSリンク", "言語", "言語を選択", "権限を管理", "テーマ", "自動", "ライト", "ダーク", "動的カラー", "利用可能な場合、端末の色に合わせてアプリを調整します。", "ピルの透明度", "Now Bar / Live Updates", "保存済みの薬に次の服用がある場合のみ有効になります。", "Now Bar の色", "色調", "触覚フィードバック", "ボタン、ナビゲーション、カードに軽い振動を適用します。", "権限", "カメラ、通知、正確なアラーム。", "開発者"),
    onboarding = OnboardingText("すべての服用に、静かで美しく正確なケアを。", "薬へようこそ", "薬を分かりやすく登録", "手動で追加し、時間を整理し、色で治療を識別できます。", "カメラでスキャン", "OCR でパッケージから名前、用量、研究所を提案します。", "ライブリマインダーと美しい履歴", "次の服用、クイック操作、Android 16 の Live Updates と Now Bar に対応する基盤を追跡します。", "続ける", "今すぐ開始"),
    permissions = PermissionsText("権限", "カメラ", "薬を撮影し、OCR でパッケージをスキャンするために必要です。", "カメラを許可", "通知", "ローカルリマインダー、クイック操作、対応する Live Updates に必要です。", "通知を許可"),
    activeReminder = ActiveReminderText("有効なリマインダー", "有効なリマインダーはありません", "時間が進行中になると、ここにクイック操作が表示されます。", "服用中", "記録ウィンドウ", "記録まで残り %1\$d 分。%2\$s まで有効で、対応時はクイック操作と Now Bar が使えます。"),
    components = ComponentsText("次", "次の服用はありません", "OCR の提案", "名前", "用量", "研究所", "提案を適用", "服用済み", "延期", "無視", "未服用", "予定"),
)
