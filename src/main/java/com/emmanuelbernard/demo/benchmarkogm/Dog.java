package com.emmanuelbernard.demo.benchmarkogm;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

/**
 * @author Emmanuel Bernard <emmanuel@hibernate.org>
 */
@Entity
@Indexed
public class Dog {
   @Id
   @GeneratedValue(strategy = GenerationType.TABLE, generator = "dog")
   @TableGenerator(
      name = "dog",
      table = "sequences",
      pkColumnName = "key",
      pkColumnValue = "dog",
      valueColumnName = "seed"
   )
   public Long getId() { return id; }
   public void setId(Long id) { this.id = id; }
   private Long id;

   @Field(analyze = Analyze.NO)
   public String getName() { return name; }
   public void setName(String name) { this.name = name; }
   private String name;

   @ManyToOne
   public Breed getBreed() { return breed; }
   public void setBreed(Breed breed) { this.breed = breed; }
   private Breed breed;
}

