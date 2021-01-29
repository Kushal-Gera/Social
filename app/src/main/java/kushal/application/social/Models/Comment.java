package kushal.application.social.Models;

public class Comment {

    private String user_id;
    private String id;
    private String comment;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public Comment() {
    }

    public Comment(String user_id, String comment) {
        this.user_id = user_id;
        this.comment = comment;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
