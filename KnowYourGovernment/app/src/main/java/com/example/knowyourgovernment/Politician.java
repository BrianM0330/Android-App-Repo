package com.example.knowyourgovernment;

public class Politician {
    private String name;
    private String party;
    private String officePosition;
    private String imageURL;
    private String address;
    private String phone;
    private String website;
    private String email;
    private String facebookID;
    private String twitterID;
    private String youtubeID;

    Politician(String name, String party, String officePosition) {
        this.name = name;
        this.party = party;
        this.officePosition = officePosition;
    }

     Politician(String name,
                String party,
                String officePosition,
                String imageURL,
                String address,
                String phone,
                String website,
                String email,
                String facebookID,
                String twitterID,
                String youtubeID) {
        this.name = name;
        this.party = party;
        this.officePosition = officePosition;
        this.imageURL = imageURL;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.email = email;
        this.facebookID = facebookID;
        this.twitterID = twitterID;
        this.youtubeID = youtubeID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public String getTwitterID() {
        return twitterID;
    }

    public void setTwitterID(String twitterID) {
        this.twitterID = twitterID;
    }

    public String getYoutubeID() {
        return youtubeID;
    }

    public void setYoutubeID(String youtubeID) {
        this.youtubeID = youtubeID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public void setOfficePosition(String officePosition) {
        this.officePosition = officePosition;
    }

    String getName() {return this.name;}

    String getParty() {return this.party;}

    String getOfficePosition() {return this.officePosition;}
}
