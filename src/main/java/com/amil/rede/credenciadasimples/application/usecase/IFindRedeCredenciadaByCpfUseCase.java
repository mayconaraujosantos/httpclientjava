package com.amil.rede.credenciadasimples.application.usecase;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Use case for finding rede credenciada by CPF. Follows Single Responsibility Principle and Clean
 * Architecture.
 */
public interface IFindRedeCredenciadaByCpfUseCase {
  /**
   * Executes the use case to find rede credenciada by CPF. Filters out results where situacao is
   * "EXCLUIDO" to return only active records.
   *
   * @param cpf The CPF to search for (must be valid format)
   * @return CompletableFuture containing list of active RedeCredenciada (situacao != "EXCLUIDO")
   * @throws IllegalArgumentException if CPF is null or invalid
   */
  CompletableFuture<List<RedeCredenciada>> execute(String cpf);

  /**
   * Executes the use case with additional filters.
   *
   * @param cpf The CPF to search for (must be valid format)
   * @param filters List of filters to apply
   * @return CompletableFuture containing filtered list of active RedeCredenciada
   * @throws IllegalArgumentException if CPF is null or invalid
   */
  CompletableFuture<List<RedeCredenciada>> execute(String cpf, List<RedeCredenciadaFilter> filters);
}
