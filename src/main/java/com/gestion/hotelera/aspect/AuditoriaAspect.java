package com.gestion.hotelera.aspect;

import com.gestion.hotelera.service.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditoriaAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaAspect.class);
    private final AuditoriaService auditoriaService;

    public AuditoriaAspect(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @AfterReturning(pointcut = "(execution(* com.gestion.hotelera.service.*.crear*(..)) || " +
            "execution(* com.gestion.hotelera.service.*.registrar*(..))) && " +
            "!execution(* com.gestion.hotelera.service.AuditoriaService.*(..))", returning = "result")
    public void auditarCreacion(JoinPoint joinPoint, Object result) {
        try {
            String metodo = joinPoint.getSignature().getName();
            String servicio = joinPoint.getTarget().getClass().getSimpleName();
            String entidad = extraerEntidad(servicio);

            Long entidadId = extraerId(result);
            String detalle = String.format("Creación de %s mediante %s", entidad, metodo);

            auditoriaService.registrarAccion("CREAR", detalle, entidad, entidadId);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de creación", e);
        }
    }

    @AfterReturning(pointcut = "execution(* com.gestion.hotelera.service.*.actualizar*(..)) || " +
            "execution(* com.gestion.hotelera.service.*.modificar*(..))")
    public void auditarActualizacion(JoinPoint joinPoint) {
        try {
            String metodo = joinPoint.getSignature().getName();
            String servicio = joinPoint.getTarget().getClass().getSimpleName();
            String entidad = extraerEntidad(servicio);

            Object[] args = joinPoint.getArgs();
            Long entidadId = extraerIdDeArgs(args);
            String detalle = String.format("Actualización de %s mediante %s", entidad, metodo);

            auditoriaService.registrarAccion("ACTUALIZAR", detalle, entidad, entidadId);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de actualización", e);
        }
    }

    @AfterReturning(pointcut = "execution(* com.gestion.hotelera.service.*.eliminar*(..)) || " +
            "execution(* com.gestion.hotelera.service.*.cancelar*(..))")
    public void auditarEliminacion(JoinPoint joinPoint) {
        try {
            String metodo = joinPoint.getSignature().getName();
            String servicio = joinPoint.getTarget().getClass().getSimpleName();
            String entidad = extraerEntidad(servicio);

            Object[] args = joinPoint.getArgs();
            Long entidadId = extraerIdDeArgs(args);
            String detalle = String.format("Eliminación/Cancelación de %s mediante %s", entidad, metodo);

            auditoriaService.registrarAccion("ELIMINAR", detalle, entidad, entidadId);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de eliminación", e);
        }
    }

    @AfterReturning(pointcut = "execution(* com.gestion.hotelera.service.ReservaService.realizarCheckIn(..))")
    public void auditarCheckIn(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Long reservaId = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
            String detalle = "Check-in realizado para la reserva";
            auditoriaService.registrarAccion("CHECK_IN", detalle, "Reserva", reservaId);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de check-in", e);
        }
    }

    @AfterReturning(pointcut = "execution(* com.gestion.hotelera.service.ReservaService.realizarCheckOut(..))")
    public void auditarCheckOut(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            Long reservaId = args.length > 0 && args[0] instanceof Long ? (Long) args[0] : null;
            String detalle = "Check-out realizado para la reserva";
            auditoriaService.registrarAccion("CHECK_OUT", detalle, "Reserva", reservaId);
        } catch (Exception e) {
            log.warn("Error al registrar auditoría de check-out", e);
        }
    }

    private String extraerEntidad(String servicio) {
        if (servicio.contains("Reserva"))
            return "Reserva";
        if (servicio.contains("Cliente"))
            return "Cliente";
        if (servicio.contains("Habitacion"))
            return "Habitacion";
        if (servicio.contains("Empleado"))
            return "Empleado";
        if (servicio.contains("Servicio"))
            return "Servicio";
        if (servicio.contains("Descuento"))
            return "Descuento";
        if (servicio.contains("Resena"))
            return "Resena";
        return "Entidad";
    }

    private Long extraerId(Object result) {
        if (result == null)
            return null;
        try {
            if (result instanceof java.util.Optional) {
                Object value = ((java.util.Optional<?>) result).orElse(null);
                if (value != null) {
                    return (Long) value.getClass().getMethod("getId").invoke(value);
                }
            } else {
                return (Long) result.getClass().getMethod("getId").invoke(result);
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer ID del resultado", e);
        }
        return null;
    }

    private Long extraerIdDeArgs(Object[] args) {
        if (args == null || args.length == 0)
            return null;
        try {
            Object firstArg = args[0];
            if (firstArg instanceof Long) {
                return (Long) firstArg;
            } else if (firstArg != null) {
                Object id = firstArg.getClass().getMethod("getId").invoke(firstArg);
                if (id instanceof Long) {
                    return (Long) id;
                }
            }
        } catch (Exception e) {
            log.debug("No se pudo extraer ID de los argumentos", e);
        }
        return null;
    }
}
