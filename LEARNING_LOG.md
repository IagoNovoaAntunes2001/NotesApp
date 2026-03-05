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
