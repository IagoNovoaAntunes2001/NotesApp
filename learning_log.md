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

- A viewModel sobrevive as mudanças de configurações por ser salva no viewModelStore, no entanto
- não sobrevive ao processo de morte do aplicativo, ou seja, quando o sistema mata o processo para liberar recursos, a viewModel é destruída e perde seu estado
- Para salvar o estado da viewModel, podemos usar o SavedStateHandle, que é um objeto que permite salvar e restaurar o estado da viewModel em caso de morte do processo
- O SavedStateHandle é um mapa de chave-valor que pode ser usado para salvar dados simples, como strings, números, etc, e também pode ser usado para salvar objetos complexos, desde que sejam serializáveis
- Para usar o SavedStateHandle, basta injetá-lo na viewModel e usar os métodos set e get para salvar e recuperar os dados, respectivamente
- O ciclo de vida do SavedStateHandle é o mesmo da viewModel, ou seja, ele é criado quando a viewModel é criada e destruído quando a viewModel é destruída, garantindo que os dados sejam salvos e restaurados corretamente em caso de morte do processo
- O ciclo de vida da viewModel é o seguinte:
  - onCleared() é chamado quando a viewModel é destruíd apenas na morte do processo, ou seja, quando o sistema mata o processo para liberar recursos
  - onCleared() é o momento ideal para limpar recursos, cancelar coroutines, etc, para evitar vazamentos de memória e garantir que a viewModel seja destruída corretamente
