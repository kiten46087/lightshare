package edu.neu.madcourse.share.Model;

public class Post {
    private String postID;
    private String postIMG;
    private String postContent;
    private String title;
    private String authorID;
    private String community;

    public Post(){

    }

    public Post(String postID, String postIMG, String postContent, String title, String author) {
        this.postID = postID;
        this.postIMG = postIMG;
        this.postContent = postContent;
        this.title = title;
        this.authorID = author;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String communityID) {
        this.community = communityID;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getPostIMG() {
        return postIMG;
    }

    public void setPostIMG(String postIMG) {
        this.postIMG = postIMG;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String author) {
        this.authorID = author;
    }
}
