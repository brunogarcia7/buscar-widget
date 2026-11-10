# Buscar Widget

Widget de tela inicial para Android inspirado no botão **"Buscar" do iOS**: uma
pílula de vidro (liquid glass) com uma lupa e o texto "Buscar". Ao tocar, abre o
app de busca que você escolher — **Pixel Search**, o **app Google**, ou qualquer
navegador instalado (Chrome, Brave, Firefox...) — de preferência já no campo de
pesquisa.

- Tela de configuração com **blur real** e efeito liquid glass.
- Vários **estilos de visual** para a pílula (vidro claro, vidro escuro, fosco,
  sólido).
- Lista de apps **dinâmica**: novos navegadores aparecem sozinhos.
- **Sem permissões. Sem internet. Sem coleta de dados.** Veja
  [SECURITY.md](SECURITY.md).

## Instalar (usuário final)

1. Baixe o APK mais recente na página de
   **[Releases](../../releases/latest)**.
2. No celular, abra o arquivo e permita "instalar de fontes desconhecidas" se
   for solicitado.
3. Segure a tela inicial → **Widgets** → **Buscar Widget** → arraste a pílula.
4. Abra o app uma vez para escolher o **visual** e o **app de busca**.

> Link direto do APK (depois de publicar um release):
> `https://github.com/SEU-USUARIO/buscar-widget/releases/latest`

## Compatibilidade

- **Android 7.0 (API 24) até Android 14 (API 34)** e mais recentes.
- Blur real da tela de config: **Android 12+**. Abaixo disso, vidro translúcido
  sem desfoque.

## Arquitetura

| Parte | Arquivo | Responsabilidade |
| --- | --- | --- |
| Widget | `res/layout/widget_buscar.xml` | Pílula: lupa + "Buscar" |
| Estilos | `WidgetStyle.kt` + `res/drawable/pill_*.xml` | Visuais da pílula |
| Provider | `BuscarWidgetProvider.kt` | Monta o widget e liga o clique |
| Clique | `SearchTrampolineActivity.kt` | Abre o app de busca (1º plano) |
| Busca | `AppRepository.kt` | Lista apps e monta o intent de busca |
| Config | `AppPickerActivity.kt` | Tela de seleção (liquid glass) |
| Preferências | `SearchConfig.kt` | Salva visual + app escolhido |
| Manifesto | `AndroidManifest.xml` | Tag `<queries>` + componentes |

### Por que uma Activity "trampolim"?

Lançar uma Activity direto de um `BroadcastReceiver` é bloqueado em segundo
plano no Android 10+. O clique do widget abre a `SearchTrampolineActivity`
(lançamento de primeiro plano), que resolve o intent e abre o app de busca sem
esse bloqueio.

## Compilar

Requer **JDK 17**. O projeto usa o Gradle Wrapper.

```bash
# APK de depuração (instala direto para teste)
./gradlew assembleDebug

# APK de release (otimizado com R8; assinado se houver keystore.properties)
./gradlew assembleRelease
```

Saídas:

- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## Assinatura de release

O build de release é assinado quando existe um arquivo `keystore.properties` na
raiz (ignorado pelo Git). Formato:

```properties
storeFile=buscar-widget-release.jks
storePassword=SUA_SENHA
keyAlias=SEU_ALIAS
keyPassword=SUA_SENHA
```

Sem esse arquivo, o release cai para a assinatura de debug (o projeto ainda
compila).

## Publicar no GitHub (release automático)

O workflow em `.github/workflows/release.yml` builda e publica o APK assinado
nos Releases quando você envia uma tag `vX.Y.Z`. Configure uma vez os
**Secrets** do repositório (Settings → Secrets and variables → Actions):

| Secret | Valor |
| --- | --- |
| `KEYSTORE_BASE64` | a keystore `.jks` em base64 |
| `KEYSTORE_PASSWORD` | senha do store |
| `KEY_ALIAS` | alias da chave |
| `KEY_PASSWORD` | senha da chave |

Depois:

```bash
git tag v1.0.0
git push origin v1.0.0
```

O APK aparece em `Releases` com o nome `BuscarWidget-v1.0.0.apk`.

## Licença

[MIT](LICENSE).
