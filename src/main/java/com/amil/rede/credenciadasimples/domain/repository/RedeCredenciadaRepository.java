package com.amil.rede.credenciadasimples.domain.repository;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Repository interface for RedeCredenciada operations. Follows the Repository pattern and
 * Dependency Inversion Principle.
 */
public interface RedeCredenciadaRepository {

  /**
   * Finds all rede credenciada records for a given CPF. Returns a CompletableFuture for async
   * operations.
   *
   * @param cpf The CPF to search for
   * @return CompletableFuture containing list of RedeCredenciada
   */
  CompletableFuture<List<RedeCredenciada>> findByCpf(String cpf);
}
