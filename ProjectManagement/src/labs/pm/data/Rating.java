package labs.pm.data;

public enum Rating {
	NOT_RATED("\u2606\u2606\u2606\u2606"),
	ONE_RATED("\u2605\u2606\u2606\u2606"),
	TWO_RATED("\u2605\u2605\u2606\u2606"),
	THREE_RATED("\u2605\u2605\u2605\u2606"),
	FOUR_RATED("\u2605\u2605\u2605\u2605");
	
	private String star;
	private Rating(String star) {
		this.star = star;
	}
	
	public String getStars() {
		return star;
	}
	
}
