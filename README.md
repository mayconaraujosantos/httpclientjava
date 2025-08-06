```sql
-- Tabela principal de trava de recebíveis
CREATE TABLE card_receivables_lock (
    id VARCHAR(26) NOT NULL,
    hub_guarantee_id VARCHAR(26) NOT NULL,
    contract_number VARCHAR(50) NOT NULL,
    contract_source VARCHAR(50) NOT NULL,
    ipoc VARCHAR(50) NOT NULL,
    register VARCHAR(20) NOT NULL,
    owner_person_id VARCHAR(26) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    recalculation_frequency VARCHAR(20) NOT NULL,
    consider_balance_on_insufficiency BOOLEAN NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

-- Tabela de titulares
CREATE TABLE card_receivables_holder (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    person_id VARCHAR(26) NOT NULL,
    tax_id VARCHAR(14) NOT NULL,
    name VARCHAR(255) NOT NULL,
    root_tax_id_operation VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Tabela de entrada Nuclea (1:1)
CREATE TABLE card_receivables_lock_nuclea (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    protocol VARCHAR(50),
    creation_retry_attempts INTEGER NOT NULL DEFAULT 0,
    proactive_search_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id),
    UNIQUE (card_receivables_lock_id)
);

-- Tabela de entrada Cerc (1:1)
CREATE TABLE card_receivables_lock_cerc (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    protocol VARCHAR(50),
    creation_retry_attempts INTEGER NOT NULL DEFAULT 0,
    proactive_search_attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id),
    UNIQUE (card_receivables_lock_id)
);

-- Tabela de parcelas do contrato
CREATE TABLE card_receivables_contract_installment (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    installment_number INTEGER NOT NULL,
    due_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Índices para melhorar performance
CREATE INDEX idx_card_receivables_lock_hub_guarantee_id ON card_receivables_lock (hub_guarantee_id);

CREATE INDEX idx_card_receivables_lock_contract_number ON card_receivables_lock (contract_number);

CREATE INDEX idx_card_receivables_lock_status ON card_receivables_lock (status);

```

```kotlin
package com.finapp.domain.tables

import com.finapp.domain.enums.CardReceivablesLockStatus
import com.finapp.domain.enums.RecalculationPeriodIndicator
import com.finapp.domain.enums.RegisterType
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock")
data class CardReceivablesLockEntity(
        @Id val id: String,
        val hubGuaranteeId: String,
        val contractNumber: String,
        val contractSource: String,
        val ipoc: String,
        @Enumerated(EnumType.STRING) val register: RegisterType,
        val ownerPersonId: String,
        val amount: java.math.BigDecimal,
        @Enumerated(EnumType.STRING) val recalculationFrequency: RecalculationPeriodIndicator,
        val considerBalanceOnInsufficiency: Boolean,
        val startDate: LocalDate,
        val endDate: LocalDate,
        @Enumerated(EnumType.STRING) var status: CardReceivablesLockStatus,
        var createdAt: LocalDateTime,
        var updatedAt: LocalDateTime,
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        var holders: MutableList<CardReceivablesHolderEntity>,
        @OneToOne(mappedBy = "cardReceivablesLock", cascade = [CascadeType.ALL])
        var nucleaEntry: CardReceivablesLockNucleaEntity?,
        @OneToOne(mappedBy = "cardReceivablesLock", cascade = [CascadeType.ALL])
        var cercEntry: CardReceivablesLockCercEntity?,
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        var contractInstallments: MutableList<CardReceivablesContractInstallmentEntity>
) {

  // Helper methods for bidirectional relationships
  fun addHolder(holder: CardReceivablesHolderEntity) {
    holders.add(holder)
    holder.cardReceivablesLock = this
  }

  fun removeHolder(holder: CardReceivablesHolderEntity) {
    holders.remove(holder)
    holder.cardReceivablesLock = null
  }

  fun addContractInstallment(installment: CardReceivablesContractInstallmentEntity) {
    contractInstallments.add(installment)
    installment.cardReceivablesLock = this
  }

  fun removeContractInstallment(installment: CardReceivablesContractInstallmentEntity) {
    contractInstallments.remove(installment)
    installment.cardReceivablesLock = null
  }

  // Helper methods for OneToOne relationships
  fun assignNucleaEntry(nuclea: CardReceivablesLockNucleaEntity?) {
    // Clear previous bidirectional relationship
    this.nucleaEntry?.cardReceivablesLock = null
    this.nucleaEntry = nuclea
    nuclea?.cardReceivablesLock = this
  }

  fun assignCercEntry(cerc: CardReceivablesLockCercEntity) {
    this.cercEntry = cerc
    cerc.cardReceivablesLock = this
  }

  fun requireCercEntry(): CardReceivablesLockCercEntity {
    return cercEntry ?: throw IllegalStateException("CercEntry is required but not set")
  }

  // Helper methods for status control (Soft Delete)
  fun activate() {
    this.status = CardReceivablesLockStatus.ACTIVE
    this.updatedAt = LocalDateTime.now()
  }

  fun deactivate() {
    this.status = CardReceivablesLockStatus.INACTIVE
    this.updatedAt = LocalDateTime.now()
  }

  fun isActive(): Boolean = this.status == CardReceivablesLockStatus.ACTIVE

  fun isInactive(): Boolean = this.status == CardReceivablesLockStatus.INACTIVE
}
```

```kotlin
@Entity
@Table(name = "card_receivables_holder")
data class CardReceivablesHolderEntity(
        @Id val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLockEntity?,
        val taxId: String,
        val rootTaxIdOperation: String,
        val paymentAccountBranch: String?,
        val paymentAccountNumber: String?,
        val paymentAccountId: String?
)

```

```kotlin
@Entity
@Table(name = "card_receivables_contract_installment")
data class CardReceivablesContractInstallmentEntity(
        @Id val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLockEntity?,
        val installmentNumber: Int,
        val date: LocalDate,
        val value: BigDecimal
)

```

```kotlin
@Entity
@Table(name = "card_receivables_lock_nuclea")
data class CardReceivablesLockNucleaEntity(
        @Id val id: String,
        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id")
        var cardReceivablesLock: CardReceivablesLockEntity?,
        var protocol: String?,
        var creationRetryAttempts: Int,
        var proactiveSearchAttempts: Int,
        var createdAt: LocalDateTime,
        var updatedAt: LocalDateTime
)

```

```kotlin
@Entity
@Table(name = "card_receivables_lock_cerc")
data class CardReceivablesLockCercEntity(
  @Id
  val id: String,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "card_receivables_lock_id")
  var cardReceivablesLock: CardReceivablesLockEntity?,

  var protocol: String?,

  var creationRetryAttempts: Int,

  var proactiveSearchAttempts: Int,

  var createdAt: LocalDateTime,

  var updatedAt: LocalDateTime
)

```

