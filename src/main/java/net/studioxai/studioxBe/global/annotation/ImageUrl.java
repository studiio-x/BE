package net.studioxai.studioxBe.global.annotation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.studioxai.studioxBe.global.aop.ImageUrlSerializerAop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JsonSerialize(using = ImageUrlSerializerAop.class)
public @interface ImageUrl {
}
