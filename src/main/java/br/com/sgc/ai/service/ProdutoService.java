package br.com.sgc.ai.service;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;

import br.com.sgc.ai.dto.ProdutoRequestDto;
import br.com.sgc.ai.dto.VeiculoDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProdutoService {
	
    @Autowired
    private OpenAiService openAiService;
    
    private final String apiKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    public ProdutoService(WebClient.Builder webClientBuilder, @Value("${openai.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1/chat/completions").build();
        this.objectMapper = new ObjectMapper();
        this.apiKey = apiKey;
    }
    
    public VeiculoDto createProductFromChatGpt(ProdutoRequestDto request) {
        
        //String prompt = "Baseado na seguinte pesquisa: '" + request.getUserQuery() + "'. Gere um JSON com os seguintes campos: 'placa' (string), 'marca' (string), 'modelo' (string), 'cor' (string) e 'ano' (string). O preço deve ser um valor numérico válido, e a descrição deve ser detalhada. Não inclua texto extra, apenas o objeto JSON.";
    	String prompt = "Baseado na seguinte pesquisa: ' Pesquise um veículo " + request.getUserQuery() + ". Onde o modelo é o nome registrado do veículo'. Gere um JSON com os seguintes campos: 'placa' (string), 'marca' (string), 'modelo' (string), 'cor' (string) e 'ano' (string). Onde o ano é o ano de lançamento do modelo. Com as letras em maiusculo. Não inclua texto extra, apenas o objeto JSON.";

    	
        // 2. Envia a requisição para a API da OpenAI
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4.1") // ou o modelo que você preferir
                .messages(List.of(
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), "Você é um assistente que pesquisa informações de produtos no formato JSON."),
                        new ChatMessage(ChatMessageRole.USER.value(), prompt)
                ))
                .build();

        // 3. Obtém a resposta do ChatGPT
        ChatMessage responseMessage = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0).getMessage();
        String jsonResponse = responseMessage.getContent();

        try {
            // 4. Desserializa o JSON para um objeto Product
        	VeiculoDto product = objectMapper.readValue(jsonResponse, VeiculoDto.class);
      
            
            log.info("Produto salvo com sucesso: " + product.getMarca() + " " + product.getModelo());
            return product;
        } catch (Exception e) {
            log.error("Erro ao processar a resposta do ChatGPT: " + e.getMessage());
            throw new RuntimeException("Não foi possível gerar e salvar o produto.", e);
        }
    }
        
    public String extractTextFromImage(byte[] imageBytes) throws IOException {
        // 1. Codifica a imagem em Base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 2. Constrói o prompt e o payload da requisição
        String prompt = "Extraia todas as informações textuais do documento de identidade na imagem. Não inclua texto que não esteja visível. Não invente dados.";
        
        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4o", // Use um modelo que suporte visão (como o GPT-4o)
            "messages", List.of(
                Map.of("role", "user",
                       "content", List.of(
                           Map.of("type", "text", "text", prompt),
                           Map.of("type", "image_url",
                                  "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image))
                       ))
            ),
            "max_tokens", 500
        );

        // 3. Faz a chamada HTTP para a API da OpenAI
        String responseJson = webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String.class)
            .block(); // .block() para simplificar o exemplo. Em um ambiente reativo, você usaria o Mono.

        // 4. Extrai o texto da resposta JSON
        JsonNode rootNode = objectMapper.readTree(responseJson);
        return rootNode.path("choices").get(0).path("message").path("content").asText();
    }

}
