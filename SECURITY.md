# Segurança e privacidade

O Buscar Widget foi feito para ser inofensivo e transparente. Qualquer pessoa
pode auditar o código deste repositório.

## O que o app faz

- Mostra uma pílula "Buscar" na tela inicial.
- Ao tocar, abre o app de busca/navegador que você escolheu, de preferência
  já na barra de pesquisa.

## O que o app NÃO faz

- **Não pede nenhuma permissão.** Não há nenhuma tag `<uses-permission>` no
  `AndroidManifest.xml`.
- **Não acessa a internet.** Sem a permissão `INTERNET`, o app é incapaz de
  enviar qualquer dado para fora do aparelho.
- **Não coleta dados pessoais.** Não lê contatos, localização, arquivos,
  câmera, microfone ou histórico.
- **Não roda em segundo plano.** Não há serviços nem jobs agendados.

## Dados armazenados

A única informação salva é a sua escolha de **visual do widget** e de **app de
busca**, guardada em `SharedPreferences` no armazenamento privado do próprio
app. Esses dados nunca saem do aparelho e são apagados ao desinstalar.

## Visibilidade de pacotes (`<queries>`)

O manifesto declara uma tag `<queries>` para conseguir **listar** os
navegadores e apps de busca instalados (exigência do Android 11+). Isso é
apenas leitura: permite descobrir que esses apps existem para montar a lista de
seleção. Não concede nenhum acesso aos dados deles.

## Assinatura

Os APKs publicados em Releases são assinados com uma chave de release. A chave
privada e as senhas **não** fazem parte deste repositório.

## Reportar um problema

Abra uma issue descrevendo o comportamento observado.
