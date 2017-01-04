package com.example.android.lifehacker;

public class Entry {
	private String header = "", summary = "",
			datePosted = "", link = "", imgURL = "";

	Entry() {

	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDatePosted() {
		return datePosted;
	}

	public void setDatePosted(String datePosted) {
		this.datePosted = datePosted;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getImgURL() {
		return imgURL;
	}

	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}

	public String toString() {
		return ("com.example.android.lifehacker.Entry title: " + header + "\nSummary: " + summary + "\nDate Posted: " + datePosted
				+ "\nLink: " + link + "\nImage URL: " + imgURL);
	}
}