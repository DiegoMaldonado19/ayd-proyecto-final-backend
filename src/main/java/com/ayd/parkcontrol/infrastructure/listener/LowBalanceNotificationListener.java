package com.ayd.parkcontrol.infrastructure.listener;

import com.ayd.parkcontrol.application.event.SubscriptionHoursUpdatedEvent;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener para notificaciones de saldo bajo de horas en suscripciones.
 * 
 * Regla de Negocio: Notificar al usuario cuando consume el 80% o 100% de sus
 * horas
 * mensuales. Las notificaciones se envían solo una vez por período mensual.
 * 
 * @author ParkControl Team
 * @version 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LowBalanceNotificationListener {

    private final EmailService emailService;

    // Sets para tracking de notificaciones enviadas (thread-safe)
    private final Set<String> notifiedAt80 = ConcurrentHashMap.newKeySet();
    private final Set<String> notifiedAt100 = ConcurrentHashMap.newKeySet();

    private static final BigDecimal THRESHOLD_80_PERCENT = BigDecimal.valueOf(80);
    private static final BigDecimal THRESHOLD_100_PERCENT = BigDecimal.valueOf(100);

    /**
     * Maneja el evento de actualización de horas consumidas.
     * Se ejecuta asíncronamente para no bloquear el flujo principal.
     * 
     * @param event evento con información de la suscripción actualizada
     */
    @EventListener
    @Async
    public void handleSubscriptionHoursUpdated(SubscriptionHoursUpdatedEvent event) {
        BigDecimal percentage = event.getPercentage();
        String key = String.valueOf(event.getSubscriptionId());

        try {
            // Notificar al 80%
            if (percentage.compareTo(THRESHOLD_80_PERCENT) >= 0 && !notifiedAt80.contains(key)) {
                sendLowBalanceAlert(event);
                notifiedAt80.add(key);
                log.info("Notificación de 80% enviada para suscripción ID: {}", event.getSubscriptionId());
            }

            // Notificar al 100%
            if (percentage.compareTo(THRESHOLD_100_PERCENT) >= 0 && !notifiedAt100.contains(key)) {
                sendBalanceDepletedAlert(event);
                notifiedAt100.add(key);
                log.info("Notificación de 100% enviada para suscripción ID: {}", event.getSubscriptionId());
            }
        } catch (Exception e) {
            log.error("Error enviando notificación de saldo bajo para suscripción ID: {}",
                    event.getSubscriptionId(), e);
        }
    }

    /**
     * Envía alerta cuando se alcanza el 80% de las horas.
     */
    private void sendLowBalanceAlert(SubscriptionHoursUpdatedEvent event) {
        String subject = "⚠️ Has consumido el 80% de tus horas mensuales";
        String body = String.format(
                "Hola,\n\n" +
                        "Te informamos que has consumido el %.2f%% de tus %d horas mensuales.\n" +
                        "Horas consumidas: %.2f\n\n" +
                        "Te recomendamos renovar tu suscripción pronto para evitar cargos adicionales.\n\n" +
                        "Saludos,\n" +
                        "Equipo ParkControl",
                event.getPercentage(),
                event.getMonthlyHours(),
                event.getHoursConsumed());

        emailService.sendEmail(event.getUserEmail(), subject, body);
    }

    /**
     * Envía alerta cuando se alcanza el 100% de las horas.
     */
    private void sendBalanceDepletedAlert(SubscriptionHoursUpdatedEvent event) {
        String subject = "🔴 Has agotado tus horas mensuales";
        String body = String.format(
                "Hola,\n\n" +
                        "Te informamos que has agotado tus %d horas mensuales.\n" +
                        "Horas consumidas: %.2f\n\n" +
                        "A partir de ahora, se aplicarán cargos adicionales por hora consumida.\n" +
                        "Renueva tu suscripción para seguir disfrutando de tus beneficios.\n\n" +
                        "Saludos,\n" +
                        "Equipo ParkControl",
                event.getMonthlyHours(),
                event.getHoursConsumed());

        emailService.sendEmail(event.getUserEmail(), subject, body);
    }

    /**
     * Job programado que limpia el tracking de notificaciones al inicio de cada
     * mes.
     * Se ejecuta el día 1 de cada mes a las 00:00.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyNotifications() {
        int count80 = notifiedAt80.size();
        int count100 = notifiedAt100.size();

        notifiedAt80.clear();
        notifiedAt100.clear();

        log.info("Notificaciones mensuales reseteadas. Total 80%: {}, Total 100%: {}", count80, count100);
    }
}
