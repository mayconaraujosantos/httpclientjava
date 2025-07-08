package com.amil.rede.credenciadasimples.application.usecase;

import com.amil.rede.credenciadasimples.domain.model.CpfValidator;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;
import com.amil.rede.credenciadasimples.domain.repository.RedeCredenciadaRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FindRedeCredenciadaByCpfUseCaseImpl implements IFindRedeCredenciadaByCpfUseCase {
  private static final String EXCLUIDO_SITUACAO = "EXCLUIDO";
  private final RedeCredenciadaRepository repository;
  private final CpfValidator cpfValidator;

  public FindRedeCredenciadaByCpfUseCaseImpl(RedeCredenciadaRepository repository) {
    this.repository = repository;
    this.cpfValidator = new CpfValidator();
  }

  @Override
  public CompletableFuture<List<RedeCredenciada>> execute(String cpf) {
    String cleanCpf = cpfValidator.validateAndClean(cpf);
    return repository.findByCpf(cleanCpf); // .thenApply(this::filterActiveSituacao);
  }

  @Override
  public CompletableFuture<List<RedeCredenciada>> execute(
      String cpf, List<RedeCredenciadaFilter> filters) {
    String cleanCpf = cpfValidator.validateAndClean(cpf);
    return repository
        .findByCpf(cleanCpf)
        .thenApply(this::filterActiveSituacao)
        .thenApply(list -> applyFilters(list, filters));
  }

  private List<RedeCredenciada> filterActiveSituacao(List<RedeCredenciada> redeCredenciadaList) {
    return redeCredenciadaList.stream()
        .filter(rede -> !EXCLUIDO_SITUACAO.equals(rede.situacao()))
        .toList();
  }

  private List<RedeCredenciada> applyFilters(
      List<RedeCredenciada> redeCredenciadaList, List<RedeCredenciadaFilter> filters) {
    if (filters == null || filters.isEmpty()) {
      return redeCredenciadaList;
    }
    return redeCredenciadaList.stream()
        .filter(rede -> filters.stream().allMatch(filter -> filter.matches(rede)))
        .toList();
  }
}
