package labs.pm.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import labs.pm.data.*;

public class Shop {
	public static void main(String[] args) {
		//Product p0 = new Product();
//		Locale l = Locale.US;
		ProductManager pm = ProductManager.getInstance();
		
		AtomicInteger clientcount = new AtomicInteger(0);
		
		
		Callable<String> client = () -> {
			String clientid = "Client" + clientcount.getAndIncrement();
			String threadname = Thread.currentThread().getName();
			int productid = ThreadLocalRandom.current().nextInt(2)+1;
			String languagetag = ProductManager.supportedlocals()
												.stream()
												.skip(ThreadLocalRandom.current().nextInt(2))
												.findFirst().get();
			StringBuilder logs = new StringBuilder();
			logs.append("\n" + clientid + " " + threadname + "\t Start of log\n");
			logs.append(pm.getDiscounts(languagetag).entrySet()
					.stream()
					.map(entry-> entry.getKey() + "\t" + entry.getValue() + "\n")
					.collect(Collectors.joining("\n"))					
					);
			Product product = pm.reviewProduct(productid, Rating.FOUR_RATED, "Will add another review");
			logs.append(product!=null ? "Reviewed id "+ productid :"Not Reviewed" + productid);
			pm.printReport(productid, languagetag, clientid);
			
			logs.append("\n" + clientid + " " + threadname + "\t End of log");
			return logs.toString();
		};
		
		List<Callable<String>> clients = Stream.generate(()->client).limit(3).collect(Collectors.toList());
		ExecutorService es = Executors.newFixedThreadPool(2);
		try {
			List<Future<String>> result = es.invokeAll(clients);
			es.shutdown();
			result.stream().forEach(r -> {
				try {
					System.out.println(r.get());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error in handling : " + e);
		}
		
		
//		pm.dumpData();
//		pm.restoreData();
		
//		pm.printReport(1, "en-us", "hh");
		
//		pm.createProduct(2,"Milk",BigDecimal.valueOf(100),Rating.FOUR_RATED);
//		pm.parseProduct("D,2,Cake,1.99,4,2021-04-02");
//		pm.parseReview("2,3, Very nice Product");
//		pm.reviewProduct(2,Rating.THREE_RATED, "Very nice Product");
//		pm.reviewProduct(2,Rating.FOUR_RATED, "MC sala");
//		pm.reviewProduct(2,Rating.TWO_RATED, "BC Sala"); 
		
//		pm.printReport(2, "en-in");
//		
//		pm.createProduct(1,"Biscuit",BigDecimal.valueOf(200),Rating.FOUR_RATED,LocalDate.now().plusDays(2));
//		pm.reviewProduct(1,Rating.ONE_RATED, "Very nice Product");
//		pm.reviewProduct(1,Rating.ONE_RATED, "MC sala");
//		pm.reviewProduct(1,Rating.ONE_RATED, "BC Sala");
//		
//		pm.printReport(1);
		
//		Comparator <Product> rsort = (p1,p2) -> p1.getRating().ordinal()- p2.getRating().ordinal();
//		Comparator <Product> psort = (p1,p2) -> p1.getPrice().compareTo(p2.getPrice());
		//pm.printProducts(rsort.thenComparing(psort));
		//pm.printProducts(rsort.thenComparing(psort).reversed());
		
//		pm.getDiscounts().forEach((rating, dis)-> System.out.println(rating + "\t" + dis));
//		
//		pm.printProducts(p->p.getPrice().floatValue() > 100, psort);
		
		/*Product p2 = pm.createProduct(2,"Biscuit",BigDecimal.valueOf(200),Rating.FOUR_RATED, LocalDate.now().plusDays(2));
		Product p3 = pm.createProduct(2,"Biscuit",BigDecimal.valueOf(200),Rating.FOUR_RATED);
		Object p4 = p1.appRating(Rating.THREE_RATED);
	    //System.out.println(((Food)p1).getBestBefore());
		Product p5 = p2.appRating(Rating.ONE_RATED);
		Product p6 = p3.appRating(Rating.ONE_RATED);
		
		
		
	    System.out.println(p1.equals(p2));
		
		/*System.out.println(p1.getId() + " " + p1.getName() + " " + p1.getPrice() + " " + p1.getRating().getStars());
		System.out.println(p2.getId() + " " + p2.getName() + " " + p2.getPrice() + " " + p2.getRating().getStars());
		System.out.println(p0.getId() + " " + p0.getName() + " " + p0.getPrice() + " " + p0.getRating().getStars());
		System.out.println(p3.getId() + " " + p3.getName() + " " + p3.getPrice() + " " + p3.getRating().getStars());
		*/
		//System.out.println(p0);
		/*System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(p4);
		System.out.println(p5);
		System.out.println(p6);*/
		
		//System.out.println("hello");
		
		//System.out.println("Rating is : "+p1.getRating().getStars());
	}
}
