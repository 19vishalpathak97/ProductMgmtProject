package labs.pm.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class ProductManager {
	private Map<Product, List<Review>> products = new HashMap<>();
	private static final Map <String,ResourceStorage> resources = Map.of("en-us",new ResourceStorage(new Locale("en","us")),
																	"en-in", new ResourceStorage(new Locale("en","in")),
																	"ru-RU", new ResourceStorage(new Locale("ru","RU"))
																	);
	private final Logger logger = Logger.getLogger(labs.pm.data.ProductManager.class.getName());
	private final ResourceBundle rbb = ResourceBundle.getBundle("labs.pm.data.Config");
	private final MessageFormat productf = new MessageFormat(rbb.getString("product.format"));
	private final MessageFormat reviewf = new MessageFormat(rbb.getString("reviews.format"));
	
	private final Path reportfolder = Path.of(rbb.getString("report.folder"));
	private final Path datafolder = Path.of(rbb.getString("data.folder"));
	private final Path tempfolder = Path.of(rbb.getString("temp.folder"));
	
	private static final ProductManager pm = new ProductManager();
	
	ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
	Lock readlock = rw.readLock();
	Lock writelock = rw.writeLock();
	
//	public ProductManager(Locale locale) {
//		this(locale.toLanguageTag());		
//	}
	
	private ProductManager() {
//		changeLocal(localtag);
		loadAllData();
	}
	
	public static ProductManager getInstance() {
		return pm;
	}
	
//	public void changeLocal(String tag) {
//		res = resources.getOrDefault(tag, resources.get("en-in"));
//	}
	
	public static Set<String> supportedlocals(){
		return resources.keySet();
	}

	public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestbefore)
	{
		Product product = null; 
		try{
			writelock.lock();
			product = new Food(id,name,price,rating,bestbefore);
			products.putIfAbsent(product, new ArrayList<>());
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING,"Writing Error" + e.getCause());
			return null;
		}
		finally {
			writelock.unlock();
		}
		return product;
	}
	
	public Product createProduct(int id, String name, BigDecimal price, Rating rating)
	{
		Product product = null;
		try{
			writelock.lock();
			product = new Drink(id,name,price,rating);
			products.putIfAbsent(product, new ArrayList<>());
		}catch(Exception e)
		{
			logger.log(Level.WARNING,"Writing Error" + e.getCause());
			return null;
		}
		finally {
			writelock.unlock();
		}
		return product;
	}
	
	public Product parseProduct(String text) {
		Product product = null;
		try {
			Object values[] = productf.parse(text);
			int id = Integer.parseInt((String)values[1]);
			String name = (String) values[2];
			BigDecimal price = BigDecimal.valueOf(Double.parseDouble((String)values[3]));
			Rating rating = Rateable.convert(Integer.parseInt((String)values[4]));
			String ptype = (String)values[0];
			
			switch(ptype) {
			case "D":
				product = new Drink(id, name, price, rating);
				break;
			case "F":
				LocalDate date = LocalDate.parse((String)values[5]);
				product = new Food(id, name, price, rating, date);
				break;
			}
		} catch (ParseException | NumberFormatException | DateTimeParseException  e) {
			logger.log(Level.WARNING,"Error occured due to : " + e.getCause());
		}
		return product;
	}
	
	public Product searchProduct(int id) throws ProductManagerException {
		try {
			readlock.lock();
			return products.keySet()
					   .stream()
					   .filter(p-> p.getId() == id)
					   .findFirst()
					   .orElseThrow(()->new ProductManagerException("Element" + id + "not found")); 
		}
		finally {
			readlock.unlock();
		}
	}
	
	public Product reviewProduct(int id, Rating r, String comment) {
		try {
			writelock.lock();
			return reviewProduct(searchProduct(id),r,comment);
		} catch (ProductManagerException e) {
			logger.log(Level.INFO,e.getMessage());
		}
		finally {
			writelock.unlock();
		}
		return null;
	}
	
	private Product reviewProduct(Product p, Rating r, String comment) {
		List <Review> reviews = products.get(p);
		products.remove(p, reviews);
		reviews.add(new Review(r, comment));
		int temp = (int) Math.round(reviews.stream().mapToInt(t->t.getRating().ordinal()).average().orElse(0));
		p = p.appRating(Rateable.convert(temp));
		products.put(p, reviews);
	    return p;
	}
	
	private Review parseReview(String text) {
		Review review = null;
		try {
			Object values[] = reviewf.parse(text);
			review = new Review(Rateable.convert(Integer.parseInt((String)values[1])),(String)values[2]);
		} catch (ParseException | NumberFormatException e) {
			logger.log(Level.WARNING, "Error occured : "+e.getMessage());
		}
		return review;
	}
	
	public void dumpData() {
		try {
			if(!Files.isDirectory(tempfolder)) {
				Files.createDirectory(tempfolder);
			}
			
			Path tempFile = tempfolder.resolve(MessageFormat.format(rbb.getString("temp.file"), Instant.now()));
			
			try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))){
				out.writeObject(products);
//				products = new HashMap<>();
			}
		}
		catch(IOException e)
		{
			logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
		}
	}
	
	public void restoreData() {
		try {
			Path tempfile = Files.list(tempfolder).filter(fn->fn.getFileName().toString().endsWith("tmp")).findFirst().orElseThrow();
			try(ObjectInputStream in = new ObjectInputStream(Files.newInputStream(tempfile, StandardOpenOption.READ))){
				try {
					products = (HashMap) in.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
				}
			}
		}
		catch(IOException e) {
			logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
		}
	}
	
	public void loadAllData() {
		try {
			products = Files.list(datafolder).filter(fn-> fn.getFileName().toString().startsWith("product"))
			.map(file -> loadProduct(file))
			.filter(product -> product!= null)
			.collect(Collectors.toMap(product -> product, product-> loadReviews(product)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
		}
	}
	
	public Product loadProduct(Path file) {
		Product p = null;
		try {
			p = parseProduct(Files.lines(datafolder.resolve(file),Charset.forName("UTF-8")).findFirst().orElseThrow());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
		}
		return p;
	}
	
	public List <Review> loadReviews(Product p){
		List <Review> reviews = null;
		Path file = datafolder.resolve(MessageFormat.format(rbb.getString("review.file"), p.getId()));
		if(Files.notExists(file)) {
			reviews = new ArrayList<>();
		}
		else {
			try {
				reviews = Files.lines(file, Charset.forName("UTF-8")).map(re->parseReview(re))
						.filter(review -> review != null)
						.collect(Collectors.toList());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.log(Level.SEVERE, "Error occured : "+e.getMessage());
			}
		}
		return reviews;
	}
	
	
	
	public void printProducts(Predicate<Product> filter,Comparator<Product> sorter, String languagetag) {
		try {
			readlock.lock();
			ResourceStorage res = resources.getOrDefault(languagetag, resources.get("en-us"));
			StringBuilder txt = new StringBuilder();
			products.keySet().stream().sorted(sorter).filter(filter).forEach(p-> txt.append(res.formatProducts(p) + "\n"));
			System.out.println(txt);
		}
		finally {
			readlock.unlock();
		}
	}
	
	public Map <String, String> getDiscounts(String languagetag){
		ResourceStorage res = resources.getOrDefault(languagetag, resources.get("en-us"));
		try {
			readlock.lock();
		return products
				.keySet()
				.stream()
				.collect(
				Collectors.groupingBy(p->p.getRating().getStars(),
						Collectors.collectingAndThen(Collectors.summingDouble(l->l.getDiscount().doubleValue()),
								dis->res.nf.format(dis))));
		}
		finally {
			readlock.unlock();
		}
	}
	
	public void printReport(int id, String languagetag, String client) {
		try {
			readlock.lock();
			printReport(searchProduct(id), languagetag, client);
		} catch (ProductManagerException e) {
			logger.log(Level.INFO,e.getMessage());
		} catch (IOException e) {
			logger.log(Level.INFO,e.getMessage());
		}
		finally {
			readlock.unlock();
		}
	}
	
	private void printReport(Product product,String languagetag, String client) throws IOException {
		ResourceStorage res = resources.getOrDefault(languagetag, resources.get("en-us"));
		List <Review> reviews = products.get(product);
		Path productFile = reportfolder.resolve(MessageFormat.format(rbb.getString("report.file"), product.getId(), client));
		try(PrintWriter out = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(productFile, StandardOpenOption.CREATE),"UTF-8"))){
			out.append(res.formatProducts(product) + System.lineSeparator());
			Collections.sort(reviews);
			if(reviews.isEmpty()) {
				out.append(res.formatText("notreviewed") + System.lineSeparator());
			}
			else {
				out.append(reviews.stream().map(r->res.formatReviews(r)).collect(Collectors.joining("\n")));
			}
			out.append(System.lineSeparator());
		}
	}
	
	private static class ResourceStorage{
		private Locale locale;
		private DateTimeFormatter datetimef;
		private ResourceBundle rb;
		private NumberFormat nf;
		
		private ResourceStorage(Locale locale) {
			this.locale = locale;
			this.datetimef = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(locale);
			this.rb = ResourceBundle.getBundle("labs.pm.data.message",locale);
			this.nf = NumberFormat.getCurrencyInstance(locale);
		}
		
		private String formatProducts(Product product) {
			//System.out.println(product.getPrice());
			return MessageFormat.format(rb.getString("product"),
					product.getName(),
					product.getPrice(),
					product.getRating().getStars(),
					product.getBestBefore());
		}
		
		private String formatReviews(Review review) {
			return MessageFormat.format(rb.getString("review"), review.getRating().getStars(),review.getComment());
		}
		
		private String formatText(String key) {
			return rb.getString(key);
		}
		
	}
	
}
