package labs.pm.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Food extends Product{
	private LocalDate bestbefore;

	Food(int id, String name, BigDecimal price, Rating rating, LocalDate bestbefore) {
		super(id, name, price, rating);
		this.bestbefore = bestbefore;
	}
	
	public LocalDate getBestBefore() {
		return bestbefore;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + " from Child " + bestbefore;
	}

	@Override
	public BigDecimal getDiscount() {
		// TODO Auto-generated method stub
		return (bestbefore.equals(LocalDate.now())) ? super.getDiscount() : BigDecimal.ZERO;
	}

	@Override
	public Product appRating(Rating newRate) {
		// TODO Auto-generated method stub
		return new Food(this.getId(), this.getName(), this.getPrice(), newRate , LocalDate.now());
	}
	
	
	
}
