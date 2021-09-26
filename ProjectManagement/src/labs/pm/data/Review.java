package labs.pm.data;

import java.io.Serializable;

public class Review implements Comparable<Review>, Serializable{
	private Rating rating;
	private String comment;
	
	public Review(Rating rating, String comment) {
		this.rating = rating;
		this.comment = comment;
	}

	public Rating getRating() {
		return rating;
	}

	

	public String getComment() {
		return comment;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Reviewed rating : " + getRating() + " and comment is : "+ getComment();
	}

	@Override
	public int compareTo(Review other) {
		// TODO Auto-generated method stub
		return other.getRating().ordinal()-this.getRating().ordinal();
	}

	
	
}
