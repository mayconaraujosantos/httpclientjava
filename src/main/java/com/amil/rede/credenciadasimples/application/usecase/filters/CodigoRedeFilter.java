package com.amil.rede.credenciadasimples.application.usecase.filters;

import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/** Filter for codigoRede field. */
public class CodigoRedeFilter implements RedeCredenciadaFilter {
  private final Integer codigoRede;

  public CodigoRedeFilter(Integer codigoRede) {
    this.codigoRede = codigoRede;
  }

  @Override
  public boolean matches(RedeCredenciada redeCredenciada) {
    if (codigoRede == null) {
      return true; // No filter applied
    }
    return codigoRede.equals(redeCredenciada.codigoRede());
  }
}
