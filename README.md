

```sql
-- Card Receivables Lock (Main table)
CREATE TABLE card_receivables_lock (
    id BIGSERIAL PRIMARY KEY,
    hub_guarantee_id BIGINT NOT NULL,
    contract_number VARCHAR(255) NOT NULL,
    contract_source VARCHAR(255) NOT NULL,
    ipoc VARCHAR(255) NOT NULL,
    owner_person_id BIGINT,
    amount DECIMAL(19, 2) NOT NULL,
    recalculation_frequency VARCHAR(255) NOT NULL,
    balance_on_insufficiency DECIMAL(19, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    creation_retry_attempts INTEGER DEFAULT 0,
    proactive_search_attempts INTEGER DEFAULT 0
);

-- Card Receivables Holder
CREATE TABLE card_receivables_holder (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_id BIGINT NOT NULL,
    tax_id VARCHAR(14) NOT NULL,
    root_tax_id_operation BOOLEAN NOT NULL,
    payment_account_branch VARCHAR(10) NOT NULL,
    payment_account_number VARCHAR(20) NOT NULL,
    payment_account_id VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_card_receivables_holder_lock FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Card Receivables Owner Arrangement
CREATE TABLE card_receivables_owner_arrangement (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_holder_id BIGINT NOT NULL,
    arrangement_code VARCHAR(10) NOT NULL,
    CONSTRAINT fk_card_receivables_owner_arrangement_holder FOREIGN KEY (
        card_receivables_lock_holder_id
    ) REFERENCES card_receivables_holder (id)
);

-- Card Receivables Owner Accreditor
CREATE TABLE card_receivables_owner_accreditor (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_holder_id BIGINT NOT NULL,
    accreditor_tax_id VARCHAR(14) NOT NULL,
    CONSTRAINT fk_card_receivables_owner_accreditor_holder FOREIGN KEY (
        card_receivables_lock_holder_id
    ) REFERENCES card_receivables_holder (id)
);

-- Card Receivables Lock Nuclea
CREATE TABLE card_receivables_lock_nuclea (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_id BIGINT NOT NULL,
    creation_retry_attempts INTEGER DEFAULT 0,
    proactive_search_attempts INTEGER DEFAULT 0,
    protocol VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_card_receivables_lock_nuclea_lock FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Card Receivables Lock Cerc
CREATE TABLE card_receivables_lock_cerc (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_id BIGINT NOT NULL,
    creation_retry_attempts INTEGER DEFAULT 0,
    proactive_search_attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_card_receivables_lock_cerc_lock FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Card Receivables Lock Cerc Protocols
CREATE TABLE card_receivables_lock_cerc_protocols (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_cerc_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    protocol VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_card_receivables_lock_cerc_protocols_cerc FOREIGN KEY (card_receivables_lock_cerc_id) REFERENCES card_receivables_lock_cerc (id)
);

-- Card Receivables Contract Installments
CREATE TABLE card_receivables_contract_installments (
    id BIGSERIAL PRIMARY KEY,
    card_receivables_lock_id BIGINT NOT NULL,
    installment_number INTEGER NOT NULL,
    date DATE NOT NULL,
    value DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_card_receivables_contract_installments_lock FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id)
);

-- Indexes for performance
CREATE INDEX idx_card_receivables_lock_contract_number ON card_receivables_lock (contract_number);

CREATE INDEX idx_card_receivables_lock_hub_guarantee_id ON card_receivables_lock (hub_guarantee_id);

CREATE INDEX idx_card_receivables_lock_status ON card_receivables_lock (status);

CREATE INDEX idx_card_receivables_holder_lock_id ON card_receivables_holder (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_holder_tax_id ON card_receivables_holder (tax_id);

CREATE INDEX idx_card_receivables_owner_arrangement_holder_id ON card_receivables_owner_arrangement (
    card_receivables_lock_holder_id
);

CREATE INDEX idx_card_receivables_owner_accreditor_holder_id ON card_receivables_owner_accreditor (
    card_receivables_lock_holder_id
);

CREATE INDEX idx_card_receivables_lock_nuclea_lock_id ON card_receivables_lock_nuclea (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_lock_nuclea_protocol ON card_receivables_lock_nuclea (protocol);

CREATE INDEX idx_card_receivables_lock_cerc_lock_id ON card_receivables_lock_cerc (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_lock_cerc_protocols_cerc_id ON card_receivables_lock_cerc_protocols (card_receivables_lock_cerc_id);

CREATE INDEX idx_card_receivables_lock_cerc_protocols_protocol ON card_receivables_lock_cerc_protocols (protocol);

CREATE INDEX idx_card_receivables_contract_installments_lock_id ON card_receivables_contract_installments (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_contract_installments_date ON card_receivables_contract_installments (date);
```

