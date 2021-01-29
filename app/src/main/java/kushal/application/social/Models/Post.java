package kushal.application.social.Models;

public class Post {

    private String description;
    private String image_url;
    private String post_id;
    private String user_id;

    public Post() {
    }

    public Post(String description, String image_url, String post_id, String user_id) {
        this.description = description;
        this.image_url = image_url;
        this.post_id = post_id;
        this.user_id = user_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
