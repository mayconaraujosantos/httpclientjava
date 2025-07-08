package com.amil.rede.credenciadasimples.application.usecase.filters;

import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/** Filter for unidade field. */
public class UnidadeFilter implements RedeCredenciadaFilter {
  private final String unidade;

  public UnidadeFilter(String unidade) {
    this.unidade = unidade;
  }

  @Override
  public boolean matches(RedeCredenciada redeCredenciada) {
    if (unidade == null || unidade.trim().isEmpty()) {
      return true; // No filter applied
    }
    return unidade.equalsIgnoreCase(redeCredenciada.unidade());
  }
}
