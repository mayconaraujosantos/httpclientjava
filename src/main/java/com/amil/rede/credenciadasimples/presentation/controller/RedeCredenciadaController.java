package com.amil.rede.credenciadasimples.presentation.controller;

import com.amil.rede.credenciadasimples.application.usecase.IFindRedeCredenciadaByCpfUseCase;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for RedeCredenciada operations. Follows Clean Architecture by depending on use cases
 * instead of direct repository access.
 */
public class RedeCredenciadaController {

  private final IFindRedeCredenciadaByCpfUseCase findRedeCredenciadaByCpfUseCase;

  public RedeCredenciadaController(
      IFindRedeCredenciadaByCpfUseCase findRedeCredenciadaByCpfUseCase) {
    this.findRedeCredenciadaByCpfUseCase = findRedeCredenciadaByCpfUseCase;
  }

  /**
   * Finds rede credenciada by CPF.
   *
   * @param cpf The CPF to search for
   * @return CompletableFuture containing list of RedeCredenciada
   */
  public CompletableFuture<List<RedeCredenciada>> findByCpf(String cpf) {
    return findRedeCredenciadaByCpfUseCase.execute(cpf);
  }
}