```kotlin
data class CardReceivablesLock(
        val id: String,
        val hubGuaranteeId: String,
        val contractNumber: String,
        val contractSource: String,
        val ipoc: String,
        val register: String,
        val ownerPersonId: String,
        val amount: BigDecimal,
        val recalculationFrequency: String,
        val considerBalanceOnInsufficiency: Boolean,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val status: CardReceivablesLockStatus,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val holders: List<CardReceivablesHolder> = emptyList(),
        val nucleaEntries: List<CardReceivablesLockNuclea> = emptyList(),
        val cercEntries: List<CardReceivablesLockCerc> = emptyList(),
        val contractInstallments: List<CardReceivablesContractInstallment> = emptyList()
)

```

```kotlin
data class CardReceivablesHolder(
        val id: String,
        val taxId: String,
        val rootTaxIdOperation: String,
        val paymentAccountBranch: String? = null,
        val paymentAccountNumber: String? = null,
        val paymentAccountId: String? = null
)

```

```kotlin
data class CardReceivablesLockNuclea(
        val id: String,
        val creationRetryAttempts: Int = 0,
        val proactiveSearchAttempts: Int = 0,
        val protocol: String? = null
)

```

```kotlin
data class CardReceivablesLockCerc(
        val id: String,
        val creationRetryAttempts: Int = 0,
        val proactiveSearchAttempts: Int = 0
)

```

```kotlin
data class CardReceivablesContractInstallment(
        val id: String,
        val installmentNumber: Int,
        val date: LocalDate,
        val value: BigDecimal
)

```

```kotlin
interface CardReceivablesLockDataAccess {
  fun create(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto
  fun update(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto
  fun getById(id: String): CardReceivablesLockDto?
  fun getByIdWithRelationships(id: String): CardReceivablesLockDto?
  fun getByContractNumber(contractNumber: String): CardReceivablesLockDto
  fun getByStatus(status: CardReceivablesLockStatus): List<CardReceivablesLockDto>
  fun getByContractNumberAndStatus(
          contractNumber: String,
          status: CardReceivablesLockStatus
  ): CardReceivablesLockDto?
  fun getByHubGuaranteeIdAndStatus(
          hubGuaranteeId: String,
          status: CardReceivablesLockStatus
  ): List<CardReceivablesLockDto>
  fun incrementCreationRetryAttempts(id: String, registerType: RegisterType)
  fun incrementProactiveSearchAttempts(id: String, registerType: RegisterType)
}

```

```kotlin

@Component
class CardReceivablesLockDataAccessImpl(
        private val cardReceivablesLockRepository: CardReceivablesLockRepository,
        private val cardReceivablesLockNucleaRepository: CardReceivablesLockNucleaRepository,
        private val cardReceivablesLockCercRepository: CardReceivablesLockCercRepository
) : CardReceivablesLockDataAccess {

  override fun create(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto {
    val cardReceivablesLock = CardReceivablesLockMapper.map(cardReceivablesLockDto)
    val savedCardReceivablesLock = cardReceivablesLockRepository.save(cardReceivablesLock)
    return CardReceivablesLockMapper.map(savedCardReceivablesLock)
  }

  override fun update(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto {
    val cardReceivablesLock = CardReceivablesLockMapper.map(cardReceivablesLockDto)
    val updatedCardReceivablesLock = cardReceivablesLockRepository.save(cardReceivablesLock)
    return CardReceivablesLockMapper.map(updatedCardReceivablesLock)
  }

  override fun getById(id: String): CardReceivablesLockDto? {
    val cardReceivablesLock = cardReceivablesLockRepository.findById(id).orElse(null)
    return cardReceivablesLock?.let { CardReceivablesLockMapper.map(it) }
  }

  override fun getByIdWithRelationships(id: String): CardReceivablesLockDto? {
    val cardReceivablesLock = cardReceivablesLockRepository.findByIdWithRelationships(id)
    return cardReceivablesLock?.let { CardReceivablesLockMapper.map(it) }
  }

  override fun getByContractNumber(contractNumber: String): CardReceivablesLockDto {
    val cardReceivablesLock = cardReceivablesLockRepository.findByContractNumber(contractNumber)
    return CardReceivablesLockMapper.map(cardReceivablesLock)
  }

  override fun getByStatus(status: CardReceivablesLockStatus): List<CardReceivablesLockDto> {
    val cardReceivablesLocks = cardReceivablesLockRepository.findByStatus(status)
    return cardReceivablesLocks.map { CardReceivablesLockMapper.map(it) }
  }

  override fun getByContractNumberAndStatus(
          contractNumber: String,
          status: CardReceivablesLockStatus
  ): CardReceivablesLockDto? {
    val cardReceivablesLock =
            cardReceivablesLockRepository.findByContractNumberAndStatus(contractNumber, status)
    return cardReceivablesLock?.let { CardReceivablesLockMapper.map(it) }
  }

  override fun getByHubGuaranteeIdAndStatus(
          hubGuaranteeId: String,
          status: CardReceivablesLockStatus
  ): List<CardReceivablesLockDto> {
    val cardReceivablesLocks =
            cardReceivablesLockRepository.findByHubGuaranteeIdAndStatus(hubGuaranteeId, status)
    return cardReceivablesLocks.map { CardReceivablesLockMapper.map(it) }
  }

  override fun incrementCreationRetryAttempts(id: String, registerType: RegisterType) {
    executeRepositoryOperation(registerType) { repository ->
      repository.incrementCreationRetryAttempts(id)
    }
  }

  override fun incrementProactiveSearchAttempts(id: String, registerType: RegisterType) {
    executeRepositoryOperation(registerType) { repository ->
      repository.incrementProactiveSearchAttempts(id)
    }
  }

  private fun executeRepositoryOperation(
          registerType: RegisterType,
          operation: (RepositoryOperations) -> Unit
  ) {
    when (registerType) {
      RegisterType.CERC -> operation(cardReceivablesLockCercRepository)
      RegisterType.NUCLEA -> operation(cardReceivablesLockNucleaRepository)
    }
  }
}

```

```kotlin
interface RepositoryOperations {
  fun incrementCreationRetryAttempts(id: String)
  fun incrementProactiveSearchAttempts(id: String)
}
```

