package com.wishlist.nathan.infrastructure.observability;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Loga a execução de TODOS os casos de uso (entrada/saída + duração + argumentos),
 * para observabilidade nos logs (ex.: Railway). Os argumentos incluem nome/telefone
 * quando presentes nos comandos — por decisão do dono do projeto, esses logs são só
 * para operação. Não logamos bytes (uploads/planilhas) nem senhas.
 */
@Aspect
@Component
public class LogCasosDeUso {

    private static final Logger log = LoggerFactory.getLogger("usecase");
    private static final int MAX_ARG = 300;

    @Around("execution(public * com.wishlist.nathan.application.usecase..*.executar(..))")
    public Object logar(ProceedingJoinPoint pjp) throws Throwable {
        String caso = pjp.getSignature().getDeclaringType().getSimpleName();
        String args = descreverArgs(pjp.getArgs());
        long inicio = System.nanoTime();
        try {
            Object resultado = pjp.proceed();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            log.info("{} OK ({}ms) {}", caso, ms, args);
            return resultado;
        } catch (Throwable e) {
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            // WARN (não ERROR): o tratador global já loga em ERROR o que vira 500.
            log.warn("{} FALHOU ({}ms) {} -> {}: {}", caso, ms, args,
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }

    private static String descreverArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return "args=" + Arrays.stream(args).map(LogCasosDeUso::descrever)
                .collect(Collectors.joining(" | ", "[", "]"));
    }

    private static String descrever(Object a) {
        if (a == null) {
            return "null";
        }
        if (a instanceof byte[] b) {
            return "bytes(" + b.length + ")"; // não despeja conteúdo de upload/planilha
        }
        if (a instanceof char[] c) {
            return "chars(" + c.length + ")"; // possível senha — não logar conteúdo
        }
        if (a.getClass().isRecord()) {
            return descreverRecord(a); // mascara senha/token campo a campo
        }
        String s = String.valueOf(a);
        return s.length() > MAX_ARG ? s.substring(0, MAX_ARG) + "…" : s;
    }

    /** Monta o record campo a campo, mascarando credenciais (senha/token/secret). */
    private static String descreverRecord(Object r) {
        var comps = r.getClass().getRecordComponents();
        StringBuilder sb = new StringBuilder(r.getClass().getSimpleName()).append("[");
        for (int i = 0; i < comps.length; i++) {
            String campo = comps[i].getName();
            sb.append(campo).append("=");
            if (sensivel(campo)) {
                sb.append("***");
            } else {
                Object valor;
                try {
                    valor = comps[i].getAccessor().invoke(r);
                } catch (ReflectiveOperationException e) {
                    valor = "?";
                }
                sb.append(descrever(valor));
            }
            if (i < comps.length - 1) {
                sb.append(", ");
            }
        }
        return sb.append("]").toString();
    }

    /** Nunca logamos esses campos, mesmo com PII liberada (são credenciais). */
    private static boolean sensivel(String campo) {
        String n = campo.toLowerCase();
        return n.contains("senha") || n.contains("password")
                || n.contains("token") || n.contains("secret");
    }
}
