package com.contentgrid.userapps.holmes.dcm.model;

import java.lang.String;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Person {
	@Id
	@Generate
	ate UUID id;

	ng name;

	private String notes;
}}}}