```kotlin
interface CardReceivablesLockDataAccess {
  // Card Receivables Lock methods
  fun create(lock: CardReceivablesLock): CardReceivablesLock
  fun update(lock: CardReceivablesLock): CardReceivablesLock
  fun getById(id: Long): CardReceivablesLock?
  fun findByContractNumber(contractNumber: String): CardReceivablesLock?
  fun incrementCreationRetryAttempts(id: Long): CardReceivablesLock?
  fun incrementProactiveSearchAttempts(id: Long): CardReceivablesLock?
}
```

```kotlin
@Entity
@Table(name = "card_receivables_contract_installments")
data class CardReceivablesContractInstallments(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @ManyToOne
        @JoinColumn(name = "card_receivables_lock_id")
        val cardReceivablesLock: CardReceivablesLock,
        val installmentNumber: Int,
        val date: LocalDate,
        val value: BigDecimal,
        val createdAt: LocalDateTime
)

@Entity
@Table(name = "card_receivables_holder")
data class CardReceivablesHolder(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @ManyToOne
        @JoinColumn(name = "card_receivables_lock_id")
        val cardReceivablesLock: CardReceivablesLock,
        val taxId: String,
        val rootTaxIdOperation: Boolean,
        val paymentAccountBranch: String,
        val paymentAccountNumber: String,
        val paymentAccountId: String,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        @OneToMany(
                mappedBy = "cardReceivablesLockHolder",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val arrangements: MutableList<CardReceivablesOwnerArrangement> = mutableListOf(),
        @OneToMany(
                mappedBy = "cardReceivablesLockHolder",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val accreditors: MutableList<CardReceivablesOwnerAccreditor> = mutableListOf()
)

@Entity
@Table(name = "card_receivables_lock")
data class CardReceivablesLock(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        val hubGuaranteeId: Long,
        val contractNumber: String,
        val contractSource: String,
        val ipoc: String,
        val ownerPersonId: Long? = null,
        val amount: Double,
        val recalculationFrequency: String,
        val balanceOnInsufficiency: Double,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime? = null,
        var status: String,
        val createdAt: LocalDateTime,
        var updatedAt: LocalDateTime,
        var creationRetryAttempts: Int = 0,
        var proactiveSearchAttempts: Int = 0,
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val holders: MutableList<CardReceivablesHolder> = mutableListOf(),
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val nucleaEntries: MutableList<CardReceivablesLockNuclea> = mutableListOf(),
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val cercEntries: MutableList<CardReceivablesLockCerc> = mutableListOf(),
        @OneToMany(
                mappedBy = "cardReceivablesLock",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val contractInstallments: MutableList<CardReceivablesContractInstallments> = mutableListOf()
)
```

```kotlin
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock_nuclea")
data class CardReceivablesLockNuclea(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @ManyToOne
        @JoinColumn(name = "card_receivables_lock_id")
        val cardReceivablesLock: CardReceivablesLock,
        var creationRetryAttempts: Int = 0,
        var proactiveSearchAttempts: Int = 0,
        val protocol: String,
        val createdAt: LocalDateTime,
        var updatedAt: LocalDateTime
)


import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_contract_installments")
data class CardReceivablesContractInstallments(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
        @ManyToOne
        @JoinColumn(name = "card_receivables_lock_id")
        val cardReceivablesLock: CardReceivablesLock,
        val installmentNumber: Int,
        val date: LocalDate,
        val value: BigDecimal,
        val createdAt: LocalDateTime
)
```

