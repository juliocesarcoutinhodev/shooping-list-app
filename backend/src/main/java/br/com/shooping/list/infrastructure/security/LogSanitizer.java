package br.com.shooping.list.infrastructure.security;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * Utilitário para sanitizar objetos antes de logar.
 * <p>
 * Remove ou mascara campos marcados com @Sensitive para evitar
 * vazamento de informações sensíveis em logs.
 * <p>
 * Uso:
 * <pre>
 * log.info("Request recebido: {}", LogSanitizer.sanitize(request));
 * </pre>
 */
@Slf4j
public final class LogSanitizer {

    private static final String MASKED_VALUE = "***REDACTED***";

    private LogSanitizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Sanitiza um objeto, mascarando campos sensíveis.
     * <p>
     * Retorna uma representação String segura do objeto, com campos
     * marcados com @Sensitive substituídos por "***REDACTED***".
     *
     * @param obj objeto a ser sanitizado
     * @return representação string segura
     */
    public static String sanitize(Object obj) {
        if (obj == null) {
            return "null";
        }

        Class<?> clazz = obj.getClass();
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.getSimpleName()).append("{");

        Field[] fields = clazz.getDeclaredFields();
        boolean first = true;

        for (Field field : fields) {
            // Pula campos estáticos ou sintéticos (gerados pelo compilador)
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            if (!first) {
                sb.append(", ");
            }
            first = false;

            field.setAccessible(true);
            sb.append(field.getName()).append("=");

            try {
                if (field.isAnnotationPresent(Sensitive.class)) {
                    // Campo sensível: mascara o valor
                    sb.append(MASKED_VALUE);
                } else {
                    // Campo normal: exibe o valor
                    Object value = field.get(obj);
                    sb.append(value);
                }
            } catch (IllegalAccessException e) {
                sb.append("???");
                log.warn("Erro ao acessar campo {} para sanitização", field.getName());
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Mascara parcialmente um token, mostrando apenas os primeiros e últimos caracteres.
     * <p>
     * Exemplo: "abc123def456" -> "abc...456"
     * <p>
     * Útil para logs de debug onde é necessário identificar qual token
     * sem expor o valor completo.
     *
     * @param token token a ser mascarado
     * @return token parcialmente mascarado
     */
    public static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return MASKED_VALUE;
        }

        int visibleChars = 3;
        String prefix = token.substring(0, visibleChars);
        String suffix = token.substring(token.length() - visibleChars);

        return prefix + "..." + suffix;
    }

    /**
     * Mascara completamente um valor sensível.
     * <p>
     * Retorna sempre "***REDACTED***" independente do valor.
     *
     * @param value valor a ser mascarado
     * @return valor mascarado
     */
    public static String mask(Object value) {
        return MASKED_VALUE;
    }
}

