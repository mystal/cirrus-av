package cirrus.common;

public class Time {
	public long start;
	public long end;

	public Time() {
		start = 0;
		end = 0;
	}

	public Time(long start, long end) {
		this.start = start;
		this.end = end;
	}

	public long getTotal() {
		return end - start;
	}
}