```kotlin
@Repository
interface CardReceivablesLockRepository : JpaRepository<CardReceivablesLockEntity, String> {

  @Query(
          """
        SELECT c FROM CardReceivablesLockEntity c
        LEFT JOIN FETCH c.holders
        LEFT JOIN FETCH c.nucleaEntry
        LEFT JOIN FETCH c.cercEntry
        LEFT JOIN FETCH c.contractInstallments
        WHERE c.id = :id
    """
  )
  fun findByIdWithRelationships(@Param("id") id: String): CardReceivablesLockEntity?

  // Buscar por contract_number simples
  fun findByContractNumber(contractNumber: String): CardReceivablesLockEntity

  // Buscar por contract_number com relacionamentos
  @Query(
          """
        SELECT c FROM CardReceivablesLockEntity c
        LEFT JOIN FETCH c.holders
        LEFT JOIN FETCH c.nucleaEntry
        LEFT JOIN FETCH c.cercEntry
        LEFT JOIN FETCH c.contractInstallments
        WHERE c.contractNumber = :contractNumber
    """
  )
  fun findByContractNumberWithRelationships(
          @Param("contractNumber") contractNumber: String
  ): CardReceivablesLockEntity?

  // Buscar por contract_number apenas se estiver ativo
  fun findByContractNumberAndStatus(
          contractNumber: String,
          status: CardReceivablesLockStatus
  ): CardReceivablesLockEntity?

  // Buscar por contract_number com relacionamentos e status ativo
  @Query(
          """
        SELECT c FROM CardReceivablesLockEntity c
        LEFT JOIN FETCH c.holders
        LEFT JOIN FETCH c.nucleaEntry
        LEFT JOIN FETCH c.cercEntry
        LEFT JOIN FETCH c.contractInstallments
        WHERE c.contractNumber = :contractNumber AND c.status = :status
    """
  )
  fun findByContractNumberAndStatusWithRelationships(
          @Param("contractNumber") contractNumber: String,
          @Param("status") status: CardReceivablesLockStatus
  ): CardReceivablesLockEntity?

  // Buscar apenas registros ativos
  fun findByStatus(status: CardReceivablesLockStatus): List<CardReceivablesLockEntity>

  // Buscar por ID apenas se estiver ativo
  fun findByIdAndStatus(id: String, status: CardReceivablesLockStatus): CardReceivablesLockEntity?

  // Buscar todos os ativos
  fun findByStatusOrderByCreatedAtDesc(
          status: CardReceivablesLockStatus
  ): List<CardReceivablesLockEntity>

  // Buscar por hub guarantee id apenas se estiver ativo
  fun findByHubGuaranteeIdAndStatus(
          hubGuaranteeId: String,
          status: CardReceivablesLockStatus
  ): List<CardReceivablesLockEntity>
}

```

```kotlin

@Repository
interface CardReceivablesLockNucleaRepository :
        JpaRepository<CardReceivablesLockNucleaEntity, String>, RepositoryOperations {

  fun findByCardReceivablesLockId(cardReceivablesLockId: String): CardReceivablesLockNucleaEntity?

  fun findByProtocol(protocol: String): List<CardReceivablesLockNucleaEntity>

  @Modifying
  @Query(
          "UPDATE CardReceivablesLockNucleaEntity c SET c.creationRetryAttempts = c.creationRetryAttempts + 1, c.updatedAt = CURRENT_TIMESTAMP WHERE c.cardReceivablesLock.id = :id"
  )
  override fun incrementCreationRetryAttempts(@Param("id") id: String)

  @Modifying
  @Query(
          "UPDATE CardReceivablesLockNucleaEntity c SET c.proactiveSearchAttempts = c.proactiveSearchAttempts + 1, c.updatedAt = CURRENT_TIMESTAMP WHERE c.cardReceivablesLock.id = :id"
  )
  override fun incrementProactiveSearchAttempts(@Param("id") id: String)

  @Modifying
  @Query(
          "UPDATE CardReceivablesLockNucleaEntity c SET c.protocol = :protocol, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id"
  )
  fun updateProtocol(@Param("id") id: String, @Param("protocol") protocol: String)
}

```

```kotlin
@Repository
interface CardReceivablesLockCercRepository :
        JpaRepository<CardReceivablesLockCercEntity, String>, RepositoryOperations {

  fun findByCardReceivablesLockId(cardReceivablesLockId: String): CardReceivablesLockCercEntity?

  @Modifying
  @Query(
          "UPDATE CardReceivablesLockCercEntity c SET c.creationRetryAttempts = c.creationRetryAttempts + 1, c.updatedAt = CURRENT_TIMESTAMP WHERE c.cardReceivablesLock.id = :id"
  )
  override fun incrementCreationRetryAttempts(@Param("id") id: String)

  @Modifying
  @Query(
          "UPDATE CardReceivablesLockCercEntity c SET c.proactiveSearchAttempts = c.proactiveSearchAttempts + 1, c.updatedAt = CURRENT_TIMESTAMP WHERE c.cardReceivablesLock.id = :id"
  )
  override fun incrementProactiveSearchAttempts(@Param("id") id: String)
}

```