```kotlin
@Service
class CardReceivablesLockDataAccessImpl(
        private val cardReceivablesLockRepository: CardReceivablesLockRepository,
        private val cardReceivablesHolderRepository: CardReceivablesHolderRepository,
        private val cardReceivablesOwnerArrangementRepository:
                CardReceivablesOwnerArrangementRepository,
        private val cardReceivablesOwnerAccreditorRepository:
                CardReceivablesOwnerAccreditorRepository,
        private val cardReceivablesLockNucleaRepository: CardReceivablesLockNucleaRepository,
        private val cardReceivablesLockCercRepository: CardReceivablesLockCercRepository,
        private val cardReceivablesLockCercProtocolsRepository:
                CardReceivablesLockCercProtocolsRepository,
        private val cardReceivablesContractInstallmentsRepository:
                CardReceivablesContractInstallmentsRepository
) : CardReceivablesLockDataAccess {

//Card Receivables Lock methods
  override fun create(lock: CardReceivablesLock): CardReceivablesLock {
    return cardReceivablesLockRepository.save(lock)
  }

  override fun update(lock: CardReceivablesLock): CardReceivablesLock {
    return cardReceivablesLockRepository.save(lock)
  }

  override fun getById(id: Long): CardReceivablesLock? {
    return cardReceivablesLockRepository.findById(id).orElse(null)
  }

  override fun findByContractNumber(contractNumber: String): CardReceivablesLock? {
    return cardReceivablesLockRepository.findByContractNumber(contractNumber)
  }

  override fun incrementCreationRetryAttempts(id: Long): CardReceivablesLock? {
    val lock = getById(id) ?: return null
    lock.creationRetryAttempts += 1
    return update(lock)
  }

  override fun incrementProactiveSearchAttempts(id: Long): CardReceivablesLock? {
    val lock = getById(id) ?: return null
    lock.proactiveSearchAttempts += 1
    return update(lock)
  }

//Card Receivables Holder methods
  override fun createHolder(holder: CardReceivablesHolder): CardReceivablesHolder {
    return cardReceivablesHolderRepository.save(holder)
  }

  override fun updateHolder(holder: CardReceivablesHolder): CardReceivablesHolder {
    return cardReceivablesHolderRepository.save(holder)
  }

  override fun getHolderById(id: Long): CardReceivablesHolder? {
    return cardReceivablesHolderRepository.findById(id).orElse(null)
  }

  override fun findHoldersByLockId(lockId: Long): List<CardReceivablesHolder> {
    return cardReceivablesHolderRepository.findByCardReceivablesLockId(lockId)
  }

  override fun findHoldersByTaxId(taxId: String): List<CardReceivablesHolder> {
    return cardReceivablesHolderRepository.findByTaxId(taxId)
  }

//Card Receivables Owner Arrangement methods
  override fun createOwnerArrangement(
          arrangement: CardReceivablesOwnerArrangement
  ): CardReceivablesOwnerArrangement {
    return cardReceivablesOwnerArrangementRepository.save(arrangement)
  }

  override fun findOwnerArrangementsByHolderId(
          holderId: Long
  ): List<CardReceivablesOwnerArrangement> {
    return cardReceivablesOwnerArrangementRepository.findByCardReceivablesLockHolderId(holderId)
  }

//Card Receivables Owner Accreditor methods
  override fun createOwnerAccreditor(
          accreditor: CardReceivablesOwnerAccreditor
  ): CardReceivablesOwnerAccreditor {
    return cardReceivablesOwnerAccreditorRepository.save(accreditor)
  }

  override fun findOwnerAccreditorsByHolderId(
          holderId: Long
  ): List<CardReceivablesOwnerAccreditor> {
    return cardReceivablesOwnerAccreditorRepository.findByCardReceivablesLockHolderId(holderId)
  }

//Card Receivables Lock Nuclea methods
  override fun createNuclea(nuclea: CardReceivablesLockNuclea): CardReceivablesLockNuclea {
    return cardReceivablesLockNucleaRepository.save(nuclea)
  }

  override fun updateNuclea(nuclea: CardReceivablesLockNuclea): CardReceivablesLockNuclea {
    return cardReceivablesLockNucleaRepository.save(nuclea)
  }

  override fun getNucleaById(id: Long): CardReceivablesLockNuclea? {
    return cardReceivablesLockNucleaRepository.findById(id).orElse(null)
  }

  override fun findNucleaByLockId(lockId: Long): List<CardReceivablesLockNuclea> {
    return cardReceivablesLockNucleaRepository.findByCardReceivablesLockId(lockId)
  }

  override fun findNucleaByProtocol(protocol: String): CardReceivablesLockNuclea? {
    return cardReceivablesLockNucleaRepository.findByProtocol(protocol)
  }

  override fun incrementNucleaCreationRetryAttempts(id: Long): CardReceivablesLockNuclea? {
    val nuclea = getNucleaById(id) ?: return null
    nuclea.creationRetryAttempts += 1
    return updateNuclea(nuclea)
  }

  override fun incrementNucleaProactiveSearchAttempts(id: Long): CardReceivablesLockNuclea? {
    val nuclea = getNucleaById(id) ?: return null
    nuclea.proactiveSearchAttempts += 1
    return updateNuclea(nuclea)
  }

//Card Receivables Lock Cerc methods
  override fun createCerc(cerc: CardReceivablesLockCerc): CardReceivablesLockCerc {
    return cardReceivablesLockCercRepository.save(cerc)
  }

  override fun updateCerc(cerc: CardReceivablesLockCerc): CardReceivablesLockCerc {
    return cardReceivablesLockCercRepository.save(cerc)
  }

  override fun getCercById(id: Long): CardReceivablesLockCerc? {
    return cardReceivablesLockCercRepository.findById(id).orElse(null)
  }

  override fun findCercByLockId(lockId: Long): List<CardReceivablesLockCerc> {
    return cardReceivablesLockCercRepository.findByCardReceivablesLockId(lockId)
  }

  override fun incrementCercCreationRetryAttempts(id: Long): CardReceivablesLockCerc? {
    val cerc = getCercById(id) ?: return null
    cerc.creationRetryAttempts += 1
    return updateCerc(cerc)
  }

  override fun incrementCercProactiveSearchAttempts(id: Long): CardReceivablesLockCerc? {
    val cerc = getCercById(id) ?: return null
    cerc.proactiveSearchAttempts += 1
    return updateCerc(cerc)
  }

//Card Receivables Lock Cerc Protocols methods
  override fun createCercProtocol(
          protocol: CardReceivablesLockCercProtocols
  ): CardReceivablesLockCercProtocols {
    return cardReceivablesLockCercProtocolsRepository.save(protocol)
  }

  override fun findCercProtocolsByCercId(cercId: Long): List<CardReceivablesLockCercProtocols> {
    return cardReceivablesLockCercProtocolsRepository.findByCardReceivablesLockCercId(cercId)
  }

  override fun findCercProtocolsByProtocol(
          protocol: String
  ): List<CardReceivablesLockCercProtocols> {
    return cardReceivablesLockCercProtocolsRepository.findByProtocol(protocol)
  }

//Card Receivables Contract Installments methods
  override fun createContractInstallment(
          installment: CardReceivablesContractInstallments
  ): CardReceivablesContractInstallments {
    return cardReceivablesContractInstallmentsRepository.save(installment)
  }

  override fun findContractInstallmentsByLockId(
          lockId: Long
  ): List<CardReceivablesContractInstallments> {
    return cardReceivablesContractInstallmentsRepository.findByCardReceivablesLockId(lockId)
  }

  override fun findContractInstallmentsByLockIdAndDateRange(
          lockId: Long,
          startDate: LocalDate,
          endDate: LocalDate
  ): List<CardReceivablesContractInstallments> {
    return cardReceivablesContractInstallmentsRepository.findByCardReceivablesLockIdAndDateBetween(
            lockId,
            startDate,
            endDate
    )
  }
}
```

