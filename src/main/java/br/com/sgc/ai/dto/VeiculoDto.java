package br.com.sgc.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VeiculoDto {
    
    private String placa;
    
    private String marca;
    
    private String modelo;
    
    private String cor;
    
    private String ano;
	
}
