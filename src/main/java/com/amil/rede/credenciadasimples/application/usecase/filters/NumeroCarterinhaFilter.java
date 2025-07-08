package com.amil.rede.credenciadasimples.application.usecase.filters;

import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/** Filter for numeroCarterinha field. */
public class NumeroCarterinhaFilter implements RedeCredenciadaFilter {
  private final String numeroCarterinha;

  public NumeroCarterinhaFilter(String numeroCarterinha) {
    this.numeroCarterinha = numeroCarterinha;
  }

  @Override
  public boolean matches(RedeCredenciada redeCredenciada) {
    if (numeroCarterinha == null || numeroCarterinha.trim().isEmpty()) {
      return true; // No filter applied
    }
    return numeroCarterinha.equals(redeCredenciada.numeroCarterinha());
  }
}
