package com.amil.rede.credenciadasimples.application.usecase;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import com.amil.rede.credenciadasimples.domain.repository.RedeCredenciadaRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Example demonstrating how to use the flexible filtering system. */
public class FilterUsageExample {
  private final IFindRedeCredenciadaByCpfUseCase useCase;

  public FilterUsageExample(RedeCredenciadaRepository repository) {
    this.useCase = new FindRedeCredenciadaByCpfUseCaseImpl(repository);
  }

  /** Example 1: Filter by codigoRede and modalidade */
  public CompletableFuture<List<RedeCredenciada>> filterByCodigoRedeAndModalidade(String cpf) {
    List<RedeCredenciadaFilter> filters =
        RedeCredenciadaFilterBuilder.create().codigoRede(879).modalidade("SAUDE").build();

    return useCase.execute(cpf, filters);
  }

  /** Example 2: Filter by unidade and contexto */
  public CompletableFuture<List<RedeCredenciada>> filterByUnidadeAndContexto(String cpf) {
    List<RedeCredenciadaFilter> filters =
        RedeCredenciadaFilterBuilder.create().unidade("Rio de Janeiro").contexto("AMIL").build();

    return useCase.execute(cpf, filters);
  }

  /** Example 3: Filter by numeroCarterinha */
  public CompletableFuture<List<RedeCredenciada>> filterByNumeroCarterinha(String cpf) {
    List<RedeCredenciadaFilter> filters =
        RedeCredenciadaFilterBuilder.create().numeroCarterinha("085818753").build();

    return useCase.execute(cpf, filters);
  }

  /** Example 4: Complex filter with multiple criteria */
  public CompletableFuture<List<RedeCredenciada>> complexFilter(String cpf) {
    List<RedeCredenciadaFilter> filters =
        RedeCredenciadaFilterBuilder.create()
            .codigoRede(879)
            .modalidade("SAUDE")
            .unidade("Rio de Janeiro")
            .contexto("AMIL")
            .numeroCarterinha("085818753")
            .build();

    return useCase.execute(cpf, filters);
  }

  /** Example 5: No filters (returns all active records) */
  public CompletableFuture<List<RedeCredenciada>> noFilters(String cpf) {
    return useCase.execute(cpf);
  }
}
