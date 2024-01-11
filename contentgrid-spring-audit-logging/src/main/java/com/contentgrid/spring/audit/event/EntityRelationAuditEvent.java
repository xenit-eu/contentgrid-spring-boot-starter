package com.contentgrid.spring.audit.event;


import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder(toBuilder = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EntityRelationAuditEvent extends AbstractEntityItemAuditEvent {
    String relationName;

    Operation operation;

    public enum Operation {
        READ,
        UPDATE,
        CLEAR
    }

}
