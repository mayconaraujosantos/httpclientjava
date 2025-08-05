```sql
-- Migration V1: Create card_receivables_lock tables
-- Based on the DER diagram for receivables lock system

-- Main table: card_receivables_lock
CREATE TABLE card_receivables_lock (
    id VARCHAR(26) NOT NULL,
    contract_number VARCHAR(50) NOT NULL,
    hub_guarantee_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    PRIMARY KEY (id)
);

-- Table: card_receivables_holder
CREATE TABLE card_receivables_holder (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    holder_type VARCHAR(50) NOT NULL,
    holder_document VARCHAR(20) NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id) ON DELETE CASCADE
);

-- Table: card_receivables_owner_arrangement
CREATE TABLE card_receivables_owner_arrangement (
    id VARCHAR(26) NOT NULL,
    card_receivables_holder_id VARCHAR(26) NOT NULL,
    arrangement_type VARCHAR(50) NOT NULL,
    arrangement_code VARCHAR(50) NOT NULL,
    arrangement_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_holder_id) REFERENCES card_receivables_holder (id) ON DELETE CASCADE
);

-- Table: card_receivables_owner_accreditor
CREATE TABLE card_receivables_owner_accreditor (
    id VARCHAR(26) NOT NULL,
    card_receivables_holder_id VARCHAR(26) NOT NULL,
    accreditor_code VARCHAR(50) NOT NULL,
    accreditor_name VARCHAR(255) NOT NULL,
    accreditor_document VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_holder_id) REFERENCES card_receivables_holder (id) ON DELETE CASCADE
);

-- Table: card_receivables_lock_nuclea
CREATE TABLE card_receivables_lock_nuclea (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    protocol VARCHAR(100),
    creation_retry_attempts INTEGER NOT NULL DEFAULT 0,
    proactive_search_attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id) ON DELETE CASCADE
);

-- Table: card_receivables_lock_cerc
CREATE TABLE card_receivables_lock_cerc (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    creation_retry_attempts INTEGER NOT NULL DEFAULT 0,
    proactive_search_attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id) ON DELETE CASCADE
);

-- Table: card_receivables_lock_cerc_protocols
CREATE TABLE card_receivables_lock_cerc_protocols (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_cerc_id VARCHAR(26) NOT NULL,
    protocol VARCHAR(100) NOT NULL,
    protocol_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_cerc_id) REFERENCES card_receivables_lock_cerc (id) ON DELETE CASCADE
);

-- Table: card_receivables_contract_installments
CREATE TABLE card_receivables_contract_installments (
    id VARCHAR(26) NOT NULL,
    card_receivables_lock_id VARCHAR(26) NOT NULL,
    installment_number INTEGER NOT NULL,
    installment_amount DECIMAL(15, 2) NOT NULL,
    installment_due_date DATE NOT NULL,
    installment_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (card_receivables_lock_id) REFERENCES card_receivables_lock (id) ON DELETE CASCADE
);

-- Table: card_receivables_schedules
CREATE TABLE card_receivables_schedules (
    id VARCHAR(26) NOT NULL,
    schedule_type VARCHAR(50) NOT NULL,
    schedule_description TEXT,
    schedule_date TIMESTAMP NOT NULL,
    schedule_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Create indexes for better performance
CREATE INDEX idx_card_receivables_lock_contract_number ON card_receivables_lock (contract_number);

CREATE INDEX idx_card_receivables_lock_hub_guarantee_id ON card_receivables_lock (hub_guarantee_id);

CREATE INDEX idx_card_receivables_lock_status ON card_receivables_lock (status);

CREATE INDEX idx_card_receivables_lock_start_date ON card_receivables_lock (start_date);

CREATE INDEX idx_card_receivables_lock_end_date ON card_receivables_lock (end_date);

CREATE INDEX idx_card_receivables_holder_lock_id ON card_receivables_holder (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_holder_document ON card_receivables_holder (holder_document);

CREATE INDEX idx_card_receivables_lock_nuclea_lock_id ON card_receivables_lock_nuclea (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_lock_nuclea_protocol ON card_receivables_lock_nuclea (protocol);

CREATE INDEX idx_card_receivables_lock_cerc_lock_id ON card_receivables_lock_cerc (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_contract_installments_lock_id ON card_receivables_contract_installments (card_receivables_lock_id);

CREATE INDEX idx_card_receivables_contract_installments_due_date ON card_receivables_contract_installments (installment_due_date);
```

