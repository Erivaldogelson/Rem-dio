# Remédios

Aplicativo Android nativo para gestão de medicamentos com foco em lembretes confiáveis, registo de doses, OCR pela câmera e uma interface premium em Jetpack Compose.

Versão atual: `0.1.1`

## O que já está pronto no MVP

- Dashboard com foco na próxima dose, círculo de progresso e ações rápidas
- Fluxo de onboarding e splash com identidade visual premium
- Lista de medicamentos, detalhe e formulário de cadastro
- Captura de foto com câmera
- Seleção de imagem pela galeria / Photo Picker
- OCR com ML Kit para sugerir nome, dose e fabricante
- Scanner em tempo real com CameraX + OCR
- Histórico por período
- Configurações com base para tema, Material You, haptics e Live Updates
- Room Database + DataStore + ViewModel + Navigation Compose
- AlarmManager + WorkManager + notificações locais com ações rápidas
- Estrutura preparada para notificações promovidas / Now Bar em versões compatíveis
- Previews Compose e dados mockados para desenvolvimento

## Direção visual

O app foi desenhado para traduzir a linguagem das referências para o contexto de saúde:

- dark theme premium por padrão
- cartões grandes e suaves
- tipografia generosa
- paleta lilás / lavanda sobre fundo escuro
- navegação inferior em cápsula
- microinterações leves e feedback visual moderno

## Stack

- Kotlin
- Jetpack Compose
- Material 3 + dynamic color quando disponível
- Navigation Compose
- Room
- DataStore
- CameraX
- ML Kit Text Recognition
- WorkManager
- AlarmManager
- Notifications / promoted ongoing request

## Estrutura do projeto

```text
app/src/main/java/com/erivaldogelson/remedios
|- core/              # container da app e factory de ViewModel
|- data/
|  |- local/          # Room, entities, dao, relations
|  |- preferences/    # DataStore
|  |- repository/     # implementação dos repositórios
|- domain/
|  |- model/          # modelos do domínio e estados
|  |- repository/     # contratos
|- media/             # captura e persistência de imagem
|- notifications/     # scheduler, receivers, worker e notifier
|- ocr/               # parser OCR e analyzer CameraX
|- ui/
   |- components/     # componentes reutilizáveis
   |- navigation/     # rotas e NavHost
   |- screens/        # telas e previews
   |- theme/          # tokens visuais
   |- viewmodel/      # ViewModels de tela
```

## Build local

Pré-requisitos:

- Android Studio / SDK Android instalado
- Java 17+ disponível

Comandos:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

APKs locais gerados:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release-unsigned.apk`

## Release e versionamento

Fluxo configurado:

- a versão atual fica em `app/build.gradle.kts`
- alterações relevantes devem atualizar `versionCode`, `versionName` e `CHANGELOG.md`
- a workflow [`android-release.yml`](.github/workflows/android-release.yml) gera e assina um APK release versionado
- quando houver tag no formato `vX.Y.Z`, a workflow publica o APK no GitHub Release

Exemplo:

```bash
git tag v0.1.2
git push origin v0.1.2
```

## Live Updates / Now Bar

O projeto já inclui a base para lembretes promovidos:

- `NotificationCompat.Builder.setRequestPromotedOngoing(...)`
- `setShortCriticalText(...)`
- ações rápidas de tomar, adiar e pular
- fallback elegante via notificação rica quando a superfície promovida não estiver disponível

## Limitações atuais do MVP

- backup/restauração manual ainda não foi implementado, só a base estrutural
- a publicação automática no GitHub depende de push/tag no repositório remoto
- o APK release automatizado usa uma chave gerada no GitHub Actions; para distribuição contínua, o ideal é trocar por uma chave persistente via secrets
- a logo final em emoji 3D pode ser trocada por assets dedicados depois

## Próximos passos sugeridos

1. adicionar edição de medicamento já cadastrado
2. criar fluxo de backup/restauração com exportação
3. evoluir o scanner para confirmar múltiplos campos com mais heurísticas
4. configurar uma chave release persistente via GitHub Secrets
5. refinar Live Updates com progress styles específicos de versões compatíveis
