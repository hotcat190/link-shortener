package com.example.linkshortener.sharding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method parameter as the sharding key.
 * The Aspect will extract this value and set it in ShardingContext
 * before the method executes.
 * 
 * Usage:
 * <pre>
 * Optional<Data> findByShortenedUrl(@ShardingKey String shortenedUrl);
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShardingKey {
}
