package com.amil.rede.credenciadasimples.application.usecase;

import com.amil.rede.credenciadasimples.application.usecase.filters.CodigoRedeFilter;
import com.amil.rede.credenciadasimples.application.usecase.filters.ContextoFilter;
import com.amil.rede.credenciadasimples.application.usecase.filters.ModalidadeFilter;
import com.amil.rede.credenciadasimples.application.usecase.filters.NumeroCarterinhaFilter;
import com.amil.rede.credenciadasimples.application.usecase.filters.UnidadeFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder class for creating RedeCredenciadaFilter lists. Provides a fluent API for building
 * complex filter combinations.
 */
public class RedeCredenciadaFilterBuilder {
  private final List<RedeCredenciadaFilter> filters = new ArrayList<>();

  /**
   * Adds a codigoRede filter.
   *
   * @param codigoRede The codigoRede to filter by
   * @return this builder for method chaining
   */
  public RedeCredenciadaFilterBuilder codigoRede(Integer codigoRede) {
    if (codigoRede != null) {
      filters.add(new CodigoRedeFilter(codigoRede));
    }
    return this;
  }

  /**
   * Adds a modalidade filter.
   *
   * @param modalidade The modalidade to filter by
   * @return this builder for method chaining
   */
  public RedeCredenciadaFilterBuilder modalidade(String modalidade) {
    if (modalidade != null && !modalidade.trim().isEmpty()) {
      filters.add(new ModalidadeFilter(modalidade));
    }
    return this;
  }

  /**
   * Adds a unidade filter.
   *
   * @param unidade The unidade to filter by
   * @return this builder for method chaining
   */
  public RedeCredenciadaFilterBuilder unidade(String unidade) {
    if (unidade != null && !unidade.trim().isEmpty()) {
      filters.add(new UnidadeFilter(unidade));
    }
    return this;
  }

  /**
   * Adds a numeroCarterinha filter.
   *
   * @param numeroCarterinha The numeroCarterinha to filter by
   * @return this builder for method chaining
   */
  public RedeCredenciadaFilterBuilder numeroCarterinha(String numeroCarterinha) {
    if (numeroCarterinha != null && !numeroCarterinha.trim().isEmpty()) {
      filters.add(new NumeroCarterinhaFilter(numeroCarterinha));
    }
    return this;
  }

  /**
   * Adds a contexto filter.
   *
   * @param contexto The contexto to filter by
   * @return this builder for method chaining
   */
  public RedeCredenciadaFilterBuilder contexto(String contexto) {
    if (contexto != null && !contexto.trim().isEmpty()) {
      filters.add(new ContextoFilter(contexto));
    }
    return this;
  }

  /**
   * Builds and returns the list of filters.
   *
   * @return List of RedeCredenciadaFilter
   */
  public List<RedeCredenciadaFilter> build() {
    return new ArrayList<>(filters);
  }

  /**
   * Creates a new filter builder instance.
   *
   * @return new RedeCredenciadaFilterBuilder
   */
  public static RedeCredenciadaFilterBuilder create() {
    return new RedeCredenciadaFilterBuilder();
  }
}
