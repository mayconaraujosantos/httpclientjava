package com.amil.rede.credenciadasimples.application.usecase;

import com.amil.rede.credenciadasimples.domain.model.RedeCredenciada;

/**
 * Interface for filtering RedeCredenciada records. Follows Strategy Pattern for flexible filtering.
 */
public interface RedeCredenciadaFilter {

  /**
   * Checks if the given RedeCredenciada matches this filter criteria.
   *
   * @param redeCredenciada The record to check
   * @return true if the record matches the filter criteria, false otherwise
   */
  boolean matches(RedeCredenciada redeCredenciada);
}
