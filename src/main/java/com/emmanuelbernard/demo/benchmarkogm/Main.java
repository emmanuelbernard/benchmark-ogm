package com.emmanuelbernard.demo.benchmarkogm;

import org.hibernate.Query;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import java.util.Random;

/**
 * @author Emmanuel Bernard <emmanuel@hibernate.org>
 */
public class Main {
	public static void main(String[] args) {
		System.out.println( "Test me" );

		TransactionManager tm = null;
		try {
			tm = getTransactionManager();
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		//build the EntityManagerFactory as you would build in in Hibernate Core
		EntityManagerFactory emf = Persistence.createEntityManagerFactory( "ogm-jpa-tutorial" );
		Random random = new Random(  );
		long counter = 0;

		//Persist entities the way you are used to in plain JPA
		try {

			while (counter < 1000) {
				tm.begin();
				EntityManager em = emf.createEntityManager();
				Breed collie = new Breed();
				collie.setName( "Collie" + random.nextInt() );
				em.persist( collie );
				Dog dina = new Dog();
				dina.setName( "Dina" + random.nextInt() );
				dina.setBreed( collie );
				em.persist( dina );
				Long dinaId = dina.getId();
				em.flush();
				em.close();
				tm.commit();

				//Retrieve your entities the way you are used to in plain JPA
				tm.begin();
				em = emf.createEntityManager();
				dina = em.find( Dog.class, dinaId );
				if ( dina.getBreed().toString().length() > 20 ) {
					counter++;
				}
				Query query = em.unwrap( Session.class ).createQuery( "from Dog d where d.name LIKE 'Dina%'" );
				query.setFirstResult( 0 ).setMaxResults( 100 );
				if ( query.list().size() > 20 ) {
					counter++;
				}
				em.flush();
				em.close();
				tm.commit();

			}
		} catch ( Exception e ) {
			e.printStackTrace();
		}
		System.out.println( counter );
		emf.close();
	}

	private static final String JBOSS_TM_CLASS_NAME = "com.arjuna.ats.jta.TransactionManager";

	public static TransactionManager getTransactionManager() throws Exception {
	    Class<?> tmClass = Main.class.getClassLoader().loadClass(JBOSS_TM_CLASS_NAME);
	    return (TransactionManager) tmClass.getMethod("transactionManager").invoke(null);
	}
}
