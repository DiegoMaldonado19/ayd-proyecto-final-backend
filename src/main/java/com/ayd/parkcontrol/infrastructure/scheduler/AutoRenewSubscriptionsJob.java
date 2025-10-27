package com.ayd.parkcontrol.infrastructure.scheduler;

import com.ayd.parkcontrol.application.dto.request.subscription.PurchaseSubscriptionRequest;
import com.ayd.parkcontrol.application.port.notification.EmailService;
import com.ayd.parkcontrol.application.usecase.subscription.PurchaseSubscriptionUseCase;
import com.ayd.parkcontrol.domain.model.subscription.Subscription;
import com.ayd.parkcontrol.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job programado para procesar renovaciones automáticas de suscripciones.
 * 
 * Regla de Negocio: Las suscripciones con auto_renew_enabled=true se renuevan
 * automáticamente
 * 7 días antes de su vencimiento. Se notifica al usuario por email del
 * resultado.
 * 
 * Ejecución: Diariamente a las 2:00 AM
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AutoRenewSubscriptionsJob {

    private static final int DAYS_BEFORE_EXPIRY = 7;

    private final SubscriptionRepository subscriptionRepository;
    private final PurchaseSubscriptionUseCase purchaseSubscriptionUseCase;
    private final EmailService emailService;

    /**
     * Procesa renovaciones automáticas de suscripciones próximas a vencer.
     * Cron: 0 0 2 * * * = Todos los días a las 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void processAutoRenewals() {
        log.info("Iniciando proceso de renovación automática de suscripciones");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startWindow = now;
        LocalDateTime endWindow = now.plusDays(DAYS_BEFORE_EXPIRY);

        List<Subscription> expiring = subscriptionRepository.findExpiringSoonWithAutoRenew(startWindow, endWindow);

        log.info("Encontradas {} suscripciones para renovación automática", expiring.size());

        int successCount = 0;
        int failureCount = 0;

        for (Subscription subscription : expiring) {
            try {
                renewSubscription(subscription);
                successCount++;
            } catch (Exception e) {
                log.error("Error renovando suscripción ID: {} para usuario: {}",
                        subscription.getId(), subscription.getUserId(), e);
                sendRenewalFailureEmail(subscription, e.getMessage());
                failureCount++;
            }
        }

        log.info("Proceso de renovación automática completado. Exitosas: {}, Fallidas: {}",
                successCount, failureCount);
    }

    /**
     * Renueva una suscripción creando una nueva con los mismos parámetros.
     */
    private void renewSubscription(Subscription subscription) {
        log.debug("Renovando suscripción ID: {} para usuario: {}",
                subscription.getId(), subscription.getUserId());

        PurchaseSubscriptionRequest renewalRequest = PurchaseSubscriptionRequest.builder()
                .planId(subscription.getPlanId())
                .licensePlate(subscription.getLicensePlate())
                .isAnnual(subscription.getIsAnnual())
                .autoRenewEnabled(true) // Mantener auto-renovación activa
                .build();

        purchaseSubscriptionUseCase.execute(renewalRequest, subscription.getUserId());

        sendRenewalSuccessEmail(subscription);

        log.info("Suscripción renovada exitosamente. Original ID: {}, Usuario: {}",
                subscription.getId(), subscription.getUserId());
    }

    /**
     * Envía email de confirmación de renovación exitosa.
     */
    private void sendRenewalSuccessEmail(Subscription subscription) {
        if (subscription.getUserEmail() == null) {
            log.warn("No se puede enviar email de renovación: usuario sin email configurado. Subscription ID: {}",
                    subscription.getId());
            return;
        }

        String subject = "✅ Suscripción renovada automáticamente - ParkControl";
        String body = buildSuccessEmailBody(subscription);

        try {
            emailService.sendEmail(subscription.getUserEmail(), subject, body);
            log.debug("Email de renovación exitosa enviado a: {}", subscription.getUserEmail());
        } catch (Exception e) {
            log.error("Error enviando email de renovación exitosa a: {}", subscription.getUserEmail(), e);
        }
    }

    /**
     * Envía email notificando fallo en la renovación automática.
     */
    private void sendRenewalFailureEmail(Subscription subscription, String errorMessage) {
        if (subscription.getUserEmail() == null) {
            log.warn("No se puede enviar email de fallo: usuario sin email configurado. Subscription ID: {}",
                    subscription.getId());
            return;
        }

        String subject = "⚠️ Fallo en renovación automática de suscripción - ParkControl";
        String body = buildFailureEmailBody(subscription, errorMessage);

        try {
            emailService.sendEmail(subscription.getUserEmail(), subject, body);
            log.debug("Email de fallo en renovación enviado a: {}", subscription.getUserEmail());
        } catch (Exception e) {
            log.error("Error enviando email de fallo en renovación a: {}", subscription.getUserEmail(), e);
        }
    }

    private String buildSuccessEmailBody(Subscription subscription) {
        return String.format("""
                Estimado/a usuario/a,

                Su suscripción ha sido renovada automáticamente con éxito.

                Detalles de la renovación:
                - Placa: %s
                - Plan: %s
                - Tipo: %s
                - Vigencia anterior: %s

                La nueva suscripción estará activa desde la fecha de vencimiento de la anterior.

                Si no desea que su suscripción se renueve automáticamente en el futuro,
                puede desactivar esta opción desde su panel de usuario.

                Saludos,
                Equipo ParkControl
                """,
                subscription.getLicensePlate(),
                subscription.getPlan() != null ? subscription.getPlan().getPlanTypeName()
                        : "Plan ID: " + subscription.getPlanId(),
                subscription.getIsAnnual() ? "Anual" : "Mensual",
                subscription.getEndDate());
    }

    private String buildFailureEmailBody(Subscription subscription, String errorMessage) {
        return String.format("""
                Estimado/a usuario/a,

                No pudimos renovar automáticamente su suscripción.

                Detalles:
                - Placa: %s
                - Fecha de vencimiento: %s
                - Motivo: %s

                Para mantener activo su servicio, por favor renueve su suscripción manualmente
                desde su panel de usuario antes de la fecha de vencimiento.

                Si necesita asistencia, contáctenos.

                Saludos,
                Equipo ParkControl
                """,
                subscription.getLicensePlate(),
                subscription.getEndDate(),
                errorMessage);
    }
}