```kotlin
package com.finapp.domain.entities

import com.finapp.domain.enums.CardReceivablesLockStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock")
class CardReceivablesLock(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @Column(name = "hub_guarantee_id", length = 50, nullable = false)
        val hubGuaranteeId: String,
        @Column(name = "contract_number", length = 50, nullable = false) val contractNumber: String,
        @Column(name = "contract_source", length = 50) val contractSource: String?,
        @Column(name = "ipoc", length = 50) val ipoc: String?,
        @Column(name = "register", length = 50) val register: String?,
        @Column(name = "owner_person_id", length = 26) val ownerPersonId: String?,
        @Column(name = "amount", precision = 19, scale = 2, nullable = false)
        val amount: BigDecimal,
        @Column(name = "recalculation_frequency", length = 50) val recalculationFrequency: String?,
        @Column(name = "consider_balance_on_insufficiency")
        val considerBalanceOnInsufficiency: Boolean = false,
        @Column(name = "start_date", nullable = false) val startDate: LocalDateTime,
        @Column(name = "end_date") val endDate: LocalDateTime?,
        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = 20, nullable = false)
        val status: CardReceivablesLockStatus,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),
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
        val contractInstallments: MutableList<CardReceivablesContractInstallment> = mutableListOf()
) {}

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_holder")
class CardReceivablesHolder(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLock? = null,
        @Column(name = "tax_id", length = 20, nullable = false) val taxId: String,
        @Column(name = "root_tax_id_operation") val rootTaxIdOperation: Boolean = false,
        @Column(name = "payment_account_branch", length = 10) val paymentAccountBranch: String?,
        @Column(name = "payment_account_number", length = 20) val paymentAccountNumber: String?,
        @Column(name = "payment_account_id", length = 26) val paymentAccountId: String?,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),
        @OneToMany(
                mappedBy = "cardReceivablesHolder",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val arrangements: MutableList<CardReceivablesOwnerArrangement> = mutableListOf(),
        @OneToMany(
                mappedBy = "cardReceivablesHolder",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val accreditors: MutableList<CardReceivablesOwnerAccreditor> = mutableListOf()
) {
  fun addArrangement(arrangement: CardReceivablesOwnerArrangement) {
    arrangement.cardReceivablesHolder = this
    arrangements.add(arrangement)
  }

  fun addAccreditor(accreditor: CardReceivablesOwnerAccreditor) {
    accreditor.cardReceivablesHolder = this
    accreditors.add(accreditor)
  }
}

package com.finapp.domain.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_contract_installments")
class CardReceivablesContractInstallment(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLock? = null,
        @Column(name = "installment_number", nullable = false) val installmentNumber: Int,
        @Column(name = "date", nullable = false) val date: LocalDate,
        @Column(name = "value", precision = 19, scale = 2, nullable = false) val value: BigDecimal,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock_cerc")
class CardReceivablesLockCerc(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLock? = null,
        @Column(name = "creation_retry_attempts", nullable = false)
        val creationRetryAttempts: Int = 0,
        @Column(name = "proactive_search_attempts", nullable = false)
        val proactiveSearchAttempts: Int = 0,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now(),
        @OneToMany(
                mappedBy = "cardReceivablesLockCerc",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY
        )
        val protocols: MutableList<CardReceivablesLockCercProtocol> = mutableListOf()
) {
  fun addProtocol(protocol: CardReceivablesLockCercProtocol) {
    protocol.cardReceivablesLockCerc = this
    protocols.add(protocol)
  }
}

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock_cerc_protocols")
class CardReceivablesLockCercProtocol(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_cerc_id", nullable = false)
        var cardReceivablesLockCerc: CardReceivablesLockCerc? = null,
        @Column(name = "action", length = 50, nullable = false) val action: String,
        @Column(name = "protocol", length = 100, nullable = false) val protocol: String,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_lock_nuclea")
class CardReceivablesLockNuclea(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_id", nullable = false)
        var cardReceivablesLock: CardReceivablesLock? = null,
        @Column(name = "creation_retry_attempts", nullable = false)
        val creationRetryAttempts: Int = 0,
        @Column(name = "proactive_search_attempts", nullable = false)
        val proactiveSearchAttempts: Int = 0,
        @Column(name = "protocol", length = 100) val protocol: String?,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now()
)

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_owner_accreditor")
class CardReceivablesOwnerAccreditor(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_holder_id", nullable = false)
        var cardReceivablesHolder: CardReceivablesHolder? = null,
        @Column(name = "accreditor_tax_id", length = 20, nullable = false)
        val accreditorTaxId: String,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now()
)

package com.finapp.domain.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "card_receivables_owner_arrangement")
class CardReceivablesOwnerArrangement(
        @Id @Column(name = "id", length = 26, nullable = false) val id: String,
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "card_receivables_lock_holder_id", nullable = false)
        var cardReceivablesHolder: CardReceivablesHolder? = null,
        @Column(name = "arrangement_code", length = 50, nullable = false)
        val arrangementCode: String,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "updated_at", nullable = false)
        val updatedAt: LocalDateTime = LocalDateTime.now()
)

package com.finapp.domain.enums

enum class CardReceivablesLockStatus {
  ACTIVE,
  INACTIVE,
  PENDING,
  CANCELLED,
  EXPIRED
}

```

