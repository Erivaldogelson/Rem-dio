# Changelog

## 0.1.15 - 2026-05-04

- separa o fluxo da Now Bar em dois disparos: lembrete normal da dose e acompanhamento ao vivo após tocar em Tomei
- adiciona Foreground Service `DoseLiveService` com tipo `health` para manter a notificação contínua do tratamento
- mostra progresso do tratamento com doses tomadas, total de doses, próxima dose e porcentagem na Live Notification / Now Bar
- mantém `Notification.ProgressStyle` no Android 16/API 36 e fallback com `NotificationCompat.setProgress`
- corrige o gesto de voltar nas páginas internas das configurações para retornar ao menu de configurações

## 0.1.14 - 2026-05-04

- adiciona controle de transparência da pílula de navegação nas configurações de aparência
- persiste a transparência da navegação inferior nas preferências do usuário
- ajusta a barra inferior para respeitar área de navegação do sistema e animar cores/seleção com a nova transparência

## 0.1.13 - 2026-05-04

- redesenha a navegação inferior no estilo de pílula com três ícones centrais
- separa o botão de adicionar em um bloco rosa arredondado ao lado da pílula
- remove textos da barra inferior e destaca o item ativo com círculo lavanda

## 0.1.12 - 2026-04-27

- adiciona camada modular de seguranca com SecurePrefsManager, AuthRepository, SessionManager e ApiClient
- ativa R8/ProGuard e shrinkResources no release com regras para Retrofit, OkHttp, Gson, Firebase, Room e ML Kit
- adiciona HTTPS obrigatorio, certificate pinning configuravel, interceptor JWT e tratamento de sessao expirada
- adiciona verificacoes basicas de ambiente inseguro, assinatura de APK, root, emulador e debugger
- bloqueia arquivos sensiveis no .gitignore e separa configuracoes por ambiente via BuildConfig/local.properties/variaveis

## 0.1.11 - 2026-04-24

- corrige o onboarding aparecendo novamente ao abrir o app aguardando as configurações reais antes de decidir a rota inicial
- garante que a conclusão do onboarding seja salva antes de navegar para a tela principal
- reposiciona o título "Bem-vindo ao Remédios" mais abaixo para não ficar apertado sob a Nowbar/status bar

## 0.1.10 - 2026-04-24

- remove a recriação automática da Activity ao selecionar idioma para eliminar o pisca-pisca
- faz o seletor de idioma atualizar textos principais em Compose sem reiniciar o app
- repostagem imediata da Now Bar / Live Updates após mudar cor ou tonalidade

## 0.1.9 - 2026-04-24

- corrige definitivamente o pisca-pisca ao trocar idioma aguardando o DataStore carregar antes de aplicar a localidade
- reforça a Now Bar / Live Updates para publicar com cor padrão se a leitura das preferências ainda não estiver pronta

## 0.1.8 - 2026-04-24

- corrige loop de recriação da Activity ao aplicar idioma, evitando a tela piscar e impedir a abertura do app

## 0.1.7 - 2026-04-24

- corrige o tamanho do círculo de próxima dose em tablets, dobráveis e telas grandes
- limita e centraliza a navegação inferior para ficar em formato de pílula em telas largas
- adiciona configuração de cor e tonalidade da Now Bar / Live Updates
- adiciona seletor de idioma com opção Sistema e todos os idiomas disponíveis no Android
- aplica a localidade escolhida ao app em versões compatíveis do Android

## 0.1.6 - 2026-04-24

- Now Bar / Live Updates agora só aparecem para remédios ainda salvos e são canceladas ao apagar um remédio
- ícone do app ganhou camada monocromática para Material You / ícones temáticos sem distorcer a arte
- adicionados widgets nativos 2x2 e 4x2 com suporte a modo claro e escuro
- dashboard e navegação receberam limites responsivos para telas pequenas, tablets e dobráveis
- botão de voltar foi padronizado no estilo circular Material em todas as telas internas
- atualizações de progresso da Live Update ficaram menos frequentes e mais econômicas para reduzir consumo de bateria

## 0.1.5 - 2026-04-24

- corrigido crash ao tocar em editar um remédio salvo
- feedback tátil agora respeita a configuração e é aplicado em navegação, botões, cartões e links
- Now Bar / Live Updates agora mostra o nome do remédio no estado compacto e respeita o toggle ao vencer a dose
- tela de configurações reorganizada em menu principal com páginas internas de Aparência, Lembretes e Sobre
- botão de voltar nas telas internas ganhou formato circular no estilo das referências
- seção Sobre adicionada com desenvolvedor Erivaldo Gelson da Rocha João e links para Instagram, Threads e GitHub

## 0.1.4 - 2026-04-24

- Live Updates / Now Bar agora são iniciadas imediatamente após salvar um remédio com lembretes ativos
- navegação em pílula ganhou animação de seleção, escala de ícone e transição suave de largura
- tocar em Hoje na navegação inferior agora retorna corretamente para a tela inicial
- cadastro de medicamento agora permite escolher data de início e data de fim em calendário
- campo de horários permite editar múltiplos horários sem apagar entradas parciais
- lista Ver medicamentos ganhou botão para editar remédios salvos

## 0.1.3 - 2026-04-24

- adiciona modo claro com paleta própria, cards claros e textos ajustados pelo Material Theme
- configurações agora permitem escolher Claro, Escuro ou Auto
- corrige a navegação inferior para impedir texto vertical no item Novo/Adicionar
- mantém a barra em pílula compacta com labels em uma linha

## 0.1.2 - 2026-04-24

- logo atualizada para p?lula em efeito 3D sobre fundo branco
- Now Bar / Live Updates refor?ada com s?mbolo ?? e ativa??o por rem?dio
- navega??o inferior em p?lula agora inclui Ver rem?dios e Adicionar
- removido o bot?o de adicionar da tela Ver rem?dios
- cart?es de rem?dios salvos ganharam bot?o para apagar o cadastro e seus dados ligados
- temporizador de Pr?xima dose / Lembrete ativo agora progride em tempo real
- a??es Tomar dose, Adiar e Ignorar atualizam hist?rico, estoque, lembretes e pr?xima dose visivelmente

## 0.1.1 - 2026-04-24

- removidos os medicamentos de exemplo do primeiro uso e de instalações atualizadas
- ações de dose agora registram tomar, adiar e ignorar a partir da dose ativa ou próxima
- telas principais respeitam a área de status dos celulares em modo edge-to-edge
- configurações mantêm apenas controles acionáveis e acesso direto a permissões
- adicionada `MedicationLiveUpdateManager` com Android 16 `Notification.ProgressStyle`
- Live Updates iniciam até 15 minutos antes da dose, atualizam progresso e encerram ao tomar, adiar, ignorar ou expirar
- Android 15 ou inferior recebe fallback com notificação rica via `NotificationCompat`

## 0.1.0 - 2026-04-23

- criação do projeto Android nativo completo em Kotlin + Jetpack Compose
- dashboard premium com próxima dose, círculo de progresso e ações rápidas
- onboarding, histórico, configurações, permissões e lembrete ativo
- cadastro de medicamentos com imagem, OCR e scanner via CameraX
- Room Database, DataStore, repositórios e ViewModels
- notificações locais com ações rápidas, AlarmManager e WorkManager
- base para promoted ongoing notifications / Now Bar
- workflow de build/release por tag no GitHub
