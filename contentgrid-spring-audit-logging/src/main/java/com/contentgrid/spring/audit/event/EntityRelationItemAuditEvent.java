package com.contentgrid.spring.audit.event;


import java.util.Locale;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EntityRelationItemAuditEvent extends AbstractEntityRelationAuditEvent {

    String relationId;
}
