package pedroMartinsMJ.MyStreaming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuração de thread pool para processamento assíncrono.
 * Usado principalmente pelo EncodingService para jobs de encoding não-bloqueantes.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "encodingExecutor")
    public Executor encodingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool: threads mínimas mantidas vivas
        executor.setCorePoolSize(2);
        
        // Max pool: threads máximas sob carga (limita concorrência de encoding)
        executor.setMaxPoolSize(4);
        
        // Fila de espera quando todos os threads estão ocupados
        executor.setQueueCapacity(10);
        
        // Prefixo para nomes de threads (útil para debugging)
        executor.setThreadNamePrefix("encoding-");
        
        // Rejeitar novos jobs se a fila estiver cheia (em vez de travar)
        executor.setRejectedExecutionHandler((r, e) -> {
            throw new RuntimeException("Encoding pool saturado: " + 
                "todos os threads ocupados e fila cheia", new IllegalStateException(
                    "Max concurrent encoding jobs reached"));
        });
        
        executor.initialize();
        return executor;
    }
}