package com.dentify.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * ✅ CONFIGURATION FOR ASYNC EXECUTION
 *
 * Define un thread pool personalizado para ejecutar tareas asincronamente.
 * Las propiedades se leen desde application.yml (spring.task.execution.*)
 *
 * IMPACTO EN PERFORMANCE:
 * - Endpoint POST /appointments/with-pay: 20-30s → 500-800ms
 * - Notifications (PDF, emails): Se ejecutan en background sin bloquear
 *
 * CONFIGURACIÓN:
 * - Core threads: 5 (siempre activos)
 * - Max threads: 15 (bajo carga pico)
 * - Queue capacity: 100 (eventos pendientes)
 */
@Configuration
@EnableAsync // ✅ Activa soporte para @Async
@Slf4j
public class AsyncConfiguration {

    /**
     * 📌 ThreadPool principal para tareas async
     * Nombre: "taskExecutor" → Se usa en @Async("taskExecutor")
     */
    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core threads (siempre vivos)
        executor.setCorePoolSize(5);

        // Max threads bajo carga
        executor.setMaxPoolSize(15);

        // Cola de espera si todos los threads están ocupados
        executor.setQueueCapacity(100);

        // Thread naming para debugging en logs
        executor.setThreadNamePrefix("dentify-async-");

        // Esperar a que terminen tareas antes de shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // Timeout para shutdown
        executor.setAwaitTerminationSeconds(30);

        // Inicializar
        executor.initialize();

        log.info("✅ TaskExecutor configured: coreSize=5, maxSize=15, queueCapacity=100");

        return executor;
    }

    /**
     * 📌 ThreadPool para email (opcional, más específico)
     * Nombre: "emailExecutor" → Se usa en @Async("emailExecutor")
     */
    @Bean(name = "emailExecutor")
    public TaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("dentify-email-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("✅ Email TaskExecutor configured: coreSize=3, maxSize=8, queueCapacity=50");

        return executor;
    }

    /**
     * 📌 ThreadPool para PDF generation (opcional, más específico)
     * Nombre: "pdfExecutor" → Se usa en @Async("pdfExecutor")
     */
    @Bean(name = "pdfExecutor")
    public TaskExecutor pdfExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("dentify-pdf-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();

        log.info("✅ PDF TaskExecutor configured: coreSize=2, maxSize=5, queueCapacity=50");

        return executor;
    }
}