```kotlin

class CardReceivablesLockDataAccessImplTest {

  private lateinit var cardReceivablesLockRepository: CardReceivablesLockRepository
  private lateinit var cardReceivablesLockNucleaRepository: CardReceivablesLockNucleaRepository
  private lateinit var cardReceivablesLockCercRepository: CardReceivablesLockCercRepository
  private lateinit var dataAccess: CardReceivablesLockDataAccessImpl

  @BeforeEach
  fun `set up`() {
    cardReceivablesLockRepository = mockk()
    cardReceivablesLockNucleaRepository = mockk()
    cardReceivablesLockCercRepository = mockk()

    dataAccess =
            CardReceivablesLockDataAccessImpl(
                    cardReceivablesLockRepository,
                    cardReceivablesLockNucleaRepository,
                    cardReceivablesLockCercRepository
            )
  }

  @Test
  fun `should create card receivables lock successfully`() {
    // Given
    val dto = createTestDto()
    val savedEntity = createTestEntity()

    every { cardReceivablesLockRepository.save(any()) } returns savedEntity

    // When
    val result = dataAccess.create(dto)

    // Then
    verify { cardReceivablesLockRepository.save(any()) }
  }

  @Test
  fun `should update card receivables lock successfully`() {
    // Given
    val dto = createTestDto()
    val updatedEntity = createTestEntity()

    every { cardReceivablesLockRepository.save(any()) } returns updatedEntity

    // When
    val result = dataAccess.update(dto)

    // Then
    verify { cardReceivablesLockRepository.save(any()) }
  }

  @Test
  fun `should get by id when entity exists`() {
    // Given
    val id = "test-id-123456789012345678901234"
    val entity = createTestEntity()

    every { cardReceivablesLockRepository.findById(id) } returns java.util.Optional.of(entity)

    // When
    val result = dataAccess.getById(id)

    // Then
    verify { cardReceivablesLockRepository.findById(id) }
    assert(result != null)
  }

  @Test
  fun `should return null when getting by id and entity does not exist`() {
    // Given
    val id = "non-existent-id"

    every { cardReceivablesLockRepository.findById(id) } returns java.util.Optional.empty()

    // When
    val result = dataAccess.getById(id)

    // Then
    verify { cardReceivablesLockRepository.findById(id) }
    assert(result == null)
  }

  @Test
  fun `should get by id with relationships when entity exists`() {
    // Given
    val id = "test-id-123456789012345678901234"
    val entity = createTestEntity()

    every { cardReceivablesLockRepository.findByIdWithRelationships(id) } returns entity

    // When
    val result = dataAccess.getByIdWithRelationships(id)

    // Then
    verify { cardReceivablesLockRepository.findByIdWithRelationships(id) }
    assert(result != null)
  }

  @Test
  fun `should return null when getting by id with relationships and entity does not exist`() {
    // Given
    val id = "non-existent-id"

    every { cardReceivablesLockRepository.findByIdWithRelationships(id) } returns null

    // When
    val result = dataAccess.getByIdWithRelationships(id)

    // Then
    verify { cardReceivablesLockRepository.findByIdWithRelationships(id) }
    assert(result == null)
  }

  @Test
  fun `should get by contract number when entity exists`() {
    // Given
    val contractNumber = "CONTRACT123"
    val entity = createTestEntity()

    every { cardReceivablesLockRepository.findByContractNumber(contractNumber) } returns entity

    // When
    val result = dataAccess.getByContractNumber(contractNumber)

    // Then
    verify { cardReceivablesLockRepository.findByContractNumber(contractNumber) }
    assert(result != null)
    assert(result.contractNumber == contractNumber)
  }

  @Test
  fun `should throw exception when getting by contract number and entity does not exist`() {
    // Given
    val contractNumber = "NON-EXISTENT"

    every { cardReceivablesLockRepository.findByContractNumber(contractNumber) } throws
            org.springframework.dao.EmptyResultDataAccessException(1)

    // When & Then
    org.junit.jupiter.api.assertThrows<org.springframework.dao.EmptyResultDataAccessException> {
      dataAccess.getByContractNumber(contractNumber)
    }
    verify { cardReceivablesLockRepository.findByContractNumber(contractNumber) }
  }

  @Test
  fun `should increment creation retry attempts for CERC`() {
    // Given
    val id = "test-id"
    val registerType = RegisterType.CERC

    every { cardReceivablesLockCercRepository.incrementCreationRetryAttempts(id) } returns Unit

    // When
    dataAccess.incrementCreationRetryAttempts(id, registerType)

    // Then
    verify { cardReceivablesLockCercRepository.incrementCreationRetryAttempts(id) }
  }

  @Test
  fun `should increment creation retry attempts for NUCLEA`() {
    // Given
    val id = "test-id"
    val registerType = RegisterType.NUCLEA

    every { cardReceivablesLockNucleaRepository.incrementCreationRetryAttempts(id) } returns Unit

    // When
    dataAccess.incrementCreationRetryAttempts(id, registerType)

    // Then
    verify { cardReceivablesLockNucleaRepository.incrementCreationRetryAttempts(id) }
  }

  @Test
  fun `should increment proactive search attempts for CERC`() {
    // Given
    val id = "test-id"
    val registerType = RegisterType.CERC

    every { cardReceivablesLockCercRepository.incrementProactiveSearchAttempts(id) } returns Unit

    // When
    dataAccess.incrementProactiveSearchAttempts(id, registerType)

    // Then
    verify { cardReceivablesLockCercRepository.incrementProactiveSearchAttempts(id) }
  }

  @Test
  fun `should increment proactive search attempts for NUCLEA`() {
    // Given
    val id = "test-id"
    val registerType = RegisterType.NUCLEA

    every { cardReceivablesLockNucleaRepository.incrementProactiveSearchAttempts(id) } returns Unit

    // When
    dataAccess.incrementProactiveSearchAttempts(id, registerType)

    // Then
    verify { cardReceivablesLockNucleaRepository.incrementProactiveSearchAttempts(id) }
  }

  @Test
  fun `should get by status when entities exist`() {
    // Given
    val status = CardReceivablesLockStatus.ACTIVE
    val entities = listOf(createTestEntity(), createTestEntity())

    every { cardReceivablesLockRepository.findByStatus(status) } returns entities

    // When
    val result = dataAccess.getByStatus(status)

    // Then
    verify { cardReceivablesLockRepository.findByStatus(status) }
    assert(result.size == 2)
    assert(result.all { it.status == status })
  }

  @Test
  fun `should return empty list when getting by status and no entities exist`() {
    // Given
    val status = CardReceivablesLockStatus.INACTIVE

    every { cardReceivablesLockRepository.findByStatus(status) } returns emptyList()

    // When
    val result = dataAccess.getByStatus(status)

    // Then
    verify { cardReceivablesLockRepository.findByStatus(status) }
    assert(result.isEmpty())
  }

  @Test
  fun `should get by contract number and status when entity exists`() {
    // Given
    val contractNumber = "CONTRACT123"
    val status = CardReceivablesLockStatus.ACTIVE
    val entity = createTestEntity()

    every {
      cardReceivablesLockRepository.findByContractNumberAndStatus(contractNumber, status)
    } returns entity

    // When
    val result = dataAccess.getByContractNumberAndStatus(contractNumber, status)

    // Then
    verify { cardReceivablesLockRepository.findByContractNumberAndStatus(contractNumber, status) }
    assert(result != null)
    assert(result!!.contractNumber == contractNumber)
    assert(result.status == status)
  }

  @Test
  fun `should return null when getting by contract number and status and entity does not exist`() {
    // Given
    val contractNumber = "NON-EXISTENT"
    val status = CardReceivablesLockStatus.ACTIVE

    every {
      cardReceivablesLockRepository.findByContractNumberAndStatus(contractNumber, status)
    } returns null

    // When
    val result = dataAccess.getByContractNumberAndStatus(contractNumber, status)

    // Then
    verify { cardReceivablesLockRepository.findByContractNumberAndStatus(contractNumber, status) }
    assert(result == null)
  }

  @Test
  fun `should get by hub guarantee id and status when entities exist`() {
    // Given
    val hubGuaranteeId = "HUB123"
    val status = CardReceivablesLockStatus.ACTIVE
    val entities = listOf(createTestEntity(), createTestEntity())

    every {
      cardReceivablesLockRepository.findByHubGuaranteeIdAndStatus(hubGuaranteeId, status)
    } returns entities

    // When
    val result = dataAccess.getByHubGuaranteeIdAndStatus(hubGuaranteeId, status)

    // Then
    verify { cardReceivablesLockRepository.findByHubGuaranteeIdAndStatus(hubGuaranteeId, status) }
    assert(result.size == 2)
    assert(result.all { it.status == status })
  }

  @Test
  fun `should return empty list when getting by hub guarantee id and status and no entities exist`() {
    // Given
    val hubGuaranteeId = "HUB456"
    val status = CardReceivablesLockStatus.INACTIVE

    every {
      cardReceivablesLockRepository.findByHubGuaranteeIdAndStatus(hubGuaranteeId, status)
    } returns emptyList()

    // When
    val result = dataAccess.getByHubGuaranteeIdAndStatus(hubGuaranteeId, status)

    // Then
    verify { cardReceivablesLockRepository.findByHubGuaranteeIdAndStatus(hubGuaranteeId, status) }
    assert(result.isEmpty())
  }

  @Test
  fun `should handle null result from repository when getting by id`() {
    // Given
    val id = "non-existent-id"

    every { cardReceivablesLockRepository.findById(id) } returns java.util.Optional.empty()

    // When
    val result = dataAccess.getById(id)

    // Then
    verify { cardReceivablesLockRepository.findById(id) }
    assert(result == null)
  }

  @Test
  fun `should handle null result from repository when getting by id with relationships`() {
    // Given
    val id = "non-existent-id"

    every { cardReceivablesLockRepository.findByIdWithRelationships(id) } returns null

    // When
    val result = dataAccess.getByIdWithRelationships(id)

    // Then
    verify { cardReceivablesLockRepository.findByIdWithRelationships(id) }
    assert(result == null)
  }

  @Test
  fun `should map entity to dto correctly when creating`() {
    // Given
    val dto = createTestDto()
    val savedEntity = createTestEntity()

    every { cardReceivablesLockRepository.save(any()) } returns savedEntity

    // When
    val result = dataAccess.create(dto)

    // Then
    verify { cardReceivablesLockRepository.save(any()) }
    assertNotNull(result)
    assertEquals(savedEntity.id, result.id)
    assertEquals(savedEntity.contractNumber, result.contractNumber)
    assertEquals(savedEntity.status, result.status)
  }

  @Test
  fun `should map entity to dto correctly when updating`() {
    // Given
    val dto = createTestDto()
    val updatedEntity = createTestEntity()

    every { cardReceivablesLockRepository.save(any()) } returns updatedEntity

    // When
    val result = dataAccess.update(dto)

    // Then
    verify { cardReceivablesLockRepository.save(any()) }
    assertNotNull(result)
    assertEquals(updatedEntity.id, result.id)
    assertEquals(updatedEntity.contractNumber, result.contractNumber)
    assertEquals(updatedEntity.status, result.status)
  }

  @Test
  fun `should handle empty list when getting by status`() {
    // Given
    val status = CardReceivablesLockStatus.INACTIVE

    every { cardReceivablesLockRepository.findByStatus(status) } returns emptyList()

    // When
    val result = dataAccess.getByStatus(status)

    // Then
    verify { cardReceivablesLockRepository.findByStatus(status) }
    assert(result.isEmpty())
  }

  @Test
  fun `should handle multiple entities when getting by status`() {
    // Given
    val status = CardReceivablesLockStatus.ACTIVE
    val entities = listOf(createTestEntity(), createTestEntity(), createTestEntity())

    every { cardReceivablesLockRepository.findByStatus(status) } returns entities

    // When
    val result = dataAccess.getByStatus(status)

    // Then
    verify { cardReceivablesLockRepository.findByStatus(status) }
    assertEquals(3, result.size)
    assert(result.all { it.status == status })
  }

  private fun createTestDto(): CardReceivablesLockDto {
    return TestDataFactory.createCardReceivablesLockDto()
  }

  private fun createTestEntity(): CardReceivablesLockEntity {
    return TestDataFactory.createCardReceivablesLockEntity()
  }
}


```

