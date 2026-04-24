# Changelog

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
