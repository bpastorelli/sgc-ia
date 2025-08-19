package br.com.sgc.ai.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProdutoRequestDto {
	
	private String userQuery;

}