```kotlin
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

object TestDataFactory {

  // Constantes para IDs
  const val TEST_LOCK_ID = "test-id-123456789012345678901234"
  const val TEST_HOLDER_ID = "holder-id-123456789012345678901234"
  const val TEST_INSTALLMENT_ID = "installment-id-123456789012345678901234"
  const val TEST_NUCLEA_ID = "nuclea-id-123456789012345678901234"
  const val TEST_CERC_ID = "cerc-id-123456789012345678901234"

  // Constantes para dados básicos
  const val TEST_HUB_GUARANTEE_ID = "HUB123"
  const val TEST_CONTRACT_NUMBER = "CONTRACT123"
  const val TEST_CONTRACT_SOURCE = "SOURCE"
  const val TEST_IPOC = "IPOC123"
  const val TEST_OWNER_PERSON_ID = "OWNER123"
  const val TEST_TAX_ID = "12345678901"
  const val TEST_PROTOCOL = "PROTOCOL-001"

  // Constantes para valores
  val TEST_AMOUNT = BigDecimal("1000.00")
  val TEST_INSTALLMENT_VALUE = BigDecimal("500.00")

  // Constantes para datas
  val TEST_START_DATE = LocalDate.now()
  val TEST_END_DATE = LocalDate.now().plusMonths(12)
  val TEST_NOW = LocalDateTime.now()

  // Factory methods para CardReceivablesLockEntity
  fun createCardReceivablesLockEntity(
          id: String = TEST_LOCK_ID,
          hubGuaranteeId: String = TEST_HUB_GUARANTEE_ID,
          contractNumber: String = TEST_CONTRACT_NUMBER,
          status: CardReceivablesLockStatus = CardReceivablesLockStatus.ACTIVE,
          register: RegisterType = RegisterType.CERC,
          nucleaEntry: CardReceivablesLockNucleaEntity? = null,
          cercEntry: CardReceivablesLockCercEntity? = null
  ): CardReceivablesLockEntity {
    return CardReceivablesLockEntity(
            id = id,
            hubGuaranteeId = hubGuaranteeId,
            contractNumber = contractNumber,
            contractSource = TEST_CONTRACT_SOURCE,
            ipoc = TEST_IPOC,
            register = register,
            ownerPersonId = TEST_OWNER_PERSON_ID,
            amount = TEST_AMOUNT,
            recalculationFrequency = RecalculationPeriodIndicator.MONTHLY,
            considerBalanceOnInsufficiency = true,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            status = status,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            holders = mutableListOf(),
            contractInstallments = mutableListOf(),
            nucleaEntry = nucleaEntry,
            cercEntry = cercEntry
    )
  }

  // Factory methods para CardReceivablesLockDto
  fun createCardReceivablesLockDto(
          id: String = TEST_LOCK_ID,
          hubGuaranteeId: String = TEST_HUB_GUARANTEE_ID,
          contractNumber: String = TEST_CONTRACT_NUMBER,
          status: CardReceivablesLockStatus = CardReceivablesLockStatus.ACTIVE,
          register: RegisterType = RegisterType.CERC
  ): CardReceivablesLockDto {
    return CardReceivablesLockDto(
            id = id,
            hubGuaranteeId = hubGuaranteeId,
            contractNumber = contractNumber,
            contractSource = TEST_CONTRACT_SOURCE,
            ipoc = TEST_IPOC,
            register = register,
            ownerPersonId = TEST_OWNER_PERSON_ID,
            amount = TEST_AMOUNT,
            recalculationFrequency = RecalculationPeriodIndicator.MONTHLY,
            considerBalanceOnInsufficiency = true,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            status = status,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            holders = emptyList(),
            nucleaEntry = null,
            cercEntry = null,
            contractInstallments = emptyList()
    )
  }

  // Factory methods para CardReceivablesHolderEntity
  fun createCardReceivablesHolderEntity(
          id: String = TEST_HOLDER_ID,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesHolderEntity {
    return CardReceivablesHolderEntity(
            id = id,
            taxId = TEST_TAX_ID,
            rootTaxIdOperation = "false",
            paymentAccountBranch = "0001",
            paymentAccountNumber = "123456",
            paymentAccountId = "account-123456789012345678901234",
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesHolderDto
  fun createCardReceivablesHolderDto(id: String = TEST_HOLDER_ID): CardReceivablesHolderDto {
    return CardReceivablesHolderDto(
            id = id,
            taxId = TEST_TAX_ID,
            rootTaxIdOperation = "false",
            paymentAccountBranch = "0001",
            paymentAccountNumber = "123456",
            paymentAccountId = "account-123456789012345678901234"
    )
  }

  // Factory methods para CardReceivablesContractInstallmentEntity
  fun createCardReceivablesContractInstallmentEntity(
          id: String = TEST_INSTALLMENT_ID,
          installmentNumber: Int = 1,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesContractInstallmentEntity {
    return CardReceivablesContractInstallmentEntity(
            id = id,
            installmentNumber = installmentNumber,
            date = TEST_START_DATE,
            value = TEST_INSTALLMENT_VALUE,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesContractInstallmentDto
  fun createCardReceivablesContractInstallmentDto(
          id: String = TEST_INSTALLMENT_ID,
          installmentNumber: Int = 1
  ): CardReceivablesContractInstallmentDto {
    return CardReceivablesContractInstallmentDto(
            id = id,
            installmentNumber = installmentNumber,
            date = TEST_START_DATE,
            value = TEST_INSTALLMENT_VALUE
    )
  }

  // Factory methods para CardReceivablesLockNucleaEntity
  fun createCardReceivablesLockNucleaEntity(
          id: String = TEST_NUCLEA_ID,
          protocol: String = TEST_PROTOCOL,
          creationRetryAttempts: Int = 0,
          proactiveSearchAttempts: Int = 0,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesLockNucleaEntity {
    return CardReceivablesLockNucleaEntity(
            id = id,
            protocol = protocol,
            creationRetryAttempts = creationRetryAttempts,
            proactiveSearchAttempts = proactiveSearchAttempts,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesLockNucleaDto
  fun createCardReceivablesLockNucleaDto(
          id: String = TEST_NUCLEA_ID,
          protocol: String = TEST_PROTOCOL
  ): CardReceivablesLockNucleaDto {
    return CardReceivablesLockNucleaDto(
            id = id,
            creationRetryAttempts = 0,
            proactiveSearchAttempts = 0,
            protocol = protocol
    )
  }

  // Factory methods para CardReceivablesLockCercEntity
  fun createCardReceivablesLockCercEntity(
          id: String = TEST_CERC_ID,
          protocol: String = TEST_PROTOCOL,
          creationRetryAttempts: Int = 0,
          proactiveSearchAttempts: Int = 0,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesLockCercEntity {
    return CardReceivablesLockCercEntity(
            id = id,
            protocol = protocol,
            creationRetryAttempts = creationRetryAttempts,
            proactiveSearchAttempts = proactiveSearchAttempts,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesLockCercDto
  fun createCardReceivablesLockCercDto(id: String = TEST_CERC_ID): CardReceivablesLockCercDto {
    return CardReceivablesLockCercDto(
            id = id,
            creationRetryAttempts = 0,
            proactiveSearchAttempts = 0
    )
  }

  // Métodos utilitários para criar entidades com relacionamentos
  fun createCardReceivablesLockWithNuclea(
          lockId: String = TEST_LOCK_ID,
          nucleaId: String = TEST_NUCLEA_ID
  ): CardReceivablesLockEntity {
    val nucleaEntity = createCardReceivablesLockNucleaEntity(nucleaId)
    val lockEntity = createCardReceivablesLockEntity(lockId, register = RegisterType.NUCLEA)
    lockEntity.assignNucleaEntry(nucleaEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithCerc(
          lockId: String = TEST_LOCK_ID,
          cercId: String = TEST_CERC_ID
  ): CardReceivablesLockEntity {
    val cercEntity = createCardReceivablesLockCercEntity(cercId)
    val lockEntity = createCardReceivablesLockEntity(lockId, register = RegisterType.CERC)
    lockEntity.assignCercEntry(cercEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithHolder(
          lockId: String = TEST_LOCK_ID,
          holderId: String = TEST_HOLDER_ID
  ): CardReceivablesLockEntity {
    val holderEntity = createCardReceivablesHolderEntity(holderId)
    val lockEntity = createCardReceivablesLockEntity(lockId)
    lockEntity.addHolder(holderEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithInstallment(
          lockId: String = TEST_LOCK_ID,
          installmentId: String = TEST_INSTALLMENT_ID
  ): CardReceivablesLockEntity {
    val installmentEntity = createCardReceivablesContractInstallmentEntity(installmentId)
    val lockEntity = createCardReceivablesLockEntity(lockId)
    lockEntity.addContractInstallment(installmentEntity)
    return lockEntity
  }
}

```

