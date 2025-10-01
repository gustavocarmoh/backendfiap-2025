package fiap.backend.dto;

/**
 * DTO para response de análise nutricional
 */
public class AnaliseNutricionalResponse {
    
    private Boolean sucesso;
    private String mensagem;
    private Double scoreSaude;
    private String classificacao;
    private Integer alertasGerados;
    private String relatorio;
    private Integer tamanhoRelatorio;
    
    public AnaliseNutricionalResponse() {
    }
    
    public AnaliseNutricionalResponse(Boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }
    
    // Getters and Setters
    
    public Boolean getSucesso() {
        return sucesso;
    }
    
    public void setSucesso(Boolean sucesso) {
        this.sucesso = sucesso;
    }
    
    public String getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
    
    public Double getScoreSaude() {
        return scoreSaude;
    }
    
    public void setScoreSaude(Double scoreSaude) {
        this.scoreSaude = scoreSaude;
    }
    
    public String getClassificacao() {
        return classificacao;
    }
    
    public void setClassificacao(String classificacao) {
        this.classificacao = classificacao;
    }
    
    public Integer getAlertasGerados() {
        return alertasGerados;
    }
    
    public void setAlertasGerados(Integer alertasGerados) {
        this.alertasGerados = alertasGerados;
    }
    
    public String getRelatorio() {
        return relatorio;
    }
    
    public void setRelatorio(String relatorio) {
        this.relatorio = relatorio;
    }
    
    public Integer getTamanhoRelatorio() {
        return tamanhoRelatorio;
    }
    
    public void setTamanhoRelatorio(Integer tamanhoRelatorio) {
        this.tamanhoRelatorio = tamanhoRelatorio;
    }
}
