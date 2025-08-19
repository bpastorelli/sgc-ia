package br.com.sgc.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.sgc.ai.dto.ProdutoRequestDto;
import br.com.sgc.ai.dto.VeiculoDto;
import br.com.sgc.ai.service.ProdutoService;

@RestController
@RequestMapping("/api/veiculos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping("/create-from-chatgpt")
    public ResponseEntity<VeiculoDto> createProduct(@RequestBody ProdutoRequestDto request) {
    	VeiculoDto newProduct = produtoService.createProductFromChatGpt(request);
        return ResponseEntity.ok(newProduct);
    }
	
}