```kotlin
object TestDataFactory {

  // Constantes para IDs
  const val TEST_LOCK_ID = "test-id-123456789012345678901234"
  const val TEST_HOLDER_ID = "holder-id-123456789012345678901234"
  const val TEST_INSTALLMENT_ID = "installment-id-123456789012345678901234"
  const val TEST_NUCLEA_ID = "nuclea-id-123456789012345678901234"
  const val TEST_CERC_ID = "cerc-id-123456789012345678901234"

  // Constantes para dados básicos
  const val TEST_HUB_GUARANTEE_ID = "HUB123"
  const val TEST_CONTRACT_NUMBER = "CONTRACT123"
  const val TEST_CONTRACT_SOURCE = "SOURCE"
  const val TEST_IPOC = "IPOC123"
  const val TEST_OWNER_PERSON_ID = "OWNER123"
  const val TEST_TAX_ID = "12345678901"
  const val TEST_PROTOCOL = "PROTOCOL-001"

  // Constantes para valores
  val TEST_AMOUNT = BigDecimal("1000.00")
  val TEST_INSTALLMENT_VALUE = BigDecimal("500.00")

  // Constantes para datas
  val TEST_START_DATE = LocalDate.now()
  val TEST_END_DATE = LocalDate.now().plusMonths(12)
  val TEST_NOW = LocalDateTime.now()

  // Factory methods para CardReceivablesLockEntity
  fun createCardReceivablesLockEntity(
          id: String = TEST_LOCK_ID,
          hubGuaranteeId: String = TEST_HUB_GUARANTEE_ID,
          contractNumber: String = TEST_CONTRACT_NUMBER,
          status: CardReceivablesLockStatus = CardReceivablesLockStatus.ACTIVE,
          register: RegisterType = RegisterType.CERC,
          nucleaEntry: CardReceivablesLockNucleaEntity? = null,
          cercEntry: CardReceivablesLockCercEntity? = null
  ): CardReceivablesLockEntity {
    return CardReceivablesLockEntity(
            id = id,
            hubGuaranteeId = hubGuaranteeId,
            contractNumber = contractNumber,
            contractSource = TEST_CONTRACT_SOURCE,
            ipoc = TEST_IPOC,
            register = register,
            ownerPersonId = TEST_OWNER_PERSON_ID,
            amount = TEST_AMOUNT,
            recalculationFrequency = RecalculationPeriodIndicator.MONTHLY,
            considerBalanceOnInsufficiency = true,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            status = status,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            holders = mutableListOf(),
            contractInstallments = mutableListOf(),
            nucleaEntry = nucleaEntry,
            cercEntry = cercEntry
    )
  }

  // Factory methods para CardReceivablesLockDto
  fun createCardReceivablesLockDto(
          id: String = TEST_LOCK_ID,
          hubGuaranteeId: String = TEST_HUB_GUARANTEE_ID,
          contractNumber: String = TEST_CONTRACT_NUMBER,
          status: CardReceivablesLockStatus = CardReceivablesLockStatus.ACTIVE,
          register: RegisterType = RegisterType.CERC
  ): CardReceivablesLockDto {
    return CardReceivablesLockDto(
            id = id,
            hubGuaranteeId = hubGuaranteeId,
            contractNumber = contractNumber,
            contractSource = TEST_CONTRACT_SOURCE,
            ipoc = TEST_IPOC,
            register = register,
            ownerPersonId = TEST_OWNER_PERSON_ID,
            amount = TEST_AMOUNT,
            recalculationFrequency = RecalculationPeriodIndicator.MONTHLY,
            considerBalanceOnInsufficiency = true,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            status = status,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            holders = emptyList(),
            nucleaEntry = null,
            cercEntry = null,
            contractInstallments = emptyList()
    )
  }

  // Factory methods para CardReceivablesHolderEntity
  fun createCardReceivablesHolderEntity(
          id: String = TEST_HOLDER_ID,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesHolderEntity {
    return CardReceivablesHolderEntity(
            id = id,
            taxId = TEST_TAX_ID,
            rootTaxIdOperation = "false",
            paymentAccountBranch = "0001",
            paymentAccountNumber = "123456",
            paymentAccountId = "account-123456789012345678901234",
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesHolderDto
  fun createCardReceivablesHolderDto(id: String = TEST_HOLDER_ID): CardReceivablesHolderDto {
    return CardReceivablesHolderDto(
            id = id,
            taxId = TEST_TAX_ID,
            rootTaxIdOperation = "false",
            paymentAccountBranch = "0001",
            paymentAccountNumber = "123456",
            paymentAccountId = "account-123456789012345678901234"
    )
  }

  // Factory methods para CardReceivablesContractInstallmentEntity
  fun createCardReceivablesContractInstallmentEntity(
          id: String = TEST_INSTALLMENT_ID,
          installmentNumber: Int = 1,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesContractInstallmentEntity {
    return CardReceivablesContractInstallmentEntity(
            id = id,
            installmentNumber = installmentNumber,
            date = TEST_START_DATE,
            value = TEST_INSTALLMENT_VALUE,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesContractInstallmentDto
  fun createCardReceivablesContractInstallmentDto(
          id: String = TEST_INSTALLMENT_ID,
          installmentNumber: Int = 1
  ): CardReceivablesContractInstallmentDto {
    return CardReceivablesContractInstallmentDto(
            id = id,
            installmentNumber = installmentNumber,
            date = TEST_START_DATE,
            value = TEST_INSTALLMENT_VALUE
    )
  }

  // Factory methods para CardReceivablesLockNucleaEntity
  fun createCardReceivablesLockNucleaEntity(
          id: String = TEST_NUCLEA_ID,
          protocol: String = TEST_PROTOCOL,
          creationRetryAttempts: Int = 0,
          proactiveSearchAttempts: Int = 0,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesLockNucleaEntity {
    return CardReceivablesLockNucleaEntity(
            id = id,
            protocol = protocol,
            creationRetryAttempts = creationRetryAttempts,
            proactiveSearchAttempts = proactiveSearchAttempts,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesLockNucleaDto
  fun createCardReceivablesLockNucleaDto(
          id: String = TEST_NUCLEA_ID,
          protocol: String = TEST_PROTOCOL
  ): CardReceivablesLockNucleaDto {
    return CardReceivablesLockNucleaDto(
            id = id,
            creationRetryAttempts = 0,
            proactiveSearchAttempts = 0,
            protocol = protocol
    )
  }

  // Factory methods para CardReceivablesLockCercEntity
  fun createCardReceivablesLockCercEntity(
          id: String = TEST_CERC_ID,
          protocol: String = TEST_PROTOCOL,
          creationRetryAttempts: Int = 0,
          proactiveSearchAttempts: Int = 0,
          cardReceivablesLock: CardReceivablesLockEntity? = null
  ): CardReceivablesLockCercEntity {
    return CardReceivablesLockCercEntity(
            id = id,
            protocol = protocol,
            creationRetryAttempts = creationRetryAttempts,
            proactiveSearchAttempts = proactiveSearchAttempts,
            createdAt = TEST_NOW,
            updatedAt = TEST_NOW,
            cardReceivablesLock = cardReceivablesLock
    )
  }

  // Factory methods para CardReceivablesLockCercDto
  fun createCardReceivablesLockCercDto(id: String = TEST_CERC_ID): CardReceivablesLockCercDto {
    return CardReceivablesLockCercDto(
            id = id,
            creationRetryAttempts = 0,
            proactiveSearchAttempts = 0
    )
  }

  // Métodos utilitários para criar entidades com relacionamentos
  fun createCardReceivablesLockWithNuclea(
          lockId: String = TEST_LOCK_ID,
          nucleaId: String = TEST_NUCLEA_ID
  ): CardReceivablesLockEntity {
    val nucleaEntity = createCardReceivablesLockNucleaEntity(nucleaId)
    val lockEntity = createCardReceivablesLockEntity(lockId, register = RegisterType.NUCLEA)
    lockEntity.assignNucleaEntry(nucleaEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithCerc(
          lockId: String = TEST_LOCK_ID,
          cercId: String = TEST_CERC_ID
  ): CardReceivablesLockEntity {
    val cercEntity = createCardReceivablesLockCercEntity(cercId)
    val lockEntity = createCardReceivablesLockEntity(lockId, register = RegisterType.CERC)
    lockEntity.assignCercEntry(cercEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithHolder(
          lockId: String = TEST_LOCK_ID,
          holderId: String = TEST_HOLDER_ID
  ): CardReceivablesLockEntity {
    val holderEntity = createCardReceivablesHolderEntity(holderId)
    val lockEntity = createCardReceivablesLockEntity(lockId)
    lockEntity.addHolder(holderEntity)
    return lockEntity
  }

  fun createCardReceivablesLockWithInstallment(
          lockId: String = TEST_LOCK_ID,
          installmentId: String = TEST_INSTALLMENT_ID
  ): CardReceivablesLockEntity {
    val installmentEntity = createCardReceivablesContractInstallmentEntity(installmentId)
    val lockEntity = createCardReceivablesLockEntity(lockId)
    lockEntity.addContractInstallment(installmentEntity)
    return lockEntity
  }
}

```