```kotlin

class CardReceivablesLockDataAccessImplTest {
  private lateinit var cardReceivablesLockRepository: CardReceivablesLockRepository
  private lateinit var cardReceivablesHolderRepository: CardReceivablesHolderRepository
  private lateinit var cardReceivablesOwnerArrangementRepository:
          CardReceivablesOwnerArrangementRepository
  private lateinit var cardReceivablesOwnerAccreditorRepository:
          CardReceivablesOwnerAccreditorRepository
  private lateinit var cardReceivablesLockNucleaRepository: CardReceivablesLockNucleaRepository
  private lateinit var cardReceivablesLockCercRepository: CardReceivablesLockCercRepository
  private lateinit var cardReceivablesLockCercProtocolsRepository:
          CardReceivablesLockCercProtocolsRepository
  private lateinit var cardReceivablesContractInstallmentsRepository:
          CardReceivablesContractInstallmentsRepository
  private lateinit var dataAccess: CardReceivablesLockDataAccessImpl

  @BeforeEach
  fun setUp() {
    cardReceivablesLockRepository = mockk<CardReceivablesLockRepository>()
    cardReceivablesHolderRepository = mockk<CardReceivablesHolderRepository>()
    cardReceivablesOwnerArrangementRepository = mockk<CardReceivablesOwnerArrangementRepository>()
    cardReceivablesOwnerAccreditorRepository = mockk<CardReceivablesOwnerAccreditorRepository>()
    cardReceivablesLockNucleaRepository = mockk<CardReceivablesLockNucleaRepository>()
    cardReceivablesLockCercRepository = mockk<CardReceivablesLockCercRepository>()
    cardReceivablesLockCercProtocolsRepository = mockk<CardReceivablesLockCercProtocolsRepository>()
    cardReceivablesContractInstallmentsRepository =
            mockk<CardReceivablesContractInstallmentsRepository>()

    dataAccess =
            CardReceivablesLockDataAccessImpl(
                    cardReceivablesLockRepository,
                    cardReceivablesHolderRepository,
                    cardReceivablesOwnerArrangementRepository,
                    cardReceivablesOwnerAccreditorRepository,
                    cardReceivablesLockNucleaRepository,
                    cardReceivablesLockCercRepository,
                    cardReceivablesLockCercProtocolsRepository,
                    cardReceivablesContractInstallmentsRepository
            )
  }

  private fun createCardReceivablesLock(
          id: Long = 1L,
          hubGuaranteeId: Long = 123L,
          contractNumber: String = "CONTRACT001",
          contractSource: String = "SOURCE1",
          ipoc: String = "IPOC001",
          ownerPersonId: Long? = 1L,
          amount: Double = 1000.0,
          recalculationFrequency: String = "MONTHLY",
          balanceOnInsufficiency: Double = 500.0,
          startDate: LocalDateTime = LocalDateTime.now(),
          endDate: LocalDateTime? = null,
          status: String = "ACTIVE",
          createdAt: LocalDateTime = LocalDateTime.now(),
          updatedAt: LocalDateTime = LocalDateTime.now(),
          creationRetryAttempts: Int = 0,
          proactiveSearchAttempts: Int = 0
  ): CardReceivablesLock {
    return CardReceivablesLock(
            id = id,
            hubGuaranteeId = hubGuaranteeId,
            contractNumber = contractNumber,
            contractSource = contractSource,
            ipoc = ipoc,
            ownerPersonId = ownerPersonId,
            amount = amount,
            recalculationFrequency = recalculationFrequency,
            balanceOnInsufficiency = balanceOnInsufficiency,
            startDate = startDate,
            endDate = endDate,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            creationRetryAttempts = creationRetryAttempts,
            proactiveSearchAttempts = proactiveSearchAttempts
    )
  }

  @Test
  fun `should create card receivables lock successfully`() {
    // Given
    val lock = createCardReceivablesLock(id = 0L)
    val savedLock = createCardReceivablesLock()
    every { cardReceivablesLockRepository.save(lock) } returns savedLock

/
/
When val result = dataAccess.create (lock)

/
/
Then
    assertEquals(savedLock, result)
    verify(exactly = 1) { cardReceivablesLockRepository.save(lock) }
  }

  @Test
  fun `should update card receivables lock successfully`() {
    // Given
    val lock = createCardReceivablesLock()
    val updatedLock = createCardReceivablesLock(status = "UPDATED")
    every { cardReceivablesLockRepository.save(lock) } returns updatedLock

/
/
When val result = dataAccess.update (lock)

/
/
Then
    assertEquals(updatedLock, result)
    verify(exactly = 1) { cardReceivablesLockRepository.save(lock) }
  }

  @Test
  fun `should get card receivables lock by id when exists`() {
    // Given
    val lockId = 1L
    val lock = createCardReceivablesLock(id = lockId)
    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.of(lock)

/
/
When val result = dataAccess.getById (lockId)

/
/
Then
    assertEquals(lock, result)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
  }

  @Test
  fun `should return null when card receivables lock not found by id`() {
    // Given
    val lockId = 999L
    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.empty()

/
/
When val result = dataAccess.getById (lockId)

/
/
Then
    assertNull(result)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
  }

  @Test
  fun `should find card receivables lock by contract number when exists`() {
    // Given
    val contractNumber = "CONTRACT001"
    val lock = createCardReceivablesLock(contractNumber = contractNumber)
    every { cardReceivablesLockRepository.findByContractNumber(contractNumber) } returns lock

/
/
When val result = dataAccess.findByContractNumber (contractNumber)

/
/
Then
    assertEquals(lock, result)
    verify(exactly = 1) { cardReceivablesLockRepository.findByContractNumber(contractNumber) }
  }

  @Test
  fun `should return null when card receivables lock not found by contract number`() {
    // Given
    val contractNumber = "NONEXISTENT"
    every { cardReceivablesLockRepository.findByContractNumber(contractNumber) } returns null

/
/
When val result = dataAccess.findByContractNumber (contractNumber)

/
/
Then
    assertNull(result)
    verify(exactly = 1) { cardReceivablesLockRepository.findByContractNumber(contractNumber) }
  }

  @Test
  fun `should increment creation retry attempts successfully`() {
    // Given
    val lockId = 1L
    val lock = createCardReceivablesLock(id = lockId, creationRetryAttempts = 2)
    val updatedLock = createCardReceivablesLock(id = lockId, creationRetryAttempts = 3)

    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.of(lock)
    every { cardReceivablesLockRepository.save(lock) } returns updatedLock

/
/
When val result = dataAccess.incrementCreationRetryAttempts (lockId)

/
/
Then
    assertEquals(updatedLock, result)
    assertEquals(3, result?.creationRetryAttempts)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
    verify(exactly = 1) { cardReceivablesLockRepository.save(lock) }
  }

  @Test
  fun `should return null when incrementing creation retry attempts for non-existent lock`() {
    // Given
    val lockId = 999L
    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.empty()

/
/
When val result = dataAccess.incrementCreationRetryAttempts (lockId)

/
/
Then
    assertNull(result)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
    verify(exactly = 0) { cardReceivablesLockRepository.save(any()) }
  }

  @Test
  fun `should increment proactive search attempts successfully`() {
    // Given
    val lockId = 1L
    val lock = createCardReceivablesLock(id = lockId, proactiveSearchAttempts = 1)
    val updatedLock = createCardReceivablesLock(id = lockId, proactiveSearchAttempts = 2)

    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.of(lock)
    every { cardReceivablesLockRepository.save(lock) } returns updatedLock

/
/
When val result = dataAccess.incrementProactiveSearchAttempts (lockId)

/
/
Then
    assertEquals(updatedLock, result)
    assertEquals(2, result?.proactiveSearchAttempts)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
    verify(exactly = 1) { cardReceivablesLockRepository.save(lock) }
  }

  @Test
  fun `should return null when incrementing proactive search attempts for non-existent lock`() {
    // Given
    val lockId = 999L
    every { cardReceivablesLockRepository.findById(lockId) } returns java.util.Optional.empty()

/
/
When val result = dataAccess.incrementProactiveSearchAttempts (lockId)

/
/
Then
    assertNull(result)
    verify(exactly = 1) { cardReceivablesLockRepository.findById(lockId) }
    verify(exactly = 0) { cardReceivablesLockRepository.save(any()) }
  }
}
```

