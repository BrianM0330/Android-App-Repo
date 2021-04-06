package com.example.rewards;

public class Profile {
    private String firstName;
    private String lastName;
    private String userName;
    private String department;
    private String bio;
    private String position;
    private String remainingPointsToAward;
    private String location;
    private String pointsAwarded;
    private String imageBytes;

    public Profile(String firstName, String lastName, String userName, String department, String bio, String position, String imageBytes, String points) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.department = department;
        this.bio = bio;
        this.position = position;
        this.imageBytes = imageBytes;
        this.pointsAwarded = points;
    }


    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFullName() {
        return String.format("%s, %s", getLastName(), getFirstName());
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRemainingPointsToAward() {
        return remainingPointsToAward;
    }

    public void setRemainingPointsToAward(String remainingPointsToAward) {
        this.remainingPointsToAward = remainingPointsToAward;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(String pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public String getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(String imageBytes) {
        this.imageBytes = imageBytes;
    }
}
