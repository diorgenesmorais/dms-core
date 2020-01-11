package com.dms.useful.exception.handler;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Este é um mock que representa um modelo (entity).
 * 
 * @author Diorgenes Morais
 *
 */
public class Model {

	@NotNull
	private String nome;
	@NotEmpty
	private String sobrenome;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getSobrenome() {
		return sobrenome;
	}

	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}
}
