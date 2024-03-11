package com.contentgrid.spring.data.support.auditing.v1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Embeddable;
import java.time.Instant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AuditMetadata {

    @CreatedBy
    @JsonProperty(value = "created_by")
    private UserMetadata createdBy;

    @CreatedDate
    @JsonProperty(value = "created_date")
    private Instant createdDate;

    @LastModifiedBy
    @JsonProperty(value = "last_modified_by")
    private UserMetadata lastModifiedBy;

    @LastModifiedDate
    @JsonProperty(value = "last_modified_date")
    private Instant lastModifiedDate;
}
