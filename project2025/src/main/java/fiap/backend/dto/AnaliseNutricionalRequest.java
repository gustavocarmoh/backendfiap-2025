package fiap.backend.dto;

import java.time.LocalDate;

/**
 * DTO para request de análise nutricional
 */
public class AnaliseNutricionalRequest {
    
    private String userId;
    private Integer diasAnalise;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    
    public AnaliseNutricionalRequest() {
    }
    
    public AnaliseNutricionalRequest(String userId, Integer diasAnalise) {
        this.userId = userId;
        this.diasAnalise = diasAnalise;
    }
    
    // Getters and Setters
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Integer getDiasAnalise() {
        return diasAnalise;
    }
    
    public void setDiasAnalise(Integer diasAnalise) {
        this.diasAnalise = diasAnalise;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }
}
