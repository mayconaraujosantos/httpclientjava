package com.amil.rede.credenciadasimples.application.usecase.filters;

import com.amil.rede.credenciadasimples.application.usecase.RedeCredenciadaFilter;
import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/** Filter for modalidade field. */
public class ModalidadeFilter implements RedeCredenciadaFilter {
  private final String modalidade;

  public ModalidadeFilter(String modalidade) {
    this.modalidade = modalidade;
  }

  @Override
  public boolean matches(RedeCredenciada redeCredenciada) {
    if (modalidade == null || modalidade.trim().isEmpty()) {
      return true; // No filter applied
    }
    return modalidade.equalsIgnoreCase(redeCredenciada.modalidade());
  }
}
