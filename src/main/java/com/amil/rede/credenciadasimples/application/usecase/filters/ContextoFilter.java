package com.amil.rede.credenciadasimples.application.usecase.filters;

import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/** Filter for contexto field. */
public class ContextoFilter implements RedeCredenciadaFilter {
  private final String contexto;

  public ContextoFilter(String contexto) {
    this.contexto = contexto;
  }

  @Override
  public boolean matches(RedeCredenciada redeCredenciada) {
    if (contexto == null || contexto.trim().isEmpty()) {
      return true; // No filter applied
    }
    return contexto.equalsIgnoreCase(redeCredenciada.contexto());
  }
}
