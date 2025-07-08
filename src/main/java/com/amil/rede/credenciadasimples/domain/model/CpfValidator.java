package com.amil.rede.credenciadasimples.domain.model;

public class CpfValidator {

  public String validateAndClean(String cpf) {
    if (cpf == null || cpf.trim().isEmpty()) {
      throw new IllegalArgumentException("CPF cannot be null or empty");
    }

    String cleanCpf = cpf.replaceAll("\\D", "");
    if (cleanCpf.length() != 11) {
      throw new IllegalArgumentException("CPF must have 11 digits");
    }

    return cleanCpf;
  }
}
