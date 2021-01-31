package kushal.application.social.Models;

public class Notification {
    private String user_id;
    private String text;
    private String post_id;
    private Boolean is_post;


    public Notification() {
    }

    public Boolean getIs_post() {
        return is_post;
    }

    public void setIs_post(Boolean is_post) {
        this.is_post = is_post;
    }

    public Notification(String user_id, String text, String post_id, boolean is_post) {
        this.user_id = user_id;
        this.text = text;
        this.post_id = post_id;
        this.is_post = is_post;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }


}