package Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by miles on 2/16/16.
 */
public class User {
    private String userID;
    private String name;
    private String school;
    private ArrayList<Course> courses;

    public User (String userID, String name, String school) {
        this.userID = userID;
        this.name = name;
        this.school = school;
    }

    public User (String userID, String name, String school, ArrayList<Course> courses) {
        this.userID = userID;
        this.name = name;
        this.school = school;
        this.courses = courses;

    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public ArrayList<Course> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<Course> courses) {
        this.courses = courses;
    }
}