```kotlin

data class CardReceivablesLockDto(
        val id: String,
        val hubGuaranteeId: String,
        val contractNumber: String,
        val contractSource: String,
        val ipoc: String,
        val register: RegisterType,
        val ownerPersonId: String,
        val amount: BigDecimal,
        val recalculationFrequency: RecalculationPeriodIndicator,
        val considerBalanceOnInsufficiency: Boolean,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val status: CardReceivablesLockStatus,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val holders: List<CardReceivablesHolderDto>,
        val nucleaEntry: CardReceivablesLockNucleaDto?,
        val cercEntry: CardReceivablesLockCercDto?,
        val contractInstallments: List<CardReceivablesContractInstallmentDto>
)

```

```kotlin

object CardReceivablesLockMapper {

  fun map(entity: CardReceivablesLockEntity): CardReceivablesLockDto {
    return CardReceivablesLockDto(
            id = entity.id,
            hubGuaranteeId = entity.hubGuaranteeId,
            contractNumber = entity.contractNumber,
            contractSource = entity.contractSource,
            ipoc = entity.ipoc,
            register = entity.register,
            ownerPersonId = entity.ownerPersonId,
            amount = entity.amount,
            recalculationFrequency = entity.recalculationFrequency,
            considerBalanceOnInsufficiency = entity.considerBalanceOnInsufficiency,
            startDate = entity.startDate,
            endDate = entity.endDate,
            status = entity.status,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            holders = entity.holders.map { CardReceivablesHolderMapper.map(it) },
            nucleaEntry = entity.nucleaEntry?.let { CardReceivablesLockNucleaMapper.map(it) },
            cercEntry = entity.cercEntry?.let { CardReceivablesLockCercMapper.map(it) },
            contractInstallments =
                    entity.contractInstallments.map {
                      CardReceivablesContractInstallmentMapper.map(it)
                    }
    )
  }

  fun map(dto: CardReceivablesLockDto): CardReceivablesLockEntity {
    return CardReceivablesLockEntity(
            id = dto.id,
            hubGuaranteeId = dto.hubGuaranteeId,
            contractNumber = dto.contractNumber,
            contractSource = dto.contractSource,
            ipoc = dto.ipoc,
            register = dto.register,
            ownerPersonId = dto.ownerPersonId,
            amount = dto.amount,
            recalculationFrequency = dto.recalculationFrequency,
            considerBalanceOnInsufficiency = dto.considerBalanceOnInsufficiency,
            startDate = dto.startDate,
            endDate = dto.endDate,
            status = dto.status,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt,
            holders = dto.holders.map { CardReceivablesHolderMapper.map(it) }.toMutableList(),
            nucleaEntry = dto.nucleaEntry?.let { CardReceivablesLockNucleaMapper.map(it) },
            cercEntry = dto.cercEntry?.let { CardReceivablesLockCercMapper.map(it) },
            contractInstallments =
                    dto.contractInstallments
                            .map { CardReceivablesContractInstallmentMapper.map(it) }
                            .toMutableList()
    )
  }
object CardReceivablesHolderMapper {

  fun map(entity: CardReceivablesHolderEntity): CardReceivablesHolderDto {
    return CardReceivablesHolderDto(
            id = entity.id,
            taxId = entity.taxId,
            rootTaxIdOperation = entity.rootTaxIdOperation,
            paymentAccountBranch = entity.paymentAccountBranch,
            paymentAccountNumber = entity.paymentAccountNumber,
            paymentAccountId = entity.paymentAccountId
    )
  }

  fun map(dto: CardReceivablesHolderDto): CardReceivablesHolderEntity {
    return CardReceivablesHolderEntity(
            id = dto.id,
            cardReceivablesLock = null,
            taxId = dto.taxId,
            rootTaxIdOperation = dto.rootTaxIdOperation,
            paymentAccountBranch = dto.paymentAccountBranch,
            paymentAccountNumber = dto.paymentAccountNumber,
            paymentAccountId = dto.paymentAccountId
    )
  }
}

object CardReceivablesLockNucleaMapper {

  fun map(entity: CardReceivablesLockNucleaEntity): CardReceivablesLockNucleaDto {
    return CardReceivablesLockNucleaDto(
            id = entity.id,
            creationRetryAttempts = entity.creationRetryAttempts,
            proactiveSearchAttempts = entity.proactiveSearchAttempts,
            protocol = entity.protocol
    )
  }

  fun map(dto: CardReceivablesLockNucleaDto): CardReceivablesLockNucleaEntity {
    return CardReceivablesLockNucleaEntity(
            id = dto.id,
            cardReceivablesLock = null,
            protocol = dto.protocol,
            creationRetryAttempts = dto.creationRetryAttempts,
            proactiveSearchAttempts = dto.proactiveSearchAttempts,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
    )
  }
}

object CardReceivablesLockCercMapper {

  fun map(entity: CardReceivablesLockCercEntity): CardReceivablesLockCercDto {
    return CardReceivablesLockCercDto(
            id = entity.id,
            creationRetryAttempts = entity.creationRetryAttempts,
            proactiveSearchAttempts = entity.proactiveSearchAttempts
    )
  }

  fun map(dto: CardReceivablesLockCercDto): CardReceivablesLockCercEntity {
    return CardReceivablesLockCercEntity(
            id = dto.id,
            cardReceivablesLock = null,
            protocol = null,
            creationRetryAttempts = dto.creationRetryAttempts,
            proactiveSearchAttempts = dto.proactiveSearchAttempts,
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
    )
  }
}

```

