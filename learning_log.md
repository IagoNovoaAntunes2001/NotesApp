# LEARNING LOG — StaffNotes

---

## Mês 1 | Semana 1 | Segunda-feira

- UI Layer = ViewModel + UI State + UI Elements
  - ViewModel é responsável por fornecer os dados para UI(Screen), 
  - sendo um stateHolder e expondo um stateFlow/LiveData com um Ui State
- ViewModel expõe StateFlow, UI coleta com collectAsStateWithLifecycle
- UI State deve ser imutável (data class)
- UI State deve conter apenas os dados necessários para a UI, 
  - não deve conter lógica de negócios ou dados desnecessários

## Mês 1 | Semana 1 | Terça-feira

- Domain Layer = Use-Cases + Entity + Repository Interface
    - Use-Cases são responsáveis por executar a lógica de negócios, 
    - Entity é a representação dos dados do domínio, 
    - Repository Interface é a abstração para acessar os dados
    - ViewModel(Ui-Layer) -> Use-Cases(Domain-Layer) -> Repository Interface(Domain-Layer)
        - Repository Interface é implementada na Data Layer

## Mês 1 | Semana 1 | Quarta-feira

- Data Layer = Repository Implementation + Data SOurce
  - Repository implementation é responsável por implementar a lógica de acesso aos dados e decidir a fonte deles
  - Data Source é responsável por acessar os dados, seja de uma API, banco de dados local, etc

## Mês 1 | Semana 1 | Quinta-feira

- A UI deve ser completamente desacoplada das regras e mudanças, delegando para a viewModel
  - UDF (Unidirectional Data Flow) é um padrão de arquitetura onde os dados fluem em uma única direção, 
    - UI -> ViewModel -> Ui State -> UI
  - Isso garante que a UI seja reativa e responda às mudanças de estado

## Mês 1 | Semana 1 | Sexta-feira

- Clean Architecture é uma abordagem de design de software que promove a separação de responsabilidades e a independência entre as camadas do aplicativo
  - As camadas são: UI Layer, Domain Layer e Data Layer
  - Cada camada tem suas responsabilidades e dependências bem definidas, 
    - UI Layer depende do Domain Layer, 
    - Domain Layer depende do Data Layer, lembrando que este, não conhece o data layer, ele olha apenas o contrato
    - Data Layer é independente das outras camadas
    - Segue o UDF (Unidirectional Data Flow) para garantir que os dados fluam em uma única direção, 
      - UI -> ViewModel -> Ui State -> UI

## Mês 1 | Semana 2 | Segunda-feira

- MVI Archutecture (Model-View-Intent) é um padrão de arquitetura que promove a separação de responsabilidades e a reatividade da UI
  - Model representa o estado da UI, 
  - View é responsável por exibir os dados e interagir com o usuário, 
  - Intent representa as ações do usuário e as intenções de mudança de estado
  - O fluxo de dados é unidirecional: 
    - View -> Intent -> Model -> View
  - Isso garante que a UI seja reativa e responda às mudanças de estado, 
    - além de facilitar a manutenção e a testabilidade do código
    - a grande diferença entre MVI e MVVM é que no MVI, a UI é completamente reativa e depende do estado, mandando intenções para a viewModel,
    - enquanto no MVVM, a UI pode ter lógica de negócios e não depende completamente do estado, podendo chamar métodos da viewModel diretamente

## Mês 1 | Semana 2 | Terça-feira

- Side-Effect é uma ação que ocorre como resultado de uma mudança de estado, 
  - pode ser uma navegação, exibição de um toast, etc
  - deve ser tratado de forma separada do estado da UI, 
    - para evitar que a UI fique acoplada a lógica de negócios e para garantir que a UI seja reativa e responda às mudanças de estado
  - pode ser tratado com um canal ou um flow separado do stateFlow do Ui State, 
    - para garantir que a UI possa coletar os side-effects de forma reativa e independente do estado da UI
  - Fluxo correto MVI + Side-Effects:
    - Intent → ViewModel → (State + Side-Effect)
    - State (Flow): dados persistentes que a UI observa sempre
    - Side-Effect (Channel): ações únicas que a UI consome e descarta

## Mês 1 | Semana 2 | Quarta-feira

- A viewModel vive no ViewModelStore, que é retido pelo sistema durante config changes (rotação, mudança de tema, etc)
  - ela **não morre com a Activity** em config changes — é exatamente para isso que ela existe
  - ela morre quando a Activity **realmente termina** (usuário pressiona back, `finish()` é chamado) ou com process death
- Process death ocorre quando o sistema mata o processo do app para liberar recursos (Low Memory Kill)
  - nesse caso a viewModel é destruída e perde todo o seu estado em memória
- Para sobreviver ao process death, usamos o SavedStateHandle
  - ele é um mapa chave-valor que serializa os dados num **Bundle**
  - esse Bundle é mantido pelo **processo do sistema** (ActivityManagerService) na RAM do sistema, **não em disco**
  - por isso tem limitações: aceita apenas tipos primitivos, Strings e Parcelables, com limite de ~500KB
  - quando o usuário retorna ao app após um process death, o sistema restaura o Bundle e o SavedStateHandle é recriado com os dados anteriores
  - diferença importante: SavedStateHandle ≠ persistência em disco (Room, DataStore) — se o dispositivo reiniciar sem o app ter sido backgrounded antes, os dados são perdidos

## Mês 1 | Semana 2 | Quarta-feira/Sexta-feira

- Usamos o MVI com StateFlow para o estado da UI e Channel para os side-effects, garantindo um fluxo unidirecional de dados e uma UI reativa
- A viewModel é responsável por gerenciar o estado da UI e os side-effects, garantindo que a UI seja desacoplada da lógica de negócios e responda às mudanças de estado de forma
- Os side-effects são tratados de forma separada do estado da UI, garantindo que a UI seja reativa e responda às mudanças de estado, além de facilitar a manutenção e a testabilidade do código
- O SavedStateHandle é usado para salvar o estado da viewModel em caso de morte do processo, garantindo que os dados sejam restaurados corretamente quando o aplicativo for reaberto
- Os dados do savedStateHandle são restaurados automaticamente quando a viewModel é recriada após a morte do processo, garantindo que a UI seja restaurada ao estado anterior sem necessidade de lógica adicional para lidar com isso