```kotlin
@Component
class CardReceivablesLockCercDataAccessImpl(
        private val cardReceivablesLockCercRepository: CardReceivablesLockCercRepository
) : CardReceivablesLockCercDataAccess {

  override fun create(cardReceivablesLockCerc: CardReceivablesLockCerc): CardReceivablesLockCerc {
    return cardReceivablesLockCercRepository.save(cardReceivablesLockCerc)
  }

  override fun update(cardReceivablesLockCerc: CardReceivablesLockCerc): CardReceivablesLockCerc {
    return cardReceivablesLockCercRepository.save(cardReceivablesLockCerc)
  }

  override fun getById(id: String): CardReceivablesLockCerc? {
    return cardReceivablesLockCercRepository.findById(id).orElse(null)
  }

  override fun findByCardReceivablesLockId(
          cardReceivablesLockId: String
  ): CardReceivablesLockCerc? {
    return cardReceivablesLockCercRepository.findByCardReceivablesLockId(cardReceivablesLockId)
  }

  override fun incrementCreationRetryAttempts(id: String) {
    cardReceivablesLockCercRepository.incrementCreationRetryAttempts(id, LocalDateTime.now())
  }

  override fun incrementProactiveSearchAttempts(id: String) {
    cardReceivablesLockCercRepository.incrementProactiveSearchAttempts(id, LocalDateTime.now())
  }
}

import com.finapp.domain.entities.CardReceivablesLockCerc

interface CardReceivablesLockCercDataAccess {

  fun create(cardReceivablesLockCerc: CardReceivablesLockCerc): CardReceivablesLockCerc

  fun update(cardReceivablesLockCerc: CardReceivablesLockCerc): CardReceivablesLockCerc

  fun getById(id: String): CardReceivablesLockCerc?

  fun findByCardReceivablesLockId(cardReceivablesLockId: String): CardReceivablesLockCerc?

  fun incrementCreationRetryAttempts(id: String): Unit

  fun incrementProactiveSearchAttempts(id: String): Unit
}

@Component
class CardReceivablesLockDataAccessImpl(
        private val cardReceivablesLockRepository: CardReceivablesLockRepository,
        private val cardReceivablesLockNucleaRepository: CardReceivablesLockNucleaRepository,
        private val cardReceivablesLockCercRepository: CardReceivablesLockCercRepository
) : CardReceivablesLockDataAccess {

  override fun create(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto {
    val cardReceivablesLock =
            CardReceivablesLockMapper.dtoToEntityMapper.map(cardReceivablesLockDto)
    val savedCardReceivablesLock = cardReceivablesLockRepository.save(cardReceivablesLock)
    return CardReceivablesLockMapper.entityToDtoMapper.map(savedCardReceivablesLock)
  }

  override fun update(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto {
    val cardReceivablesLock =
            CardReceivablesLockMapper.dtoToEntityMapper.map(cardReceivablesLockDto)
    val updatedCardReceivablesLock = cardReceivablesLockRepository.save(cardReceivablesLock)
    return CardReceivablesLockMapper.entityToDtoMapper.map(updatedCardReceivablesLock)
  }

  override fun getById(id: String): CardReceivablesLockDto? {
    val cardReceivablesLock = cardReceivablesLockRepository.findById(id).orElse(null)
    return cardReceivablesLock?.let { CardReceivablesLockMapper.entityToDtoMapper.map(it) }
  }

  override fun incrementCreationRetryAttempts(id: String) {
    // Incrementa nas entidades Nuclea e Cerc relacionadas
    cardReceivablesLockNucleaRepository.incrementCreationRetryAttempts(id, LocalDateTime.now())
    cardReceivablesLockCercRepository.incrementCreationRetryAttempts(id, LocalDateTime.now())
  }

  override fun incrementProactiveSearchAttempts(id: String) {
    // Incrementa nas entidades Nuclea e Cerc relacionadas
    cardReceivablesLockNucleaRepository.incrementProactiveSearchAttempts(id, LocalDateTime.now())
    cardReceivablesLockCercRepository.incrementProactiveSearchAttempts(id, LocalDateTime.now())
  }
}

@Repository
interface CardReceivablesLockRepository : JpaRepository<CardReceivablesLock, String>

interface CardReceivablesLockDataAccess {

  fun create(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto

  fun update(cardReceivablesLockDto: CardReceivablesLockDto): CardReceivablesLockDto

  fun getById(id: String): CardReceivablesLockDto?

  fun incrementCreationRetryAttempts(id: String): Unit

  fun incrementProactiveSearchAttempts(id: String): Unit
}

import tech.mappie.api.ObjectMappie

object CardReceivablesLockMapper {
  object entityToDtoMapper : ObjectMappie<CardReceivablesLock, CardReceivablesLockDto>()
  object dtoToEntityMapper : ObjectMappie<CardReceivablesLockDto, CardReceivablesLock>()
}


```

```kotlin

```
