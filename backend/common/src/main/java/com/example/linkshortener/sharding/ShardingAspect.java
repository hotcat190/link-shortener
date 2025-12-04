package com.example.linkshortener.sharding;

import com.example.linkshortener.data.entity.Data;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Aspect that intercepts method calls and sets the sharding context
 * based on parameters annotated with @ShardingKey.
 */
@Aspect
@Component
@Order(1) // Execute before transaction aspects
public class ShardingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ShardingAspect.class);

    /**
     * Intercepts all methods in Repository classes ONLY.
     * Service layer is excluded to avoid intercepting methods before data is ready.
     */
    @Around("execution(* com.example.linkshortener.data.repository..*(..))")
    public Object routeToShard(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        System.out.println("=== ShardingAspect INTERCEPTED: " + joinPoint.getTarget().getClass().getSimpleName() + "." + methodName + " ===");
        
        String shardingKey = extractShardingKey(joinPoint);
        
        System.out.println("=== Aspect - Extracted shardingKey: " + shardingKey + " ===");
        
        if (shardingKey != null) {
            ShardingContext.setShardingKey(shardingKey);
            ClientDatabase targetShard = ShardingContext.determineTargetShard();
            System.out.println("=== Aspect - Routing to: " + targetShard + " for key: " + shardingKey + " ===");
            logger.info("Routing to {} for key: {}", targetShard, shardingKey);
        } else {
            System.out.println("=== Aspect - WARNING: No sharding key found, will use default shard! ===");
            logger.warn("No sharding key found for method: {}, will use default shard", methodName);
        }

        try {
            return joinPoint.proceed();
        } finally {
            ShardingContext.clear();
        }
    }

    /**
     * Extracts the sharding key from method parameters.
     * Strategy:
     * 1. First, check for @ShardingKey annotation on parameters (including interface methods)
     * 2. Fallback: For save() methods, auto-extract from Data entity
     */
    private String extractShardingKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String methodName = method.getName();
        
        System.out.println("=== extractShardingKey - Method: " + methodName + ", Args count: " + args.length + " ===");
        
        // Strategy 1: Check annotation on proxy method
        String key = extractFromAnnotation(method, args);
        if (key != null) {
            System.out.println("=== extractShardingKey - Found via annotation: " + key + " ===");
            return key;
        }
        
        // Strategy 2: Check annotation on interface method (Spring Data JPA proxy issue)
        key = extractFromInterfaceAnnotation(joinPoint, args);
        if (key != null) {
            System.out.println("=== extractShardingKey - Found via interface annotation: " + key + " ===");
            return key;
        }
        
        // Strategy 3: Fallback for save() - auto-extract from Data entity
        if (methodName.equals("save") && args.length > 0 && args[0] instanceof Data) {
            Data data = (Data) args[0];
            String shortenedUrl = data.getShortenedUrl();
            System.out.println("=== extractShardingKey - Fallback save() - ShortenedURL: " + shortenedUrl + " ===");
            if (shortenedUrl != null && !shortenedUrl.isEmpty()) {
                return shortenedUrl;
            }
            System.out.println("=== extractShardingKey - WARNING: Data.shortenedUrl is NULL or EMPTY! ===");
        }
        
        return null;
    }
    
    /**
     * Extract sharding key from @ShardingKey annotation on method parameters.
     */
    private String extractFromAnnotation(Method method, Object[] args) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        
        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof ShardingKey) {
                    Object arg = args[i];
                    if (arg != null) {
                        return extractKeyFromObject(arg);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Extract sharding key from interface method annotations.
     * Spring Data JPA creates proxy classes, annotations on interface methods
     * are not visible on the proxy method directly.
     */
    private String extractFromInterfaceAnnotation(ProceedingJoinPoint joinPoint, Object[] args) {
        try {
            Class<?>[] interfaces = joinPoint.getTarget().getClass().getInterfaces();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();
            Class<?>[] paramTypes = signature.getMethod().getParameterTypes();
            
            for (Class<?> iface : interfaces) {
                try {
                    Method interfaceMethod = iface.getMethod(methodName, paramTypes);
                    String key = extractFromAnnotation(interfaceMethod, args);
                    if (key != null) {
                        return key;
                    }
                } catch (NoSuchMethodException ignored) {
                    // Method not found in this interface, try next
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting from interface: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extracts the actual sharding key value from an object.
     * If the object is a Data entity, extracts the shortenedUrl field.
     * Otherwise, uses toString().
     */
    private String extractKeyFromObject(Object arg) {
        if (arg instanceof Data) {
            Data data = (Data) arg;
            String shortenedUrl = data.getShortenedUrl();
            System.out.println("=== extractKeyFromObject - Data entity, shortenedUrl: " + shortenedUrl + " ===");
            if (shortenedUrl != null && !shortenedUrl.isEmpty()) {
                return shortenedUrl;
            }
            System.out.println("=== extractKeyFromObject - WARNING: shortenedUrl is NULL/EMPTY ===");
            return null;
        }
        // For String or other simple types
        return arg.toString();
    }
}