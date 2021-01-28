package kushal.application.social.Models;

public class User {

    private String name;
    private String email;
    private String bio;
    private String image_url;
    private String id;

    public User() {
    }

    public User(String name, String email, String bio, String image_url, String id) {
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.image_url = image_url;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
