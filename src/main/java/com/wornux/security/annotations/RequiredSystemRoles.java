package com.wornux.security.annotations;

import com.wornux.data.enums.SystemRole;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify required system roles for accessing a view or component.
 * Can be applied to classes to enforce role-based access control.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredSystemRoles {
    
    /**
     * The system roles that are allowed to access the annotated component.
     * 
     * @return Array of allowed system roles
     */
    SystemRole[] value();
    
    /**
     * Whether all specified roles are required (AND logic) or any role is sufficient (OR logic).
     * Default is false (OR logic - any role is sufficient).
     * 
     * @return true if all roles are required, false if any role is sufficient
     */
    boolean requireAll() default false;
    
    /**
     * Custom message to display when access is denied.
     * 
     * @return Custom access denied message
     */
    String accessDeniedMessage() default "";
}