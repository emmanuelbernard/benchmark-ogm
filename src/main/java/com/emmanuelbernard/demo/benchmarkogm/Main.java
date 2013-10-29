package com.emmanuelbernard.demo.benchmarkogm;

import org.hibernate.Query;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author Emmanuel Bernard <emmanuel@hibernate.org>
 */
public class Main {

	/**
	 * To use jClarity's Censum:
	 * -Xmx2G -Xms2G -XX:MaxPermSize=128M -XX:+HeapDumpOnOutOfMemoryError -Xss512k -XX:HeapDumpPath=/tmp/java_heap -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 -Xbatch -server -XX:+UseLargePages -XX:LargePageSizeInBytes=2m -XX:+AlwaysPreTouch -Xloggc:gc-full.log -XX:+PrintGCDetails -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime
	 *
	 * To use HPJmeter:
	 * -Xmx2G -Xms2G -XX:MaxPermSize=128M -XX:+HeapDumpOnOutOfMemoryError -Xss512k -XX:HeapDumpPath=/tmp/java_heap -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 -Xbatch -server -XX:+UseLargePages -XX:LargePageSizeInBytes=2m -XX:+AlwaysPreTouch -Xloggc:gc-simple.log
	 *
	 * To use Flight Recorder:
	 * -Xmx2G -Xms2G -XX:MaxPermSize=128M -XX:+HeapDumpOnOutOfMemoryError -Xss512k -XX:HeapDumpPath=/tmp/java_heap -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 -Xbatch -server -XX:+UseLargePages -XX:LargePageSizeInBytes=2m -XX:+AlwaysPreTouch -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,disk=true,dumponexit=true,dumponexitpath=/tmp
	 */

	private static final int LOOPS = 10000000;
	private static final int OUTPUT_EACH = 200;

	public static void main(String[] args) throws Exception {
		System.out.println( "Test me" );

		TransactionManager tm = getTransactionManager();

		//build the EntityManagerFactory as you would build in in Hibernate Core
		EntityManagerFactory emf = Persistence.createEntityManagerFactory( "ogm-jpa-tutorial" );
		Random random = new Random( 17 );
		long counter = 0;

		long startime = System.nanoTime();
		//Persist entities the way you are used to in plain JPA
		try {

			while (counter < LOOPS) {
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

				if (counter % OUTPUT_EACH == 0) {
					long afterTime = System.nanoTime();
					long millis = TimeUnit.MILLISECONDS.convert( afterTime - startime, TimeUnit.NANOSECONDS );
					long txPerSecond = (OUTPUT_EACH *1000 / millis);
					System.out.println( "I'm alive sill, and counter is at " + counter + ". Milliseconds from last check:" + millis + "\t TX/second: " + txPerSecond);
					startime = afterTime;
				}

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
