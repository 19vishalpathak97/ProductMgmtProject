package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalTime;

public class Drink extends Product{
	public Drink() {
			
	}

	Drink(int id, String name, BigDecimal price, Rating rating) {
		super(id, name, price, rating);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public BigDecimal getDiscount() {
		// TODO Auto-generated method stub
		LocalTime lt = LocalTime.now();
		return (lt.isAfter(LocalTime.of(19, 00))) && (lt.isBefore(LocalTime.of(22, 00))) ? super.getDiscount() : BigDecimal.ZERO;
	}

	@Override
	public Product appRating(Rating newRate) {
		// TODO Auto-generated method stub
		return new Drink(this.getId(), this.getName(),this.getPrice(),newRate);
	}
	
	
}
