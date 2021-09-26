package labs.pm.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public abstract class Product implements Rateable <Product>, Serializable{
	public static final BigDecimal DISCOUNT = BigDecimal.valueOf(0.1);
	private int id;
	private String name;
	private BigDecimal price;
	private Rating rating;
	
	public Product(int id, String name, BigDecimal price, Rating rating) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.rating = rating;
	}
	
	public LocalDate getBestBefore() {
		return LocalDate.now();
	}
	
	public Product() {
		this(0,"No name", BigDecimal.valueOf(0.0));
	}
	
	public Product(int id, String name, BigDecimal price) {
		this(id,name,price,Rating.NOT_RATED);
	}
	
	public int getId() {
		return id;
	}
	/*public void setId(int id) {
		this.id = id;
	}*/
	public String getName() {
		return name;
	}
	/*public void setName(String name) {
		this.name = name;
	}*/
	public BigDecimal getPrice() {
		return price.subtract(getDiscount());
	}
	/*public void setPrice(BigDecimal price) {
		this.price = price;
	}*/
	
	public BigDecimal getDiscount() {
		return price.multiply(DISCOUNT).setScale(2, RoundingMode.HALF_UP);
	}
	
	public Rating getRating() {
		return rating;
	}
	
//	public abstract Product appRating(Rating newRate);
//	{
//		return new Product(id, name, price, newRate);
//	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getId() + " " + getName() + " " + getPrice() + " " + getRating().getStars();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(this == obj)
			return true;
		if(obj instanceof Product) {
			final Product other = (Product) obj;
			return this.id == other.id;// && Objects.equals(this.name, other.name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int hash = 5;
		hash = 24 * hash + this.id;
		return hash;
	}

}
